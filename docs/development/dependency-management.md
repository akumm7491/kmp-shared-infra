# Dependency Management Guide

This guide explains how to use the centralized dependency management system in KMP Shared Infrastructure.

## Quick Start

1. **Use the KMP Service Plugin**
```kotlin
// build.gradle.kts
plugins {
    id("com.example.kmp.service")  // Provides all standard dependencies
}
```

2. **Add Common Modules**
```kotlin
dependencies {
    implementation(project(":common-libs:auth-module"))
    implementation(project(":common-libs:messaging-module"))
    // Add other modules as needed
}
```

## Available Modules

### 1. Auth Module
- Authentication providers
- Authorization handlers
- Security utilities

### 2. Messaging Module
- Kafka integration
- Event handling
- Message serialization

### 3. Storage Module
- Database clients
- Cache implementations
- Data migration tools

### 4. Monitoring Module
- Metrics collection
- Logging utilities
- Tracing integration

### 5. Networking Module
- HTTP clients
- Service discovery
- Load balancing

## Version Management

All dependency versions are managed centrally in `buildSrc/src/main/kotlin/com/example/kmp/gradle/Dependencies.kt`:

```kotlin
object Versions {
    const val kotlin = "1.9.20"
    const val ktor = "2.3.6"
    // ... other versions
}
```

## Adding New Dependencies

1. **Update Versions**
```kotlin
// In Dependencies.kt
object Versions {
    const val newDependency = "1.0.0"
}
```

2. **Add Dependency Definition**
```kotlin
object Dependencies {
    object YourCategory {
        const val newDependency = "com.example:library:${Versions.newDependency}"
    }
}
```

3. **Update Plugin**
```kotlin
// In KmpServicePlugin.kt
class KmpServicePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.dependencies {
            "implementation"(Dependencies.YourCategory.newDependency)
        }
    }
}
```

## Best Practices

1. **Version Management**
   - Keep versions centralized
   - Document version changes
   - Test compatibility

2. **Module Usage**
   - Import only needed modules
   - Follow module guidelines
   - Check module documentation

3. **Dependency Updates**
   - Regular version checks
   - Security vulnerability scans
   - Compatibility testing

## Troubleshooting

1. **Dependency Conflicts**
   - Check dependency tree
   - Use version catalogs
   - Resolve conflicts explicitly

2. **Missing Dependencies**
   - Verify plugin application
   - Check module imports
   - Validate repository access

3. **Version Issues**
   - Check centralized versions
   - Verify compatibility
   - Update gradually
