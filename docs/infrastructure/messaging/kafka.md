# Kafka Integration

Apache Kafka serves as our primary event streaming platform, enabling reliable, scalable, and high-throughput message processing.

## Features

- Event streaming
- Message persistence
- Exactly-once semantics
- Stream processing
- Schema registry

## Setup

### 1. Infrastructure Configuration

```yaml
# deploy/k8s/modules/messaging/kafka/values.yaml
kafka:
  enabled: true
  version: "3.5"
  
  replicas: 3
  partitions: 3
  replicationFactor: 3
  
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
    
  config:
    autoCreateTopics: false
    deleteTopicEnable: true
    messageMaxBytes: 1048576  # 1MB
    
  monitoring:
    enabled: true
    serviceMonitor:
      enabled: true

schemaRegistry:
  enabled: true
  replicaCount: 2
```

### 2. Service Integration

```kotlin
@Configuration
class KafkaConfig {
    @Bean
    fun kafkaClient(): KafkaClient {
        return KafkaClient {
            // Broker configuration
            brokers = config.getList("kafka.brokers")
            
            // Producer settings
            producer {
                acks = "all"
                retries = 3
                batchSize = 16384
                linger = 1.milliseconds
                compression = "snappy"
            }
            
            // Consumer settings
            consumer {
                groupId = "${config.serviceName}-group"
                autoOffsetReset = "earliest"
                maxPollRecords = 500
                sessionTimeout = 30.seconds
                heartbeatInterval = 10.seconds
            }
            
            // Schema registry
            schemaRegistry {
                url = config.getString("kafka.schema.registry.url")
                cacheSize = 100
            }
        }
    }
}
```

## Usage

### 1. Message Publishing

```kotlin
@Service
class EventPublisher(
    private val kafka: KafkaClient
) {
    suspend fun publishEvent(
        event: Event,
        key: String? = null
    ) {
        kafka.publish(
            topic = event.topic,
            key = key,
            value = event,
            headers = mapOf(
                "trace-id" to getTraceId(),
                "timestamp" to Instant.now().toString()
            )
        )
    }
    
    @Transactional
    suspend fun publishWithTransaction(
        events: List<Event>
    ) {
        kafka.transaction { producer ->
            events.forEach { event ->
                producer.publish(
                    topic = event.topic,
                    value = event
                )
            }
        }
    }
}
```

### 2. Message Consumption

```kotlin
@Service
class EventConsumer(
    private val kafka: KafkaClient
) {
    @KafkaListener(
        topics = ["user-events"],
        groupId = "user-service-group"
    )
    suspend fun handleUserEvents(
        event: UserEvent,
        metadata: MessageMetadata
    ) {
        logger.info("Received user event", {
            "eventType" to event.type,
            "userId" to event.userId,
            "offset" to metadata.offset,
            "partition" to metadata.partition
        })
        
        when (event) {
            is UserCreated -> handleUserCreated(event)
            is UserUpdated -> handleUserUpdated(event)
            is UserDeleted -> handleUserDeleted(event)
        }
    }
    
    @KafkaListener(
        topics = ["high-priority-events"],
        concurrency = 3,
        properties = [
            "max.poll.interval.ms=300000",
            "max.poll.records=100"
        ]
    )
    suspend fun handlePriorityEvents(
        event: PriorityEvent
    ) {
        // Handle priority events
    }
}
```

### 3. Stream Processing

```kotlin
@Service
class StreamProcessor(
    private val kafka: KafkaClient
) {
    @StreamProcessor(
        input = "raw-metrics",
        output = "processed-metrics"
    )
    suspend fun processMetrics(
        metrics: Flow<MetricEvent>
    ): Flow<ProcessedMetric> = metrics
        .filter { it.value > 0 }
        .map { event ->
            ProcessedMetric(
                timestamp = event.timestamp,
                value = calculateMetric(event),
                metadata = event.metadata
            )
        }
        .buffer(100)
    
    @StreamProcessor(
        input = ["user-events", "order-events"],
        output = "user-activity"
    )
    suspend fun processUserActivity(
        events: Flow<Event>
    ): Flow<UserActivity> = events
        .groupBy { it.userId }
        .flatMapMerge { userEvents ->
            userEvents
                .windowedBy(1.minutes)
                .map { window ->
                    UserActivity(
                        userId = userEvents.key,
                        events = window.events,
                        period = window.period
                    )
                }
        }
}
```

## Best Practices

### 1. Topic Design

```kotlin
// Good: Clear topic naming and structure
object Topics {
    const val USER_EVENTS = "user-events"
    const val ORDER_EVENTS = "order-events"
    const val PAYMENT_EVENTS = "payment-events"
    
    fun serviceTopic(service: String) = "$service-events"
    fun deadLetterTopic(topic: String) = "$topic.dlq"
}

// Bad: Inconsistent naming
const val UserEvents = "UserEvents"  // Inconsistent casing
const val orders = "orders"          // Too generic
const val SERVICE_1_TOPIC = "svc1"   // Unclear purpose
```

### 2. Message Schema

```kotlin
@Serializable
sealed class Event {
    abstract val id: String
    abstract val timestamp: Instant
    abstract val version: Int
    
    @Serializable
    data class UserCreated(
        override val id: String,
        override val timestamp: Instant,
        override val version: Int = 1,
        val userId: String,
        val email: String
    ) : Event()
}

// Schema Registry
@Configuration
class SchemaConfig {
    fun registerSchemas() {
        schemaRegistry.register(
            topic = Topics.USER_EVENTS,
            schema = Event.serializer().descriptor,
            version = 1
        )
    }
}
```

### 3. Error Handling

```kotlin
@Configuration
class ErrorConfig {
    @Bean
    fun errorHandler(): KafkaErrorHandler {
        return KafkaErrorHandler {
            // Retry policy
            retry {
                maxAttempts = 3
                backoff = ExponentialBackoff(
                    initial = 100.milliseconds,
                    multiplier = 2.0
                )
            }
            
            // Dead letter queue
            deadLetter {
                enabled = true
                suffix = ".dlq"
            }
            
            // Error handlers
            handle<DeserializationException> { ex ->
                logger.error("Failed to deserialize message", ex)
                ErrorAction.DEAD_LETTER
            }
            
            handle<ProcessingException> { ex ->
                logger.error("Failed to process message", ex)
                ErrorAction.RETRY
            }
        }
    }
}
```

## Monitoring

### 1. Health Checks

```kotlin
@HealthIndicator("kafka")
class KafkaHealthCheck(
    private val kafka: KafkaClient
) {
    suspend fun check(): Health {
        return try {
            val info = kafka.adminClient.describeCluster()
            Health.up()
                .withDetail("nodes", info.nodes.size)
                .withDetail("controller", info.controller)
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
class KafkaMetrics {
    fun registerMetrics() {
        metrics.gauge("kafka.consumer.lag") {
            tags("topic", "group")
        }
        
        metrics.counter("kafka.messages") {
            tags("topic", "status")
        }
        
        metrics.timer("kafka.processing.duration") {
            tags("topic", "handler")
        }
    }
}
```

## Troubleshooting

### Common Issues

1. **Consumer Issues**
```kotlin
@Troubleshoot
class ConsumerTroubleshooter {
    fun diagnose(): List<Issue> {
        return listOf(
            checkConsumerLag(),
            checkPartitionAssignment(),
            checkProcessingRate(),
            checkErrorRate()
        )
    }
}
```

2. **Producer Issues**
```kotlin
@Troubleshoot
class ProducerTroubleshooter {
    fun diagnose(): List<Issue> {
        return listOf(
            checkBatchSize(),
            checkCompressionRate(),
            checkFailureRate(),
            checkLatency()
        )
    }
}
```

## References

1. [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
2. [Kafka Streams Documentation](https://kafka.apache.org/documentation/streams/)
3. [Schema Registry Documentation](https://docs.confluent.io/platform/current/schema-registry/index.html)
4. [Kafka on Kubernetes](https://strimzi.io/documentation/)
