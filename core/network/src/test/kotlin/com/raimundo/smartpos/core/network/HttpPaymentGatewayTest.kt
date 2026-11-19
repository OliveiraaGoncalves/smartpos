package com.raimundo.smartpos.core.network

import com.raimundo.smartpos.core.common.CommunicationException
import com.raimundo.smartpos.core.security.Aad
import com.raimundo.smartpos.core.security.AesGcmPayloadCipher
import com.raimundo.smartpos.core.security.StaticKeyProvider
import com.raimundo.smartpos.core.testing.transaction
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class HttpPaymentGatewayTest {
    private lateinit var server: MockWebServer
    private val cipher = AesGcmPayloadCipher(StaticKeyProvider("k1", ByteArray(32) { it.toByte() }))

    private val serverCertificate =
        HeldCertificate
            .Builder()
            .addSubjectAlternativeName("localhost")
            .build()
    private val serverCertificates =
        HandshakeCertificates
            .Builder()
            .heldCertificate(serverCertificate)
            .build()
    private val clientCertificates =
        HandshakeCertificates
            .Builder()
            .addTrustedCertificate(serverCertificate.certificate)
            .build()

    private val client =
        OkHttpClient
            .Builder()
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .build()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.useHttps(serverCertificates.sslSocketFactory())
        server.start()
    }

    @After
    fun tearDown() {
        runCatching { server.close() }
    }

    private fun gateway() = HttpPaymentGateway(client, server.url("/"), cipher)

    @Test
    fun `authorization com 200 nao lanca`() =
        runTest {
            server.enqueue(MockResponse(code = 200))
            gateway().authorize(transaction())
        }

    @Test
    fun `authorization com http de erro lanca CommunicationException`() {
        server.enqueue(MockResponse(code = 409))
        assertThrows(CommunicationException::class.java) {
            runTest { gateway().authorize(transaction()) }
        }
    }

    @Test
    fun `polling 204 retorna null (pendente)`() =
        runTest {
            server.enqueue(MockResponse(code = 204))
            assertNull(gateway().poll("tx-1"))
        }

    @Test
    fun `polling 200 decifra a resposta`() =
        runTest {
            val tx = transaction(id = "tx-9", amountCents = 4_321)
            val body =
                PollingResponse(
                    "Credito",
                    tx.id,
                    tx.amountCents,
                    tx.merchantId,
                    tx.terminalId,
                    RemoteStatusDto.APROVADA,
                )
            val envelope =
                cipher.encrypt(
                    SmartPosJson.encodeToString(body).encodeToByteArray(),
                    Aad.of("GET", Endpoints.POLLING, tx.id),
                )
            server.enqueue(MockResponse(code = 200, body = SmartPosJson.encodeToString(envelope)))

            val result = gateway().poll(tx.id)

            assertEquals(tx.id, result?.transactionId)
            assertEquals(4_321L, result?.amountCents)
        }

    @Test
    fun `resposta cifrada com chave errada eh falha de comunicacao`() {
        val tx = transaction()
        val outroCipher = AesGcmPayloadCipher(StaticKeyProvider("k1", ByteArray(32) { 9 }))
        val body =
            PollingResponse(
                "Credito",
                tx.id,
                tx.amountCents,
                tx.merchantId,
                tx.terminalId,
                RemoteStatusDto.APROVADA,
            )
        val envelope =
            outroCipher.encrypt(
                SmartPosJson.encodeToString(body).encodeToByteArray(),
                Aad.of("GET", Endpoints.POLLING, tx.id),
            )
        server.enqueue(MockResponse(code = 200, body = SmartPosJson.encodeToString(envelope)))

        assertThrows(CommunicationException::class.java) {
            runTest { gateway().poll(tx.id) }
        }
    }

    @Test
    fun `sem resposta do servidor vira CommunicationException`() {
        server.close() // nenhuma conexão possível
        assertThrows(CommunicationException::class.java) {
            runTest { gateway().authorize(transaction()) }
        }
    }

    @Test
    fun `exige HTTPS`() {
        assertThrows(IllegalArgumentException::class.java) {
            HttpPaymentGateway(client, "http://example.com".toHttpUrl(), cipher)
        }
    }
}
