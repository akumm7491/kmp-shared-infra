package com.example.kmp.monitoring

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.time.Duration
import java.util.concurrent.TimeUnit

class MetricsRegistry private constructor() {
    companion object {
        private var registry: PrometheusMeterRegistry? = null
        private var instance: MetricsRegistry? = null

        fun initialize(meterRegistry: PrometheusMeterRegistry) {
            registry = meterRegistry
            instance = MetricsRegistry()
        }

        @Synchronized
        fun getInstance(): MetricsRegistry {
            if (instance == null) {
                // Auto-initialize with default Prometheus registry if not initialized
                val defaultRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
                initialize(defaultRegistry)
            }
            return instance!!
        }

        fun getRegistry(): PrometheusMeterRegistry {
            return registry ?: throw IllegalStateException("MetricsRegistry not initialized")
        }
    }

    fun counter(name: String, description: String): Counter {
        return Counter.builder(name)
            .description(description)
            .register(getRegistry())
    }

    fun timer(name: String, description: String): Timer {
        return Timer.builder(name)
            .description(description)
            .register(getRegistry())
    }

    inline fun <T> Timer.record(block: () -> T): T {
        val start = System.nanoTime()
        try {
            return block()
        } finally {
            val duration = System.nanoTime() - start
            record(duration, TimeUnit.NANOSECONDS)
        }
    }
}
