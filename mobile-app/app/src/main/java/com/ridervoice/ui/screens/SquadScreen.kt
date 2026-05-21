package com.ridervoice.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridervoice.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SquadScreen(
    onBackClick: () -> Unit
) {
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
                    text = "YOUR SQUAD",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "MUTUAL TRUSTED RIDERS",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
            
            // Add Friend / QR Code Button
            IconButton(
                onClick = { /* Open Add Friend Modal */ },
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSlate)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Rider",
                    tint = NeonOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Bar for Handles
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by @handle", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = NeonOrange,
                unfocusedBorderColor = Gunmetal,
                containerColor = DarkSlate
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Incoming Ride Invites Section
        Text(
            text = "INCOMING INVITES (1)",
            color = NeonOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Placeholder for an Invite
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSlate)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "PUNE NIGHT RIDERS", color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = "Invited by @GhostRider", color = TextSecondary, fontSize = 12.sp)
            }
            Button(
                onClick = { /* Join Room */ },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("JOIN", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Active Squad List
        Text(
            text = "ONLINE FRIENDS",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SquadMemberCard(handle = "@TorqueLead", bike = "Ducati V4", status = "Online")
            }
            item {
                SquadMemberCard(handle = "@ApexWolf", bike = "Yamaha MT-09", status = "In a Ride")
            }
        }
    }
}

@Composable
fun SquadMemberCard(handle: String, bike: String, status: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSlate)
            .clickable { /* View Profile */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Gunmetal),
            contentAlignment = Alignment.Center
        ) {
            Text(handle.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(text = handle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = bike, color = TextSecondary, fontSize = 12.sp)
        }
        
        Text(
            text = status,
            color = if (status == "Online") SuccessGreen else NeonOrange,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
