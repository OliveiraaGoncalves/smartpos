package com.raimundo.smartpos.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raimundo.smartpos.core.ui.component.BannerTone
import com.raimundo.smartpos.core.ui.component.BottomDestination
import com.raimundo.smartpos.core.ui.component.EmptyState
import com.raimundo.smartpos.core.ui.component.MoneyText
import com.raimundo.smartpos.core.ui.component.PosBanner
import com.raimundo.smartpos.core.ui.component.PosBottomBar
import com.raimundo.smartpos.core.ui.component.PosButton
import com.raimundo.smartpos.core.ui.component.PosSnackbarHost
import com.raimundo.smartpos.core.ui.component.StatusTone
import com.raimundo.smartpos.core.ui.component.StatusUi
import com.raimundo.smartpos.core.ui.component.TransactionRow
import com.raimundo.smartpos.core.ui.component.TransactionRowUi
import com.raimundo.smartpos.core.ui.theme.SmartPosShapes
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun HomeScreen(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onSellClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onPendingBannerClick: () -> Unit,
    onToggleHideValues: () -> Unit,
    onBottomNavigate: (BottomDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { PosSnackbarHost(snackbarHostState) },
        bottomBar = { PosBottomBar(selected = BottomDestination.HOME, onSelect = onBottomNavigate) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!state.isOnline) {
                PosBanner(
                    text = "Sem conexão. Aguarde a rede voltar.",
                    tone = BannerTone.WARNING,
                    modifier = Modifier.padding(SmartPosSpacing.lg),
                )
            } else if (state.hasPendingUndo) {
                PosBanner(
                    text = "Não entregue a mercadoria de uma venda pendente. Toque para ver.",
                    tone = BannerTone.WARNING,
                    modifier =
                        Modifier
                            .padding(SmartPosSpacing.lg)
                            .clickable(onClick = onPendingBannerClick),
                )
            }

            ApprovedTodayCard(
                amountCents = state.approvedTodayCents,
                hidden = state.hideValues,
                onToggleHideValues = onToggleHideValues,
                modifier = Modifier.padding(SmartPosSpacing.lg),
            )

            PosButton(
                text = "Vender",
                onClick = onSellClick,
                modifier = Modifier.padding(horizontal = SmartPosSpacing.lg),
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(SmartPosSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "Últimas transações", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Ver todas",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onSeeAllClick),
                )
            }

            if (state.isEmpty) {
                EmptyState(
                    title = "Nenhuma venda ainda",
                    description = "Toque em Vender para começar.",
                )
            } else {
                LazyColumn {
                    items(state.transactions, key = TransactionRowUi::id) { item ->
                        TransactionRow(
                            item = item,
                            onClick = { onTransactionClick(item.id) },
                            hideAmount = state.hideValues,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ApprovedTodayCard(
    amountCents: Long,
    hidden: Boolean,
    onToggleHideValues: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SmartPosShapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(SmartPosSpacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = "Vendas aprovadas hoje", style = MaterialTheme.typography.bodyMedium)
                MoneyText(
                    amountCents = amountCents,
                    hidden = hidden,
                    modifier = Modifier.padding(top = SmartPosSpacing.xs),
                )
            }
            Icon(
                imageVector = if (hidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = if (hidden) "Mostrar valores" else "Ocultar valores",
                modifier =
                    Modifier
                        .size(32.dp)
                        .clickable(onClick = onToggleHideValues),
            )
        }
    }
}

private val NO_OP: () -> Unit = {}
private val NO_OP_STRING: (String) -> Unit = {}
private val NO_OP_DESTINATION: (BottomDestination) -> Unit = {}

@Suppress("ktlint:standard:max-line-length")
@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    SmartPosTheme {
        HomeScreen(
            state =
                HomeUiState(
                    isLoading = false,
                    transactions =
                        persistentListOf(
                            TransactionRowUi(
                                "tx-1",
                                "Crédito · 14:32",
                                5_000,
                                StatusUi("Aprovada", StatusTone.SUCCESS),
                            ),
                        ),
                    approvedTodayCents = 12_000,
                ),
            snackbarHostState = SnackbarHostState(),
            onSellClick = NO_OP,
            onTransactionClick = NO_OP_STRING,
            onSeeAllClick = NO_OP,
            onPendingBannerClick = NO_OP,
            onToggleHideValues = NO_OP,
            onBottomNavigate = NO_OP_DESTINATION,
        )
    }
}
