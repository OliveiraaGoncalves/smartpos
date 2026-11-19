package com.raimundo.smartpos.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raimundo.smartpos.core.domain.usecase.ObserveThemeModeUseCase
import com.raimundo.smartpos.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Tema lido no topo da árvore de composição, antes do StartupGate decidir qual tela mostrar. */
@HiltViewModel
class ThemeViewModel
    @Inject
    constructor(
        observeThemeMode: ObserveThemeModeUseCase,
    ) : ViewModel() {
        val themeMode: StateFlow<ThemeMode> =
            observeThemeMode()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ThemeMode.SYSTEM)

        private companion object {
            const val STOP_TIMEOUT_MS = 5_000L
        }
    }
