package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

/** Cabeçalho da tela de Detalhe: selo + status por extenso + valor + subtítulo para o operador. */
@Composable
fun ResultHeader(
    status: StatusUi,
    amountCents: Long,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(SmartPosSpacing.lg)
                .semantics { contentDescription = "${status.label}, ${formatMoney(amountCents)}. $subtitle" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        StatusChip(status)
        MoneyText(
            amountCents = amountCents,
            modifier = Modifier.padding(top = SmartPosSpacing.md),
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = SmartPosSpacing.xs),
        )
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ResultHeaderPreview() {
    SmartPosTheme {
        ResultHeader(
            status = StatusUi("Aprovada", StatusTone.SUCCESS),
            amountCents = 10_000,
            subtitle = "Venda concluída com sucesso",
        )
    }
}
