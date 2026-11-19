package com.raimundo.smartpos

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.raimundo.smartpos.core.localstorage.db.SmartPosDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Fluxos de ponta a ponta no flavor `mock` (backend em processo, sem servidor real).
 * Os cenários são escolhidos pelos centavos do valor (ver AmountBasedScenarios em core:network-mock).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SmartPosE2ETest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var database: SmartPosDatabase

    @Before
    fun setUp() {
        hiltRule.inject()
        // Isola cada teste de dados deixados por execuções anteriores no mesmo dispositivo.
        database.clearAllTables()
    }

    private fun startSale(
        digits: String,
        paymentLabel: String = "Crédito",
    ) {
        composeRule.onNodeWithText("Vender").performClick()
        composeRule.onNodeWithContentDescription(paymentLabel).performClick()
        digits.forEach { digit -> composeRule.onNodeWithContentDescription(digit.toString()).performClick() }
        composeRule.onNode(hasText("Pagar", substring = true)).performClick()
    }

    private fun waitForRowWithStatus(
        status: String,
        timeoutMillis: Long,
    ) {
        val matcher = hasContentDescription(status, substring = true)
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun venda_aprovada_mostra_status_e_comprovante() {
        // R$ 10,00 -> mock aprova.
        startSale(digits = "1000")

        waitForRowWithStatus("Aprovada", timeoutMillis = 10_000)
        composeRule.onAllNodes(hasContentDescription("Aprovada", substring = true)).onFirst().performClick()

        composeRule.onAllNodes(hasContentDescription("Aprovada", substring = true)).onFirst().assertExists()
        composeRule.onNodeWithText("Ver comprovante").performClick()
        composeRule.onNodeWithText("Comprovante").assertExists()
    }

    @Test
    fun venda_sem_resposta_desfaz_apos_timeout() {
        // R$ 10,20 -> mock nunca responde: engine estoura os 15s e chama /undo -> Desfeita.
        startSale(digits = "1020")

        waitForRowWithStatus("Desfeita", timeoutMillis = 20_000)
        composeRule.onAllNodes(hasContentDescription("Desfeita", substring = true)).onFirst().performClick()
        composeRule.onAllNodes(hasContentDescription("Desfeita", substring = true)).onFirst().assertExists()
    }

    @Test
    fun duas_vendas_concorrentes_nao_bloqueiam_a_ui() {
        // A primeira venda retorna à Home imediatamente (fire-and-forget); a UI segue respondendo
        // para disparar a segunda venda sem esperar o resultado da primeira.
        startSale(digits = "1000") // R$ 10,00 -> aprova.
        composeRule.onNodeWithText("Vender").assertExists()

        startSale(digits = "13") // R$ 0,13 -> mock rejeita.

        waitForRowWithStatus("Aprovada", timeoutMillis = 10_000)
        waitForRowWithStatus("Rejeitada", timeoutMillis = 10_000)
    }
}
