package com.example.kmp.monitoring

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.util.concurrent.ConcurrentHashMap

/**
 * Micrometer implementation of MetricsProvider
 */
class MicrometerMetricsProvider(
    private val registry: MeterRegistry = SimpleMeterRegistry()
) : MetricsProvider {
    private val metrics = ConcurrentHashMap<String, Any>()

    override fun incrementCounter(name: String, tags: Map<String, String>) {
        registry.counter(name, tags.map { (k, v) -> Tag.of(k, v) })
            .increment()
        updateMetrics(name, tags)
    }

    override fun recordGauge(name: String, value: Double, tags: Map<String, String>) {
        registry.gauge(name, tags.map { (k, v) -> Tag.of(k, v) }, value)
        updateMetrics(name, tags, value)
    }

    override fun recordTimer(name: String, durationMs: Double, tags: Map<String, String>) {
        registry.timer(name, tags.map { (k, v) -> Tag.of(k, v) })
            .record(java.time.Duration.ofMillis(durationMs.toLong()))
        updateMetrics(name, tags, durationMs)
    }

    override fun getMetrics(): Map<String, Any> = metrics.toMap()

    private fun updateMetrics(name: String, tags: Map<String, String>, value: Any? = null) {
        val key = if (tags.isEmpty()) name else "$name${tags.entries.joinToString(prefix = "[", postfix = "]")}"
        metrics[key] = value ?: (metrics[key] as? Number)?.let { it.toLong() + 1 } ?: 1L
    }
}

/**
 * Logback implementation of LogProvider
 */
class LogbackLogProvider(private val serviceName: String) : LogProvider {
    private val logger = org.slf4j.LoggerFactory.getLogger(serviceName)

    override fun info(message: String, metadata: Map<String, Any>) {
        logger.info(formatMessage(message, metadata))
    }

    override fun warn(message: String, metadata: Map<String, Any>) {
        logger.warn(formatMessage(message, metadata))
    }

    override fun error(message: String, error: Throwable?, metadata: Map<String, Any>) {
        logger.error(formatMessage(message, metadata), error)
    }

    override fun debug(message: String, metadata: Map<String, Any>) {
        logger.debug(formatMessage(message, metadata))
    }

    private fun formatMessage(message: String, metadata: Map<String, Any>): String {
        return if (metadata.isEmpty()) message
        else "$message ${metadata.entries.joinToString(prefix = "{", postfix = "}")}"
    }
}

/**
 * JVM implementation of MonitoringFactory
 */
class JvmMonitoringFactory : MonitoringFactory {
    override fun createMetricsProvider(): MetricsProvider {
        return MicrometerMetricsProvider()
    }

    override fun createLogProvider(serviceName: String): LogProvider {
        return LogbackLogProvider(serviceName)
    }
}
