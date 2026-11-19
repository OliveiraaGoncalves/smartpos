package com.raimundo.smartpos.core.testing

import com.raimundo.smartpos.core.domain.gateway.PaymentGateway
import com.raimundo.smartpos.core.model.PollingResult
import com.raimundo.smartpos.core.model.Transaction

/** Gateway programável; os comportamentos podem usar delay() (tempo virtual do runTest). */
class FakeGateway : PaymentGateway {
    val authorizeCalls = mutableListOf<Transaction>()
    val undoCalls = mutableListOf<Transaction>()
    val pollCalls = mutableMapOf<String, Int>()
    private val sent = mutableMapOf<String, Transaction>()

    var onAuthorize: suspend (Transaction) -> Unit = {}
    var onPoll: suspend (transaction: Transaction, attempt: Int) -> PollingResult? = { _, _ -> null }
    var onUndo: suspend (transaction: Transaction, attempt: Int) -> Unit = { _, _ -> }

    override suspend fun authorize(transaction: Transaction) {
        authorizeCalls += transaction
        sent[transaction.id] = transaction
        onAuthorize(transaction)
    }

    override suspend fun undo(transaction: Transaction) {
        undoCalls += transaction
        onUndo(transaction, undoCalls.count { it.id == transaction.id })
    }

    override suspend fun poll(transactionId: String): PollingResult? {
        val attempt = (pollCalls[transactionId] ?: 0) + 1
        pollCalls[transactionId] = attempt
        return onPoll(sent.getValue(transactionId), attempt)
    }
}
