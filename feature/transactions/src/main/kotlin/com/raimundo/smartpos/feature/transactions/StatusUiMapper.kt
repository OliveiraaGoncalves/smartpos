package com.raimundo.smartpos.feature.transactions

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
