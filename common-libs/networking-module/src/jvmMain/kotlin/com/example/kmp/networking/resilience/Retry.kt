package com.example.kmp.networking.resilience

import kotlinx.coroutines.delay
import kotlin.math.pow

class RetryConfig(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 100,
    val maxDelayMs: Long = 1000,
    val factor: Double = 2.0,
    val retryOn: Set<Class<out Throwable>> = setOf(Exception::class.java)
)

suspend fun <T> retry(
    config: RetryConfig = RetryConfig(),
    block: suspend () -> T
): T {
    var currentDelay = config.initialDelayMs
    var lastException: Throwable? = null

    repeat(config.maxAttempts) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            lastException = e
            if (!config.retryOn.any { it.isInstance(e) }) {
                throw e
            }
            if (attempt == config.maxAttempts - 1) {
                throw MaxRetriesExceededException(config.maxAttempts, e)
            }
            
            delay(currentDelay)
            currentDelay = (currentDelay * config.factor)
                .toLong()
                .coerceAtMost(config.maxDelayMs)
        }
    }
    
    throw lastException ?: IllegalStateException("Retry failed for unknown reason")
}

class MaxRetriesExceededException(
    val attempts: Int,
    cause: Throwable
) : Exception("Max retries ($attempts) exceeded", cause)
