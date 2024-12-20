package com.example.kmp.serviceb

import com.example.kmp.messaging.EventBus
import com.example.kmp.messaging.EventConsumer
import com.example.kmp.messaging.EventProcessor
import com.example.kmp.monitoring.KtorMonitoring
import com.example.kmp.monitoring.MonitoringFactory
import com.example.kmp.networking.KtorServer
import com.example.kmp.storage.StorageClient
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch

fun main() {
    KtorServer(port = 8082) {
        // Install monitoring with auto-configuration from environment
        install(KtorMonitoring) {
            metrics = true
            tracing = true
            logging = true
            serviceName = "service-b"
        }
        module()
    }.start()
}

fun Application.module() {
    val eventBus = EventBus()
    val storageClient = StorageClient()
    val logProvider = MonitoringFactory.createLogProvider("service-b")
    val metricsProvider = MonitoringFactory.createMetricsProvider()

    // Configure event consumer
    val eventConsumer = EventConsumer("service-a-events") {
        // Process events from service-a
        process { event ->
            logProvider.info("Processing event", mapOf(
                "eventType" to event.type,
                "timestamp" to event.timestamp
            ))

            // Track metrics
            metricsProvider.incrementCounter("events.processed", mapOf(
                "type" to event.type
            ))

            // Store event data
            storageClient.storeData(
                "event-${event.timestamp}",
                event.payload
            )

            // Publish processed event
            eventBus.publish("service-b-events", event.copy(
                type = "event-processed",
                payload = mapOf(
                    "originalEvent" to event.payload,
                    "processingTimestamp" to System.currentTimeMillis()
                )
            ))
        }

        // Configure error handling
        onError { event, error ->
            logProvider.error("Failed to process event", mapOf(
                "eventType" to event.type,
                "error" to error.message
            ))
            
            metricsProvider.incrementCounter("events.failed", mapOf(
                "type" to event.type,
                "error" to error.javaClass.simpleName
            ))
        }

        // Configure retry policy
        retryPolicy {
            maxAttempts = 3
            backoffMs = 1000
        }
    }

    routing {
        // Health check endpoint
        get("/health") {
            val status = if (eventConsumer.isHealthy()) "healthy" else "unhealthy"
            call.respond(HttpStatusCode.OK, mapOf("status" to status))
        }

        // Status endpoint
        get("/status") {
            call.respond(HttpStatusCode.OK, mapOf(
                "eventsProcessed" to metricsProvider.getCounter("events.processed"),
                "eventsFailed" to metricsProvider.getCounter("events.failed"),
                "isConsumerHealthy" to eventConsumer.isHealthy()
            ))
        }
    }
}
