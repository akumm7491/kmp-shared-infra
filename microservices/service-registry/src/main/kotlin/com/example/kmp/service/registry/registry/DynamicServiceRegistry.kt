package com.example.kmp.service.registry.registry

import com.example.kmp.service.registry.model.ServiceInstance
import com.example.kmp.services.base.KMPService
import com.example.kmp.networking.configureServiceRegistry
import com.example.kmp.networking.models.ServiceRegistryConfig
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import com.netflix.eureka.registry.PeerAwareInstanceRegistry
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl

class DynamicServiceRegistry : KMPService(
    projectId = "kmp",
    serviceName = "service-registry",
    isServiceRegistry = true
) {
    private val servicesByName = ConcurrentHashMap<String, MutableMap<String, ServiceInstance>>()
    private lateinit var eurekaRegistry: PeerAwareInstanceRegistry

    override fun Application.configureCustomService() {

        // Configure service registry
        configureServiceRegistry(ServiceRegistryConfig(
            port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt() ?: 8761,
            enableSelfPreservation = false,
            renewalPercentThreshold = 0.85
        ))

        // Configure custom registry routes
        routing {
            registerServiceEndpoints()
            registerEurekaEndpoints()
        }
    }

    fun Routing.registerServiceEndpoints() {
        post("/services") {
            val instance = call.receive<ServiceInstance>()
            val registeredInstance = register(instance)
            call.respond(HttpStatusCode.Created, registeredInstance)
        }

        get("/services") {
            call.respond(getAllInstances())
        }

        get("/services/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
            val instance = getInstance(id)
            if (instance != null) {
                call.respond(instance)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete("/services/{id}") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
            if (deregister(id)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        put("/services/{id}/heartbeat") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
            val instance = heartbeat(id)
            if (instance != null) {
                call.respond(instance)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/services/{id}/status") {
            val id = call.parameters["id"] ?: throw IllegalArgumentException("Missing id")
            val status = getStatus(id)
            if (status != null) {
                call.respond(status)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    fun Routing.registerEurekaEndpoints() {
        // Implement Eureka-compatible endpoints
        get("/eureka/apps") {
            val instances = getAllInstances()
            call.respond(instances.groupBy { it.serviceName })
        }

        get("/eureka/apps/{appId}") {
            val appId = call.parameters["appId"] ?: throw IllegalArgumentException("Missing appId")
            val instances = getInstancesByService(appId)
            if (instances.isNotEmpty()) {
                call.respond(instances)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/eureka/vips/{vipAddress}") {
            val vipAddress = call.parameters["vipAddress"] ?: throw IllegalArgumentException("Missing vipAddress")
            val instances = getAllInstances().filter { it.serviceName == vipAddress }
            if (instances.isNotEmpty()) {
                call.respond(instances)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    fun register(instance: ServiceInstance): ServiceInstance {
        log().info("Registering instance: ${instance.id} for service: ${instance.serviceName}")
        val services = servicesByName.computeIfAbsent(instance.serviceName) { 
            log().info("Creating new service map for: ${instance.serviceName}")
            ConcurrentHashMap() 
        }
        val registeredInstance = instance.copy(
            status = "UP",
            lastUpdated = System.currentTimeMillis()
        )
        services[instance.id] = registeredInstance
        log().info("Current services: ${servicesByName.keys}")
        return registeredInstance
    }

    fun deregister(instanceId: String): Boolean {
        log().info("Deregistering instance: $instanceId")
        servicesByName.forEach { (serviceName, services) ->
            if (services.remove(instanceId) != null) {
                log().info("Instance removed from service: $serviceName")
                if (services.isEmpty()) {
                    log().info("Removing empty service: $serviceName")
                    servicesByName.entries.removeIf { it.value === services }
                }
                return true
            }
        }
        log().warn("Instance not found: $instanceId")
        return false
    }

    fun updateStatus(instanceId: String, status: String): ServiceInstance? {
        log().info("Updating status for instance: $instanceId to: $status")
        servicesByName.forEach { (serviceName, services) ->
            services[instanceId]?.let { instance ->
                val updatedInstance = instance.copy(
                    status = status,
                    lastUpdated = System.currentTimeMillis()
                )
                services[instanceId] = updatedInstance
                log().info("Status updated for service: $serviceName, instance: $instanceId")
                return updatedInstance
            }
        }
        log().warn("Instance not found: $instanceId")
        return null
    }

    fun heartbeat(instanceId: String): ServiceInstance? {
        log().info("Processing heartbeat for instance: $instanceId")
        return updateStatus(instanceId, "UP")
    }

    fun getStatus(instanceId: String): String? {
        log().info("Getting status for instance: $instanceId")
        return getInstance(instanceId)?.status
    }

    fun getAllInstances(): List<ServiceInstance> {
        val instances = servicesByName.values.flatMap { it.values }
        log().info("Getting all instances. Found: ${instances.size}")
        log().info("Current services: ${servicesByName.keys}")
        return instances
    }

    fun getInstance(instanceId: String): ServiceInstance? {
        log().info("Looking up instance: $instanceId")
        servicesByName.forEach { (serviceName, services) ->
            services[instanceId]?.let { 
                log().info("Instance found in service: $serviceName")
                return it 
            }
        }
        log().warn("Instance not found: $instanceId")
        return null
    }

    fun getInstancesByService(serviceName: String): List<ServiceInstance> {
        log().info("Looking up instances for service: $serviceName")
        val instances = servicesByName[serviceName]?.values?.toList() ?: emptyList()
        log().info("Found ${instances.size} instances")
        return instances
    }

    fun clear() {
        log().info("Clearing all registered services")
        servicesByName.clear()
    }
}
