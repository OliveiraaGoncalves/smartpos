package com.raimundo.smartpos.ui.sale

import com.raimundo.smartpos.domain.SaleLauncher
import com.raimundo.smartpos.domain.model.Money
import com.raimundo.smartpos.domain.model.PaymentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SaleViewModelTest {

    private class FakeLauncher : SaleLauncher {
        val calls = mutableListOf<Triple<String, Long, PaymentType>>()
        override fun startSale(transactionId: String, amountCents: Long, paymentType: PaymentType): Boolean {
            calls += Triple(transactionId, amountCents, paymentType)
            return true
        }
    }

    private val launcher = FakeLauncher()
    private var generated = 0
    private val viewModel = SaleViewModel(launcher) { "id-${++generated}" }

    @Test
    fun `aceita so digitos e trata como centavos`() {
        viewModel.onAmountChange("R$ 0012,34x")
        assertEquals("1234", viewModel.state.value.amountDigits)
        assertEquals(1_234L, viewModel.state.value.amountCents)
    }

    @Test
    fun `pagar exige valor e tipo de pagamento`() {
        viewModel.onAmountChange("1000")
        assertFalse(viewModel.state.value.canSubmit)
        viewModel.onPaymentTypeSelected(PaymentType.DEBITO)
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `toque duplo em pagar dispara uma unica venda`() {
        viewModel.onAmountChange("1000")
        viewModel.onPaymentTypeSelected(PaymentType.CREDITO)

        viewModel.onConfirm()
        viewModel.onConfirm()

        assertEquals(listOf(Triple("id-1", 1_000L, PaymentType.CREDITO)), launcher.calls)
        assertEquals(1, generated)
    }

    @Test
    fun `formatacao de moeda pt-BR`() {
        assertEquals("R$ 0,05", Money.format(5))
        assertEquals("R$ 12,34", Money.format(1_234))
        assertEquals("R$ 1.234.567,89", Money.format(123_456_789))
    }
}
