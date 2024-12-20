#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
SERVICE_NAME=""
PROJECT_NAME=""
ENVIRONMENT=""
CONTAINER_PORT="8080"
REPLICAS="1"
TEMPLATE_PATH="infra/k8s/templates/service-template.yaml"

# Helper functions
print_status() {
    echo -e "${GREEN}=== $1 ===${NC}"
}

print_error() {
    echo -e "${RED}ERROR: $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}WARNING: $1${NC}"
}

show_help() {
    echo "Usage: ./deploy-service.sh --service <service-name> --project <project-name> --env <environment> [options]"
    echo ""
    echo "Required:"
    echo "  --service    Name of your service"
    echo "  --project    Name of your project"
    echo "  --env        Environment (dev/staging/prod)"
    echo ""
    echo "Options:"
    echo "  --port       Container port (default: 8080)"
    echo "  --replicas   Number of replicas (default: 1)"
    echo "  --help       Show this help message"
    echo ""
    echo "Example:"
    echo "  ./deploy-service.sh --service my-service --project my-app --env dev --port 8081 --replicas 2"
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --service)
            SERVICE_NAME="$2"
            shift 2
            ;;
        --project)
            PROJECT_NAME="$2"
            shift 2
            ;;
        --env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --port)
            CONTAINER_PORT="$2"
            shift 2
            ;;
        --replicas)
            REPLICAS="$2"
            shift 2
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            print_error "Unknown parameter: $1"
            show_help
            exit 1
            ;;
    esac
done

# Validate required arguments
if [ -z "$SERVICE_NAME" ] || [ -z "$PROJECT_NAME" ] || [ -z "$ENVIRONMENT" ]; then
    print_error "Service name, project name, and environment are required"
    show_help
    exit 1
fi

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|staging|prod)$ ]]; then
    print_error "Environment must be one of: dev, staging, prod"
    exit 1
fi

# Check dependencies
if ! command -v kubectl &> /dev/null; then
    print_error "kubectl is not installed"
    exit 1
fi

if ! command -v envsubst &> /dev/null; then
    print_error "envsubst is not installed"
    exit 1
fi

# Check if template exists
if [ ! -f "$TEMPLATE_PATH" ]; then
    print_error "Template file not found: $TEMPLATE_PATH"
    exit 1
fi

# Check if namespace exists
if ! kubectl get namespace "${PROJECT_NAME}-${ENVIRONMENT}" &> /dev/null; then
    print_error "Namespace ${PROJECT_NAME}-${ENVIRONMENT} does not exist"
    echo "Run setup-project-namespaces.sh first to create the required namespaces"
    exit 1
fi

print_status "Deploying service: $SERVICE_NAME"
echo "Project: $PROJECT_NAME"
echo "Environment: $ENVIRONMENT"
echo "Container Port: $CONTAINER_PORT"
echo "Replicas: $REPLICAS"

# Create temporary file with substituted values
TEMP_FILE=$(mktemp)
export SERVICE_NAME PROJECT_NAME ENVIRONMENT CONTAINER_PORT REPLICAS
envsubst < "$TEMPLATE_PATH" > "$TEMP_FILE"

# Apply the configuration
if kubectl apply -f "$TEMP_FILE"; then
    print_status "Successfully deployed $SERVICE_NAME to ${PROJECT_NAME}-${ENVIRONMENT}"
    
    # Wait for deployment to be ready
    print_status "Waiting for deployment to be ready..."
    if kubectl -n "${PROJECT_NAME}-${ENVIRONMENT}" rollout status deployment/"${SERVICE_NAME}"; then
        print_status "Deployment is ready"
        
        # Get service URL
        CLUSTER_IP=$(kubectl -n "${PROJECT_NAME}-${ENVIRONMENT}" get service "${SERVICE_NAME}" -o jsonpath='{.spec.clusterIP}')
        echo ""
        echo "Service is available at: http://${CLUSTER_IP}"
        echo "Internal service URL: ${SERVICE_NAME}.${PROJECT_NAME}-${ENVIRONMENT}.svc.cluster.local"
        
        # Show monitoring URLs
        echo ""
        echo "Monitoring:"
        echo "- Metrics endpoint: http://${CLUSTER_IP}/metrics"
        echo "- Health endpoint: http://${CLUSTER_IP}/health"
        echo "- View in Grafana: http://grafana.shared-monitoring:3000"
    else
        print_error "Deployment failed to become ready"
        kubectl -n "${PROJECT_NAME}-${ENVIRONMENT}" get pods -l app="${SERVICE_NAME}"
        rm "$TEMP_FILE"
        exit 1
    fi
else
    print_error "Failed to deploy service"
    rm "$TEMP_FILE"
    exit 1
fi

# Clean up
rm "$TEMP_FILE"

print_status "Next steps:"
echo "1. Monitor your service:"
echo "   - Check logs: kubectl -n ${PROJECT_NAME}-${ENVIRONMENT} logs -l app=${SERVICE_NAME}"
echo "   - View metrics in Grafana: http://grafana.shared-monitoring:3000"
echo ""
echo "2. Test your service:"
echo "   - Health check: curl http://${CLUSTER_IP}/health"
echo "   - Metrics: curl http://${CLUSTER_IP}/metrics"
echo ""
echo "3. Scale your service (if needed):"
echo "   kubectl -n ${PROJECT_NAME}-${ENVIRONMENT} scale deployment/${SERVICE_NAME} --replicas=<count>"
echo ""
echo "For more information, see docs/infrastructure/README.md"
