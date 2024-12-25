package com.example.kmp.networking

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import com.example.kmp.networking.models.ServiceConfig
import com.netflix.discovery.EurekaClient
import com.netflix.discovery.DiscoveryClient
import com.netflix.discovery.DefaultEurekaClientConfig
import com.netflix.appinfo.ApplicationInfoManager
import com.netflix.appinfo.MyDataCenterInstanceConfig
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider

/**
 * Extension function to configure service discovery and configuration for a Ktor application
 */
fun Application.configureServiceDiscovery(serviceConfig: ServiceConfig) {
    // Configure Eureka client
    System.setProperty("eureka.name", serviceConfig.serviceName)
    System.setProperty("eureka.registration.enabled", "true")
    System.setProperty("eureka.port", serviceConfig.serviceUrl.substringAfterLast(":"))
    System.setProperty("eureka.serviceUrl.default", serviceConfig.registryUrl)

    // Create Eureka instance configuration
    val instanceConfig = MyDataCenterInstanceConfig()
    val instanceInfoProvider = EurekaConfigBasedInstanceInfoProvider(instanceConfig)
    val instanceInfo = instanceInfoProvider.get()
    val applicationInfoManager = ApplicationInfoManager(instanceConfig, instanceInfo)
    
    // Create and initialize Eureka client
    val eurekaClient: EurekaClient = DiscoveryClient(
        applicationInfoManager,
        DefaultEurekaClientConfig()
    )

    // Register shutdown hook
    environment.monitor.subscribe(ApplicationStopping) {
        eurekaClient.shutdown()
    }

    // Register health check endpoint
    routing {
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }
    }

    // Log service configuration
    log.info("Service ${serviceConfig.serviceName} starting with configuration:")
    log.info("- Service URL: ${serviceConfig.serviceUrl}")
    log.info("- Registry URL: ${serviceConfig.registryUrl}")
    log.info("- Config Server URL: ${serviceConfig.configServerUrl}")
}
