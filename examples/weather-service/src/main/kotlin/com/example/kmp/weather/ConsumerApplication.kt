package com.example.kmp.weather

import com.example.kmp.monitoring.MetricsRegistry
import com.example.kmp.monitoring.configureMonitoring
import com.example.kmp.networking.configureNetworking
import com.example.kmp.networking.configureServiceDiscovery
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.kmp.weather.config.WeatherConfig
import com.example.kmp.weather.service.WeatherConsumerService
import io.ktor.http.*
import kotlinx.coroutines.*

fun main() {
    // Start the consumer service in a coroutine scope
    val consumerService = WeatherConsumerService()
    val consumerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    consumerScope.launch {
        consumerService.start()
    }
    
    // Start the web server for health checks and metrics
    embeddedServer(
        Netty,
        port = WeatherConfig.consumerPort,
        host = WeatherConfig.host
    ) { consumerModule() }.start(wait = true)
}

fun Application.consumerModule() {
    // Configure shared infrastructure components
    configureMonitoring()
    configureNetworking()
    
    // Configure service discovery (adds health endpoint)
    configureServiceDiscovery(WeatherConfig.consumerServiceConfig)
    
    routing {
        get("/metrics") {
            // Metrics endpoint is automatically configured by the monitoring module
            call.respond(HttpStatusCode.OK)
        }
    }
}
