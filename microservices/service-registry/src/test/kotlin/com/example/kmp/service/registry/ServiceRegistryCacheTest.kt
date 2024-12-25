package com.example.kmp.service.registry

import com.example.kmp.service.registry.model.ServiceInstance
import com.example.kmp.service.registry.test.ServiceRegistryTestBase
import com.example.kmp.service.registry.test.ServiceRegistryTestConfig
import com.example.kmp.service.registry.test.ServiceRegistryTestData
import com.example.kmp.testing.config.KMPTestConfig
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class ServiceRegistryCacheTest : ServiceRegistryTestBase() {
    override val config: KMPTestConfig = ServiceRegistryTestConfig()

    @Test
    suspend fun `test service registration with caching`() {
        withTestServer {
            val instance = ServiceRegistryTestData.createTestInstance()
            
            val response = client.post("/services") {
                setBody(instance)
            }
            assertEquals(HttpStatusCode.Created, response.status)

            val getCachedResponse = client.get("/services/${instance.id}")
            assertEquals(HttpStatusCode.OK, getCachedResponse.status)
            
            val cachedInstance = getCachedResponse.bodyAsText().let { Json.decodeFromString<ServiceInstance>(it) }
            assertNotNull(cachedInstance)
            assertEquals(instance.serviceName, cachedInstance.serviceName)
        }
    }

    @Test
    suspend fun `test service deregistration with cache invalidation`() {
        withTestServer {
            val instance = ServiceRegistryTestData.createTestInstance()
            
            client.post("/services") {
                setBody(instance)
            }

            val deregisterResponse = client.delete("/services/${instance.id}")
            assertEquals(HttpStatusCode.OK, deregisterResponse.status)

            val getCachedResponse = client.get("/services/${instance.id}")
            assertEquals(HttpStatusCode.NotFound, getCachedResponse.status)
        }
    }

    @Test
    suspend fun `test service status update with cache update`() {
        withTestServer {
            val instance = ServiceRegistryTestData.createTestInstance()
            
            client.post("/services") {
                setBody(instance)
            }

            val updateResponse = client.put("/services/${instance.id}/status") {
                setBody("DOWN")
            }
            assertEquals(HttpStatusCode.OK, updateResponse.status)

            val getCachedResponse = client.get("/services/${instance.id}")
            assertEquals(HttpStatusCode.OK, getCachedResponse.status)
            
            val cachedInstance = getCachedResponse.bodyAsText().let { Json.decodeFromString<ServiceInstance>(it) }
            assertNotNull(cachedInstance)
            assertEquals("DOWN", cachedInstance.status)
        }
    }

    @Test
    suspend fun `test service heartbeat with cache update`() {
        withTestServer {
            val instance = ServiceRegistryTestData.createTestInstance()
            
            client.post("/services") {
                setBody(instance)
            }

            val heartbeatResponse = client.put("/services/${instance.id}/heartbeat")
            assertEquals(HttpStatusCode.OK, heartbeatResponse.status)

            val getCachedResponse = client.get("/services/${instance.id}")
            assertEquals(HttpStatusCode.OK, getCachedResponse.status)
            
            val cachedInstance = getCachedResponse.bodyAsText().let { Json.decodeFromString<ServiceInstance>(it) }
            assertNotNull(cachedInstance)
            assertTrue(cachedInstance.lastUpdated > instance.lastUpdated)
        }
    }
}
