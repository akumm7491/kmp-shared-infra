# Kubernetes Configuration Structure

```
k8s/
├── base/                   # Base configurations
│   ├── common/            # Common resources (monitoring, kafka, etc)
│   └── templates/         # Service templates
├── overlays/              # Environment-specific configurations
│   ├── dev/
│   │   ├── service-a/     # Service A dev configuration
│   │   ├── service-b/     # Service B dev configuration
│   │   └── kustomization.yaml
│   ├── staging/
│   └── prod/
└── modules/               # Reusable modules
    ├── monitoring/       
    ├── networking/
    └── kafka/
```

## Usage

To deploy a service to a specific environment and namespace:

```bash
# Deploy service-a to dev environment in namespace my-project
kustomize build k8s/overlays/dev/service-a | kubectl apply -f - -n my-project

# Deploy service-b to prod environment in namespace another-project
kustomize build k8s/overlays/prod/service-b | kubectl apply -f - -n another-project
```

## Structure Details

1. **base/** - Contains base configurations that are common across all deployments
   - **common/** - Shared resources like monitoring configs
   - **templates/** - Generic templates for services

2. **overlays/** - Environment-specific configurations
   - Each service has its own directory per environment
   - Allows for environment-specific customizations
   - Supports different namespaces per deployment

3. **modules/** - Reusable Kubernetes configurations
   - Can be included in any service deployment
   - Configurable through kustomize parameters

## Best Practices

1. Use kustomize patches for environment-specific changes
2. Keep sensitive data in Kubernetes secrets
3. Use configmaps for environment-specific configurations
4. Maintain consistent resource naming across environments
