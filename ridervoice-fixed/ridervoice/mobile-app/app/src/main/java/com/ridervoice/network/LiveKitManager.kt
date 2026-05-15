package com.ridervoice.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.roomOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveKitManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var room: Room? = null

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _participants = MutableStateFlow<List<String>>(emptyList())
    val participants: StateFlow<List<String>> = _participants

    fun connect(url: String, token: String) {

        _connectionState.value = ConnectionState.CONNECTING

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // FIX: pass real appContext — was null before
                room = LiveKit.create(appContext = context)

                room?.connect(
                    url = url,
                    token = token,
                    options = roomOptions {
                        adaptiveStream = true
                        dynacast = true
                    }
                )

                room?.localParticipant?.setMicrophoneEnabled(true)

                _connectionState.value = ConnectionState.CONNECTED

                // Collect and handle room events
                room?.events?.collect { event ->
                    handleEvent(event)
                }

            } catch (e: Exception) {
                _connectionState.value = ConnectionState.FAILED
                e.printStackTrace()
            }
        }
    }

    private fun handleEvent(event: RoomEvent) {
        when (event) {
            is RoomEvent.ParticipantConnected -> updateParticipantList()
            is RoomEvent.ParticipantDisconnected -> updateParticipantList()
            is RoomEvent.Disconnected -> _connectionState.value = ConnectionState.DISCONNECTED
            else -> {}
        }
    }

    private fun updateParticipantList() {
        val ids = room?.remoteParticipants?.keys?.toList() ?: emptyList()
        _participants.value = ids
    }

    fun disconnect() {
        room?.disconnect()
        room = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _participants.value = emptyList()
    }

    fun setMicrophoneEnabled(enabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            room?.localParticipant?.setMicrophoneEnabled(enabled)
        }
    }
}
