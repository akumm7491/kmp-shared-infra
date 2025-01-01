package com.example.kmp.service.registry.registry

import com.example.kmp.service.registry.model.ServiceInstance
import com.example.kmp.services.base.KMPService
import com.example.kmp.networking.configureServiceRegistry
import com.example.kmp.networking.models.ServiceRegistryConfig
import com.example.kmp.monitoring.KtorMonitoringFactory
import com.example.kmp.service.registry.routes.registerServiceEndpoints
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class DynamicServiceRegistry : KMPService(
    projectId = "kmp",
    serviceName = "service-registry",
    isServiceRegistry = true
) {
    private val servicesByName = ConcurrentHashMap<String, MutableMap<String, ServiceInstance>>()

    override fun Application.configureService() {
        log().info("Configuring Dynamic Service Registry")

        // Configure service registry
        configureServiceRegistry(ServiceRegistryConfig(
            port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt() ?: 8761,
            enableSelfPreservation = false,
            renewalPercentThreshold = 0.85,
            peerEurekaNodes = emptyList()
        ))

        // Log successful configuration
        log().info("Dynamic Service Registry configured successfully")
    }

    override fun Routing.configureRoutes() {
        registerServiceEndpoints(this@DynamicServiceRegistry)
    }

    fun register(instance: ServiceInstance): ServiceInstance {
        log().info("Registering instance: ${instance.id} for service: ${instance.serviceName}")
        
        // Check if instance already exists
        servicesByName.forEach { (_, services) ->
            services[instance.id]?.let { existingInstance ->
                log().warn("Instance ${instance.id} already registered, updating status")
                return updateStatus(instance.id, instance.status) ?: existingInstance
            }
        }
        
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
