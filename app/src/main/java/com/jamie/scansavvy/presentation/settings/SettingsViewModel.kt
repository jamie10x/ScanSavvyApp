package com.jamie.scansavvy.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jamie.scansavvy.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // The ViewModel consumes the data layer's Flow and maps it to the UI layer's state.
    val settingsUiState: StateFlow<SettingsUiState> = settingsRepository.appSettingsFlow
        .map { appSettings ->
            SettingsUiState(isBiometricLockEnabled = appSettings.isBiometricLockEnabled)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState() // The initial value is now of the correct type.
        )

    fun onBiometricLockToggled(isEnabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBiometricLockEnabled(isEnabled)
        }
    }
}

// This data class belongs to the UI layer.
data class SettingsUiState(
    val isBiometricLockEnabled: Boolean = false
)