package com.example.kmp.service.registry.test

import com.example.kmp.testing.service.KMPServiceTestBase
import com.example.kmp.testing.config.KMPTestConfig
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.ExperimentalSerializationApi
import com.example.kmp.service.registry.model.ServiceInstance
import com.example.kmp.service.registry.registry.DynamicServiceRegistry
import com.example.kmp.networking.configureServiceRegistry
import com.example.kmp.networking.models.ServiceRegistryConfig
import com.example.kmp.monitoring.KtorMonitoring
import kotlinx.serialization.modules.SerializersModule
import io.ktor.server.routing.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import com.example.kmp.service.registry.routes.registerServiceEndpoints

@OptIn(ExperimentalSerializationApi::class)
abstract class ServiceRegistryTestBase : KMPServiceTestBase() {
    private val testConfig = ServiceRegistryTestConfig()
    protected val serviceRegistry = DynamicServiceRegistry()

    override val config: KMPTestConfig
        get() = testConfig

    override val json = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        coerceInputValues = true
        serializersModule = SerializersModule {
            contextual(ServiceInstance::class, ServiceInstance.serializer())
        }
    }

    @BeforeEach
    override fun setupTestCase(testInfo: TestInfo) {
        super.setupTestCase(testInfo)
        configureTestEnvironment()
    }

    override fun configureTestEnvironment() {
        super.configureTestEnvironment()
        // Configure test environment properties
        val testPort = findAvailablePort()
        System.setProperty("ktor.deployment.port", testPort.toString())
        System.setProperty("ktor.deployment.host", "127.0.0.1")
    }

    override fun cleanupTestEnvironment() {
        super.cleanupTestEnvironment()
        serviceRegistry.clear()
    }

    override suspend fun configureService(): suspend Application.() -> Unit = {
        install(KtorMonitoring) {
            metricsPath = "/metrics"
            onError = { error -> 
                testLogger.error("Server error", error)
            }
        }

        install(ContentNegotiation) {
            json(json)
        }
        
        // Configure service registry with test settings
        configureServiceRegistry(ServiceRegistryConfig(
            port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt() 
                ?: throw IllegalStateException("Test port not configured"),
            enableSelfPreservation = false,
            renewalPercentThreshold = 0.85
        ))
        
        routing {
            serviceRegistry.apply {
                registerServiceEndpoints(this)
            }
        }
    }

    protected suspend fun withTestServer(block: suspend ApplicationTestBuilder.() -> Unit) {
        withService(block)
    }
}
