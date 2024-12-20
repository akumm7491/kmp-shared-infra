#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
COMPOSE_FILE="./docker-compose.yml"

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

check_dependencies() {
    local missing_deps=0
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        missing_deps=1
    fi

    if ! docker info &> /dev/null; then
        print_error "Docker daemon is not running"
        missing_deps=1
    fi

    if [ $missing_deps -eq 1 ]; then
        exit 1
    fi
}

check_service_health() {
    local service=$1
    local container_name="kmp-shared-infra-${service}-1"
    
    if [ "$(docker container inspect -f '{{.State.Status}}' $container_name 2>/dev/null)" == "running" ]; then
        echo -e "${GREEN}✓${NC} $service is healthy"
        return 0
    else
        echo -e "${RED}✗${NC} $service is not healthy"
        return 1
    fi
}

# Command functions
cmd_start() {
    print_status "Starting infrastructure services"
    check_dependencies
    docker compose up -d
    sleep 5
    cmd_status
}

cmd_stop() {
    print_status "Stopping infrastructure services"
    docker compose down
    print_status "All services stopped"
}

cmd_restart() {
    cmd_stop
    sleep 5
    cmd_start
}

cmd_status() {
    print_status "Checking infrastructure status"
    
    # Check core infrastructure
    echo "Core Infrastructure:"
    check_service_health "zookeeper"
    check_service_health "kafka"
    check_service_health "schema-registry"
    
    # Check monitoring stack
    echo -e "\nMonitoring Stack:"
    check_service_health "prometheus"
    check_service_health "grafana"
    
    # Show ports
    echo -e "\nService Endpoints:"
    echo "Grafana:         http://localhost:3000 (admin/admin)"
    echo "Prometheus:      http://localhost:9090"
    echo "Schema Registry: http://localhost:8081"
    echo "Kafka:          localhost:9092"
}

cmd_logs() {
    local service=$1
    
    if [ -z "$service" ]; then
        print_error "Please specify a service name (e.g. ./dev.sh logs kafka)"
        echo "Available services:"
        echo "  - kafka"
        echo "  - zookeeper"
        echo "  - schema-registry"
        echo "  - prometheus"
        echo "  - grafana"
        exit 1
    fi
    
    docker compose logs -f $service
}

cmd_help() {
    echo "Usage: ./dev.sh COMMAND"
    echo ""
    echo "Commands:"
    echo "  start     Start all infrastructure services"
    echo "  stop      Stop all infrastructure services"
    echo "  restart   Restart all services"
    echo "  status    Check the status of all services"
    echo "  logs      View logs for a specific service (e.g. ./dev.sh logs kafka)"
    echo "  help      Show this help message"
}

# Main script
case "$1" in
    start)
        cmd_start
        ;;
    stop)
        cmd_stop
        ;;
    restart)
        cmd_restart
        ;;
    status)
        cmd_status
        ;;
    logs)
        cmd_logs $2
        ;;
    help|--help|-h)
        cmd_help
        ;;
    *)
        print_error "Unknown command: $1"
        cmd_help
        exit 1
        ;;
esac
