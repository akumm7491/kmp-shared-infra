package com.example.kmp.messaging

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis()
)
