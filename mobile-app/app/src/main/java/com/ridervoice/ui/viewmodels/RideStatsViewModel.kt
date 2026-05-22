package com.ridervoice.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RideStatsState(
    val totalDistance: String = "0.0",
    val totalTime: String = "00:00:00",
    val avgSpeed: String = "0",
    val topSpeed: String = "0",
    // This represents speed points over time for the graph
    val speedDataPoints: List<Float> = emptyList()
)

@HiltViewModel
class RideStatsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RideStatsState())
    val uiState: StateFlow<RideStatsState> = _uiState.asStateFlow()

    init {
        // Load some dummy data to simulate a fetch from Room/Firebase
        loadMockHistory()
    }

    private fun loadMockHistory() {
        _uiState.value = _uiState.value.copy(
            totalDistance = "124.7",
            totalTime = "02:35:18",
            avgSpeed = "68",
            topSpeed = "142",
            speedDataPoints = listOf(10f, 60f, 90f, 85f, 120f, 110f, 150f)
        )
    }
}
