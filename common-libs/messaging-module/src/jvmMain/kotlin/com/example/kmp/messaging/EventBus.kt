package com.example.kmp.messaging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

actual class EventBus {
    private val producer by lazy {
        val props = Properties().apply {
            put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092")
            put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        }
        KafkaProducer<String, String>(props)
    }

    actual suspend fun publish(topic: String, event: Event) {
        withContext(Dispatchers.IO) {
            val record = ProducerRecord(topic, event.type, event.payload)
            producer.send(record)
        }
    }

    actual suspend fun subscribe(topic: String, handler: suspend (Event) -> Unit) {
        // Stub implementation - in real code, would set up a Kafka consumer
        println("Subscribed to topic: $topic")
    }
}
