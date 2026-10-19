package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zaswix_settings")

enum class ThemeMode {
    SYSTEM, DARK, LIGHT
}

class PreferencesManager(private val context: Context) {
    companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch_completed")
        private val KEY_ADS_ENABLED = booleanPreferencesKey("ads_enabled")
        private val KEY_HAPTIC = booleanPreferencesKey("haptic_feedback")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(raw)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    val isFirstLaunchCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_FIRST_LAUNCH] ?: false
    }

    val adsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ADS_ENABLED] ?: true
    }

    val hapticFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_HAPTIC] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setFirstLaunchCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FIRST_LAUNCH] = completed
        }
    }

    suspend fun setAdsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ADS_ENABLED] = enabled
        }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAPTIC] = enabled
        }
    }
}
