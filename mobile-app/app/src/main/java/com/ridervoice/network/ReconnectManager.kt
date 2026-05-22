package com.ridervoice.network

import kotlinx.coroutines.*

class ReconnectManager(
    private val reconnectAction: suspend () -> Unit
) {
    private var reconnectAttempts = 0
    private val maxAttempts = 10
    private var job: Job? = null

    fun attemptReconnect() {
        // Cancel any existing reconnect loop
        job?.cancel()

        job = CoroutineScope(Dispatchers.IO).launch {
            reconnectAttempts = 0

            while (reconnectAttempts < maxAttempts) {
                try {
                    reconnectAction()
                    reconnectAttempts = 0
                    break
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    reconnectAttempts++
                    // Exponential backoff capped at 30s
                    val delayMs = minOf(2000L * reconnectAttempts, 30_000L)
                    delay(delayMs)
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
        reconnectAttempts = 0
    }
}
