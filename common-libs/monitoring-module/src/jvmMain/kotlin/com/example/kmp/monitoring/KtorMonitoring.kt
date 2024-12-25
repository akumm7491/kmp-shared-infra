package com.example.kmp.monitoring

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * Ktor-specific implementation of MetricsProvider using Micrometer
 */
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

    override fun getMetrics(): Map<String, Any> {
        return mapOf("metrics" to registry.scrape())
    }

    // Ktor-specific extension function
    fun install(application: Application) {
        application.install(MicrometerMetrics) {
            registry = this@KtorMetricsProvider.registry
        }
    }

    // Access to raw registry for Ktor-specific use cases
    fun getRegistry(): PrometheusMeterRegistry = registry
}

/**
 * Ktor-specific implementation of LogProvider using SLF4J
 */
class KtorLogProvider(
    private val serviceName: String,
    private val logger: org.slf4j.Logger = LoggerFactory.getLogger(serviceName)
) : LogProvider {
    private fun enrichMetadata(metadata: Map<String, Any>): Map<String, Any> {
        return metadata + mapOf(
            "service" to serviceName,
            "timestamp" to Instant.now().toString()
        )
    }

    override fun info(message: String, metadata: Map<String, Any>) {
        logger.info("$message ${formatMetadata(enrichMetadata(metadata))}")
    }

    override fun warn(message: String, metadata: Map<String, Any>) {
        logger.warn("$message ${formatMetadata(enrichMetadata(metadata))}")
    }

    override fun error(message: String, error: Throwable?, metadata: Map<String, Any>) {
        if (error != null) {
            logger.error("$message ${formatMetadata(enrichMetadata(metadata))}", error)
        } else {
            logger.error("$message ${formatMetadata(enrichMetadata(metadata))}")
        }
    }

    override fun debug(message: String, metadata: Map<String, Any>) {
        logger.debug("$message ${formatMetadata(enrichMetadata(metadata))}")
    }

    private fun formatMetadata(metadata: Map<String, Any>): String {
        if (metadata.isEmpty()) return ""
        return metadata.entries.joinToString(prefix = "[", postfix = "]") { "${it.key}=${it.value}" }
    }
}

/**
 * Ktor-specific implementation of MonitoringFactory
 */
object KtorMonitoringFactory : MonitoringFactory {
    private val metricsProvider by lazy { KtorMetricsProvider() }

    override fun createMetricsProvider(): MetricsProvider = metricsProvider

    override fun createLogProvider(serviceName: String): LogProvider = KtorLogProvider(serviceName)
}
