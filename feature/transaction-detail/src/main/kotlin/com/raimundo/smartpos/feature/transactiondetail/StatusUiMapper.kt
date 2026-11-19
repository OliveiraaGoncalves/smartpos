package com.raimundo.smartpos.feature.transactiondetail

import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.ui.component.StatusTone
import com.raimundo.smartpos.core.ui.component.StatusUi

internal fun TransactionStatus.toStatusUi(): StatusUi =
    when (this) {
        TransactionStatus.CRIADA, TransactionStatus.AUTORIZANDO, TransactionStatus.AGUARDANDO_RESULTADO -> {
            StatusUi("Em andamento", StatusTone.PENDING)
        }

        TransactionStatus.DESFAZENDO -> {
            StatusUi("Desfazendo", StatusTone.PENDING)
        }

        TransactionStatus.DESFAZIMENTO_PENDENTE -> {
            StatusUi("Pendente", StatusTone.WARNING)
        }

        TransactionStatus.APROVADA -> {
            StatusUi("Aprovada", StatusTone.SUCCESS)
        }

        TransactionStatus.REJEITADA -> {
            StatusUi("Rejeitada", StatusTone.ERROR)
        }

        TransactionStatus.DESFEITA -> {
            StatusUi("Desfeita", StatusTone.WARNING)
        }
    }

/** Texto para o operador: nunca termos técnicos (isso fica só na linha do tempo). */
internal fun TransactionStatus.operatorSubtitle(): String =
    when (this) {
        TransactionStatus.DESFEITA, TransactionStatus.DESFAZIMENTO_PENDENTE, TransactionStatus.DESFAZENDO -> {
            "Não entregue a mercadoria."
        }

        TransactionStatus.REJEITADA -> {
            "Peça outra forma de pagamento. Nada foi cobrado."
        }

        TransactionStatus.APROVADA -> {
            "Venda concluída com sucesso."
        }

        TransactionStatus.CRIADA, TransactionStatus.AUTORIZANDO, TransactionStatus.AGUARDANDO_RESULTADO -> {
            "Aguarde a confirmação. Não entregue a mercadoria ainda."
        }
    }
