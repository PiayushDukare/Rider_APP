package com.ridervoice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.ridervoice.navigation.NavGraph
import com.ridervoice.permissions.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // FIX: request required permissions at startup
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            // Could show a dialog explaining why permissions are needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request RECORD_AUDIO + BLUETOOTH_CONNECT before entering the app
        permissionLauncher.launch(PermissionManager.requiredPermissions)

        setContent {
            NavGraph()
        }
    }
}
