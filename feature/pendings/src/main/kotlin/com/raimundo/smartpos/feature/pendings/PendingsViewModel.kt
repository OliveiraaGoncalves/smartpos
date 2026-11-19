package com.raimundo.smartpos.feature.pendings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionsUseCase
import com.raimundo.smartpos.core.domain.usecase.RetryPendingUndoUseCase
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.ui.component.StatusTone
import com.raimundo.smartpos.core.ui.component.StatusUi
import com.raimundo.smartpos.core.ui.component.TransactionRowUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingsViewModel
    @Inject
    constructor(
        observeTransactions: ObserveTransactionsUseCase,
        private val retryPendingUndo: RetryPendingUndoUseCase,
    ) : ViewModel() {
        private val events = Channel<PendingsEvent>(Channel.BUFFERED)
        val eventFlow = events.receiveAsFlow()

        val uiState: StateFlow<PendingsUiState> =
            observeTransactions()
                .map { transactions ->
                    PendingsUiState(
                        isLoading = false,
                        pendings =
                            transactions
                                .filter { it.status == TransactionStatus.DESFAZIMENTO_PENDENTE }
                                .map { it.toRowUi() }
                                .toImmutableList(),
                    )
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), PendingsUiState())

        fun onRetryAllClick() {
            viewModelScope.launch {
                retryPendingUndo(null)
                events.send(PendingsEvent.ShowSnackbar("Tentando desfazer todas as pendências…"))
            }
        }

        fun onRetryOneClick(transactionId: String) {
            viewModelScope.launch { retryPendingUndo(transactionId) }
        }

        private fun Transaction.toRowUi() =
            TransactionRowUi(
                id = id,
                subtitle = paymentType.label,
                amountCents = amountCents,
                status = StatusUi("Pendente", StatusTone.WARNING),
            )

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
