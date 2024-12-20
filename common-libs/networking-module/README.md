# Networking Module

This module provides a standardized way to handle HTTP networking across all microservices in the KMP project.

## Features

- Standardized HTTP client/server setup
- Built-in serialization and content negotiation
- Integrated logging and metrics
- Common error handling
- Consistent response formats
- Type-safe API responses

## Usage

### 1. Add the dependency to your service

```kotlin
// build.gradle.kts
dependencies {
    implementation(project(":common-libs:networking-module"))
}
```

### 2. Set up the networking in your service

```kotlin
fun Application.module() {
    val logProvider = MonitoringFactory.createLogProvider("service-name")
    val metricsProvider = MonitoringFactory.createMetricsProvider()
    
    val networking = NetworkingFactory.create {
        baseUrl = "http://localhost:8081"
        timeout = 30000
        retries = 3
        logging(logProvider)
        metrics(metricsProvider)
    }
    
    // Install networking features
    networking.install(this)
    
    routing {
        get("/api/data") {
            try {
                val data = fetchData()
                call.respondSuccess(data)
            } catch (e: Exception) {
                call.respondError(ApiError.internalError())
            }
        }
    }
}
```

### 3. Use type-safe responses

```kotlin
@Serializable
data class UserData(
    val id: String,
    val name: String
)

// Your response will be wrapped in ApiResponse
// GET /api/data returns:
{
    "data": {
        "id": "123",
        "name": "John"
    },
    "error": null,
    "metadata": {
        "timestamp": "2024-12-17T13:00:29-07:00"
    }
}
```

## Benefits

1. **Consistency**: All services use the same networking setup and response format
2. **Type Safety**: Serialization is handled automatically with Kotlin serialization
3. **Monitoring**: Built-in logging and metrics
4. **Error Handling**: Standardized error responses
5. **Flexibility**: Can be customized per service while maintaining core functionality
