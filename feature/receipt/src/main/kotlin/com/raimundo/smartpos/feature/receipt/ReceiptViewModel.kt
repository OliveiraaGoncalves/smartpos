package com.raimundo.smartpos.feature.receipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.core.domain.usecase.ObserveTransactionUseCase
import com.raimundo.smartpos.core.domain.usecase.ReprintReceiptUseCase
import com.raimundo.smartpos.core.model.ReceiptFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel
    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        observeTransaction: ObserveTransactionUseCase,
        private val reprintReceipt: ReprintReceiptUseCase,
    ) : ViewModel() {
        private val transactionId: String = checkNotNull(savedStateHandle["transactionId"])
        private val formatter = ReceiptFormatter()

        private val events = Channel<ReceiptEvent>(Channel.BUFFERED)
        val eventFlow = events.receiveAsFlow()

        val uiState: StateFlow<ReceiptUiState> =
            observeTransaction(transactionId)
                .map { transaction ->
                    if (transaction == null) {
                        ReceiptUiState(isLoading = true)
                    } else {
                        ReceiptUiState(isLoading = false, receiptText = formatter.format(transaction))
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ReceiptUiState())

        fun onReprintClick() {
            viewModelScope.launch {
                reprintReceipt(transactionId)
                events.send(ReceiptEvent.ShowSnackbar("Nova via impressa"))
            }
        }

        fun onCloseClick() {
            viewModelScope.launch { events.send(ReceiptEvent.Dismiss) }
        }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
