package com.raimundo.smartpos.feature.sale

import app.cash.turbine.test
import com.raimundo.smartpos.core.common.IdGenerator
import com.raimundo.smartpos.core.domain.usecase.StartSaleUseCase
import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.testing.FakeTransactionRepository
import com.raimundo.smartpos.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class SequentialIdGenerator : IdGenerator {
    private var counter = 0

    override fun newId(): String = "tx-${counter++}"
}

class SaleViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTransactionRepository()
    private val startSale = StartSaleUseCase(repository, SequentialIdGenerator())

    private fun viewModel() = SaleViewModel(startSale)

    @Test
    fun `digitar monta o valor em centavos`() {
        val vm = viewModel()
        vm.onDigit("1")
        vm.onDigit("0")
        vm.onDigit("0")
        vm.onDigit("0")

        assertEquals(1_000L, vm.uiState.value.amountCents)
    }

    @Test
    fun `apagar remove o ultimo digito`() {
        val vm = viewModel()
        vm.onDigit("1")
        vm.onDigit("0")
        vm.onBackspace()

        assertEquals(1L, vm.uiState.value.amountCents)
    }

    @Test
    fun `valor no limite bloqueia mais digitos e avisa`() =
        runTest {
            val vm = viewModel()
            repeat(9) { vm.onDigit("9") }
            assertEquals(SaleUiState.MAX_AMOUNT_CENTS, vm.uiState.value.amountCents)

            vm.eventFlow.test {
                vm.onDigit("9")
                val event = awaitItem()
                assertTrue(event is SaleEvent.ShowSnackbar)
            }
            assertEquals(SaleUiState.MAX_AMOUNT_CENTS, vm.uiState.value.amountCents)
        }

    @Test
    fun `venda incompleta desabilita pagar e toque avisa o motivo`() =
        runTest {
            val vm = viewModel()

            assertFalse(vm.uiState.value.canPay)

            vm.eventFlow.test {
                vm.onPayClick()
                val event = awaitItem()
                assertTrue(event is SaleEvent.ShowSnackbar)
                assertEquals(
                    "Informe o valor e a forma de pagamento.",
                    (event as SaleEvent.ShowSnackbar).message,
                )
            }
        }

    @Test
    fun `toque duplo em pagar gera uma unica transacao`() =
        runTest {
            val vm = viewModel()
            vm.onDigit("1")
            vm.onDigit("0")
            vm.onDigit("0")
            vm.onDigit("0")
            vm.onPaymentTypeSelect(PaymentType.CREDITO)

            vm.eventFlow.test {
                vm.onPayClick()
                vm.onPayClick() // segundo toque, antes da coroutine da primeira venda terminar
                awaitItem()
            }

            assertEquals(1, repository.startSaleCalls.size)
        }
}
