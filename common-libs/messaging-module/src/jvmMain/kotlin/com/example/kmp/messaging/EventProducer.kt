package com.example.kmp.messaging

actual class EventProducer actual constructor() {
    private val kafkaProducer = KafkaEventProducer()

    actual suspend fun publish(topic: String, event: Event) {
        kafkaProducer.publish(topic, event)
    }

    actual suspend fun close() {
        kafkaProducer.close()
    }

    actual companion object {
        actual fun create(): EventProducer = EventProducer()
    }
}
