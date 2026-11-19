package com.raimundo.smartpos.core.networkmock

import com.raimundo.smartpos.core.common.CommunicationException
import com.raimundo.smartpos.core.model.RemoteStatus
import com.raimundo.smartpos.core.network.HttpPaymentGateway
import com.raimundo.smartpos.core.network.SmartPosHeaders
import com.raimundo.smartpos.core.network.toPayload
import com.raimundo.smartpos.core.security.AesGcmPayloadCipher
import com.raimundo.smartpos.core.security.StaticKeyProvider
import com.raimundo.smartpos.core.testing.transaction
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

/** Cliente HTTP real (OkHttp + AES-GCM) contra o backend simulado: valida o contrato ponta a ponta. */
class GatewayWithMockBackendTest {
    private var now = 0L
    private var scenario = MockScenario.APPROVE
    private val cipher = AesGcmPayloadCipher(StaticKeyProvider("k1", ByteArray(32) { 3 }))
    private val backend = MockBackend(clock = { now }, scenarioResolver = { scenario }, resultDelayMs = { 2_000 })
    private val client =
        OkHttpClient
            .Builder()
            .addInterceptor(MockBackendInterceptor(backend, cipher, latencyMs = { 0 }))
            .build()
    private val gateway = HttpPaymentGateway(client, "https://mock.smartpos.local/".toHttpUrl(), cipher)
    private val tx = transaction(id = "tx-42", amountCents = 2_500)

    @Test
    fun `fluxo aprovado - 204 enquanto pendente e depois resultado cifrado`() =
        runBlocking {
            gateway.authorize(tx)
            assertNull(gateway.poll("tx-42"))

            now = 2_000
            val result = gateway.poll("tx-42")!!

            assertEquals(RemoteStatus.APROVADA, result.status)
            assertEquals(tx.amountCents, result.amountCents)
        }

    @Test
    fun `authorization repetida nao duplica a transacao (idempotencia)`() =
        runBlocking {
            gateway.authorize(tx)
            gateway.authorize(tx)

            assertEquals(2, backend.authorizationRequests)
            assertEquals(1, backend.transactionCount())
        }

    @Test
    fun `mesmo transactionId com outro valor eh rejeitado com 409`() =
        runBlocking {
            gateway.authorize(tx)
            try {
                gateway.authorize(tx.copy(amountCents = 9_999))
                fail("Deveria falhar")
            } catch (e: CommunicationException) {
                assertEquals(true, e.message?.contains("409"))
            }
        }

    @Test
    fun `resposta perdida do authorization - undo garante DESFEITA no backend`() =
        runBlocking {
            scenario = MockScenario.AUTH_NETWORK_FAILURE
            try {
                gateway.authorize(tx)
                fail("Deveria falhar")
            } catch (
                @Suppress("SwallowedException") e: CommunicationException,
            ) {
                // O backend recebeu (e aprovaria); o terminal não sabe -> desfaz.
            }
            gateway.undo(tx)

            now = 10_000
            assertEquals(RemoteStatus.DESFEITA, gateway.poll("tx-42")!!.status)
        }

    @Test
    fun `undo antes da authorization impede aprovacao tardia (lapide)`() =
        runBlocking {
            gateway.undo(tx)
            gateway.authorize(tx)

            now = 10_000
            assertEquals(RemoteStatus.DESFEITA, gateway.poll("tx-42")!!.status)
        }

    @Test
    fun `backend recusa corpo sem a camada AES`() {
        val request =
            Request
                .Builder()
                .url("https://mock.smartpos.local/authorization")
                .header(SmartPosHeaders.TRANSACTION_ID, "tx-42")
                .post("""{"transactionId":"tx-42","amount":2500}""".toRequestBody("application/json".toMediaType()))
                .build()

        val code = client.newCall(request).execute().use { it.code }

        assertEquals(400, code)
        assertEquals(0, backend.transactionCount())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `gateway exige HTTPS`() {
        HttpPaymentGateway(client, "http://mock.smartpos.local/".toHttpUrl(), cipher)
    }

    @Test
    fun `toPayload mapeia corretamente o tipo de pagamento`() {
        assertEquals("Credito", tx.toPayload().paymentType)
    }
}
