package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raimundo.smartpos.core.ui.theme.ReceiptTextStyle
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing

/** Folha inferior do comprovante (fonte mono, 32 colunas) e da falha de impressão. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, modifier = modifier) {
        content()
    }
}

@Composable
fun ReceiptText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(text = text, style = ReceiptTextStyle, modifier = modifier.padding(horizontal = SmartPosSpacing.lg))
}
