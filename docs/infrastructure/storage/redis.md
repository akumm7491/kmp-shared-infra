# Redis Integration

Redis serves as our primary solution for caching, session management, and real-time data handling.

## Features

- In-memory data storage
- Pub/Sub messaging
- Distributed locking
- Rate limiting
- Session management

## Setup

### 1. Infrastructure Configuration

```yaml
# deploy/k8s/modules/storage/redis/values.yaml
redis:
  enabled: true
  version: "7.2"
  
  architecture: "replication"  # standalone, replication, or cluster
  
  auth:
    enabled: true
    secretName: "redis-credentials"
    
  master:
    resources:
      requests:
        memory: "2Gi"
        cpu: "1"
      limits:
        memory: "4Gi"
        cpu: "2"
        
  replica:
    replicaCount: 2
    resources:
      requests:
        memory: "2Gi"
        cpu: "1"
      limits:
        memory: "4Gi"
        cpu: "2"
        
  persistence:
    enabled: true
    size: "20Gi"
    
  metrics:
    enabled: true
    serviceMonitor:
      enabled: true
```

### 2. Service Integration

```kotlin
@Configuration
class RedisConfig {
    @Bean
    fun redisClient(): RedisClient {
        return RedisClient {
            // Connection settings
            host = config.getString("redis.host")
            port = config.getInt("redis.port")
            credentials {
                fromSecret("redis-credentials")
            }
            
            // Pool settings
            pool {
                maxSize = 30
                minIdle = 5
                maxIdle = 10
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

### 1. Caching

```kotlin
@Service
class CacheService(
    private val redis: RedisClient
) {
    suspend fun <T> cached(
        key: String,
        ttl: Duration = 1.hours,
        loader: suspend () -> T
    ): T {
        // Try cache first
        return redis.get<T>(key) ?: loader().also {
            redis.set(key, it, ttl)
        }
    }
    
    @Cached(
        key = "'user:' + #userId",
        ttl = "30m"
    )
    suspend fun getUser(userId: String): User {
        return userRepository.findById(userId)
    }
}
```

### 2. Session Management

```kotlin
@Service
class SessionManager(
    private val redis: RedisClient
) {
    suspend fun createSession(
        userId: String,
        data: SessionData
    ): String {
        val sessionId = UUID.randomUUID().toString()
        redis.set(
            key = "session:$sessionId",
            value = data,
            ttl = 24.hours
        )
        return sessionId
    }
    
    suspend fun getSession(sessionId: String): SessionData? {
        return redis.get("session:$sessionId")
    }
    
    suspend fun updateSession(
        sessionId: String,
        update: (SessionData) -> SessionData
    ) {
        redis.atomic { client ->
            val session = client.get<SessionData>("session:$sessionId")
            if (session != null) {
                client.set(
                    key = "session:$sessionId",
                    value = update(session),
                    keepTtl = true
                )
            }
        }
    }
}
```

### 3. Rate Limiting

```kotlin
@Service
class RateLimiter(
    private val redis: RedisClient
) {
    suspend fun checkLimit(
        key: String,
        limit: Int,
        window: Duration
    ): Boolean {
        return redis.atomic { client ->
            val current = client.incr(key)
            if (current == 1L) {
                client.expire(key, window)
            }
            current <= limit
        }
    }
}

@RateLimit(
    key = "'api:' + #userId",
    limit = 100,
    window = "1m"
)
suspend fun apiCall(userId: String) {
    // Implementation
}
```

### 4. Distributed Locking

```kotlin
@Service
class DistributedLock(
    private val redis: RedisClient
) {
    suspend fun <T> withLock(
        key: String,
        ttl: Duration = 30.seconds,
        action: suspend () -> T
    ): T {
        val lockId = UUID.randomUUID().toString()
        
        try {
            // Acquire lock
            val acquired = redis.set(
                key = "lock:$key",
                value = lockId,
                ttl = ttl,
                nx = true
            )
            
            if (!acquired) {
                throw LockAcquisitionException("Failed to acquire lock: $key")
            }
            
            return action()
        } finally {
            // Release lock using Lua script for atomicity
            redis.eval("""
                if redis.call("get", KEYS[1]) == ARGV[1] then
                    return redis.call("del", KEYS[1])
                else
                    return 0
                end
            """.trimIndent(),
            keys = listOf("lock:$key"),
            args = listOf(lockId)
            )
        }
    }
}
```

## Best Practices

### 1. Key Management

```kotlin
// Good: Structured key naming
object Keys {
    fun userKey(id: String) = "user:$id"
    fun sessionKey(id: String) = "session:$id"
    fun rateLimit(userId: String, api: String) = "rate:$userId:$api"
}

// Bad: Inconsistent naming
val key1 = "user_$id"        // Inconsistent separator
val key2 = "Session" + id    // Inconsistent casing
```

### 2. Memory Management

```kotlin
@Configuration
class MemoryConfig {
    fun configureEviction() {
        redis.config {
            // Eviction policy
            maxmemory = "2gb"
            maxmemoryPolicy = "volatile-lru"
            
            // Key expiration
            activeExpireEnabled = true
            activeExpireEffort = 1
        }
    }
}
```

### 3. Error Handling

```kotlin
class RedisOperations(
    private val redis: RedisClient
) {
    suspend fun <T> withRetry(
        operation: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        retry(
            attempts = 3,
            backoff = exponentialBackoff(100.milliseconds)
        ) {
            try {
                operation()
            } catch (e: RedisException) {
                when (e) {
                    is RedisConnectionException -> throw e
                    is RedisCommandTimeoutException -> throw e
                    else -> throw RedisOperationException(e)
                }
            }
        }
    }
}
```

## Monitoring

### 1. Health Checks

```kotlin
@HealthIndicator("redis")
class RedisHealthCheck(
    private val redis: RedisClient
) {
    suspend fun check(): Health {
        return try {
            val info = redis.info()
            Health.up()
                .withDetail("version", info["redis_version"])
                .withDetail("connected_clients", info["connected_clients"])
                .withDetail("used_memory", info["used_memory_human"])
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
class RedisMetrics {
    fun registerMetrics() {
        metrics.gauge("redis.memory.used") {
            redis.info("memory")["used_memory_rss"]?.toLong() ?: 0L
        }
        
        metrics.counter("redis.operations") {
            tags("type", "get", "result")
        }
        
        metrics.timer("redis.operation.duration") {
            tags("type")
        }
    }
}
```

## Backup and Recovery

### 1. Backup Configuration

```yaml
backup:
  enabled: true
  schedule: "0 1 * * *"  # Daily at 1 AM
  type: "rdb"  # or aof
  compression: true
  destination: "s3://backups/redis"
```

### 2. Recovery Procedures

```bash
# Restore from backup
./tools/scripts/restore-redis.sh \
  --backup-id 2024-01-01-01-00 \
  --target-instance redis-prod

# Verify restoration
./tools/scripts/verify-redis.sh \
  --instance redis-prod
```

## Troubleshooting

### Common Issues

1. **Memory Issues**
```kotlin
@Troubleshoot
class MemoryTroubleshooter {
    fun diagnose(): List<Issue> {
        return listOf(
            checkMemoryUsage(),
            checkEvictionStats(),
            checkKeySpace(),
            checkFragmentation()
        )
    }
}
```

2. **Connection Issues**
```kotlin
@Troubleshoot
class ConnectionTroubleshooter {
    fun diagnose(): List<Issue> {
        return listOf(
            checkConnectivity(),
            checkClientList(),
            checkSlowLog(),
            checkNetworkLatency()
        )
    }
}
```

## References

1. [Redis Documentation](https://redis.io/documentation)
2. [Redis Best Practices](https://redis.io/topics/memory-optimization)
3. [Redis Kubernetes Operator](https://github.com/spotahome/redis-operator)
