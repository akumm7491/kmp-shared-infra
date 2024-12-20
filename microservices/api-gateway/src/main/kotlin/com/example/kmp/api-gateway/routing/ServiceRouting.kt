package com.example.kmp.api.gateway.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import com.example.kmp.api.gateway.client.ServiceClient
import com.example.kmp.api.gateway.discovery.ServiceDiscovery

fun Application.configureServiceRouting(
    serviceDiscovery: ServiceDiscovery,
    serviceClient: ServiceClient
) {
    routing {
        route("/api") {
            // Dynamic project routing
            route("/{projectId}") {
                // Forward all requests to appropriate services
                handle {
                    val projectId = call.parameters["projectId"] ?: throw InvalidProjectException()
                    val path = call.request.path().removePrefix("/api/$projectId")
                    
                    // Get service name from the first path segment
                    val serviceName = path.split("/")
                        .firstOrNull { it.isNotEmpty() }
                        ?: throw InvalidServiceException()
                    
                    // Construct full service name with project prefix
                    val fullServiceName = "$projectId-$serviceName"
                    
                    val serviceInstance = serviceDiscovery.getService(fullServiceName)
                        ?: throw ServiceNotFoundException(fullServiceName)
                    
                    val response = serviceClient.forward(
                        serviceInstance = serviceInstance,
                        path = path,
                        originalRequest = call
                    )
                    call.respond(response.status, response.body())
                }
            }
        }

        // Error handling
        install(StatusPages) {
            exception<ServiceNotFoundException> { call, cause ->
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf(
                    "error" to "Service Unavailable",
                    "message" to cause.message
                ))
            }
            
            exception<InvalidProjectException> { call, _ ->
                call.respond(HttpStatusCode.BadRequest, mapOf(
                    "error" to "Invalid Project",
                    "message" to "Project ID is required"
                ))
            }
            
            exception<Throwable> { call, cause ->
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to "Internal Server Error",
                    "message" to (cause.message ?: "Unknown error")
                ))
            }
        }
    }
}

class InvalidProjectException : Exception("Invalid project ID")
class InvalidServiceException : Exception("Invalid service name")

// Supporting classes
class ServiceNotFoundException(serviceName: String) : 
    Exception("Service $serviceName not found or unavailable") 