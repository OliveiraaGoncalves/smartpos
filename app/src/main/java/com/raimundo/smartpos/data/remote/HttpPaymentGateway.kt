package com.raimundo.smartpos.data.remote

import com.raimundo.smartpos.data.crypto.Aad
import com.raimundo.smartpos.data.crypto.AesGcmPayloadCipher
import com.raimundo.smartpos.data.crypto.EncryptedEnvelope
import com.raimundo.smartpos.data.crypto.PayloadCipher
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.GeneralSecurityException

class HttpPaymentGateway(
    private val client: OkHttpClient,
    private val baseUrl: HttpUrl,
    private val cipher: PayloadCipher,
    private val json: Json = SmartPosJson,
) : PaymentGateway {

    init {
        require(baseUrl.isHttps) { "O backend deve ser acessado via HTTPS" }
    }

    override suspend fun authorize(payload: TransactionPayload) =
        postEncrypted(Endpoints.AUTHORIZATION, payload)

    override suspend fun undo(payload: TransactionPayload) =
        postEncrypted(Endpoints.UNDO, payload)

    override suspend fun poll(transactionId: String): PollingResponse? = communication(Endpoints.POLLING) {
        val url = baseUrl.newBuilder()
            .addPathSegment(Endpoints.POLLING.removePrefix("/"))
            .addQueryParameter("transactionId", transactionId)
            .build()
        val request = Request.Builder()
            .url(url)
            .get()
            .header(SmartPosHeaders.TRANSACTION_ID, transactionId)
            .build()

        client.newCall(request).await().use { response ->
            when {
                // Premissa documentada no README: 204 = ainda sem resultado final.
                response.code == HTTP_NO_CONTENT -> null
                response.isSuccessful -> {
                    val envelope = json.decodeFromString<EncryptedEnvelope>(response.body?.string().orEmpty())
                    val plain = cipher.decrypt(envelope, Aad.of("GET", Endpoints.POLLING, transactionId))
                    json.decodeFromString<PollingResponse>(plain.decodeToString())
                }
                else -> throw CommunicationException("${Endpoints.POLLING} retornou HTTP ${response.code}")
            }
        }
    }

    private suspend fun postEncrypted(path: String, payload: TransactionPayload) = communication(path) {
        val transactionId = payload.transactionId
        val envelope = cipher.encrypt(
            plaintext = json.encodeToString(payload).encodeToByteArray(),
            aad = Aad.of("POST", path, transactionId),
        )
        val url = baseUrl.newBuilder().addPathSegment(path.removePrefix("/")).build()
        val request = Request.Builder()
            .url(url)
            .post(json.encodeToString(envelope).toRequestBody(JSON_MEDIA_TYPE))
            .header(SmartPosHeaders.TRANSACTION_ID, transactionId)
            .header(SmartPosHeaders.IDEMPOTENCY_KEY, transactionId)
            .header(SmartPosHeaders.PAYLOAD_ENCRYPTION, AesGcmPayloadCipher.ALGORITHM)
            .build()

        client.newCall(request).await().use { response ->
            if (!response.isSuccessful) throw CommunicationException("$path retornou HTTP ${response.code}")
        }
    }

    /** Normaliza todas as falhas de comunicação em [CommunicationException]. Cancelamento passa direto. */
    private inline fun <T> communication(operation: String, block: () -> T): T = try {
        block()
    } catch (e: CommunicationException) {
        throw e
    } catch (e: IOException) {
        throw CommunicationException("Falha de rede em $operation: ${e.message}", e)
    } catch (e: GeneralSecurityException) {
        throw CommunicationException("Falha de criptografia em $operation", e)
    } catch (e: IllegalArgumentException) { // inclui SerializationException
        throw CommunicationException("Resposta inválida em $operation", e)
    }

    private companion object {
        const val HTTP_NO_CONTENT = 204
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
