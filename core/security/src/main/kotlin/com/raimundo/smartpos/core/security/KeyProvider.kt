package com.raimundo.smartpos.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

interface KeyProvider {
    val activeKeyId: String

    fun keyFor(keyId: String): SecretKey
}

/**
 * Chave fixa – somente para desenvolvimento/mock (flavor `mock`, ver `:app`). Em produção,
 * [AndroidKeystoreKeyProvider] é usado: a chave real é injetada por terminal e nunca
 * sai do Keystore em texto claro.
 */
class StaticKeyProvider(
    override val activeKeyId: String,
    keyBytes: ByteArray,
) : KeyProvider {
    private val key: SecretKey

    init {
        require(keyBytes.size == AES_256_KEY_BYTES) {
            "AES-256 exige chave de 32 bytes (recebido ${keyBytes.size})"
        }
        key = SecretKeySpec(keyBytes.copyOf(), "AES")
    }

    override fun keyFor(keyId: String): SecretKey {
        if (keyId != activeKeyId) throw GeneralSecurityException("Chave desconhecida: $keyId")
        return key
    }

    private companion object {
        const val AES_256_KEY_BYTES = 32
    }
}

/**
 * Chave AES-256 não exportável, gerada e mantida pelo Android Keystore (`AndroidKeyStore`).
 * A chave nunca existe em texto claro fora do hardware/TEE do dispositivo.
 */
class AndroidKeystoreKeyProvider(
    override val activeKeyId: String = DEFAULT_KEY_ALIAS,
) : KeyProvider {
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }

    override fun keyFor(keyId: String): SecretKey {
        if (keyId != activeKeyId) throw GeneralSecurityException("Chave desconhecida: $keyId")
        return (keyStore.getKey(keyId, null) as? SecretKey) ?: generateKey(keyId)
    }

    private fun generateKey(alias: String): SecretKey {
        val spec =
            KeyGenParameterSpec
                .Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

        return KeyGenerator
            .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
            .apply { init(spec) }
            .generateKey()
    }

    companion object {
        const val DEFAULT_KEY_ALIAS = "smartpos-payload-key"
        private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    }
}

fun String.hexToBytes(): ByteArray {
    require(length % 2 == 0) { "Hex com tamanho ímpar" }
    return ByteArray(length / 2) { i -> substring(i * 2, i * 2 + 2).toInt(16).toByte() }
}
