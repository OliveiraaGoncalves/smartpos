package com.raimundo.smartpos.core.testing

import com.raimundo.smartpos.core.domain.preferences.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferences(
    hideValues: Boolean = false,
) : UserPreferences {
    private val state = MutableStateFlow(hideValues)
    override val hideValues = state

    override suspend fun setHideValues(hidden: Boolean) {
        state.value = hidden
    }
}
