package com.raimundo.smartpos.core.localstorage.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionEventDao {
    @Insert
    suspend fun insert(event: TransactionEventEntity)

    @Query("SELECT * FROM transaction_events WHERE transactionId = :transactionId ORDER BY timestampMillis ASC")
    fun observeByTransaction(transactionId: String): Flow<List<TransactionEventEntity>>

    @Query("SELECT * FROM transaction_events WHERE transactionId = :transactionId ORDER BY timestampMillis ASC")
    suspend fun getByTransaction(transactionId: String): List<TransactionEventEntity>
}
