package com.raimundo.smartpos.core.domain.usecase

import com.raimundo.smartpos.core.domain.repository.TransactionRepository
import javax.inject.Inject

class ReprintReceiptUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        suspend operator fun invoke(transactionId: String) = repository.reprintReceipt(transactionId)
    }
