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

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val apiService: ApiService,
    private val liveKitManager: LiveKitManager
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = liveKitManager.connectionState

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants

    private val _muted = MutableStateFlow(false)
    val muted: StateFlow<Boolean> = _muted

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        // Sync participant list from LiveKit events
        liveKitManager.participants
            .onEach { identities ->
                _participants.value = identities.map { Participant(identity = it) }
            }
            .launchIn(viewModelScope)
    }

    fun joinRoom(roomName: String, userName: String) {
        viewModelScope.launch {
            try {
                _error.value = null

                // 1. Fetch token from backend
                val response = apiService.getToken(roomName, userName)

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
        liveKitManager.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        liveKitManager.disconnect()
    }
}
