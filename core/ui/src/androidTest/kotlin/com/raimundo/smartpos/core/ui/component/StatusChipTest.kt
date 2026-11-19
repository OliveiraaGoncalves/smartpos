package com.raimundo.smartpos.core.ui.component

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.raimundo.smartpos.core.ui.theme.SmartPosTheme
import org.junit.Rule
import org.junit.Test

class StatusChipTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun statusChipMostraIconeETexto() {
        composeRule.setContent {
            SmartPosTheme {
                StatusChip(StatusUi("Aprovada", StatusTone.SUCCESS))
            }
        }

        composeRule.onNodeWithText("Aprovada").assertExists()
    }
}
