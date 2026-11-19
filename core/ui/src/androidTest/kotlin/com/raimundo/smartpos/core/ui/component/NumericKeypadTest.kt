package com.raimundo.smartpos.core.ui.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import org.junit.Rule
import org.junit.Test

class NumericKeypadTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tocarDigitoChamaOnDigit() {
        val digits = mutableListOf<String>()
        composeRule.setContent {
            SmartPosTheme {
                NumericKeypad(onDigit = { digits += it }, onBackspace = {})
            }
        }

        composeRule.onNodeWithContentDescription("7").performClick()

        assertThat(digits).containsExactly("7")
    }

    @Test
    fun tocarApagarChamaOnBackspace() {
        var backspaceCount = 0
        composeRule.setContent {
            SmartPosTheme {
                NumericKeypad(onDigit = {}, onBackspace = { backspaceCount++ })
            }
        }

        composeRule.onNodeWithContentDescription("Apagar").performClick()

        assertThat(backspaceCount).isEqualTo(1)
    }
}
