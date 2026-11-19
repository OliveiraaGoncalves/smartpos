package com.raimundo.smartpos.core.printer

import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.model.UndoReason
import com.raimundo.smartpos.core.testing.transaction
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class ReceiptFormatterTest {
    private val formatter = ReceiptFormatter(timeZone = TimeZone.getTimeZone("UTC"))

    @Test
    fun `comprovante contem transactionId, valor e status`() {
        val tx =
            transaction(id = "123e4567-e89b-12d3-a456-426614174000", amountCents = 123_456)
                .copy(status = TransactionStatus.DESFEITA, undoReason = UndoReason.TIMEOUT)

        val receipt = formatter.format(tx)

        assertTrue(receipt.contains("123e4567-e89b-12d3-a456-42661417")) // quebrado em 32 colunas
        assertTrue(receipt.contains("4000"))
        assertTrue(receipt.contains("R$ 1.234,56"))
        assertTrue(receipt.contains("DESFEITA"))
        assertTrue(receipt.contains("TEMPO ESGOTADO"))
        assertTrue(receipt.lines().all { it.length <= 32 })
    }
}
