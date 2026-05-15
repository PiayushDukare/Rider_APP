package com.ridervoice.ui.components
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ridervoice.models.Participant
@Composable
fun ParticipantCard(participant: Participant) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (participant.isSpeaking) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(participant.identity, style = MaterialTheme.typography.titleMedium)
            if (participant.isSpeaking) Text("🎙 Speaking")
            if (participant.isMuted) Text("🔇 Muted")
        }
    }
}
