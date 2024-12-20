package com.example.kmp.service.registry.config

import com.example.kmp.service.registry.model.ServiceInstance
import com.example.kmp.service.registry.model.ServiceStatus

class InvalidServiceNameException(message: String) : Exception(message)
class UnknownProjectException(message: String) : Exception(message)

data class ProjectConfig(
    val id: String,
    val name: String,
    val allowedDomains: List<String>,
    val servicePrefix: String,
    val requirements: ServiceRequirements
)

data class ServiceRequirements(
    val requiredMetadata: Set<String> = setOf("version", "project"),
    val healthCheckRequired: Boolean = true,
    val securePortRequired: Boolean = false,
    val allowedStatuses: Set<ServiceStatus> = setOf(ServiceStatus.UP, ServiceStatus.STARTING)
)

class ProjectRegistry {
    private val projects = mutableMapOf<String, ProjectConfig>()

    fun registerProject(config: ProjectConfig) {
        projects[config.id] = config
    }

    fun getProject(id: String): ProjectConfig? = projects[id]

    fun validateService(instance: ServiceInstance) {
        val projectId = instance.app.split("-").firstOrNull()
            ?: throw InvalidServiceNameException("Invalid service name format")

        val projectConfig = getProject(projectId)
            ?: throw UnknownProjectException("Project $projectId not registered")

        with(projectConfig.requirements) {
            // Validate required metadata
            requiredMetadata.forEach { key ->
                require(instance.metadata.containsKey(key)) {
                    "Required metadata '$key' missing"
                }
            }

            // Validate health check
            if (healthCheckRequired) {
                require(instance.healthCheckUrl != null) {
                    "Health check URL required"
                }
            }

            // Validate status
            require(instance.status in allowedStatuses) {
                "Service status ${instance.status} not allowed"
            }
        }
    }
}
