package com.example.kmp.messaging

expect class EventConsumer() {
    /**
     * Start consuming events from the specified topic
     * @param topic The topic to consume events from
     * @param processor The processor to handle consumed events
     */
    suspend fun consume(topic: String, processor: EventProcessor)
    
    /**
     * Stop consuming events from the specified topic
     * @param topic The topic to stop consuming from
     */
    suspend fun stop(topic: String)
    
    /**
     * Stop consuming events from all topics
     */
    suspend fun stopAll()

    companion object {
        fun create(): EventConsumer
    }
}
