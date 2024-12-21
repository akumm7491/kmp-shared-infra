package com.example.kmp.monitoring

import io.ktor.server.application.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.ktor.server.metrics.micrometer.*

fun Application.configureMonitoring() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
    }
    MetricsRegistry.initialize(appMicrometerRegistry)
}
