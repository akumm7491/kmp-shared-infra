package com.example.kmp.monitoring

/**
 * Generic metrics interface that can be implemented for any platform
 */
interface MetricsProvider {
    fun incrementCounter(name: String, tags: Map<String, String> = emptyMap())
    fun recordGauge(name: String, value: Double, tags: Map<String, String> = emptyMap())
    fun recordTimer(name: String, durationMs: Double, tags: Map<String, String> = emptyMap())
    fun getMetrics(): Map<String, Any>
}

/**
 * Generic logging interface that can be implemented for any platform
 */
interface LogProvider {
    fun info(message: String, metadata: Map<String, Any> = emptyMap())
    fun warn(message: String, metadata: Map<String, Any> = emptyMap())
    fun error(message: String, error: Throwable? = null, metadata: Map<String, Any> = emptyMap())
    fun debug(message: String, metadata: Map<String, Any> = emptyMap())
}

/**
 * Factory interface for creating monitoring components
 * Each platform can provide its own implementation
 */
interface MonitoringFactory {
    fun createMetricsProvider(): MetricsProvider
    fun createLogProvider(serviceName: String): LogProvider
}
