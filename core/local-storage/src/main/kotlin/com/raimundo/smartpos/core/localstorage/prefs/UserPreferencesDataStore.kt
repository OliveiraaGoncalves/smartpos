package com.raimundo.smartpos.core.localstorage.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "smartpos_prefs")

/** Preferência "olho" da Home: oculta/mostra valores monetários, persistida entre sessões. */
class UserPreferencesDataStore(
    private val context: Context,
) {
    val hideValues: Flow<Boolean> = context.dataStore.data.map { it[Keys.HIDE_VALUES] ?: false }

    suspend fun setHideValues(hide: Boolean) {
        context.dataStore.edit { it[Keys.HIDE_VALUES] = hide }
    }

    private object Keys {
        val HIDE_VALUES = booleanPreferencesKey("hide_values")
    }
}
