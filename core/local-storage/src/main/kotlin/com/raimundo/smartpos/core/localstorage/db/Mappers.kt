package com.raimundo.smartpos.core.localstorage.db

import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionEvent
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.model.UndoReason

fun Transaction.toEntity() =
    TransactionEntity(
        id = id,
        amountCents = amountCents,
        paymentType = paymentType.name,
        merchantId = merchantId,
        terminalId = terminalId,
        status = status.name,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        undoReason = undoReason?.name,
        receiptPrinted = receiptPrinted,
    )

fun TransactionEntity.toDomain() =
    Transaction(
        id = id,
        amountCents = amountCents,
        paymentType = PaymentType.valueOf(paymentType),
        merchantId = merchantId,
        terminalId = terminalId,
        status = TransactionStatus.valueOf(status),
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        undoReason = undoReason?.let(UndoReason::valueOf),
        receiptPrinted = receiptPrinted,
    )

fun Transaction.toEventEntity() =
    TransactionEventEntity(
        transactionId = id,
        status = status.name,
        undoReason = undoReason?.name,
        timestampMillis = updatedAtMillis,
    )

fun TransactionEventEntity.toDomain() =
    TransactionEvent(
        transactionId = transactionId,
        status = TransactionStatus.valueOf(status),
        undoReason = undoReason?.let(UndoReason::valueOf),
        timestampMillis = timestampMillis,
    )
