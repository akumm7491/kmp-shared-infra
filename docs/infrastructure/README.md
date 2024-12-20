# Infrastructure Integration Guide

## Overview
This shared infrastructure provides core services and templates that can be used by multiple projects. Each project should maintain its own deployment configuration while leveraging the shared infrastructure components.

## Core Components

### Shared Namespaces
- `shared-infra`: Core infrastructure components
- `shared-monitoring`: Centralized monitoring stack
- `shared-resources`: Shared resources (databases, message queues, etc.)

## Project Integration

### 1. Create Project Namespaces
Each project should create its own namespaces using the provided template:

1. Copy the template from `infra/k8s/templates/project-namespaces.yaml`
2. Replace `${PROJECT_NAME}` with your project name
3. Apply the configuration:
```bash
# Example for a project named "my-app"
envsubst < infra/k8s/templates/project-namespaces.yaml | kubectl apply -f -
```

### 2. Deploy Services
When deploying your services:

1. Use project-specific namespaces for your services:
   - Development: `<project-name>-dev`
   - Staging: `<project-name>-staging`
   - Production: `<project-name>-prod`

2. Configure service access to shared resources:
```yaml
# Example service configuration
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-service
  namespace: my-app-dev  # Project-specific namespace
spec:
  template:
    spec:
      containers:
        - name: my-service
          env:
            - name: KAFKA_BOOTSTRAP_SERVERS
              value: kafka.shared-resources:9092
            - name: PROMETHEUS_PUSHGATEWAY
              value: pushgateway.shared-monitoring:9091
```

### 3. Monitoring Integration
Services can integrate with the shared monitoring stack:

1. Add Prometheus annotations to your services:
```yaml
metadata:
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    prometheus.io/path: "/metrics"
```

2. Configure logging to send to the centralized logging stack:
```yaml
spec:
  template:
    spec:
      containers:
        - name: my-service
          env:
            - name: LOG_FORMAT
              value: "json"
            - name: LOG_LEVEL
              value: "info"
```

## Best Practices

1. **Namespace Organization**
   - Use project-specific namespaces for all your services
   - Keep service dependencies within the same namespace where possible
   - Use shared resources through service endpoints

2. **Resource Management**
   - Set appropriate resource requests and limits
   - Use horizontal pod autoscaling for scalable services
   - Monitor resource usage through the shared monitoring stack

3. **Security**
   - Follow the principle of least privilege
   - Use network policies to control traffic between namespaces
   - Keep sensitive configuration in Kubernetes secrets

4. **Monitoring**
   - Implement health checks
   - Export metrics in Prometheus format
   - Use structured logging

## Example Project Structure
```
my-project/
├── k8s/
│   ├── namespaces/
│   │   └── environments.yaml      # Project namespaces
│   ├── base/
│   │   ├── deployment.yaml
│   │   ├── service.yaml
│   │   └── kustomization.yaml
│   └── overlays/
│       ├── dev/
│       ├── staging/
│       └── prod/
└── services/
    └── my-service/
        ├── Dockerfile
        └── src/
```

## Troubleshooting

1. **Namespace Issues**
   - Ensure namespaces exist: `kubectl get ns`
   - Check namespace labels: `kubectl get ns <namespace> --show-labels`

2. **Resource Access**
   - Verify service endpoints: `kubectl -n shared-resources get endpoints`
   - Check network policies: `kubectl -n shared-resources get networkpolicies`

3. **Monitoring**
   - Verify metrics endpoints: `curl http://<service>:8080/metrics`
   - Check Prometheus targets: Access Prometheus UI in shared-monitoring namespace
