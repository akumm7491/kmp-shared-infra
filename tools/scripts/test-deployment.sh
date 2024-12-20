#!/bin/bash
set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo "Starting deployment test..."

# 1. Check prerequisites
echo "Checking prerequisites..."
command -v kubectl >/dev/null 2>&1 || { echo >&2 "kubectl is required but not installed. Aborting."; exit 1; }
command -v helm >/dev/null 2>&1 || { echo >&2 "helm is required but not installed. Aborting."; exit 1; }
command -v docker >/dev/null 2>&1 || { echo >&2 "docker is required but not installed. Aborting."; exit 1; }

# 2. Build test service
echo "Building test service..."
./gradlew :microservices:test-service:build

# 3. Run tests
echo "Running tests..."
./gradlew :microservices:test-service:test

# 4. Build docker image
echo "Building docker image..."
docker build -t test-service:latest microservices/test-service

# 5. Deploy to local cluster
echo "Deploying to local cluster..."
kubectl create namespace test --dry-run=client -o yaml | kubectl apply -f -

helm upgrade --install test-service \
  ./deploy/helm/charts/service-base \
  --namespace test \
  --set image.repository=test-service \
  --set image.tag=latest \
  --set kafka.enabled=true \
  --set monitoring.enabled=true \
  --wait

# 6. Wait for deployment
echo "Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=60s deployment/test-service -n test

# 7. Test endpoints
echo "Testing endpoints..."
PORT=$(kubectl get svc -n test test-service -o jsonpath="{.spec.ports[0].nodePort}")

# Test health endpoint
HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/health)
if [ $HEALTH_STATUS -eq 200 ]; then
    echo -e "${GREEN}Health endpoint test passed${NC}"
else
    echo -e "${RED}Health endpoint test failed${NC}"
    exit 1
fi

# Test metrics endpoint
METRICS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/metrics)
if [ $METRICS_STATUS -eq 200 ]; then
    echo -e "${GREEN}Metrics endpoint test passed${NC}"
else
    echo -e "${RED}Metrics endpoint test failed${NC}"
    exit 1
fi

echo -e "${GREEN}All tests passed!${NC}"
