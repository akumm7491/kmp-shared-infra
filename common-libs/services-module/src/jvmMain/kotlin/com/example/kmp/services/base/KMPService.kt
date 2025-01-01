package com.example.kmp.services.base

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.http.*
import com.example.kmp.monitoring.*
import com.example.kmp.networking.configureServiceDiscovery
import com.example.kmp.networking.configureServiceRegistry
import com.example.kmp.networking.models.ServiceConfig
import com.example.kmp.networking.models.ServiceRegistryConfig
import com.example.kmp.auth.*
import com.example.kmp.messaging.*
import com.example.kmp.storage.*
import kotlinx.serialization.json.Json
import com.example.kmp.services.events.HealthStatus

/**
 * Core abstraction for KMP microservices that provides a standardized way to configure
 * and initialize services with all necessary infrastructure components.
 */
abstract class KMPService(
    private val projectId: String,
    private val serviceName: String,
    configuration: KMPServiceConfiguration.() -> Unit = {},
    private val isServiceRegistry: Boolean = false
) {
    private val config = KMPServiceConfiguration().apply(configuration)
    
    protected var logger: LogProvider = KtorMonitoringFactory.createLogProvider("$projectId-$serviceName-init")
    protected var authProvider: AuthProvider? = null
    protected var messageBroker: MessageBroker? = null
    protected var storageProvider: StorageProvider? = null
    
    fun start(port: Int = 8080) {
        embeddedServer(Netty, port = port) {
            configureApplication()
        }.start(wait = true)
    }

    protected fun Application.configureApplication() {
        // Initialize core infrastructure using Ktor-specific factory
        logger = KtorMonitoringFactory.createLogProvider("$projectId-$serviceName")
        val metricsProvider = KtorMonitoringFactory.createMetricsProvider() as KtorMetricsProvider

        // Configure standard plugins
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
                encodeDefaults = true
                useArrayPolymorphism = true
            })
        }

        // Set up monitoring
        metricsProvider.install(this)
        
        // Configure service discovery or registry based on role
        if (isServiceRegistry) {
            configureServiceRegistry(ServiceRegistryConfig(
                port = environment.config.port,
                enableSelfPreservation = false,
                renewalPercentThreshold = 0.85
            ))
        } else if (config.serviceDiscovery) {
            configureServiceDiscovery(ServiceConfig(
                serviceName = serviceName,
                serviceUrl = "http://localhost:${environment.config.port}",
                registryUrl = "http://localhost:8761",
                configServerUrl = "http://localhost:8888",
                instanceId = "$serviceName-${System.currentTimeMillis()}",
                vipAddress = serviceName,
                secureVipAddress = serviceName,
                preferIpAddress = true
            ))
        }

        // Configure authentication if enabled
        config.auth?.let { auth ->
            authProvider = AuthFactory.createAuthProvider(AuthConfig(
                authServerUrl = auth.authServerUrl ?: "http://localhost:8081",
                clientId = auth.clientId ?: serviceName,
                clientSecret = auth.clientSecret ?: "secret",
                authMethods = auth.authMethods
            ))
        }

        // Configure messaging if enabled
        if (config.messaging) {
            messageBroker = MessagingFactory.createMessageBroker(MessagingConfig(
                brokerUrl = "localhost:9092",
                clientId = serviceName
            ))
        }

        // Configure storage if enabled
        config.storage?.let { storage ->
            storageProvider = StorageFactory.createStorageProvider(StorageConfig(
                type = storage.storageType,
                connectionString = storage.connectionString ?: "localhost:27017"
            ))
        }

        // Configure all routing in a single block
        install(Routing) {
            // Core endpoints (must be registered first)
            get("/health") {
                logger.info("Health check requested", mapOf(
                    "service" to "$projectId-$serviceName",
                    "status" to "UP"
                ))
                call.respond(HttpStatusCode.OK, HealthStatus(
                    status = "UP",
                    service = "$projectId-$serviceName",
                    timestamp = System.currentTimeMillis()
                ))
            }

            get("/metrics") {
                call.respond(metricsProvider.getMetrics())
            }

            // Service-specific routes
            configureRoutes()
        }

        // Log service startup
        environment.monitor.subscribe(ApplicationStarted) {
            logger.info("Service started", mapOf(
                "projectId" to projectId,
                "serviceName" to serviceName,
                "port" to environment.config.port
            ))
        }

        // Configure service-specific logic
        configureService()
    }

    /**
     * Override this function to configure service-specific logic
     */
    abstract fun Application.configureService()

    /**
     * Override this function to configure service routes
     */
    protected open fun Routing.configureRoutes() {
        // Default empty implementation
    }

    /**
     * Helper function for services to access logger
     */
    protected fun log() = logger
}

/**
 * Configuration class for KMP services that allows enabling/disabling features
 * and configuring their behavior
 */
class KMPServiceConfiguration {
    var serviceDiscovery: Boolean = true
    var auth: AuthConfiguration? = null
    var messaging: Boolean = false
    var storage: StorageConfiguration? = null
    
    fun withAuth(configuration: AuthConfiguration.() -> Unit) {
        auth = AuthConfiguration().apply(configuration)
    }
    
    fun withStorage(configuration: StorageConfiguration.() -> Unit) {
        storage = StorageConfiguration().apply(configuration)
    }
}

/**
 * Configuration classes for different service aspects
 */
class AuthConfiguration {
    var jwt: Boolean = false
    var oauth: Boolean = false
    var sessionAuth: Boolean = false
    var authServerUrl: String? = null
    var clientId: String? = null
    var clientSecret: String? = null
    var authMethods: Set<AuthMethod> = setOf(AuthMethod.JWT)
}

class StorageConfiguration {
    var storageType: StorageType = StorageType.DATABASE
    var connectionString: String? = null
    var cache: Boolean = false
    var persistence: Boolean = true
}
