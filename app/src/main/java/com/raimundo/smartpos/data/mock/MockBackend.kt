package com.raimundo.smartpos.data.mock

import com.raimundo.smartpos.data.remote.PollingResponse
import com.raimundo.smartpos.data.remote.RemoteStatus
import com.raimundo.smartpos.data.remote.TransactionPayload
import com.raimundo.smartpos.domain.model.TerminalConfig
import kotlin.random.Random

/**
 * Cenários do mock, escolhidos pelos centavos do valor para facilitar a demonstração
 * (tabela no README).
 */
enum class MockScenario(val resolvesTo: RemoteStatus?) {
    APPROVE(RemoteStatus.APROVADA),
    REJECT(RemoteStatus.REJEITADA),

    /** Nunca chega a um resultado: força o timeout de 15 s e o /undo. */
    NEVER_RESPONDS(null),

    /** Backend recebe e aprova, mas a resposta do /authorization se perde na rede. */
    AUTH_NETWORK_FAILURE(RemoteStatus.APROVADA),

    /** A partir do 2º polling a rede falha. */
    POLL_NETWORK_FAILURE(RemoteStatus.APROVADA),

    /** Nunca responde e as duas primeiras tentativas de /undo falham (testa o retry do undo). */
    UNDO_FLAKY(null),
}

object AmountBasedScenarios {
    fun resolve(payload: TransactionPayload): MockScenario = when (payload.amount % 100) {
        13L -> MockScenario.REJECT
        20L -> MockScenario.NEVER_RESPONDS
        50L -> MockScenario.AUTH_NETWORK_FAILURE
        66L -> MockScenario.POLL_NETWORK_FAILURE
        77L -> MockScenario.UNDO_FLAKY
        else -> MockScenario.APPROVE
    }
}

sealed interface MockResponse {
    data object Ok : MockResponse
    data object Pending : MockResponse
    data object NotFound : MockResponse
    data object Conflict : MockResponse
    data object BadRequest : MockResponse
    data object NetworkFailure : MockResponse
    data class Result(val body: PollingResponse) : MockResponse
}

/**
 * Backend simulado em memória. Regras importantes que ele garante:
 * - Idempotência: o mesmo transactionId com o mesmo payload não cria nova transação;
 *   com payload diferente responde 409.
 * - /undo é idempotente e, se chegar antes da autorização, deixa uma "lápide" que impede
 *   que uma autorização atrasada seja aprovada.
 */
class MockBackend(
    private val clock: () -> Long = System::currentTimeMillis,
    private val scenarioResolver: (TransactionPayload) -> MockScenario = AmountBasedScenarios::resolve,
    private val resultDelayMs: () -> Long = { Random.nextLong(1_500, 6_000) },
) {
    private class Record(
        val payload: TransactionPayload,
        val receivedAtMillis: Long,
        val scenario: MockScenario,
        val resultAfterMs: Long,
        var status: RemoteStatus? = null,
        var pollCount: Int = 0,
        var undoAttempts: Int = 0,
    )

    private val records = HashMap<String, Record>()

    var authorizationRequests: Int = 0
        private set

    @Synchronized
    fun authorize(payload: TransactionPayload): MockResponse {
        authorizationRequests++
        if (!payload.isWellFormed()) return MockResponse.BadRequest

        records[payload.transactionId]?.let { existing ->
            return if (existing.payload == payload) MockResponse.Ok else MockResponse.Conflict
        }

        val scenario = scenarioResolver(payload)
        records[payload.transactionId] = Record(payload, clock(), scenario, resultDelayMs())
        return if (scenario == MockScenario.AUTH_NETWORK_FAILURE) MockResponse.NetworkFailure else MockResponse.Ok
    }

    @Synchronized
    fun undo(payload: TransactionPayload): MockResponse {
        if (!payload.isWellFormed()) return MockResponse.BadRequest
        val record = records[payload.transactionId]
        if (record == null) {
            records[payload.transactionId] = Record(
                payload = payload,
                receivedAtMillis = clock(),
                scenario = MockScenario.APPROVE,
                resultAfterMs = 0,
                status = RemoteStatus.DESFEITA,
            )
            return MockResponse.Ok
        }
        if (record.payload != payload) return MockResponse.Conflict
        if (record.scenario == MockScenario.UNDO_FLAKY && record.undoAttempts++ < 2) {
            return MockResponse.NetworkFailure
        }
        record.status = RemoteStatus.DESFEITA
        return MockResponse.Ok
    }

    @Synchronized
    fun poll(transactionId: String): MockResponse {
        val record = records[transactionId] ?: return MockResponse.NotFound
        record.pollCount++
        if (record.scenario == MockScenario.POLL_NETWORK_FAILURE && record.pollCount >= 2) {
            return MockResponse.NetworkFailure
        }
        val target = record.scenario.resolvesTo
        if (record.status == null && target != null && clock() - record.receivedAtMillis >= record.resultAfterMs) {
            record.status = target
        }
        val status = record.status ?: return MockResponse.Pending
        val p = record.payload
        return MockResponse.Result(
            PollingResponse(p.paymentType, p.transactionId, p.amount, p.merchantId, p.terminalId, status),
        )
    }

    @Synchronized
    fun statusOf(transactionId: String): RemoteStatus? = records[transactionId]?.status

    @Synchronized
    fun transactionCount(): Int = records.size

    private fun TransactionPayload.isWellFormed() =
        amount > 0 &&
            transactionId.isNotBlank() &&
            TerminalConfig.MERCHANT_ID_REGEX.matches(merchantId) &&
            TerminalConfig.TERMINAL_ID_REGEX.matches(terminalId)
}
