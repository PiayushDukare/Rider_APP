package com.ridervoice.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridervoice.network.ConnectionState
import com.ridervoice.services.VoiceForegroundService
import com.ridervoice.state.RoomViewModel
import com.ridervoice.ui.components.ParticipantCard
import com.ridervoice.ui.theme.*

import com.mapbox.geojson.Point
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.style.MapStyle

import com.ridervoice.network.NetworkHealth

@Composable
fun RoomScreen(
    roomName: String,
    userName: String,
    onLeave: () -> Unit,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val connectionState by viewModel.connectionState.collectAsState()
    val participants by viewModel.participants.collectAsState()
    val muted by viewModel.muted.collectAsState()
    val remoteLocations by viewModel.remoteLocations.collectAsState()
    val networkHealth by viewModel.networkHealth.collectAsState()

    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(12.0)
            pitch(45.0)
        }
    }

    LaunchedEffect(roomName, userName) {
        val serviceIntent = Intent(context, VoiceForegroundService::class.java)
        context.startForegroundService(serviceIntent)
        viewModel.joinRoom(roomName, userName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteBase)
    ) {
        // Top Tactical Map Area (40%)
        Box(modifier = Modifier.weight(0.4f).fillMaxWidth()) {
            // Check Location Permission contextually
            val hasLocation = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            
            if (hasLocation) {
                MapboxMap(
                    modifier = Modifier.fillMaxSize(),
                    mapViewportState = mapViewportState,
                    style = { MapStyle(style = Style.DARK) }
                ) {
                    // Draw Remote Locations
                    remoteLocations.forEach { (userId, location) ->
                        PointAnnotation(
                            point = Point.fromLngLat(location.longitude, location.latitude)
                        )
                    }
                }
            } else {
                // Degraded Map State
                Box(
                    modifier = Modifier.fillMaxSize().background(DarkSlate),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CONVOY MAP DISABLED", color = AlertRed, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("Location Permission Required", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { /* Prompt Contextual Permission */ }, colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)) {
                            Text("GRANT LOCATION", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            // Weak Signal / Degraded Overlay
            if (networkHealth != NetworkHealth.CONNECTED) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0xBBFF3B30)) // Alert Red with alpha
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (networkHealth == NetworkHealth.RECONNECTING) "WEAK SIGNAL - ATTEMPTING RECONNECT" else "DEGRADED CONNECTION",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Bottom Voice Channel Area (60%)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(DarkSlate, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "VOICE CHANNEL",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = roomName.uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${participants.size} Online",
                        color = SuccessGreen,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ALL", color = NeonOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("NEARBY", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("LEADER", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Divider(color = NeonOrange, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp).width(40.dp), thickness = 2.dp)

            // Participants List
            val now = System.currentTimeMillis()
            val activeOrRecentGhosts = participants.filter { 
                !it.isGhost || (it.disconnectedAt != null && (now - it.disconnectedAt) < 300000) 
            }
            val deeplyDisconnected = participants.filter { 
                it.isGhost && (it.disconnectedAt != null && (now - it.disconnectedAt) >= 300000) 
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(activeOrRecentGhosts) { participant ->
                    ParticipantCard(participant = participant)
                }
                
                if (deeplyDisconnected.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "DISCONNECTED RIDERS",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(deeplyDisconnected) { participant ->
                        ParticipantCard(participant = participant, isFaded = true) // Will update ParticipantCard separately
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Tactical Debug Overlay for AVRCP Testing
            com.ridervoice.ui.components.TacticalDebugOverlay(
                hardwarePTTManager = viewModel.hardwarePTTManager
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Massive Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Button (Circle)
                Button(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (muted) AlertRed else Gunmetal
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute Toggle",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                // PTT Button (Giant Pill)
                Button(
                    onClick = { /* Handle PTT Logic */ },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                ) {
                    Text(
                        text = "PUSH TO TALK",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
