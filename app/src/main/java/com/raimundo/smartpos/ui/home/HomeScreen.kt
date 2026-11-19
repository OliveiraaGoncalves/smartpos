package com.raimundo.smartpos.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raimundo.smartpos.domain.model.TransactionStatus
import com.raimundo.smartpos.ui.theme.SmartPosTheme
import com.raimundo.smartpos.ui.theme.StatusColors

@Composable
fun HomeRoute(viewModel: HomeViewModel, onSellClick: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreen(state = state, onSellClick = onSellClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(state: HomeUiState, onSellClick: () -> Unit) {
    var receiptToShow by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("SmartPOS") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Button(
                    onClick = onSellClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                ) {
                    Text("Vender", style = MaterialTheme.typography.headlineSmall)
                }
            }
            item {
                val suffix = if (state.inProgressCount > 0) " · ${state.inProgressCount} em andamento" else ""
                Text(
                    text = "Transações$suffix",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (state.transactions.isEmpty()) {
                item {
                    Text(
                        "Nenhuma transação ainda. Toque em Vender para começar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.transactions, key = TransactionItemUi::id) { item ->
                TransactionCard(item = item, onShowReceipt = { receiptToShow = it })
            }
        }
    }

    receiptToShow?.let { receipt ->
        AlertDialog(
            onDismissRequest = { receiptToShow = null },
            confirmButton = { TextButton(onClick = { receiptToShow = null }) { Text("Fechar") } },
            title = { Text("Comprovante impresso") },
            text = {
                Text(
                    text = receipt,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            },
        )
    }
}

@Composable
private fun TransactionCard(item: TransactionItemUi, onShowReceipt: (String) -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.amount, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                StatusChip(item)
            }
            Text(
                "${item.paymentType} · ${item.shortId}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            item.undoReason?.let {
                Text("Motivo do desfazimento: $it", style = MaterialTheme.typography.bodySmall)
            }
            if (item.inProgress) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            item.receipt?.let { receipt ->
                TextButton(onClick = { onShowReceipt(receipt) }) { Text("Ver comprovante") }
            }
        }
    }
}

@Composable
private fun StatusChip(item: TransactionItemUi) {
    val (background, content) = when (item.status) {
        TransactionStatus.APROVADA -> StatusColors.Approved to Color.White
        TransactionStatus.REJEITADA,
        TransactionStatus.DESFAZIMENTO_PENDENTE -> StatusColors.Rejected to Color.White
        TransactionStatus.DESFEITA -> StatusColors.Undone to Color.White
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(color = background, contentColor = content, shape = RoundedCornerShape(50)) {
        Text(
            item.statusLabel,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SmartPosTheme {
        HomeScreen(
            state = HomeUiState(
                listOf(
                    TransactionItemUi("a1", "a1b2c3d4", "R$ 12,34", "Crédito", TransactionStatus.AGUARDANDO_RESULTADO, "Aguardando resultado", true, null, null),
                    TransactionItemUi("b2", "e5f6a7b8", "R$ 99,90", "Débito", TransactionStatus.APROVADA, "Aprovada", false, null, "..."),
                    TransactionItemUi("c3", "c9d0e1f2", "R$ 10,20", "Crédito", TransactionStatus.DESFEITA, "Desfeita", false, "TEMPO ESGOTADO", "..."),
                ),
            ),
            onSellClick = {},
        )
    }
}
