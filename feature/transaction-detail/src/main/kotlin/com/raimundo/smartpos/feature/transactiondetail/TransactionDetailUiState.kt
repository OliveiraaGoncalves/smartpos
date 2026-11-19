package com.raimundo.smartpos.feature.transactiondetail

import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.ui.component.StatusUi
import com.raimundo.smartpos.core.ui.component.TimelineStepUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val transactionId: String = "",
    val rawStatus: TransactionStatus? = null,
    val status: StatusUi? = null,
    val amountCents: Long = 0,
    val subtitle: String = "",
    val paymentTypeLabel: String = "",
    val merchantId: String = "",
    val terminalId: String = "",
    val undoReasonLabel: String? = null,
    val timeline: ImmutableList<TimelineStepUi> = persistentListOf(),
) {
    val isFinal: Boolean get() = rawStatus?.isFinal == true
    val canReprint: Boolean get() = isFinal
    val canRetryUndoNow: Boolean get() = rawStatus == TransactionStatus.DESFAZIMENTO_PENDENTE
}

sealed interface TransactionDetailEvent {
    data class ShowReceipt(
        val transactionId: String,
    ) : TransactionDetailEvent

    data class ShowSnackbar(
        val message: String,
    ) : TransactionDetailEvent
}
