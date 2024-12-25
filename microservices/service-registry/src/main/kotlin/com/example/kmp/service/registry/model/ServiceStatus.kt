package com.example.kmp.service.registry.model

import kotlinx.serialization.Serializable
import com.netflix.appinfo.InstanceInfo

@Serializable
enum class ServiceStatus {
    UP, DOWN, STARTING, OUT_OF_SERVICE, UNKNOWN;

    companion object {
        fun from(eurekaStatus: InstanceInfo.InstanceStatus): ServiceStatus {
            return when (eurekaStatus) {
                InstanceInfo.InstanceStatus.UP -> UP
                InstanceInfo.InstanceStatus.DOWN -> DOWN
                InstanceInfo.InstanceStatus.STARTING -> STARTING
                InstanceInfo.InstanceStatus.OUT_OF_SERVICE -> OUT_OF_SERVICE
                else -> UNKNOWN
            }
        }
    }
}
