package com.example.kmp.weather

import com.example.kmp.messaging.EventProducer
import com.example.kmp.messaging.schema.SchemaRegistrationExtension
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
    // Initialize schema registration
    val schemaRegistryUrl = System.getenv("SCHEMA_REGISTRY_URL") ?: "http://schema-registry.kafka:8081"
    SchemaRegistrationExtension.initializeAndRegisterSchemas(
        schemaRegistryUrl = schemaRegistryUrl,
        basePackage = "com.example.kmp.weather.model"
    )

    embeddedServer(
        Netty,
        port = WeatherConfig.port,
        host = WeatherConfig.host
    ) { weatherModule() }.start(wait = true)
}

fun Application.weatherModule() {
    // Configure content negotiation with consistent JSON settings
    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
        })
    }

    // Configure shared infrastructure components
    configureMonitoring()
    configureNetworking()
    configureServiceDiscovery(WeatherConfig.serviceConfig)

    // Initialize services
    val weatherService = WeatherService()
    
    // Start continuous weather monitoring in a coroutine scope
    val monitoringScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    monitoringScope.launch {
        weatherService.startMonitoring()
    }
    
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
            call.respond(MetricsRegistry.getRegistry().scrape())
        }
    }
}
