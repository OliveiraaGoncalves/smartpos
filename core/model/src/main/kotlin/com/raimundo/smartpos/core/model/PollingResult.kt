package com.raimundo.smartpos.core.model

/** Resultado final informado pelo backend (contrato HTTP), antes de virar [TransactionStatus] local. */
enum class RemoteStatus { APROVADA, REJEITADA, DESFEITA }

/** Resposta de GET /polling quando já existe resultado final, no formato de domínio (sem DTOs de rede). */
data class PollingResult(
    val transactionId: String,
    val amountCents: Long,
    val paymentType: PaymentType,
    val merchantId: String,
    val terminalId: String,
    val status: RemoteStatus,
)
