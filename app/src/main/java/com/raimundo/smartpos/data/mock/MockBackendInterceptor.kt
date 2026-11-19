package com.raimundo.smartpos.data.mock

import com.raimundo.smartpos.data.crypto.Aad
import com.raimundo.smartpos.data.crypto.EncryptedEnvelope
import com.raimundo.smartpos.data.crypto.PayloadCipher
import com.raimundo.smartpos.data.remote.Endpoints
import com.raimundo.smartpos.data.remote.SmartPosHeaders
import com.raimundo.smartpos.data.remote.SmartPosJson
import com.raimundo.smartpos.data.remote.TransactionPayload
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.security.GeneralSecurityException
import kotlin.random.Random

/**
 * Responde às chamadas HTTPS dentro do próprio processo, sem rede. O cliente
 * (HttpPaymentGateway) é exatamente o mesmo que falaria com o backend real:
 * para trocar, basta não registrar este interceptor e apontar API_BASE_URL.
 */
class MockBackendInterceptor(
    private val backend: MockBackend,
    private val cipher: PayloadCipher,
    private val json: Json = SmartPosJson,
    private val latencyMs: () -> Long = { Random.nextLong(100, 400) },
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val latency = latencyMs()
        if (latency > 0) Thread.sleep(latency)

        val path = request.url.encodedPath
        val result = try {
            when {
                request.method == "POST" && path == Endpoints.AUTHORIZATION ->
                    backend.authorize(decryptPayload(request, path))
                request.method == "POST" && path == Endpoints.UNDO ->
                    backend.undo(decryptPayload(request, path))
                request.method == "GET" && path == Endpoints.POLLING ->
                    request.url.queryParameter("transactionId")?.let(backend::poll) ?: MockResponse.BadRequest
                else -> MockResponse.NotFound
            }
        } catch (e: GeneralSecurityException) {
            MockResponse.BadRequest // ciphertext adulterado, AAD errado ou chave desconhecida
        } catch (e: IllegalArgumentException) {
            MockResponse.BadRequest // JSON inválido
        }

        return when (result) {
            MockResponse.NetworkFailure -> throw IOException("Falha de rede simulada pelo mock")
            MockResponse.Ok -> respond(request, 200)
            MockResponse.Pending -> respond(request, 204)
            MockResponse.BadRequest -> respond(request, 400)
            MockResponse.NotFound -> respond(request, 404)
            MockResponse.Conflict -> respond(request, 409)
            is MockResponse.Result -> {
                val transactionId = result.body.transactionId
                val envelope = cipher.encrypt(
                    plaintext = json.encodeToString(result.body).encodeToByteArray(),
                    aad = Aad.of("GET", Endpoints.POLLING, transactionId),
                )
                respond(request, 200, json.encodeToString(envelope))
            }
        }
    }

    private fun decryptPayload(request: Request, path: String): TransactionPayload {
        val headerId = request.header(SmartPosHeaders.TRANSACTION_ID)
        requireNotNull(headerId) { "Header ${SmartPosHeaders.TRANSACTION_ID} ausente" }
        val body = requireNotNull(request.body) { "Corpo ausente" }
        val raw = Buffer().also { body.writeTo(it) }.readUtf8()
        val envelope = json.decodeFromString<EncryptedEnvelope>(raw)
        val plain = cipher.decrypt(envelope, Aad.of("POST", path, headerId))
        val payload = json.decodeFromString<TransactionPayload>(plain.decodeToString())
        require(payload.transactionId == headerId) { "transactionId do corpo difere do header" }
        return payload
    }

    private fun respond(request: Request, code: Int, body: String = ""): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code in 200..299) "OK" else "Error")
            .body(body.toResponseBody(JSON))
            .build()

    private companion object {
        val JSON = "application/json".toMediaType()
    }
}
