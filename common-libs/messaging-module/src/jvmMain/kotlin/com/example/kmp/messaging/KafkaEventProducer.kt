package com.example.kmp.messaging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

class KafkaEventProducer : EventProducer {
    private val producer by lazy {
        val props = Properties().apply {
            put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092")
            put("key.serializer", StringSerializer::class.java.name)
            put("value.serializer", StringSerializer::class.java.name)
            // Add additional producer configs for reliability
            put("acks", "all")
            put("retries", 3)
            put("linger.ms", 1)
        }
        KafkaProducer<String, String>(props)
    }

    override suspend fun publish(topic: String, event: Event) {
        withContext(Dispatchers.IO) {
            val record = ProducerRecord(topic, event.type, event.payload)
            producer.send(record).get() // Wait for acknowledgment
        }
    }

    override suspend fun close() {
        withContext(Dispatchers.IO) {
            producer.flush()
            producer.close()
        }
    }
}
