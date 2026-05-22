package com.ridervoice.services

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ThermalState {
    NORMAL,
    LEVEL_1_WARM,
    LEVEL_2_HOT,
    LEVEL_3_CRITICAL
}

@Singleton
class ThermalManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationService: LocationService
) {
    private val _thermalState = MutableStateFlow(ThermalState.NORMAL)
    val thermalState: StateFlow<ThermalState> = _thermalState.asStateFlow()

    private var monitorJob: Job? = null

    fun startMonitoring(scope: CoroutineScope) {
        if (monitorJob != null) return

        monitorJob = scope.launch(Dispatchers.IO) {
            while (isActive) {
                val intent = androidx.core.content.ContextCompat.registerReceiver(
                    context, 
                    null, 
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED), 
                    androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
                )
                val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
                val temperatureCelsius = temp / 10.0f

                val newState = when {
                    temperatureCelsius >= 45.0f -> ThermalState.LEVEL_3_CRITICAL
                    temperatureCelsius >= 41.0f -> ThermalState.LEVEL_2_HOT
                    temperatureCelsius >= 38.0f -> ThermalState.LEVEL_1_WARM
                    else -> ThermalState.NORMAL
                }

                if (_thermalState.value != newState) {
                    _thermalState.value = newState
                    applyThermalDegradation(newState)
                }

                delay(15000) // Check every 15 seconds
            }
        }
    }

    private fun applyThermalDegradation(state: ThermalState) {
        Log.w("ThermalManager", "Thermal State Transitioned to: $state")
        when (state) {
            ThermalState.NORMAL -> {
                locationService.updatePollingInterval(2000L) // 2s
            }
            ThermalState.LEVEL_1_WARM -> {
                locationService.updatePollingInterval(5000L) // 5s
            }
            ThermalState.LEVEL_2_HOT -> {
                locationService.updatePollingInterval(10000L) // 10s
                // Disable cosmetics signal handled by UI observing thermalState
            }
            ThermalState.LEVEL_3_CRITICAL -> {
                locationService.updatePollingInterval(15000L) // 15s
                // Cinematic replay paused, Tactical Mode enforced by UI
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }
}
