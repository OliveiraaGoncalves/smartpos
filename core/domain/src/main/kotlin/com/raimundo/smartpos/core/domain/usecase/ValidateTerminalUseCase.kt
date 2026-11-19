package com.raimundo.smartpos.core.domain.usecase

import com.raimundo.smartpos.core.model.TerminalConfig
import javax.inject.Inject

sealed interface TerminalValidation {
    data class Valid(
        val config: TerminalConfig,
    ) : TerminalValidation

    data object Invalid : TerminalValidation
}

/** Nunca lança: terminal mal configurado deve levar à tela de bloqueio, não a um crash (cenário 22). */
class ValidateTerminalUseCase
    @Inject
    constructor() {
        operator fun invoke(
            merchantId: String,
            terminalId: String,
        ): TerminalValidation {
            val valid =
                TerminalConfig.MERCHANT_ID_REGEX.matches(merchantId) &&
                    TerminalConfig.TERMINAL_ID_REGEX.matches(terminalId)
            return if (valid) {
                TerminalValidation.Valid(TerminalConfig(merchantId, terminalId))
            } else {
                TerminalValidation.Invalid
            }
        }
    }
