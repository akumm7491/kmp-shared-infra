package com.example.kmp.template

import com.example.kmp.template.config.AppConfig
import com.example.kmp.template.di.serviceModule
import com.example.kmp.template.routes.configureApiRoutes
import com.example.kmp.di.configureInfrastructure
import com.example.kmp.di.InfrastructureConfig
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(
        Netty,
        port = AppConfig.PORT,
        host = AppConfig.HOST
    ) { templateModule() }.start(wait = true)
}

fun Application.templateModule() {
    // Configure infrastructure with our service-specific config
    configureInfrastructure(
        serviceName = AppConfig.SERVICE_NAME,
        config = InfrastructureConfig(
            storage = InfrastructureConfig.StorageConfig(
                connectionString = AppConfig.Storage.CONNECTION_STRING
            ),
            messaging = InfrastructureConfig.MessagingConfig(
                brokerUrl = AppConfig.Messaging.BROKER_URL
            ),
            auth = InfrastructureConfig.AuthConfig(
                serverUrl = AppConfig.Auth.SERVER_URL,
                clientSecret = AppConfig.Auth.CLIENT_SECRET
            )
        ),
        extraModules = listOf(serviceModule)
    )

    // Configure API routes
    routing {
        configureApiRoutes()
    }
}
