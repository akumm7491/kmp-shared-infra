# Creating a New Service

This guide walks you through creating a new service using the KMP Shared Infrastructure.

## Service Types

1. **HTTP Service**
   - REST API endpoints
   - GraphQL endpoints
   - Web applications

2. **Event-Driven Service**
   - Kafka consumers
   - Event processors
   - Stream processors

3. **Worker Service**
   - Background jobs
   - Scheduled tasks
   - Data processors

## Creating a Service

### 1. Generate Service

```bash
# Generate new service using the CLI tool
./tools/scripts/create-service.sh \
  --name user-service \
  --type http \
  --modules auth,storage,messaging \
  --namespace my-project-ns
```

### 2. Configure Service

```kotlin
// build.gradle.kts
plugins {
    id("com.example.kmp.service")
}

kmpService {
    name = "user-service"
    
    modules {
        auth {
            enabled = true
            roles = ["user.read", "user.write"]
        }
        
        storage {
            type = "timescaledb"
            migrations = true
        }
        
        messaging {
            kafka {
                topics = ["user.events"]
                consumers = ["notification.events"]
            }
        }
    }
}
```

### 3. Implement Service Logic

```kotlin
@Service
class UserService(
    private val auth: AuthProvider,
    private val db: DatabaseClient,
    private val messaging: MessageBus
) {
    @Authenticated
    @HasRole("user.write")
    suspend fun createUser(request: CreateUserRequest): User {
        // Your business logic here
    }
}
```

### 4. Configure Resources

```yaml
# config/service.yaml
service:
  name: user-service
  replicas: 2
  
  resources:
    requests:
      cpu: "0.5"
      memory: "512Mi"
    limits:
      cpu: "1"
      memory: "1Gi"
      
  scaling:
    enabled: true
    min: 2
    max: 5
    cpu_threshold: 70
```

## Service Features

### 1. Built-in Capabilities
- Health checks
- Metrics collection
- Distributed tracing
- Structured logging
- Configuration management

### 2. Security
- Authentication
- Authorization
- Rate limiting
- Input validation
- CORS configuration

### 3. Monitoring
- Prometheus metrics
- Grafana dashboards
- Log aggregation
- Trace visualization

## Development Workflow

### 1. Local Development
```bash
# Start service locally
./gradlew :services:user-service:runDev

# Run tests
./gradlew :services:user-service:test

# Build container
./gradlew :services:user-service:buildImage
```

### 2. Testing
```kotlin
@Test
fun `should create user when authorized`() {
    // Testing utilities provided by the framework
    withTestClient { client ->
        withAuth(roles = ["user.write"]) {
            val response = client.post("/users") {
                // Test implementation
            }
            assertEquals(HttpStatusCode.Created, response.status)
        }
    }
}
```

### 3. Deployment
```bash
# Deploy to development
./tools/scripts/deploy-service.sh \
  --service user-service \
  --env dev

# Verify deployment
kubectl get pods -l app=user-service -n my-project-ns
```

## Best Practices

### 1. Code Organization
```
service/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/service/
│   │   │       ├── config/
│   │   │       ├── domain/
│   │   │       ├── api/
│   │   │       └── Application.kt
│   │   └── resources/
│   └── test/
└── build.gradle.kts
```

### 2. Error Handling
```kotlin
sealed class ServiceError : RuntimeException {
    data class NotFound(val id: String) : ServiceError()
    data class ValidationError(val fields: List<String>) : ServiceError()
}

// Global error handler provided by framework
@ErrorHandler
fun handleServiceError(error: ServiceError): Response {
    // Error handling logic
}
```

### 3. Configuration
```kotlin
@Configuration
data class ServiceConfig(
    val feature: FeatureConfig,
    val client: ClientConfig
)

// Automatically loaded from environment/config files
@Inject
lateinit var config: ServiceConfig
```

## Customization Points

### 1. Custom Modules
```kotlin
class CustomAuthProvider : AuthProvider {
    override fun authenticate(request: Request): Principal {
        // Custom authentication logic
    }
}

// Register in configuration
kmpService {
    auth {
        provider = CustomAuthProvider::class
    }
}
```

### 2. Middleware
```kotlin
@Middleware
fun loggingMiddleware(
    context: Context,
    next: suspend () -> Response
): Response {
    // Custom middleware logic
}
```

### 3. Extensions
```kotlin
fun KMPService.customFeature() {
    // Add custom feature to service
}
```

## Troubleshooting

See [Service Troubleshooting](../operations/troubleshooting/services.md) for common issues and solutions.
