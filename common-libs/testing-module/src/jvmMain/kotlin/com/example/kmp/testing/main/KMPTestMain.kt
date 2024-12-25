package com.example.kmp.testing.main

import org.slf4j.LoggerFactory

/**
 * Base class for KMP test main entry points
 */
abstract class KMPTestMain {
    protected val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Base package for test discovery
     */
    abstract val basePackage: String

    /**
     * Run all tests
     */
    abstract fun runTests()
} 