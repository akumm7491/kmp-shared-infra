# KMP Shared Infrastructure Project

This project demonstrates a Kotlin Multiplatform (KMP) microservices architecture with shared modules and infrastructure components.

## Project Structure

```
my-kmp-project/
├── common-libs/              # Shared KMP modules
│   ├── auth-module/         # Authentication and authorization
│   ├── messaging-module/    # Kafka/event bus integration
│   └── storage-module/      # Database/storage utilities
├── template-service/        # Template for new microservices
├── microservices/          # Concrete service implementations
│   ├── service-a/
│   └── service-b/
├── infra/                  # Infrastructure configurations
│   ├── docker-compose.yml
│   ├── prometheus/
│   └── k8s-manifests/
└── tooling/                # Development tools
    └── ScaffoldTool.kt     # Service scaffolding utility
```

## Getting Started

### Prerequisites

- JDK 17+
- Docker and Docker Compose
- Kubernetes cluster (optional)

### Building the Project

```bash
./gradlew build
```

### Running Locally with Docker Compose

```bash
cd infra
docker-compose up
```

This will start:
- Kafka and Zookeeper
- Service A (port 8081)
- Service B (port 8082)
- Prometheus (port 9090)
- Grafana (port 3000)

### Creating a New Service

Use the scaffold tool:

```bash
cd tooling
./gradlew run --args="--service-name=my-new-service --port=8083"
```

### Testing

Run all tests:
```bash
./gradlew test
```

### Deployment

For Kubernetes deployment:
```bash
kubectl apply -f infra/k8s-manifests/
```

## Shared Modules

### Auth Module
Provides token validation and authentication utilities.

### Messaging Module
Implements event bus functionality using Kafka.

### Storage Module
Provides abstract storage operations for databases or file systems.

## Monitoring

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

## Contributing

1. Create a feature branch
2. Make your changes
3. Submit a pull request

## License

This project is licensed under the MIT License.
