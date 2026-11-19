package com.raimundo.smartpos.feature.transactiondetail

import com.raimundo.smartpos.core.model.TransactionEvent
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.model.UndoReason
import com.raimundo.smartpos.core.ui.component.TimelineStepState
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineMapperTest {
    private fun event(
        status: TransactionStatus,
        timestampMillis: Long = 0,
    ) = TransactionEvent("tx-1", status, undoReason = null, timestampMillis = timestampMillis)

    @Test
    fun `passos anteriores ao ultimo sempre aparecem como concluidos`() {
        val timeline =
            listOf(
                event(TransactionStatus.CRIADA),
                event(TransactionStatus.AUTORIZANDO),
                event(TransactionStatus.AGUARDANDO_RESULTADO),
            ).toTimelineUi()

        assertEquals(TimelineStepState.DONE, timeline[0].state)
        assertEquals(TimelineStepState.DONE, timeline[1].state)
        assertEquals(TimelineStepState.CURRENT, timeline[2].state)
    }

    @Test
    fun `ultimo passo nao final aparece como atual`() {
        val timeline = listOf(event(TransactionStatus.AGUARDANDO_RESULTADO)).toTimelineUi()

        assertEquals(TimelineStepState.CURRENT, timeline.single().state)
    }

    @Test
    fun `ultimo passo aprovado aparece como concluido`() {
        val timeline = listOf(event(TransactionStatus.APROVADA)).toTimelineUi()

        assertEquals(TimelineStepState.DONE, timeline.single().state)
    }

    @Test
    fun `ultimo passo rejeitado ou desfeito tambem aparece como concluido`() {
        val rejeitada = listOf(event(TransactionStatus.REJEITADA)).toTimelineUi()
        val desfeita = listOf(event(TransactionStatus.DESFEITA)).toTimelineUi()

        assertEquals(TimelineStepState.DONE, rejeitada.single().state)
        assertEquals(TimelineStepState.DONE, desfeita.single().state)
    }

    @Test
    fun `titulo usa termos tecnicos so na linha do tempo`() {
        val titles =
            listOf(
                TransactionStatus.CRIADA to "Transação criada",
                TransactionStatus.AUTORIZANDO to "POST /authorization",
                TransactionStatus.AGUARDANDO_RESULTADO to "Aguardando resultado (polling)",
                TransactionStatus.DESFAZENDO to "POST /undo",
                TransactionStatus.DESFAZIMENTO_PENDENTE to "Desfazimento pendente",
                TransactionStatus.APROVADA to "Aprovada",
                TransactionStatus.REJEITADA to "Rejeitada",
                TransactionStatus.DESFEITA to "Desfeita",
            )

        for ((status, expectedTitle) in titles) {
            val step = listOf(event(status)).toTimelineUi().single()
            assertEquals(expectedTitle, step.title)
        }
    }

    @Test
    fun `timestamp eh formatado como hora-minuto-segundo`() {
        // 12:00:00 UTC de 1970-01-01 em millis.
        val step =
            listOf(
                event(TransactionStatus.APROVADA, timestampMillis = 12 * 60 * 60 * 1000L),
            ).toTimelineUi().single()

        assertEquals(8, step.timestamp?.length)
        assertEquals(2, step.timestamp?.count { it == ':' })
    }

    @Test
    fun `undoReason nao interfere no estado do passo`() {
        val event = TransactionEvent("tx-1", TransactionStatus.DESFEITA, UndoReason.TIMEOUT, timestampMillis = 0)

        assertEquals(TimelineStepState.DONE, listOf(event).toTimelineUi().single().state)
    }
}
