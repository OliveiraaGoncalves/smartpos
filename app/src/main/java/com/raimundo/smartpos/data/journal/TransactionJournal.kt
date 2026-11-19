package com.raimundo.smartpos.data.journal

import android.content.Context
import com.raimundo.smartpos.data.remote.SmartPosJson
import com.raimundo.smartpos.domain.model.Transaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

/**
 * Registro persistente (write-ahead) de cada mudança de estado. A transação é gravada
 * ANTES de ir para a rede: se o app morrer no meio do fluxo, na próxima abertura o
 * terminal sabe que existe uma transação sem resultado e a desfaz.
 */
interface TransactionJournal {
    suspend fun save(transaction: Transaction)
    suspend fun loadAll(): List<Transaction>
}

class SharedPreferencesTransactionJournal(
    context: Context,
    private val json: Json = SmartPosJson,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : TransactionJournal {

    private val prefs = context.getSharedPreferences("transaction_journal", Context.MODE_PRIVATE)

    override suspend fun save(transaction: Transaction) = withContext(io) {
        // commit() síncrono de propósito: precisa estar em disco antes do envio.
        prefs.edit().putString(transaction.id, json.encodeToString(transaction)).commit()
        Unit
    }

    override suspend fun loadAll(): List<Transaction> = withContext(io) {
        prefs.all.values.mapNotNull { value ->
            (value as? String)?.let { runCatching { json.decodeFromString<Transaction>(it) }.getOrNull() }
        }
    }
}

class InMemoryTransactionJournal : TransactionJournal {
    private val store = ConcurrentHashMap<String, Transaction>()
    override suspend fun save(transaction: Transaction) {
        store[transaction.id] = transaction
    }

    override suspend fun loadAll(): List<Transaction> = store.values.toList()
    fun get(id: String): Transaction? = store[id]
}
