#!/bin/bash
set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_status() {
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ $1${NC}"
    else
        echo -e "${RED}✗ $1${NC}"
        exit 1
    fi
}

echo "🔍 Verifying KMP Shared Infrastructure..."

# 1. Check Prerequisites
echo -e "\n${YELLOW}Checking Prerequisites...${NC}"
command -v kubectl >/dev/null 2>&1 || { echo >&2 "kubectl is required but not installed. Aborting."; exit 1; }
command -v helm >/dev/null 2>&1 || { echo >&2 "helm is required but not installed. Aborting."; exit 1; }
command -v docker >/dev/null 2>&1 || { echo >&2 "docker is required but not installed. Aborting."; exit 1; }
print_status "Prerequisites verified"

# 2. Create test namespace
echo -e "\n${YELLOW}Setting up test environment...${NC}"
kubectl create namespace kmp-test --dry-run=client -o yaml | kubectl apply -f -
print_status "Test namespace created"

# 3. Deploy Infrastructure Components
echo -e "\n${YELLOW}Deploying infrastructure components...${NC}"

# Deploy Kafka
helm upgrade --install kafka bitnami/kafka \
    --namespace kmp-test \
    --set persistence.enabled=false \
    --wait
print_status "Kafka deployed"

# Deploy Monitoring Stack
helm upgrade --install prometheus prometheus-community/kube-prometheus-stack \
    --namespace kmp-test \
    --set grafana.enabled=true \
    --set prometheus.enabled=true \
    --wait
print_status "Monitoring stack deployed"

# 4. Build and Deploy Test Service
echo -e "\n${YELLOW}Building test service...${NC}"
./gradlew :examples:template-service:build
print_status "Service built"

echo -e "\n${YELLOW}Building Docker image...${NC}"
docker build -t template-service:test examples/template-service
print_status "Docker image built"

echo -e "\n${YELLOW}Deploying test service...${NC}"
helm upgrade --install template-service ./infra/helm/charts/service \
    --namespace kmp-test \
    --set image.repository=template-service \
    --set image.tag=test \
    --set kafka.enabled=true \
    --set monitoring.enabled=true \
    --wait
print_status "Test service deployed"

# 5. Verify Components
echo -e "\n${YELLOW}Verifying components...${NC}"

# Wait for service to be ready
kubectl wait --for=condition=available --timeout=60s deployment/template-service -n kmp-test
print_status "Service is running"

# Get service port
PORT=$(kubectl get svc -n kmp-test template-service -o jsonpath="{.spec.ports[0].nodePort}")

# Test endpoints
echo -e "\n${YELLOW}Testing endpoints...${NC}"

# Health check
HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/health)
if [ "$HEALTH_STATUS" -eq 200 ]; then
    print_status "Health endpoint OK"
else
    echo -e "${RED}✗ Health endpoint returned $HEALTH_STATUS${NC}"
    exit 1
fi

# Metrics check
METRICS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/metrics)
if [ "$METRICS_STATUS" -eq 200 ]; then
    print_status "Metrics endpoint OK"
else
    echo -e "${RED}✗ Metrics endpoint returned $METRICS_STATUS${NC}"
    exit 1
fi

# 6. Verify Monitoring
echo -e "\n${YELLOW}Verifying monitoring...${NC}"

# Check if Prometheus can scrape metrics
PROMETHEUS_POD=$(kubectl get pods -n kmp-test -l app=prometheus -o jsonpath="{.items[0].metadata.name}")
kubectl exec -n kmp-test $PROMETHEUS_POD -- curl -s localhost:9090/api/v1/targets | grep "template-service" > /dev/null
print_status "Prometheus scraping metrics"

# Check if Grafana is running
GRAFANA_POD=$(kubectl get pods -n kmp-test -l app=grafana -o jsonpath="{.items[0].metadata.name}")
kubectl exec -n kmp-test $GRAFANA_POD -- curl -s localhost:3000/api/health | grep "ok" > /dev/null
print_status "Grafana running"

# 7. Verify Logging
echo -e "\n${YELLOW}Verifying logging...${NC}"
kubectl logs -n kmp-test deployment/template-service --tail=1 | grep "JSON" > /dev/null
print_status "Structured logging verified"

# 8. Clean up
echo -e "\n${YELLOW}Cleaning up...${NC}"
kubectl delete namespace kmp-test
print_status "Test namespace cleaned up"

echo -e "\n${GREEN}✅ All infrastructure components verified successfully!${NC}"
