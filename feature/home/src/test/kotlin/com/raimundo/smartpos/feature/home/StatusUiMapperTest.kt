package com.raimundo.smartpos.feature.home

import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.ui.component.StatusTone
import org.junit.Assert.assertEquals
import org.junit.Test

/** A Home nunca mostra termos técnicos (`/undo`, polling): só a linha do tempo do Detalhe mostra. */
class StatusUiMapperTest {
    @Test
    fun `estados nao finais mostram Em andamento`() {
        for (status in listOf(
            TransactionStatus.CRIADA,
            TransactionStatus.AUTORIZANDO,
            TransactionStatus.AGUARDANDO_RESULTADO,
        )) {
            val ui = status.toStatusUi()
            assertEquals("Em andamento", ui.label)
            assertEquals(StatusTone.PENDING, ui.tone)
        }
    }

    @Test
    fun `desfazendo mostra Desfazendo`() {
        val ui = TransactionStatus.DESFAZENDO.toStatusUi()
        assertEquals("Desfazendo", ui.label)
        assertEquals(StatusTone.PENDING, ui.tone)
    }

    @Test
    fun `desfazimento pendente mostra Pendente`() {
        val ui = TransactionStatus.DESFAZIMENTO_PENDENTE.toStatusUi()
        assertEquals("Pendente", ui.label)
        assertEquals(StatusTone.WARNING, ui.tone)
    }

    @Test
    fun `aprovada mostra Aprovada`() {
        val ui = TransactionStatus.APROVADA.toStatusUi()
        assertEquals("Aprovada", ui.label)
        assertEquals(StatusTone.SUCCESS, ui.tone)
    }

    @Test
    fun `rejeitada mostra Rejeitada`() {
        val ui = TransactionStatus.REJEITADA.toStatusUi()
        assertEquals("Rejeitada", ui.label)
        assertEquals(StatusTone.ERROR, ui.tone)
    }

    @Test
    fun `desfeita mostra Desfeita`() {
        val ui = TransactionStatus.DESFEITA.toStatusUi()
        assertEquals("Desfeita", ui.label)
        assertEquals(StatusTone.WARNING, ui.tone)
    }
}
