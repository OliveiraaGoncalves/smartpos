package com.raimundo.smartpos.core.localstorage.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Um degrau da linha do tempo da transação (tela de detalhe), gravado a cada transição de estado. */
@Entity(
    tableName = "transaction_events",
    indices = [Index("transactionId")],
)
data class TransactionEventEntity(
    @PrimaryKey(autoGenerate = true) val eventId: Long = 0,
    val transactionId: String,
    val status: String,
    val undoReason: String?,
    val timestampMillis: Long,
)
