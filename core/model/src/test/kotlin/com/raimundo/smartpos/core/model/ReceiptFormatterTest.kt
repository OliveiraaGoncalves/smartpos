package com.raimundo.smartpos.core.model

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class ReceiptFormatterTest {
    private val formatter = ReceiptFormatter(timeZone = TimeZone.getTimeZone("UTC"))

    @Test
    fun `comprovante contem transactionId, valor e status`() {
        val tx =
            Transaction(
                id = "123e4567-e89b-12d3-a456-426614174000",
                amountCents = 123_456,
                paymentType = PaymentType.CREDITO,
                merchantId = "123456789012345",
                terminalId = "12345678",
                status = TransactionStatus.DESFEITA,
                createdAtMillis = 0,
                updatedAtMillis = 0,
                undoReason = UndoReason.TIMEOUT,
            )

        val receipt = formatter.format(tx)

        assertTrue(receipt.contains("123e4567-e89b-12d3-a456-42661417")) // quebrado em 32 colunas
        assertTrue(receipt.contains("4000"))
        assertTrue(receipt.contains("R$ 1.234,56"))
        assertTrue(receipt.contains("DESFEITA"))
        assertTrue(receipt.contains("TEMPO ESGOTADO"))
        assertTrue(receipt.lines().all { it.length <= 32 })
    }
}
