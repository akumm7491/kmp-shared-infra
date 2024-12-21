package com.example.kmp.messaging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual class EventConsumer actual constructor() {
    private val kafkaConsumer = KafkaEventConsumer(CoroutineScope(Dispatchers.IO))

    actual suspend fun consume(topic: String, processor: EventProcessor) {
        kafkaConsumer.consume(topic, processor)
    }

    actual suspend fun stop(topic: String) {
        kafkaConsumer.stop(topic)
    }

    actual suspend fun stopAll() {
        kafkaConsumer.stopAll()
    }

    actual companion object {
        actual fun create(): EventConsumer = EventConsumer()
    }
}
