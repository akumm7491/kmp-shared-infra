package com.example.kmp.testing.listener

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

/**
 * Base class for KMP test execution listeners
 */
abstract class KMPTestListener : TestExecutionListener {
    protected val logger = LoggerFactory.getLogger(javaClass)
    protected val testStartTimes = mutableMapOf<String, Instant>()
    protected val testResults = mutableMapOf<String, TestExecutionResult>()
    protected val testDurations = mutableMapOf<String, Duration>()
    protected val testErrors = mutableMapOf<String, Throwable>()

    /**
     * Get service name for metrics
     */
    abstract val serviceName: String

    /**
     * Record test metrics
     */
    protected open fun recordTestMetrics(testId: String) {
        val result = testResults[testId]
        val duration = testDurations[testId]
        val error = testErrors[testId]

        when (result?.status) {
            TestExecutionResult.Status.SUCCESSFUL -> {
                recordMetric("test.success.total", MetricValue.Counter(1))
                recordMetric("test.duration", MetricValue.Timer(duration?.toMillis() ?: 0))
            }
            TestExecutionResult.Status.FAILED -> {
                recordMetric("test.failure.total", MetricValue.Counter(1))
                recordMetric("test.error.total", MetricValue.Counter(1))
                recordTestError(testId, error)
            }
            TestExecutionResult.Status.ABORTED -> {
                recordMetric("test.aborted.total", MetricValue.Counter(1))
            }
            null -> {
                recordMetric("test.skipped.total", MetricValue.Counter(1))
            }
        }
    }

    /**
     * Record test error
     */
    protected open fun recordTestError(testId: String, error: Throwable?) {
        error?.let { e ->
            when (e) {
                is AssertionError -> {
                    recordMetric("test.assertion.failure", MetricValue.Counter(1))
                }
                is RuntimeException -> {
                    recordMetric("test.runtime.error", MetricValue.Counter(1))
                }
                is Exception -> {
                    recordMetric("test.error", MetricValue.Counter(1))
                }
                else -> {
                    recordMetric("test.unknown.error", MetricValue.Counter(1))
                }
            }
        }
    }

    /**
     * Record metric value
     */
    protected abstract fun recordMetric(name: String, value: MetricValue)

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        logger.info("Test plan execution started")
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        logger.info("Test plan execution finished")
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        if (testIdentifier.isTest) {
            testStartTimes[testIdentifier.uniqueId] = Instant.now()
            logger.info("Test started: ${testIdentifier.displayName}")
        }
    }

    override fun executionFinished(testIdentifier: TestIdentifier, result: TestExecutionResult) {
        if (testIdentifier.isTest) {
            val startTime = testStartTimes[testIdentifier.uniqueId]
            val duration = startTime?.let { Duration.between(it, Instant.now()) }

            testResults[testIdentifier.uniqueId] = result
            duration?.let { testDurations[testIdentifier.uniqueId] = it }
            result.throwable.ifPresent { testErrors[testIdentifier.uniqueId] = it }

            recordTestMetrics(testIdentifier.uniqueId)
            logger.info("Test finished: ${testIdentifier.displayName}, status: ${result.status}")
        }
    }
}

/**
 * Metric value types
 */
sealed class MetricValue {
    data class Counter(val value: Long) : MetricValue()
    data class Gauge(val value: Double) : MetricValue()
    data class Timer(val durationMs: Long) : MetricValue()
}
