package com.example.kmp.service.registry.model

import jakarta.validation.constraints.*
import kotlinx.serialization.Serializable

/**
 * Service instance model.
 * Represents a registered service instance with its metadata.
 */
@Serializable
data class ServiceInstance(
    @field:NotBlank(message = "Service ID is required")
    val id: String,

    @field:NotBlank(message = "Service name is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Service name must be alphanumeric with hyphens")
    @field:Size(max = 64, message = "Service name must not exceed 64 characters")
    val serviceName: String,

    @field:NotBlank(message = "Host is required")
    @field:Pattern(regexp = "^[a-zA-Z0-9-.]+$", message = "Host must be a valid hostname")
    val host: String,

    @field:Min(value = 1024, message = "Port must be greater than or equal to 1024")
    @field:Max(value = 65535, message = "Port must be less than or equal to 65535")
    val port: Int,

    @field:NotBlank(message = "Status is required")
    @field:Pattern(regexp = "^(UP|DOWN|STARTING|OUT_OF_SERVICE|UNKNOWN)$", message = "Invalid status")
    val status: String,

    @field:Size(max = 10, message = "Metadata must not exceed 10 entries")
    val metadata: Map<String, String> = emptyMap(),

    @field:Min(value = 0, message = "Last updated timestamp must be positive")
    val lastUpdated: Long = System.currentTimeMillis()
)
