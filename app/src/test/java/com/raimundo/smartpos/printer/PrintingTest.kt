package com.raimundo.smartpos.printer

import com.raimundo.smartpos.domain.model.TransactionStatus
import com.raimundo.smartpos.domain.model.UndoReason
import com.raimundo.smartpos.testing.FakePrinter
import com.raimundo.smartpos.testing.transaction
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class PrintingTest {

    private val formatter = ReceiptFormatter(timeZone = TimeZone.getTimeZone("UTC"))

    @Test
    fun `comprovante contem transactionId, valor e status`() {
        val tx = transaction(id = "123e4567-e89b-12d3-a456-426614174000", amountCents = 123_456)
            .copy(status = TransactionStatus.DESFEITA, undoReason = UndoReason.TIMEOUT)

        val receipt = formatter.format(tx)

        assertTrue(receipt.contains("123e4567-e89b-12d3-a456-42661417")) // quebrado em 32 colunas
        assertTrue(receipt.contains("4000"))
        assertTrue(receipt.contains("R$ 1.234,56"))
        assertTrue(receipt.contains("DESFEITA"))
        assertTrue(receipt.contains("TEMPO ESGOTADO"))
        assertTrue(receipt.lines().all { it.length <= 32 })
    }

    @Test
    fun `falha de impressora tem retry e cada transacao imprime uma unica vez`() = runTest {
        val printer = FakePrinter().apply { failuresBeforeSuccess = 2 }
        val queue = PrintQueue(printer, formatter, backgroundScope, { testScheduler.currentTime }, retryDelayMs = 1_000)
        val tx = transaction().copy(status = TransactionStatus.APROVADA)

        queue.enqueue(tx)
        queue.enqueue(tx)
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(1, printer.printed.size)
        assertEquals(1, queue.printed.value.size)
    }
}
