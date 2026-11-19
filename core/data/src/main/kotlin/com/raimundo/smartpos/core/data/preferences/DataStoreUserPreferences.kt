package com.raimundo.smartpos.core.data.preferences

import com.raimundo.smartpos.core.domain.preferences.UserPreferences
import com.raimundo.smartpos.core.localstorage.prefs.UserPreferencesDataStore
import com.raimundo.smartpos.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DataStoreUserPreferences
    @Inject
    constructor(
        private val dataStore: UserPreferencesDataStore,
    ) : UserPreferences {
        override val hideValues: Flow<Boolean> = dataStore.hideValues

        override suspend fun setHideValues(hidden: Boolean) = dataStore.setHideValues(hidden)

        override val themeMode: Flow<ThemeMode> = dataStore.themeMode

        override suspend fun setThemeMode(mode: ThemeMode) = dataStore.setThemeMode(mode)
    }
