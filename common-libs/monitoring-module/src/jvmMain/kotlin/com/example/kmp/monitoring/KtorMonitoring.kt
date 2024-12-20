package com.example.kmp.monitoring

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.LoggerFactory
import java.time.Instant

class KtorMetricsProvider(
    private val registry: PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
) : MetricsProvider {
    override fun incrementCounter(name: String, tags: Map<String, String>) {
        registry.counter(name, tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) })
            .increment()
    }

    override fun recordGauge(name: String, value: Double, tags: Map<String, String>) {
        registry.gauge(name, 
            tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) },
            value)
    }

    override fun recordTimer(name: String, durationMs: Double, tags: Map<String, String>) {
        registry.timer(name, tags.map { (k, v) -> io.micrometer.core.instrument.Tag.of(k, v) })
            .record(java.time.Duration.ofMillis(durationMs.toLong()))
    }

    fun install(application: Application) {
        application.install(MicrometerMetrics) {
            registry = this@KtorMetricsProvider.registry
            meterBinders = listOf()
        }

        application.install(ContentNegotiation) {
            json()
        }

        application.routing {
            get("/metrics") {
                call.respond(registry.scrape())
            }

            get("/health") {
                // We can extend this later to include component health checks
                call.respond(mapOf(
                    "status" to "UP",
                    "timestamp" to System.currentTimeMillis(),
                    "components" to mapOf(
                        "service" to "UP"
                    )
                ))
            }
        }
    }
}

class KtorLogProvider(
    private val serviceName: String,
    private val logger: org.slf4j.Logger = LoggerFactory.getLogger(KtorLogProvider::class.java)
) : LogProvider {
    private fun enrichMetadata(metadata: Map<String, Any>): Map<String, Any> {
        return metadata + mapOf(
            "service" to serviceName,
            "timestamp" to Instant.now().toString()
        )
    }

    override fun info(message: String, metadata: Map<String, Any>) {
        logger.info("$message ${enrichMetadata(metadata)}")
    }

    override fun warn(message: String, metadata: Map<String, Any>) {
        logger.warn("$message ${enrichMetadata(metadata)}")
    }

    override fun error(message: String, error: Throwable?, metadata: Map<String, Any>) {
        logger.error("$message ${enrichMetadata(metadata)}", error)
    }

    override fun debug(message: String, metadata: Map<String, Any>) {
        logger.debug("$message ${enrichMetadata(metadata)}")
    }
}

object MonitoringFactory {
    fun createMetricsProvider(): KtorMetricsProvider {
        return KtorMetricsProvider()
    }

    fun createLogProvider(serviceName: String): LogProvider {
        return KtorLogProvider(serviceName)
    }
}
