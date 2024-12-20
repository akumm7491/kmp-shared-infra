# Creating a New Project

This guide walks you through creating a new project using the KMP Shared Infrastructure.

## Prerequisites

- Access to your organization's Kubernetes cluster
- `kubectl` configured with appropriate permissions
- Docker installed locally
- Git access to the infrastructure repository

## Steps

### 1. Initialize Project

```bash
# Clone the infrastructure repository
git clone https://github.com/your-org/kmp-shared-infra
cd kmp-shared-infra

# Run the project creation script
./tools/scripts/create-project.sh \
  --name my-project \
  --namespace my-project-ns \
  --description "My new project description"
```

### 2. Configure Infrastructure Requirements

Edit `config/infrastructure.yaml`:

```yaml
project:
  name: my-project
  namespace: my-project-ns
  
infrastructure:
  compute:
    min_nodes: 2
    max_nodes: 5
  
  storage:
    timescaledb:
      enabled: true
      size: 50Gi
    redis:
      enabled: true
      replicas: 3
    
  messaging:
    kafka:
      enabled: true
      topics:
        - name: events
          partitions: 3
          retention: 7d
    
  monitoring:
    metrics: true
    logging: true
    tracing: true
```

### 3. Set Up Development Environment

```bash
# Install development dependencies
./tools/scripts/setup-dev.sh

# Start local development environment
./tools/scripts/start-dev.sh
```

### 4. Configure CI/CD

1. Set up GitHub Actions:
   - Copy `.github/workflows/templates/` to your project
   - Configure secrets in GitHub repository settings

2. Configure deployment environments:
   ```bash
   # Generate environment configurations
   ./tools/scripts/generate-env.sh --env dev
   ./tools/scripts/generate-env.sh --env staging
   ./tools/scripts/generate-env.sh --env prod
   ```

### 5. Deploy Infrastructure

```bash
# Deploy to development
./tools/scripts/deploy.sh --env dev

# Verify deployment
kubectl get pods -n my-project-ns
```

## Project Structure

Your new project will have this structure:
```
my-project/
├── .github/
│   └── workflows/          # CI/CD workflows
├── config/
│   ├── dev/               # Development configuration
│   ├── staging/           # Staging configuration
│   └── prod/              # Production configuration
├── services/              # Your microservices
├── infrastructure/        # Infrastructure configuration
└── tools/                # Development tools
```

## Next Steps

1. [Create Your First Service](new-service.md)
2. [Set Up Monitoring](../operations/monitoring/setup.md)
3. [Configure Authentication](../modules/auth/setup.md)
4. [Development Guidelines](../development/guidelines.md)

## Common Customizations

### Custom Resource Limits
```yaml
# config/resources.yaml
resources:
  limits:
    cpu: "4"
    memory: "8Gi"
  requests:
    cpu: "2"
    memory: "4Gi"
```

### Custom Network Policies
```yaml
# config/network.yaml
network:
  ingress:
    enabled: true
    hosts:
      - api.my-project.com
  egress:
    restricted: true
    allowed:
      - external-api.com
```

## Troubleshooting

See [Common Issues](../operations/troubleshooting/common-issues.md) for solutions to frequent problems.

## Support

For additional help:
1. Check the [FAQ](../FAQ.md)
2. Contact the infrastructure team
3. Open a GitHub issue
