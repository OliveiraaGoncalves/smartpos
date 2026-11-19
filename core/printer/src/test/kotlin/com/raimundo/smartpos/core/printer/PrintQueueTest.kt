package com.raimundo.smartpos.core.printer

import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.testing.TestClock
import com.raimundo.smartpos.core.testing.transaction
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class FakePrinter : ReceiptPrinter {
    val printed = mutableListOf<String>()
    var failuresBeforeSuccess = 0

    override suspend fun print(receipt: String) {
        if (failuresBeforeSuccess > 0) {
            failuresBeforeSuccess--
            error("Sem papel")
        }
        printed += receipt
    }
}

class PrintQueueTest {
    private val formatter = ReceiptFormatter(timeZone = TimeZone.getTimeZone("UTC"))

    @Test
    fun `falha de impressora tem retry e cada transacao imprime uma unica vez`() =
        runTest {
            val printer = FakePrinter().apply { failuresBeforeSuccess = 2 }
            val clock = TestClock { testScheduler.currentTime }
            val queue = PrintQueue(printer, formatter, backgroundScope, clock, retryDelayMs = 1_000)
            val tx = transaction().copy(status = TransactionStatus.APROVADA)

            queue.enqueue(tx)
            queue.enqueue(tx)
            advanceTimeBy(5_000)
            runCurrent()

            assertEquals(1, printer.printed.size)
            assertEquals(1, queue.printed.value.size)
        }

    @Test
    fun `impressora sem papel esgota as tentativas e permite nova tentativa depois`() =
        runTest {
            val printer = FakePrinter().apply { failuresBeforeSuccess = 5 }
            val clock = TestClock { testScheduler.currentTime }
            val queue = PrintQueue(printer, formatter, backgroundScope, clock, retryDelayMs = 100)
            val tx = transaction().copy(status = TransactionStatus.APROVADA)

            queue.enqueue(tx)
            advanceTimeBy(1_000)
            runCurrent()

            assertEquals(0, printer.printed.size) // as 3 tentativas falharam

            printer.failuresBeforeSuccess = 0
            queue.enqueue(tx) // "Tentar de novo" do operador
            runCurrent()

            assertEquals(1, printer.printed.size)
        }

    @Test
    fun `reimpressao gera nova via mesmo apos sucesso`() =
        runTest {
            val printer = FakePrinter()
            val queue = PrintQueue(printer, formatter, backgroundScope, TestClock { testScheduler.currentTime })
            val tx = transaction().copy(status = TransactionStatus.APROVADA)

            queue.enqueue(tx)
            runCurrent()
            queue.enqueue(tx, forceReprint = true)
            runCurrent()

            assertEquals(2, printer.printed.size)
            assertEquals(2, queue.printed.value.size)
        }
}
