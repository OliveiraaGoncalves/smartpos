package com.raimundo.smartpos.core.data.repository

import com.raimundo.smartpos.core.model.TerminalConfig

/**
 * Configuração do terminal (merchantId/terminalId), validada na inicialização pelo
 * StartupGate do `:app` via `ValidateTerminalUseCase`. A implementação real (lendo
 * `BuildConfig` ou preferências) é fornecida pelo `:app` (ver Fase 8).
 */
fun interface TerminalConfigProvider {
    fun current(): TerminalConfig
}
