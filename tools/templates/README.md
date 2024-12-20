# ${PROJECT_NAME}

This project uses the KMP Shared Infrastructure framework for rapid microservice development.

## Quick Start

1. Create a new microservice:
```bash
./gradlew createService -PserviceName=my-service
```

2. Implement your business logic in `microservices/my-service/src/main/kotlin`:
```kotlin
fun Application.module() {
    // Your domain-specific code here
    routing {
        get("/api/my-endpoint") {
            // Your endpoint logic
        }
    }
}
```

3. Deploy your service:
```bash
# Development
kubectl apply -k deploy/helm/overlays/dev

# Production
kubectl apply -k deploy/helm/overlays/prod
```

## Infrastructure Features

This project inherits the following features from kmp-shared-infra:

- **Monitoring**: Prometheus metrics, Grafana dashboards
- **Logging**: Structured logging, distributed tracing
- **Security**: Authentication, authorization, network policies
- **Messaging**: Kafka integration with schema registry
- **Resilience**: Circuit breakers, rate limiting, retries

## Project Structure

```
.
├── microservices/          # Your microservices
│   └── my-service/        # Example service
├── deploy/                # Deployment configurations
│   ├── helm/             # Kubernetes resources
│   └── terraform/        # Infrastructure as code
└── .github/              # CI/CD workflows
```

## Development Workflow

1. **Create Service**:
```bash
./gradlew createService -PserviceName=new-service
```

2. **Local Development**:
```bash
docker-compose up -d    # Start infrastructure
./gradlew :microservices:new-service:run
```

3. **Deploy**:
```bash
# Automatic via GitHub Actions on push
git push origin main

# Manual deployment
./gradlew deploy -Penv=dev -Pservice=new-service
```

## Configuration

### Environment Variables

Each service inherits these configurations:
```yaml
MONITORING_ENABLED: true
TRACING_ENABLED: true
LOG_LEVEL: INFO
KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

### Kubernetes Resources

Default resource limits:
```yaml
resources:
  requests:
    cpu: 100m
    memory: 256Mi
  limits:
    cpu: 200m
    memory: 512Mi
```

## Adding Dependencies

1. **Common Libraries**:
```kotlin
// build.gradle.kts
dependencies {
    implementation("com.example.kmp:networking-module")
    implementation("com.example.kmp:monitoring-module")
    // Add other modules as needed
}
```

2. **External Dependencies**:
```kotlin
dependencies {
    implementation("your.dependency:library:version")
}
```
