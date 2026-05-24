package com.ridervoice.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.ridervoice.utils.OEMBatteryWarning
import com.ridervoice.ui.theme.*
import com.ridervoice.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onHostRideClick: () -> Unit,
    onJoinRideClick: () -> Unit,
    onStartRideClick: (String) -> Unit,
    onSquadClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRoutePlannerClick: () -> Unit,
    onRideHistoryClick: () -> Unit,
    onDeviceSetupClick: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (OEMBatteryWarning.isAggressiveOEM()) {
            val warning = OEMBatteryWarning.getBatteryOptimizationWarningText()
            Toast.makeText(context, warning, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshDeviceState()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(GraphiteBase)
                .padding(24.dp)
        ) {
            // Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HEY, RIDER",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "READY TO RIDE?",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onSettingsClick() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Active Ride Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSlate)
                    .border(1.dp, Gunmetal, RoundedCornerShape(16.dp))
                    .clickable { 
                        if (uiState.hasActiveRide && uiState.activeRideName.isNotBlank()) {
                            onStartRideClick(uiState.activeRideName) 
                        }
                    }
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = if (uiState.hasActiveRide) uiState.activeRideName else "No Active Ride",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = uiState.activeRideSubtitle,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "QUICK ACTIONS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item { QuickActionCard("HOST RIDE", Icons.Default.PlayArrow, NeonOrange) { onHostRideClick() } }
                item { QuickActionCard("JOIN RIDE", Icons.Default.GroupAdd, ElectricCyan) { onJoinRideClick() } }
                item { QuickActionCard("ROUTE PLANNER", Icons.Default.Map, TextSecondary) { onRoutePlannerClick() } }
                item { QuickActionCard("SQUAD", Icons.Default.Group, TextSecondary) { onSquadClick() } }
                item { QuickActionCard("NEARBY RIDERS", Icons.Default.LocationOn, TextSecondary) { } }
                item { QuickActionCard("RIDE HISTORY", Icons.Default.History, TextSecondary) { onRideHistoryClick() } }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "SYSTEM STATUS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SystemStatusCard(
                    title = uiState.deviceName,
                    status = uiState.deviceStatus,
                    color = if (uiState.isDeviceConnected) SuccessGreen 
                            else if (uiState.isDeviceConfigured) NeonOrange 
                            else TextSecondary,
                    icon = Icons.Default.Headset,
                    modifier = Modifier.weight(1f).clickable { onDeviceSetupClick() }
                )
                SystemStatusCard(
                    title = "Network",
                    status = uiState.networkStatus,
                    color = if (uiState.isNetworkStrong) SuccessGreen else NeonOrange,
                    icon = Icons.Default.NetworkCell,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
        }
        
        // Bottom Navigation Bar Overlay
        BottomNavBar(modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSlate)
            .clickable { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun SystemStatusCard(
    title: String,
    status: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSlate)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = status,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun BottomNavBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GraphiteBase)
            .border(1.dp, Gunmetal)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.Home, contentDescription = "Home", tint = NeonOrange, modifier = Modifier.size(28.dp))
        Icon(imageVector = Icons.Default.Group, contentDescription = "Squad", tint = TextSecondary, modifier = Modifier.size(28.dp))
        Icon(imageVector = Icons.Default.Map, contentDescription = "Map", tint = TextSecondary, modifier = Modifier.size(28.dp))
        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = TextSecondary, modifier = Modifier.size(28.dp))
    }
}
