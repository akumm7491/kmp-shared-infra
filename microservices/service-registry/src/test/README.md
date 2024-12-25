# Service Registry Test Infrastructure

This document describes the test infrastructure for the service registry microservice.

## Overview

The test infrastructure is built on several key components:

1. Base Test Classes
   - `KMPTestConfig`: Common configuration management
   - `KMPTestData`: Test data handling
   - `KMPServiceTest`: Base test functionality
   - `ServiceRegistryTestBase`: Service-specific test base

2. Test Categories
   - Basic functionality (ServiceRegistryTest)
   - Eureka integration (ServiceRegistryEurekaTest)
   - Cache management (ServiceRegistryCacheTest)
   - Event handling (ServiceRegistryEventsTest)
   - Validation (ServiceRegistryValidationTest)
   - Metrics (ServiceRegistryMetricsTest)
   - Authentication (ServiceRegistryAuthTest)
   - Integration tests (ServiceRegistryIntegrationTest)

## Test Data

Test data is organized in `src/test/resources/test-data/`:

- `service-instances.json`: Sample service instances
- `instance-metadata.json`: Instance metadata
- `test-configs.json`: Test configurations

## Configuration

Test configuration files in `src/test/resources/`:

- `application-test.conf`: Test application settings
- `eureka-client.properties`: Eureka client configuration
- `logback-test.xml`: Test logging configuration

## Running Tests

### Running All Tests

```bash
./gradlew :microservices:service-registry:test
```

### Running Specific Tests

```bash
# Run a specific test class
./gradlew :microservices:service-registry:test --tests "com.example.kmp.service.registry.ServiceRegistryTest"

# Run a specific test method
./gradlew :microservices:service-registry:test --tests "com.example.kmp.service.registry.ServiceRegistryTest.test service registration and discovery"
```

### Test Suite

The `ServiceRegistryTestSuite` organizes all tests and ensures proper execution order:

1. Basic functionality tests
2. Eureka integration tests
3. Cache management tests
4. Event handling tests
5. Validation tests
6. Metrics tests
7. Authentication tests
8. Integration tests

## Test Environment

Tests run with:

- In-memory storage
- Local Eureka server
- Disabled security
- Enhanced logging
- Test-specific timeouts

## Writing Tests

### Test Base Class

Extend `ServiceRegistryTestBase` for new test classes:

```kotlin
class YourTest : ServiceRegistryTestBase() {
    @Test
    fun `your test case`() = withTestServer {
        // Test implementation
    }

    override fun Application.configureServer() {
        // Server configuration
    }
}
```

### Test Data

Use `ServiceRegistryTestData` for test instances:

```kotlin
val testInstance = ServiceRegistryTestData.createTestInstance(
    name = "test-service",
    host = "localhost",
    port = 8080
)
```

### Test Utilities

Common test utilities:

```kotlin
// Server operations
withTestServer { /* server context */ }

// Eureka operations
withEurekaClient { client -> /* client operations */ }

// Timeouts and retries
withTimeout(5.seconds) { /* timed operation */ }
retry(attempts = 3) { /* retried operation */ }
```

## Test Reports

Test reports are generated in:

- HTML: `build/reports/tests/test/index.html`
- XML: `build/test-results/test/`
- Logs: `build/reports/tests/test.log`

## Debugging Tests

1. Enable debug logging in `logback-test.xml`
2. Use test-specific properties in `application-test.conf`
3. Check test logs in `build/reports/tests/test.log`

## Common Issues

1. Connection Refused
   - Check if test server port is available
   - Verify Eureka client configuration

2. Test Timeouts
   - Adjust timeouts in `application-test.conf`
   - Check for resource contention

3. Flaky Tests
   - Use retry mechanism for unstable operations
   - Increase test timeouts
   - Ensure proper test isolation

## Best Practices

1. Test Organization
   - One test class per feature area
   - Clear test names using backticks
   - Proper test isolation

2. Test Data
   - Use test data helpers
   - Clean up after tests
   - Avoid shared state

3. Configuration
   - Use test-specific settings
   - Avoid hardcoded values
   - Document configuration changes

4. Error Handling
   - Test error cases
   - Verify error responses
   - Check error logging

## Contributing

1. Follow existing test patterns
2. Add tests for new features
3. Update test documentation
4. Verify test suite passes
