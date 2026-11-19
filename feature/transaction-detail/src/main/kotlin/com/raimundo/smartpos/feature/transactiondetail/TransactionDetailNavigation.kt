package com.raimundo.smartpos.feature.transactiondetail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDetailRoute(
    val transactionId: String,
)

fun NavController.navigateToTransactionDetail(transactionId: String) = navigate(TransactionDetailRoute(transactionId))

fun NavGraphBuilder.transactionDetailScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onShowReceipt: (String) -> Unit,
) {
    composable<TransactionDetailRoute> {
        val viewModel: TransactionDetailViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is TransactionDetailEvent.ShowReceipt -> onShowReceipt(event.transactionId)
                    is TransactionDetailEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        TransactionDetailScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onReprintClick = viewModel::onReprintClick,
            onRetryUndoNowClick = viewModel::onRetryUndoNowClick,
        )
    }
}
