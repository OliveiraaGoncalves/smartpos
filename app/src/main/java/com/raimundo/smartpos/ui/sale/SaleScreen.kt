package com.raimundo.smartpos.ui.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raimundo.smartpos.domain.model.Money
import com.raimundo.smartpos.domain.model.PaymentType
import com.raimundo.smartpos.ui.theme.SmartPosTheme

@Composable
fun SaleRoute(
    viewModel: SaleViewModel,
    onBack: () -> Unit,
    onSaleStarted: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SaleEvent.SaleStarted -> onSaleStarted()
            }
        }
    }

    SaleScreen(
        state = state,
        onAmountChange = viewModel::onAmountChange,
        onPaymentTypeSelected = viewModel::onPaymentTypeSelected,
        onConfirm = viewModel::onConfirm,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleScreen(
    state: SaleUiState,
    onAmountChange: (String) -> Unit,
    onPaymentTypeSelected: (PaymentType) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nova venda") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedTextField(
                value = state.amountDigits,
                onValueChange = onAmountChange,
                label = { Text("Valor") },
                singleLine = true,
                enabled = !state.submitting,
                visualTransformation = CurrencyVisualTransformation,
                textStyle = MaterialTheme.typography.headlineMedium,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Tipo de pagamento", style = MaterialTheme.typography.titleMedium)
            val options = PaymentType.entries
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, type ->
                    SegmentedButton(
                        selected = state.paymentType == type,
                        onClick = { onPaymentTypeSelected(type) },
                        enabled = !state.submitting,
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    ) {
                        Text(type.label)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onConfirm,
                enabled = state.canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            ) {
                Text("Pagar ${Money.format(state.amountCents)}", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Exibe os dígitos digitados como moeda: "1234" -> "R$ 12,34" (cursor sempre no fim). */
private object CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = Money.format(text.text.toLongOrNull() ?: 0L)
        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int = formatted.length
                override fun transformedToOriginal(offset: Int): Int = text.length
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SaleScreenPreview() {
    SmartPosTheme {
        SaleScreen(
            state = SaleUiState(amountDigits = "1234", paymentType = PaymentType.CREDITO),
            onAmountChange = {},
            onPaymentTypeSelected = {},
            onConfirm = {},
            onBack = {},
        )
    }
}
