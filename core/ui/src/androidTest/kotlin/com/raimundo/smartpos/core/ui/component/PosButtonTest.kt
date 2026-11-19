package com.raimundo.smartpos.core.ui.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import org.junit.Rule
import org.junit.Test

class PosButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** Regra de ouro: botão "desabilitado" continua respondendo ao toque (nenhum botão morto). */
    @Test
    fun botaoDesabilitadoContinuaRespondendoAoToque() {
        var clicks = 0
        composeRule.setContent {
            SmartPosTheme {
                PosButton(text = "Pagar", onClick = { clicks++ }, enabled = false)
            }
        }

        composeRule.onNodeWithText("Pagar").performClick()

        assertThat(clicks).isEqualTo(1)
    }

    @Test
    fun botaoHabilitadoRespondeAoToque() {
        var clicks = 0
        composeRule.setContent {
            SmartPosTheme {
                PosButton(text = "Pagar", onClick = { clicks++ })
            }
        }

        composeRule.onNodeWithText("Pagar").performClick()

        assertThat(clicks).isEqualTo(1)
    }
}
