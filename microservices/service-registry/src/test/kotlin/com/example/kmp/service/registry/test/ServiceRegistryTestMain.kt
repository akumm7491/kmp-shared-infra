package com.example.kmp.service.registry.test

import com.example.kmp.testing.main.KMPTestMain
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.slf4j.LoggerFactory

class ServiceRegistryTestMain : KMPTestMain() {
    override val basePackage = "com.example.kmp.service.registry"

    override fun runTests() {
        logger.info("Starting Service Registry tests...")

        val request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectPackage(basePackage))
            .build()

        val launcher = LauncherFactory.create()
        val listener = ServiceRegistryTestListener()
        val reporter = ServiceRegistryTestReporter()

        launcher.registerTestExecutionListeners(listener)
        launcher.execute(request)

        logger.info("Service Registry tests completed.")
        reporter.saveReport("build/reports/tests/service-registry-test-report.txt")
    }
}

fun main() {
    ServiceRegistryTestMain().runTests()
}
