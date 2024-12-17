package com.example.kmp.serviceb

import com.example.kmp.auth.TokenValidator
import com.example.kmp.messaging.Event
import com.example.kmp.messaging.EventBus
import com.example.kmp.storage.StorageClient
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch

fun main() {
    embeddedServer(Netty, port = 8082, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val tokenValidator = TokenValidator()
    val eventBus = EventBus()
    val storageClient = StorageClient()

    // Subscribe to events from service-a
    launch {
        eventBus.subscribe("service-a-events") { event ->
            println("Service B received event from Service A: $event")
        }
    }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "UP", "service" to "service-b"))
        }

        get("/api/service-b/process") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ") ?: "invalid"
            
            val validationResult = tokenValidator.validateToken(token)
            if (!validationResult.isValid) {
                call.respond(mapOf("error" to "Invalid token"))
                return@get
            }

            launch {
                eventBus.publish("service-b-events", Event(
                    type = "processing-started",
                    payload = "Processing started in service-b",
                    timestamp = System.currentTimeMillis()
                ))
            }

            val repository = storageClient.getRepository<Map<String, String>>("service-b-data")
            val data = repository.save(mapOf(
                "message" to "Processing completed in Service B",
                "timestamp" to System.currentTimeMillis().toString()
            ))

            call.respond(data)
        }
    }
}
