package com.raimundo.smartpos.core.localstorage.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.raimundo.smartpos.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "smartpos_prefs")

/** Preferências do operador, persistidas entre sessões. */
class UserPreferencesDataStore(
    private val context: Context,
) {
    val hideValues: Flow<Boolean> = context.dataStore.data.map { it[Keys.HIDE_VALUES] ?: false }

    suspend fun setHideValues(hide: Boolean) {
        context.dataStore.edit { it[Keys.HIDE_VALUES] = hide }
    }

    val themeMode: Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.THEME_MODE]?.let { stored ->
                runCatching { ThemeMode.valueOf(stored) }.getOrNull()
            } ?: ThemeMode.SYSTEM
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    private object Keys {
        val HIDE_VALUES = booleanPreferencesKey("hide_values")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
