package com.ridervoice.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RoutePlannerState(
    val origin: String = "",
    val destination: String = "",
    val routeName: String = "NO ROUTE",
    val distanceKm: String = "0",
    val duration: String = "00:00",
    val elevationGain: String = "0"
)

@HiltViewModel
class RoutePlannerViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RoutePlannerState())
    val uiState: StateFlow<RoutePlannerState> = _uiState.asStateFlow()

    fun updateOrigin(origin: String) {
        _uiState.value = _uiState.value.copy(origin = origin)
        recalculateRoute()
    }

    fun updateDestination(destination: String) {
        _uiState.value = _uiState.value.copy(destination = destination)
        recalculateRoute()
    }

    private fun recalculateRoute() {
        val state = _uiState.value
        if (state.origin.isNotBlank() && state.destination.isNotBlank()) {
            // Simulate route calculation based on user input
            _uiState.value = state.copy(
                routeName = "${state.destination.uppercase()} LOOP",
                distanceKm = "123",
                duration = "02:40",
                elevationGain = "1420"
            )
        } else {
            _uiState.value = state.copy(
                routeName = "NO ROUTE",
                distanceKm = "0",
                duration = "00:00",
                elevationGain = "0"
            )
        }
    }
}
