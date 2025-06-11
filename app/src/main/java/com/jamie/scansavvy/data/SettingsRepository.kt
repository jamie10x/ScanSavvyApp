package com.jamie.scansavvy.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jamie.scansavvy.utils.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class AppSettings(
    val isBiometricLockEnabled: Boolean,
    val scanCount: Int,
    val lastReviewRequestTimestamp: Long,
    val themeMode: ThemeMode
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
        val SCAN_COUNT = intPreferencesKey("scan_count")
        val LAST_REVIEW_REQUEST = longPreferencesKey("last_review_request_timestamp")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data
        .map { preferences ->
            mapAppSettings(preferences)
        }

    suspend fun setBiometricLockEnabled(isEnabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.BIOMETRIC_LOCK_ENABLED] = isEnabled }
    }

    suspend fun incrementScanCount() {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.SCAN_COUNT] ?: 0
            preferences[PreferencesKeys.SCAN_COUNT] = currentCount + 1
        }
    }

    suspend fun updateReviewRequestTimestamp() {
        context.dataStore.edit { it[PreferencesKeys.LAST_REVIEW_REQUEST] = System.currentTimeMillis() }
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode.name
        }
    }

    private fun mapAppSettings(preferences: Preferences): AppSettings {
        val isBiometricLockEnabled = preferences[PreferencesKeys.BIOMETRIC_LOCK_ENABLED] ?: false
        val scanCount = preferences[PreferencesKeys.SCAN_COUNT] ?: 0
        val lastReviewRequestTimestamp = preferences[PreferencesKeys.LAST_REVIEW_REQUEST] ?: 0L
        val themeMode = ThemeMode.valueOf(
            preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        )
        return AppSettings(
            isBiometricLockEnabled = isBiometricLockEnabled,
            scanCount = scanCount,
            lastReviewRequestTimestamp = lastReviewRequestTimestamp,
            themeMode = themeMode
        )
    }
}