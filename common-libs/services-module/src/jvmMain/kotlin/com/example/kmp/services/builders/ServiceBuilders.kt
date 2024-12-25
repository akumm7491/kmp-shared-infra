package com.example.kmp.services.builders

import com.example.kmp.services.base.KMPService
import com.example.kmp.services.base.KMPServiceConfiguration
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.http.*

/**
 * Builder functions for common service patterns
 */

/**
 * Creates a REST API service with standardized configuration
 */
abstract class RestApiService(
    projectId: String,
    serviceName: String,
    configuration: KMPServiceConfiguration.() -> Unit = {}
) : KMPService(projectId, serviceName) {
    init {
        KMPServiceConfiguration().apply {
            // Enable service discovery by default for API services
            serviceDiscovery = true
            
            // Enable authentication by default
            withAuth {
                jwt = true
            }
            
            // Apply custom configuration
            configuration()
        }
    }

    override fun Application.configureCustomService() {
        routing {
            // API versioning support
            route("/api") {
                route("/v1") {
                    configureV1Routes()
                }
            }
        }
    }

    abstract fun Route.configureV1Routes()
}

/**
 * Creates an event processing service with standardized configuration
 */
abstract class EventProcessorService(
    projectId: String,
    serviceName: String,
    configuration: KMPServiceConfiguration.() -> Unit = {}
) : KMPService(projectId, serviceName) {
    init {
        KMPServiceConfiguration().apply {
            // Enable messaging by default for event processors
            messaging = true
            
            // Enable service discovery for coordination
            serviceDiscovery = true
            
            // Apply custom configuration
            configuration()
        }
    }

    override fun Application.configureCustomService() {
        configureEventHandlers()
    }

    abstract fun Application.configureEventHandlers()
}

/**
 * Creates a data service with standardized configuration
 */
abstract class DataService(
    projectId: String,
    serviceName: String,
    configuration: KMPServiceConfiguration.() -> Unit = {}
) : KMPService(projectId, serviceName) {
    init {
        KMPServiceConfiguration().apply {
            // Enable storage by default
            withStorage {
                persistence = true
                cache = true
            }
            
            // Enable service discovery for data services
            serviceDiscovery = true
            
            // Apply custom configuration
            configuration()
        }
    }

    override fun Application.configureCustomService() {
        routing {
            route("/data") {
                configureDataRoutes()
            }
        }
    }

    abstract fun Route.configureDataRoutes()
}

/**
 * Creates an integration service for external system communication
 */
abstract class IntegrationService(
    projectId: String,
    serviceName: String,
    configuration: KMPServiceConfiguration.() -> Unit = {}
) : KMPService(projectId, serviceName) {
    init {
        KMPServiceConfiguration().apply {
            // Enable messaging for async communication
            messaging = true
            
            // Enable caching for external system responses
            withStorage {
                cache = true
            }
            
            // Enable service discovery
            serviceDiscovery = true
            
            // Apply custom configuration
            configuration()
        }
    }

    override fun Application.configureCustomService() {
        configureIntegrations()
    }

    abstract fun Application.configureIntegrations()
}

/**
 * Extension functions for common service configurations
 */

fun KMPServiceConfiguration.withCaching() {
    withStorage {
        cache = true
    }
}

fun KMPServiceConfiguration.withPersistence() {
    withStorage {
        persistence = true
    }
}

fun KMPServiceConfiguration.withJWT() {
    withAuth {
        jwt = true
    }
}

fun KMPServiceConfiguration.withOAuth() {
    withAuth {
        oauth = true
    }
}

fun KMPServiceConfiguration.withMessaging() {
    messaging = true
}
