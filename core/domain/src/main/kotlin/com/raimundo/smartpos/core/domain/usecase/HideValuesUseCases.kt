package com.raimundo.smartpos.core.domain.usecase

import com.raimundo.smartpos.core.domain.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveHideValuesUseCase
    @Inject
    constructor(
        private val preferences: UserPreferences,
    ) {
        operator fun invoke(): Flow<Boolean> = preferences.hideValues
    }

class ToggleHideValuesUseCase
    @Inject
    constructor(
        private val preferences: UserPreferences,
    ) {
        suspend operator fun invoke(hidden: Boolean) = preferences.setHideValues(hidden)
    }
