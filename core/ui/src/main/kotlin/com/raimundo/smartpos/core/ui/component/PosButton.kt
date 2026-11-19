package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.theme.SmartPosShapes
import com.raimundo.smartpos.core.ui.theme.SmartPosSizing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

enum class PosButtonVariant { PRIMARY, TONAL }

/**
 * Botão primário/tonal. [enabled] só controla a aparência: o toque SEMPRE dispara [onClick],
 * mesmo "desabilitado" — é o chamador que decide mostrar um snackbar explicando o motivo
 * (regra de ouro de interação: nenhum botão "morto").
 */
@Composable
fun PosButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PosButtonVariant = PosButtonVariant.PRIMARY,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val (container, content) =
        when {
            !enabled -> scheme.surfaceVariant to scheme.onSurfaceVariant
            variant == PosButtonVariant.PRIMARY -> scheme.primary to scheme.onPrimary
            else -> scheme.primaryContainer to scheme.onPrimaryContainer
        }

    Surface(
        onClick = onClick,
        modifier =
            modifier
                .fillMaxWidth()
                .height(SmartPosSizing.buttonHeight),
        shape = SmartPosShapes.large,
        color = container,
        contentColor = content,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PosButtonPreview() {
    SmartPosTheme {
        PosButton(text = "Pagar R$ 10,00", onClick = {})
    }
}

@Preview(name = "Desabilitado - continua respondendo")
@Composable
private fun PosButtonDisabledPreview() {
    SmartPosTheme {
        PosButton(text = "Pagar", onClick = {}, enabled = false)
    }
}
