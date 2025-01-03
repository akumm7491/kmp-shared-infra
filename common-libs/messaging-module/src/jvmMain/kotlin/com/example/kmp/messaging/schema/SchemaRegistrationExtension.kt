package com.example.kmp.messaging.schema

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import org.reflections.util.FilterBuilder
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

object SchemaRegistrationExtension {
    private val logger = LoggerFactory.getLogger(SchemaRegistrationExtension::class.java)

    /**
     * Initialize and register all schemas found in the specified package
     *
     * @param schemaRegistryUrl The URL of the Schema Registry
     * @param basePackage The base package to scan for schema classes
     * @param excludePackages Optional list of packages to exclude from scanning
     */
    fun initializeAndRegisterSchemas(
        schemaRegistryUrl: String = System.getenv("SCHEMA_REGISTRY_URL") ?: "http://schema-registry.kafka:8081",
        basePackage: String,
        excludePackages: List<String> = emptyList()
    ) {
        try {
            // Initialize schema registry client
            logger.info("Initializing Schema Registry client with URL: $schemaRegistryUrl")
            SchemaRegistry.initialize(schemaRegistryUrl)
            
            // Configure package scanning
            val configBuilder = ConfigurationBuilder()
                .forPackage(basePackage)
                .setScanners(Scanners.TypesAnnotated)
            
            // Add package exclusions if specified
            if (excludePackages.isNotEmpty()) {
                val filterBuilder = FilterBuilder()
                excludePackages.forEach { pkg ->
                    filterBuilder.excludePackage(pkg)
                }
                configBuilder.filterInputsBy(filterBuilder)
            }
            
            // Scan for classes with @RegisterSchema annotation
            logger.info("Scanning for schema classes in package: $basePackage")
            val reflections = Reflections(configBuilder)
            
            val schemaClasses = reflections.getTypesAnnotatedWith(RegisterSchema::class.java)
            logger.info("Found ${schemaClasses.size} classes with @RegisterSchema annotation")
            
            if (schemaClasses.isEmpty()) {
                logger.warn("No schema classes found in package: $basePackage")
                return
            }
            
            // Register schemas in a deterministic order
            schemaClasses.sortedBy { it.name }.forEach { clazz ->
                try {
                    logger.debug("Registering schema for class: ${clazz.simpleName}")
                    SchemaRegistry.registerSchema(clazz.kotlin)
                } catch (e: Exception) {
                    logger.error("Failed to register schema for class: ${clazz.simpleName}", e)
                    throw SchemaRegistrationException("Failed to register schema for ${clazz.simpleName}", e)
                }
            }
            
            logger.info("Schema registration completed successfully")
        } catch (e: Exception) {
            logger.error("Failed to initialize and register schemas", e)
            throw SchemaRegistrationException("Schema registration failed", e)
        }
    }

    /**
     * Register a single schema class
     *
     * @param schemaRegistryUrl The URL of the Schema Registry
     * @param schemaClass The class to register
     */
    fun registerSingleSchema(schemaRegistryUrl: String, schemaClass: KClass<*>) {
        try {
            SchemaRegistry.initialize(schemaRegistryUrl)
            SchemaRegistry.registerSchema(schemaClass)
            logger.info("Successfully registered schema for ${schemaClass.simpleName}")
        } catch (e: Exception) {
            logger.error("Failed to register schema for ${schemaClass.simpleName}", e)
            throw SchemaRegistrationException("Failed to register schema for ${schemaClass.simpleName}", e)
        }
    }
}

/**
 * Custom exception for schema registration errors
 */
class SchemaRegistrationException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)
