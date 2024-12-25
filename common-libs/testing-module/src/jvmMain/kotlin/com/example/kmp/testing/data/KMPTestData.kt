package com.example.kmp.testing.data

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

/**
 * Base class for KMP test data management
 */
abstract class KMPTestData {
    protected val dataCache = mutableMapOf<String, Any>()
    protected val objectMapper = ObjectMapper()

    /**
     * Load service test data
     */
    protected open fun loadServiceData() {
        // Override to load specific test data
    }

    /**
     * Get list of required data files
     */
    protected open fun getRequiredData(): List<String> = emptyList()

    /**
     * Load data from JSON file
     */
    protected inline fun <reified T> loadDataFromFile(filename: String): T {
        val file = resolveDataPath(filename)
        if (!file.exists()) {
            throw IllegalStateException("Required data file '$filename' not found")
        }
        return objectMapper.readValue(file)
    }

    /**
     * Resolve data file path
     */
    protected fun resolveDataPath(filename: String): File {
        val dataDirs = listOf(
            "src/test/resources/data",
            "src/test/resources",
            "data"
        )

        for (dir in dataDirs) {
            val file = File(dir, filename)
            if (file.exists()) {
                return file
            }
        }

        return File(filename)
    }

    /**
     * Get cached data
     */
    protected inline fun <reified T> getData(key: String): T? {
        return dataCache[key] as? T
    }

    /**
     * Set cached data
     */
    protected fun setData(key: String, value: Any) {
        dataCache[key] = value
    }

    /**
     * Clear cached data
     */
    protected fun clearData() {
        dataCache.clear()
    }

    /**
     * Cleanup test data
     */
    open fun cleanupServiceData() {
        clearData()
    }
}
