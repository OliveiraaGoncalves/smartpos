package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.theme.SmartPosColors
import com.raimundo.smartpos.core.ui.theme.SmartPosShapes
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

enum class BannerTone { INFO, WARNING, ERROR, SUCCESS }

/** No máximo um banner por tela (regra da §4 do desafio). */
@Composable
fun PosBanner(
    text: String,
    tone: BannerTone,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val extended = SmartPosColors.extended
    val (container, content, icon) =
        when (tone) {
            BannerTone.INFO -> {
                Triple(
                    scheme.primaryContainer,
                    scheme.onPrimaryContainer,
                    Icons.Filled.Info,
                )
            }

            BannerTone.WARNING -> {
                Triple(
                    extended.warningContainer,
                    extended.onWarningContainer,
                    Icons.Filled.Warning,
                )
            }

            BannerTone.ERROR -> {
                Triple(
                    scheme.errorContainer,
                    scheme.onErrorContainer,
                    Icons.Filled.Error,
                )
            }

            BannerTone.SUCCESS -> {
                Triple(
                    extended.successContainer,
                    extended.onSuccessContainer,
                    Icons.Filled.CheckCircle,
                )
            }
        }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = SmartPosShapes.medium,
        color = container,
        contentColor = content,
    ) {
        Row(
            modifier = Modifier.padding(SmartPosSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = SmartPosSpacing.md),
            )
        }
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PosBannerPreview() {
    SmartPosTheme {
        PosBanner(text = "Sem conexão. Aguarde a rede voltar.", tone = BannerTone.WARNING)
    }
}
