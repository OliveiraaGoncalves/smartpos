package com.raimundo.smartpos.feature.receipt

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
data class ReceiptRoute(
    val transactionId: String,
)

fun NavController.navigateToReceipt(transactionId: String) = navigate(ReceiptRoute(transactionId))

fun NavGraphBuilder.receiptScreen(
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit,
) {
    composable<ReceiptRoute> {
        val viewModel: ReceiptViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    ReceiptEvent.Dismiss -> onClose()
                    is ReceiptEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        ReceiptScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onClose = onClose,
            onReprintClick = viewModel::onReprintClick,
        )
    }
}
