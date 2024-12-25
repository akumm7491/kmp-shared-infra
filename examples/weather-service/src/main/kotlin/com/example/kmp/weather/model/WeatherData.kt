package com.example.kmp.weather.model

import kotlinx.serialization.Serializable
import com.example.kmp.messaging.schema.RegisterSchema

@Serializable
@RegisterSchema(
    topic = "weather.update",
    name = "WeatherData",
    namespace = "com.example.kmp.weather.model"
)
data class WeatherData(
    val city: String,
    val temperature: Double,
    val conditions: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long = System.currentTimeMillis()
)
