package com.raimundo.smartpos.core.domain.engine

import com.raimundo.smartpos.core.common.CommunicationException
import com.raimundo.smartpos.core.model.RemoteStatus
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.model.UndoReason
import com.raimundo.smartpos.core.testing.FakeGateway
import com.raimundo.smartpos.core.testing.InMemoryTransactionJournal
import com.raimundo.smartpos.core.testing.TestClock
import com.raimundo.smartpos.core.testing.result
import com.raimundo.smartpos.core.testing.transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionEngineTest {
    private val gateway = FakeGateway()
    private val journal = InMemoryTransactionJournal()

    private fun TestScope.engine() =
        TransactionEngine(
            gateway = gateway,
            journal = journal,
            config = EngineConfig(),
            clock = TestClock { testScheduler.currentTime },
        )

    // Cenário 1: aprovada.
    @Test
    fun `aprovada apos alguns pollings - sem undo`() =
        runTest {
            gateway.onPoll = { tx, attempt -> if (attempt >= 3) tx.result(RemoteStatus.APROVADA) else null }
            val updates = mutableListOf<TransactionStatus>()

            val result = engine().process(transaction()) { updates += it.status }

            assertEquals(TransactionStatus.APROVADA, result.status)
            assertEquals(
                listOf(
                    TransactionStatus.AUTORIZANDO,
                    TransactionStatus.AGUARDANDO_RESULTADO,
                    TransactionStatus.APROVADA,
                ),
                updates,
            )
            assertTrue(gateway.undoCalls.isEmpty())
            assertEquals(TransactionStatus.APROVADA, journal.get("tx-1")?.status)
        }

    // Cenário 2: rejeitada.
    @Test
    fun `rejeitada - sem undo`() =
        runTest {
            gateway.onPoll = { tx, _ -> tx.result(RemoteStatus.REJEITADA) }

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.REJEITADA, result.status)
            assertTrue(gateway.undoCalls.isEmpty())
        }

    // Cenário 3: desfeita pelo backend, sem novo /undo.
    @Test
    fun `backend informa DESFEITA - nao chama undo de novo`() =
        runTest {
            gateway.onPoll = { tx, _ -> tx.result(RemoteStatus.DESFEITA) }

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.DESFEITA, result.status)
            assertTrue(gateway.undoCalls.isEmpty())
        }

    // Cenário 4: aguardando - estado intermediário visível antes do resultado final.
    @Test
    fun `aguardando resultado antes do polling final`() =
        runTest {
            gateway.onPoll = { tx, attempt -> if (attempt >= 5) tx.result(RemoteStatus.APROVADA) else null }
            val updates = mutableListOf<TransactionStatus>()

            engine().process(transaction()) { updates += it.status }

            assertTrue(updates.contains(TransactionStatus.AGUARDANDO_RESULTADO))
        }

    // Cenário 5: sem resultado em 15s -> timeout -> undo -> DESFEITA.
    @Test
    fun `sem resultado em 15s - desfaz exatamente aos 15s`() =
        runTest {
            gateway.onPoll = { _, _ -> null }

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.DESFEITA, result.status)
            assertEquals(UndoReason.TIMEOUT, result.undoReason)
            assertEquals(15_000L, testScheduler.currentTime)
            assertEquals(1, gateway.undoCalls.size)
        }

    // Cenário 6: /authorization lento também conta no prazo e é cancelado.
    @Test
    fun `authorization lento tambem conta no prazo de 15s`() =
        runTest {
            gateway.onAuthorize = { delay(20_000) }

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.DESFEITA, result.status)
            assertEquals(UndoReason.TIMEOUT, result.undoReason)
            assertEquals(15_000L, testScheduler.currentTime)
        }

    // Cenário 7: rede cai no envio -> undo.
    @Test
    fun `falha de rede no authorization - desfaz`() =
        runTest {
            gateway.onAuthorize = { throw CommunicationException("rede caiu") }

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.DESFEITA, result.status)
            assertEquals(UndoReason.FALHA_COMUNICACAO, result.undoReason)
            assertEquals(1, gateway.undoCalls.size)
        }

    // Cenário 8: resposta do envio perdida é tratada como falha de comunicação -> undo garante não cobrança.
    @Test
    fun `resposta do authorization perdida - desfaz para garantir que nada fique cobrado`() =
        runTest {
            gateway.onAuthorize = { throw CommunicationException("resposta perdida") }
            gateway.onPoll = { tx, _ -> tx.result(RemoteStatus.APROVADA) } // backend já teria aprovado

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.DESFEITA, result.status)
            assertEquals(1, gateway.undoCalls.size)
        }

    // Cenário 9/10: HTTP de erro ou rede cai no polling -> undo.
    @Test
    fun `falha de rede no polling - desfaz`() =
        runTest {
            gateway.onPoll = { _, attempt ->
                if (attempt == 2) throw CommunicationException("timeout de leitura") else null
            }

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.DESFEITA, result.status)
            assertEquals(UndoReason.FALHA_COMUNICACAO, result.undoReason)
        }

    // Cenário 11: resposta inconsistente -> undo.
    @Test
    fun `resposta de polling de outra transacao ou valor - desfaz`() =
        runTest {
            gateway.onPoll = { tx, _ -> tx.result(RemoteStatus.APROVADA).copy(amountCents = tx.amountCents + 1) }

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.DESFEITA, result.status)
            assertEquals(UndoReason.FALHA_COMUNICACAO, result.undoReason)
        }

    // Cenário 13: /undo falha e funciona depois.
    @Test
    fun `undo falha duas vezes e funciona na terceira`() =
        runTest {
            gateway.onPoll = { _, _ -> null }
            gateway.onUndo = { _, attempt -> if (attempt < 3) throw CommunicationException("undo falhou") }

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.DESFEITA, result.status)
            assertEquals(3, gateway.undoCalls.size)
        }

    // Cenário 14: /undo nunca confirma -> DESFAZIMENTO_PENDENTE.
    @Test
    fun `undo nunca confirma - fica DESFAZIMENTO_PENDENTE`() =
        runTest {
            gateway.onAuthorize = { throw CommunicationException("rede caiu") }
            gateway.onUndo = { _, _ -> throw CommunicationException("undo falhou") }

            val result = engine().process(transaction())

            assertEquals(TransactionStatus.DESFAZIMENTO_PENDENTE, result.status)
            assertEquals(TransactionStatus.DESFAZIMENTO_PENDENTE, journal.get("tx-1")?.status)
            assertEquals(3, gateway.undoCalls.size)
        }

    // Cenário 17 (idempotência): mesmo transactionId/payload em authorization, polling e undo.
    @Test
    fun `mesmo transactionId e payload em authorization, polling e undo (idempotencia)`() =
        runTest {
            gateway.onPoll = { _, _ -> null }
            val tx = transaction(id = "abc-123", amountCents = 4_990)

            engine().process(tx)

            val sent = gateway.authorizeCalls.single()
            assertEquals("abc-123", sent.id)
            assertEquals(4_990L, sent.amountCents)
            assertEquals(sent.id, gateway.undoCalls.single().id)
            assertEquals(setOf("abc-123"), gateway.pollCalls.keys)
        }

    @Test
    fun `estado eh gravado no journal antes de ir para a rede`() =
        runTest {
            var statusNoJournalDuranteEnvio: TransactionStatus? = null
            gateway.onAuthorize = { statusNoJournalDuranteEnvio = journal.get(it.id)?.status }
            gateway.onPoll = { tx, _ -> tx.result(RemoteStatus.APROVADA) }

            engine().process(transaction())

            assertEquals(TransactionStatus.AUTORIZANDO, statusNoJournalDuranteEnvio)
        }
}
