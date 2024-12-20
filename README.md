# KMP Shared Infrastructure

A shared infrastructure platform that provides common services, monitoring, and deployment tools for Kotlin Multiplatform projects.

## Overview

This repository contains:
- Shared infrastructure components (Kafka, Prometheus, etc.)
- Common libraries and modules
- Deployment tools and templates
- Documentation and examples

## Getting Started

### Prerequisites
- Kubernetes cluster
- kubectl
- Helm 3
- envsubst

### Setting Up Core Infrastructure

1. Deploy core namespaces:
```bash
kubectl apply -f infra/k8s/namespaces/core.yaml
```

2. Deploy shared services:
```bash
# Deploy monitoring stack
helm upgrade --install monitoring ./infra/helm/charts/monitoring \
  --namespace shared-monitoring

# Deploy shared resources (Kafka, etc.)
helm upgrade --install resources ./infra/helm/charts/resources \
  --namespace shared-resources
```

### Project Integration

1. Create project namespaces:
```bash
./tools/scripts/setup-project-namespaces.sh --project my-app
```

2. Deploy your service:
```bash
./tools/scripts/deploy-service.sh \
  --service my-service \
  --project my-app \
  --env dev \
  --port 8080 \
  --replicas 2
```

## Repository Structure

```
.
├── common-libs/           # Shared Kotlin libraries
│   ├── auth-module/
│   ├── messaging-module/
│   ├── monitoring-module/
│   └── networking-module/
│
├── docs/                  # Documentation
│   ├── architecture/
│   ├── deployment/
│   └── infrastructure/
│
├── examples/             # Example services and implementations
│   ├── demo-service/
│   ├── service-a/
│   └── template-service/
│
├── infra/               # Infrastructure configuration
│   ├── helm/
│   │   ├── charts/
│   │   └── values/
│   ├── k8s/
│   │   ├── namespaces/
│   │   └── templates/
│   └── terraform/
│
└── tools/               # Scripts and utilities
    └── scripts/
        ├── setup-project-namespaces.sh
        └── deploy-service.sh
```

## Common Libraries

The platform provides several shared libraries:

- **auth-module**: Authentication and authorization
- **messaging-module**: Kafka messaging integration
- **monitoring-module**: Metrics and logging
- **networking-module**: HTTP client and server utilities

### Using Common Libraries

Add dependencies to your project's build.gradle.kts:
```kotlin
dependencies {
    implementation("com.example.kmp:auth-module:1.0.0")
    implementation("com.example.kmp:messaging-module:1.0.0")
    // ... other modules as needed
}
```

## Infrastructure Components

### Core Services
- **Monitoring Stack**: Prometheus, Grafana, Alert Manager
- **Message Queue**: Kafka, Schema Registry
- **Logging**: ELK Stack

### Namespaces
- **shared-infra**: Core infrastructure components
- **shared-monitoring**: Centralized monitoring
- **shared-resources**: Shared services (Kafka, etc.)

## Development Workflow

1. **Create Project Structure**
   - Use project namespace template
   - Set up environment-specific configurations

2. **Implement Services**
   - Use common libraries
   - Follow monitoring guidelines
   - Implement health checks

3. **Deploy Services**
   - Use deployment scripts
   - Configure environment variables
   - Set up monitoring

## Contributing

1. Fork the repository
2. Create a feature branch
3. Submit a pull request

## Documentation

- [Architecture Overview](docs/architecture/README.md)
- [Deployment Guide](docs/deployment/README.md)
- [Infrastructure Guide](docs/infrastructure/README.md)

## Support

For issues and questions:
1. Check the documentation
2. Search existing issues
3. Create a new issue if needed

## License

This project is licensed under the MIT License - see the LICENSE file for details.
