package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.theme.SmartPosSizing
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

data class TransactionRowUi(
    val id: String,
    val subtitle: String,
    val amountCents: Long,
    val status: StatusUi,
)

@Composable
fun TransactionRow(
    item: TransactionRowUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hideAmount: Boolean = false,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = SmartPosSizing.minTouchTarget)
                .clickable(onClick = onClick)
                .padding(horizontal = SmartPosSpacing.lg, vertical = SmartPosSpacing.md)
                .semantics { contentDescription = "${item.subtitle}, ${item.status.label}" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = item.subtitle, style = MaterialTheme.typography.bodyLarge)
            StatusChip(item.status, modifier = Modifier.padding(top = SmartPosSpacing.xs))
        }
        MoneyText(
            amountCents = item.amountCents,
            hidden = hideAmount,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TransactionRowPreview() {
    SmartPosTheme {
        TransactionRow(
            item =
                TransactionRowUi(
                    id = "tx-1",
                    subtitle = "Crédito · 14:32",
                    amountCents = 5_000,
                    status = StatusUi("Aprovada", StatusTone.SUCCESS),
                ),
            onClick = {},
        )
    }
}
