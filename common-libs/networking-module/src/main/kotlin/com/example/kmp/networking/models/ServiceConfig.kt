package com.example.kmp.networking.models

/**
 * Configuration model for service registration and discovery
 *
 * @property serviceName Name of the service to be registered
 * @property serviceUrl URL where the service is accessible
 * @property registryUrl URL of the Eureka service registry
 * @property configServerUrl URL of the Spring Cloud Config server
 */
data class ServiceConfig(
    val serviceName: String,
    val serviceUrl: String,
    val registryUrl: String,
    val configServerUrl: String
)
