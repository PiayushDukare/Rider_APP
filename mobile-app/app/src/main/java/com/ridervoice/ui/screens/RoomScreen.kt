package com.ridervoice.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridervoice.services.VoiceForegroundService
import com.ridervoice.state.RoomViewModel
import com.ridervoice.ui.components.ParticipantCard
import com.ridervoice.ui.theme.*

@Composable
fun RoomScreen(
    roomName: String,
    userName: String,
    onLeave: () -> Unit,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val participants by viewModel.participants.collectAsState()
    val muted by viewModel.muted.collectAsState()

    var isVoiceChannelExpanded by remember { mutableStateOf(false) }

    // Fix: Move checkSelfPermission to remember block
    val hasLocation by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(roomName, userName) {
        val serviceIntent = Intent(context, VoiceForegroundService::class.java)
        context.startForegroundService(serviceIntent)
        viewModel.joinRoom(roomName, userName)
    }

    Box(modifier = Modifier.fillMaxSize().background(GraphiteBase)) {
        // Tactical Architecture: Background no longer uses in-app map.
        // Instead, we encourage Navigation Delegation to external apps.
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.Navigation, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("TACTICAL CONVOY ACTIVE", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text("Voice and telemetry are running in background.", color = TextSecondary)
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    // Navigation Delegation Intent (example opens maps)
                    val gmmIntentUri = Uri.parse("geo:0,0?q=")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("LAUNCH NAVIGATION", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        if (!hasLocation) {
            Box(modifier = Modifier.fillMaxSize().background(Color(0x99000000)), contentAlignment = Alignment.Center) {
                Text("LOCATION PERMISSION DENIED", color = AlertRed, style = MaterialTheme.typography.titleLarge)
            }
        }

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Live Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(AlertRed))
                Spacer(modifier = Modifier.width(8.dp))
                Text("LIVE", color = AlertRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            
            // Room Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(roomName, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Text("${participants.size} Connected", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
            }

            // Battery
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BatteryChargingFull, contentDescription = "Battery", tint = SuccessGreen)
                Spacer(modifier = Modifier.width(4.dp))
                Text("90%", color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }

        // Bottom Voice Channel Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(if (isVoiceChannelExpanded) 0.85f else 0.3f)
                .background(DarkSlate, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .padding(24.dp)
        ) {
            // Drag Handle / Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isVoiceChannelExpanded = !isVoiceChannelExpanded },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(TextSecondary))
            }

            if (isVoiceChannelExpanded) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = TextSecondary)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("VOICE CHANNEL", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                        Text(roomName, color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text("${participants.size} Online", color = SuccessGreen, style = MaterialTheme.typography.labelLarge)
                    }
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextSecondary)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ALL", color = NeonOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("NEARBY", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("LEADER", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Divider(color = NeonOrange, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp).width(40.dp), thickness = 2.dp)

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(participants) { participant ->
                        ParticipantCard(participant = participant)
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                // Collapsed View - Show Active Speaker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Gunmetal), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Rahul", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Speaking", color = ElectricCyan, fontSize = 12.sp)
                    }
                    // Fake waveform
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth().height(72.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { viewModel.toggleMute() }) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(if (muted) AlertRed else Gunmetal), contentAlignment = Alignment.Center) {
                        Icon(if (muted) Icons.Default.MicOff else Icons.Default.MicOff, contentDescription = "Mute", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("MUTE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // Giant PTT
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp).fillMaxHeight(),
                    shape = RoundedCornerShape(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PTT", color = Color.White, style = MaterialTheme.typography.titleLarge)
                }

                // SOS
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onLeave() }) {
                    Box(modifier = Modifier.size(56.dp).clip(CircleShape).border(2.dp, AlertRed, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Warning, contentDescription = "SOS", tint = AlertRed)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("LEAVE", color = AlertRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
