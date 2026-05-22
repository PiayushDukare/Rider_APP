package com.ridervoice.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridervoice.models.Participant
import com.ridervoice.network.ApiService
import com.ridervoice.network.ConnectionState
import com.ridervoice.network.LiveKitManager
import com.ridervoice.utils.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.ridervoice.audio.HardwarePTTManager
import com.ridervoice.models.RiderLocation
import com.ridervoice.services.LocationService
import com.ridervoice.services.RideRecorder
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

import com.ridervoice.network.NetworkHealth
import com.ridervoice.network.NetworkResilienceManager

import com.ridervoice.services.ThermalManager
import com.ridervoice.services.ServiceWatchdog

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val apiService: ApiService,
    private val liveKitManager: LiveKitManager,
    private val locationService: LocationService,
    val hardwarePTTManager: HardwarePTTManager,
    val networkResilienceManager: NetworkResilienceManager,
    private val rideRecorder: RideRecorder,
    private val thermalManager: ThermalManager,
    private val serviceWatchdog: ServiceWatchdog
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = liveKitManager.connectionState
    val remoteLocations: StateFlow<Map<String, RiderLocation>> = liveKitManager.remoteLocations
    val networkHealth: StateFlow<NetworkHealth> = networkResilienceManager.networkHealth

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants

    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentUserName = ""

    init {
        // Sync participant list with Ghost Rider logic
        liveKitManager.participants
            .onEach { activeIdentities ->
                val currentParticipants = _participants.value.toMutableList()
                val now = System.currentTimeMillis()

                // Mark disconnected as Ghosts
                currentParticipants.forEachIndexed { index, p ->
                    if (!activeIdentities.contains(p.identity)) {
                        if (!p.isGhost) {
                            currentParticipants[index] = p.copy(isGhost = true, disconnectedAt = now)
                        }
                    }
                }

                // Add newly connected
                activeIdentities.forEach { identity ->
                    val existingIdx = currentParticipants.indexOfFirst { it.identity == identity }
                    if (existingIdx >= 0) {
                        // Reconnected!
                        currentParticipants[existingIdx] = currentParticipants[existingIdx].copy(isGhost = false, disconnectedAt = null)
                    } else {
                        currentParticipants.add(Participant(identity = identity))
                    }
                }

                _participants.value = currentParticipants.toList()
            }
            .launchIn(viewModelScope)

        // Broadcast local location when it updates
        locationService.currentLocation
            .filterNotNull()
            .onEach { loc ->
                if (liveKitManager.connectionState.value == ConnectionState.CONNECTED) {
                    liveKitManager.publishLocation(
                        RiderLocation(
                            riderId = currentUserName,
                            lat = loc.latitude,
                            lng = loc.longitude,
                            speed = loc.speed,
                            heading = loc.bearing,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
            .launchIn(viewModelScope)

        // Throttle Telemetry on Weak Signal
        networkResilienceManager.networkHealth
            .onEach { health ->
                locationService.setNetworkDegraded(health == NetworkHealth.DEGRADED)
            }
            .launchIn(viewModelScope)

        // Wire PTT to LiveKit Mic
        hardwarePTTManager.onMicToggleRequest = { isMicOpen ->
            _muted.value = !isMicOpen
            liveKitManager.setMicrophoneEnabled(isMicOpen)
        }
        
        // Ghost Rider Purge Loop
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60000) // check every minute
                val now = System.currentTimeMillis()
                _participants.value = _participants.value.filter {
                    if (it.isGhost && it.disconnectedAt != null) {
                        (now - it.disconnectedAt) < (15 * 60 * 1000) // Keep for 15 mins max
                    } else {
                        true
                    }
                }
            }
        }
    }

    fun joinRoom(roomName: String, userName: String) {
        currentUserName = userName
        locationService.startTracking()
        hardwarePTTManager.activateSession()
        rideRecorder.startRecording(roomName)
        thermalManager.startMonitoring(viewModelScope)
        serviceWatchdog.startMonitoring(viewModelScope)
        viewModelScope.launch {
            try {
                _error.value = null

                // 1. Fetch token from backend
                val response = apiService.getRoomToken(com.ridervoice.models.RoomTokenRequest(roomName))

                if (!response.isSuccessful || response.body() == null) {
                    _error.value = "Failed to get token: ${response.code()}"
                    return@launch
                }

                val roomData = response.body()!!

                // 2. Connect to LiveKit with real token
                liveKitManager.connect(
                    url = Constants.LIVEKIT_URL,
                    token = roomData.token
                )

            } catch (e: Exception) {
                _error.value = "Connection error: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun toggleMute() {
        val newMuted = !_muted.value
        _muted.value = newMuted
        liveKitManager.setMicrophoneEnabled(!newMuted)
    }

    fun leaveRoom() {
        cleanup()
        liveKitManager.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }

    private fun cleanup() {
        locationService.stopTracking()
        hardwarePTTManager.deactivateSession()
        rideRecorder.stopRecording()
        thermalManager.stopMonitoring()
        serviceWatchdog.stopMonitoring()
    }
}
