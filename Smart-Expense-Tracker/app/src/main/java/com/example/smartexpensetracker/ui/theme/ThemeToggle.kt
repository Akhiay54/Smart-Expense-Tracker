package com.example.smartexpensetracker.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.preferencesDataStore

enum class ThemeMode { System, Light, Dark }

private val Context.dataStore by preferencesDataStore(name = "settings")
private val KEY_THEME = stringPreferencesKey("theme_mode")

class ThemePreferences(private val context: Context) {
    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[KEY_THEME]) {
            ThemeMode.Light.name -> ThemeMode.Light
            ThemeMode.Dark.name -> ThemeMode.Dark
            else -> ThemeMode.System
        }
    }

    suspend fun save(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }
}