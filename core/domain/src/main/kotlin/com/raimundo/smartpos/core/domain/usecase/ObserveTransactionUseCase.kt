package com.raimundo.smartpos.core.domain.usecase

import com.raimundo.smartpos.core.domain.repository.TransactionRepository
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTransactionUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(transactionId: String): Flow<Transaction?> = repository.observe(transactionId)

        fun events(transactionId: String): Flow<List<TransactionEvent>> = repository.observeEvents(transactionId)
    }
