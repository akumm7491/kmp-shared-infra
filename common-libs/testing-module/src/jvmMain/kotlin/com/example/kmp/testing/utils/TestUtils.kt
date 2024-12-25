package com.example.kmp.testing.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Common test utilities
 */
object TestUtils {
    private val logger = LoggerFactory.getLogger(TestUtils::class.java)

    /**
     * Retry an operation with delay between attempts
     */
    suspend fun <T> retry(
        attempts: Int = 3,
        delay: Duration = 1.seconds,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null
        
        repeat(attempts) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                lastException = e
                logger.warn("Attempt ${attempt + 1} failed: ${e.message}")
                if (attempt < attempts - 1) {
                    delay(delay)
                }
            }
        }
        
        throw lastException ?: IllegalStateException("All attempts failed")
    }

    /**
     * Execute block with timeout
     */
    suspend fun <T> withTimeout(
        timeout: Duration = 5.seconds,
        block: suspend () -> T
    ): T = withTimeout(timeout.inWholeMilliseconds) {
        block()
    }

    /**
     * Execute block with logging
     */
    suspend fun <T> withLogging(
        operation: String,
        block: suspend () -> T
    ): T {
        logger.info("Starting: $operation")
        try {
            val result = block()
            logger.info("Completed: $operation")
            return result
        } catch (e: Exception) {
            logger.error("Failed: $operation", e)
            throw e
        }
    }

    /**
     * Execute block with retry and timeout
     */
    suspend fun <T> withRetryAndTimeout(
        attempts: Int = 3,
        delay: Duration = 1.seconds,
        timeout: Duration = 5.seconds,
        block: suspend () -> T
    ): T = retry(attempts, delay) {
        withTimeout(timeout) {
            block()
        }
    }

    /**
     * Execute block with retry and logging
     */
    suspend fun <T> withRetryAndLogging(
        operation: String,
        attempts: Int = 3,
        delay: Duration = 1.seconds,
        block: suspend () -> T
    ): T = withLogging(operation) {
        retry(attempts, delay) {
            block()
        }
    }

    /**
     * Execute block with timeout and logging
     */
    suspend fun <T> withTimeoutAndLogging(
        operation: String,
        timeout: Duration = 5.seconds,
        block: suspend () -> T
    ): T = withLogging(operation) {
        withTimeout(timeout) {
            block()
        }
    }

    /**
     * Execute block with retry, timeout and logging
     */
    suspend fun <T> withRetryTimeoutAndLogging(
        operation: String,
        attempts: Int = 3,
        delay: Duration = 1.seconds,
        timeout: Duration = 5.seconds,
        block: suspend () -> T
    ): T = withLogging(operation) {
        retry(attempts, delay) {
            withTimeout(timeout) {
                block()
            }
        }
    }
}
