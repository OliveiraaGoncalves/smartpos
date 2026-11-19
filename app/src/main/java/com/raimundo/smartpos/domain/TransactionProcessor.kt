package com.raimundo.smartpos.domain

import com.raimundo.smartpos.data.journal.TransactionJournal
import com.raimundo.smartpos.data.remote.CommunicationException
import com.raimundo.smartpos.data.remote.PaymentGateway
import com.raimundo.smartpos.data.remote.PollingResponse
import com.raimundo.smartpos.data.remote.RemoteStatus
import com.raimundo.smartpos.data.remote.TransactionPayload
import com.raimundo.smartpos.data.remote.toPayload
import com.raimundo.smartpos.domain.model.Transaction
import com.raimundo.smartpos.domain.model.TransactionStatus
import com.raimundo.smartpos.domain.model.UndoReason
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

data class ProcessorConfig(
    /** Prazo total para obter resultado final, contado a partir do envio. */
    val resultTimeoutMs: Long = 15_000,
    val initialPollDelayMs: Long = 500,
    val pollIntervalMs: Long = 1_000,
    val undoMaxAttempts: Int = 3,
    val undoRetryBaseDelayMs: Long = 1_000,
)

/**
 * Executa o fluxo de UMA transação:
 *
 *   AUTORIZANDO -> POST /authorization (200 = recebido)
 *   AGUARDANDO_RESULTADO -> GET /polling até APROVADA | REJEITADA | DESFEITA
 *
 * Regras:
 *  - todo o fluxo tem no máximo [ProcessorConfig.resultTimeoutMs]; estourou -> /undo;
 *  - qualquer falha de comunicação -> /undo (inclusive quando o /authorization falha:
 *    o backend pode ter recebido e a resposta ter se perdido);
 *  - o /undo roda fora do prazo e não é cancelável; se não for confirmado fica
 *    DESFAZIMENTO_PENDENTE e o [TransactionManager] tenta de novo depois.
 *
 * Toda transição é gravada no journal antes de seguir (write-ahead).
 */
class TransactionProcessor(
    private val gateway: PaymentGateway,
    private val journal: TransactionJournal,
    private val config: ProcessorConfig = ProcessorConfig(),
    private val clock: () -> Long = System::currentTimeMillis,
) {

    suspend fun process(transaction: Transaction, onUpdate: (Transaction) -> Unit = {}): Transaction {
        val tracker = Tracker(transaction, onUpdate)
        val payload = transaction.toPayload()
        tracker.move(TransactionStatus.AUTORIZANDO)

        val remoteStatus = try {
            withTimeoutOrNull(config.resultTimeoutMs) {
                gateway.authorize(payload)
                tracker.move(TransactionStatus.AGUARDANDO_RESULTADO)
                awaitFinalStatus(payload)
            }
        } catch (e: CommunicationException) {
            return undo(tracker.current, UndoReason.FALHA_COMUNICACAO, onUpdate)
        }

        if (remoteStatus == null) return undo(tracker.current, UndoReason.TIMEOUT, onUpdate)

        return tracker.move(
            when (remoteStatus) {
                RemoteStatus.APROVADA -> TransactionStatus.APROVADA
                RemoteStatus.REJEITADA -> TransactionStatus.REJEITADA
                RemoteStatus.DESFEITA -> TransactionStatus.DESFEITA
            },
        )
    }

    /**
     * Solicita o desfazimento com o MESMO transactionId/payload da autorização.
     * Público para ser usado na recuperação após reinício do app.
     */
    suspend fun undo(
        transaction: Transaction,
        reason: UndoReason,
        onUpdate: (Transaction) -> Unit = {},
    ): Transaction = withContext(NonCancellable) {
        val tracker = Tracker(transaction, onUpdate)
        tracker.move(TransactionStatus.DESFAZENDO, reason)
        val payload = transaction.toPayload()

        repeat(config.undoMaxAttempts) { attempt ->
            try {
                gateway.undo(payload)
                return@withContext tracker.move(TransactionStatus.DESFEITA)
            } catch (e: CommunicationException) {
                if (attempt < config.undoMaxAttempts - 1) {
                    delay(config.undoRetryBaseDelayMs * (attempt + 1))
                }
            }
        }
        tracker.move(TransactionStatus.DESFAZIMENTO_PENDENTE)
    }

    private suspend fun awaitFinalStatus(payload: TransactionPayload): RemoteStatus {
        delay(config.initialPollDelayMs)
        while (true) {
            val response = gateway.poll(payload.transactionId)
            if (response != null) {
                ensureMatches(payload, response)
                return response.status
            }
            delay(config.pollIntervalMs)
        }
    }

    /** O resultado precisa ser da MESMA transação; qualquer divergência é tratada como falha. */
    private fun ensureMatches(sent: TransactionPayload, received: PollingResponse) {
        val consistent = received.transactionId == sent.transactionId &&
            received.amount == sent.amount &&
            received.paymentType == sent.paymentType &&
            received.merchantId == sent.merchantId &&
            received.terminalId == sent.terminalId
        if (!consistent) throw CommunicationException("Resposta de polling não corresponde à transação ${sent.transactionId}")
    }

    private inner class Tracker(var current: Transaction, private val onUpdate: (Transaction) -> Unit) {
        suspend fun move(status: TransactionStatus, reason: UndoReason? = null): Transaction {
            current = current.copy(
                status = status,
                undoReason = reason ?: current.undoReason,
                updatedAtMillis = clock(),
            )
            journal.save(current)
            onUpdate(current)
            return current
        }
    }
}
