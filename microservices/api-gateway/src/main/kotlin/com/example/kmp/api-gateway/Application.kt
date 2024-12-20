package com.example.kmp.api.gateway

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.*
import io.ktor.http.*
import com.example.kmp.api.gateway.discovery.EurekaServiceDiscovery
import com.example.kmp.api.gateway.client.HttpServiceClient

fun main() {
    embeddedServer(Netty, port = 8000) {
        module()
    }.start(wait = true)
}

fun Application.module() {
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
    val serviceDiscovery = EurekaServiceDiscovery(eurekaClient)
    val serviceClient = HttpServiceClient(httpClient)
    
    // Configure routing
    configureServiceRouting(
        serviceDiscovery = serviceDiscovery,
        serviceClient = serviceClient
    )
}

fun Application.configureRouting() {
    routing {
        // Routes will be configured based on service discovery
    }
} 