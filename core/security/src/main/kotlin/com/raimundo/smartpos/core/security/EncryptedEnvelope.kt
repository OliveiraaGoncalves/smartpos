package com.raimundo.smartpos.core.security

import kotlinx.serialization.Serializable

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
