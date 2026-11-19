package com.raimundo.smartpos.printer

import android.util.Log
import com.raimundo.smartpos.domain.model.Money
import com.raimundo.smartpos.domain.model.Transaction
import com.raimundo.smartpos.domain.model.TransactionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Abstração da impressora térmica. Em um POS real, adaptador para o SDK do fabricante. */
interface ReceiptPrinter {
    suspend fun print(receipt: String)
}

class SimulatedReceiptPrinter(private val printDurationMs: Long = 600) : ReceiptPrinter {
    override suspend fun print(receipt: String) {
        delay(printDurationMs) // tempo de impressão física
        Log.i("SmartPOS-Printer", "\n$receipt")
    }
}

class ReceiptFormatter(
    private val width: Int = 32,
    private val timeZone: TimeZone = TimeZone.getDefault(),
) {
    fun format(tx: Transaction): String {
        val separator = "-".repeat(width)
        val date = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
            .apply { this.timeZone = this@ReceiptFormatter.timeZone }
            .format(Date(tx.updatedAtMillis))

        return buildString {
            appendLine(separator)
            appendLine(center("COMPROVANTE DE VENDA"))
            appendLine(separator)
            appendLine(row("EC", tx.merchantId))
            appendLine(row("TERMINAL", tx.terminalId))
            appendLine(row("DATA", date))
            appendLine(separator)
            appendLine("TRANSACAO (transactionId):")
            tx.id.chunked(width).forEach { appendLine(it) }
            appendLine(row("TIPO", tx.paymentType.receiptLabel))
            appendLine(row("VALOR", Money.format(tx.amountCents)))
            appendLine(row("STATUS", tx.status.name))
            val reason = tx.undoReason
            if (tx.status == TransactionStatus.DESFEITA && reason != null) {
                appendLine(row("MOTIVO", reason.receiptLabel))
            }
            append(separator)
        }
    }

    private fun row(label: String, value: String): String {
        val gap = width - label.length - value.length
        return if (gap >= 1) label + " ".repeat(gap) + value else "$label $value"
    }

    private fun center(text: String): String = text.padStart((width + text.length) / 2).padEnd(width)
}

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
    private val clock: () -> Long = System::currentTimeMillis,
    private val maxAttempts: Int = 3,
    private val retryDelayMs: Long = 2_000,
) {
    private class PrintJob(val transaction: Transaction, val onPrinted: suspend (Transaction) -> Unit)

    private val queue = Channel<PrintJob>(Channel.UNLIMITED)
    private val printedIds = mutableSetOf<String>() // acessado só pela coroutine consumidora

    private val _printed = MutableStateFlow<List<PrintedReceipt>>(emptyList())
    val printed: StateFlow<List<PrintedReceipt>> = _printed.asStateFlow()

    init {
        scope.launch {
            for (job in queue) process(job)
        }
    }

    fun enqueue(transaction: Transaction, onPrinted: suspend (Transaction) -> Unit = {}) {
        require(transaction.status.isFinal) { "Só transações finalizadas geram comprovante" }
        queue.trySend(PrintJob(transaction, onPrinted))
    }

    private suspend fun process(job: PrintJob) {
        val tx = job.transaction
        if (!printedIds.add(tx.id)) return
        val text = formatter.format(tx)

        repeat(maxAttempts) { attempt ->
            try {
                printer.print(text)
                _printed.update { listOf(PrintedReceipt(tx.id, tx.status, text, clock())) + it }
                job.onPrinted(tx)
                return
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w("SmartPOS-Printer", "Falha ao imprimir ${tx.id} (tentativa ${attempt + 1})", e)
                if (attempt < maxAttempts - 1) delay(retryDelayMs)
            }
        }
        printedIds.remove(tx.id) // permite reimpressão futura
    }
}
