package com.raimundo.smartpos.feature.home

import com.raimundo.smartpos.core.ui.component.TransactionRowUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HomeUiState(
    val isLoading: Boolean = true,
    val transactions: ImmutableList<TransactionRowUi> = persistentListOf(),
    val approvedTodayCents: Long = 0,
    val hideValues: Boolean = false,
    val isOnline: Boolean = true,
    val hasPendingUndo: Boolean = false,
) {
    val isEmpty: Boolean get() = !isLoading && transactions.isEmpty()
}

sealed interface HomeEvent {
    data object NavigateToSale : HomeEvent

    data class NavigateToDetail(
        val transactionId: String,
    ) : HomeEvent

    data object NavigateToTransactions : HomeEvent

    data object NavigateToPendings : HomeEvent

    data class ShowSnackbar(
        val message: String,
    ) : HomeEvent
}
