package com.ridervoice.services

import com.ridervoice.data.local.RideDao
import com.ridervoice.data.local.entities.ConvoyEventEntity
import com.ridervoice.data.local.entities.RawWaypointEntity
import com.ridervoice.data.local.entities.RideSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RideRecorder @Inject constructor(
    private val rideDao: RideDao,
    private val locationService: LocationService
) {
    private var recordingJob: Job? = null
    private var currentSessionId: String? = null
    private var lastLat = 0.0
    private var lastLng = 0.0
    
    // Naive distance function using Pythagoras approximation for small distances
    private fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Earth radius
        val p = Math.PI / 180
        val a = 0.5 - Math.cos((lat2 - lat1) * p) / 2 + 
                Math.cos(lat1 * p) * Math.cos(lat2 * p) * 
                (1 - Math.cos((lon2 - lon1) * p)) / 2
        return 2 * R * Math.asin(Math.sqrt(a))
    }

    fun startRecording(roomName: String) {
        if (recordingJob != null) return
        
        CoroutineScope(Dispatchers.IO).launch {
            val session = RideSessionEntity(
                roomName = roomName,
                startTime = System.currentTimeMillis()
            )
            rideDao.insertSession(session)
            currentSessionId = session.id

            recordingJob = locationService.currentLocation
                .onEach { loc ->
                    if (loc == null || currentSessionId == null) return@onEach
                    
                    val dist = distanceInMeters(lastLat, lastLng, loc.latitude, loc.longitude)
                    
                    // Simple Compression: Only log point if moved > 20 meters or it's the very first point
                    if (lastLat == 0.0 || dist > 20.0) {
                        lastLat = loc.latitude
                        lastLng = loc.longitude
                        
                        rideDao.insertWaypoint(
                            RawWaypointEntity(
                                sessionId = currentSessionId!!,
                                lat = loc.latitude,
                                lng = loc.longitude,
                                speedMps = loc.speed,
                                heading = loc.bearing,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        
                        // Event Detection
                        if (loc.speed > 36.1f) { // ~130 km/h
                            rideDao.insertEvent(
                                ConvoyEventEntity(
                                    sessionId = currentSessionId!!,
                                    eventType = "HIGH_SPEED_ZONE",
                                    lat = loc.latitude,
                                    lng = loc.longitude,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
                .launchIn(CoroutineScope(Dispatchers.IO))
        }
    }

    fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        currentSessionId?.let { sessionId ->
            CoroutineScope(Dispatchers.IO).launch {
                // We'll update the session endTime
                // For a real app, we'd fetch the session first then update it.
                // We can do this in the processing engine.
            }
        }
        currentSessionId = null
    }
}
