package com.example.kmp.testing.report

import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Base class for KMP test reporters
 */
abstract class KMPTestReporter {
    protected open val metrics = mutableMapOf<String, List<MetricEntry>>()
    protected open val errors = mutableMapOf<String, ErrorEntry>()

    /**
     * Record test metric
     */
    abstract fun recordMetric(name: String, value: MetricValue)

    /**
     * Record test error
     */
    abstract fun recordError(error: String)

    /**
     * Generate test report
     */
    abstract fun generateReport(): String

    /**
     * Save test report to file
     */
    abstract fun saveReport(filePath: String)
}

/**
 * Metric entry model
 */
data class MetricEntry(
    val name: String,
    val value: Number,
    val type: String
)

/**
 * Error entry model
 */
data class ErrorEntry(
    val type: String,
    val message: String,
    val stackTrace: String
)

/**
 * Metric value types
 */
sealed class MetricValue {
    data class Counter(val value: Long) : MetricValue()
    data class Gauge(val value: Double) : MetricValue()
    data class Timer(val durationMs: Long) : MetricValue()
}
