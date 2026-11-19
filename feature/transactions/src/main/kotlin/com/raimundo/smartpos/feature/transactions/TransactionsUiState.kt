package com.raimundo.smartpos.feature.transactions

import com.raimundo.smartpos.core.ui.component.TransactionRowUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class TransactionFilter(
    val label: String,
) {
    ALL("Todas"),
    IN_PROGRESS("Em andamento"),
    APPROVED("Aprovadas"),
    UNDONE("Desfeitas"),
    REJECTED("Rejeitadas"),
}

data class DayGroup(
    val dayLabel: String,
    val items: ImmutableList<TransactionRowUi>,
)

data class TransactionsUiState(
    val isLoading: Boolean = true,
    val filter: TransactionFilter = TransactionFilter.ALL,
    val groups: ImmutableList<DayGroup> = persistentListOf(),
    val totalApprovedCents: Long = 0,
) {
    val isEmpty: Boolean get() = !isLoading && groups.isEmpty()
}
