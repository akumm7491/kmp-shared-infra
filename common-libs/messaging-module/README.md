# Messaging Module

This module provides a generic messaging infrastructure for event-driven communication between services.

## Features

- Event publishing and consumption
- Kafka integration with configurable settings
- Asynchronous message processing
- Generic event format supporting any serializable payload
- Automatic connection management and cleanup

## Usage

### Producer

```kotlin
// Create a producer
val producer = EventProducer.create()

// Create and publish an event
val event = Event(
    type = "user.created",
    payload = json.encodeToString(userData)
)
producer.publish("users", event)
```

### Consumer

```kotlin
// Create a consumer
val consumer = EventConsumer.create()

// Start consuming events
consumer.start(
    topics = listOf("users"),
    groupId = "user-service"
) { key, value ->
    // Process the event
    val userData = json.decodeFromString<UserData>(
        String(value, Charsets.UTF_8)
    )
    // Handle the user data...
}
```

## Configuration

The module uses environment variables for configuration:

- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker addresses (default: "localhost:9092")
- `SCHEMA_REGISTRY_URL`: Schema Registry URL if using Avro (default: "http://localhost:8081")

## Integration

To use this module in your service:

1. Add the dependency in your build.gradle.kts:
```kotlin
implementation(project(":common-libs:messaging-module"))
```

2. Configure environment variables in your docker-compose.yml:
```yaml
environment:
  KAFKA_BOOTSTRAP_SERVERS: kafka:29092
```

## Best Practices

1. Always close producers when done:
```kotlin
producer.close()
```

2. Stop consumers gracefully:
```kotlin
consumer.stop()
```

3. Handle errors in event processing:
```kotlin
consumer.start(topics, groupId) { key, value ->
    try {
        processEvent(value)
    } catch (e: Exception) {
        // Log error, handle failure...
    }
}
