package com.example.kmp.messaging

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val type: String,
    val payload: String,
    val timestamp: Long
)

expect class EventBus {
    suspend fun publish(topic: String, event: Event)
    suspend fun subscribe(topic: String, handler: suspend (Event) -> Unit)
}
