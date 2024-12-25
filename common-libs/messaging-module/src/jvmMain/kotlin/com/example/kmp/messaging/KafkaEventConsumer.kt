package com.example.kmp.messaging

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

class KafkaEventConsumer(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val consumers = ConcurrentHashMap<String, Pair<KafkaConsumer<String, ByteArray>, Job>>()
    
    suspend fun consume(topic: String, processor: EventProcessor) {
        // Stop existing consumer for this topic if it exists
        stop(topic)
        
        val consumer = createConsumer("group-$topic")
        val job = scope.launch {
            try {
                consumer.subscribe(listOf(topic))
                while (isActive) {
                    consumer.poll(Duration.ofMillis(100))?.forEach { record ->
                        launch {
                            try {
                                val event = Json { ignoreUnknownKeys = true }.decodeFromString<Event>(String(record.value()))
                                processor.process(event)
                            } catch (e: Exception) {
                                println("Error processing event: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Consumer error: ${e.message}")
            }
        }
        
        consumers[topic] = consumer to job
    }

    suspend fun stop(topic: String) {
        consumers.remove(topic)?.let { (consumer, job) ->
            job.cancelAndJoin()
            withContext(Dispatchers.IO) {
                consumer.close()
            }
        }
    }

    suspend fun stopAll() {
        consumers.keys.forEach { topic ->
            stop(topic)
        }
    }
    
    private fun createConsumer(groupId: String): KafkaConsumer<String, ByteArray> {
        val props = Properties().apply {
            // Kafka connection
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092")
            
            // Consumer group
            put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
            
            // Deserializers
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer::class.java.name)
            
            // Consumer configs
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
            put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000")
        }
        return KafkaConsumer(props)
    }
}
