package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raimundo.smartpos.core.ui.theme.SmartPosShapes
import com.raimundo.smartpos.core.ui.theme.SmartPosSizing
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

/** Seletor Crédito/Débito da tela de venda. */
@Composable
fun <T> SegmentedSelector(
    options: List<Pair<T, String>>,
    selectedValue: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .height(SmartPosSizing.minTouchTarget),
        shape = SmartPosShapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            options.forEach { (value, label) ->
                val isSelected = value == selectedValue
                val scheme = MaterialTheme.colorScheme
                val containerColor = if (isSelected) scheme.primary else scheme.surfaceVariant
                val labelColor = if (isSelected) scheme.onPrimary else scheme.onSurfaceVariant
                Surface(
                    onClick = { onSelect(value) },
                    modifier =
                        Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription = label
                                selected = isSelected
                            },
                    shape = SmartPosShapes.medium,
                    color = containerColor,
                    contentColor = labelColor,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = SmartPosSpacing.md),
                    ) {
                        Text(text = label, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SegmentedSelectorPreview() {
    SmartPosTheme {
        SegmentedSelector(
            options = listOf("CREDITO" to "Crédito", "DEBITO" to "Débito"),
            selectedValue = "CREDITO",
            onSelect = {},
        )
    }
}
