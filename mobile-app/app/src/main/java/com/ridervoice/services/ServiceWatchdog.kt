package com.ridervoice.services

import android.util.Log
import com.ridervoice.audio.HardwarePTTManager
import com.ridervoice.network.ConnectionState
import com.ridervoice.network.LiveKitManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceWatchdog @Inject constructor(
    private val liveKitManager: LiveKitManager,
    private val hardwarePTTManager: HardwarePTTManager,
    private val locationService: LocationService
) {
    companion object {
        private const val TAG = "ServiceWatchdog"

        // BUG FIX: DISCONNECTED is also the initial state. We must wait long enough
        // for the connection attempt to either succeed or fail before concluding it's
        // a zombie. 30s covers slow networks and Render cold-start (~25s).
        private const val STARTUP_GRACE_MS = 30_000L
        private const val CHECK_INTERVAL_MS = 10_000L
    }

    private var watchdogJob: Job? = null
    private var startTimeMs   = 0L

    fun startMonitoring(scope: CoroutineScope) {
        if (watchdogJob?.isActive == true) return
        startTimeMs = System.currentTimeMillis()

        watchdogJob = scope.launch(Dispatchers.IO) {
            // Wait for startup grace period before the first check
            delay(STARTUP_GRACE_MS)

            while (isActive) {
                val connectionState = liveKitManager.connectionState.value
                val isTracking      = locationService.isTracking.value

                // ── Zombie 1: Location running but LiveKit definitively dead ──
                // Only trigger after grace period AND only on DISCONNECTED (not RECONNECTING)
                if (isTracking && connectionState == ConnectionState.DISCONNECTED) {
                    val elapsed = System.currentTimeMillis() - startTimeMs
                    if (elapsed > STARTUP_GRACE_MS) {
                        Log.e(TAG, "Zombie detected: location active but LiveKit dead. Cleaning up.")
                        locationService.stopTracking()
                        hardwarePTTManager.deactivateSession()
                    }
                }

                // ── Zombie 2: Orphaned MediaSession holding Bluetooth SCO ──
                if (!isTracking && hardwarePTTManager.isSessionActive()) {
                    Log.e(TAG, "Zombie detected: orphaned MediaSession. Releasing.")
                    hardwarePTTManager.deactivateSession()
                }

                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    fun stopMonitoring() {
        watchdogJob?.cancel()
        watchdogJob = null
    }
}
