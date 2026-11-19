package com.raimundo.smartpos.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PaymentType(val label: String, val receiptLabel: String) {
    @SerialName("Credito")
    CREDITO("Crédito", "CREDITO"),

    @SerialName("Debito")
    DEBITO("Débito", "DEBITO"),
}

/**
 * Ciclo de vida local da transação no terminal.
 * Somente APROVADA, REJEITADA e DESFEITA são finais (e geram comprovante).
 */
@Serializable
enum class TransactionStatus(val isFinal: Boolean) {
    CRIADA(false),
    AUTORIZANDO(false),
    AGUARDANDO_RESULTADO(false),
    DESFAZENDO(false),

    /** O /undo não pôde ser confirmado; o terminal continua tentando em background. */
    DESFAZIMENTO_PENDENTE(false),

    APROVADA(true),
    REJEITADA(true),
    DESFEITA(true),
}

@Serializable
enum class UndoReason(val receiptLabel: String) {
    TIMEOUT("TEMPO ESGOTADO"),
    FALHA_COMUNICACAO("FALHA DE COMUNICACAO"),

    /** Transação encontrada em estado intermediário ao reiniciar o app. */
    RECUPERACAO("RECUPERACAO"),
}

/**
 * Valores monetários são sempre em centavos (Long) para evitar erro de ponto flutuante.
 * O [id] é o transactionId: gerado uma única vez por venda e reutilizado em
 * /authorization, /polling e /undo (idempotência).
 */
@Serializable
data class Transaction(
    val id: String,
    val amountCents: Long,
    val paymentType: PaymentType,
    val merchantId: String,
    val terminalId: String,
    val status: TransactionStatus,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val undoReason: UndoReason? = null,
    val receiptPrinted: Boolean = false,
)
