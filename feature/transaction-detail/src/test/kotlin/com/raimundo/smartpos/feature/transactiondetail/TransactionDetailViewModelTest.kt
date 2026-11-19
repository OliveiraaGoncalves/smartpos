package com.raimundo.smartpos.feature.transactiondetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionUseCase
import com.raimundo.smartpos.core.domain.usecase.RetryPendingUndoUseCase
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.testing.FakeTransactionRepository
import com.raimundo.smartpos.core.testing.MainDispatcherRule
import com.raimundo.smartpos.core.testing.transaction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TransactionDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTransactionRepository()

    private fun viewModel(transactionId: String = "tx-1") =
        TransactionDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("transactionId" to transactionId)),
            observeTransaction = ObserveTransactionUseCase(repository),
            retryPendingUndo = RetryPendingUndoUseCase(repository),
        )

    @Test
    fun `aprovada permite reimprimir mas nao desfazer agora`() =
        runTest {
            repository.publish(transaction(id = "tx-1").copy(status = TransactionStatus.APROVADA))

            viewModel().uiState.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                assertTrue(state.canReprint)
                assertEquals(false, state.canRetryUndoNow)
            }
        }

    @Test
    fun `desfazimento pendente permite tentar desfazer agora`() =
        runTest {
            repository.publish(transaction(id = "tx-1").copy(status = TransactionStatus.DESFAZIMENTO_PENDENTE))

            viewModel().uiState.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                assertTrue(state.canRetryUndoNow)
                assertEquals(false, state.canReprint)
            }
        }

    @Test
    fun `tentar desfazer agora chama o use case com o id da transacao`() =
        runTest {
            repository.publish(transaction(id = "tx-1").copy(status = TransactionStatus.DESFAZIMENTO_PENDENTE))
            val vm = viewModel()

            vm.eventFlow.test {
                vm.onRetryUndoNowClick()
                awaitItem()
            }

            assertEquals(listOf("tx-1"), repository.retryPendingUndoCalls)
        }
}
