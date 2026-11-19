package com.raimundo.smartpos.core.common

import javax.inject.Inject

/** Injetável para testes de tempo virtual (`kotlinx-coroutines-test`) e write-ahead com timestamp. */
interface Clock {
    fun nowMillis(): Long
}

class SystemClock
    @Inject
    constructor() : Clock {
        override fun nowMillis(): Long = System.currentTimeMillis()
    }
