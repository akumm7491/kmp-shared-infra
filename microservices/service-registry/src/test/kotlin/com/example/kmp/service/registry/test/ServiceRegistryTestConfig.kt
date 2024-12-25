package com.example.kmp.service.registry.test

import com.example.kmp.testing.config.KMPTestConfig
import io.ktor.server.config.*
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import org.slf4j.LoggerFactory

open class ServiceRegistryTestConfig : KMPTestConfig() {
    private val logger = LoggerFactory.getLogger(javaClass)
    private var configMap = mutableMapOf<String, Any>()

    override val applicationConfig: ApplicationConfig
        get() = HoconApplicationConfig(buildConfig())

    init {
        // Set Ktor deployment properties
        setProperty("ktor.deployment.port", 8761)
        setProperty("ktor.deployment.host", "0.0.0.0")
        
        // Set service registry specific properties
        setProperty("service-registry.name", "test-registry")
        setProperty("service-registry.host", "0.0.0.0")
        setProperty("service-registry.port", 8761)
        
        // Configure test mode
        setProperty("test.enabled", true)
        setProperty("test.auth.enabled", false)
        setProperty("test.storage.type", "memory")
        setProperty("test.messaging.type", "memory")
        
        // Configure Eureka client properties
        setProperty("eureka.client.register-with-eureka", false)
        setProperty("eureka.client.fetch-registry", false)
        setProperty("eureka.client.serviceUrl.defaultZone", "http://localhost:8761/eureka/")
        setProperty("eureka.server.enable-self-preservation", false)
        setProperty("eureka.server.eviction-interval-timer-in-ms", 1000)
        
        // Configure service registry
        setProperty("service-registry.config.port", 8761)
        setProperty("service-registry.config.enableSelfPreservation", false)
        setProperty("service-registry.config.renewalPercentThreshold", 0.85)
        
        // Configure network settings
        setProperty("ktor.network.host", "0.0.0.0")
        setProperty("ktor.network.port", 8761)
        setProperty("ktor.network.requestQueueSize", 16)
        setProperty("ktor.network.responseWriteTimeoutSeconds", 10)
        
        // Configure development mode for better error messages
        setProperty("ktor.development", true)
        setProperty("ktor.deployment.watch", listOf("classes", "resources"))
        
        // Configure serialization
        setProperty("ktor.serialization.json.prettyPrint", true)
        setProperty("ktor.serialization.json.ignoreUnknownKeys", true)
        
        // Load additional properties from test config file
        try {
            val baseConfig = ConfigFactory.load("application-test.conf")
            baseConfig.entrySet().forEach { entry ->
                try {
                    val key = entry.key
                    val value = entry.value.unwrapped()
                    configMap[key] = value
                    logger.debug("Loaded property $key = $value")
                } catch (e: Exception) {
                    logger.error("Failed to read property ${entry.key}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            logger.warn("No application-test.conf found, using default test configuration")
        }
    }

    override fun getRequiredProperties(): List<String> = listOf(
        "ktor.deployment.port",
        "ktor.deployment.host",
        "service-registry.name",
        "service-registry.host",
        "service-registry.port"
    )

    fun setProperty(key: String, value: Any) {
        configMap[key] = value
        logger.debug("Set property $key = $value")
    }

    private fun buildConfig(): com.typesafe.config.Config {
        var config = ConfigFactory.empty()
        configMap.forEach { (key, value) ->
            config = config.withValue(key, ConfigValueFactory.fromAnyRef(value))
        }
        return config
    }
}
