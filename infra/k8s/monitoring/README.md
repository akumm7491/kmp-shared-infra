# Monitoring Infrastructure

This directory contains a reusable monitoring setup using Prometheus and Grafana for Kotlin Multiplatform services.

## Features

- Prometheus for metrics collection
- Grafana for visualization
- Auto-provisioned datasources and dashboards
- Service discovery via file-based configuration
- Generic service metrics dashboard

## Directory Structure

```
monitoring/
├── docker-compose.yml
├── prometheus/
│   ├── prometheus.yml
│   └── targets/
│       └── services.yml
└── grafana/
    ├── provisioning/
    │   ├── datasources/
    │   │   └── prometheus.yml
    │   └── dashboards/
    │       └── dashboards.yml
    └── dashboards/
        └── service-metrics.json
```

## Quick Start

1. Copy this directory to your project
2. Update `prometheus/targets/services.yml` with your service names and ports
3. Run the monitoring stack:
   ```bash
   docker-compose up -d
   ```
4. Access Grafana at http://localhost:3000 (admin/admin)

## Service Integration

1. Ensure your services expose Prometheus metrics at `/metrics`
2. Add your service to `prometheus/targets/services.yml`:
   ```yaml
   - targets:
       - 'your-service:8080'
     labels:
       service: 'your-service'
       env: 'local'
   ```

## Dashboard Details

The default dashboard includes:
- Average response time by service and endpoint
- Request rate by service and endpoint
- Active requests by service
- Overall success rate

## Customization

1. Add custom dashboards to `grafana/dashboards/`
2. Update Prometheus scrape configs in `prometheus/prometheus.yml`
3. Modify Grafana settings in `docker-compose.yml`
