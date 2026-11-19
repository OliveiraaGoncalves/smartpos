package com.raimundo.smartpos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.domain.model.Money
import com.raimundo.smartpos.domain.model.Transaction
import com.raimundo.smartpos.domain.model.TransactionStatus
import com.raimundo.smartpos.printer.PrintedReceipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class TransactionItemUi(
    val id: String,
    val shortId: String,
    val amount: String,
    val paymentType: String,
    val status: TransactionStatus,
    val statusLabel: String,
    val inProgress: Boolean,
    val undoReason: String?,
    val receipt: String?,
)

data class HomeUiState(
    val transactions: List<TransactionItemUi> = emptyList(),
) {
    val inProgressCount: Int get() = transactions.count { it.inProgress }
}

class HomeViewModel(
    transactions: Flow<List<Transaction>>,
    receipts: Flow<List<PrintedReceipt>>,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = combine(transactions, receipts) { txs, printed ->
        val receiptById = printed.associateBy(PrintedReceipt::transactionId)
        HomeUiState(txs.map { it.toUi(receiptById[it.id]?.text) })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}

private fun Transaction.toUi(receipt: String?) = TransactionItemUi(
    id = id,
    shortId = id.take(8),
    amount = Money.format(amountCents),
    paymentType = paymentType.label,
    status = status,
    statusLabel = status.label(),
    inProgress = !status.isFinal,
    undoReason = undoReason?.receiptLabel,
    receipt = receipt,
)

private fun TransactionStatus.label(): String = when (this) {
    TransactionStatus.CRIADA -> "Iniciando"
    TransactionStatus.AUTORIZANDO -> "Enviando"
    TransactionStatus.AGUARDANDO_RESULTADO -> "Aguardando resultado"
    TransactionStatus.DESFAZENDO -> "Desfazendo"
    TransactionStatus.DESFAZIMENTO_PENDENTE -> "Desfazimento pendente"
    TransactionStatus.APROVADA -> "Aprovada"
    TransactionStatus.REJEITADA -> "Rejeitada"
    TransactionStatus.DESFEITA -> "Desfeita"
}
