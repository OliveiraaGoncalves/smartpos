package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.raimundo.smartpos.core.ui.theme.SmartPosColors
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

enum class TimelineStepState { DONE, CURRENT, FAILED, PENDING }

data class TimelineStepUi(
    val title: String,
    val timestamp: String?,
    val state: TimelineStepState,
)

@Composable
fun TimelineStep(
    step: TimelineStepUi,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val extended = SmartPosColors.extended
    val (dotColor, icon) =
        when (step.state) {
            TimelineStepState.DONE -> extended.success to Icons.Filled.Check
            TimelineStepState.CURRENT -> scheme.primary to null
            TimelineStepState.FAILED -> scheme.error to Icons.Filled.Close
            TimelineStepState.PENDING -> scheme.outline to null
        }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .background(dotColor, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (icon !=
                null
            ) {
                Icon(icon, contentDescription = null, tint = scheme.surface, modifier = Modifier.size(16.dp))
            }
        }
        Column(modifier = Modifier.padding(start = SmartPosSpacing.md)) {
            Text(text = step.title, style = MaterialTheme.typography.bodyLarge)
            if (step.timestamp != null) {
                Text(
                    text = step.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TimelineStepPreview() {
    SmartPosTheme {
        Column {
            TimelineStep(TimelineStepUi("Autorizando", "14:32:01", TimelineStepState.DONE))
            TimelineStep(TimelineStepUi("Aguardando resultado", "14:32:02", TimelineStepState.CURRENT))
            TimelineStep(TimelineStepUi("Aprovada", null, TimelineStepState.PENDING))
        }
    }
}
