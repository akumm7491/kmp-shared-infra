package com.example.kmp.networking.models

/**
 * Configuration model for Eureka service registry server
 *
 * @property port Port for the registry server (default 8761)
 * @property enableSelfPreservation Enable self-preservation mode (default false for development)
 * @property renewalPercentThreshold Renewal percent threshold (default 0.85)
 * @property peerEurekaNodes List of peer Eureka nodes for clustering
 */
data class ServiceRegistryConfig(
    val port: Int = 8761,
    val enableSelfPreservation: Boolean = false,
    val renewalPercentThreshold: Double = 0.85,
    val peerEurekaNodes: List<String> = emptyList()
)
