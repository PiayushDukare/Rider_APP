package com.ridervoice.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridervoice.network.ConnectionState
import com.ridervoice.services.VoiceForegroundService
import com.ridervoice.state.RoomViewModel
import com.ridervoice.ui.components.ParticipantCard
import com.ridervoice.ui.components.VoiceControls

@Composable
fun RoomScreen(
    roomName: String,
    userName: String,
    onLeave: () -> Unit,
    // FIX: inject real ViewModel via Hilt
    viewModel: RoomViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val connectionState by viewModel.connectionState.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val muted by viewModel.muted.collectAsState()
    val error by viewModel.error.collectAsState()

    // Connect on first composition
    LaunchedEffect(roomName, userName) {
        // Start foreground service so audio keeps running in background
        val serviceIntent = Intent(context, VoiceForegroundService::class.java)
        context.startForegroundService(serviceIntent)

        // FIX: actually call the backend and connect to LiveKit
        viewModel.joinRoom(roomName, userName)
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Connection status bar
        Surface(
            color = when (connectionState) {
                ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primaryContainer
                ConnectionState.CONNECTING, ConnectionState.RECONNECTING ->
                    MaterialTheme.colorScheme.secondaryContainer
                ConnectionState.FAILED -> MaterialTheme.colorScheme.errorContainer
                ConnectionState.DISCONNECTED -> MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = when (connectionState) {
                    ConnectionState.CONNECTED -> "Connected · $roomName"
                    ConnectionState.CONNECTING -> "Connecting…"
                    ConnectionState.RECONNECTING -> "Reconnecting…"
                    ConnectionState.FAILED -> "Connection failed"
                    ConnectionState.DISCONNECTED -> "Disconnected"
                },
                modifier = Modifier.padding(12.dp)
            )
        }

        // Error banner
        error?.let {
            Surface(color = MaterialTheme.colorScheme.errorContainer) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        }

        // Participant list
        if (participants.isEmpty() && connectionState == ConnectionState.CONNECTED) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Waiting for other riders to join…")
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(participants) { participant ->
                    ParticipantCard(participant = participant)
                }
            }
        }

        VoiceControls(
            muted = muted,
            onMuteToggle = { viewModel.toggleMute() },
            onLeave = {
                viewModel.leaveRoom()
                onLeave()
            }
        )
    }
}
