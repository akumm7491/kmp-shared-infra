package com.example.kmp.service.registry.test

import com.example.kmp.service.registry.model.ServiceInstance
import java.util.*

object ServiceRegistryTestData {
    fun createTestInstance(
        id: String = UUID.randomUUID().toString(),
        serviceName: String = "test-service",
        host: String = "localhost",
        port: Int = 8080,
        status: String = "UP",
        metadata: Map<String, String> = mapOf(
            "version" to "1.0.0",
            "environment" to "test",
            "zone" to "us-east-1"
        )
    ): ServiceInstance {
        return ServiceInstance(
            id = id,
            serviceName = serviceName.uppercase(),
            host = host,
            port = port,
            status = status.uppercase(),
            metadata = metadata,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun createTestInstances(count: Int): List<ServiceInstance> {
        return (1..count).map {
            createTestInstance(
                serviceName = "test-service-$it",
                host = "localhost",
                port = 8080 + it,
                metadata = mapOf(
                    "version" to "1.0.0",
                    "environment" to "test",
                    "zone" to "us-east-1",
                    "instance" to it.toString()
                )
            )
        }
    }
}
