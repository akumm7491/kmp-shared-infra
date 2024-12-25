package com.example.kmp.service.registry

import org.junit.platform.suite.api.*
import org.junit.platform.suite.api.SuiteDisplayName

/**
 * Test suite for service registry.
 * Organizes and runs all service registry tests:
 * - Basic functionality
 * - Eureka integration
 * - Cache management
 * - Event handling
 * - Validation
 * - Metrics
 */
@Suite
@SuiteDisplayName("Service Registry Test Suite")
@SelectPackages("com.example.kmp.service.registry")
@IncludeClassNamePatterns(".*Test")
@SelectClasses(
    ServiceRegistryTest::class,
    ServiceRegistryEurekaTest::class,
    ServiceRegistryCacheTest::class,
    ServiceRegistryEventsTest::class,
    ServiceRegistryValidationTest::class,
    ServiceRegistryMetricsTest::class,
    ServiceRegistryAuthTest::class,
    ServiceRegistryIntegrationTest::class
)
@ConfigurationParameter(
    key = "junit.jupiter.execution.parallel.enabled",
    value = "false"
)
@ConfigurationParameter(
    key = "junit.jupiter.testclass.order.default",
    value = "org.junit.jupiter.api.ClassOrderer\$OrderAnnotation"
)
class ServiceRegistryTestSuite {
    // Test suite configuration class
    // Actual tests are in the referenced test classes
}

/**
 * Test execution order:
 * 1. Basic functionality (ServiceRegistryTest)
 * 2. Eureka integration (ServiceRegistryEurekaTest)
 * 3. Cache management (ServiceRegistryCacheTest)
 * 4. Event handling (ServiceRegistryEventsTest)
 * 5. Validation (ServiceRegistryValidationTest)
 * 6. Metrics (ServiceRegistryMetricsTest)
 * 7. Authentication (ServiceRegistryAuthTest)
 * 8. Integration tests (ServiceRegistryIntegrationTest)
 *
 * Notes:
 * - Tests are run sequentially to avoid interference
 * - Each test class focuses on a specific aspect
 * - Test classes can be run individually if needed
 * - Integration tests run last to verify full system
 */
