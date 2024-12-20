package com.example.kmp.service.registry.config

data class EurekaServerConfig(
    val port: Int,
    val peerEurekaNodes: List<String>,
    val renewalPercentThreshold: Double,
    val renewalIntervalInSecs: Int,
    val enableSelfPreservation: Boolean
)
