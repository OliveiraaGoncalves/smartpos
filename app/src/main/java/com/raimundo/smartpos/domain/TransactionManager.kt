package com.raimundo.smartpos.domain

import com.raimundo.smartpos.data.journal.TransactionJournal
import com.raimundo.smartpos.domain.model.PaymentType
import com.raimundo.smartpos.domain.model.TerminalConfig
import com.raimundo.smartpos.domain.model.Transaction
import com.raimundo.smartpos.domain.model.TransactionStatus
import com.raimundo.smartpos.domain.model.UndoReason
import com.raimundo.smartpos.printer.PrintQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/** O que a tela de venda precisa: disparar uma venda e seguir em frente. */
interface SaleLauncher {
    /** @return false se o transactionId já foi usado (chamada duplicada é ignorada). */
    fun startSale(transactionId: String, amountCents: Long, paymentType: PaymentType): Boolean
}

/**
 * Orquestra várias transações em paralelo, fora da UI:
 *  - cada venda roda na sua própria coroutine no escopo da aplicação (sobrevive à
 *    navegação e à rotação de tela; a UI nunca espera);
 *  - expõe o estado de todas as transações como Flow;
 *  - envia para a fila de impressão quando cada uma termina;
 *  - na inicialização, desfaz transações que ficaram pela metade e retenta
 *    desfazimentos pendentes periodicamente.
 */
class TransactionManager(
    private val processor: TransactionProcessor,
    private val journal: TransactionJournal,
    private val printQueue: PrintQueue,
    private val terminal: TerminalConfig,
    private val scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
    private val pendingUndoRetryIntervalMs: Long = 30_000,
) : SaleLauncher {

    private val state = MutableStateFlow<Map<String, Transaction>>(emptyMap())
    private val active: MutableSet<String> = ConcurrentHashMap.newKeySet<String>()
    @Volatile private var started = false

    val transactions: Flow<List<Transaction>> =
        state.map { all -> all.values.sortedByDescending(Transaction::createdAtMillis) }

    fun start() {
        if (started) return
        started = true
        scope.launch {
            val stored = journal.loadAll()
            state.update { current -> stored.associateBy(Transaction::id) + current }
            stored.forEach { tx ->
                when {
                    !tx.status.isFinal -> recover(tx)
                    !tx.receiptPrinted -> print(tx)
                }
            }
            while (isActive) {
                delay(pendingUndoRetryIntervalMs)
                state.value.values
                    .filter { it.status == TransactionStatus.DESFAZIMENTO_PENDENTE }
                    .forEach(::recover)
            }
        }
    }

    override fun startSale(transactionId: String, amountCents: Long, paymentType: PaymentType): Boolean {
        require(amountCents > 0) { "Valor deve ser maior que zero" }
        if (state.value.containsKey(transactionId) || !active.add(transactionId)) return false

        val now = clock()
        val transaction = Transaction(
            id = transactionId,
            amountCents = amountCents,
            paymentType = paymentType,
            merchantId = terminal.merchantId,
            terminalId = terminal.terminalId,
            status = TransactionStatus.CRIADA,
            createdAtMillis = now,
            updatedAtMillis = now,
        )
        publish(transaction)

        scope.launch {
            try {
                finish(processor.process(transaction, ::publish))
            } finally {
                active.remove(transactionId)
            }
        }
        return true
    }

    private fun recover(transaction: Transaction) {
        if (!active.add(transaction.id)) return
        scope.launch {
            try {
                val reason = transaction.undoReason ?: UndoReason.RECUPERACAO
                finish(processor.undo(transaction, reason, ::publish))
            } finally {
                active.remove(transaction.id)
            }
        }
    }

    private fun finish(transaction: Transaction) {
        if (transaction.status.isFinal) print(transaction)
    }

    private fun print(transaction: Transaction) {
        printQueue.enqueue(transaction) { printed ->
            val updated = (state.value[printed.id] ?: printed).copy(receiptPrinted = true)
            journal.save(updated)
            publish(updated)
        }
    }

    private fun publish(transaction: Transaction) {
        state.update { it + (transaction.id to transaction) }
    }
}
