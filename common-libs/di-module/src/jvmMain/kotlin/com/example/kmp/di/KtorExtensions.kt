package com.example.kmp.di

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import org.koin.core.logger.Level
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger
import kotlinx.serialization.json.Json
import com.example.kmp.monitoring.configureMonitoring
import com.example.kmp.networking.configureNetworking
import com.example.kmp.networking.configureServiceDiscovery
import com.example.kmp.networking.models.ServiceConfig

/**
 * Creates a default service registry configuration based on service name and environment
 */
private fun createDefaultServiceConfig(
    serviceName: String,
    application: Application
): ServiceConfig {
    val port = application.environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt() ?: 8080
    val registryUrl = System.getenv("REGISTRY_URL") ?: "http://service-registry:8761"
    val configServerUrl = System.getenv("CONFIG_SERVER_URL") ?: "http://config-server:8888"
    val host = System.getenv("HOST") ?: "localhost"
    
    return ServiceConfig(
        serviceName = serviceName,
        serviceUrl = "http://$host:$port",
        registryUrl = registryUrl,
        configServerUrl = configServerUrl,
        instanceId = "$serviceName-${System.currentTimeMillis()}",
        vipAddress = serviceName,
        secureVipAddress = serviceName,
        preferIpAddress = true
    )
}

/**
 * Extension function to configure standard Ktor infrastructure with Koin DI
 */
fun Application.configureInfrastructure(
    serviceName: String,
    config: InfrastructureConfig = InfrastructureConfig(),
    serviceRegistryConfig: ServiceConfig? = null,
    extraModules: List<org.koin.core.module.Module> = emptyList()
) {
    // Initialize infrastructure factories
    InfrastructureModule.initialize()

    // Configure content negotiation
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
            prettyPrint = true
        })
    }

    // Install Koin with infrastructure module
    install(Koin) {
        SLF4JLogger(Level.INFO)
        modules(InfrastructureModule.create(serviceName, config))
        modules(extraModules)
    }

    // Configure shared infrastructure components
    configureMonitoring()
    configureNetworking()
    
    // Use provided config or create a default one
    val registryConfig = serviceRegistryConfig ?: createDefaultServiceConfig(serviceName, this)
    configureServiceDiscovery(registryConfig)
}

/**
 * Extension function to configure infrastructure with custom JSON configuration
 */
fun Application.configureInfrastructure(
    serviceName: String,
    config: InfrastructureConfig = InfrastructureConfig(),
    serviceRegistryConfig: ServiceConfig? = null,
    extraModules: List<org.koin.core.module.Module> = emptyList(),
    jsonConfig: Json.() -> Unit
) {
    // Initialize infrastructure factories
    InfrastructureModule.initialize()

    // Configure content negotiation with custom JSON config
    install(ContentNegotiation) {
        json(Json.Default.apply(jsonConfig))
    }

    // Install Koin with infrastructure module
    install(Koin) {
        SLF4JLogger(Level.INFO)
        modules(InfrastructureModule.create(serviceName, config))
        modules(extraModules)
    }

    // Configure shared infrastructure components
    configureMonitoring()
    configureNetworking()
    
    // Use provided config or create a default one
    val registryConfig = serviceRegistryConfig ?: createDefaultServiceConfig(serviceName, this)
    configureServiceDiscovery(registryConfig)
} 