package com.raimundo.smartpos.feature.blocking

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raimundo.smartpos.core.ui.component.PosButton
import com.raimundo.smartpos.core.ui.component.PosButtonVariant
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import kotlinx.serialization.Serializable

@Serializable
enum class BlockingReason(
    val title: String,
    val description: String,
) {
    TERMINAL_NOT_CONFIGURED(
        title = "Terminal não configurado",
        description = "O EC e o terminal deste POS não puderam ser validados. Fale com o suporte.",
    ),
    INVALID_SECURITY_KEY(
        title = "Chave de segurança inválida",
        description = "Não foi possível carregar a chave de criptografia do terminal.",
    ),
}

/** Cenários 22/23: tela sem crash, sempre com saída (Tentar novamente / Suporte). */
@Composable
fun BlockingScreen(
    reason: BlockingReason,
    onRetryClick: () -> Unit,
    onSupportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(SmartPosSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = SmartPosSpacing.xl).size(48.dp),
            )
            Text(
                text = reason.title,
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = SmartPosSpacing.lg),
            )
            Text(
                text = reason.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = SmartPosSpacing.sm),
            )
            PosButton(
                text = "Tentar novamente",
                onClick = onRetryClick,
                modifier = Modifier.padding(top = SmartPosSpacing.xl),
            )
            PosButton(
                text = "Falar com o suporte",
                onClick = onSupportClick,
                variant = PosButtonVariant.TONAL,
                modifier = Modifier.padding(top = SmartPosSpacing.md),
            )
        }
    }
}

private val NO_OP: () -> Unit = {}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BlockingScreenPreview() {
    SmartPosTheme {
        BlockingScreen(
            reason = BlockingReason.TERMINAL_NOT_CONFIGURED,
            onRetryClick = NO_OP,
            onSupportClick = NO_OP,
        )
    }
}
