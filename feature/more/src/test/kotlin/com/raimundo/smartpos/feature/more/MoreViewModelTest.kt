package com.raimundo.smartpos.feature.more

import app.cash.turbine.test
import com.raimundo.smartpos.core.domain.usecase.ObserveThemeModeUseCase
import com.raimundo.smartpos.core.domain.usecase.SetThemeModeUseCase
import com.raimundo.smartpos.core.model.ThemeMode
import com.raimundo.smartpos.core.testing.FakeUserPreferences
import com.raimundo.smartpos.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoreViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val preferences = FakeUserPreferences()

    private fun viewModel() =
        MoreViewModel(
            observeThemeMode = ObserveThemeModeUseCase(preferences),
            setThemeMode = SetThemeModeUseCase(preferences),
        )

    @Test
    fun `tema comeca em Automatico por padrao`() {
        val vm = viewModel()

        assertEquals(ThemeMode.SYSTEM, vm.themeMode.value)
    }

    @Test
    fun `selecionar um tema atualiza a preferencia`() =
        runTest {
            val vm = viewModel()

            vm.themeMode.test {
                assertEquals(ThemeMode.SYSTEM, awaitItem())

                vm.onThemeModeSelect(ThemeMode.DARK)

                assertEquals(ThemeMode.DARK, awaitItem())
            }
        }
}
