package com.example.kmp.service.registry.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class RegistrationRequest(
    val serviceName: String,
    val host: String,
    val port: Int,
    val metadata: Map<String, String> = emptyMap()
) {
    fun toServiceInstance(): ServiceInstance {
        return ServiceInstance(
            id = UUID.randomUUID().toString(),
            serviceName = serviceName,
            host = host,
            port = port,
            status = "UP",
            metadata = metadata,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
