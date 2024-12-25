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
    System.setProperty("eureka.serviceUrl.default", "${serviceConfig.registryUrl}/eureka/")
    
    // Configure heartbeat and lease
    System.setProperty("eureka.heartbeat.enabled", "true")
    System.setProperty("eureka.instance.leaseRenewalIntervalInSeconds", "30")
    System.setProperty("eureka.instance.leaseExpirationDurationInSeconds", "90")
    
    // Configure instance
    System.setProperty("eureka.instance.preferIpAddress", serviceConfig.preferIpAddress.toString())
    System.setProperty("eureka.instance.instanceId", serviceConfig.instanceId)
    System.setProperty("eureka.instance.vipAddress", serviceConfig.vipAddress)
    System.setProperty("eureka.instance.secureVipAddress", serviceConfig.secureVipAddress)
    System.setProperty("eureka.instance.appname", serviceConfig.serviceName.uppercase())
    System.setProperty("eureka.instance.virtualHostName", serviceConfig.vipAddress)
    System.setProperty("eureka.instance.secureVirtualHostName", serviceConfig.secureVipAddress)

    // Create Eureka instance configuration with custom config
    val instanceConfig = object : MyDataCenterInstanceConfig() {
        override fun getLeaseRenewalIntervalInSeconds(): Int = 30
        override fun getLeaseExpirationDurationInSeconds(): Int = 90
        override fun getSecurePortEnabled(): Boolean = false
        override fun isNonSecurePortEnabled(): Boolean = true
        override fun getVirtualHostName(): String = serviceConfig.vipAddress
        override fun getSecureVirtualHostName(): String = serviceConfig.secureVipAddress
        override fun getAppname(): String = serviceConfig.serviceName.uppercase()
        override fun getInstanceId(): String = serviceConfig.instanceId
    }
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
