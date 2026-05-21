package com.ridervoice.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.ridervoice.MainActivity
import com.ridervoice.network.ConnectionState
import com.ridervoice.network.LiveKitManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VoiceForegroundService : Service() {

    @Inject
    lateinit var liveKitManager: LiveKitManager

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val channelId = "voice_channel"
    private val notificationId = 1
    private var wakeLock: PowerManager.WakeLock? = null

    @SuppressLint("WakelockTimeout")
    override fun onCreate() {
        super.onCreate()
        
        // 1. Acquire Partial WakeLock to prevent CPU sleep when screen is off
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RiderVoice::VoiceServiceWakeLock")
        wakeLock?.acquire() // Intentional infinite timeout until service is destroyed

        createNotificationChannel()
        startForeground(notificationId, buildNotification("Initializing..."))

        // Observe LiveKit state to update notification dynamically
        serviceScope.launch {
            liveKitManager.connectionState.collect { state ->
                val statusText = when (state) {
                    ConnectionState.CONNECTED -> "Live Voice Connected"
                    ConnectionState.CONNECTING -> "Connecting to Room..."
                    ConnectionState.RECONNECTING -> "Reconnecting..."
                    ConnectionState.FAILED -> "Connection Failed"
                    ConnectionState.DISCONNECTED -> "Disconnected"
                }
                updateNotification(statusText)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Stop service via intent action if needed
        if (intent?.action == "ACTION_STOP_SERVICE") {
            liveKitManager.disconnect()
            stopSelf()
            return START_NOT_STICKY
        }
        
        // Return START_REDELIVER_INTENT so OS automatically resurrects the service if killed for memory
        return START_REDELIVER_INTENT
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Voice Communication",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps voice connection active in background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val returnIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, returnIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Action to stop service
        val stopIntent = Intent(this, VoiceForegroundService::class.java).apply {
            action = "ACTION_STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, channelId)
            .setContentTitle("Rider Voice Active")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Disconnect", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, buildNotification(contentText))
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        liveKitManager.disconnect()
        
        // Release WakeLock safely
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        
        stopForeground(STOP_FOREGROUND_REMOVE)
    }
}
