package com.raimundo.smartpos.core.printer

import android.util.Log
import com.raimundo.smartpos.core.common.Clock
import com.raimundo.smartpos.core.model.ReceiptFormatter
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrintedReceipt(
    val transactionId: String,
    val status: TransactionStatus,
    val text: String,
    val printedAtMillis: Long,
)

/**
 * Fila de impressão: a impressora é um recurso físico único, então os comprovantes são
 * impressos um por vez, na ordem em que as transações terminam (não na ordem em que
 * começaram). Um comprovante por transação; falha de impressão tem retry.
 */
class PrintQueue(
    private val printer: ReceiptPrinter,
    private val formatter: ReceiptFormatter,
    scope: CoroutineScope,
    private val clock: Clock,
    private val maxAttempts: Int = 3,
    private val retryDelayMs: Long = 2_000,
) {
    private class PrintJob(
        val transaction: Transaction,
        val onPrinted: suspend (Transaction) -> Unit,
    )

    private val queue = Channel<PrintJob>(Channel.UNLIMITED)
    private val printedIds = mutableSetOf<String>() // acessado só pela coroutine consumidora

    private val _printed = MutableStateFlow<List<PrintedReceipt>>(emptyList())
    val printed: StateFlow<List<PrintedReceipt>> = _printed.asStateFlow()

    init {
        scope.launch {
            for (job in queue) process(job)
        }
    }

    /** Reimpressão (ação do operador): ignora o controle de "uma via por transação". */
    fun enqueue(
        transaction: Transaction,
        forceReprint: Boolean = false,
        onPrinted: suspend (Transaction) -> Unit = {},
    ) {
        require(transaction.status.isFinal) { "Só transações finalizadas geram comprovante" }
        if (forceReprint) printedIds.remove(transaction.id)
        queue.trySend(PrintJob(transaction, onPrinted))
    }

    private suspend fun process(job: PrintJob) {
        val tx = job.transaction
        if (!printedIds.add(tx.id)) return
        val text = formatter.format(tx)

        repeat(maxAttempts) { attempt ->
            try {
                printer.print(text)
                _printed.update { listOf(PrintedReceipt(tx.id, tx.status, text, clock.nowMillis())) + it }
                job.onPrinted(tx)
                return
            } catch (e: CancellationException) {
                throw e
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                Log.w(TAG, "Falha ao imprimir ${tx.id} (tentativa ${attempt + 1})", e)
                if (attempt < maxAttempts - 1) delay(retryDelayMs)
            }
        }
        printedIds.remove(tx.id) // permite reimpressão futura
    }

    private companion object {
        const val TAG = "SmartPOS-Printer"
    }
}
