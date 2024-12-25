package com.example.kmp.api.gateway

import io.ktor.server.application.*
import io.ktor.util.*
import kotlin.reflect.KClass
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*
import com.netflix.discovery.EurekaClient
import com.example.kmp.networking.HttpClientEngine
import com.example.kmp.api.gateway.discovery.EurekaServiceDiscovery
import com.example.kmp.api.gateway.client.HttpServiceClient
import com.example.kmp.networking.NetworkingFactory
import com.example.kmp.networking.configureServiceDiscovery
import com.example.kmp.networking.models.ServiceConfig
import com.example.kmp.monitoring.KtorMonitoringFactory
import com.example.kmp.api.gateway.routing.configureServiceRouting

fun main() {
    embeddedServer(Netty, port = 8000) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Initialize monitoring
    val logProvider = KtorMonitoringFactory.createLogProvider("api-gateway")
    val metricsProvider = KtorMonitoringFactory.createMetricsProvider()
    
    // Initialize networking
    val networking = NetworkingFactory.create {
        port = 8000
        host = "0.0.0.0"
        timeout = 30000
        retries = 3
        logging(logProvider)
        metrics(metricsProvider)
        clientEngine = HttpClientEngine.CIO
    }
    
    // Install networking features
    networking.install(this)
    
    // Configure service discovery
    configureServiceDiscovery(ServiceConfig(
        serviceName = "api-gateway",
        serviceUrl = "http://localhost:8000",
        registryUrl = "http://localhost:8761",
        configServerUrl = "http://localhost:8888"
    ))
    
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }
    
    // Setup dependencies
    val eurekaClientKey = AttributeKey<EurekaClient>("eureka-client")
    val eurekaClient = try {
        attributes[eurekaClientKey]
    } catch (e: IllegalStateException) {
        throw IllegalStateException("Eureka client not found in application attributes", e)
    }
    val serviceDiscovery = EurekaServiceDiscovery(eurekaClient)
    val serviceClient = HttpServiceClient(networking.client)
    
    // Configure routing
    configureServiceRouting(
        serviceDiscovery = serviceDiscovery,
        serviceClient = serviceClient
    )
}
