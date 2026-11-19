package com.raimundo.smartpos.feature.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.raimundo.smartpos.core.model.PaymentType
import com.raimundo.smartpos.core.ui.component.MoneyText
import com.raimundo.smartpos.core.ui.component.NumericKeypad
import com.raimundo.smartpos.core.ui.component.PosButton
import com.raimundo.smartpos.core.ui.component.PosSnackbarHost
import com.raimundo.smartpos.core.ui.component.PosTopBar
import com.raimundo.smartpos.core.ui.component.SegmentedSelector
import com.raimundo.smartpos.core.ui.component.formatMoney
import com.raimundo.smartpos.core.ui.theme.SmartPosSpacing
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme

private val PAYMENT_OPTIONS =
    listOf(
        PaymentType.CREDITO to PaymentType.CREDITO.label,
        PaymentType.DEBITO to PaymentType.DEBITO.label,
    )

@Composable
fun SaleScreen(
    state: SaleUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onPaymentTypeSelect: (PaymentType) -> Unit,
    onPayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { PosTopBar(title = "Nova venda", onBack = onBack) },
        snackbarHost = { PosSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(SmartPosSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(SmartPosSpacing.lg),
        ) {
            SegmentedSelector(
                options = PAYMENT_OPTIONS,
                selectedValue = state.paymentType,
                onSelect = onPaymentTypeSelect,
            )

            MoneyText(
                amountCents = state.amountCents,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = SmartPosSpacing.xl),
            )

            NumericKeypad(
                onDigit = onDigit,
                onBackspace = onBackspace,
                modifier = Modifier.weight(1f, fill = false),
            )

            PosButton(
                text = "Pagar ${formatMoney(state.amountCents)}",
                onClick = onPayClick,
                enabled = state.canPay,
            )
        }
    }
}

@Preview(name = "Claro")
@Preview(name = "Escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SaleScreenPreview() {
    SmartPosTheme {
        SaleScreen(
            state = SaleUiState(amountCents = 1_000, paymentType = PaymentType.CREDITO),
            snackbarHostState = SnackbarHostState(),
            onBack = {},
            onDigit = {},
            onBackspace = {},
            onPaymentTypeSelect = {},
            onPayClick = {},
        )
    }
}
