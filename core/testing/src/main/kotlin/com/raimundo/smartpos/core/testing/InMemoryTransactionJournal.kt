package com.raimundo.smartpos.core.testing

import com.raimundo.smartpos.core.domain.journal.TransactionJournal
import com.raimundo.smartpos.core.model.Transaction
import java.util.concurrent.ConcurrentHashMap

class InMemoryTransactionJournal : TransactionJournal {
    private val store = ConcurrentHashMap<String, Transaction>()

    override suspend fun save(transaction: Transaction) {
        store[transaction.id] = transaction
    }

    override suspend fun loadAll(): List<Transaction> = store.values.toList()

    fun get(id: String): Transaction? = store[id]
}
