package com.example.kmp.service.registry.routes

import com.example.kmp.service.registry.model.*
import com.example.kmp.service.registry.registry.DynamicServiceRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.*
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("EurekaRoutes")

fun Route.registerEurekaEndpoints(serviceRegistry: DynamicServiceRegistry) {
    route("/eureka/apps") {
        // Registration endpoint with appId
        post("/{appId}") {
            val appId = call.parameters["appId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
            val requestBody = call.receive<JsonObject>()
            val instanceJson = requestBody.jsonObject["instance"]?.jsonObject
                ?: return@post call.respond(HttpStatusCode.BadRequest)
            
            logger.info("Registering service with appId: $appId")
            val instance = ServiceInstance(
                id = instanceJson["instanceId"]?.jsonPrimitive?.content 
                    ?: "${instanceJson["hostName"]?.jsonPrimitive?.content}:${appId}:${instanceJson["port"]?.jsonObject?.get("\$")?.jsonPrimitive?.content}",
                serviceName = appId.uppercase(),
                host = instanceJson["hostName"]?.jsonPrimitive?.content ?: "",
                port = instanceJson["port"]?.jsonObject?.get("\$")?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                status = instanceJson["status"]?.jsonPrimitive?.content ?: "STARTING",
                metadata = instanceJson["metadata"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap(),
                lastUpdated = System.currentTimeMillis()
            )
            
            serviceRegistry.register(instance)
            logger.info("Service registered: ${instance.serviceName} (${instance.id})")
            call.respond(HttpStatusCode.NoContent)
        }

        // Query all instances
        get {
            val instances = serviceRegistry.getAllInstances()
            logger.info("Querying all instances. Found: ${instances.size}")
            val response = EurekaResponse(
                applications = Applications(
                    application = instances.groupBy { it.serviceName }.map { (name, serviceInstances) ->
                        EurekaApplication(
                            name = name,
                            instance = serviceInstances.map { instance ->
                                createInstanceInfo(instance)
                            }
                        )
                    }
                )
            )
            call.respond(response)
        }

        // Query specific application
        get("/{appId}") {
            val appId = call.parameters["appId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            logger.info("Querying service: $appId")
            val instances = serviceRegistry.getInstancesByService(appId.uppercase())
            logger.info("Found ${instances.size} instances for service: $appId")
            
            val response = EurekaResponse(
                applications = Applications(
                    application = if (instances.isEmpty()) {
                        emptyList()
                    } else {
                        listOf(
                            EurekaApplication(
                                name = appId.uppercase(),
                                instance = instances.map { instance ->
                                    createInstanceInfo(instance)
                                }
                            )
                        )
                    }
                )
            )
            call.respond(response)
        }

        // Heartbeat endpoint
        put("/{appId}/{instanceId}/status") {
            val appId = call.parameters["appId"]?.uppercase()
            val instanceId = call.parameters["instanceId"]
            if (appId != null && instanceId != null) {
                logger.info("Updating status for service: $appId, instance: $instanceId")
                val instance = serviceRegistry.updateStatus(instanceId, "UP")
                if (instance != null) {
                    logger.info("Status updated successfully")
                    call.respond(HttpStatusCode.OK)
                } else {
                    logger.warn("Instance not found: $instanceId")
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        // Deregistration endpoint
        delete("/{appId}/{instanceId}") {
            val appId = call.parameters["appId"]?.uppercase()
            val instanceId = call.parameters["instanceId"]
            if (appId != null && instanceId != null) {
                logger.info("Deregistering service: $appId, instance: $instanceId")
                if (serviceRegistry.deregister(instanceId)) {
                    logger.info("Service deregistered successfully")
                    call.respond(HttpStatusCode.OK)
                } else {
                    logger.warn("Instance not found: $instanceId")
                    call.respond(HttpStatusCode.NotFound)
                }
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        // Root redirect
        get("/") {
            call.respondRedirect("/eureka/apps", permanent = true)
        }
    }
}

private fun createInstanceInfo(instance: ServiceInstance): InstanceInfo {
    return InstanceInfo(
        instanceId = instance.id,
        hostName = instance.host,
        app = instance.serviceName,
        ipAddr = instance.host,
        status = instance.status,
        port = Port(instance.port),
        leaseInfo = LeaseInfo(
            registrationTimestamp = instance.lastUpdated,
            lastRenewalTimestamp = instance.lastUpdated,
            serviceUpTimestamp = instance.lastUpdated
        ),
        metadata = instance.metadata,
        homePageUrl = "http://${instance.host}:${instance.port}/",
        statusPageUrl = "http://${instance.host}:${instance.port}/info",
        healthCheckUrl = "http://${instance.host}:${instance.port}/health",
        vipAddress = instance.serviceName,
        secureVipAddress = instance.serviceName,
        lastUpdatedTimestamp = instance.lastUpdated,
        lastDirtyTimestamp = instance.lastUpdated
    )
} 