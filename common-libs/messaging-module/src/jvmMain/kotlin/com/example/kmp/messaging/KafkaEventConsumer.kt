package com.example.kmp.messaging

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.Properties
import java.util.concurrent.ConcurrentHashMap

class KafkaEventConsumer : EventConsumer {
    private val consumerJobs = ConcurrentHashMap<String, Job>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private fun createConsumer(groupId: String): KafkaConsumer<String, String> {
        val props = Properties().apply {
            put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS") ?: "localhost:9092")
            put("group.id", groupId)
            put("key.deserializer", StringDeserializer::class.java.name)
            put("value.deserializer", StringDeserializer::class.java.name)
            put("auto.offset.reset", "earliest")
            put("enable.auto.commit", "true")
        }
        return KafkaConsumer<String, String>(props)
    }

    override suspend fun consume(topic: String, processor: EventProcessor) {
        if (consumerJobs.containsKey(topic)) {
            throw IllegalStateException("Already consuming from topic: $topic")
        }

        val consumer = createConsumer("consumer-${topic}-${System.currentTimeMillis()}")
        consumer.subscribe(listOf(topic))

        val job = scope.launch {
            try {
                processor.onStart()
                while (isActive) {
                    val records = withContext(Dispatchers.IO) {
                        consumer.poll(Duration.ofMillis(100))
                    }
                    
                    for (record in records) {
                        try {
                            val event = Event(
                                type = record.key() ?: "",
                                payload = record.value() ?: "",
                                timestamp = record.timestamp()
                            )
                            
                            processor.process(event)
                        } catch (e: Exception) {
                            processor.handleError(
                                Event(
                                    type = record.key() ?: "",
                                    payload = record.value() ?: "",
                                    timestamp = record.timestamp()
                                ),
                                e
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                // Normal cancellation, cleanup
                withContext(NonCancellable) {
                    processor.onStop()
                    consumer.close()
                }
            } catch (e: Exception) {
                // Unexpected error
                withContext(NonCancellable) {
                    processor.onStop()
                    consumer.close()
                }
                throw e
            }
        }
        
        consumerJobs[topic] = job
    }

    override suspend fun stop(topic: String) {
        consumerJobs.remove(topic)?.let { job ->
            job.cancelAndJoin()
        }
    }

    override suspend fun stopAll() {
        consumerJobs.values.forEach { job ->
            job.cancelAndJoin()
        }
        consumerJobs.clear()
    }
}
