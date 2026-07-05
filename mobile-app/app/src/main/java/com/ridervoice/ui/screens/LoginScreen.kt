package com.ridervoice.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.ridervoice.R
import com.ridervoice.ui.components.TacticalButton
import com.ridervoice.ui.theme.*
import com.ridervoice.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onGoogleSignInClick: () -> Unit = {},
    onPhoneOtpClick: () -> Unit = {},
    onLoginSuccess: () -> Unit
) {
    val isLoading = viewModel.isLoading.collectAsState().value
    val loginSuccess = viewModel.loginSuccess.collectAsState().value
    val errorMessage = viewModel.errorMessage.collectAsState().value
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPhoneInput by remember { mutableStateOf(false) }
    var showOtpInput by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("+1") }
    var otpCode by remember { mutableStateOf("") }

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    fun launchGoogleSignIn() {
        coroutineScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()
                    
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                    
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.handleGoogleIdToken(googleIdTokenCredential.idToken)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitPhoneNumber() {
        viewModel.setLoading(true)
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    viewModel.handlePhoneCredential(credential)
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    viewModel.setLoading(false)
                    coroutineScope.launch { snackbarHostState.showSnackbar(e.localizedMessage ?: "Verification Failed") }
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    viewModel.onOtpSent(verificationId)
                    showPhoneInput = false
                    showOtpInput = true
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun submitOtpCode() {
        val verificationId = viewModel.getVerificationId()
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            viewModel.handlePhoneCredential(credential)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GraphiteBase)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "RIDER LINK",
            color = Color.White,
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = "STAY CONNECTED. RIDE UNITED.",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "WELCOME RIDER",
            color = NeonOrange,
            style = MaterialTheme.typography.titleLarge,
            letterSpacing = 1.sp
        )
        Text(
            text = "Let's get you connected.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        if (showPhoneInput) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TacticalButton(
                text = if (isLoading) "Sending..." else "Send Code",
                onClick = { submitPhoneNumber() },
                color = NeonOrange,
                textColor = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { showPhoneInput = false }) {
                Text("Cancel", color = TextSecondary)
            }
        } else if (showOtpInput) {
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("6-Digit Code", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            TacticalButton(
                text = if (isLoading) "Verifying..." else "Verify Code",
                onClick = { submitOtpCode() },
                color = ElectricCyan,
                textColor = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { 
                showOtpInput = false
                showPhoneInput = true
            }) {
                Text("Change Number", color = TextSecondary)
            }
        } else {
            // Default Action Buttons
            TacticalButton(
                text = "G  Continue with Google",
                onClick = { launchGoogleSignIn() },
                isOutlined = true,
                color = DarkSlate,
                textColor = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TacticalButton(
                text = "📞  Continue with Phone",
                onClick = { showPhoneInput = true },
                isOutlined = true,
                color = DarkSlate,
                textColor = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TacticalButton(
                text = if (isLoading) "Connecting..." else "Continue as Guest",
                onClick = { if (!isLoading) viewModel.signInAnonymously() },
                isOutlined = true,
                color = Color(0xFF1D232B),
                textColor = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "By continuing, you agree to our\nTerms of Service and Privacy Policy",
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            style = MaterialTheme.typography.bodyLarge
        )
        
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}
