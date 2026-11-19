package com.raimundo.smartpos.domain

import com.raimundo.smartpos.data.journal.InMemoryTransactionJournal
import com.raimundo.smartpos.data.remote.CommunicationException
import com.raimundo.smartpos.data.remote.RemoteStatus
import com.raimundo.smartpos.domain.model.TransactionStatus
import com.raimundo.smartpos.domain.model.UndoReason
import com.raimundo.smartpos.testing.FakeGateway
import com.raimundo.smartpos.testing.result
import com.raimundo.smartpos.testing.transaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionProcessorTest {

    private val gateway = FakeGateway()
    private val journal = InMemoryTransactionJournal()

    private fun TestScope.processor() = TransactionProcessor(
        gateway = gateway,
        journal = journal,
        config = ProcessorConfig(),
        clock = { testScheduler.currentTime },
    )

    @Test
    fun `aprovada apos alguns pollings - sem undo`() = runTest {
        gateway.onPoll = { payload, attempt -> if (attempt >= 3) payload.result(RemoteStatus.APROVADA) else null }
        val updates = mutableListOf<TransactionStatus>()

        val result = processor().process(transaction()) { updates += it.status }

        assertEquals(TransactionStatus.APROVADA, result.status)
        assertEquals(
            listOf(TransactionStatus.AUTORIZANDO, TransactionStatus.AGUARDANDO_RESULTADO, TransactionStatus.APROVADA),
            updates,
        )
        assertTrue(gateway.undoCalls.isEmpty())
        assertEquals(TransactionStatus.APROVADA, journal.get("tx-1")?.status)
    }

    @Test
    fun `rejeitada - sem undo`() = runTest {
        gateway.onPoll = { payload, _ -> payload.result(RemoteStatus.REJEITADA) }

        val result = processor().process(transaction())

        assertEquals(TransactionStatus.REJEITADA, result.status)
        assertTrue(gateway.undoCalls.isEmpty())
    }

    @Test
    fun `sem resultado em 15s - desfaz exatamente aos 15s`() = runTest {
        gateway.onPoll = { _, _ -> null }

        val result = processor().process(transaction())

        assertEquals(TransactionStatus.DESFEITA, result.status)
        assertEquals(UndoReason.TIMEOUT, result.undoReason)
        assertEquals(15_000L, testScheduler.currentTime)
        assertEquals(1, gateway.undoCalls.size)
    }

    @Test
    fun `authorization lento tambem conta no prazo de 15s`() = runTest {
        gateway.onAuthorize = { delay(20_000) }

        val result = processor().process(transaction())

        assertEquals(TransactionStatus.DESFEITA, result.status)
        assertEquals(UndoReason.TIMEOUT, result.undoReason)
        assertEquals(15_000L, testScheduler.currentTime)
    }

    @Test
    fun `falha de rede no authorization - desfaz`() = runTest {
        gateway.onAuthorize = { throw CommunicationException("rede caiu") }

        val result = processor().process(transaction())

        assertEquals(TransactionStatus.DESFEITA, result.status)
        assertEquals(UndoReason.FALHA_COMUNICACAO, result.undoReason)
        assertEquals(1, gateway.undoCalls.size)
    }

    @Test
    fun `falha de rede no polling - desfaz`() = runTest {
        gateway.onPoll = { _, attempt -> if (attempt == 2) throw CommunicationException("timeout de leitura") else null }

        val result = processor().process(transaction())

        assertEquals(TransactionStatus.DESFEITA, result.status)
        assertEquals(UndoReason.FALHA_COMUNICACAO, result.undoReason)
    }

    @Test
    fun `resposta de polling de outra transacao ou valor - desfaz`() = runTest {
        gateway.onPoll = { payload, _ -> payload.copy(amount = payload.amount + 1).result(RemoteStatus.APROVADA) }

        val result = processor().process(transaction())

        assertEquals(TransactionStatus.DESFEITA, result.status)
        assertEquals(UndoReason.FALHA_COMUNICACAO, result.undoReason)
    }

    @Test
    fun `backend informa DESFEITA - nao chama undo de novo`() = runTest {
        gateway.onPoll = { payload, _ -> payload.result(RemoteStatus.DESFEITA) }

        val result = processor().process(transaction())

        assertEquals(TransactionStatus.DESFEITA, result.status)
        assertTrue(gateway.undoCalls.isEmpty())
    }

    @Test
    fun `undo falha duas vezes e funciona na terceira`() = runTest {
        gateway.onPoll = { _, _ -> null }
        gateway.onUndo = { _, attempt -> if (attempt < 3) throw CommunicationException("undo falhou") }

        val result = processor().process(transaction())

        assertEquals(TransactionStatus.DESFEITA, result.status)
        assertEquals(3, gateway.undoCalls.size)
    }

    @Test
    fun `undo nunca confirma - fica DESFAZIMENTO_PENDENTE`() = runTest {
        gateway.onAuthorize = { throw CommunicationException("rede caiu") }
        gateway.onUndo = { _, _ -> throw CommunicationException("undo falhou") }

        val result = processor().process(transaction())

        assertEquals(TransactionStatus.DESFAZIMENTO_PENDENTE, result.status)
        assertEquals(TransactionStatus.DESFAZIMENTO_PENDENTE, journal.get("tx-1")?.status)
        assertEquals(3, gateway.undoCalls.size)
    }

    @Test
    fun `mesmo transactionId e payload em authorization, polling e undo (idempotencia)`() = runTest {
        gateway.onPoll = { _, _ -> null }
        val tx = transaction(id = "abc-123", amountCents = 4_990)

        processor().process(tx)

        val sent = gateway.authorizeCalls.single()
        assertEquals("abc-123", sent.transactionId)
        assertEquals(4_990L, sent.amount)
        assertEquals(sent, gateway.undoCalls.single())
        assertEquals(setOf("abc-123"), gateway.pollCalls.keys)
    }

    @Test
    fun `estado eh gravado no journal antes de ir para a rede`() = runTest {
        var statusNoJournalDuranteEnvio: TransactionStatus? = null
        gateway.onAuthorize = { statusNoJournalDuranteEnvio = journal.get(it.transactionId)?.status }
        gateway.onPoll = { payload, _ -> payload.result(RemoteStatus.APROVADA) }

        processor().process(transaction())

        assertEquals(TransactionStatus.AUTORIZANDO, statusNoJournalDuranteEnvio)
    }
}
