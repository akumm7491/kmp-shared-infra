package com.example.kmp.testing.runner

import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.TagFilter
import org.junit.platform.launcher.TestExecutionListener
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

/**
 * Base class for KMP test runners
 */
abstract class KMPTestRunner {
    protected open val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Get base package for test discovery
     */
    abstract val basePackage: String

    /**
     * Create reporting listener
     */
    protected abstract fun createReportingListener(): TestExecutionListener

    /**
     * Run all tests
     */
    abstract fun runTests()

    /**
     * Run tests by pattern
     */
    protected fun runTestsByPattern(pattern: String) {
        val request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectPackage(basePackage))
            .filters(TagFilter.includeTags(pattern))
            .build()
        runTests()
    }

    /**
     * Run tests by tag
     */
    protected fun runTestsByTag(tag: String) {
        logger.info("Running tests with tag: $tag")
        runTestsByPattern(tag)
    }

    companion object {
        const val DEFAULT_TIMEOUT = 30L // seconds
        const val DEFAULT_RETRY_COUNT = 3
    }
}
