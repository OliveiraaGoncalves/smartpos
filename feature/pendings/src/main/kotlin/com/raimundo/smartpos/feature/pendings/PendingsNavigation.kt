package com.raimundo.smartpos.feature.pendings

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
data object PendingsRoute

fun NavController.navigateToPendings() = navigate(PendingsRoute)

fun NavGraphBuilder.pendingsScreen(
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
) {
    composable<PendingsRoute> {
        val viewModel: PendingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is PendingsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        PendingsScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onBack = onBack,
            onTransactionClick = onTransactionClick,
            onRetryAllClick = viewModel::onRetryAllClick,
        )
    }
}
