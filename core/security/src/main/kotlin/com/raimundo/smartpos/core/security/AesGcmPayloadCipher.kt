package com.raimundo.smartpos.core.security

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

interface PayloadCipher {
    fun encrypt(
        plaintext: ByteArray,
        aad: ByteArray,
    ): EncryptedEnvelope

    fun decrypt(
        envelope: EncryptedEnvelope,
        aad: ByteArray,
    ): ByteArray
}

/**
 * AES-256-GCM: confidencialidade + integridade (AEAD).
 * - IV de 96 bits aleatório por mensagem (nunca reutilizado com a mesma chave).
 * - AAD amarra o ciphertext ao endpoint e ao transactionId: um corpo capturado de
 *   /authorization não pode ser reenviado para /undo nem associado a outra transação.
 */
class AesGcmPayloadCipher(
    private val keys: KeyProvider,
    private val random: SecureRandom = SecureRandom(),
) : PayloadCipher {
    override fun encrypt(
        plaintext: ByteArray,
        aad: ByteArray,
    ): EncryptedEnvelope {
        val iv = ByteArray(IV_BYTES).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keys.keyFor(keys.activeKeyId), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(aad)
        val sealed = cipher.doFinal(plaintext)
        return EncryptedEnvelope(
            kid = keys.activeKeyId,
            alg = ALGORITHM,
            iv = iv.base64(),
            data = sealed.base64(),
        )
    }

    override fun decrypt(
        envelope: EncryptedEnvelope,
        aad: ByteArray,
    ): ByteArray {
        if (envelope.alg != ALGORITHM) throw GeneralSecurityException("Algoritmo não suportado: ${envelope.alg}")
        val iv = envelope.iv.base64ToBytes()
        if (iv.size != IV_BYTES) throw GeneralSecurityException("IV com tamanho inválido")
        val sealed = envelope.data.base64ToBytes()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keys.keyFor(envelope.kid), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(aad)
        return cipher.doFinal(sealed) // AEADBadTagException se houver adulteração
    }

    private fun ByteArray.base64(): String = Base64Codec.encode(this)

    private fun String.base64ToBytes(): ByteArray =
        try {
            Base64Codec.decode(this)
        } catch (e: IllegalArgumentException) {
            throw GeneralSecurityException("Base64 inválido", e)
        }

    companion object {
        const val ALGORITHM = "A256GCM"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_BYTES = 12
        private const val TAG_BITS = 128
    }
}

object Aad {
    fun of(
        method: String,
        path: String,
        transactionId: String,
    ): ByteArray = "$method $path|$transactionId".toByteArray(Charsets.UTF_8)
}
