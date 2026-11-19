package com.raimundo.smartpos.feature.transactiondetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.component.BannerTone
import com.raimundo.smartpos.core.ui.component.DataRow
import com.raimundo.smartpos.core.ui.component.PosBanner
import com.raimundo.smartpos.core.ui.component.PosButton
import com.raimundo.smartpos.core.ui.component.PosButtonVariant
import com.raimundo.smartpos.core.ui.component.PosSnackbarHost
import com.raimundo.smartpos.core.ui.component.PosTopBar
import com.raimundo.smartpos.core.ui.component.ResultHeader
import com.raimundo.smartpos.core.ui.component.StatusTone
import com.raimundo.smartpos.core.ui.component.StatusUi
import com.raimundo.smartpos.core.ui.component.TimelineStep
import com.raimundo.smartpos.core.ui.theme.SmartPosShapes
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun TransactionDetailScreen(
    state: TransactionDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onReprintClick: () -> Unit,
    onRetryUndoNowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { PosTopBar(title = "Detalhe da venda", onBack = onBack) },
        snackbarHost = { PosSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(SmartPosSpacing.lg),
        ) {
            if (state.status != null) {
                ResultHeader(status = state.status, amountCents = state.amountCents, subtitle = state.subtitle)
            }

            if (state.canRetryUndoNow) {
                PosBanner(
                    text = "Não entregue a mercadoria.",
                    tone = BannerTone.WARNING,
                    modifier = Modifier.padding(top = SmartPosSpacing.lg),
                )
            }

            Card(title = "Linha do tempo") {
                state.timeline.forEach { step ->
                    TimelineStep(step, modifier = Modifier.padding(vertical = SmartPosSpacing.xs))
                }
            }

            Card(title = "Dados da transação") {
                DataRow(label = "ID da transação", value = state.transactionId, expandable = true)
                DataRow(label = "Forma de pagamento", value = state.paymentTypeLabel)
                DataRow(label = "Terminal", value = state.terminalId)
                if (state.undoReasonLabel != null) {
                    DataRow(label = "Motivo", value = state.undoReasonLabel)
                }
            }

            if (state.canReprint) {
                PosButton(
                    text = "Ver comprovante",
                    onClick = onReprintClick,
                    variant = PosButtonVariant.TONAL,
                    modifier = Modifier.padding(top = SmartPosSpacing.lg),
                )
            }
            if (state.canRetryUndoNow) {
                PosButton(
                    text = "Tentar desfazer agora",
                    onClick = onRetryUndoNowClick,
                    modifier = Modifier.padding(top = SmartPosSpacing.lg),
                )
            }
        }
    }
}

@Composable
private fun Card(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = SmartPosSpacing.lg),
        shape = SmartPosShapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(modifier = Modifier.padding(SmartPosSpacing.lg)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.padding(top = SmartPosSpacing.md)) { content() }
        }
    }
}

private val NO_OP: () -> Unit = {}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionDetailScreenPreview() {
    SmartPosTheme {
        TransactionDetailScreen(
            state =
                TransactionDetailUiState(
                    isLoading = false,
                    transactionId = "tx-1",
                    status = StatusUi("Aprovada", StatusTone.SUCCESS),
                    amountCents = 10_000,
                    subtitle = "Venda concluída com sucesso.",
                    paymentTypeLabel = "Crédito",
                    terminalId = "12345678",
                    timeline = persistentListOf(),
                ),
            snackbarHostState = SnackbarHostState(),
            onBack = NO_OP,
            onReprintClick = NO_OP,
            onRetryUndoNowClick = NO_OP,
        )
    }
}
