package com.raimundo.smartpos.feature.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.raimundo.smartpos.core.ui.component.BottomDestination
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/**
 * Home é raiz da navegação por abas: trocar de aba não deve empilhar uma nova instância
 * cada vez, senão o botão Voltar do Android vai desempilhando toda troca de aba já feita.
 */
fun NavController.navigateToHome() =
    navigate(HomeRoute) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

fun NavGraphBuilder.homeScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToSale: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToPendings: () -> Unit,
    onBottomNavigate: (BottomDestination) -> Unit,
) {
    composable<HomeRoute> {
        val viewModel: HomeViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    HomeEvent.NavigateToSale -> onNavigateToSale()
                    is HomeEvent.NavigateToDetail -> onNavigateToDetail(event.transactionId)
                    HomeEvent.NavigateToTransactions -> onNavigateToTransactions()
                    HomeEvent.NavigateToPendings -> onNavigateToPendings()
                    is HomeEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        HomeScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onSellClick = viewModel::onSellClick,
            onTransactionClick = viewModel::onTransactionClick,
            onSeeAllClick = viewModel::onSeeAllClick,
            onPendingBannerClick = viewModel::onPendingBannerClick,
            onToggleHideValues = viewModel::onToggleHideValues,
            onBottomNavigate = onBottomNavigate,
        )
    }
}
