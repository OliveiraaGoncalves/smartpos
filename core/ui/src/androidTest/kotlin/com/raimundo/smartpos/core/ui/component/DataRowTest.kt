package com.raimundo.smartpos.core.ui.component

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import org.junit.Rule
import org.junit.Test

private const val LONG_ID = "c72aa10d-0dfe-4b76-9deb-7879a435946f"

class DataRowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun valorExpansivelComecaRecolhidoComIconeDeExpandir() {
        composeRule.setContent {
            SmartPosTheme {
                DataRow(label = "ID da transação", value = LONG_ID, expandable = true)
            }
        }

        composeRule.onNode(hasContentDescription("Expandir")).assertExists()
    }

    @Test
    fun tocarNaLinhaExpansivelMostraOIconeDeRecolher() {
        composeRule.setContent {
            SmartPosTheme {
                DataRow(label = "ID da transação", value = LONG_ID, expandable = true)
            }
        }

        composeRule.onNodeWithText("ID da transação").performClick()

        composeRule.onNode(hasContentDescription("Recolher")).assertExists()
    }

    @Test
    fun linhaNaoExpansivelNaoTemIconeDeExpandir() {
        composeRule.setContent {
            SmartPosTheme {
                DataRow(label = "Terminal", value = "12345678")
            }
        }

        composeRule.onNode(hasContentDescription("Expandir")).assertDoesNotExist()
    }
}
