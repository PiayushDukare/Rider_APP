package com.ridervoice.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridervoice.data.local.RideDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Locale

data class RideStatsState(
    val totalDistance: String = "0.0",
    val totalTime: String = "00:00:00",
    val avgSpeed: String = "0",
    val topSpeed: String = "0",
    val speedDataPoints: List<Float> = emptyList()
)

@HiltViewModel
class RideStatsViewModel @Inject constructor(
    private val rideDao: RideDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideStatsState())
    val uiState: StateFlow<RideStatsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            rideDao.getAllSessions().collectLatest { sessions ->
                var totalDistMeters = 0f
                var totalDurationSec = 0L

                for (session in sessions) {
                    totalDistMeters += session.totalDistanceMeters
                    val end = session.endTime ?: System.currentTimeMillis()
                    totalDurationSec += (end - session.startTime) / 1000
                }

                val totalKm = totalDistMeters / 1000f
                val avgSpeedKmh = if (totalDurationSec > 0) (totalKm / (totalDurationSec / 3600f)) else 0f
                
                val hours = totalDurationSec / 3600
                val mins = (totalDurationSec % 3600) / 60
                val secs = totalDurationSec % 60
                val timeStr = String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)

                _uiState.value = _uiState.value.copy(
                    totalDistance = String.format(Locale.US, "%.1f", totalKm),
                    totalTime = timeStr,
                    avgSpeed = String.format(Locale.US, "%.0f", avgSpeedKmh),
                    topSpeed = "0", // Top speed would require waypoint analysis
                    speedDataPoints = emptyList() // Needs full waypoint query to plot
                )
            }
        }
    }
}
