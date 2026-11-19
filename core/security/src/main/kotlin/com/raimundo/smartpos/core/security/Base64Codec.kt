package com.raimundo.smartpos.core.security

/**
 * Base64 (RFC 4648, com padding) sem depender de `android.util.Base64` (exige Robolectric
 * nos testes de JVM) nem de `java.util.Base64` (só a partir da API 26). Portável entre o
 * teste unitário (JVM puro) e qualquer dispositivo Android a partir do minSdk 24.
 */
internal object Base64Codec {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    private val DECODE_TABLE =
        IntArray(128).apply {
            fill(-1)
            ALPHABET.forEachIndexed { index, c -> this[c.code] = index }
        }

    fun encode(bytes: ByteArray): String {
        val out = StringBuilder((bytes.size + 2) / 3 * 4)
        var i = 0
        while (i < bytes.size) {
            val b0 = bytes[i].toInt() and 0xFF
            val b1 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < bytes.size) bytes[i + 2].toInt() and 0xFF else 0
            val triple = (b0 shl 16) or (b1 shl 8) or b2

            out.append(ALPHABET[(triple shr 18) and 0x3F])
            out.append(ALPHABET[(triple shr 12) and 0x3F])
            out.append(if (i + 1 < bytes.size) ALPHABET[(triple shr 6) and 0x3F] else '=')
            out.append(if (i + 2 < bytes.size) ALPHABET[triple and 0x3F] else '=')
            i += 3
        }
        return out.toString()
    }

    fun decode(text: String): ByteArray {
        val clean = text.trimEnd('=')
        require(clean.all { it.code < 128 && DECODE_TABLE[it.code] >= 0 }) { "Caractere Base64 inválido" }
        val out = ArrayList<Byte>(clean.length * 3 / 4 + 3)
        var buffer = 0
        var bits = 0
        for (c in clean) {
            buffer = (buffer shl 6) or DECODE_TABLE[c.code]
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out.add(((buffer shr bits) and 0xFF).toByte())
            }
        }
        return out.toByteArray()
    }
}
