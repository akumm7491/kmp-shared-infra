package com.example.kmp.service.registry.model

import kotlinx.serialization.Serializable

@Serializable
data class ServiceInstance(
    val app: String,
    val instanceId: String,
    val hostName: String,
    val ipAddr: String,
    val status: ServiceStatus,
    val port: Int,
    val securePort: Int,
    val healthCheckUrl: String?,
    val homePageUrl: String,
    val statusPageUrl: String,
    val metadata: Map<String, String>
) {
    companion object {
        fun from(eurekaInstance: com.netflix.appinfo.InstanceInfo): ServiceInstance {
            return ServiceInstance(
                app = eurekaInstance.appName,
                instanceId = eurekaInstance.instanceId,
                hostName = eurekaInstance.hostName,
                ipAddr = eurekaInstance.ipAddr,
                status = ServiceStatus.from(eurekaInstance.status),
                port = eurekaInstance.port,
                securePort = eurekaInstance.securePort,
                healthCheckUrl = eurekaInstance.healthCheckUrl,
                homePageUrl = eurekaInstance.homePageUrl,
                statusPageUrl = eurekaInstance.statusPageUrl,
                metadata = eurekaInstance.metadata
            )
        }
    }
}

@Serializable
enum class ServiceStatus {
    UP, DOWN, STARTING, OUT_OF_SERVICE, UNKNOWN;

    companion object {
        fun from(eurekaStatus: com.netflix.appinfo.InstanceInfo.InstanceStatus): ServiceStatus {
            return when (eurekaStatus) {
                com.netflix.appinfo.InstanceInfo.InstanceStatus.UP -> UP
                com.netflix.appinfo.InstanceInfo.InstanceStatus.DOWN -> DOWN
                com.netflix.appinfo.InstanceInfo.InstanceStatus.STARTING -> STARTING
                com.netflix.appinfo.InstanceInfo.InstanceStatus.OUT_OF_SERVICE -> OUT_OF_SERVICE
                else -> UNKNOWN
            }
        }
    }
}
