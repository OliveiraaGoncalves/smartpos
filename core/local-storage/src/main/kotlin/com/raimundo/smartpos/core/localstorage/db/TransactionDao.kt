package com.raimundo.smartpos.core.localstorage.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    /** Write-ahead: grava o novo estado antes de qualquer chamada de rede. */
    @Upsert
    suspend fun upsert(entity: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY createdAtMillis DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: String): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE status IN (:statuses)")
    suspend fun getByStatuses(statuses: List<String>): List<TransactionEntity>

    /** Transações fora de estado final: usadas na recuperação ao abrir o app e no retry de pendências. */
    suspend fun getNonFinal(): List<TransactionEntity> = getByStatuses(NON_FINAL_STATUSES)

    suspend fun getPendingUndo(): List<TransactionEntity> = getByStatuses(listOf("DESFAZIMENTO_PENDENTE"))

    companion object {
        val NON_FINAL_STATUSES =
            listOf(
                "CRIADA",
                "AUTORIZANDO",
                "AGUARDANDO_RESULTADO",
                "DESFAZENDO",
                "DESFAZIMENTO_PENDENTE",
            )
    }
}
