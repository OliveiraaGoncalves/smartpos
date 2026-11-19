package com.raimundo.smartpos.feature.receipt

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionUseCase
import com.raimundo.smartpos.core.domain.usecase.ReprintReceiptUseCase
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.testing.FakeTransactionRepository
import com.raimundo.smartpos.core.testing.MainDispatcherRule
import com.raimundo.smartpos.core.testing.transaction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReceiptViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTransactionRepository()

    private fun viewModel() =
        ReceiptViewModel(
            savedStateHandle = SavedStateHandle(mapOf("transactionId" to "tx-1")),
            observeTransaction = ObserveTransactionUseCase(repository),
            reprintReceipt = ReprintReceiptUseCase(repository),
        )

    @Test
    fun `comprovante contem o id da transacao`() =
        runTest {
            repository.publish(transaction(id = "tx-1").copy(status = TransactionStatus.APROVADA))

            viewModel().uiState.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                assertTrue(state.receiptText.contains("tx-1"))
            }
        }

    @Test
    fun `reimprimir chama o use case e avisa`() =
        runTest {
            repository.publish(transaction(id = "tx-1").copy(status = TransactionStatus.APROVADA))
            val vm = viewModel()

            vm.eventFlow.test {
                vm.onReprintClick()
                val event = awaitItem()
                assertTrue(event is ReceiptEvent.ShowSnackbar)
            }
            assertEquals(listOf("tx-1"), repository.reprintReceiptCalls)
        }
}
