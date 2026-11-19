package com.raimundo.smartpos.core.model

/** Formatação em pt-BR sem depender do Locale do dispositivo (POS costuma vir em en-US). */
object Money {
    private const val CENTS_PER_REAL = 100L

    fun format(cents: Long): String {
        val negative = cents < 0
        val abs = kotlin.math.abs(cents)
        val reais =
            (abs / CENTS_PER_REAL)
                .toString()
                .reversed()
                .chunked(3)
                .joinToString(".")
                .reversed()
        val centavos = (abs % CENTS_PER_REAL).toString().padStart(2, '0')
        return (if (negative) "-R$ " else "R$ ") + "$reais,$centavos"
    }
}
