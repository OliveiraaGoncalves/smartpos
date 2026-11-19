package com.raimundo.smartpos.core.common

/** Injetável para testes de tempo virtual (`kotlinx-coroutines-test`) e write-ahead com timestamp. */
interface Clock {
    fun nowMillis(): Long
}

class SystemClock : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
