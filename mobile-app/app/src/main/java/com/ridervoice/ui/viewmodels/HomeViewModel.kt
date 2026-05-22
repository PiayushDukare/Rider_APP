package com.ridervoice.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class HomeState(
    val hasActiveRide: Boolean = false,
    val activeRideName: String = "",
    val activeRideSubtitle: String = "Start or join a ride to connect",
    val deviceName: String = "Cardo Packtalk",
    val deviceStatus: String = "Connected",
    val isDeviceConnected: Boolean = true,
    val networkStatus: String = "Strong",
    val isNetworkStrong: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    // Example methods to dynamically alter state later based on real sensors/data
    fun updateDeviceStatus(isConnected: Boolean, name: String = "Cardo Packtalk") {
        _uiState.value = _uiState.value.copy(
            isDeviceConnected = isConnected,
            deviceName = name,
            deviceStatus = if (isConnected) "Connected" else "Disconnected"
        )
    }

    fun updateNetworkStatus(isStrong: Boolean) {
        _uiState.value = _uiState.value.copy(
            isNetworkStrong = isStrong,
            networkStatus = if (isStrong) "Strong" else "Weak"
        )
    }

    fun setActiveRide(roomName: String) {
        _uiState.value = _uiState.value.copy(
            hasActiveRide = true,
            activeRideName = roomName,
            activeRideSubtitle = "Currently sharing location & audio"
        )
    }
}
