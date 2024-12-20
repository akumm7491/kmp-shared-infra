# KMP Shared Infrastructure Modules

This directory contains reusable Kubernetes modules that can be used by any project, even those in different repositories.

## Using These Modules in Your Project

### 1. Reference the Module

In your project's `kustomization.yaml`:

```yaml
resources:
  - github.com/your-org/kmp-shared-infra/k8s/modules/service?ref=v1.0.0

namePrefix: myproject-  # Your project prefix

commonLabels:
  app.kubernetes.io/part-of: your-project-name
  environment: dev  # or staging, prod
```

### 2. Configure Your Service

Create a ConfigMap for your service configuration:

```yaml
configMapGenerator:
  - name: service-config
    literals:
      - SERVICE_NAME=your-service-name
      - ENVIRONMENT=dev
      - LOG_LEVEL=DEBUG
```

### 3. Customize Resources

Apply patches for your specific needs:

```yaml
patches:
  - target:
      kind: Deployment
      name: .*
    patch: |-
      - op: replace
        path: /spec/template/spec/containers/0/image
        value: your-registry/your-service:latest
      - op: replace
        path: /spec/replicas
        value: 2
```

### 4. Deploy

```bash
# Deploy to your namespace
kubectl create namespace your-namespace  # if it doesn't exist
kustomize build k8s | kubectl apply -f - -n your-namespace
```

## Available Modules

### Service Module
- Base service deployment with best practices
- Monitoring integration
- Resource management
- Network policies

### Monitoring Module
- Prometheus integration
- Grafana dashboards
- Alert configurations

### Networking Module
- Service mesh integration
- Network policies
- Ingress configurations

## Best Practices

1. Always specify a version tag when referencing modules
2. Use namespaces for isolation
3. Override resource limits based on your needs
4. Add appropriate labels for organization

## Example Project Structure

```
your-project/
├── k8s/
│   ├── base/
│   │   └── kustomization.yaml  # References KMP modules
│   └── overlays/
│       ├── dev/
│       ├── staging/
│       └── prod/
└── src/
    └── your-service-code/
```

## Version Compatibility

- v1.x.x - Compatible with Kubernetes 1.24+
- Each release is tagged and documented
- Breaking changes are noted in release notes
