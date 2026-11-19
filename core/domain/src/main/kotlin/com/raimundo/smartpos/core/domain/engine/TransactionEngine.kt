package com.raimundo.smartpos.core.domain.engine

import com.raimundo.smartpos.core.common.Clock
import com.raimundo.smartpos.core.common.CommunicationException
import com.raimundo.smartpos.core.domain.gateway.PaymentGateway
import com.raimundo.smartpos.core.domain.journal.TransactionJournal
import com.raimundo.smartpos.core.model.PollingResult
import com.raimundo.smartpos.core.model.RemoteStatus
import com.raimundo.smartpos.core.model.Transaction
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.model.UndoReason
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Executa o fluxo de UMA transação:
 *
 *   AUTORIZANDO -> POST /authorization (200 = recebido)
 *   AGUARDANDO_RESULTADO -> GET /polling até APROVADA | REJEITADA | DESFEITA
 *
 * Regras:
 *  - todo o fluxo tem no máximo [EngineConfig.resultTimeoutMs]; estourou -> /undo;
 *  - qualquer falha de comunicação -> /undo (inclusive quando o /authorization falha:
 *    o backend pode ter recebido e a resposta ter se perdido);
 *  - o /undo roda fora do prazo e não é cancelável; se não for confirmado fica
 *    DESFAZIMENTO_PENDENTE e quem orquestra múltiplas transações (`core:data`) tenta de novo depois.
 *
 * Toda transição é gravada no journal antes de seguir (write-ahead).
 */
class TransactionEngine
    @Inject
    constructor(
        private val gateway: PaymentGateway,
        private val journal: TransactionJournal,
        private val clock: Clock,
        private val config: EngineConfig,
    ) {
        suspend fun process(
            transaction: Transaction,
            onUpdate: (Transaction) -> Unit = {},
        ): Transaction {
            val tracker = Tracker(transaction, onUpdate)
            tracker.move(TransactionStatus.AUTORIZANDO)

            val remoteStatus =
                try {
                    withTimeoutOrNull(config.resultTimeoutMs) {
                        gateway.authorize(tracker.current)
                        tracker.move(TransactionStatus.AGUARDANDO_RESULTADO)
                        awaitFinalStatus(tracker.current)
                    }
                } catch (
                    @Suppress("SwallowedException") e: CommunicationException,
                ) {
                    // O motivo já é FALHA_COMUNICACAO; não há informação adicional a propagar.
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
        ): Transaction =
            withContext(NonCancellable) {
                val tracker = Tracker(transaction, onUpdate)
                tracker.move(TransactionStatus.DESFAZENDO, reason)

                repeat(config.undoMaxAttempts) { attempt ->
                    try {
                        gateway.undo(tracker.current)
                        return@withContext tracker.move(TransactionStatus.DESFEITA)
                    } catch (
                        @Suppress("SwallowedException") e: CommunicationException,
                    ) {
                        // Falha transitória esperada: a próxima tentativa (ou DESFAZIMENTO_PENDENTE) assume.
                        if (attempt < config.undoMaxAttempts - 1) {
                            delay(config.undoRetryBaseDelayMs * (attempt + 1))
                        }
                    }
                }
                tracker.move(TransactionStatus.DESFAZIMENTO_PENDENTE)
            }

        private suspend fun awaitFinalStatus(transaction: Transaction): RemoteStatus {
            delay(config.initialPollDelayMs)
            while (true) {
                val response = gateway.poll(transaction.id)
                if (response != null) {
                    ensureMatches(transaction, response)
                    return response.status
                }
                delay(config.pollIntervalMs)
            }
        }

        /** O resultado precisa ser da MESMA transação; qualquer divergência é tratada como falha. */
        private fun ensureMatches(
            sent: Transaction,
            received: PollingResult,
        ) {
            val consistent =
                received.transactionId == sent.id &&
                    received.amountCents == sent.amountCents &&
                    received.paymentType == sent.paymentType &&
                    received.merchantId == sent.merchantId &&
                    received.terminalId == sent.terminalId
            if (!consistent) {
                throw CommunicationException("Resposta de polling não corresponde à transação ${sent.id}")
            }
        }

        private inner class Tracker(
            var current: Transaction,
            private val onUpdate: (Transaction) -> Unit,
        ) {
            suspend fun move(
                status: TransactionStatus,
                reason: UndoReason? = null,
            ): Transaction {
                current =
                    current.copy(
                        status = status,
                        undoReason = reason ?: current.undoReason,
                        updatedAtMillis = clock.nowMillis(),
                    )
                journal.save(current)
                onUpdate(current)
                return current
            }
        }
    }
