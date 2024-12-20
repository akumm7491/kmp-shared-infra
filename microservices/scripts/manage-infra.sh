#!/bin/bash

COMMAND=$1
INFRA_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

function start_infra() {
    echo "Starting core infrastructure..."
    docker-compose -f "$INFRA_DIR/docker-compose.yml" up -d
    
    echo "Waiting for services to be ready..."
    wait_for_service "Service Registry" "http://localhost:8761"
    wait_for_service "Config Server" "http://localhost:8888"
    wait_for_service "API Gateway" "http://localhost:8000"
    
    echo "Core infrastructure is ready!"
}

function stop_infra() {
    echo "Stopping core infrastructure..."
    docker-compose -f "$INFRA_DIR/docker-compose.yml" down
}

function wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo "Waiting for $service_name to be ready..."
    while ! curl -s "$url" > /dev/null; do
        if [ $attempt -eq $max_attempts ]; then
            echo "Error: $service_name failed to start"
            exit 1
        fi
        echo "Attempt $attempt: $service_name not ready yet..."
        sleep 2
        ((attempt++))
    done
    echo "$service_name is ready!"
}

case $COMMAND in
    "start")
        start_infra
        ;;
    "stop")
        stop_infra
        ;;
    "restart")
        stop_infra
        start_infra
        ;;
    *)
        echo "Usage: $0 {start|stop|restart}"
        exit 1
        ;;
esac 