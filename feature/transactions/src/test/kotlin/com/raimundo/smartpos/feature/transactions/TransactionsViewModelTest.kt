package com.raimundo.smartpos.feature.transactions

import app.cash.turbine.test
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionsUseCase
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.testing.FakeTransactionRepository
import com.raimundo.smartpos.core.testing.MainDispatcherRule
import com.raimundo.smartpos.core.testing.transaction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TransactionsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTransactionRepository()

    private fun viewModel() = TransactionsViewModel(ObserveTransactionsUseCase(repository))

    @Test
    fun `total aprovado soma somente transacoes aprovadas`() =
        runTest {
            repository.publish(transaction(id = "tx-1", amountCents = 1_000).copy(status = TransactionStatus.APROVADA))
            repository.publish(transaction(id = "tx-2", amountCents = 2_000).copy(status = TransactionStatus.APROVADA))
            repository.publish(transaction(id = "tx-3", amountCents = 5_000).copy(status = TransactionStatus.REJEITADA))

            viewModel().uiState.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()
                assertEquals(3_000L, state.totalApprovedCents)
            }
        }

    @Test
    fun `filtro aprovadas mostra somente transacoes aprovadas`() =
        runTest {
            repository.publish(transaction(id = "tx-1").copy(status = TransactionStatus.APROVADA))
            repository.publish(transaction(id = "tx-2").copy(status = TransactionStatus.REJEITADA))
            val vm = viewModel()

            vm.uiState.test {
                var state = awaitItem()
                while (state.isLoading) state = awaitItem()

                vm.onFilterSelect(TransactionFilter.APPROVED)
                state = awaitItem()

                val allItems = state.groups.flatMap { it.items }
                assertEquals(1, allItems.size)
                assertEquals("tx-1", allItems.single().id)
            }
        }
}
