package com.raimundo.smartpos.feature.transactions

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
data object TransactionsRoute

/** Raiz de aba: ver HomeNavigation.navigateToHome para o porquê do popUpTo/singleTop/restoreState. */
fun NavController.navigateToTransactions() =
    navigate(TransactionsRoute) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

fun NavGraphBuilder.transactionsScreen(
    onTransactionClick: (String) -> Unit,
    onBottomNavigate: (BottomDestination) -> Unit,
) {
    composable<TransactionsRoute> {
        val viewModel: TransactionsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        TransactionsScreen(
            state = state,
            onFilterSelect = viewModel::onFilterSelect,
            onTransactionClick = onTransactionClick,
            onBottomNavigate = onBottomNavigate,
        )
    }
}
