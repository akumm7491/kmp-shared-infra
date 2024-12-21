package com.example.kmp.messaging

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.Properties

class KafkaEventProducer {
    private val producer: KafkaProducer<String, ByteArray> by lazy {
        val props = Properties().apply {
            // Kafka connection
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092")
            
            // Serializers
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer::class.java.name)
            
            // Producer reliability configs
            put(ProducerConfig.ACKS_CONFIG, "all")
            put(ProducerConfig.RETRIES_CONFIG, 3)
            put(ProducerConfig.LINGER_MS_CONFIG, 1)
        }
        KafkaProducer<String, ByteArray>(props)
    }

    suspend fun publish(topic: String, event: Event) {
        withContext(Dispatchers.IO) {
            val record = ProducerRecord(
                topic,
                event.type,
                event.payload.toByteArray(Charsets.UTF_8)
            )
            producer.send(record).get() // Wait for acknowledgment
        }
    }

    suspend fun close() {
        withContext(Dispatchers.IO) {
            producer.flush()
            producer.close()
        }
    }
}
