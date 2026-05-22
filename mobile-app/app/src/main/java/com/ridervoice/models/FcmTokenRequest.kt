package com.ridervoice.models

data class FcmTokenRequest(
    val userId: String,
    val token: String,
    val platform: String = "android"
)
