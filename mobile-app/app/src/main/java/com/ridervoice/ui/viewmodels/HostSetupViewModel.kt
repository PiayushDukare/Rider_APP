package com.ridervoice.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HostSetupViewModel @Inject constructor() : ViewModel() {
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
        // Stub implementation
        _createdConvoyName.value = convoyName
    }
}
