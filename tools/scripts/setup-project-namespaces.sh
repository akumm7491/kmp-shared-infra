#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
PROJECT_NAME=""
TEMPLATE_PATH="infra/k8s/templates/project-namespaces.yaml"

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
    echo "Usage: ./setup-project-namespaces.sh --project <project-name>"
    echo ""
    echo "Options:"
    echo "  --project    Name of your project (required)"
    echo "  --help       Show this help message"
    echo ""
    echo "Example:"
    echo "  ./setup-project-namespaces.sh --project my-app"
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --project)
            PROJECT_NAME="$2"
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
if [ -z "$PROJECT_NAME" ]; then
    print_error "Project name is required"
    show_help
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

print_status "Setting up namespaces for project: $PROJECT_NAME"

# Create temporary file with substituted values
TEMP_FILE=$(mktemp)
export PROJECT_NAME
envsubst < "$TEMPLATE_PATH" > "$TEMP_FILE"

# Apply the configuration
if kubectl apply -f "$TEMP_FILE"; then
    print_status "Successfully created namespaces for $PROJECT_NAME"
    echo "Created namespaces:"
    echo "  - ${PROJECT_NAME}-dev"
    echo "  - ${PROJECT_NAME}-staging"
    echo "  - ${PROJECT_NAME}-prod"
else
    print_error "Failed to create namespaces"
    rm "$TEMP_FILE"
    exit 1
fi

# Clean up
rm "$TEMP_FILE"

print_status "Next steps:"
echo "1. Update your Kubernetes configurations to use these namespaces:"
echo "   - Use ${PROJECT_NAME}-dev for development"
echo "   - Use ${PROJECT_NAME}-staging for staging"
echo "   - Use ${PROJECT_NAME}-prod for production"
echo ""
echo "2. Configure access to shared resources:"
echo "   - Kafka: kafka.shared-resources:9092"
echo "   - Prometheus: prometheus.shared-monitoring:9090"
echo "   - Grafana: grafana.shared-monitoring:3000"
echo ""
echo "3. Set up monitoring:"
echo "   - Add Prometheus annotations to your services"
echo "   - Configure logging to use the centralized logging stack"
echo ""
echo "For more information, see docs/infrastructure/README.md"
