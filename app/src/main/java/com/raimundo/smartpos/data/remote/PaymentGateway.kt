package com.raimundo.smartpos.data.remote

/**
 * Qualquer falha na conversa com o backend: rede, HTTP != 2xx, payload que não decifra
 * ou resposta inconsistente. Para o fluxo de venda todas significam "desfazer".
 */
class CommunicationException(message: String, cause: Throwable? = null) : Exception(message, cause)

interface PaymentGateway {
    /** POST /authorization – retorna apenas quando o backend confirma o recebimento (200). */
    suspend fun authorize(payload: TransactionPayload)

    /** POST /undo – idempotente no backend. */
    suspend fun undo(payload: TransactionPayload)

    /** GET /polling – `null` enquanto o resultado ainda não está disponível. */
    suspend fun poll(transactionId: String): PollingResponse?
}
