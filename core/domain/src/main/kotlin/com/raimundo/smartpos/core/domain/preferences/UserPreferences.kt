package com.raimundo.smartpos.core.domain.preferences

import kotlinx.coroutines.flow.Flow

/** Preferência "olho" da Home: oculta/mostra valores monetários. */
interface UserPreferences {
    val hideValues: Flow<Boolean>

    suspend fun setHideValues(hidden: Boolean)
}
