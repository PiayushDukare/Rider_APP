package com.ridervoice.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ReconnectManager(
    private val reconnectAction: suspend () -> Unit
) {
    private val maxAttempts = 10
    private var attempts    = 0
    private var job: Job?   = null

    fun attemptReconnect() {
        job?.cancel()
        attempts = 0

        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && attempts < maxAttempts) {
                try {
                    reconnectAction()
                    attempts = 0
                    return@launch  // success — stop retrying
                } catch (e: CancellationException) {
                    // BUG FIX: CancellationException must NOT be caught and retried.
                    // Swallowing it prevents coroutine cancellation from propagating,
                    // causing the reconnect loop to run indefinitely even after the
                    // user has left the room.
                    throw e
                } catch (e: Exception) {
                    attempts++
                    val backoffMs = minOf(2_000L * attempts, 30_000L)
                    delay(backoffMs)
                }
            }
            // Exhausted retries — caller should observe connectionState = FAILED
        }
    }

    fun cancel() {
        job?.cancel()
        job = null
        attempts = 0
    }
}
