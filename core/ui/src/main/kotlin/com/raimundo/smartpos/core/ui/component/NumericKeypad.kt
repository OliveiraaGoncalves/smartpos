package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.theme.SmartPosShapes
import com.raimundo.smartpos.core.ui.theme.SmartPosSizing
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

private val ROWS =
    listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("00", "0", "<"),
    )

/** Teclado numérico próprio da tela de venda: 1-9, 00, 0 e apagar, com estado pressionado. */
@Composable
fun NumericKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(SmartPosSpacing.sm)) {
        ROWS.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(SmartPosSpacing.sm)) {
                row.forEach { key ->
                    Key(
                        label = key,
                        onClick = { if (key == "<") onBackspace() else onDigit(key) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun Key(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scheme = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        modifier =
            modifier
                .height(SmartPosSizing.keyHeight)
                .semantics { contentDescription = if (label == "<") "Apagar" else label },
        shape = SmartPosShapes.medium,
        color = if (pressed) scheme.surfaceVariant else scheme.surface,
        interactionSource = interactionSource,
    ) {
        androidx.compose.foundation.layout.Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = if (label == "<") "⌫" else label, style = MaterialTheme.typography.headlineLarge)
        }
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NumericKeypadPreview() {
    SmartPosTheme {
        NumericKeypad(onDigit = {}, onBackspace = {})
    }
}
