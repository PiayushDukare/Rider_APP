package com.ridervoice.models

data class Participant(
    val identity: String,
    val isGhost: Boolean = false,
    val disconnectedAt: Long? = null
)
