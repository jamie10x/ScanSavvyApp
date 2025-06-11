package com.jamie.scansavvy.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.scansavvy.data.AppSettings
import com.jamie.scansavvy.data.SettingsRepository
import com.jamie.scansavvy.utils.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // The state now directly maps the AppSettings model from the repository.
    val settingsUiState: StateFlow<AppSettings> = settingsRepository.appSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings(
                isBiometricLockEnabled = false,
                scanCount = 0,
                lastReviewRequestTimestamp = 0,
                themeMode = ThemeMode.SYSTEM
            )
        )

    fun onBiometricLockToggled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricLockEnabled(isEnabled)
        }
    }

    fun onThemeChanged(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode)
        }
    }
}