package com.raimundo.smartpos.feature.sale

import com.raimundo.smartpos.core.model.PaymentType

data class SaleUiState(
    val amountCents: Long = 0,
    // Já vem com Crédito selecionado: o operador não deveria precisar escolher uma forma de
    // pagamento antes de poder digitar o valor na maioria das vendas.
    val paymentType: PaymentType? = PaymentType.CREDITO,
    val isSubmitting: Boolean = false,
) {
    val canPay: Boolean get() = amountCents > 0 && paymentType != null && !isSubmitting
    val isAtLimit: Boolean get() = amountCents >= MAX_AMOUNT_CENTS

    companion object {
        /** 9 dígitos: R$ 9.999.999,99. */
        const val MAX_AMOUNT_CENTS = 999_999_999L
    }
}

sealed interface SaleEvent {
    data class SaleSubmitted(
        val transactionId: String,
        val amountCents: Long,
    ) : SaleEvent

    data class ShowSnackbar(
        val message: String,
    ) : SaleEvent
}
