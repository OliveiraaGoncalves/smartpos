package com.raimundo.smartpos.core.testing

import com.raimundo.smartpos.core.domain.preferences.UserPreferences
import com.raimundo.smartpos.core.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferences(
    hideValues: Boolean = false,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
) : UserPreferences {
    private val hideValuesState = MutableStateFlow(hideValues)
    override val hideValues = hideValuesState

    override suspend fun setHideValues(hidden: Boolean) {
        hideValuesState.value = hidden
    }

    private val themeModeState = MutableStateFlow(themeMode)
    override val themeMode = themeModeState

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeModeState.value = mode
    }
}
