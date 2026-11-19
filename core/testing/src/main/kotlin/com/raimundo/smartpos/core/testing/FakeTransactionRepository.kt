package com.raimundo.smartpos.core.testing

import com.raimundo.smartpos.core.domain.repository.TransactionRepository
import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeTransactionRepository : TransactionRepository {
    private val state = MutableStateFlow<Map<String, Transaction>>(emptyMap())
    private val events = MutableStateFlow<Map<String, List<TransactionEvent>>>(emptyMap())

    val startSaleCalls = mutableListOf<Triple<String, Long, PaymentType>>()
    val retryPendingUndoCalls = mutableListOf<String?>()
    val reprintReceiptCalls = mutableListOf<String>()
    var startSaleResult: ((String, Long, PaymentType) -> Transaction?)? = null

    override val transactions: Flow<List<Transaction>> = state.map { it.values.toList() }

    override fun observe(transactionId: String) = state.map { it[transactionId] }

    override fun observeEvents(transactionId: String) = events.map { it[transactionId].orEmpty() }

    override suspend fun startSale(
        transactionId: String,
        amountCents: Long,
        paymentType: PaymentType,
    ): Transaction? {
        startSaleCalls += Triple(transactionId, amountCents, paymentType)
        return startSaleResult?.invoke(transactionId, amountCents, paymentType)
    }

    override suspend fun retryPendingUndo(transactionId: String?) {
        retryPendingUndoCalls += transactionId
    }

    override suspend fun reprintReceipt(transactionId: String) {
        reprintReceiptCalls += transactionId
    }

    fun publish(transaction: Transaction) {
        state.value = state.value + (transaction.id to transaction)
    }

    fun publishEvents(
        transactionId: String,
        newEvents: List<TransactionEvent>,
    ) {
        events.value = events.value + (transactionId to newEvents)
    }
}
