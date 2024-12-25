package com.example.kmp.service.registry.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@Serializable
data class Port(
    @SerialName("\$")
    @field:Min(value = 0, message = "Port must be at least 0")
    @field:Max(value = 65535, message = "Port must be at most 65535")
    val port: Int,

    @SerialName("@enabled")
    val enabled: Boolean = true
) {
    constructor(port: Int) : this(port, true)

    init {
        require(port in 0..65535) { "Port must be between 0 and 65535" }
        require(!enabled || port > 0) { "Enabled port must be greater than 0" }
    }
}
