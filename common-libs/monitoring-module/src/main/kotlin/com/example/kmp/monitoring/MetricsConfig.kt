package com.example.kmp.monitoring

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

object MetricsConfig {
    private val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    fun Application.configureMetrics(
        serviceName: String,
        environment: String,
        additionalTags: List<Tag> = emptyList()
    ) {
        // Add common tags
        val commonTags = listOf(
            Tag.of("service", serviceName),
            Tag.of("environment", environment)
        ) + additionalTags
        
        registry.config().commonTags(commonTags)

        // Install micrometer plugin
        install(MicrometerMetrics) {
            registry = this@MetricsConfig.registry
            // Enable JVM metrics
            JvmMemoryMetrics().bindTo(registry)
            JvmGcMetrics().bindTo(registry)
            ProcessorMetrics().bindTo(registry)
        }

        // Configure metrics endpoint
        routing {
            get("/metrics") {
                call.respond(registry.scrape())
            }
        }
    }

    // Get registry for custom metrics
    fun getMeterRegistry(): MeterRegistry = registry
}

// Extension functions for common metrics
fun MeterRegistry.recordLatency(name: String, tags: List<Tag> = emptyList(), latencyMs: Long) {
    timer(name, tags).record(java.time.Duration.ofMillis(latencyMs))
}

fun MeterRegistry.incrementCounter(name: String, tags: List<Tag> = emptyList()) {
    counter(name, tags).increment()
}
