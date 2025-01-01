package com.example.kmp.template

import com.example.kmp.monitoring.configureMonitoring
import com.example.kmp.networking.configureNetworking
import com.example.kmp.networking.configureServiceDiscovery
import com.example.kmp.template.config.AppConfig
import com.example.kmp.template.di.serviceModule
import com.example.kmp.template.routes.configureApiRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import org.koin.core.logger.Level
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger

fun main() {
    embeddedServer(
        Netty,
        port = AppConfig.PORT,
        host = AppConfig.HOST
    ) { templateModule() }.start(wait = true)
}

fun Application.templateModule() {
    // Configure content negotiation
    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
            allowSpecialFloatingPointValues = true
        })
    }

    // Install Koin for dependency injection
    install(Koin) {
        SLF4JLogger(Level.INFO)
        modules(serviceModule)
    }

    // Configure shared infrastructure components
    configureMonitoring()
    configureNetworking()
    configureServiceDiscovery(AppConfig.Registry.serviceConfig)

    // Configure API routes
    routing {
        configureApiRoutes()
    }
}
