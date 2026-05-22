package com.ridervoice.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFocusManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    private val focusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> Log.d("AudioFocusManager", "Audio Focus Gained")
            AudioManager.AUDIOFOCUS_LOSS -> Log.d("AudioFocusManager", "Audio Focus Lost (Permanent)")
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> Log.d("AudioFocusManager", "Audio Focus Lost (Transient)")
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Log.d("AudioFocusManager", "Audio Focus Lost (Ducking)")
        }
    }

    fun requestFocus() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(audioAttributes)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(focusChangeListener)
            .build()

        focusRequest?.let {
            val result = audioManager.requestAudioFocus(it)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.d("AudioFocusManager", "Audio Focus Granted")
            } else {
                Log.d("AudioFocusManager", "Audio Focus Denied")
            }
        }
    }

    fun abandonFocus() {
        focusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
            focusRequest = null
            Log.d("AudioFocusManager", "Audio Focus Abandoned")
        }
    }
}
