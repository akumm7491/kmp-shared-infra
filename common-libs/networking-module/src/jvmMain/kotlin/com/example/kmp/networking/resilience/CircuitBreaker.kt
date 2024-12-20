package com.example.kmp.networking.resilience

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import kotlin.math.min

class CircuitBreaker(
    private val failureThreshold: Int = 5,
    private val resetTimeoutMs: Long = 60000
) {
    private var failures = 0
    private var lastFailureTime: Instant? = null
    private var state: State = State.CLOSED
    private val mutex = Mutex()

    sealed class State {
        object CLOSED : State()
        object OPEN : State()
        object HALF_OPEN : State()
    }

    suspend fun <T> execute(block: suspend () -> T): T {
        mutex.withLock {
            when (state) {
                State.OPEN -> {
                    if (shouldReset()) {
                        state = State.HALF_OPEN
                    } else {
                        throw CircuitBreakerOpenException()
                    }
                }
                State.HALF_OPEN, State.CLOSED -> {
                    // Continue with execution
                }
            }
        }

        return try {
            val result = block()
            mutex.withLock {
                reset()
            }
            result
        } catch (e: Exception) {
            mutex.withLock {
                recordFailure()
            }
            throw e
        }
    }

    private fun shouldReset(): Boolean {
        return lastFailureTime?.let {
            Instant.now().toEpochMilli() - it.toEpochMilli() > resetTimeoutMs
        } ?: true
    }

    private fun recordFailure() {
        failures = min(failures + 1, failureThreshold)
        lastFailureTime = Instant.now()
        if (failures >= failureThreshold) {
            state = State.OPEN
        }
    }

    private fun reset() {
        failures = 0
        lastFailureTime = null
        state = State.CLOSED
    }
}

class CircuitBreakerOpenException : Exception("Circuit breaker is open")
