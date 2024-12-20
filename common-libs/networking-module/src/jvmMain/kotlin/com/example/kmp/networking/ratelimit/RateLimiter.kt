package com.example.kmp.networking.ratelimit

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class RateLimiter(
    private val maxRequests: Int,
    private val duration: Duration = 1.seconds
) {
    private val mutex = Mutex()
    private val requests = mutableListOf<Instant>()

    suspend fun <T> execute(block: suspend () -> T): T {
        mutex.withLock {
            val now = Instant.now()
            val windowStart = now.minusMillis(duration.inWholeMilliseconds)
            
            // Remove old requests
            requests.removeAll { it.isBefore(windowStart) }
            
            // Check if we can make a new request
            if (requests.size >= maxRequests) {
                throw RateLimitExceededException()
            }
            
            // Record the new request
            requests.add(now)
        }
        
        return block()
    }
}

class RateLimitExceededException : Exception("Rate limit exceeded")
