# Weather Service Example

This example demonstrates how to create a service that leverages the KMP shared infrastructure components.

## Features

- Uses shared infrastructure components:
  - Service Registry for service discovery
  - Config Server for centralized configuration
  - Monitoring Module for metrics and alerts
  - Messaging Module for event publishing
  - Networking Module for HTTP client configuration

## Architecture

- REST API endpoint for weather data
- Event publishing for weather updates
- Prometheus metrics integration
- Service discovery registration

## Prerequisites

1. Start the core infrastructure:
```bash
cd microservices
docker-compose up -d
```

2. Ensure the following networks exist:
```bash
docker network create kmp-core-network
docker network create kmp-monitoring-network
```

## Running the Service

1. Start the weather service:
```bash
cd examples/weather-service
docker-compose up -d
```

2. Test the API:
```bash
curl http://localhost:8084/api/v1/weather/london
```

3. View metrics:
```bash
curl http://localhost:8084/metrics
```

## Monitoring

The service exposes the following metrics:
- `weather_requests_total`: Counter of total weather requests
- `weather_request_duration`: Timer for request duration

Alerts are configured for:
- High request rates (>100 req/sec)
- Slow requests (>500ms avg)

## Infrastructure Integration

- Registers with Service Registry (Eureka) at startup
- Fetches configuration from Config Server
- Publishes events to Kafka
- Exposes metrics to Prometheus
- Visualizes metrics in Grafana

## Docker Networks

The service operates in multiple networks:
- `weather-network`: Internal network for weather services
- `kmp-core-network`: Connects to core infrastructure
- `kmp-monitoring-network`: Exposes metrics to monitoring stack
