package com.raimundo.smartpos.feature.sale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.core.domain.usecase.StartSaleUseCase
import com.raimundo.smartpos.core.model.PaymentType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaleViewModel
    @Inject
    constructor(
        private val startSale: StartSaleUseCase,
    ) : ViewModel() {
        /** Gerado uma única vez por venda: toque duplo em "Pagar" reenvia o MESMO id (idempotência). */
        private val transactionId = startSale.newTransactionId()

        private val _uiState = MutableStateFlow(SaleUiState())
        val uiState: StateFlow<SaleUiState> = _uiState.asStateFlow()

        private val events = Channel<SaleEvent>(Channel.BUFFERED)
        val eventFlow = events.receiveAsFlow()

        fun onDigit(digit: String) {
            val state = _uiState.value
            if (state.isAtLimit) {
                emitSnackbar("Valor no limite de R$ 9.999.999,99.")
                return
            }
            val next = (state.amountCents * 10 + digit.toLong()).coerceAtMost(SaleUiState.MAX_AMOUNT_CENTS)
            _uiState.value = state.copy(amountCents = next)
        }

        fun onBackspace() {
            _uiState.value = _uiState.value.let { it.copy(amountCents = it.amountCents / 10) }
        }

        fun onPaymentTypeSelect(paymentType: PaymentType) {
            _uiState.value = _uiState.value.copy(paymentType = paymentType)
        }

        fun onPayClick() {
            val state = _uiState.value
            if (state.isSubmitting) return // toque duplo: a primeira chamada já está em voo.
            if (!state.canPay) {
                val reason =
                    when {
                        state.amountCents <= 0 && state.paymentType == null -> "Informe o valor e a forma de pagamento."
                        state.amountCents <= 0 -> "Informe o valor."
                        else -> "Informe a forma de pagamento."
                    }
                emitSnackbar(reason)
                return
            }

            _uiState.value = state.copy(isSubmitting = true)
            viewModelScope.launch {
                startSale(transactionId, state.amountCents, requireNotNull(state.paymentType))
                events.send(SaleEvent.SaleSubmitted(transactionId, state.amountCents))
            }
        }

        private fun emitSnackbar(message: String) {
            viewModelScope.launch { events.send(SaleEvent.ShowSnackbar(message)) }
        }
    }
