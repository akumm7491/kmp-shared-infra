package com.example.kmp.monitoring

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.ktor.server.metrics.micrometer.*
import io.micrometer.core.instrument.binder.jvm.*
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("MonitoringService")

fun main() {
    embeddedServer(Netty, port = 9090) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    logger.info("Initializing monitoring service")
    
    val prometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    
    install(MicrometerMetrics) {
        registry = prometheusMeterRegistry
        // Configure metrics
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            JvmThreadMetrics(),
            ClassLoaderMetrics(),
            ProcessorMetrics(),
            FileDescriptorMetrics()
        )
    }
    
    routing {
        get("/metrics") {
            logger.debug("Scraping metrics")
            call.respondText(
                text = prometheusMeterRegistry.scrape(),
                contentType = ContentType.parse("text/plain")
            )
        }
        
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }
    }
    
    logger.info("Monitoring service initialized")
}
