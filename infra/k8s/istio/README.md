# Istio Service Mesh Configuration

This directory contains the Istio service mesh configuration for the KMP Shared Infrastructure project. The setup provides service mesh capabilities including traffic management, observability, and security features.

## 🚀 Prerequisites

### System Requirements
- CPU: 4+ cores recommended
- RAM: 8GB+ recommended
- Disk: 20GB+ free space
- Docker Desktop (Mac/Windows) or Docker Engine (Linux)
- Homebrew (Mac) or equivalent package manager

### Installing Kubernetes
If you don't have Kubernetes installed:

1. For local development (choose one):
   ```bash
   # Option 1: Docker Desktop with Kubernetes
   # 1. Open Docker Desktop
   # 2. Go to Preferences/Settings
   # 3. Select Kubernetes tab
   # 4. Check "Enable Kubernetes"
   # 5. Click Apply & Restart

   # Option 2: Minikube
   brew install minikube
   minikube start --memory=4096 --cpus=4
   # For Apple Silicon Macs, add: --driver=docker

   # Option 3: Kind
   brew install kind
   kind create cluster --name kmp-cluster
   ```

2. Install kubectl:
   ```bash
   brew install kubectl
   
   # Verify installation
   kubectl version
   kubectl cluster-info
   ```

### Installing Istio

1. Install Istio CLI:
   ```bash
   brew install istioctl

   # Verify installation
   istioctl version
   ```

2. Install Prometheus Operator (required for monitoring):
   ```bash
   # Add Helm repo
   helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
   helm repo update

   # Create namespace
   kubectl create namespace monitoring

   # Install operator
   helm install prometheus prometheus-community/kube-prometheus-stack \
     --namespace monitoring \
     --set grafana.enabled=true \
     --set prometheus.enabled=true
   ```

3. Install Kiali (optional, for service mesh visualization):
   ```bash
   kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.24/samples/addons/kiali.yaml
   ```

### Troubleshooting Prerequisites

1. Docker Issues:
   ```bash
   # Check Docker status
   docker info
   # If permission error on Linux:
   sudo usermod -aG docker $USER
   ```

2. Kubernetes Issues:
   ```bash
   # Check if kubectl can access cluster
   kubectl get nodes
   
   # If using Docker Desktop:
   # 1. Reset Kubernetes cluster in preferences
   # 2. Reset Docker Desktop
   
   # If using Minikube:
   minikube delete
   minikube start --memory=4096 --cpus=4
   ```

3. Resource Issues:
   - Increase Docker Desktop resources in preferences
   - Close unnecessary applications
   - For M1/M2 Macs, ensure Rosetta 2 is installed:
     ```bash
     softwareupdate --install-rosetta
     ```

## 🚀 Quick Start

```bash
# 1. Install Istio
istioctl install -f base/mesh.yaml --skip-confirmation

# 2. Enable sidecar injection
kubectl label namespace default istio-injection=enabled

# 3. Apply configurations
kubectl apply -k .
```

## 📁 Directory Structure

```
istio/
├── base/           # Core Istio installation
│   └── mesh.yaml   # Base mesh configuration
├── monitoring/     # Telemetry setup
│   └── monitoring.yaml
└── gateway/        # Ingress configuration
    └── gateway.yaml
```

## 🔧 Configuration Details

### Base Configuration (`base/mesh.yaml`)
- Default Istio profile with customized settings
- Enabled components:
  - Base Istio functionality
  - Pilot for service discovery
  - Ingress/Egress gateways
  - Automatic sidecar injection
  - Telemetry collection

### Monitoring (`monitoring/monitoring.yaml`)
- Prometheus metrics collection
- Zipkin distributed tracing
- Custom metrics configuration
- ServiceMonitor for Prometheus Operator

### Gateway (`gateway/gateway.yaml`)
- HTTP/HTTPS ingress configuration
- Path-based routing rules
- Load balancing settings
- Circuit breaking configuration

## 🌐 Service Access

### Internal Services
Services are accessible through the Istio Gateway:
- Template Service: `http://localhost/template-service/*`
- Weather Service: `http://localhost/weather-service/*`

### Monitoring Stack
- Prometheus: `http://prometheus:9090`
  - Metrics collection and storage
- Grafana: `http://grafana:3000`
  - Metrics visualization and dashboards
- Kiali: `http://kiali:20001`
  - Service mesh visualization
- Zipkin: `http://zipkin:9411`
  - Distributed tracing

## 🛠 Advanced Configuration

### Custom Metrics
To add custom metrics, modify `monitoring/monitoring.yaml`:
```yaml
spec:
  metrics:
    - providers:
      - name: prometheus
      # Add custom metric configurations here
```

### Traffic Management
Modify `gateway/gateway.yaml` for:
- Custom routing rules
- Traffic splitting
- Fault injection
- Circuit breaking

### Security
Enable additional security features in `base/mesh.yaml`:
- mTLS
- Authorization policies
- Rate limiting

## 🔍 Troubleshooting

### Common Commands
```bash
# Check pod status and sidecar injection
kubectl get pods -n default

# Verify Istio proxy logs
kubectl logs <pod-name> -c istio-proxy

# Check gateway status
kubectl get gateway,virtualservice,destinationrule

# View Istio configuration
istioctl analyze
```

### Common Issues

1. Sidecar Not Injected
   ```bash
   # Verify namespace label
   kubectl get namespace default --show-labels
   # Re-apply label if needed
   kubectl label namespace default istio-injection=enabled --overwrite
   ```

2. Gateway Unreachable
   ```bash
   # Check gateway service
   kubectl get svc istio-ingressgateway -n istio-system
   # Verify gateway configuration
   kubectl describe gateway kmp-gateway
   ```

3. Metrics Not Showing
   ```bash
   # Check ServiceMonitor
   kubectl get servicemonitor -n monitoring
   # Verify Prometheus target configuration
   kubectl port-forward svc/prometheus -n monitoring 9090:9090
   # Then visit localhost:9090/targets
   ```

## 📚 Additional Resources

- [Istio Documentation](https://istio.io/docs/)
- [Prometheus Operator](https://prometheus-operator.dev/)
- [Kiali Documentation](https://kiali.io/docs/)
- [Zipkin Documentation](https://zipkin.io/) 