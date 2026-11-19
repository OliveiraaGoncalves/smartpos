package com.raimundo.smartpos.core.domain.repository

import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionEvent
import kotlinx.coroutines.flow.Flow

/**
 * Fonte única de verdade das transações. Implementado por `core:data` (TransactionCoordinator
 * + Room + PrintQueue + WorkManager); `core:testing` tem a fake em memória.
 */
interface TransactionRepository {
    val transactions: Flow<List<Transaction>>

    fun observe(transactionId: String): Flow<Transaction?>

    fun observeEvents(transactionId: String): Flow<List<TransactionEvent>>

    /** @return `null` se o transactionId já foi usado (toque duplo é ignorado). */
    suspend fun startSale(
        transactionId: String,
        amountCents: Long,
        paymentType: PaymentType,
    ): Transaction?

    /** Dispara o /undo imediatamente. `transactionId == null` retenta todas as pendentes. */
    suspend fun retryPendingUndo(transactionId: String? = null)

    suspend fun reprintReceipt(transactionId: String)
}
