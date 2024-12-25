package com.example.kmp.service.registry.model

import kotlinx.serialization.Serializable

@Serializable
data class EurekaResponse(
    val applications: Applications
)

@Serializable
data class Applications(
    val application: List<EurekaApplication>
)

@Serializable
data class EurekaApplication(
    val name: String,
    val instance: List<InstanceInfo>
)

@Serializable
data class InstanceInfo(
    val instanceId: String,
    val hostName: String,
    val app: String,
    val ipAddr: String,
    val status: String,
    val port: Port,
    val leaseInfo: LeaseInfo,
    val metadata: Map<String, String>,
    val homePageUrl: String,
    val statusPageUrl: String,
    val healthCheckUrl: String,
    val vipAddress: String,
    val secureVipAddress: String,
    val lastUpdatedTimestamp: Long,
    val lastDirtyTimestamp: Long
)

@Serializable
data class LeaseInfo(
    val registrationTimestamp: Long,
    val lastRenewalTimestamp: Long,
    val serviceUpTimestamp: Long,
    val renewalIntervalInSecs: Int = 30,
    val durationInSecs: Int = 90,
    val evictionTimestamp: Long = 0,
    val registrationTimestampLegacy: Long = registrationTimestamp,
    val lastRenewalTimestampLegacy: Long = lastRenewalTimestamp,
    val serviceUpTimestampLegacy: Long = serviceUpTimestamp
) 