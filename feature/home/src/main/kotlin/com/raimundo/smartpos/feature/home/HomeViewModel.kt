package com.raimundo.smartpos.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.core.common.Clock
import com.raimundo.smartpos.core.domain.usecase.ObserveConnectivityUseCase
import com.raimundo.smartpos.core.domain.usecase.ObserveHideValuesUseCase
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionsUseCase
import com.raimundo.smartpos.core.domain.usecase.ToggleHideValuesUseCase
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.ui.component.TransactionRowUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        observeTransactions: ObserveTransactionsUseCase,
        observeHideValues: ObserveHideValuesUseCase,
        observeConnectivity: ObserveConnectivityUseCase,
        private val toggleHideValues: ToggleHideValuesUseCase,
        private val clock: Clock,
    ) : ViewModel() {
        private val events = Channel<HomeEvent>(Channel.BUFFERED)
        val eventFlow = events.receiveAsFlow()

        val uiState: StateFlow<HomeUiState> =
            combine(
                observeTransactions(),
                observeHideValues(),
                observeConnectivity(),
            ) { transactions, hideValues, isOnline ->
                val sorted = transactions.sortedByDescending(Transaction::createdAtMillis)
                HomeUiState(
                    isLoading = false,
                    transactions = sorted.map { it.toRowUi() }.toImmutableList(),
                    approvedTodayCents =
                        sorted
                            .filter { it.status == TransactionStatus.APROVADA && isToday(it.updatedAtMillis) }
                            .sumOf(Transaction::amountCents),
                    hideValues = hideValues,
                    isOnline = isOnline,
                    hasPendingUndo = sorted.any { it.status == TransactionStatus.DESFAZIMENTO_PENDENTE },
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

        fun onSellClick() {
            viewModelScope.launch {
                if (uiState.value.isOnline) {
                    events.send(HomeEvent.NavigateToSale)
                } else {
                    events.send(HomeEvent.ShowSnackbar("Sem conexão. Aguarde a rede voltar."))
                }
            }
        }

        fun onTransactionClick(transactionId: String) {
            viewModelScope.launch { events.send(HomeEvent.NavigateToDetail(transactionId)) }
        }

        fun onSeeAllClick() {
            viewModelScope.launch { events.send(HomeEvent.NavigateToTransactions) }
        }

        fun onPendingBannerClick() {
            viewModelScope.launch { events.send(HomeEvent.NavigateToPendings) }
        }

        fun onToggleHideValues() {
            viewModelScope.launch { toggleHideValues(!uiState.value.hideValues) }
        }

        private fun isToday(epochMillis: Long): Boolean {
            val today = Calendar.getInstance().apply { timeInMillis = clock.nowMillis() }
            val that = Calendar.getInstance().apply { timeInMillis = epochMillis }
            return today.get(Calendar.YEAR) == that.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == that.get(Calendar.DAY_OF_YEAR)
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
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.forLanguageTag("pt-BR"))
        }
    }
