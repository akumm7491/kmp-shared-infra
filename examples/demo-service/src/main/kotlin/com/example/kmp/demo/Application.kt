package com.example.kmp.demo

import com.example.kmp.monitoring.MonitoringFactory
import com.example.kmp.networking.NetworkingFactory
import com.example.kmp.networking.models.ApiError
import com.example.kmp.networking.models.ApiResponse
import com.example.kmp.networking.respondError
import com.example.kmp.networking.respondSuccess
import com.example.kmp.messaging.Event
import com.example.kmp.messaging.EventBus
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.launch

@Serializable
data class WeatherData(
    val temperature: Double,
    val condition: String,
    val location: String
)

@Serializable
data class UserProfile(
    val id: String,
    val name: String,
    val email: String
)

fun main() {
    embeddedServer(Netty, port = 8083, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val metricsProvider = MonitoringFactory.createMetricsProvider()
    val logProvider = MonitoringFactory.createLogProvider("demo-service")
    val eventBus = EventBus()
    
    val networking = NetworkingFactory.create {
        baseUrl = System.getenv("SERVICE_A_URL") ?: "http://localhost:8081"
        timeout = 30000
        retries = 3
        rateLimitRequests = 100
        rateLimitDuration = 1.seconds
        circuitBreakerFailureThreshold = 5
        circuitBreakerResetTimeout = 60000
        logging(logProvider)
        metrics(metricsProvider)
    }
    
    networking.install(this)

    routing {
        // Example 1: Basic GET endpoint with success response and event publishing
        get("/api/weather/{location}") {
            val location = call.parameters["location"] ?: run {
                call.respondError(ApiError.badRequest("Location is required"))
                return@get
            }
            
            try {
                networking.executeWithResilience {
                    // Simulate getting weather data
                    val weatherData = WeatherData(
                        temperature = 72.5,
                        condition = "Sunny",
                        location = location
                    )

                    // Publish weather check event
                    launch {
                        eventBus.publish("demo-weather-events", Event(
                            type = "weather-checked",
                            payload = mapOf(
                                "location" to location,
                                "temperature" to 72.5,
                                "condition" to "Sunny"
                            ),
                            timestamp = System.currentTimeMillis()
                        ))
                    }

                    call.respondSuccess(weatherData)
                }
            } catch (e: Exception) {
                logProvider.error("Failed to get weather data", e, mapOf(
                    "location" to location
                ))
                call.respondError(ApiError.internalError())
            }
        }

        // Example 2: Making HTTP requests to service-a and publishing events
        get("/api/users/{userId}/profile") {
            val userId = call.parameters["userId"] ?: run {
                call.respondError(ApiError.badRequest("User ID is required"))
                return@get
            }
            
            try {
                networking.executeWithResilience {
                    // Make HTTP request to service-a
                    val userProfile = networking.client.get<ApiResponse<UserProfile>>(
                        url = "/api/service-a/data"
                    )

                    // Publish user profile accessed event
                    launch {
                        eventBus.publish("demo-user-events", Event(
                            type = "user-profile-accessed",
                            payload = mapOf(
                                "userId" to userId,
                                "timestamp" to System.currentTimeMillis()
                            ),
                            timestamp = System.currentTimeMillis()
                        ))
                    }

                    call.respondSuccess(userProfile.data)
                }
            } catch (e: Exception) {
                logProvider.error("Failed to get user profile", e, mapOf(
                    "userId" to userId
                ))
                call.respondError(ApiError.internalError())
            }
        }

        // Example 3: Rate-limited endpoint with event tracking
        get("/api/limited") {
            try {
                networking.executeWithResilience {
                    launch {
                        eventBus.publish("demo-rate-limit-events", Event(
                            type = "rate-limit-accessed",
                            payload = mapOf(
                                "timestamp" to System.currentTimeMillis()
                            ),
                            timestamp = System.currentTimeMillis()
                        ))
                    }

                    call.respondSuccess(mapOf(
                        "message" to "This endpoint is rate-limited to 100 requests per second"
                    ))
                }
            } catch (e: com.example.kmp.networking.ratelimit.RateLimitExceededException) {
                // Track rate limit exceeded events
                launch {
                    eventBus.publish("demo-rate-limit-events", Event(
                        type = "rate-limit-exceeded",
                        payload = mapOf(
                            "timestamp" to System.currentTimeMillis()
                        ),
                        timestamp = System.currentTimeMillis()
                    ))
                }

                call.respondError(ApiError(
                    code = "RATE_LIMIT_EXCEEDED",
                    message = "Too many requests. Please try again later."
                ))
            }
        }

        // Example 4: Circuit breaker demonstration with event tracking
        get("/api/circuit-breaker-test") {
            try {
                networking.executeWithResilience {
                    // Simulate an operation that might fail
                    if (Math.random() < 0.7) { // 70% chance of failure
                        throw RuntimeException("Simulated failure")
                    }
                    
                    launch {
                        eventBus.publish("demo-circuit-breaker-events", Event(
                            type = "circuit-breaker-success",
                            payload = mapOf(
                                "timestamp" to System.currentTimeMillis()
                            ),
                            timestamp = System.currentTimeMillis()
                        ))
                    }

                    call.respondSuccess(mapOf(
                        "message" to "Operation succeeded!"
                    ))
                }
            } catch (e: com.example.kmp.networking.resilience.CircuitBreakerOpenException) {
                launch {
                    eventBus.publish("demo-circuit-breaker-events", Event(
                        type = "circuit-breaker-open",
                        payload = mapOf(
                            "timestamp" to System.currentTimeMillis()
                        ),
                        timestamp = System.currentTimeMillis()
                    ))
                }

                call.respondError(ApiError(
                    code = "CIRCUIT_BREAKER_OPEN",
                    message = "Service is temporarily unavailable due to too many failures"
                ))
            } catch (e: Exception) {
                launch {
                    eventBus.publish("demo-circuit-breaker-events", Event(
                        type = "circuit-breaker-error",
                        payload = mapOf(
                            "error" to e.message,
                            "timestamp" to System.currentTimeMillis()
                        ),
                        timestamp = System.currentTimeMillis()
                    ))
                }

                call.respondError(ApiError.internalError())
            }
        }
    }
}
