package com.raimundo.smartpos.data.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.security.GeneralSecurityException

class AesGcmPayloadCipherTest {

    private val key = ByteArray(32) { it.toByte() }
    private val cipher = AesGcmPayloadCipher(StaticKeyProvider("k1", key))
    private val aad = Aad.of("POST", "/authorization", "tx-1")
    private val plain = """{"transactionId":"tx-1","amount":1000}""".encodeToByteArray()

    @Test
    fun `cifra e decifra`() {
        assertArrayEquals(plain, cipher.decrypt(cipher.encrypt(plain, aad), aad))
    }

    @Test
    fun `IV aleatorio - mesma mensagem gera ciphertexts diferentes`() {
        val a = cipher.encrypt(plain, aad)
        val b = cipher.encrypt(plain, aad)
        assertNotEquals(a.iv, b.iv)
        assertNotEquals(a.data, b.data)
    }

    @Test(expected = GeneralSecurityException::class)
    fun `ciphertext adulterado eh rejeitado`() {
        val envelope = cipher.encrypt(plain, aad)
        val tampered = envelope.data.toCharArray().also { it[0] = if (it[0] == 'A') 'B' else 'A' }.concatToString()
        cipher.decrypt(envelope.copy(data = tampered), aad)
    }

    @Test(expected = GeneralSecurityException::class)
    fun `corpo de authorization nao pode ser reaproveitado no undo`() {
        val envelope = cipher.encrypt(plain, aad)
        cipher.decrypt(envelope, Aad.of("POST", "/undo", "tx-1"))
    }

    @Test(expected = GeneralSecurityException::class)
    fun `chave diferente nao decifra`() {
        val other = AesGcmPayloadCipher(StaticKeyProvider("k1", ByteArray(32) { 7 }))
        other.decrypt(cipher.encrypt(plain, aad), aad)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `exige chave de 256 bits`() {
        StaticKeyProvider("k1", ByteArray(16))
    }
}
