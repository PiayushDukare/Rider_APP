package com.ridervoice.services

import android.util.Log
import com.ridervoice.audio.HardwarePTTManager
import com.ridervoice.network.LiveKitManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceWatchdog @Inject constructor(
    private val liveKitManager: LiveKitManager,
    private val hardwarePTTManager: HardwarePTTManager,
    private val locationService: LocationService
) {
    private var watchdogJob: Job? = null
    
    fun startMonitoring(scope: CoroutineScope) {
        if (watchdogJob != null) return
        
        watchdogJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(10000) // Check every 10 seconds
                
                val connectionState = liveKitManager.connectionState.value
                val isServiceRunning = locationService.isTracking.value // We use LocationService as proxy for foreground
                
                // Zombie Condition 1: Foreground is running, but LiveKit is definitively disconnected (not reconnecting)
                if (isServiceRunning && connectionState == com.ridervoice.network.ConnectionState.DISCONNECTED) {
                    Log.e("ServiceWatchdog", "Zombie State Detected: LiveKit dead but Foreground active. Self-Healing...")
                    locationService.stopTracking()
                    hardwarePTTManager.deactivateSession()
                }
                
                // Zombie Condition 2: Bluetooth MediaSession is active, but we aren't tracking a ride
                if (!isServiceRunning && hardwarePTTManager.isSessionActive()) {
                    Log.e("ServiceWatchdog", "Zombie State Detected: Orphaned MediaSession holding SCO. Releasing...")
                    hardwarePTTManager.deactivateSession()
                }
            }
        }
    }
    
    fun stopMonitoring() {
        watchdogJob?.cancel()
        watchdogJob = null
    }
}
