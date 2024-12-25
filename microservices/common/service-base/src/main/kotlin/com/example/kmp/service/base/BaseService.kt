package com.example.kmp.service.base

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import com.example.kmp.monitoring.MonitoringFactory
import com.example.kmp.monitoring.LogProvider
import kotlinx.serialization.json.Json

abstract class BaseService(
    private val projectId: String,
    private val serviceName: String
) {
    private lateinit var logger: LogProvider

    fun Application.configureBase() {
        // Initialize monitoring and logging
        logger = MonitoringFactory.createLogProvider("$projectId-$serviceName")
        val metricsProvider = MonitoringFactory.createMetricsProvider()
        metricsProvider.install(this)

        // Load project-specific config
        val config = ConfigClient("http://config-server:8888")
            .loadConfig("$projectId-$serviceName", projectId)
        
        // Register with service discovery
        install(EurekaClient) {
            serviceName = "$projectId-$serviceName"
            serviceUrl = "http://service-registry:8761/eureka"
            metadata["project"] = projectId
        }

        // Configure content negotiation
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                isLenient = true
                prettyPrint = true
            })
        }

        // Configure error handling
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                logger.error("Unhandled error: ${cause.message}", cause, mapOf(
                    "projectId" to projectId,
                    "serviceName" to serviceName
                ))
                call.respond(HttpStatusCode.InternalServerError, mapOf(
                    "error" to (cause.message ?: "Internal Server Error"),
                    "type" to cause.javaClass.simpleName,
                    "timestamp" to System.currentTimeMillis()
                ))
            }
        }

        // Configure health check endpoint
        routing {
            get("/health") {
                call.respond(HttpStatusCode.OK, mapOf(
                    "status" to "UP",
                    "service" to "$projectId-$serviceName",
                    "timestamp" to System.currentTimeMillis()
                ))
            }

            get("/metrics") {
                call.respond(metricsProvider.getMetrics())
            }
        }

        // Log service startup
        environment.monitor.subscribe(ApplicationStarted) {
            logger.info("Service started", mapOf(
                "projectId" to projectId,
                "serviceName" to serviceName
            ))
        }

        environment.monitor.subscribe(ApplicationStopped) {
            logger.info("Service stopping", mapOf(
                "projectId" to projectId,
                "serviceName" to serviceName
            ))
        }
        
        // Configure custom service
        configureService(config)
    }
    
    // To be implemented by specific services
    abstract fun Application.configureService(config: Config)

    // Helper function for services to access logger
    protected fun getLogger(): LogProvider = logger
}
