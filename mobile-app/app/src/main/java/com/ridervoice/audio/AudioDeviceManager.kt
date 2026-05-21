package com.ridervoice.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class BluetoothState { CONNECTING, CONNECTED, DISCONNECTED }

@Singleton
class AudioDeviceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    private val _bluetoothState = MutableStateFlow(BluetoothState.DISCONNECTED)
    val bluetoothState: StateFlow<BluetoothState> = _bluetoothState

    private var previousVolume = 0

    private val scoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED) {
                when (intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR)) {
                    AudioManager.SCO_AUDIO_STATE_CONNECTED -> {
                        _bluetoothState.value = BluetoothState.CONNECTED
                        Log.d("AudioDeviceManager", "Bluetooth SCO Connected")
                    }
                    AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> {
                        if (_bluetoothState.value == BluetoothState.CONNECTED) {
                            handleBluetoothDisconnect()
                        }
                        _bluetoothState.value = BluetoothState.DISCONNECTED
                        Log.d("AudioDeviceManager", "Bluetooth SCO Disconnected")
                    }
                    AudioManager.SCO_AUDIO_STATE_CONNECTING -> {
                        _bluetoothState.value = BluetoothState.CONNECTING
                        Log.d("AudioDeviceManager", "Bluetooth SCO Connecting...")
                    }
                }
            }
        }
    }

    fun enableBluetoothAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Modern API (Android 12+)
            val devices = audioManager.availableCommunicationDevices
            val bluetoothDevice = devices.firstOrNull { 
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || 
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP 
            }
            
            if (bluetoothDevice != null) {
                audioManager.setCommunicationDevice(bluetoothDevice)
                _bluetoothState.value = BluetoothState.CONNECTED
                Log.d("AudioDeviceManager", "Routed via setCommunicationDevice to ${bluetoothDevice.productName}")
            } else {
                Log.e("AudioDeviceManager", "No Bluetooth device found for modern routing")
            }
        } else {
            // Legacy fallback
            if (!audioManager.isBluetoothScoAvailableOffCall) {
                Log.e("AudioDeviceManager", "Bluetooth SCO not available off call.")
                return
            }

            val filter = IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            context.registerReceiver(scoReceiver, filter)

            Log.d("AudioDeviceManager", "Starting Bluetooth SCO...")
            audioManager.startBluetoothSco()
            audioManager.isBluetoothScoOn = true
        }
    }

    fun disableBluetoothAudio() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.clearCommunicationDevice()
        } else {
            audioManager.stopBluetoothSco()
            audioManager.isBluetoothScoOn = false
            try {
                context.unregisterReceiver(scoReceiver)
            } catch (e: Exception) {
                // Ignore if not registered
            }
        }
        _bluetoothState.value = BluetoothState.DISCONNECTED
    }

    private fun handleBluetoothDisconnect() {
        Log.w("AudioDeviceManager", "DANGER: Bluetooth Disconnected unexpectedly mid-ride!")
        // Immediately prevent speaker blast by zeroing call volume
        previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0)
        audioManager.isSpeakerphoneOn = false
    }

    fun switchToSpeaker() {
        // Only called explicitly if user hits "Switch to Phone Speaker"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val speaker = audioManager.availableCommunicationDevices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
            if (speaker != null) audioManager.setCommunicationDevice(speaker)
        }
        audioManager.isSpeakerphoneOn = true
        if (previousVolume > 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, previousVolume, 0)
        }
    }
}
