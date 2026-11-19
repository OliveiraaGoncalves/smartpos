package com.raimundo.smartpos.feature.transactiondetail

import com.raimundo.smartpos.core.model.TransactionEvent
import com.raimundo.smartpos.core.model.TransactionStatus
import com.raimundo.smartpos.core.ui.component.TimelineStepState
import com.raimundo.smartpos.core.ui.component.TimelineStepUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Termos técnicos (`/undo`, polling) só aparecem aqui, nunca nos títulos da tela. */
private fun TransactionStatus.timelineTitle(): String =
    when (this) {
        TransactionStatus.CRIADA -> "Transação criada"
        TransactionStatus.AUTORIZANDO -> "POST /authorization"
        TransactionStatus.AGUARDANDO_RESULTADO -> "Aguardando resultado (polling)"
        TransactionStatus.DESFAZENDO -> "POST /undo"
        TransactionStatus.DESFAZIMENTO_PENDENTE -> "Desfazimento pendente"
        TransactionStatus.APROVADA -> "Aprovada"
        TransactionStatus.REJEITADA -> "Rejeitada"
        TransactionStatus.DESFEITA -> "Desfeita"
    }

private val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.forLanguageTag("pt-BR"))

internal fun List<TransactionEvent>.toTimelineUi(): List<TimelineStepUi> =
    mapIndexed { index, event ->
        val isLast = index == lastIndex
        // Todo status final (inclusive REJEITADA/DESFEITA) marca o passo como concluído: o
        // "erro" já aparece no ResultHeader da tela, a linha do tempo só narra os passos.
        val state = if (!isLast || event.status.isFinal) TimelineStepState.DONE else TimelineStepState.CURRENT
        TimelineStepUi(
            title = event.status.timelineTitle(),
            timestamp = timeFormatter.format(Date(event.timestampMillis)),
            state = state,
        )
    }
