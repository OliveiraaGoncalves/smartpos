package com.raimundo.smartpos.core.model

/** Um degrau da linha do tempo da transação (tela de detalhe), gerado a cada transição de estado. */
data class TransactionEvent(
    val transactionId: String,
    val status: TransactionStatus,
    val undoReason: UndoReason?,
    val timestampMillis: Long,
)
