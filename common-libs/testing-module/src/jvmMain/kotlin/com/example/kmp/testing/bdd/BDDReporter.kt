package com.example.kmp.testing.bdd

import arrow.core.Either
import arrow.core.raise.either
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * BDD test reporter.
 * Generates HTML reports for BDD test execution:
 * - Test results summary
 * - Scenario details
 * - Step execution
 * - Error reporting
 */
object BDDReporter {
    /**
     * Generate test report
     */
    fun generateReport(results: List<KMPBDDRunner.TestResult>) {
        val report = createReport(results)
        saveReport(report)
    }

    private fun createReport(results: List<KMPBDDRunner.TestResult>): String = createHTML().html {
        head {
            title("BDD Test Report")
            style {
                +"""
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .summary { margin-bottom: 20px; }
                    .success { color: green; }
                    .failure { color: red; }
                    .skipped { color: orange; }
                    .step { margin: 10px 0; padding: 10px; border-radius: 4px; }
                    .step-success { background-color: #e6ffe6; }
                    .step-failure { background-color: #ffe6e6; }
                    .step-skipped { background-color: #fff3e6; }
                    .error-details { 
                        background-color: #f8f8f8;
                        padding: 10px;
                        margin: 10px 0;
                        border-left: 3px solid red;
                        font-family: monospace;
                    }
                    .timestamp { color: #666; font-size: 0.9em; }
                    .metrics { 
                        display: grid;
                        grid-template-columns: repeat(3, 1fr);
                        gap: 10px;
                        margin: 20px 0;
                    }
                    .metric-card {
                        padding: 15px;
                        border-radius: 4px;
                        text-align: center;
                    }
                    .metric-success { background-color: #e6ffe6; }
                    .metric-failure { background-color: #ffe6e6; }
                    .metric-skipped { background-color: #fff3e6; }
                """.trimIndent()
            }
        }
        body {
            h1 { +"BDD Test Report" }
            
            // Timestamp
            div("timestamp") {
                val timestamp = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
                +"Generated at: $timestamp"
            }

            // Metrics
            div("metrics") {
                val successCount = results.count { it is KMPBDDRunner.TestResult.Success }
                val failureCount = results.count { it is KMPBDDRunner.TestResult.Failure }
                val skippedCount = results.count { it is KMPBDDRunner.TestResult.Skipped }
                
                div("metric-card metric-success") {
                    h3 { +"Passed" }
                    p { +"$successCount" }
                }
                div("metric-card metric-failure") {
                    h3 { +"Failed" }
                    p { +"$failureCount" }
                }
                div("metric-card metric-skipped") {
                    h3 { +"Skipped" }
                    p { +"$skippedCount" }
                }
            }

            // Summary
            div("summary") {
                h2 { +"Summary" }
                p {
                    +"Total steps: ${results.size}"
                    br {}
                    +"Success rate: ${calculateSuccessRate(results)}%"
                }
            }

            // Results
            h2 { +"Test Results" }
            results.forEach { result ->
                when (result) {
                    is KMPBDDRunner.TestResult.Success -> div("step step-success") {
                        span("success") { +"✓ " }
                        +result.step
                    }
                    is KMPBDDRunner.TestResult.Failure -> div("step step-failure") {
                        span("failure") { +"✗ " }
                        +result.step
                        div("error-details") {
                            +"Error: ${result.error.message}"
                            br {}
                            +"Stack trace:"
                            pre {
                                +result.error.stackTraceToString()
                            }
                        }
                    }
                    is KMPBDDRunner.TestResult.Skipped -> div("step step-skipped") {
                        span("skipped") { +"○ " }
                        +result.step
                    }
                }
            }
        }
    }

    private fun calculateSuccessRate(results: List<KMPBDDRunner.TestResult>): Double {
        if (results.isEmpty()) return 0.0
        val successCount = results.count { it is KMPBDDRunner.TestResult.Success }
        return (successCount.toDouble() / results.size * 100).round(2)
    }

    private fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return kotlin.math.round(this * multiplier) / multiplier
    }

    private fun saveReport(report: String): Either<Throwable, Unit> = either {
        try {
            val reportsDir = File("build/reports/bdd")
            reportsDir.mkdirs()
            
            val timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            )
            val reportFile = File(reportsDir, "bdd-report-$timestamp.html")
            
            reportFile.writeText(report)
        } catch (e: Throwable) {
            raise(e)
        }
    }
}
