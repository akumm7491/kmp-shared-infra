package com.example.kmp.testing.config

import io.ktor.server.config.*
import java.io.File
import java.util.Properties

/**
 * Base class for KMP test configuration
 */
abstract class KMPTestConfig {
    protected val properties = Properties()
    
    /**
     * Application configuration for the service
     */
    abstract val applicationConfig: ApplicationConfig

    /**
     * Load service configuration from properties files
     */
    protected open fun loadServiceConfig() {
        val configFiles = listOf(
            resolveConfigPath("application.properties"),
            resolveConfigPath("test.properties")
        )

        configFiles.forEach { file ->
            if (file.exists()) {
                loadProperties("", file)
            }
        }

        // Load environment-specific properties
        val env = System.getenv("TEST_ENV") ?: "local"
        val envConfig = resolveConfigPath("application-$env.properties")
        if (envConfig.exists()) {
            loadProperties("", envConfig)
        }
    }

    /**
     * Load properties from file with optional prefix
     */
    protected fun loadProperties(prefix: String, file: File): Map<String, String> {
        val props = Properties()
        if (file.exists()) {
            file.inputStream().use { props.load(it) }
        }

        val result = props.entries.associate { (key, value) ->
            val prefixedKey = if (prefix.isNotEmpty()) "$prefix.$key" else key.toString()
            prefixedKey to value.toString()
        }

        // Add to main properties
        properties.putAll(props)

        return result
    }

    /**
     * Configure environment for tests
     */
    protected open fun configureEnvironment() {
        loadServiceConfig()
        getRequiredProperties().forEach { key ->
            if (!properties.containsKey(key)) {
                throw IllegalStateException("Required property '$key' not found")
            }
        }
    }

    /**
     * Get list of required properties
     */
    protected open fun getRequiredProperties(): List<String> = emptyList()

    /**
     * Get property value
     */
    protected fun getProperty(key: String): String? = properties.getProperty(key)

    /**
     * Set property value
     */
    protected fun setProperty(key: String, value: String) {
        properties.setProperty(key, value)
    }

    /**
     * Get properties with prefix
     */
    protected fun getPropertiesWithPrefix(prefix: String): Map<String, String> {
        return properties.entries
            .filter { (key, _) -> key.toString().startsWith(prefix) }
            .associate { (key, value) -> key.toString() to value.toString() }
    }

    /**
     * Resolve config file path
     */
    protected fun resolveConfigPath(filename: String): File {
        val configDirs = listOf(
            "src/test/resources",
            "src/main/resources",
            "config"
        )

        for (dir in configDirs) {
            val file = File(dir, filename)
            if (file.exists()) {
                return file
            }
        }

        return File(filename)
    }

    /**
     * Cleanup configuration
     */
    open fun cleanup() {
        properties.clear()
    }
}
