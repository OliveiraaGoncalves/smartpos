package com.raimundo.smartpos.data.crypto

import kotlinx.serialization.Serializable
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString
import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Envelope que trafega no corpo HTTP no lugar do JSON em claro.
 * `data` = ciphertext || tag GCM (128 bits), em Base64.
 */
@Serializable
data class EncryptedEnvelope(
    val kid: String,
    val alg: String,
    val iv: String,
    val data: String,
)

interface KeyProvider {
    val activeKeyId: String
    fun keyFor(keyId: String): SecretKey
}

/**
 * Chave fixa – somente para desenvolvimento/mock. Em produção a chave seria injetada
 * por terminal (KMS/HSM) e guardada no Android Keystore (ver README).
 */
class StaticKeyProvider(
    override val activeKeyId: String,
    keyBytes: ByteArray,
) : KeyProvider {
    private val key: SecretKey

    init {
        require(keyBytes.size == 32) { "AES-256 exige chave de 32 bytes (recebido ${keyBytes.size})" }
        key = SecretKeySpec(keyBytes.copyOf(), "AES")
    }

    override fun keyFor(keyId: String): SecretKey {
        if (keyId != activeKeyId) throw GeneralSecurityException("Chave desconhecida: $keyId")
        return key
    }
}

interface PayloadCipher {
    fun encrypt(plaintext: ByteArray, aad: ByteArray): EncryptedEnvelope
    fun decrypt(envelope: EncryptedEnvelope, aad: ByteArray): ByteArray
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

    override fun encrypt(plaintext: ByteArray, aad: ByteArray): EncryptedEnvelope {
        val iv = ByteArray(IV_BYTES).also { random.nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keys.keyFor(keys.activeKeyId), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(aad)
        val sealed = cipher.doFinal(plaintext)
        return EncryptedEnvelope(
            kid = keys.activeKeyId,
            alg = ALGORITHM,
            iv = iv.toByteString().base64(),
            data = sealed.toByteString().base64(),
        )
    }

    override fun decrypt(envelope: EncryptedEnvelope, aad: ByteArray): ByteArray {
        if (envelope.alg != ALGORITHM) throw GeneralSecurityException("Algoritmo não suportado: ${envelope.alg}")
        val iv = envelope.iv.decodeBase64()?.toByteArray()
            ?: throw GeneralSecurityException("IV inválido")
        if (iv.size != IV_BYTES) throw GeneralSecurityException("IV com tamanho inválido")
        val sealed = envelope.data.decodeBase64()?.toByteArray()
            ?: throw GeneralSecurityException("Payload inválido")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, keys.keyFor(envelope.kid), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(aad)
        return cipher.doFinal(sealed) // AEADBadTagException se houver adulteração
    }

    companion object {
        const val ALGORITHM = "A256GCM"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_BYTES = 12
        private const val TAG_BITS = 128
    }
}

object Aad {
    fun of(method: String, path: String, transactionId: String): ByteArray =
        "$method $path|$transactionId".toByteArray(Charsets.UTF_8)
}

fun String.hexToBytes(): ByteArray {
    require(length % 2 == 0) { "Hex com tamanho ímpar" }
    return ByteArray(length / 2) { i -> substring(i * 2, i * 2 + 2).toInt(16).toByte() }
}
