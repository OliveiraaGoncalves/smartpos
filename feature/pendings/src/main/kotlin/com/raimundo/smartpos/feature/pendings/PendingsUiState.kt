package com.raimundo.smartpos.feature.pendings

import com.raimundo.smartpos.core.ui.component.TransactionRowUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PendingsUiState(
    val isLoading: Boolean = true,
    val pendings: ImmutableList<TransactionRowUi> = persistentListOf(),
) {
    val isEmpty: Boolean get() = !isLoading && pendings.isEmpty()
}

sealed interface PendingsEvent {
    data class ShowSnackbar(
        val message: String,
    ) : PendingsEvent
}
