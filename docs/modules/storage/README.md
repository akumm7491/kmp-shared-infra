# Storage Module

The Storage Module provides a unified interface for data persistence across different storage backends in the KMP Shared Infrastructure project.

## Features

- Multiple storage backend support
  - TimescaleDB for time-series data
  - Neo4j for graph data
  - Redis for caching and session storage
- Connection pooling
- Transaction management
- Retry mechanisms
- Monitoring integration
- Migration tools
- Backup/restore utilities

## Getting Started

### 1. Add Dependencies

Add the storage module to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":common-libs:storage-module"))
}
```

### 2. Basic Usage

```kotlin
val storageConfig = StorageConfig {
    timescaleDb {
        host = "localhost"
        port = 5432
        database = "myapp"
        username = "user"
        password = "password"
    }
    
    neo4j {
        uri = "bolt://localhost:7687"
        username = "neo4j"
        password = "password"
    }
    
    redis {
        host = "localhost"
        port = 6379
    }
}

val storage = StorageFactory.create(storageConfig)
```

## Storage Backends

### 1. TimescaleDB

#### Configuration
```kotlin
timescaleDb {
    host = "localhost"
    port = 5432
    database = "myapp"
    username = "user"
    password = "password"
    
    pool {
        maxSize = 10
        minIdle = 2
        idleTimeout = Duration.minutes(10)
    }
    
    retry {
        attempts = 3
        backoff = ExponentialBackoff(
            initial = Duration.milliseconds(100),
            max = Duration.seconds(1)
        )
    }
}
```

#### Usage Examples
```kotlin
// Time-series data operations
storage.timescale.use { client ->
    client.insert("metrics", 
        TimeSeriesData(
            timestamp = Instant.now(),
            value = 42.0,
            tags = mapOf("host" to "server1")
        )
    )
    
    val data = client.query("metrics")
        .where("host" eq "server1")
        .timeRange(start, end)
        .execute()
}
```

### 2. Neo4j

#### Configuration
```kotlin
neo4j {
    uri = "bolt://localhost:7687"
    username = "neo4j"
    password = "password"
    
    pool {
        maxSize = 5
        acquisitionTimeout = Duration.seconds(30)
    }
}
```

#### Usage Examples
```kotlin
// Graph operations
storage.neo4j.use { client ->
    client.createNode("User", 
        mapOf(
            "name" to "John",
            "email" to "john@example.com"
        )
    )
    
    client.createRelationship(
        from = "User:1",
        to = "Post:42",
        type = "AUTHORED"
    )
}
```

### 3. Redis

#### Configuration
```kotlin
redis {
    host = "localhost"
    port = 6379
    password = "password"
    
    pool {
        maxTotal = 8
        maxIdle = 8
        minIdle = 0
    }
}
```

#### Usage Examples
```kotlin
// Cache operations
storage.redis.use { client ->
    client.set("user:1", userData, ttl = Duration.hours(1))
    val user = client.get<UserData>("user:1")
    
    // Pub/Sub
    client.publish("notifications", message)
    client.subscribe("notifications") { message ->
        println("Received: $message")
    }
}
```

## Data Models

### 1. Time Series Data
```kotlin
data class TimeSeriesData(
    val timestamp: Instant,
    val value: Double,
    val tags: Map<String, String>
)
```

### 2. Graph Data
```kotlin
data class Node(
    val labels: Set<String>,
    val properties: Map<String, Any>
)

data class Relationship(
    val type: String,
    val properties: Map<String, Any>
)
```

### 3. Cache Data
```kotlin
interface Cacheable {
    val key: String
    val ttl: Duration
}
```

## Monitoring Integration

### 1. Metrics
- Connection pool statistics
- Query execution times
- Cache hit/miss rates
- Error rates
- Storage capacity utilization

### 2. Logging
- Query execution logs
- Error logs
- Performance logs
- Connection pool events

## Best Practices

### 1. Connection Management
- Use connection pooling
- Configure appropriate timeouts
- Implement retry mechanisms
- Handle connection failures gracefully

### 2. Data Access Patterns
- Use appropriate storage backend for data type
- Implement proper indexing
- Use batch operations when possible
- Implement caching strategies

### 3. Security
- Use secure connections
- Implement proper authentication
- Encrypt sensitive data
- Regular security audits

### 4. Performance
- Monitor query performance
- Use appropriate batch sizes
- Implement connection pooling
- Regular maintenance tasks

## Migration Tools

### 1. Schema Migration
```kotlin
storage.timescale.migrate {
    createTable("metrics") {
        column("timestamp", TIMESTAMP)
        column("value", DOUBLE)
        column("tags", JSONB)
    }
}
```

### 2. Data Migration
```kotlin
storage.migrate {
    source("old_metrics")
        .transform { row ->
            TimeSeriesData(
                timestamp = row.getInstant("time"),
                value = row.getDouble("value"),
                tags = row.getMap("tags")
            )
        }
        .target("new_metrics")
}
```

## Backup and Restore

### 1. Backup Configuration
```kotlin
storage.backup {
    schedule = "0 0 * * *" // Daily backup
    retention = Duration.days(30)
    compression = true
    destination = "s3://backups"
}
```

### 2. Restore Procedures
```kotlin
storage.restore {
    source = "s3://backups/2024-01-01"
    validateData = true
    parallel = true
}
```

## Troubleshooting

Common issues and solutions:

1. **Connection Issues**
   - Check network connectivity
   - Verify credentials
   - Review connection pool settings
   - Check firewall rules

2. **Performance Issues**
   - Monitor query execution times
   - Review indexing strategy
   - Check connection pool usage
   - Analyze query patterns

3. **Data Consistency Issues**
   - Verify transaction boundaries
   - Check replication status
   - Review backup integrity
   - Validate data constraints

## Support

For issues and feature requests, please use the project's issue tracker.
