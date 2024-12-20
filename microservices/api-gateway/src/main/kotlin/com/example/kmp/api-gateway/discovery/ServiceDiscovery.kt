package com.example.kmp.api.gateway.discovery

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
    override suspend fun getService(serviceName: String): ServiceInstance? {
        return eurekaClient.getService(serviceName)?.let { instance ->
            ServiceInstance(
                serviceName = instance.app,
                url = instance.homePageUrl,
                metadata = instance.metadata
            )
        }
    }
} 