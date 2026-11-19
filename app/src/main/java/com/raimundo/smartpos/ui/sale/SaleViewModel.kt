package com.raimundo.smartpos.ui.sale

import androidx.lifecycle.ViewModel
import com.raimundo.smartpos.domain.SaleLauncher
import com.raimundo.smartpos.domain.model.PaymentType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class SaleUiState(
    /** Apenas dígitos, em centavos: "1234" = R$ 12,34. */
    val amountDigits: String = "",
    val paymentType: PaymentType? = null,
    val submitting: Boolean = false,
) {
    val amountCents: Long get() = amountDigits.toLongOrNull() ?: 0L
    val canSubmit: Boolean get() = amountCents > 0 && paymentType != null && !submitting
}

sealed interface SaleEvent {
    data class SaleStarted(val transactionId: String) : SaleEvent
}

class SaleViewModel(
    private val launcher: SaleLauncher,
    private val newTransactionId: () -> String = { UUID.randomUUID().toString() },
) : ViewModel() {

    private val _state = MutableStateFlow(SaleUiState())
    val state: StateFlow<SaleUiState> = _state.asStateFlow()

    private val _events = Channel<SaleEvent>(Channel.BUFFERED)
    val events: Flow<SaleEvent> = _events.receiveAsFlow()

    /** Gerado uma vez por venda: toques repetidos em "Pagar" nunca criam uma segunda transação. */
    private var transactionId: String? = null

    fun onAmountChange(input: String) {
        if (_state.value.submitting) return
        val digits = input.filter(Char::isDigit).trimStart('0').take(MAX_AMOUNT_DIGITS)
        _state.update { it.copy(amountDigits = digits) }
    }

    fun onPaymentTypeSelected(type: PaymentType) {
        if (_state.value.submitting) return
        _state.update { it.copy(paymentType = type) }
    }

    fun onConfirm() {
        val current = _state.value
        val type = current.paymentType
        if (!current.canSubmit || type == null) return

        val id = transactionId ?: newTransactionId().also { transactionId = it }
        _state.update { it.copy(submitting = true) }
        launcher.startSale(id, current.amountCents, type) // não bloqueia: o processamento segue em background
        _events.trySend(SaleEvent.SaleStarted(id))
    }

    companion object {
        /** Até R$ 9.999.999,99. */
        const val MAX_AMOUNT_DIGITS = 9
    }
}
