package com.raimundo.smartpos.core.model

data class TerminalConfig(
    val merchantId: String,
    val terminalId: String,
) {
    init {
        require(MERCHANT_ID_REGEX.matches(merchantId)) { "merchantId deve ter 15 dígitos numéricos" }
        require(TERMINAL_ID_REGEX.matches(terminalId)) { "terminalId deve ter 8 dígitos numéricos" }
    }

    companion object {
        val MERCHANT_ID_REGEX = Regex("^\\d{15}$")
        val TERMINAL_ID_REGEX = Regex("^\\d{8}$")
    }
}
