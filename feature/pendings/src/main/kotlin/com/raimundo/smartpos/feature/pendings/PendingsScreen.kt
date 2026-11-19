package com.raimundo.smartpos.feature.pendings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.component.BannerTone
import com.raimundo.smartpos.core.ui.component.EmptyState
import com.raimundo.smartpos.core.ui.component.PosBanner
import com.raimundo.smartpos.core.ui.component.PosButton
import com.raimundo.smartpos.core.ui.component.PosSnackbarHost
import com.raimundo.smartpos.core.ui.component.PosTopBar
import com.raimundo.smartpos.core.ui.component.TransactionRow
import com.raimundo.smartpos.core.ui.component.TransactionRowUi
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PendingsScreen(
    state: PendingsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onRetryAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { PosTopBar(title = "Pendências", onBack = onBack) },
        snackbarHost = { PosSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            PosBanner(
                text = "Essas vendas ainda não foram confirmadas como desfeitas. Não entregue a mercadoria.",
                tone = BannerTone.WARNING,
                modifier = Modifier.padding(SmartPosSpacing.lg),
            )

            if (state.isEmpty) {
                EmptyState(title = "Nenhuma pendência", description = "Tudo certo por aqui.")
            } else {
                PosButton(
                    text = "Tentar desfazer todas agora",
                    onClick = onRetryAllClick,
                    modifier = Modifier.padding(horizontal = SmartPosSpacing.lg),
                )
                LazyColumn(modifier = Modifier.padding(top = SmartPosSpacing.md)) {
                    items(state.pendings, key = TransactionRowUi::id) { item ->
                        TransactionRow(item = item, onClick = { onTransactionClick(item.id) })
                    }
                }
            }
        }
    }
}

private val NO_OP: () -> Unit = {}
private val NO_OP_STRING: (String) -> Unit = {}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PendingsScreenPreview() {
    SmartPosTheme {
        PendingsScreen(
            state = PendingsUiState(isLoading = false, pendings = persistentListOf()),
            snackbarHostState = SnackbarHostState(),
            onBack = NO_OP,
            onTransactionClick = NO_OP_STRING,
            onRetryAllClick = NO_OP,
        )
    }
}
