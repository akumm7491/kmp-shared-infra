# Development Guide

## Getting Started

### Project Structure
```
kmp-shared-infra/
├── common-libs/          # Shared Kotlin libraries
├── microservices/       # Example services
├── deploy/             # Deployment configurations
└── docs/              # Documentation
```

### Setting Up Development Environment

1. **Prerequisites**
   - JDK 17
   - Docker
   - Kubernetes (minikube/kind)
   - Helm
   - Terraform

2. **Local Development**
```bash
# Start local Kubernetes cluster
minikube start

# Install local dependencies
./gradlew build

# Run service locally
./gradlew :microservices:service-a:run
```

## Creating New Services

1. **Use Project Generator**
```bash
./tools/create-kmp-project.sh my-service com.example.myservice
```

2. **Implement Service Logic**
- Extend base service classes
- Add required endpoints
- Implement business logic
- Add tests

3. **Local Testing**
```bash
# Run tests
./gradlew test

# Run integration tests
./gradlew integrationTest
```

## Using Common Libraries

### Monitoring Module
```kotlin
fun Application.module() {
    install(KtorMonitoring) {
        metricsPath = "/metrics"
    }
}
```

### Networking Module
```kotlin
fun Application.module() {
    install(KtorServer) {
        port = 8080
    }
}
```

## Best Practices

### Code Style
- Follow Kotlin coding conventions
- Use meaningful names
- Write comprehensive tests
- Document public APIs

### Git Workflow
1. Create feature branch
2. Implement changes
3. Write tests
4. Submit PR
5. Address review comments
6. Merge to develop

### Testing Strategy
- Unit tests for business logic
- Integration tests for API endpoints
- Performance tests for critical paths
- Security tests for authentication/authorization

## Troubleshooting

### Common Issues
1. **Build Failures**
   - Check Gradle dependencies
   - Verify JDK version
   - Clear Gradle cache

2. **Runtime Errors**
   - Check logs
   - Verify configurations
   - Check resource limits

3. **Deployment Issues**
   - Verify Kubernetes context
   - Check pod logs
   - Verify secrets/configmaps
