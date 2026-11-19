package com.raimundo.smartpos.feature.transactiondetail

import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.ui.component.StatusTone
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusUiMapperTest {
    @Test
    fun `estados nao finais mostram Em andamento e orientam a nao entregar`() {
        for (
        status in
        listOf(TransactionStatus.CRIADA, TransactionStatus.AUTORIZANDO, TransactionStatus.AGUARDANDO_RESULTADO)
        ) {
            assertEquals("Em andamento", status.toStatusUi().label)
            assertEquals(StatusTone.PENDING, status.toStatusUi().tone)
            assertEquals("Aguarde a confirmação. Não entregue a mercadoria ainda.", status.operatorSubtitle())
        }
    }

    @Test
    fun `desfazendo mostra Desfazendo e orienta a nao entregar`() {
        assertEquals("Desfazendo", TransactionStatus.DESFAZENDO.toStatusUi().label)
        assertEquals(StatusTone.PENDING, TransactionStatus.DESFAZENDO.toStatusUi().tone)
        assertEquals("Não entregue a mercadoria.", TransactionStatus.DESFAZENDO.operatorSubtitle())
    }

    @Test
    fun `desfazimento pendente mostra Pendente e orienta a nao entregar`() {
        assertEquals("Pendente", TransactionStatus.DESFAZIMENTO_PENDENTE.toStatusUi().label)
        assertEquals(StatusTone.WARNING, TransactionStatus.DESFAZIMENTO_PENDENTE.toStatusUi().tone)
        assertEquals("Não entregue a mercadoria.", TransactionStatus.DESFAZIMENTO_PENDENTE.operatorSubtitle())
    }

    @Test
    fun `aprovada mostra Aprovada e confirma a conclusao`() {
        assertEquals("Aprovada", TransactionStatus.APROVADA.toStatusUi().label)
        assertEquals(StatusTone.SUCCESS, TransactionStatus.APROVADA.toStatusUi().tone)
        assertEquals("Venda concluída com sucesso.", TransactionStatus.APROVADA.operatorSubtitle())
    }

    @Test
    fun `rejeitada mostra Rejeitada e pede outra forma de pagamento`() {
        assertEquals("Rejeitada", TransactionStatus.REJEITADA.toStatusUi().label)
        assertEquals(StatusTone.ERROR, TransactionStatus.REJEITADA.toStatusUi().tone)
        assertEquals("Peça outra forma de pagamento. Nada foi cobrado.", TransactionStatus.REJEITADA.operatorSubtitle())
    }

    @Test
    fun `desfeita mostra Desfeita e orienta a nao entregar`() {
        assertEquals("Desfeita", TransactionStatus.DESFEITA.toStatusUi().label)
        assertEquals(StatusTone.WARNING, TransactionStatus.DESFEITA.toStatusUi().tone)
        assertEquals("Não entregue a mercadoria.", TransactionStatus.DESFEITA.operatorSubtitle())
    }
}
