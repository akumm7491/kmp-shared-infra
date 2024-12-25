package com.example.kmp.networking.models

/**
 * Configuration model for service registration and discovery
 *
 * @property serviceName Name of the service to be registered
 * @property serviceUrl URL where the service is accessible
 * @property registryUrl URL of the Eureka service registry
 * @property configServerUrl URL of the Spring Cloud Config server
 * @property instanceId Unique identifier for this service instance (defaults to serviceName:port)
 * @property vipAddress Virtual IP address for service discovery (defaults to serviceName)
 * @property secureVipAddress Secure Virtual IP address for service discovery (defaults to serviceName)
 * @property preferIpAddress Whether to prefer IP address over hostname (defaults to true)
 */
data class ServiceConfig(
    val serviceName: String,
    val serviceUrl: String,
    val registryUrl: String,
    val configServerUrl: String,
    val instanceId: String = "$serviceName:${serviceUrl.substringAfterLast(":")}",
    val vipAddress: String = serviceName,
    val secureVipAddress: String = serviceName,
    val preferIpAddress: Boolean = true
)
