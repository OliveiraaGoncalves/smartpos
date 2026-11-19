package com.raimundo.smartpos.core.domain.usecase

import com.raimundo.smartpos.core.domain.repository.TransactionRepository
import com.raimundo.smartpos.core.model.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTransactionsUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(): Flow<List<Transaction>> = repository.transactions
    }
