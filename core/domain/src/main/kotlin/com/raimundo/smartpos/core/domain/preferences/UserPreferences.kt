package com.raimundo.smartpos.core.domain.preferences

import com.raimundo.smartpos.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** Preferências do operador, persistidas entre sessões. */
interface UserPreferences {
    /** "Olho" da Home: oculta/mostra valores monetários. */
    val hideValues: Flow<Boolean>

    suspend fun setHideValues(hidden: Boolean)

    val themeMode: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
