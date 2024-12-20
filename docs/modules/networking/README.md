# Networking Module

The Networking Module provides a robust, feature-rich HTTP client implementation based on Ktor, designed for reliable and efficient network communication in the KMP Shared Infrastructure.

## Features

- Multiple HTTP client engine support (OkHttp, CIO)
- Circuit breaker pattern implementation
- Rate limiting
- Retry mechanisms
- Comprehensive logging and metrics
- Connection pooling
- Timeout configuration
- Content negotiation
- Compression support

## Getting Started

### 1. Add Dependencies

Add the networking module to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":common-libs:networking-module"))
}
```

### 2. Basic Usage

```kotlin
val networking = NetworkingFactory.create {
    baseUrl = "https://api.example.com"
    timeout = 30000 // 30 seconds
    retries = 3
    clientEngine = HttpClientEngine.OKHTTP
    
    // Optional: Add logging and metrics
    logging(logProvider)
    metrics(metricsProvider)
}

val client = networking.client
```

## Configuration Options

### Core Configuration

```kotlin
NetworkingFactory.create {
    // Base Configuration
    baseUrl = "https://api.example.com"
    timeout = 30000
    connectTimeout = 10000
    readTimeout = 30000
    writeTimeout = 30000
    
    // Connection Pool
    maxIdleConnections = 5
    keepAliveDuration = 300000 // 5 minutes
    connectionPoolSize = 1000
    pipelineSize = 20
    
    // Features
    enableCompression = true
    followRedirects = true
    defaultHeaders = mapOf(
        "User-Agent" to "KMP-Client/1.0"
    )
    
    // Client Engine Selection
    clientEngine = HttpClientEngine.OKHTTP // or HttpClientEngine.CIO
}
```

### Resilience Features

#### Circuit Breaker
```kotlin
// Configure circuit breaker
circuitBreakerFailureThreshold = 5
circuitBreakerResetTimeout = 60000 // 60 seconds
```

#### Rate Limiting
```kotlin
// Configure rate limiting
rateLimitRequests = 100
rateLimitDuration = 1.seconds
```

#### Retry Configuration
```kotlin
// Configure retries
retries = 3
```

## HTTP Client Engines

### OkHttp Engine
- Default engine
- Better performance on JVM
- More mature and feature-rich
- Extensive configuration options

### CIO Engine
- Kotlin-first implementation
- Lightweight and efficient
- Good for high-concurrency scenarios
- Cross-platform compatibility

## Monitoring Integration

### Metrics
The module can integrate with the monitoring module to collect metrics:

- Request counts
- Response times
- Error rates
- Circuit breaker states
- Rate limiter statistics

### Logging
Comprehensive logging support with configurable levels:

- Request/response details
- Error information
- Performance metrics
- Circuit breaker state changes
- Rate limiting events

## Best Practices

### 1. Connection Management
- Configure connection pool size based on expected load
- Set appropriate timeouts
- Use keep-alive for persistent connections

### 2. Error Handling
- Implement proper error handling
- Use circuit breakers for failing endpoints
- Configure appropriate retry strategies

### 3. Performance Optimization
- Enable compression for large payloads
- Use connection pooling effectively
- Configure appropriate timeouts

### 4. Security
- Use HTTPS for all external communications
- Implement proper authentication
- Handle sensitive data securely

## Common Use Cases

### 1. REST API Client
```kotlin
val client = NetworkingFactory.create {
    baseUrl = "https://api.example.com"
    timeout = 30000
    retries = 3
}
```

### 2. High-Performance Client
```kotlin
val client = NetworkingFactory.create {
    clientEngine = HttpClientEngine.CIO
    connectionPoolSize = 1000
    pipelineSize = 20
    timeout = 5000
}
```

### 3. Resilient Client
```kotlin
val client = NetworkingFactory.create {
    retries = 3
    circuitBreakerFailureThreshold = 5
    circuitBreakerResetTimeout = 60000
    rateLimitRequests = 100
    rateLimitDuration = 1.seconds
}
```

## Troubleshooting

Common issues and their solutions:

1. **Connection Timeouts**
   - Check network connectivity
   - Verify timeout configurations
   - Review server response times

2. **Circuit Breaker Trips**
   - Monitor service health
   - Check error thresholds
   - Review reset timeout settings

3. **Rate Limiting Issues**
   - Adjust rate limit parameters
   - Review request patterns
   - Consider client-side queuing

## Support

For issues and feature requests, please use the project's issue tracker.
