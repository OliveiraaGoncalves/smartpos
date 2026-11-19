package com.raimundo.smartpos.feature.receipt

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.component.PosButton
import com.raimundo.smartpos.core.ui.component.PosButtonVariant
import com.raimundo.smartpos.core.ui.component.PosSnackbarHost
import com.raimundo.smartpos.core.ui.component.PosTopBar
import com.raimundo.smartpos.core.ui.component.ReceiptText
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

@Composable
fun ReceiptScreen(
    state: ReceiptUiState,
    snackbarHostState: SnackbarHostState,
    onClose: () -> Unit,
    onReprintClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { PosTopBar(title = "Comprovante", onBack = onClose) },
        snackbarHost = { PosSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(SmartPosSpacing.lg)) {
            ReceiptText(text = state.receiptText, modifier = Modifier.fillMaxWidth())
            HorizontalDivider(modifier = Modifier.padding(vertical = SmartPosSpacing.lg))
            PosButton(text = "Reimprimir", onClick = onReprintClick, variant = PosButtonVariant.TONAL)
            PosButton(
                text = "Fechar",
                onClick = onClose,
                modifier = Modifier.padding(top = SmartPosSpacing.md),
            )
        }
    }
}

private val NO_OP: () -> Unit = {}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReceiptScreenPreview() {
    SmartPosTheme {
        ReceiptScreen(
            state = ReceiptUiState(isLoading = false, receiptText = "--------------------------------\nCOMPROVANTE"),
            snackbarHostState = SnackbarHostState(),
            onClose = NO_OP,
            onReprintClick = NO_OP,
        )
    }
}
