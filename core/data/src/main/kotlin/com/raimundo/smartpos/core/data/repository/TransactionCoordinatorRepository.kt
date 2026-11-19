package com.raimundo.smartpos.core.data.repository

import com.raimundo.smartpos.core.common.ApplicationScope
import com.raimundo.smartpos.core.common.Clock
import com.raimundo.smartpos.core.domain.engine.TransactionEngine
import com.raimundo.smartpos.core.domain.journal.TransactionJournal
import com.raimundo.smartpos.core.domain.repository.TransactionRepository
import com.raimundo.smartpos.core.localstorage.db.TransactionDao
import com.raimundo.smartpos.core.localstorage.db.TransactionEventDao
import com.raimundo.smartpos.core.localstorage.db.toDomain
import com.raimundo.smartpos.core.localstorage.db.toEntity
import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionEvent
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.model.UndoReason
import com.raimundo.smartpos.core.printer.PrintQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Substitui o `TransactionManager` do protótipo: orquestra várias transações em paralelo,
 * fora da UI, usando o Room como fonte única de verdade (os `Flow` de leitura já refletem
 * cada `journal.save()` feito pelo [TransactionEngine]).
 *
 *  - cada venda roda na sua própria coroutine no [appScope] (sobrevive à navegação);
 *  - ao concluir, envia para a [PrintQueue];
 *  - [recoverOnOpen] desfaz transações órfãs e imprime comprovantes pendentes;
 *  - [TransactionRepository.retryPendingUndo] é chamado a cada 30 s (ver [startPeriodicRetry])
 *    enquanto o app está aberto, e por WorkManager quando fechado (ver `RetryPendingUndoWorker`).
 */
@Singleton
class TransactionCoordinatorRepository
    @Inject
    constructor(
        private val engine: TransactionEngine,
        private val journal: TransactionJournal,
        private val transactionDao: TransactionDao,
        private val eventDao: TransactionEventDao,
        private val printQueue: PrintQueue,
        private val terminal: TerminalConfigProvider,
        private val clock: Clock,
        @ApplicationScope private val appScope: CoroutineScope,
    ) : TransactionRepository {
        private val active: MutableSet<String> = ConcurrentHashMap.newKeySet()

        @Volatile private var started = false

        override val transactions: Flow<List<Transaction>> =
            transactionDao.observeAll().map { entities -> entities.map { it.toDomain() } }

        override fun observe(transactionId: String): Flow<Transaction?> {
            val entityFlow = transactionDao.observeById(transactionId)
            return entityFlow.map { it?.toDomain() }
        }

        override fun observeEvents(transactionId: String): Flow<List<TransactionEvent>> =
            eventDao.observeByTransaction(transactionId).map { events -> events.map { it.toDomain() } }

        /** Chamado uma vez pelo StartupGate do `:app`: recupera transações órfãs e inicia o retry periódico. */
        fun start() {
            if (started) return
            started = true
            appScope.launch {
                recoverOnOpen()
                startPeriodicRetry()
            }
        }

        override suspend fun startSale(
            transactionId: String,
            amountCents: Long,
            paymentType: PaymentType,
        ): Transaction? {
            if (transactionDao.getById(transactionId) != null || !active.add(transactionId)) return null

            val now = clock.nowMillis()
            val config = terminal.current()
            val transaction =
                Transaction(
                    id = transactionId,
                    amountCents = amountCents,
                    paymentType = paymentType,
                    merchantId = config.merchantId,
                    terminalId = config.terminalId,
                    status = TransactionStatus.CRIADA,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                )
            journal.save(transaction)

            appScope.launch {
                try {
                    finish(engine.process(transaction))
                } finally {
                    active.remove(transactionId)
                }
            }
            return transaction
        }

        override suspend fun retryPendingUndo(transactionId: String?) {
            val pending =
                if (transactionId != null) {
                    transactionDao.getById(transactionId)?.let { listOf(it) }.orEmpty()
                } else {
                    transactionDao.getPendingUndo()
                }
            pending.map { it.toDomain() }.forEach(::recover)
        }

        override suspend fun reprintReceipt(transactionId: String) {
            val tx = transactionDao.getById(transactionId)?.toDomain() ?: return
            require(tx.status.isFinal) { "Só transações finalizadas podem ser reimpressas" }
            printQueue.enqueue(tx, forceReprint = true, onPrinted = ::markPrinted)
        }

        private suspend fun recoverOnOpen() {
            transactionDao.getNonFinal().map { it.toDomain() }.forEach(::recover)
            transactionDao
                .getAll()
                .map { it.toDomain() }
                .filter { it.status.isFinal && !it.receiptPrinted }
                .forEach { print(it) }
        }

        private fun recover(transaction: Transaction) {
            if (!active.add(transaction.id)) return
            appScope.launch {
                try {
                    val reason = transaction.undoReason ?: UndoReason.RECUPERACAO
                    finish(engine.undo(transaction, reason))
                } finally {
                    active.remove(transaction.id)
                }
            }
        }

        private suspend fun startPeriodicRetry() {
            while (appScope.isActive) {
                delay(PENDING_UNDO_RETRY_INTERVAL_MS)
                retryPendingUndo()
            }
        }

        private fun finish(transaction: Transaction) {
            if (transaction.status.isFinal) print(transaction)
        }

        private fun print(transaction: Transaction) {
            printQueue.enqueue(transaction, onPrinted = ::markPrinted)
        }

        private suspend fun markPrinted(printed: Transaction) {
            journal.save(printed.copy(receiptPrinted = true))
        }

        private companion object {
            const val PENDING_UNDO_RETRY_INTERVAL_MS = 30_000L
        }
    }
