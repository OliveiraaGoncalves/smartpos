package com.raimundo.smartpos.core.ui.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

/** Formata centavos em pt-BR (R$ 1.234,56) independente do Locale do dispositivo. */
@Composable
fun MoneyText(
    amountCents: Long,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    style: TextStyle = MaterialTheme.typography.displayLarge,
    color: Color = LocalContentColor.current,
) {
    val text = if (hidden) HIDDEN_PLACEHOLDER else formatMoney(amountCents)
    val semanticsModifier =
        if (hidden) {
            // Sem isso, o leitor de tela lê os bullets um a um em vez de "valor oculto".
            modifier.semantics { contentDescription = "Valor oculto" }
        } else {
            modifier
        }
    Text(text = text, modifier = semanticsModifier, style = style, color = color)
}

private const val HIDDEN_PLACEHOLDER = "R$ ••••"

fun formatMoney(cents: Long): String {
    val negative = cents < 0
    val abs = kotlin.math.abs(cents)
    val reais =
        (abs / 100)
            .toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    val centavos = (abs % 100).toString().padStart(2, '0')
    return (if (negative) "-R$ " else "R$ ") + "$reais,$centavos"
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MoneyTextPreview() {
    SmartPosTheme {
        MoneyText(amountCents = 123_456)
    }
}
