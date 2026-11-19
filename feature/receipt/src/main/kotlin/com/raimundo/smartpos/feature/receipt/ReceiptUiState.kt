package com.raimundo.smartpos.feature.receipt

data class ReceiptUiState(
    val isLoading: Boolean = true,
    val receiptText: String = "",
)

sealed interface ReceiptEvent {
    data object Dismiss : ReceiptEvent

    data class ShowSnackbar(
        val message: String,
    ) : ReceiptEvent
}
