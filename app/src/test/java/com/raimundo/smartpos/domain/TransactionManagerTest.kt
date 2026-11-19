package com.raimundo.smartpos.domain

import com.raimundo.smartpos.data.journal.InMemoryTransactionJournal
import com.raimundo.smartpos.data.remote.CommunicationException
import com.raimundo.smartpos.data.remote.RemoteStatus
import com.raimundo.smartpos.domain.model.PaymentType
import com.raimundo.smartpos.domain.model.TerminalConfig
import com.raimundo.smartpos.domain.model.TransactionStatus
import com.raimundo.smartpos.domain.model.UndoReason
import com.raimundo.smartpos.printer.PrintQueue
import com.raimundo.smartpos.printer.ReceiptFormatter
import com.raimundo.smartpos.testing.FakeGateway
import com.raimundo.smartpos.testing.FakePrinter
import com.raimundo.smartpos.testing.MERCHANT
import com.raimundo.smartpos.testing.TERMINAL
import com.raimundo.smartpos.testing.result
import com.raimundo.smartpos.testing.transaction
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class TransactionManagerTest {

    private val gateway = FakeGateway()
    private val journal = InMemoryTransactionJournal()
    private val printer = FakePrinter()

    private class Sut(val manager: TransactionManager, val printQueue: PrintQueue)

    private fun TestScope.sut(): Sut {
        val clock = { testScheduler.currentTime }
        val printQueue = PrintQueue(printer, ReceiptFormatter(timeZone = TimeZone.getTimeZone("UTC")), backgroundScope, clock)
        val manager = TransactionManager(
            processor = TransactionProcessor(gateway, journal, clock = clock),
            journal = journal,
            printQueue = printQueue,
            terminal = TerminalConfig(MERCHANT, TERMINAL),
            scope = backgroundScope,
            clock = clock,
        )
        return Sut(manager, printQueue)
    }

    @Test
    fun `vendas simultaneas correm em paralelo e comprovantes saem na ordem de conclusao`() = runTest {
        val finishAt = mapOf("lenta" to 8_000L, "rapida" to 2_000L, "media" to 5_000L)
        gateway.onPoll = { payload, _ ->
            if (testScheduler.currentTime >= finishAt.getValue(payload.transactionId)) payload.result(RemoteStatus.APROVADA) else null
        }
        val sut = sut()

        assertTrue(sut.manager.startSale("lenta", 100, PaymentType.CREDITO))
        assertTrue(sut.manager.startSale("rapida", 200, PaymentType.DEBITO))
        assertTrue(sut.manager.startSale("media", 300, PaymentType.CREDITO))
        advanceTimeBy(10_000)
        runCurrent()

        // Todas terminaram antes de 10 s: nenhuma esperou a outra (em série seriam ~15 s).
        val printOrder = sut.printQueue.printed.value.reversed().map { it.transactionId }
        assertEquals(listOf("rapida", "media", "lenta"), printOrder)
        assertTrue(journal.loadAll().all { it.status == TransactionStatus.APROVADA && it.receiptPrinted })
    }

    @Test
    fun `mesmo transactionId duas vezes gera uma unica transacao`() = runTest {
        gateway.onPoll = { payload, _ -> payload.result(RemoteStatus.APROVADA) }
        val sut = sut()

        assertTrue(sut.manager.startSale("dup", 500, PaymentType.DEBITO))
        assertFalse(sut.manager.startSale("dup", 500, PaymentType.DEBITO))
        advanceTimeBy(5_000)
        runCurrent()

        assertEquals(1, gateway.authorizeCalls.size)
        assertEquals(1, printer.printed.size)
    }

    @Test
    fun `ao iniciar, desfaz transacao interrompida e imprime comprovante`() = runTest {
        val interrupted = transaction(id = "orfa").copy(status = TransactionStatus.AGUARDANDO_RESULTADO)
        journal.save(interrupted)
        val sut = sut()

        sut.manager.start()
        advanceTimeBy(1_000)
        runCurrent()

        val recovered = journal.get("orfa")!!
        assertEquals(TransactionStatus.DESFEITA, recovered.status)
        assertEquals(UndoReason.RECUPERACAO, recovered.undoReason)
        assertTrue(recovered.receiptPrinted)
        assertEquals("orfa", gateway.undoCalls.single().transactionId)
    }

    @Test
    fun `desfazimento pendente eh retentado periodicamente`() = runTest {
        journal.save(transaction(id = "pendente").copy(status = TransactionStatus.DESFAZIMENTO_PENDENTE, undoReason = UndoReason.TIMEOUT))
        gateway.onUndo = { _, attempt -> if (attempt <= 3) throw CommunicationException("backend fora") }
        val sut = sut()

        sut.manager.start()
        advanceTimeBy(10_000)
        runCurrent()
        assertEquals(TransactionStatus.DESFAZIMENTO_PENDENTE, journal.get("pendente")!!.status)

        advanceTimeBy(30_000)
        runCurrent()
        val tx = journal.get("pendente")!!
        assertEquals(TransactionStatus.DESFEITA, tx.status)
        assertEquals(UndoReason.TIMEOUT, tx.undoReason) // motivo original preservado
        assertEquals(1, printer.printed.size)
    }
}
