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

echo "🔄 Running Integration Tests..."

# 1. Build all modules
echo -e "\n${YELLOW}Building common modules...${NC}"
./gradlew :common-libs:auth-module:build \
         :common-libs:messaging-module:build \
         :common-libs:monitoring-module:build \
         :common-libs:networking-module:build \
         :common-libs:storage-module:build
print_status "Common modules built"

# 2. Run unit tests
echo -e "\n${YELLOW}Running unit tests...${NC}"
./gradlew test
print_status "Unit tests passed"

# 3. Start local infrastructure
echo -e "\n${YELLOW}Starting local infrastructure...${NC}"
docker-compose up -d
print_status "Local infrastructure started"

# 4. Run integration tests
echo -e "\n${YELLOW}Running integration tests...${NC}"

# Test Auth Module
echo "Testing Auth Module..."
./gradlew :common-libs:auth-module:integrationTest
print_status "Auth module tests passed"

# Test Messaging Module
echo "Testing Messaging Module..."
./gradlew :common-libs:messaging-module:integrationTest
print_status "Messaging module tests passed"

# Test Storage Module
echo "Testing Storage Module..."
./gradlew :common-libs:storage-module:integrationTest
print_status "Storage module tests passed"

# 5. Test example service
echo -e "\n${YELLOW}Testing example service...${NC}"
./gradlew :examples:template-service:build
docker build -t template-service:test examples/template-service
docker run -d --name test-service template-service:test

# Wait for service to start
sleep 5

# Test endpoints
HEALTH_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/health)
if [ "$HEALTH_STATUS" -eq 200 ]; then
    print_status "Service health check passed"
else
    echo -e "${RED}✗ Service health check failed${NC}"
    exit 1
fi

# Clean up
docker stop test-service
docker rm test-service

# 6. Clean up
echo -e "\n${YELLOW}Cleaning up...${NC}"
docker-compose down
print_status "Local infrastructure cleaned up"

echo -e "\n${GREEN}✅ All integration tests passed successfully!${NC}"
