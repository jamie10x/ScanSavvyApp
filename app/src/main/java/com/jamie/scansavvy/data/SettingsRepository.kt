package com.jamie.scansavvy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val isBiometricLockEnabled: Boolean
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
    }

    // The repository now exposes a Flow of its own data model.
    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data
        .map { preferences ->
            mapAppSettings(preferences)
        }

    suspend fun setBiometricLockEnabled(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_LOCK_ENABLED] = isEnabled
        }
    }

    private fun mapAppSettings(preferences: Preferences): AppSettings {
        val isBiometricLockEnabled = preferences[PreferencesKeys.BIOMETRIC_LOCK_ENABLED] ?: false
        return AppSettings(isBiometricLockEnabled = isBiometricLockEnabled)
    }
}