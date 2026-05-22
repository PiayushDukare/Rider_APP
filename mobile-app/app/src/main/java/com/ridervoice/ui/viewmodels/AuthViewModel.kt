package com.ridervoice.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridervoice.security.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _otpSent = MutableStateFlow(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    private var _verificationId: String? = null

    fun signInAnonymously() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val success = authRepository.signInAnonymously()
            if (success) {
                _loginSuccess.value = true
            } else {
                _errorMessage.value = "Guest sign-in failed. Check your network or Firebase setup."
            }
            
            _isLoading.value = false
        }
    }
    
    fun signOut() {
        authRepository.signOut()
        _loginSuccess.value = false
        _otpSent.value = false
        _verificationId = null
    }

    fun handleGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            val success = authRepository.signInWithGoogle(idToken)
            if (success) {
                _loginSuccess.value = true
            } else {
                _errorMessage.value = "Google sign-in failed."
            }
            _isLoading.value = false
        }
    }

    fun onOtpSent(verificationId: String) {
        _verificationId = verificationId
        _otpSent.value = true
        _isLoading.value = false
    }

    fun handlePhoneCredential(credential: com.google.firebase.auth.PhoneAuthCredential) {
        viewModelScope.launch {
            _isLoading.value = true
            val success = authRepository.signInWithPhoneAuthCredential(credential)
            if (success) {
                _loginSuccess.value = true
            } else {
                _errorMessage.value = "Invalid OTP or sign-in failed."
                _isLoading.value = false
            }
        }
    }

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }
    
    fun getVerificationId(): String? = _verificationId
}
