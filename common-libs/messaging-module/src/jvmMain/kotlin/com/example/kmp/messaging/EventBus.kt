package com.example.kmp.messaging

actual class EventBus {
    private val producer = KafkaEventProducer()
    private val consumer = KafkaEventConsumer()

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
