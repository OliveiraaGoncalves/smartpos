package com.raimundo.smartpos.data

import com.raimundo.smartpos.data.crypto.AesGcmPayloadCipher
import com.raimundo.smartpos.data.crypto.StaticKeyProvider
import com.raimundo.smartpos.data.mock.MockBackend
import com.raimundo.smartpos.data.mock.MockBackendInterceptor
import com.raimundo.smartpos.data.mock.MockScenario
import com.raimundo.smartpos.data.remote.CommunicationException
import com.raimundo.smartpos.data.remote.HttpPaymentGateway
import com.raimundo.smartpos.data.remote.RemoteStatus
import com.raimundo.smartpos.data.remote.SmartPosHeaders
import com.raimundo.smartpos.data.remote.toPayload
import com.raimundo.smartpos.testing.transaction
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
    private val client = OkHttpClient.Builder()
        .addInterceptor(MockBackendInterceptor(backend, cipher, latencyMs = { 0 }))
        .build()
    private val gateway = HttpPaymentGateway(client, "https://mock.smartpos.local/".toHttpUrl(), cipher)
    private val payload = transaction(id = "tx-42", amountCents = 2_500).toPayload()

    @Test
    fun `fluxo aprovado - 204 enquanto pendente e depois resultado cifrado`() = runBlocking {
        gateway.authorize(payload)
        assertNull(gateway.poll("tx-42"))

        now = 2_000
        val result = gateway.poll("tx-42")!!

        assertEquals(RemoteStatus.APROVADA, result.status)
        assertEquals(payload.amount, result.amount)
    }

    @Test
    fun `authorization repetida nao duplica a transacao`() = runBlocking {
        gateway.authorize(payload)
        gateway.authorize(payload)

        assertEquals(2, backend.authorizationRequests)
        assertEquals(1, backend.transactionCount())
    }

    @Test
    fun `mesmo transactionId com outro valor eh rejeitado com 409`() = runBlocking {
        gateway.authorize(payload)
        try {
            gateway.authorize(payload.copy(amount = 9_999))
            fail("Deveria falhar")
        } catch (e: CommunicationException) {
            assertEquals(true, e.message?.contains("409"))
        }
    }

    @Test
    fun `resposta perdida do authorization - undo garante DESFEITA no backend`() = runBlocking {
        scenario = MockScenario.AUTH_NETWORK_FAILURE
        try {
            gateway.authorize(payload)
            fail("Deveria falhar")
        } catch (e: CommunicationException) {
            // O backend recebeu (e aprovaria); o terminal não sabe -> desfaz.
        }
        gateway.undo(payload)

        now = 10_000
        assertEquals(RemoteStatus.DESFEITA, gateway.poll("tx-42")!!.status)
    }

    @Test
    fun `undo antes da authorization impede aprovacao tardia`() = runBlocking {
        gateway.undo(payload)
        gateway.authorize(payload)

        now = 10_000
        assertEquals(RemoteStatus.DESFEITA, gateway.poll("tx-42")!!.status)
    }

    @Test
    fun `backend recusa corpo sem a camada AES`() {
        val request = Request.Builder()
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
}
