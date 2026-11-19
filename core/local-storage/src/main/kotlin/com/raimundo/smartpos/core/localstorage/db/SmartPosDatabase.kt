package com.raimundo.smartpos.core.localstorage.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TransactionEntity::class, TransactionEventEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SmartPosDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    abstract fun transactionEventDao(): TransactionEventDao

    companion object {
        const val NAME = "smartpos.db"
    }
}
