package com.raimundo.smartpos.feature.home

import app.cash.turbine.test
import com.raimundo.smartpos.core.domain.usecase.ObserveConnectivityUseCase
import com.raimundo.smartpos.core.domain.usecase.ObserveHideValuesUseCase
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionsUseCase
import com.raimundo.smartpos.core.domain.usecase.ToggleHideValuesUseCase
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.testing.FakeNetworkMonitor
import com.raimundo.smartpos.core.testing.FakeTransactionRepository
import com.raimundo.smartpos.core.testing.FakeUserPreferences
import com.raimundo.smartpos.core.testing.MainDispatcherRule
import com.raimundo.smartpos.core.testing.TestClock
import com.raimundo.smartpos.core.testing.transaction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeTransactionRepository()
    private val networkMonitor = FakeNetworkMonitor(online = true)
    private val preferences = FakeUserPreferences()
    private var now = 0L

    private fun viewModel() =
        HomeViewModel(
            observeTransactions = ObserveTransactionsUseCase(repository),
            observeHideValues = ObserveHideValuesUseCase(preferences),
            observeConnectivity = ObserveConnectivityUseCase(networkMonitor),
            toggleHideValues = ToggleHideValuesUseCase(preferences),
            clock = TestClock { now },
        )

    @Test
    fun `home vazia quando nao ha transacoes`() =
        runTest {
            viewModel().uiState.test {
                val state = awaitItem()
                assertTrue(state.isEmpty)
            }
        }

    @Test
    fun `lista mostra transacoes publicadas`() =
        runTest {
            val vm = viewModel()
            repository.publish(transaction(id = "tx-1").copy(status = TransactionStatus.APROVADA))

            vm.uiState.test {
                val state = awaitItem()
                assertEquals(1, state.transactions.size)
                assertFalse(state.isEmpty)
            }
        }

    @Test
    fun `vender offline mostra snackbar e nao navega`() =
        runTest {
            networkMonitor.setOnline(false)
            val vm = viewModel()
            // Garante que o StateFlow (WhileSubscribed) já absorveu o estado offline do fake.
            vm.uiState.test { awaitItem() }

            vm.eventFlow.test {
                vm.onSellClick()
                val event = awaitItem()
                assertTrue(event is HomeEvent.ShowSnackbar)
            }
        }

    @Test
    fun `vender online navega para a venda`() =
        runTest {
            val vm = viewModel()

            vm.eventFlow.test {
                vm.onSellClick()
                val event = awaitItem()
                assertEquals(HomeEvent.NavigateToSale, event)
            }
        }

    @Test
    fun `banner de pendencia aparece quando ha desfazimento pendente`() =
        runTest {
            val vm = viewModel()
            repository.publish(transaction(id = "tx-pendente").copy(status = TransactionStatus.DESFAZIMENTO_PENDENTE))

            vm.uiState.test {
                val state = awaitItem()
                assertTrue(state.hasPendingUndo)
            }
        }

    @Test
    fun `alternar ocultar valores chama o use case`() =
        runTest {
            val vm = viewModel()
            vm.onToggleHideValues()

            preferences.hideValues.test {
                assertTrue(awaitItem())
            }
        }
}
