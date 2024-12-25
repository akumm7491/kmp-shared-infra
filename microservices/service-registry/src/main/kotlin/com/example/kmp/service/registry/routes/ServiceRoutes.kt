package com.example.kmp.service.registry.routes

import com.example.kmp.service.registry.model.ServiceInstance
import com.example.kmp.service.registry.registry.DynamicServiceRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.registerServiceEndpoints(serviceRegistry: DynamicServiceRegistry) {
    route("/services") {
        // Register service
        post {
            val instance = call.receive<ServiceInstance>()
            val registeredInstance = serviceRegistry.register(instance)
            call.respond(HttpStatusCode.Created, registeredInstance)
        }

        // Get all services
        get {
            val instances = serviceRegistry.getAllInstances()
            call.respond(instances)
        }

        // Get specific service instance
        get("/{instanceId}") {
            val instanceId = call.parameters["instanceId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val instance = serviceRegistry.getInstance(instanceId)
            if (instance != null) {
                call.respond(instance)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Update service status (heartbeat)
        put("/{instanceId}/heartbeat") {
            val instanceId = call.parameters["instanceId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
            val instance = serviceRegistry.heartbeat(instanceId)
            if (instance != null) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Get service status
        get("/{instanceId}/status") {
            val instanceId = call.parameters["instanceId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val status = serviceRegistry.getStatus(instanceId)
            if (status != null) {
                call.respond(status)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        // Deregister service
        delete("/{instanceId}") {
            val instanceId = call.parameters["instanceId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            if (serviceRegistry.deregister(instanceId)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
} 