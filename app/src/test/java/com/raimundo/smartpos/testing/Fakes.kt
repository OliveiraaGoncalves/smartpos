package com.raimundo.smartpos.testing

import com.raimundo.smartpos.data.remote.PaymentGateway
import com.raimundo.smartpos.data.remote.PollingResponse
import com.raimundo.smartpos.data.remote.RemoteStatus
import com.raimundo.smartpos.data.remote.TransactionPayload
import com.raimundo.smartpos.domain.model.PaymentType
import com.raimundo.smartpos.domain.model.Transaction
import com.raimundo.smartpos.domain.model.TransactionStatus
import com.raimundo.smartpos.printer.ReceiptPrinter

const val MERCHANT = "123456789012345"
const val TERMINAL = "12345678"

fun transaction(
    id: String = "tx-1",
    amountCents: Long = 1_000,
    paymentType: PaymentType = PaymentType.CREDITO,
) = Transaction(
    id = id,
    amountCents = amountCents,
    paymentType = paymentType,
    merchantId = MERCHANT,
    terminalId = TERMINAL,
    status = TransactionStatus.CRIADA,
    createdAtMillis = 0,
    updatedAtMillis = 0,
)

fun TransactionPayload.result(status: RemoteStatus) =
    PollingResponse(paymentType, transactionId, amount, merchantId, terminalId, status)

/** Gateway programável; os comportamentos podem usar delay() (tempo virtual do runTest). */
class FakeGateway : PaymentGateway {
    val authorizeCalls = mutableListOf<TransactionPayload>()
    val undoCalls = mutableListOf<TransactionPayload>()
    val pollCalls = mutableMapOf<String, Int>()
    private val payloads = mutableMapOf<String, TransactionPayload>()

    var onAuthorize: suspend (TransactionPayload) -> Unit = {}
    var onPoll: suspend (payload: TransactionPayload, attempt: Int) -> PollingResponse? = { _, _ -> null }
    var onUndo: suspend (payload: TransactionPayload, attempt: Int) -> Unit = { _, _ -> }

    override suspend fun authorize(payload: TransactionPayload) {
        authorizeCalls += payload
        payloads[payload.transactionId] = payload
        onAuthorize(payload)
    }

    override suspend fun undo(payload: TransactionPayload) {
        undoCalls += payload
        onUndo(payload, undoCalls.count { it.transactionId == payload.transactionId })
    }

    override suspend fun poll(transactionId: String): PollingResponse? {
        val attempt = (pollCalls[transactionId] ?: 0) + 1
        pollCalls[transactionId] = attempt
        return onPoll(payloads.getValue(transactionId), attempt)
    }
}

class FakePrinter : ReceiptPrinter {
    val printed = mutableListOf<String>()
    var failuresBeforeSuccess = 0

    override suspend fun print(receipt: String) {
        if (failuresBeforeSuccess > 0) {
            failuresBeforeSuccess--
            throw IllegalStateException("Sem papel")
        }
        printed += receipt
    }
}
