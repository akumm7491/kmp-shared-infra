package com.example.kmp.service.registry

import com.example.kmp.service.registry.registry.DynamicServiceRegistry
import com.example.kmp.service.registry.routes.registerEurekaEndpoints
import com.example.kmp.service.registry.routes.registerServiceEndpoints
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import com.example.kmp.networking.configureServiceRegistry
import com.example.kmp.networking.models.ServiceRegistryConfig

fun main() {
    embeddedServer(Netty, port = 8761, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Configure Eureka server
    System.setProperty("eureka.client.register-with-eureka", "false")
    System.setProperty("eureka.client.fetch-registry", "false")
    System.setProperty("eureka.client.serviceUrl.defaultZone", "http://localhost:8761/eureka/")
    System.setProperty("eureka.server.enable-self-preservation", "false")
    System.setProperty("eureka.server.eviction-interval-timer-in-ms", "1000")

    configureServiceRegistry(ServiceRegistryConfig(
        port = 8761,
        enableSelfPreservation = false,
        renewalPercentThreshold = 0.85
    ))

    install(ContentNegotiation) {
        json()
    }

    // Create and configure the service registry
    val serviceRegistry = DynamicServiceRegistry()

    // Configure routing
    routing {
        registerServiceEndpoints(serviceRegistry)
        registerEurekaEndpoints(serviceRegistry)
    }
}
