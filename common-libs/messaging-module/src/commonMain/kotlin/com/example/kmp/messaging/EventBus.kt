package com.example.kmp.messaging

expect class EventBus {
    suspend fun publish(topic: String, event: Event)
    suspend fun subscribe(topic: String, handler: suspend (Event) -> Unit)
}
