package com.raimundo.smartpos.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionsUseCase
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.ui.component.TransactionRowUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel
    @Inject
    constructor(
        observeTransactions: ObserveTransactionsUseCase,
    ) : ViewModel() {
        private val filter = MutableStateFlow(TransactionFilter.ALL)

        val uiState: StateFlow<TransactionsUiState> =
            combine(observeTransactions(), filter) { transactions, selected ->
                val filtered = transactions.filter { matches(it.status, selected) }
                val grouped =
                    filtered
                        .sortedByDescending(Transaction::createdAtMillis)
                        .groupBy { dayFormatter.format(Date(it.createdAtMillis)) }
                        .map { (day, items) -> DayGroup(day, items.map { it.toRowUi() }.toImmutableList()) }
                        .toImmutableList()

                TransactionsUiState(
                    isLoading = false,
                    filter = selected,
                    groups = grouped,
                    totalApprovedCents =
                        transactions
                            .filter { it.status == TransactionStatus.APROVADA }
                            .sumOf(Transaction::amountCents),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TransactionsUiState())

        fun onFilterSelect(newFilter: TransactionFilter) {
            filter.value = newFilter
        }

        private fun matches(
            status: TransactionStatus,
            selected: TransactionFilter,
        ): Boolean =
            when (selected) {
                TransactionFilter.ALL -> true
                TransactionFilter.IN_PROGRESS -> !status.isFinal
                TransactionFilter.APPROVED -> status == TransactionStatus.APROVADA
                TransactionFilter.UNDONE -> status == TransactionStatus.DESFEITA
                TransactionFilter.REJECTED -> status == TransactionStatus.REJEITADA
            }

        private fun Transaction.toRowUi() =
            TransactionRowUi(
                id = id,
                subtitle = "${paymentType.label} · ${timeFormatter.format(Date(createdAtMillis))}",
                amountCents = amountCents,
                status = status.toStatusUi(),
            )

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
            val dayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR"))
        }
    }
