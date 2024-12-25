package com.example.kmp.service.registry

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

class ServiceRegistryTest : ServiceRegistryTestBase() {
    @Test
    suspend fun `test service registration`() {
        try {
            withTestServer {
                // Given a test service instance
                val testInstance = ServiceRegistryTestData.createTestInstance()
                testLogger.info("Created test instance: $testInstance")

                // When registering the service
                val response = client.post("/services") {
                    setBody(testInstance)
                }
                testLogger.info("Registration response: ${response.status}")

                // Then registration should succeed
                assertEquals(HttpStatusCode.Created, response.status)
                testLogger.info("Registration response body: ${response.bodyAsText()}")

                // And service should be discoverable
                val discoveryResponse = client.get("/services/${testInstance.id}")
                testLogger.info("Discovery response: ${discoveryResponse.status}")
                assertEquals(HttpStatusCode.OK, discoveryResponse.status)

                val discoveredInstance = Json.decodeFromString<ServiceInstance>(discoveryResponse.bodyAsText())
                testLogger.info("Discovered instance: $discoveredInstance")
                assertEquals(testInstance.serviceName, discoveredInstance.serviceName)
                assertEquals(testInstance.host, discoveredInstance.host)
                assertEquals(testInstance.port, discoveredInstance.port)
            }
        } catch (e: Exception) {
            testLogger.error("Test failed", e)
            throw e
        }
    }

    @Test
    suspend fun `test service deregistration`() {
        try {
            withTestServer {
                // Given a registered service
                val testInstance = ServiceRegistryTestData.createTestInstance()
                testLogger.info("Created test instance: $testInstance")

                val registrationResponse = client.post("/services") {
                    setBody(testInstance)
                }
                testLogger.info("Registration response: ${registrationResponse.status}")
                assertEquals(HttpStatusCode.Created, registrationResponse.status)

                // When deregistering the service
                val response = client.delete("/services/${testInstance.id}")
                testLogger.info("Deregistration response: ${response.status}")
                assertEquals(HttpStatusCode.OK, response.status)

                // Then service should not be discoverable
                val discoveryResponse = client.get("/services/${testInstance.id}")
                testLogger.info("Discovery response after deregistration: ${discoveryResponse.status}")
                assertEquals(HttpStatusCode.NotFound, discoveryResponse.status)
            }
        } catch (e: Exception) {
            testLogger.error("Test failed", e)
            throw e
        }
    }

    @Test
    suspend fun `test service heartbeat`() {
        try {
            withTestServer {
                // Given a registered service
                val testInstance = ServiceRegistryTestData.createTestInstance()
                testLogger.info("Created test instance: $testInstance")

                val registrationResponse = client.post("/services") {
                    setBody(testInstance)
                }
                testLogger.info("Registration response: ${registrationResponse.status}")
                assertEquals(HttpStatusCode.Created, registrationResponse.status)

                // When sending heartbeat
                val response = client.put("/services/${testInstance.id}/heartbeat")
                testLogger.info("Heartbeat response: ${response.status}")
                assertEquals(HttpStatusCode.OK, response.status)

                // Then service should be marked as UP
                val statusResponse = client.get("/services/${testInstance.id}/status")
                testLogger.info("Status response: ${statusResponse.status}")
                assertEquals(HttpStatusCode.OK, statusResponse.status)
                
                val status = statusResponse.bodyAsText().trim('"')
                testLogger.info("Status value: $status")
                assertEquals("UP", status)
            }
        } catch (e: Exception) {
            testLogger.error("Test failed", e)
            throw e
        }
    }
}
