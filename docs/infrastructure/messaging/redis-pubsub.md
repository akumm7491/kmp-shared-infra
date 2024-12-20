# Redis Pub/Sub Integration

Redis Pub/Sub provides lightweight, real-time messaging capabilities for scenarios requiring low-latency message delivery.

## Features

- Real-time messaging
- Pattern matching subscriptions
- Channel-based communication
- Low latency delivery
- Simple integration

## Setup

### 1. Infrastructure Configuration

```yaml
# deploy/k8s/modules/messaging/redis/values.yaml
redis:
  enabled: true
  version: "7.2"
  
  pubsub:
    enabled: true
    channels:
      - notifications
      - presence
      - events.*
    
  resources:
    requests:
      memory: "2Gi"
      cpu: "1"
    limits:
      memory: "4Gi"
      cpu: "2"
```

### 2. Service Integration

```kotlin
@Configuration
class RedisPubSubConfig {
    @Bean
    fun pubSubClient(): RedisPubSubClient {
        return RedisPubSubClient {
            // Connection settings
            host = config.getString("redis.host")
            port = config.getInt("redis.port")
            
            // Pub/Sub settings
            subscriptions {
                bufferSize = 1000
                timeout = 30.seconds
            }
            
            // Retry settings
            retry {
                maxAttempts = 3
                backoff = ExponentialBackoff(
                    initial = 100.milliseconds,
                    multiplier = 2.0
                )
            }
        }
    }
}
```

## Usage

### 1. Publishing Messages

```kotlin
@Service
class NotificationPublisher(
    private val pubsub: RedisPubSubClient
) {
    suspend fun publishNotification(
        userId: String,
        notification: Notification
    ) {
        pubsub.publish(
            channel = "notifications",
            message = NotificationMessage(
                userId = userId,
                notification = notification,
                timestamp = Instant.now()
            )
        )
    }
    
    suspend fun broadcastEvent(event: Event) {
        pubsub.publish(
            channel = "events.${event.type}",
            message = event
        )
    }
}
```

### 2. Subscribing to Messages

```kotlin
@Service
class NotificationSubscriber(
    private val pubsub: RedisPubSubClient
) {
    @Subscribe("notifications")
    suspend fun handleNotification(
        message: NotificationMessage
    ) {
        logger.info("Received notification", {
            "userId" to message.userId,
            "type" to message.notification.type
        })
        
        notificationService.process(message)
    }
    
    @Subscribe("events.*")
    suspend fun handleEvents(
        message: Event,
        channel: String
    ) {
        val eventType = channel.removePrefix("events.")
        logger.info("Received event", {
            "type" to eventType,
            "id" to message.id
        })
        
        eventProcessor.process(message)
    }
}
```

### 3. Presence System

```kotlin
@Service
class PresenceSystem(
    private val pubsub: RedisPubSubClient
) {
    private val presence = ConcurrentHashMap<String, UserPresence>()
    
    init {
        subscribeToPresence()
    }
    
    private fun subscribeToPresence() {
        pubsub.subscribe("presence") { message: PresenceMessage ->
            when (message) {
                is UserOnline -> presence[message.userId] = message.presence
                is UserOffline -> presence.remove(message.userId)
            }
        }
    }
    
    suspend fun updatePresence(
        userId: String,
        status: PresenceStatus
    ) {
        pubsub.publish(
            channel = "presence",
            message = UserOnline(
                userId = userId,
                presence = UserPresence(
                    status = status,
                    lastSeen = Instant.now()
                )
            )
        )
    }
    
    fun getOnlineUsers(): Map<String, UserPresence> {
        return presence.toMap()
    }
}
```

## Best Practices

### 1. Channel Design

```kotlin
// Good: Clear channel structure
object Channels {
    const val NOTIFICATIONS = "notifications"
    const val PRESENCE = "presence"
    
    fun userChannel(userId: String) = "user:$userId"
    fun eventChannel(type: String) = "events:$type"
}

// Bad: Inconsistent naming
const val CHANNEL_1 = "ch1"          // Unclear purpose
const val UserNotifications = "un"    // Unclear abbreviation
```

### 2. Message Structure

```kotlin
@Serializable
sealed class PubSubMessage {
    abstract val id: String
    abstract val timestamp: Instant
    
    @Serializable
    data class Notification(
        override val id: String,
        override val timestamp: Instant,
        val userId: String,
        val type: String,
        val data: JsonObject
    ) : PubSubMessage()
}
```

### 3. Error Handling

```kotlin
@Configuration
class PubSubErrorHandler {
    fun configure() {
        pubsub.errorHandler {
            retry {
                maxAttempts = 3
                backoff = ExponentialBackoff(
                    initial = 100.milliseconds,
                    multiplier = 2.0
                )
            }
            
            handle<ConnectionException> { ex ->
                logger.error("Connection lost", ex)
                ErrorAction.RETRY
            }
            
            handle<DeserializationException> { ex ->
                logger.error("Failed to deserialize message", ex)
                ErrorAction.SKIP
            }
        }
    }
}
```

## Monitoring

### 1. Health Checks

```kotlin
@HealthIndicator("redis-pubsub")
class PubSubHealthCheck(
    private val pubsub: RedisPubSubClient
) {
    suspend fun check(): Health {
        return try {
            val info = pubsub.info("pubsub")
            Health.up()
                .withDetail("channels", info["pubsub_channels"])
                .withDetail("patterns", info["pubsub_patterns"])
                .withDetail("subscribers", info["pubsub_subscribers"])
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
class PubSubMetrics {
    fun registerMetrics() {
        metrics.counter("pubsub.messages") {
            tags("channel", "type")
        }
        
        metrics.gauge("pubsub.subscribers") {
            tags("channel")
        }
        
        metrics.timer("pubsub.processing.duration") {
            tags("channel", "handler")
        }
    }
}
```

## Use Cases

### 1. Real-time Notifications

```kotlin
@Service
class NotificationService(
    private val pubsub: RedisPubSubClient
) {
    suspend fun notifyUser(
        userId: String,
        notification: Notification
    ) {
        // Publish to user-specific channel
        pubsub.publish(
            channel = Channels.userChannel(userId),
            message = notification
        )
        
        // Also publish to general notifications
        pubsub.publish(
            channel = Channels.NOTIFICATIONS,
            message = notification
        )
    }
}
```

### 2. Chat System

```kotlin
@Service
class ChatService(
    private val pubsub: RedisPubSubClient
) {
    suspend fun sendMessage(
        roomId: String,
        message: ChatMessage
    ) {
        pubsub.publish(
            channel = "chat:$roomId",
            message = message
        )
    }
    
    suspend fun joinRoom(
        roomId: String,
        userId: String
    ) {
        pubsub.subscribe("chat:$roomId") { message: ChatMessage ->
            handleChatMessage(roomId, message)
        }
    }
}
```

### 3. Live Updates

```kotlin
@Service
class LiveUpdateService(
    private val pubsub: RedisPubSubClient
) {
    suspend fun broadcastUpdate(
        update: LiveUpdate
    ) {
        pubsub.publish(
            channel = "updates",
            message = update
        )
    }
    
    @Subscribe("updates")
    suspend fun handleUpdate(update: LiveUpdate) {
        when (update) {
            is DataUpdate -> refreshData(update)
            is ConfigUpdate -> refreshConfig(update)
            is StatusUpdate -> updateStatus(update)
        }
    }
}
```

## References

1. [Redis Pub/Sub Documentation](https://redis.io/topics/pubsub)
2. [Redis Pub/Sub Patterns](https://redis.io/topics/patterns)
3. [Redis Pub/Sub Tutorial](https://redis.io/topics/pubsub)
