package com.raimundo.smartpos.core.domain.usecase

import com.raimundo.smartpos.core.common.IdGenerator
import com.raimundo.smartpos.core.domain.repository.TransactionRepository
import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.model.Transaction
import javax.inject.Inject

/**
 * O transactionId é gerado uma única vez por venda e guardado pela UI (ver `feature:sale`);
 * reenviar a MESMA chamada com o MESMO id (toque duplo) é ignorado pelo repositório.
 */
class StartSaleUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
        private val idGenerator: IdGenerator,
    ) {
        fun newTransactionId(): String = idGenerator.newId()

        suspend operator fun invoke(
            transactionId: String,
            amountCents: Long,
            paymentType: PaymentType,
        ): Transaction? {
            require(amountCents > 0) { "Valor deve ser maior que zero" }
            return repository.startSale(transactionId, amountCents, paymentType)
        }
    }
