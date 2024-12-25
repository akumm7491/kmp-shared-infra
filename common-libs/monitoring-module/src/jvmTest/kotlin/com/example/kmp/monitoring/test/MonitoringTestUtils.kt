package com.example.kmp.monitoring.test

import com.example.kmp.monitoring.MetricsProvider
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking

/**
 * Test utilities for monitoring and metrics
 */
object MonitoringTestUtils {
    /**
     * Extension function to verify metrics were tracked
     */
    fun MetricsProvider.verifyMetric(name: String, times: Int = 1) = runBlocking {
        coVerify(exactly = times) {
            incrementCounter(name)
        }
    }

    /**
     * Extension function to verify multiple metrics were tracked
     */
    fun MetricsProvider.verifyMetrics(vararg metrics: String) = runBlocking {
        metrics.forEach { metric ->
            verifyMetric(metric)
        }
    }

    /**
     * Extension function to verify gauge metric
     */
    fun MetricsProvider.verifyGaugeValue(name: String, value: Double) = runBlocking {
        coVerify {
            recordGauge(name, value)
        }
    }

    /**
     * Extension function to verify timer metric
     */
    fun MetricsProvider.verifyTimerValue(name: String, durationMs: Double) = runBlocking {
        coVerify {
            recordTimer(name, durationMs)
        }
    }

    /**
     * Extension function to verify metric with tags
     */
    fun MetricsProvider.verifyMetricWithTags(name: String, tags: Map<String, String>) = runBlocking {
        coVerify {
            incrementCounter(name, tags)
        }
    }

    /**
     * Extension function to verify gauge metric with tags
     */
    fun MetricsProvider.verifyGaugeValueWithTags(name: String, value: Double, tags: Map<String, String>) = runBlocking {
        coVerify {
            recordGauge(name, value, tags)
        }
    }

    /**
     * Extension function to verify timer metric with tags
     */
    fun MetricsProvider.verifyTimerValueWithTags(name: String, durationMs: Double, tags: Map<String, String>) = runBlocking {
        coVerify {
            recordTimer(name, durationMs, tags)
        }
    }
}
