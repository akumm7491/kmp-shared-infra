package com.example.kmp.servicea

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
    embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
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
            call.respond(HttpStatusCode.OK, mapOf("status" to "healthy"))
        }

        get("/api/service-a/data") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ") ?: "invalid"
            val validationResult = tokenValidator.validateToken(token)
            
            if (!validationResult.isValid) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                return@get
            }

            val repository = storageClient.getRepository<Map<String, String>>("service-a")
            val data = repository.findAll().firstOrNull() ?: mapOf("message" to "No data found")

            launch {
                eventBus.publish("service-a-events", Event(
                    type = "data-accessed",
                    payload = "Data accessed by user ${validationResult.userId}",
                    timestamp = System.currentTimeMillis()
                ))
            }

            call.respond(HttpStatusCode.OK, data)
        }
    }
}
