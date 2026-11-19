package com.raimundo.smartpos.feature.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.component.BottomDestination
import com.raimundo.smartpos.core.ui.component.EmptyState
import com.raimundo.smartpos.core.ui.component.MoneyText
import com.raimundo.smartpos.core.ui.component.PosBottomBar
import com.raimundo.smartpos.core.ui.component.PosFilterChip
import com.raimundo.smartpos.core.ui.component.PosTopBar
import com.raimundo.smartpos.core.ui.component.TransactionRow
import com.raimundo.smartpos.core.ui.component.TransactionRowUi
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun TransactionsScreen(
    state: TransactionsUiState,
    onFilterSelect: (TransactionFilter) -> Unit,
    onTransactionClick: (String) -> Unit,
    onBottomNavigate: (BottomDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { PosTopBar(title = "Transações") },
        bottomBar = { PosBottomBar(selected = BottomDestination.TRANSACTIONS, onSelect = onBottomNavigate) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(SmartPosSpacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "Total aprovado", style = MaterialTheme.typography.bodyMedium)
                MoneyText(amountCents = state.totalApprovedCents, style = MaterialTheme.typography.titleMedium)
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SmartPosSpacing.lg),
                horizontalArrangement = Arrangement.spacedBy(SmartPosSpacing.sm),
            ) {
                TransactionFilter.entries.forEach { filter ->
                    PosFilterChip(
                        label = filter.label,
                        selected = state.filter == filter,
                        onClick = { onFilterSelect(filter) },
                    )
                }
            }

            if (state.isEmpty) {
                EmptyState(title = "Nenhuma transação", description = "As vendas aparecem aqui conforme são feitas.")
            } else {
                LazyColumn(modifier = Modifier.padding(top = SmartPosSpacing.md)) {
                    state.groups.forEach { group ->
                        item(key = "header-${group.dayLabel}") {
                            Text(
                                text = group.dayLabel,
                                style = MaterialTheme.typography.labelLarge,
                                modifier =
                                    Modifier.padding(
                                        horizontal = SmartPosSpacing.lg,
                                        vertical = SmartPosSpacing.sm,
                                    ),
                            )
                        }
                        items(group.items, key = TransactionRowUi::id) { item ->
                            TransactionRow(item = item, onClick = { onTransactionClick(item.id) })
                        }
                    }
                }
            }
        }
    }
}

private val NO_OP_FILTER: (TransactionFilter) -> Unit = {}
private val NO_OP_STRING: (String) -> Unit = {}
private val NO_OP_DESTINATION: (BottomDestination) -> Unit = {}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionsScreenPreview() {
    SmartPosTheme {
        TransactionsScreen(
            state = TransactionsUiState(isLoading = false, groups = persistentListOf()),
            onFilterSelect = NO_OP_FILTER,
            onTransactionClick = NO_OP_STRING,
            onBottomNavigate = NO_OP_DESTINATION,
        )
    }
}
