package com.example.kmp.networking

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import com.example.kmp.networking.models.ServiceConfig
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable

@Serializable
data class ServiceRegistrationRequest(
    val id: String,
    val serviceName: String,
    val host: String,
    val port: Int,
    val status: String,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class ServiceRegistrationResponse(
    val id: String,
    val serviceName: String,
    val status: String
)

/**
 * Extension function to configure service discovery and configuration for a Ktor application
 */
fun Application.configureServiceDiscovery(serviceConfig: ServiceConfig) {
    val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
    }
    
    // Start registration and heartbeat process
    val registrationJob = launch {
        try {
            // Parse service URL for host and port
            val serviceUrl = serviceConfig.serviceUrl.removePrefix("http://")
            val host = serviceUrl.substringBefore(":")
            val port = serviceUrl.substringAfter(":").toInt()
            
            // Register service
            val registrationRequest = ServiceRegistrationRequest(
                id = serviceConfig.instanceId,
                serviceName = serviceConfig.serviceName,
                host = host,
                port = port,
                status = "UP"
            )
            
            val response: HttpResponse = client.post("${serviceConfig.registryUrl}/services") {
                contentType(ContentType.Application.Json)
                setBody(registrationRequest)
            }
            
            if (response.status.isSuccess()) {
                log.info("Successfully registered service ${serviceConfig.serviceName}")
                
                // Start heartbeat
                while (isActive) {
                    delay(30.seconds)
                    try {
                        val heartbeatResponse: HttpResponse = client.put("${serviceConfig.registryUrl}/services/${serviceConfig.instanceId}/heartbeat")
                        if (heartbeatResponse.status.isSuccess()) {
                            log.debug("Heartbeat sent successfully")
                        } else {
                            log.error("Failed to send heartbeat: ${heartbeatResponse.status}")
                        }
                    } catch (e: Exception) {
                        log.error("Failed to send heartbeat", e)
                    }
                }
            } else {
                log.error("Failed to register service: ${response.status}")
            }
        } catch (e: Exception) {
            log.error("Failed to register service", e)
        }
    }

    // Register shutdown hook
    environment.monitor.subscribe(ApplicationStopping) {
        runBlocking {
            registrationJob.cancelAndJoin() // Cancel the registration/heartbeat job
            try {
                val response: HttpResponse = client.delete("${serviceConfig.registryUrl}/services/${serviceConfig.instanceId}")
                if (response.status.isSuccess()) {
                    log.info("Successfully deregistered service ${serviceConfig.serviceName}")
                } else {
                    log.error("Failed to deregister service: ${response.status}")
                }
            } catch (e: Exception) {
                log.error("Failed to deregister service", e)
            } finally {
                client.close()
            }
        }
    }

    // Register health check endpoint
    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }
    }

    // Log service configuration
    log.info("Service ${serviceConfig.serviceName} starting with configuration:")
    log.info("- Service URL: ${serviceConfig.serviceUrl}")
    log.info("- Registry URL: ${serviceConfig.registryUrl}")
    log.info("- Config Server URL: ${serviceConfig.configServerUrl}")
}
