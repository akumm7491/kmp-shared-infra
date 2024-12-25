package com.example.kmp.service.registry.steps

import com.example.kmp.service.registry.model.ServiceInstance
import com.example.kmp.service.registry.test.ServiceRegistryTestBase
import com.example.kmp.service.registry.test.ServiceRegistryTestConfig
import com.example.kmp.service.registry.test.ServiceRegistryTestData
import com.example.kmp.testing.config.KMPTestConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class ServiceRegistrySteps : ServiceRegistryTestBase() {
    override val config: KMPTestConfig = ServiceRegistryTestConfig()

    suspend fun registerService(instance: ServiceInstance): HttpResponse {
        return client?.post("/services") {
            setBody(instance)
        } ?: throw IllegalStateException("Client not initialized")
    }

    suspend fun deregisterService(instance: ServiceInstance): HttpResponse {
        return client?.delete("/services/${instance.id}")
            ?: throw IllegalStateException("Client not initialized")
    }

    suspend fun updateServiceStatus(instance: ServiceInstance, status: String): HttpResponse {
        return client?.put("/services/${instance.id}/status") {
            setBody(status)
        } ?: throw IllegalStateException("Client not initialized")
    }

    suspend fun sendHeartbeat(instance: ServiceInstance): HttpResponse {
        return client?.put("/services/${instance.id}/heartbeat")
            ?: throw IllegalStateException("Client not initialized")
    }

    suspend fun getServiceStatus(instance: ServiceInstance): HttpResponse {
        return client?.get("/services/${instance.id}/status")
            ?: throw IllegalStateException("Client not initialized")
    }

    suspend fun getServiceById(instance: ServiceInstance): HttpResponse {
        return client?.get("/services/${instance.id}")
            ?: throw IllegalStateException("Client not initialized")
    }

    suspend fun getServicesByName(serviceName: String): HttpResponse {
        return client?.get("/services?serviceName=$serviceName")
            ?: throw IllegalStateException("Client not initialized")
    }
}
