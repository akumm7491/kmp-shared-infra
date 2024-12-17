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
import kotlinx.coroutines.launch

fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val tokenValidator = TokenValidator()
    val eventBus = EventBus()
    val storageClient = StorageClient()

    routing {
        get("/health") {
            call.respond(mapOf("status" to "UP", "service" to "service-a"))
        }

        get("/api/service-a/data") {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ") ?: "invalid"
            
            val validationResult = tokenValidator.validateToken(token)
            if (!validationResult.isValid) {
                call.respond(mapOf("error" to "Invalid token"))
                return@get
            }

            launch {
                eventBus.publish("service-a-events", Event(
                    type = "data-accessed",
                    payload = "Data accessed in service-a",
                    timestamp = System.currentTimeMillis()
                ))
            }

            val repository = storageClient.getRepository<Map<String, String>>("service-a-data")
            val data = repository.save(mapOf(
                "message" to "Hello from Service A!",
                "timestamp" to System.currentTimeMillis().toString()
            ))

            call.respond(data)
        }
    }
}
