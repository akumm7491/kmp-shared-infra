#!/bin/bash

# Check if service registry is available
max_attempts=30
attempt=1
echo "Waiting for service registry to be available..."

# First check if service registry container exists and is running
if ! docker ps | grep -q service-registry; then
    echo "Error: service-registry container is not running"
    echo "Please start the core infrastructure first with: docker-compose up -d service-registry"
    exit 1
fi

while [ $attempt -le $max_attempts ]; do
    if curl -f "http://localhost:8761/actuator/health" >/dev/null 2>&1; then
        echo "Service registry is available!"
        exit 0
    fi
    
    echo "Attempt $attempt of $max_attempts: Service registry not ready yet..."
    
    # On first attempt, show detailed connection info
    if [ $attempt -eq 1 ]; then
        echo -e "\nChecking service registry status..."
        echo "Container status:"
        docker ps --filter name=service-registry --format "{{.Status}}"
        echo -e "\nTrying connection with verbose output:"
        curl -v "http://localhost:8761/actuator/health" 2>&1 || true
        echo -e "\n"
    fi
    
    sleep 2
    attempt=$((attempt + 1))
done

echo "Error: Service registry did not become available in time"
exit 1
