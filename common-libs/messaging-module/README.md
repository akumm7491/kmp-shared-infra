# Messaging Module

This module provides messaging infrastructure with automatic schema registration support for the KMP shared infrastructure.

## Features

- Kafka messaging support with Avro serialization
- Automatic schema generation and registration
- Type-safe message definitions using Kotlin data classes
- Annotation-based schema registration
- Runtime schema discovery

## Schema Registration

### Quick Start

1. Add the `@RegisterSchema` annotation to your data class:

```kotlin
@Serializable
@RegisterSchema(
    topic = "your.topic",
    name = "YourDataType",
    namespace = "com.your.package"
)
data class YourDataType(
    val field1: String,
    val field2: Int
)
```

2. Initialize schema registration in your application:

```kotlin
SchemaRegistrationExtension.initializeAndRegisterSchemas(
    schemaRegistryUrl = "http://schema-registry:8081",
    basePackage = "com.your.package"
)
```

### Supported Types

The schema registry automatically converts these Kotlin types to Avro:

| Kotlin Type | Avro Type |
|------------|-----------|
| String     | string    |
| Int        | int       |
| Long       | long      |
| Double     | double    |
| Boolean    | boolean   |

### Schema Evolution Best Practices

When modifying data classes:
- ✅ Add new optional fields
- ✅ Use default values for backward compatibility
- ❌ Don't remove existing fields
- ❌ Don't change field types
- ❌ Don't rename fields

## Configuration

### Docker Compose

Add these environment variables to your service:

```yaml
environment:
  SCHEMA_REGISTRY_URL: http://schema-registry:8081
  KAFKA_BOOTSTRAP_SERVERS: kafka:29092
```

### Application Configuration

Initialize schema registration early in your application:

```kotlin
fun main() {
    // Initialize schema registration
    SchemaRegistrationExtension.initializeAndRegisterSchemas(
        schemaRegistryUrl = System.getenv("SCHEMA_REGISTRY_URL"),
        basePackage = "com.example.kmp"
    )
    
    // Start your application
    // ...
}
```

## Error Handling

The schema registration system includes comprehensive error handling:

- Schema validation errors
- Registry connection issues
- Type conversion problems
- Duplicate schema registration detection

All errors are properly logged with detailed information to help diagnose issues.

## Complete Example

Here's a complete example using the weather service:

```kotlin
// 1. Define your data model
@Serializable
@RegisterSchema(
    topic = "weather.update",
    name = "WeatherData",
    namespace = "com.example.kmp.weather.model"
)
data class WeatherData(
    val city: String,
    val temperature: Double,
    val conditions: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long = System.currentTimeMillis()
)

// 2. Initialize in your application
fun main() {
    SchemaRegistrationExtension.initializeAndRegisterSchemas(
        schemaRegistryUrl = System.getenv("SCHEMA_REGISTRY_URL"),
        basePackage = "com.example.kmp.weather"
    )
    
    // Your application code...
}
```

## Benefits

- ✨ No manual schema registration required
- 🔒 Type-safe schema definition
- 🔄 Automatic schema evolution handling
- 📦 Centralized schema management
- 📝 Comprehensive logging
- 🚀 Part of the shared infrastructure

## Troubleshooting

If you encounter issues:

1. Check schema registry connectivity:
   ```bash
   curl http://schema-registry:8081/subjects
   ```

2. Verify schema registration:
   ```bash
   curl http://schema-registry:8081/subjects/your.topic-value/versions/latest
   ```

3. Check application logs for detailed error messages

## Contributing

When adding new features:
1. Add appropriate tests
2. Update documentation
3. Follow Kotlin coding conventions
4. Consider schema evolution impact
