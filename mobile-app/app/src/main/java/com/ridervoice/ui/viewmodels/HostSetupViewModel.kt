package com.ridervoice.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridervoice.models.ConvoyCreateRequest
import com.ridervoice.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostSetupViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _createdConvoyName = MutableStateFlow<String?>(null)
    val createdConvoyName = _createdConvoyName.asStateFlow()

    fun createConvoy(
        convoyName: String,
        origin: String?,
        destination: String?,
        meetupPoint: String?,
        estimatedDurationMin: Int?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val req = ConvoyCreateRequest(
                    convoyName, origin, destination, estimatedDurationMin, meetupPoint
                )
                val response = apiService.createConvoy(req)
                if (response.isSuccessful && response.body() != null) {
                    // BUG FIX: Use convoyName (the human-readable name), NOT roomId (UUID).
                    // All downstream APIs (lobby status, start ride, invite friends) look up
                    // rooms by Room.name, not Room.id. Passing a UUID causes 404 everywhere.
                    _createdConvoyName.value = response.body()!!.convoyName
                } else {
                    _error.value = response.errorBody()?.string() ?: "Failed to create convoy"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Network error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
