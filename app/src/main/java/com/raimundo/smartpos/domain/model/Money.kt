package com.raimundo.smartpos.domain.model

/** Formatação em pt-BR sem depender do Locale do dispositivo (POS costuma vir em en-US). */
object Money {
    fun format(cents: Long): String {
        val negative = cents < 0
        val abs = kotlin.math.abs(cents)
        val reais = (abs / 100).toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
        val centavos = (abs % 100).toString().padStart(2, '0')
        return (if (negative) "-R$ " else "R$ ") + "$reais,$centavos"
    }
}
