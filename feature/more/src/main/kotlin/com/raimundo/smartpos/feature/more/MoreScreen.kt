package com.raimundo.smartpos.feature.more

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.model.ThemeMode
import com.raimundo.smartpos.core.ui.component.BottomDestination
import com.raimundo.smartpos.core.ui.component.DataRow
import com.raimundo.smartpos.core.ui.component.MenuItem
import com.raimundo.smartpos.core.ui.component.PosBottomBar
import com.raimundo.smartpos.core.ui.component.PosTopBar
import com.raimundo.smartpos.core.ui.component.SupportContactDialog
import com.raimundo.smartpos.core.ui.component.ThemeModeDialog
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

@Composable
fun MoreScreen(
    appVersion: String,
    merchantId: String,
    terminalId: String,
    themeMode: ThemeMode,
    onThemeModeSelect: (ThemeMode) -> Unit,
    onPendingsClick: () -> Unit,
    onBottomNavigate: (BottomDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTerminalDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

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
            MenuItem(label = "Dados do terminal", onClick = { showTerminalDialog = true })
            MenuItem(label = "Tema · ${themeMode.label}", onClick = { showThemeDialog = true })
            HorizontalDivider()
            MenuItem(label = "Falar com o suporte", onClick = { showSupportDialog = true })

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

    if (showTerminalDialog) {
        AlertDialog(
            onDismissRequest = { showTerminalDialog = false },
            title = { Text("Dados do terminal") },
            text = {
                Column {
                    DataRow(label = "EC", value = merchantId)
                    DataRow(label = "Terminal", value = terminalId)
                }
            },
            confirmButton = {
                TextButton(onClick = { showTerminalDialog = false }) {
                    Text("Fechar")
                }
            },
        )
    }

    if (showSupportDialog) {
        SupportContactDialog(onDismiss = { showSupportDialog = false })
    }

    if (showThemeDialog) {
        ThemeModeDialog(
            selected = themeMode,
            onSelect = onThemeModeSelect,
            onDismiss = { showThemeDialog = false },
        )
    }
}

private val NO_OP: () -> Unit = {}
private val NO_OP_DESTINATION: (BottomDestination) -> Unit = {}
private val NO_OP_THEME: (ThemeMode) -> Unit = {}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MoreScreenPreview() {
    SmartPosTheme {
        MoreScreen(
            appVersion = "1.0.0",
            merchantId = "123456789012345",
            terminalId = "12345678",
            themeMode = ThemeMode.SYSTEM,
            onThemeModeSelect = NO_OP_THEME,
            onPendingsClick = NO_OP,
            onBottomNavigate = NO_OP_DESTINATION,
        )
    }
}
