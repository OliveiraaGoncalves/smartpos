package com.raimundo.smartpos.core.data.journal

import com.raimundo.smartpos.core.domain.journal.TransactionJournal
import com.raimundo.smartpos.core.localstorage.db.TransactionDao
import com.raimundo.smartpos.core.localstorage.db.TransactionEventDao
import com.raimundo.smartpos.core.localstorage.db.toDomain
import com.raimundo.smartpos.core.localstorage.db.toEntity
import com.raimundo.smartpos.core.localstorage.db.toEventEntity
import com.raimundo.smartpos.core.model.Transaction
import javax.inject.Inject

/**
 * Write-ahead real: cada [save] grava a transação e um evento de linha do tempo no Room
 * ANTES do [com.raimundo.smartpos.core.domain.engine.TransactionEngine] seguir para a rede.
 */
class RoomTransactionJournal
    @Inject
    constructor(
        private val transactionDao: TransactionDao,
        private val eventDao: TransactionEventDao,
    ) : TransactionJournal {
        override suspend fun save(transaction: Transaction) {
            val previous = transactionDao.getById(transaction.id)
            transactionDao.upsert(transaction.toEntity())
            // Só gera um novo degrau na linha do tempo quando o status muda (não a cada save()).
            if (previous == null || previous.status != transaction.status.name) {
                eventDao.insert(transaction.toEventEntity())
            }
        }

        override suspend fun loadAll(): List<Transaction> = transactionDao.getAll().map { it.toDomain() }
    }
