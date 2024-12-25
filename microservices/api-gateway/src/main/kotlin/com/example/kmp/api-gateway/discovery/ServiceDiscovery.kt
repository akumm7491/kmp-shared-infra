package com.example.kmp.api.gateway.discovery

import com.netflix.discovery.EurekaClient
import com.netflix.appinfo.InstanceInfo
import io.ktor.server.application.*
import org.slf4j.LoggerFactory

interface ServiceDiscovery {
    suspend fun getService(serviceName: String): ServiceInstance?
}

data class ServiceInstance(
    val serviceName: String,
    val url: String,
    val metadata: Map<String, String> = emptyMap()
)

class EurekaServiceDiscovery(
    private val eurekaClient: EurekaClient
) : ServiceDiscovery {
    private val logger = LoggerFactory.getLogger(EurekaServiceDiscovery::class.java)

    override suspend fun getService(serviceName: String): ServiceInstance? {
        return try {
            val instances = eurekaClient.getApplication(serviceName)
                ?.instances
                ?.filter { it.status == InstanceInfo.InstanceStatus.UP }
                
            if (instances.isNullOrEmpty()) {
                logger.warn("No instances found for service: $serviceName")
                return null
            }
            
            // Simple round-robin for now, could be enhanced with load balancing
            val instance = instances.random()
            
            ServiceInstance(
                serviceName = instance.appName,
                url = instance.homePageUrl,
                metadata = instance.metadata
            ).also {
                logger.debug("Found service instance: $it")
            }
        } catch (e: Exception) {
            logger.error("Error getting service instance for $serviceName", e)
            null
        }
    }
}
