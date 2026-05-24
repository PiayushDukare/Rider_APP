package com.ridervoice.ui.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.ridervoice.security.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AppSettingsState(
    val noiseCancellation: Boolean = true,
    val openMic: Boolean = true,
    val autoHudMode: Boolean = true,
    val gloveMode: Boolean = true,
    val micSensitivity: String = "High",
    val voxSensitivity: String = "Medium",
    val audioOutput: String = "Auto",
    val speedForHud: String = "15 km/h",
    val reconnectMode: String = "Auto"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val securePrefs: SecurePreferences
) : ViewModel() {

    private val _settingsState = MutableStateFlow(AppSettingsState())
    val settingsState: StateFlow<AppSettingsState> = _settingsState.asStateFlow()

    fun toggleNoiseCancellation() {
        val current = _settingsState.value.noiseCancellation
        _settingsState.value = _settingsState.value.copy(noiseCancellation = !current)
        // Todo: Actually save to SecurePreferences
    }

    fun toggleOpenMic() {
        val current = _settingsState.value.openMic
        _settingsState.value = _settingsState.value.copy(openMic = !current)
    }

    fun toggleAutoHud() {
        val current = _settingsState.value.autoHudMode
        _settingsState.value = _settingsState.value.copy(autoHudMode = !current)
    }

    fun toggleGloveMode() {
        val current = _settingsState.value.gloveMode
        _settingsState.value = _settingsState.value.copy(gloveMode = !current)
    }

    fun updateSettingValue(key: String, value: String) {
        val current = _settingsState.value
        _settingsState.value = when (key) {
            "micSensitivity" -> current.copy(micSensitivity = value)
            "voxSensitivity" -> current.copy(voxSensitivity = value)
            "audioOutput" -> current.copy(audioOutput = value)
            "speedForHud" -> current.copy(speedForHud = value)
            "reconnectMode" -> current.copy(reconnectMode = value)
            else -> current
        }
    }
}
