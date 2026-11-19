package com.raimundo.smartpos.feature.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.core.domain.usecase.ObserveThemeModeUseCase
import com.raimundo.smartpos.core.domain.usecase.SetThemeModeUseCase
import com.raimundo.smartpos.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoreViewModel
    @Inject
    constructor(
        observeThemeMode: ObserveThemeModeUseCase,
        private val setThemeMode: SetThemeModeUseCase,
    ) : ViewModel() {
        val themeMode: StateFlow<ThemeMode> =
            observeThemeMode()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ThemeMode.SYSTEM)

        fun onThemeModeSelect(mode: ThemeMode) {
            viewModelScope.launch { setThemeMode(mode) }
        }

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
