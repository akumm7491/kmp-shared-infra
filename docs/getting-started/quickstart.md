# Quick Start Guide

This guide will help you get started with the KMP Shared Infrastructure platform. Follow these steps to create and deploy your first service.

## Prerequisites

- Docker Desktop
- Kubernetes cluster access
- Gradle 8.x
- JDK 17+
- Git

## Quick Setup

1. **Create a New Project**

```bash
# Clone the template project
git clone https://github.com/your-org/kmp-shared-infra
cd kmp-shared-infra

# Generate new project
./tools/scripts/create-project.sh \
  --name my-project \
  --namespace my-namespace \
  --modules auth,storage,monitoring
```

2. **Configure Infrastructure Components**

```yaml
# config/infrastructure.yaml
infrastructure:
  storage:
    timescaledb:
      enabled: true
      size: 10Gi
    redis:
      enabled: true
  messaging:
    kafka:
      enabled: true
      topics:
        - name: my-topic
          partitions: 3
  monitoring:
    enabled: true
    retention: 7d
```

3. **Deploy Your Project**

```bash
# Deploy infrastructure
./tools/scripts/deploy.sh --env dev

# Verify deployment
kubectl get pods -n my-namespace
```

## What's Included

Your new project comes with:

### 1. Core Infrastructure
- Dedicated namespace
- Resource quotas
- Network policies
- Monitoring setup
- Logging pipeline

### 2. Common Libraries
- Authentication module
- Storage clients
- Monitoring integration
- Networking utilities

### 3. Development Tools
- Local development environment
- Test frameworks
- CI/CD pipelines
- Deployment scripts

## Next Steps

1. [Create a New Service](new-service.md)
2. [Configure Monitoring](../operations/monitoring/setup.md)
3. [Set Up CI/CD](../operations/deployment/cicd.md)
4. [Development Guidelines](../development/guidelines.md)

## Customization

The infrastructure is designed to be customizable:

### 1. Module Configuration
```kotlin
// build.gradle.kts
kmpInfra {
    modules {
        auth {
            provider = "keycloak"
            customRoles = true
        }
        storage {
            type = "timescaledb"
            customSchema = true
        }
    }
}
```

### 2. Resource Limits
```yaml
# config/resources.yaml
compute:
  cpu: "2"
  memory: "4Gi"
storage:
  size: "20Gi"
```

### 3. Custom Components
```kotlin
// Add custom components
class CustomAuthProvider : AuthProvider {
    // Your implementation
}

// Register in configuration
kmpInfra {
    auth {
        register(CustomAuthProvider())
    }
}
```

## Common Tasks

### Add a New Service
```bash
./tools/scripts/create-service.sh \
  --name user-service \
  --type http \
  --modules auth,storage
```

### Scale Resources
```bash
kubectl scale deployment user-service --replicas=3 -n my-namespace
```

### Monitor Services
```bash
# Access monitoring dashboard
kubectl port-forward svc/grafana 3000:3000 -n monitoring
```

## Getting Help

- [Troubleshooting Guide](../operations/troubleshooting/README.md)
- [FAQ](../FAQ.md)
- [Support Channels](../SUPPORT.md)
