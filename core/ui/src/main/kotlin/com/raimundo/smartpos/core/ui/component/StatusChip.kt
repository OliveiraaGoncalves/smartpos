package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raimundo.smartpos.core.ui.theme.SmartPosColors
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

/** Papel semântico do status — nunca a cor isolada (acessibilidade para daltonismo: sempre ícone + texto). */
enum class StatusTone { PENDING, SUCCESS, WARNING, ERROR, NEUTRAL }

data class StatusUi(
    val label: String,
    val tone: StatusTone,
    val icon: ImageVector = iconFor(tone),
)

private fun iconFor(tone: StatusTone): ImageVector =
    when (tone) {
        StatusTone.PENDING -> Icons.Filled.HourglassEmpty
        StatusTone.SUCCESS -> Icons.Filled.CheckCircle
        StatusTone.WARNING -> Icons.Filled.Warning
        StatusTone.ERROR -> Icons.Filled.Error
        StatusTone.NEUTRAL -> Icons.Filled.CheckCircle
    }

@Composable
private fun toneColors(tone: StatusTone): Pair<Color, Color> {
    val extended = SmartPosColors.extended
    val scheme = MaterialTheme.colorScheme
    return when (tone) {
        StatusTone.PENDING -> scheme.onSurfaceVariant to scheme.surfaceVariant
        StatusTone.SUCCESS -> extended.success to extended.successContainer
        StatusTone.WARNING -> extended.warning to extended.warningContainer
        StatusTone.ERROR -> scheme.error to scheme.errorContainer
        StatusTone.NEUTRAL -> scheme.onSurfaceVariant to scheme.surfaceVariant
    }
}

@Composable
fun StatusChip(
    status: StatusUi,
    modifier: Modifier = Modifier,
) {
    val (contentColor, containerColor) = toneColors(status.tone)
    Row(
        modifier =
            modifier
                .background(containerColor, RoundedCornerShape(50))
                .padding(horizontal = SmartPosSpacing.md, vertical = SmartPosSpacing.xs)
                .semantics { contentDescription = status.label },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(status.icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
        Text(
            text = status.label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(start = SmartPosSpacing.xs),
        )
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatusChipPreview() {
    SmartPosTheme {
        Row {
            StatusChip(StatusUi("Aprovada", StatusTone.SUCCESS))
            StatusChip(StatusUi("Em andamento", StatusTone.PENDING))
            StatusChip(StatusUi("Rejeitada", StatusTone.ERROR))
        }
    }
}
