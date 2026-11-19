package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

/**
 * Linha rótulo/valor do card "Dados da transação". Com [expandable], o valor começa truncado
 * numa linha (ex.: um transactionId longo) e o toque expande/recolhe — nunca só corta o texto
 * sem dar um jeito de ver o conteúdo inteiro.
 */
@Composable
fun DataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    expandable: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .let { if (expandable) it.clickable { expanded = !expanded } else it }
                .padding(vertical = SmartPosSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(SmartPosSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End,
            maxLines = if (expandable && !expanded) 1 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (expandable) {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Recolher" else "Expandir",
                modifier = Modifier.size(20.dp).padding(top = 2.dp),
            )
        }
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DataRowPreview() {
    SmartPosTheme {
        DataRow(label = "Terminal", value = "12345678")
    }
}

@Preview(name = "Expansível - recolhido")
@Composable
private fun DataRowExpandableCollapsedPreview() {
    SmartPosTheme {
        DataRow(
            label = "ID da transação",
            value = "c72aa10d-0dfe-4b76-9deb-7879a435946f",
            expandable = true,
        )
    }
}
