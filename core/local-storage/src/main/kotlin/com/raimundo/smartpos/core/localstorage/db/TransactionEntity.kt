package com.raimundo.smartpos.core.localstorage.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amountCents: Long,
    val paymentType: String,
    val merchantId: String,
    val terminalId: String,
    val status: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val undoReason: String?,
    val receiptPrinted: Boolean,
)
