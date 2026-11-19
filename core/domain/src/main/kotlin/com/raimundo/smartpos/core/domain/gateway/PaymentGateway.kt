package com.raimundo.smartpos.core.domain.gateway

import com.raimundo.smartpos.core.model.PollingResult
import com.raimundo.smartpos.core.model.Transaction

/**
 * Interface declarada pelo domínio (regra D do SOLID); `core:network` fornece a
 * implementação real (HTTPS + AES-256-GCM) e `core:testing` as fakes de teste.
 */
interface PaymentGateway {
    /** POST /authorization – retorna apenas quando o backend confirma o recebimento (200). */
    suspend fun authorize(transaction: Transaction)

    /** POST /undo – idempotente no backend. */
    suspend fun undo(transaction: Transaction)

    /** GET /polling – `null` enquanto o resultado ainda não está disponível. */
    suspend fun poll(transactionId: String): PollingResult?
}
