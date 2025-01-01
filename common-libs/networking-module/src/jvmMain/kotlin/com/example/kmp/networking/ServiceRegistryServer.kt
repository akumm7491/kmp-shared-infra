package com.example.kmp.networking

import io.ktor.server.application.*
import com.example.kmp.networking.models.ServiceRegistryConfig
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ServiceRegistryServer")

fun Application.configureServiceRegistry(config: ServiceRegistryConfig) {
    logger.info("Configuring Service Registry with port: ${config.port}")
    
    // Configure basic service registry settings
    environment.monitor.subscribe(ApplicationStarted) {
        logger.info("Service Registry started successfully")
    }

    environment.monitor.subscribe(ApplicationStopping) {
        logger.info("Service Registry shutting down")
    }
}
