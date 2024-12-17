package com.example.kmp.template

import com.example.kmp.auth.TokenValidator
import com.example.kmp.messaging.Event
import com.example.kmp.messaging.EventBus
import com.example.kmp.storage.StorageClient
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        // Configure metrics
        meterBinders = listOf()
    }

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    val tokenValidator = TokenValidator()
    val eventBus = EventBus()
    val storageClient = StorageClient()

    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }

        get("/health") {
            call.respond(mapOf("status" to "UP"))
        }

        get("/demo") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ") ?: "invalid"
            
            val validationResult = tokenValidator.validateToken(token)
            if (!validationResult.isValid) {
                call.respond(HttpStatusCode.OK, mapOf("error" to "Invalid token"))
                return@get
            }

            launch {
                eventBus.publish("demo-topic", Event(
                    type = "demo-event",
                    payload = "Hello from template service!",
                    timestamp = System.currentTimeMillis()
                ))
            }

            val repository = storageClient.getRepository<Map<String, String>>("demo")
            repository.save(mapOf("message" to "Hello from storage!"))

            call.respond(mapOf(
                "message" to "Demo endpoint successful",
                "userId" to validationResult.userId
            ))
        }
    }
}
