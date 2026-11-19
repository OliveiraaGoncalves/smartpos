package com.raimundo.smartpos.feature.pendings

import app.cash.turbine.test
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionsUseCase
import com.raimundo.smartpos.core.domain.usecase.RetryPendingUndoUseCase
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.testing.FakeTransactionRepository
import com.raimundo.smartpos.core.testing.MainDispatcherRule
import com.raimundo.smartpos.core.testing.transaction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PendingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTransactionRepository()

    private fun viewModel() =
        PendingsViewModel(
            ObserveTransactionsUseCase(repository),
            RetryPendingUndoUseCase(repository),
        )

    @Test
    fun `mostra apenas transacoes com desfazimento pendente`() =
        runTest {
            repository.publish(transaction(id = "tx-1").copy(status = TransactionStatus.DESFAZIMENTO_PENDENTE))
            repository.publish(transaction(id = "tx-2").copy(status = TransactionStatus.APROVADA))

            viewModel().uiState.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                assertEquals(listOf("tx-1"), state.pendings.map { it.id })
            }
        }

    @Test
    fun `tentar desfazer todas chama o use case sem id especifico`() =
        runTest {
            val vm = viewModel()
            vm.eventFlow.test {
                vm.onRetryAllClick()
                awaitItem()
            }
            assertEquals(listOf<String?>(null), repository.retryPendingUndoCalls)
            assertNull(repository.retryPendingUndoCalls.single())
        }
}
