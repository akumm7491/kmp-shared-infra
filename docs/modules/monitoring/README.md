# Monitoring Module

The Monitoring Module provides a unified way to handle metrics collection, health checks, and logging across services in the KMP Shared Infrastructure. It integrates with industry-standard tools like Prometheus for metrics and SLF4J for logging.

## Features

- Prometheus-compatible metrics collection and exposition
- Structured logging with context enrichment
- Health check endpoints
- Easy integration with Ktor applications

## Getting Started

### 1. Add Dependencies

Add the monitoring module to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":common-libs:monitoring-module"))
}
```

### 2. Initialize Monitoring

```kotlin
// Create metrics and logging providers
val metricsProvider = MonitoringFactory.createMetricsProvider()
val logProvider = MonitoringFactory.createLogProvider("your-service-name")

// In your Ktor application setup
fun Application.configureMonitoring() {
    val metricsProvider = MonitoringFactory.createMetricsProvider()
    metricsProvider.install(this)
}
```

## Metrics

### Available Endpoints

- `/metrics` - Prometheus-compatible metrics endpoint
- `/health` - Service health check endpoint

### Recording Metrics

```kotlin
// Counter
metricsProvider.incrementCounter(
    name = "http_requests_total",
    tags = mapOf("endpoint" to "/api/users", "method" to "GET")
)

// Gauge
metricsProvider.recordGauge(
    name = "queue_size",
    value = 42.0,
    tags = mapOf("queue" to "processing")
)

// Timer
metricsProvider.recordTimer(
    name = "request_duration_ms",
    durationMs = 100.0,
    tags = mapOf("endpoint" to "/api/users")
)
```

## Logging

The logging system provides structured logging with automatic context enrichment.

### Log Levels

```kotlin
// Info level
logProvider.info(
    message = "User logged in",
    metadata = mapOf("userId" to "123", "ipAddress" to "192.168.1.1")
)

// Warning level
logProvider.warn(
    message = "Rate limit approaching",
    metadata = mapOf("currentRate" to "95", "limit" to "100")
)

// Error level
logProvider.error(
    message = "Failed to process request",
    error = exception,
    metadata = mapOf("requestId" to "abc-123")
)

// Debug level
logProvider.debug(
    message = "Processing request details",
    metadata = mapOf("payload" to "...")
)
```

### Automatic Context Enrichment

Every log message is automatically enriched with:
- Service name
- Timestamp
- Any additional metadata provided in the log call

## Best Practices

1. **Metric Naming**
   - Use lowercase with underscores
   - Include units in the metric name (e.g., `duration_ms`, `size_bytes`)
   - Follow the pattern: `<namespace>_<metric>_<unit>`

2. **Tagging/Labels**
   - Keep cardinality under control
   - Use consistent naming across services
   - Include relevant dimensions for analysis

3. **Logging**
   - Use appropriate log levels
   - Include relevant context in metadata
   - Don't log sensitive information

## Configuration

### Metrics Configuration

The metrics system uses Prometheus by default. The configuration is handled through the `KtorMetricsProvider` class.

### Logging Configuration

Logging is implemented using SLF4J. Configure the underlying logging framework (e.g., Logback) according to your needs.

## Health Checks

The `/health` endpoint provides basic service health information:

```json
{
    "status": "UP",
    "timestamp": 1639876543210,
    "components": {
        "service": "UP"
    }
}
```

You can extend the health check implementation to include additional component checks as needed.

## Integration with Observability Stack

This monitoring module is designed to work seamlessly with:

- Prometheus for metrics collection
- Grafana for metrics visualization
- ELK Stack or similar for log aggregation
- Alert managers for notification systems

## Extending the Module

The monitoring module is designed to be extensible. You can:

1. Add custom metric types
2. Implement additional health checks
3. Extend logging context
4. Add new monitoring endpoints

## Troubleshooting

Common issues and their solutions:

1. **Metrics not showing up in Prometheus**
   - Verify the `/metrics` endpoint is accessible
   - Check Prometheus scrape configuration
   - Ensure metrics are being recorded correctly

2. **Missing logs**
   - Verify logging configuration
   - Check log level settings
   - Ensure log provider is properly initialized

## Support

For issues and feature requests, please use the project's issue tracker.
