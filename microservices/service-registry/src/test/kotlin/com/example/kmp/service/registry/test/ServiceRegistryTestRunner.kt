package com.example.kmp.service.registry.test

import com.example.kmp.testing.runner.KMPTestRunner
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.slf4j.LoggerFactory

class ServiceRegistryTestRunner : KMPTestRunner() {
    override protected val logger = LoggerFactory.getLogger(javaClass)
    override val basePackage = "com.example.kmp.service.registry"

    override fun createReportingListener(): TestExecutionListener {
        return ServiceRegistryTestListener()
    }

    override fun runTests() {
        logger.info("Starting Service Registry tests...")

        val request: LauncherDiscoveryRequest = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectPackage(basePackage))
            .build()

        val launcher = LauncherFactory.create()
        val listener = createReportingListener()
        val reporter = ServiceRegistryTestReporter()

        launcher.registerTestExecutionListeners(listener)
        launcher.execute(request)

        logger.info("Service Registry tests completed.")
        reporter.saveReport("build/reports/tests/service-registry-test-report.txt")
    }
}
