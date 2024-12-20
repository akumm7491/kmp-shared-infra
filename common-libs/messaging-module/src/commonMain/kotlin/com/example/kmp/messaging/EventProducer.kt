package com.example.kmp.messaging

interface EventProducer {
    /**
     * Publish an event to the specified topic
     * @param topic The topic to publish the event to
     * @param event The event to publish
     */
    suspend fun publish(topic: String, event: Event)
    
    /**
     * Close the producer and release resources
     */
    suspend fun close()
}
