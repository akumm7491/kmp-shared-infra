package com.example.kmp.service.registry.test

import com.example.kmp.testing.listener.KMPTestListener
import com.example.kmp.testing.listener.MetricValue
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan
import org.junit.platform.engine.TestExecutionResult

class ServiceRegistryTestListener : KMPTestListener() {
    override val serviceName = "service-registry"
    private val metrics = mutableMapOf<String, MetricValue>()
    private val reporter = ServiceRegistryTestReporter()

    override fun testPlanExecutionStarted(testPlan: TestPlan) {
        super.testPlanExecutionStarted(testPlan)
        metrics.clear()
        recordMetric("test.plan.started", MetricValue.Counter(1))
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan) {
        super.testPlanExecutionFinished(testPlan)
        recordMetric("test.plan.finished", MetricValue.Counter(1))
        metrics.forEach { (name, value) ->
            when (value) {
                is MetricValue.Counter -> reporter.recordMetric(name, com.example.kmp.testing.report.MetricValue.Counter(value.value))
                is MetricValue.Gauge -> reporter.recordMetric(name, com.example.kmp.testing.report.MetricValue.Gauge(value.value))
                is MetricValue.Timer -> reporter.recordMetric(name, com.example.kmp.testing.report.MetricValue.Timer(value.durationMs))
            }
        }
        reporter.saveReport("build/reports/tests/service-registry-test-report.txt")
    }

    override fun executionStarted(testIdentifier: TestIdentifier) {
        super.executionStarted(testIdentifier)
        if (testIdentifier.isTest) {
            recordMetric("test.started", MetricValue.Counter(1))
        }
    }

    override fun executionFinished(testIdentifier: TestIdentifier, testExecutionResult: TestExecutionResult) {
        super.executionFinished(testIdentifier, testExecutionResult)
        if (testIdentifier.isTest) {
            when (testExecutionResult.status) {
                TestExecutionResult.Status.SUCCESSFUL -> {
                    recordMetric("test.passed", MetricValue.Counter(1))
                }
                TestExecutionResult.Status.FAILED -> {
                    recordMetric("test.failed", MetricValue.Counter(1))
                    testExecutionResult.throwable.ifPresent { error ->
                        recordError(error.message ?: "Unknown error")
                    }
                }
                TestExecutionResult.Status.ABORTED -> {
                    recordMetric("test.aborted", MetricValue.Counter(1))
                }
            }
        }
    }

    override protected fun recordMetric(name: String, value: MetricValue) {
        metrics[name] = value
    }

    private fun recordError(error: String) {
        recordMetric("test.error", MetricValue.Counter(1))
        reporter.recordError(error)
    }
}
