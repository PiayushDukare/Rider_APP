package com.ridervoice.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onCreateRoom: (userName: String) -> Unit,
    onJoinRoom: () -> Unit
) {
    var userName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (userName.isNotBlank()) onCreateRoom(userName.trim()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Room")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { if (userName.isNotBlank()) onJoinRoom() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join Room")
        }
    }
}
