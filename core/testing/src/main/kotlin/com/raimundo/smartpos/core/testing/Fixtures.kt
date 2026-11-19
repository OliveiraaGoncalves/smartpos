package com.raimundo.smartpos.core.testing

import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.model.PollingResult
import com.raimundo.smartpos.core.model.RemoteStatus
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionStatus

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

fun Transaction.result(status: RemoteStatus) =
    PollingResult(
        transactionId = id,
        amountCents = amountCents,
        paymentType = paymentType,
        merchantId = merchantId,
        terminalId = terminalId,
        status = status,
    )
