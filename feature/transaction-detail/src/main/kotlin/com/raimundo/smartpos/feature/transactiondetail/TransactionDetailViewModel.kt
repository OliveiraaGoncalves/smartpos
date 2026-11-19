package com.raimundo.smartpos.feature.transactiondetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionUseCase
import com.raimundo.smartpos.core.domain.usecase.RetryPendingUndoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeTransaction: ObserveTransactionUseCase,
        private val retryPendingUndo: RetryPendingUndoUseCase,
    ) : ViewModel() {
        // Navigation grava os argumentos simples da rota type-safe com o próprio nome do campo.
        private val transactionId: String = checkNotNull(savedStateHandle["transactionId"])

        private val events = Channel<TransactionDetailEvent>(Channel.BUFFERED)
        val eventFlow = events.receiveAsFlow()

        val uiState: StateFlow<TransactionDetailUiState> =
            combine(
                observeTransaction(transactionId),
                observeTransaction.events(transactionId),
            ) { transaction, timelineEvents ->
                if (transaction == null) {
                    TransactionDetailUiState(isLoading = true, transactionId = transactionId)
                } else {
                    TransactionDetailUiState(
                        isLoading = false,
                        transactionId = transaction.id,
                        rawStatus = transaction.status,
                        status = transaction.status.toStatusUi(),
                        amountCents = transaction.amountCents,
                        subtitle = transaction.status.operatorSubtitle(),
                        paymentTypeLabel = transaction.paymentType.label,
                        merchantId = transaction.merchantId,
                        terminalId = transaction.terminalId,
                        undoReasonLabel = transaction.undoReason?.receiptLabel,
                        timeline = timelineEvents.toTimelineUi().toImmutableList(),
                    )
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), TransactionDetailUiState())

        fun onReprintClick() {
            viewModelScope.launch { events.send(TransactionDetailEvent.ShowReceipt(transactionId)) }
        }

        fun onRetryUndoNowClick() {
            viewModelScope.launch {
                retryPendingUndo(transactionId)
                events.send(TransactionDetailEvent.ShowSnackbar("Tentando desfazer novamente…"))
            }
        }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
