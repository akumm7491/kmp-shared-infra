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
    protected lateinit var logger: LogProvider
    protected var authProvider: AuthProvider? = null
    protected var messageBroker: MessageBroker? = null
    protected var storageProvider: StorageProvider? = null
    
    fun start(port: Int = 8080) {
        embeddedServer(Netty, port = port) {
            configureService()
        }.start(wait = true)
    }

    private fun Application.configureService() {
        // Initialize core infrastructure using Ktor-specific factory
        logger = KtorMonitoringFactory.createLogProvider("$projectId-$serviceName")
        val metricsProvider = KtorMonitoringFactory.createMetricsProvider() as KtorMetricsProvider

        // Configure standard plugins
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        // Set up monitoring
        metricsProvider.install(this)
        
        // Configure service discovery or registry based on role
        if (isServiceRegistry) {
            configureServiceRegistry(ServiceRegistryConfig(
                port = environment.config.port,
                enableSelfPreservation = false, // Disabled for development
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

        // Set up standard endpoints
        routing {
            get("/health") {
                call.respond(mapOf(
                    "status" to "UP",
                    "service" to "$projectId-$serviceName",
                    "timestamp" to System.currentTimeMillis()
                ))
            }

            get("/metrics") {
                call.respond(metricsProvider.getMetrics())
            }
        }

        // Log service startup
        environment.monitor.subscribe(ApplicationStarted) {
            logger.info("Service started", mapOf(
                "projectId" to projectId,
                "serviceName" to serviceName,
                "port" to environment.config.port
            ))
        }

        // Configure custom service logic
        configureCustomService()
    }

    /**
     * Override this function to configure custom service-specific logic
     */
    abstract fun Application.configureCustomService()

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
