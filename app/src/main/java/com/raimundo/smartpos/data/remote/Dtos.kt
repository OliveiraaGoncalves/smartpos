package com.raimundo.smartpos.data.remote

import com.raimundo.smartpos.domain.model.PaymentType
import com.raimundo.smartpos.domain.model.Transaction
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Corpo de /authorization e /undo, exatamente como no contrato (antes da cifragem). */
@Serializable
data class TransactionPayload(
    val paymentType: PaymentType,
    val transactionId: String,
    val amount: Long,
    val merchantId: String,
    val terminalId: String,
)

@Serializable
enum class RemoteStatus { APROVADA, REJEITADA, DESFEITA }

/** Resposta de GET /polling quando já existe resultado final. */
@Serializable
data class PollingResponse(
    val paymentType: PaymentType,
    val transactionId: String,
    val amount: Long,
    val merchantId: String,
    val terminalId: String,
    val status: RemoteStatus,
)

fun Transaction.toPayload() = TransactionPayload(
    paymentType = paymentType,
    transactionId = id,
    amount = amountCents,
    merchantId = merchantId,
    terminalId = terminalId,
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

val SmartPosJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
