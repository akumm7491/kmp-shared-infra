#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Helper functions
print_error() {
    echo -e "${RED}ERROR: $1${NC}"
}

show_help() {
    echo "Usage: ./create-service.sh --name <service-name> --type <http|event|worker> --modules <comma-separated-modules> --namespace <namespace> [options]"
    echo ""
    echo "Required:"
    echo "  --name        Name of the service"
    echo "  --namespace   Kubernetes namespace"
    echo ""
    echo "Optional:"
    echo "  --type        Service type (http|event|worker) (default: http)"
    echo "  --modules     Comma-separated list of modules to include (auth,messaging,monitoring,etc.)"
    echo "  --port        Port number (default: 8080)"
    echo "  --help        Show this help message"
    echo ""
    echo "Example:"
    echo "  ./create-service.sh --name my-service --type http --modules auth,monitoring --namespace my-project-dev"
}

# Parse arguments to pass to the Kotlin application
ARGS=""
while [[ $# -gt 0 ]]; do
    case $1 in
        --name)
            ARGS="$ARGS --name $2"
            shift 2
            ;;
        --type)
            ARGS="$ARGS --type $2"
            shift 2
            ;;
        --modules)
            ARGS="$ARGS --modules $2"
            shift 2
            ;;
        --namespace)
            ARGS="$ARGS --namespace $2"
            shift 2
            ;;
        --port)
            ARGS="$ARGS --port $2"
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

# Build and run the Kotlin application
./gradlew :tools:build
java -jar tools/build/libs/tools.jar $ARGS
