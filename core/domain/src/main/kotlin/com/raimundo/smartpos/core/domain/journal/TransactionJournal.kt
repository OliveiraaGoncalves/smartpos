package com.raimundo.smartpos.core.domain.journal

import com.raimundo.smartpos.core.model.Transaction

/**
 * Registro persistente (write-ahead) de cada mudança de estado. A transação é gravada
 * ANTES de ir para a rede: se o app morrer no meio do fluxo, na próxima abertura o
 * terminal sabe que existe uma transação sem resultado e a desfaz.
 *
 * Implementado por `core:local-storage` (Room) via `core:data`; `core:testing` tem a fake em memória.
 */
interface TransactionJournal {
    suspend fun save(transaction: Transaction)

    suspend fun loadAll(): List<Transaction>
}
