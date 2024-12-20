package com.example.kmp.messaging
import com.example.kmp.messaging.EventProducer
import com.example.kmp.messaging.EventConsumer
import com.example.kmp.messaging.Event
import com.example.kmp.messaging.EventProcessor

actual class EventBus {
    private val producer = EventProducer()
    private val consumer = EventConsumer()

    actual suspend fun publish(topic: String, event: Event) {
        producer.publish(topic, event)
    }

    actual suspend fun subscribe(topic: String, handler: suspend (Event) -> Unit) {
        val processor = object : EventProcessor {
            override suspend fun process(event: Event): Boolean {
                handler(event)
                return true
            }
            
            override suspend fun handleError(event: Event, error: Throwable) {
                // Log error or handle based on application needs
                println("Error processing event: ${error.message}")
            }
        }
        
        consumer.consume(topic, processor)
    }
}
