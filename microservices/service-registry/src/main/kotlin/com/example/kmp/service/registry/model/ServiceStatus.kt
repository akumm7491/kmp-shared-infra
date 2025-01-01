package com.example.kmp.service.registry.model

import kotlinx.serialization.Serializable

@Serializable
enum class ServiceStatus {
    UP, 
    DOWN, 
    STARTING, 
    OUT_OF_SERVICE, 
    UNKNOWN;

    companion object {
        fun fromString(status: String): ServiceStatus {
            return try {
                valueOf(status.uppercase())
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
        }
    }
}
