# Monitoring & Logging Guide

This guide explains how to use the standardized monitoring and logging setup in KMP Shared Infrastructure.

## Metrics

### 1. Built-in Metrics
All services automatically collect:
- JVM metrics (memory, GC, threads)
- HTTP metrics (request counts, latencies)
- System metrics (CPU, disk)

### 2. Custom Metrics

```kotlin
import com.example.kmp.monitoring.MetricsConfig
import com.example.kmp.monitoring.recordLatency
import com.example.kmp.monitoring.incrementCounter

class YourService {
    private val registry = MetricsConfig.getMeterRegistry()

    fun trackOperation() {
        // Record operation count
        registry.incrementCounter(
            name = "operation_count",
            tags = listOf(Tag.of("type", "your_operation"))
        )

        // Record latency
        registry.recordLatency(
            name = "operation_latency",
            tags = listOf(Tag.of("type", "your_operation")),
            latencyMs = 100
        )
    }
}
```

## Logging

### 1. Configuration
The standard logging configuration is provided in `logback-common.xml` and includes:
- JSON formatted logs
- Correlation ID tracking
- Service name and environment tags
- File rotation
- Async logging

### 2. Usage

```kotlin
private val logger = LoggerFactory.getLogger(YourClass::class.java)

fun yourFunction() {
    logger.info("Operation started")
    
    // With structured data
    logger.info("User action", mapOf(
        "userId" to "123",
        "action" to "login",
        "status" to "success"
    ))
    
    // With correlation ID
    withCorrelationId("request-123") {
        logger.info("Processing request")
    }
}
```

## Monitoring Stack

### 1. Prometheus
- Metrics are exposed at `/metrics`
- Standard service discovery via annotations
- Retention and alerting rules configured

### 2. Grafana
- Pre-configured dashboards for:
  - Service Overview
  - JVM Metrics
  - HTTP Metrics
  - Business Metrics

### 3. Log Aggregation
- Structured JSON logs
- Automatic parsing of known fields
- Standard indexes and visualizations

## Best Practices

1. **Metrics Naming**
   - Use lowercase with underscores
   - Include unit in name (e.g., `duration_ms`)
   - Use standard prefixes (`http_`, `db_`, etc.)

2. **Logging**
   - Use appropriate log levels
   - Include relevant context
   - Don't log sensitive information
   - Use structured logging for machine-readable data

3. **Alerting**
   - Define SLOs for critical paths
   - Set up alerts for SLO violations
   - Include runbooks in alert definitions

## Troubleshooting

1. **Missing Metrics**
   - Check `/metrics` endpoint
   - Verify service annotations
   - Check Prometheus targets

2. **Log Issues**
   - Check log files in `/logs`
   - Verify logback configuration
   - Check disk space

3. **Dashboard Issues**
   - Verify metric names
   - Check time range
   - Validate Prometheus data source
