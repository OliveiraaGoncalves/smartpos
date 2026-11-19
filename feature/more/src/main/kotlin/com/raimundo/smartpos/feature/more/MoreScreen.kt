package com.raimundo.smartpos.feature.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.component.BottomDestination
import com.raimundo.smartpos.core.ui.component.MenuItem
import com.raimundo.smartpos.core.ui.component.PosBottomBar
import com.raimundo.smartpos.core.ui.component.PosTopBar
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

@Composable
fun MoreScreen(
    appVersion: String,
    terminalId: String,
    onPendingsClick: () -> Unit,
    onTerminalDataClick: () -> Unit,
    onSupportClick: () -> Unit,
    onBottomNavigate: (BottomDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { PosTopBar(title = "Mais") },
        bottomBar = { PosBottomBar(selected = BottomDestination.MORE, onSelect = onBottomNavigate) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                text = "Operação",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(SmartPosSpacing.lg),
            )
            MenuItem(label = "Pendências", onClick = onPendingsClick)
            MenuItem(label = "Dados do terminal", onClick = onTerminalDataClick)
            HorizontalDivider()
            MenuItem(label = "Falar com o suporte", onClick = onSupportClick)

            Column(modifier = Modifier.fillMaxWidth().padding(SmartPosSpacing.lg)) {
                Text(
                    text = "Versão $appVersion · TID $terminalId",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private val NO_OP: () -> Unit = {}
private val NO_OP_DESTINATION: (BottomDestination) -> Unit = {}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MoreScreenPreview() {
    SmartPosTheme {
        MoreScreen(
            appVersion = "1.0.0",
            terminalId = "12345678",
            onPendingsClick = NO_OP,
            onTerminalDataClick = NO_OP,
            onSupportClick = NO_OP,
            onBottomNavigate = NO_OP_DESTINATION,
        )
    }
}
