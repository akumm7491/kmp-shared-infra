package com.example.kmp.service.registry.test

import com.example.kmp.testing.report.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ServiceRegistryTestReporter : KMPTestReporter() {
    override val metrics = mutableMapOf<String, List<MetricEntry>>()
    override val errors = mutableMapOf<String, ErrorEntry>()

    override fun recordMetric(name: String, value: MetricValue) {
        val entry = when (value) {
            is MetricValue.Counter -> MetricEntry(name, value.value, "counter")
            is MetricValue.Gauge -> MetricEntry(name, value.value, "gauge")
            is MetricValue.Timer -> MetricEntry(name, value.durationMs, "timer")
        }
        metrics[name] = listOf(entry)
    }

    override fun recordError(error: String) {
        val entry = ErrorEntry(
            type = "Test Error",
            message = error,
            stackTrace = ""
        )
        errors[error] = entry
    }

    override fun generateReport(): String {
        val builder = StringBuilder()
        builder.appendLine("Service Registry Test Report")
        builder.appendLine("Generated at: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        builder.appendLine()

        builder.appendLine("Metrics:")
        metrics.forEach { (name, entries) ->
            entries.forEach { entry ->
                builder.appendLine("  $name: ${entry.value} (${entry.type})")
            }
        }
        builder.appendLine()

        if (errors.isNotEmpty()) {
            builder.appendLine("Errors:")
            errors.forEach { (_, error) ->
                builder.appendLine("  - Type: ${error.type}")
                builder.appendLine("    Message: ${error.message}")
                if (error.stackTrace.isNotEmpty()) {
                    builder.appendLine("    Stack Trace:")
                    builder.appendLine(error.stackTrace)
                }
            }
            builder.appendLine()
        }

        return builder.toString()
    }

    override fun saveReport(filePath: String) {
        val report = generateReport()
        File(filePath).writeText(report)
    }
}
