package com.example.kmp.api.gateway.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.client.statement.*
import com.example.kmp.api.gateway.client.ServiceClient
import com.example.kmp.api.gateway.discovery.ServiceDiscovery
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ServiceRouting")

fun Application.configureServiceRouting(
    serviceDiscovery: ServiceDiscovery,
    serviceClient: ServiceClient
) {
    // Install status pages for error handling
    install(StatusPages) {
        exception<ServiceNotFoundException> { call, cause ->
            logger.warn("Service not found: ${cause.message}")
            call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                "error" to "Service Unavailable",
                "message" to cause.message,
                "timestamp" to System.currentTimeMillis()
            ))
        }
        
        exception<InvalidProjectException> { call, cause ->
            logger.warn("Invalid project: ${cause.message}")
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "Invalid Project",
                "message" to cause.message,
                "timestamp" to System.currentTimeMillis()
            ))
        }
        
        exception<InvalidServiceException> { call, cause ->
            logger.warn("Invalid service: ${cause.message}")
            call.respond(HttpStatusCode.BadRequest, mapOf(
                "error" to "Invalid Service",
                "message" to cause.message,
                "timestamp" to System.currentTimeMillis()
            ))
        }
        
        exception<Throwable> { call, cause ->
            logger.error("Unexpected error", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf(
                "error" to "Internal Server Error",
                "message" to (cause.message ?: "Unknown error"),
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }
    
    routing {
        route("/api") {
            // Dynamic project routing
            route("/{projectId}") {
                // Forward all requests to appropriate services
                handle {
                    logger.debug("Handling request for path: ${call.request.path()}")
                    
                    val projectId = call.parameters["projectId"] 
                        ?: throw InvalidProjectException("Project ID is required")
                    
                    val path = call.request.path().removePrefix("/api/$projectId")
                    
                    // Get service name from the first path segment
                    val serviceName = path.split("/")
                        .firstOrNull { it.isNotEmpty() }
                        ?: throw InvalidServiceException("Service name not found in path")
                    
                    // Construct full service name with project prefix
                    val fullServiceName = "$projectId-$serviceName"
                    logger.debug("Looking up service: $fullServiceName")
                    
                    val serviceInstance = serviceDiscovery.getService(fullServiceName)
                        ?: throw ServiceNotFoundException(fullServiceName)
                    
                    logger.debug("Forwarding request to service: ${serviceInstance.url}$path")
                    
                    val response = serviceClient.forward(
                        serviceInstance = serviceInstance,
                        path = path,
                        originalRequest = call
                    )
                    
                    val responseText = response.bodyAsText()
                    call.respond(response.status, responseText)
                }
            }
        }
    }
}

class InvalidProjectException(message: String) : Exception(message)
class InvalidServiceException(message: String) : Exception(message)
class ServiceNotFoundException(serviceName: String) : 
    Exception("Service $serviceName not found or unavailable")
