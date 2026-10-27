package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {
    private val keyDarkMode = booleanPreferencesKey("is_dark_mode")
    private val keyHaptics = booleanPreferencesKey("is_haptics_enabled")
    private val keySound = booleanPreferencesKey("is_sound_enabled")
    private val keyGeminiApiKey = androidx.datastore.preferences.core.stringPreferencesKey("gemini_api_key")

    val geminiApiKey: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[keyGeminiApiKey] ?: ""
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[keyDarkMode] ?: true // Dark mode by default per requirements
    }

    val isHapticsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[keyHaptics] ?: true
    }

    val isSoundEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[keySound] ?: true
    }

    suspend fun setGeminiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[keyGeminiApiKey] = key.trim()
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[keyDarkMode] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[keyHaptics] = enabled
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[keySound] = enabled
        }
    }
}
