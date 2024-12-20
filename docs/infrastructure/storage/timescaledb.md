# TimescaleDB Integration

TimescaleDB is our primary solution for time-series data storage, offering high-performance time-series data handling with SQL compatibility.

## Features

- Time-series optimized storage
- Automatic partitioning
- Continuous aggregations
- SQL compatibility
- Horizontal scaling

## Setup

### 1. Infrastructure Configuration

```yaml
# deploy/k8s/modules/storage/timescaledb/values.yaml
timescaledb:
  enabled: true
  version: "2.11.1"
  
  resources:
    requests:
      memory: "4Gi"
      cpu: "2"
    limits:
      memory: "8Gi"
      cpu: "4"
      
  storage:
    size: "100Gi"
    class: "standard"
    
  backup:
    enabled: true
    schedule: "0 2 * * *"
    retention: "7d"
```

### 2. Service Integration

```kotlin
@Configuration
class TimescaleConfig {
    @Bean
    fun timeScaleClient(): TimeScaleClient {
        return TimeScaleClient {
            host = config.getString("timescale.host")
            port = config.getInt("timescale.port")
            database = config.getString("timescale.database")
            credentials {
                fromSecret("timescale-credentials")
            }
        }
    }
}
```

## Usage

### 1. Time-Series Tables

```kotlin
@TimeSeries(
    timeColumn = "timestamp",
    partitionBy = "1d",
    indexes = ["device_id", "metric_type"]
)
data class MetricData(
    val timestamp: Instant,
    val deviceId: String,
    val metricType: String,
    val value: Double
)
```

### 2. Continuous Aggregations

```kotlin
@ContinuousAggregate(
    name = "hourly_metrics",
    source = MetricData::class,
    interval = "1h"
)
data class HourlyMetrics(
    val hour: Instant,
    val deviceId: String,
    val avgValue: Double,
    val minValue: Double,
    val maxValue: Double
)
```

### 3. Querying

```kotlin
@Repository
class MetricsRepository(
    private val timeScale: TimeScaleClient
) {
    suspend fun getMetrics(
        deviceId: String,
        from: Instant,
        to: Instant
    ): List<MetricData> = timeScale.query {
        """
        SELECT *
        FROM metric_data
        WHERE device_id = :deviceId
        AND timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
        """.trimIndent(),
        parameters = mapOf(
            "deviceId" to deviceId,
            "from" to from,
            "to" to to
        )
    }
    
    suspend fun getHourlyAggregates(
        deviceId: String,
        date: LocalDate
    ): List<HourlyMetrics> = timeScale.query {
        """
        SELECT *
        FROM hourly_metrics
        WHERE device_id = :deviceId
        AND hour::date = :date
        """.trimIndent(),
        parameters = mapOf(
            "deviceId" to deviceId,
            "date" to date
        )
    }
}
```

## Best Practices

### 1. Schema Design

```kotlin
// Good: Efficient time-series schema
@TimeSeries
data class SensorData(
    val timestamp: Instant,        // Primary time column
    val sensorId: String,         // Partition key
    val type: String,             // Additional dimension
    val value: Double             // Metric value
)

// Bad: Inefficient schema
data class SensorDataBad(
    val id: UUID,                 // Unnecessary surrogate key
    val timestamp: String,        // String timestamp is inefficient
    val data: JsonObject         // Unstructured data is harder to query
)
```

### 2. Indexing Strategy

```kotlin
@Configuration
class IndexConfig {
    fun configureIndexes() {
        timeScale.createIndex(
            table = "sensor_data",
            columns = ["sensor_id", "timestamp DESC"],
            type = "btree"
        )
        
        timeScale.createIndex(
            table = "sensor_data",
            columns = ["type"],
            type = "hash"
        )
    }
}
```

### 3. Retention Policy

```kotlin
@Configuration
class RetentionConfig {
    fun configureRetention() {
        timeScale.setRetentionPolicy(
            table = "sensor_data",
            interval = Duration.days(30)
        )
        
        timeScale.setRetentionPolicy(
            table = "hourly_metrics",
            interval = Duration.days(365)
        )
    }
}
```

## Monitoring

### 1. Health Checks

```kotlin
@HealthIndicator("timescale")
class TimescaleHealthCheck(
    private val client: TimeScaleClient
) {
    suspend fun check(): Health {
        return try {
            val result = client.query("SELECT 1")
            Health.up()
                .withDetail("connected", true)
                .withDetail("version", client.version())
                .build()
        } catch (e: Exception) {
            Health.down()
                .withException(e)
                .build()
        }
    }
}
```

### 2. Metrics

```kotlin
@MetricsConfiguration
class TimescaleMetrics {
    fun registerMetrics() {
        metrics.gauge("timescale.connections.active") {
            client.activeConnections()
        }
        
        metrics.timer("timescale.query.duration") {
            // Query execution time
        }
        
        metrics.counter("timescale.errors") {
            // Error count
        }
    }
}
```

## Backup and Recovery

### 1. Backup Configuration

```yaml
backup:
  schedule: "0 2 * * *"  # Daily at 2 AM
  retention: "7d"        # Keep backups for 7 days
  destination: "s3://backups/timescale"
  compression: true
```

### 2. Recovery Procedures

```bash
# Restore from backup
./tools/scripts/restore-timescale.sh \
  --backup-id 2024-01-01-02-00 \
  --target-instance timescale-prod

# Verify restoration
./tools/scripts/verify-timescale.sh \
  --instance timescale-prod
```

## Troubleshooting

### Common Issues

1. **Connection Issues**
```kotlin
@Troubleshoot
class ConnectionTroubleshooter {
    fun diagnose(): List<Issue> {
        return listOf(
            checkConnectivity(),
            checkCredentials(),
            checkResourceLimits(),
            checkNetworkPolicies()
        )
    }
}
```

2. **Performance Issues**
```kotlin
@Troubleshoot
class PerformanceTroubleshooter {
    fun diagnose(): List<Issue> {
        return listOf(
            checkSlowQueries(),
            checkIndexUsage(),
            checkChunkSize(),
            checkCompressionSettings()
        )
    }
}
```

## References

1. [TimescaleDB Documentation](https://docs.timescale.com/)
2. [Time-Series Best Practices](https://blog.timescale.com/blog/time-series-data-best-practices/)
3. [Kubernetes Integration](https://github.com/timescale/timescaledb-kubernetes)
