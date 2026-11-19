package com.raimundo.smartpos.core.network

import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.model.PollingResult
import com.raimundo.smartpos.core.model.RemoteStatus
import com.raimundo.smartpos.core.model.Transaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Corpo de /authorization e /undo, exatamente como no contrato (antes da cifragem). */
@Serializable
data class TransactionPayload(
    val paymentType: String,
    val transactionId: String,
    val amount: Long,
    val merchantId: String,
    val terminalId: String,
)

@Serializable
enum class RemoteStatusDto { APROVADA, REJEITADA, DESFEITA }

/** Resposta de GET /polling quando já existe resultado final. */
@Serializable
data class PollingResponse(
    val paymentType: String,
    val transactionId: String,
    val amount: Long,
    val merchantId: String,
    val terminalId: String,
    val status: RemoteStatusDto,
)

fun PaymentType.toWire(): String =
    when (this) {
        PaymentType.CREDITO -> "Credito"
        PaymentType.DEBITO -> "Debito"
    }

fun String.toPaymentType(): PaymentType =
    when (this) {
        "Credito" -> PaymentType.CREDITO
        "Debito" -> PaymentType.DEBITO
        else -> throw IllegalArgumentException("paymentType desconhecido: $this")
    }

fun Transaction.toPayload() =
    TransactionPayload(
        paymentType = paymentType.toWire(),
        transactionId = id,
        amount = amountCents,
        merchantId = merchantId,
        terminalId = terminalId,
    )

fun PollingResponse.toDomain() =
    PollingResult(
        transactionId = transactionId,
        amountCents = amount,
        paymentType = paymentType.toPaymentType(),
        merchantId = merchantId,
        terminalId = terminalId,
        status =
            when (status) {
                RemoteStatusDto.APROVADA -> RemoteStatus.APROVADA
                RemoteStatusDto.REJEITADA -> RemoteStatus.REJEITADA
                RemoteStatusDto.DESFEITA -> RemoteStatus.DESFEITA
            },
    )

object Endpoints {
    const val AUTHORIZATION = "/authorization"
    const val UNDO = "/undo"
    const val POLLING = "/polling"
}

object SmartPosHeaders {
    const val TRANSACTION_ID = "X-Transaction-Id"
    const val IDEMPOTENCY_KEY = "Idempotency-Key"
    const val PAYLOAD_ENCRYPTION = "X-Payload-Encryption"
}

val SmartPosJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
