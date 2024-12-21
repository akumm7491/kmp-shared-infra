package com.example.kmp.weather.model

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val city: String,
    val temperature: Double,
    val conditions: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long = System.currentTimeMillis()
)
