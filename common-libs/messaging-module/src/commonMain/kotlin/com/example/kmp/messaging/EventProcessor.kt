package com.example.kmp.messaging

interface EventProcessor {
    /**
     * Process a received event
     * @param event The event to process
     * @return Boolean indicating whether the processing was successful
     */
    suspend fun process(event: Event): Boolean
    
    /**
     * Handle any errors that occur during event processing
     * @param event The event that caused the error
     * @param error The error that occurred
     */
    suspend fun handleError(event: Event, error: Throwable)
    
    /**
     * Called when the processor is starting up
     */
    suspend fun onStart() { }
    
    /**
     * Called when the processor is shutting down
     */
    suspend fun onStop() { }
}
