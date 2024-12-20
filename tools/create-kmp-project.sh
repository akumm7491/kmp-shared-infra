#!/bin/bash

# KMP Project Generator
# Usage: ./create-kmp-project.sh <project-name> <group-id> [--with-kafka] [--with-auth] [--cloud=aws|gcp|azure]

PROJECT_NAME=$1
GROUP_ID=$2
KAFKA_ENABLED=false
AUTH_ENABLED=false
CLOUD_PROVIDER="aws"

# Parse arguments
for arg in "$@"
do
    case $arg in
        --with-kafka)
        KAFKA_ENABLED=true
        shift
        ;;
        --with-auth)
        AUTH_ENABLED=true
        shift
        ;;
        --cloud=*)
        CLOUD_PROVIDER="${arg#*=}"
        shift
        ;;
    esac
done

# Validate required arguments
if [ -z "$PROJECT_NAME" ] || [ -z "$GROUP_ID" ]; then
    echo "Error: Project name and group ID are required"
    echo "Usage: ./create-kmp-project.sh <project-name> <group-id> [--with-kafka] [--with-auth] [--cloud=aws|gcp|azure]"
    exit 1
fi

# Create project structure
mkdir -p "${PROJECT_NAME}/microservices"
mkdir -p "${PROJECT_NAME}/deploy/helm"
mkdir -p "${PROJECT_NAME}/deploy/terraform/${CLOUD_PROVIDER}"

# Create root build.gradle.kts
cat > "${PROJECT_NAME}/build.gradle.kts" << EOL
plugins {
    kotlin("jvm") version "1.9.0" apply false
    kotlin("plugin.serialization") version "1.9.0" apply false
    id("io.ktor.plugin") version "2.3.6" apply false
}

allprojects {
    group = "${GROUP_ID}"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        // Add your organization's artifact repository here
        // maven("https://your-org-repo")
    }
}
EOL

# Create settings.gradle.kts with proper Kotlin string interpolation
KAFKA_MODULE=""
AUTH_MODULE=""

if [ "$KAFKA_ENABLED" = true ]; then
    KAFKA_MODULE='substitute(module("com.example.kmp:messaging-module")).using(project(":common-libs:messaging-module"))'
fi

if [ "$AUTH_ENABLED" = true ]; then
    AUTH_MODULE='substitute(module("com.example.kmp:auth-module")).using(project(":common-libs:auth-module"))'
fi

cat > "${PROJECT_NAME}/settings.gradle.kts" << EOL
rootProject.name = "$PROJECT_NAME"

// Include common infrastructure dependencies
includeBuild("../kmp-shared-infra") {
    dependencySubstitution {
        substitute(module("com.example.kmp:networking-module"))
            .using(project(":common-libs:networking-module"))
        substitute(module("com.example.kmp:monitoring-module"))
            .using(project(":common-libs:monitoring-module"))
        ${KAFKA_MODULE:+"$KAFKA_MODULE"}
        ${AUTH_MODULE:+"$AUTH_MODULE"}
    }
}
EOL

# Copy Kubernetes templates if they exist
if [ -d "infra/k8s/templates" ]; then
    cp -r infra/k8s/templates/* "${PROJECT_NAME}/deploy/helm/"
fi

# Create terraform configuration
cat > "${PROJECT_NAME}/deploy/terraform/${CLOUD_PROVIDER}/main.tf" << EOL
module "kmp_infrastructure" {
    source = "../../../../kmp-shared-infra/deploy/terraform/modules/${CLOUD_PROVIDER}"
    
    project_name    = "${PROJECT_NAME}"
    environment     = terraform.workspace
    kafka_enabled   = ${KAFKA_ENABLED}
    auth_enabled    = ${AUTH_ENABLED}
}
EOL

# Create GitHub Actions workflow
mkdir -p "${PROJECT_NAME}/.github/workflows"
cat > "${PROJECT_NAME}/.github/workflows/deploy.yml" << EOL
name: Deploy

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  deploy:
    uses: your-org/kmp-shared-infra/.github/workflows/deploy.yml@main
    with:
      project: ${PROJECT_NAME}
      cloud: ${CLOUD_PROVIDER}
    secrets: inherit
EOL

echo "Project ${PROJECT_NAME} created successfully!"
echo "Next steps:"
echo "1. cd ${PROJECT_NAME}"
echo "2. Update settings.gradle.kts with your organization's repository"
echo "3. Create your first microservice using the template service"
