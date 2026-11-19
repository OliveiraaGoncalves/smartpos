package com.raimundo.smartpos.core.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Cenário 22: terminal mal configurado nunca pode travar o app, só bloquear a venda. */
class ValidateTerminalUseCaseTest {
    private val useCase = ValidateTerminalUseCase()

    @Test
    fun `merchantId e terminalId validos retornam Valid`() {
        val result = useCase("123456789012345", "12345678")

        assertTrue(result is TerminalValidation.Valid)
        val valid = result as TerminalValidation.Valid
        assertEquals("123456789012345", valid.config.merchantId)
        assertEquals("12345678", valid.config.terminalId)
    }

    @Test
    fun `merchantId com tamanho errado retorna Invalid sem lancar`() {
        val result = useCase("123", "12345678")

        assertEquals(TerminalValidation.Invalid, result)
    }

    @Test
    fun `terminalId nao numerico retorna Invalid sem lancar`() {
        val result = useCase("123456789012345", "ABCDEFGH")

        assertEquals(TerminalValidation.Invalid, result)
    }

    @Test
    fun `merchantId e terminalId vazios retornam Invalid sem lancar`() {
        val result = useCase("", "")

        assertEquals(TerminalValidation.Invalid, result)
    }
}
