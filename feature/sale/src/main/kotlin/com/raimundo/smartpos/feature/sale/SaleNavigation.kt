package com.raimundo.smartpos.feature.sale

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
data object SaleRoute

fun NavController.navigateToSale() = navigate(SaleRoute)

fun NavGraphBuilder.saleScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSaleSubmitted: (transactionId: String) -> Unit,
) {
    composable<SaleRoute> {
        val viewModel: SaleViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is SaleEvent.SaleSubmitted -> onSaleSubmitted(event.transactionId)
                    is SaleEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        SaleScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onDigit = viewModel::onDigit,
            onBackspace = viewModel::onBackspace,
            onPaymentTypeSelect = viewModel::onPaymentTypeSelect,
            onPayClick = viewModel::onPayClick,
        )
    }
}
