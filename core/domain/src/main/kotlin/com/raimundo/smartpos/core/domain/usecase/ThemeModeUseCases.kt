package com.raimundo.smartpos.core.domain.usecase

import com.raimundo.smartpos.core.domain.preferences.UserPreferences
import com.raimundo.smartpos.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveThemeModeUseCase
    @Inject
    constructor(
        private val preferences: UserPreferences,
    ) {
        operator fun invoke(): Flow<ThemeMode> = preferences.themeMode
    }

class SetThemeModeUseCase
    @Inject
    constructor(
        private val preferences: UserPreferences,
    ) {
        suspend operator fun invoke(mode: ThemeMode) = preferences.setThemeMode(mode)
    }
