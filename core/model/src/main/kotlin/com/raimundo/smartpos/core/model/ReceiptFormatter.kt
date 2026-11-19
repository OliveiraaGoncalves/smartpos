package com.raimundo.smartpos.core.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Comprovante em 32 colunas (bobina 58 mm), com transactionId, amount e status.
 * Kotlin puro de propósito: `core:printer` usa para imprimir e `feature:receipt` para a
 * pré-visualização na folha, sem que a feature precise depender de `core:printer`.
 */
class ReceiptFormatter(
    private val width: Int = 32,
    private val timeZone: TimeZone = TimeZone.getDefault(),
) {
    fun format(tx: Transaction): String {
        val separator = "-".repeat(width)
        val date =
            SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.forLanguageTag("pt-BR"))
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

    private fun row(
        label: String,
        value: String,
    ): String {
        val gap = width - label.length - value.length
        return if (gap >= 1) label + " ".repeat(gap) + value else "$label $value"
    }

    private fun center(text: String): String = text.padStart((width + text.length) / 2).padEnd(width)
}
