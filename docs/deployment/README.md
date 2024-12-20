# Deployment Guide

## Infrastructure Setup

### Prerequisites
- Terraform >= 1.0
- Helm >= 3.0
- kubectl
- Cloud provider CLI tools (aws, gcloud, az)

### Cloud Provider Setup

#### AWS
```bash
# Initialize Terraform
cd deploy/terraform/aws/dev
terraform init

# Apply infrastructure
terraform apply
```

#### GCP
```bash
# Initialize Terraform
cd deploy/terraform/gcp/prod
terraform init

# Apply infrastructure
terraform apply
```

### Helm Deployment

1. **Base Service Deployment**
```bash
# Deploy to dev
helm upgrade --install myservice ./deploy/helm/charts/service-base \
  -f ./deploy/helm/values/dev/values.yaml \
  --namespace dev

# Deploy to prod
helm upgrade --install myservice ./deploy/helm/charts/service-base \
  -f ./deploy/helm/values/prod/values.yaml \
  --namespace prod
```

2. **Monitoring Stack**
```bash
helm upgrade --install monitoring ./deploy/helm/charts/monitoring \
  -f ./deploy/helm/values/prod/values.yaml \
  --namespace monitoring
```

## CI/CD Setup

### GitHub Actions
1. Add required secrets:
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`
   - `DOCKER_USERNAME`
   - `DOCKER_PASSWORD`

### GitLab CI
1. Configure GitLab variables:
   - `KUBE_CONFIG`
   - `DOCKER_REGISTRY_USER`
   - `DOCKER_REGISTRY_PASSWORD`

## Environment Configuration

### Development
- Resource limits optimized for local development
- Debug logging enabled
- Local Kafka instance

### Staging
- Moderate resource limits
- Basic monitoring and alerting
- Multi-replica services

### Production
- High availability configuration
- Advanced monitoring and alerting
- Auto-scaling enabled
- Production-grade security measures
