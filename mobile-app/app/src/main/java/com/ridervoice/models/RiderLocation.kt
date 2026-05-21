package com.ridervoice.models

import com.google.gson.annotations.SerializedName

data class RiderLocation(
    @SerializedName("riderId") val riderId: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("speed") val speed: Float,
    @SerializedName("heading") val heading: Float,
    @SerializedName("timestamp") val timestamp: Long
)
