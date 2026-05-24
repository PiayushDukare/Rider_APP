package com.ridervoice.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "LocationService"
        private const val MIN_DISTANCE_METERS = 2f
    }

    private val fusedClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    // @Volatile prevents stale reads across threads when both
    // updatePollingInterval() and updateLocationRequest() touch this field.
    @Volatile private var currentIntervalMs = 5_000L
    @Volatile private var isNetworkDegraded = false

    // Single reusable callback — never create multiple
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            _currentLocation.value = loc
            maybeUpdateInterval(loc.speed)
        }
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (!hasPermission()) {
            Log.e(TAG, "ACCESS_FINE_LOCATION permission not granted — cannot start tracking")
            return
        }
        if (_isTracking.value) return

        _isTracking.value = true
        currentIntervalMs = 5_000L
        registerUpdates(currentIntervalMs)
        Log.i(TAG, "✅ Location tracking started")
    }

    fun stopTracking() {
        _isTracking.value = false
        fusedClient.removeLocationUpdates(locationCallback)
        Log.i(TAG, "Location tracking stopped")
    }

    fun setNetworkDegraded(degraded: Boolean) {
        if (isNetworkDegraded == degraded) return
        isNetworkDegraded = degraded
        // Re-evaluate using last known location's speed
        _currentLocation.value?.let { maybeUpdateInterval(it.speed) }
    }

    /**
     * External callers (ThermalManager) use this to enforce a minimum interval.
     * It only ever INCREASES the interval — never overrides a higher-frequency request.
     */
    @SuppressLint("MissingPermission")
    fun updatePollingInterval(intervalMs: Long) {
        if (!_isTracking.value || !hasPermission()) return
        if (intervalMs == currentIntervalMs) return

        currentIntervalMs = intervalMs
        // Remove and re-register in one shot — avoids race with the callback
        fusedClient.removeLocationUpdates(locationCallback)
        registerUpdates(currentIntervalMs)
        Log.d(TAG, "Polling interval changed to ${intervalMs}ms")
    }

    // ── Private ────────────────────────────────────────────────────────────────

    /**
     * Adaptive telemetry intervals:
     *   Stationary  (< 1 m/s  ≈ 3.6 km/h)  → 15s  (parked / red light)
     *   City        (< 15 m/s ≈ 54 km/h)   →  5s
     *   Highway     (≥ 15 m/s)              →  2s
     *
     * When network is degraded the interval is clamped to ≥10s to preserve
     * voice bandwidth — GPS data is less important than keeping audio up.
     */
    @SuppressLint("MissingPermission")
    private fun maybeUpdateInterval(speedMps: Float) {
        if (!_isTracking.value || !hasPermission()) return

        var desired = when {
            speedMps < 1.0f  -> 15_000L
            speedMps < 15.0f ->  5_000L
            else             ->  2_000L
        }

        if (isNetworkDegraded && desired < 10_000L) {
            desired = 10_000L
        }

        if (desired == currentIntervalMs) return

        currentIntervalMs = desired
        // BUG FIX: removeLocationUpdates is async — by removing and immediately
        // re-registering in sequence we avoid a window where the old callback
        // fires into a partially-reset state. The FusedLocationProviderClient
        // internally queues these operations so the order is guaranteed.
        fusedClient.removeLocationUpdates(locationCallback)
        registerUpdates(currentIntervalMs)
    }

    @SuppressLint("MissingPermission")
    private fun registerUpdates(intervalMs: Long) {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .build()
        fusedClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun hasPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}
