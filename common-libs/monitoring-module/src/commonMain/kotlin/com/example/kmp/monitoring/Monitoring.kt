package com.example.kmp.monitoring

interface MetricsProvider {
    fun incrementCounter(name: String, tags: Map<String, String> = emptyMap())
    fun recordGauge(name: String, value: Double, tags: Map<String, String> = emptyMap())
    fun recordTimer(name: String, durationMs: Double, tags: Map<String, String> = emptyMap())
}

interface LogProvider {
    fun info(message: String, metadata: Map<String, Any> = emptyMap())
    fun warn(message: String, metadata: Map<String, Any> = emptyMap())
    fun error(message: String, error: Throwable? = null, metadata: Map<String, Any> = emptyMap())
    fun debug(message: String, metadata: Map<String, Any> = emptyMap())
}
