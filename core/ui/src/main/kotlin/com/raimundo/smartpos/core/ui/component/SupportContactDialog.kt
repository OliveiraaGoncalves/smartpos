package com.raimundo.smartpos.core.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

/**
 * Contato de suporte do terminal. Placeholder de desenvolvimento (igual à chave AES do flavor
 * `mock`, ver README > Segurança): num deploy real, viria da configuração do terminal, não
 * hardcoded aqui.
 */
@Composable
fun SupportContactDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Falar com o suporte") },
        text = {
            Column {
                DataRow(label = "Telefone", value = "0800 000 0000")
                DataRow(label = "E-mail", value = "suporte@smartpos.com.br")
                DataRow(label = "Horário", value = "Seg a sex, 8h-20h")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        },
    )
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SupportContactDialogPreview() {
    SmartPosTheme {
        SupportContactDialog(onDismiss = {})
    }
}
