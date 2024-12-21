package com.example.kmp.weather

import com.example.kmp.messaging.EventProducer
import com.example.kmp.monitoring.MetricsRegistry
import com.example.kmp.monitoring.configureMonitoring
import com.example.kmp.networking.configureNetworking
import com.example.kmp.networking.configureServiceDiscovery
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.example.kmp.weather.config.WeatherConfig
import com.example.kmp.weather.service.WeatherService
import kotlinx.coroutines.*

fun main() {
    embeddedServer(
        Netty,
        port = WeatherConfig.port,
        host = WeatherConfig.host
    ) { weatherModule() }.start(wait = true)
}

fun Application.weatherModule() {
    // Configure content negotiation
    install(ContentNegotiation) {
        json()
    }

    // Configure shared infrastructure components
    configureMonitoring()
    configureNetworking()

    // Initialize services
    val weatherService = WeatherService()
    
    // Start continuous weather monitoring in a coroutine scope
    val monitoringScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    monitoringScope.launch {
        weatherService.startMonitoring()
    }
    
    // Configure service discovery (adds health endpoint)
    configureServiceDiscovery(WeatherConfig.serviceConfig)
    
    routing {
        get("/api/v1/weather/{city}") {
            val city = call.parameters["city"] ?: throw IllegalArgumentException("City parameter is required")
            try {
                call.respond(weatherService.getWeather(city))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        get("/metrics") {
            // Metrics endpoint is automatically configured by the monitoring module
            call.respond(HttpStatusCode.OK)
        }
    }
}
