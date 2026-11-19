package com.raimundo.smartpos.core.domain.engine

data class EngineConfig(
    /** Prazo total para obter resultado final, contado a partir do envio. */
    val resultTimeoutMs: Long = 15_000,
    val initialPollDelayMs: Long = 500,
    val pollIntervalMs: Long = 1_000,
    val undoMaxAttempts: Int = 3,
    val undoRetryBaseDelayMs: Long = 1_000,
)
