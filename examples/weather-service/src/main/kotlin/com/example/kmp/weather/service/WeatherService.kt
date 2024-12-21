package com.example.kmp.weather.service

import com.example.kmp.messaging.Event
import com.example.kmp.messaging.EventProducer
import com.example.kmp.monitoring.MetricsRegistry
import com.example.kmp.weather.model.WeatherData
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WeatherService(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
    private val eventProducer: EventProducer = EventProducer.create()
) {
    private val metricsRegistry = MetricsRegistry.getInstance()
    private val json = Json { prettyPrint = true }
    
    private val cities = listOf("London", "New York", "Tokyo", "Paris", "Sydney")
    private var monitoringJob: Job? = null
    
    suspend fun startMonitoring() {
        monitoringJob?.cancelAndJoin()
        monitoringJob = scope.launch {
            while (isActive) {
                cities.forEach { city ->
                    try {
                        getWeather(city)
                        println("Generated weather data for $city")
                    } catch (e: Exception) {
                        println("Error generating weather for $city: ${e.message}")
                    }
                }
                delay(10000) // 10 second delay
            }
        }
    }
    
    suspend fun stopMonitoring() {
        monitoringJob?.cancelAndJoin()
        monitoringJob = null
    }
    
    private val weatherRequestCounter = metricsRegistry.counter(
        name = "weather_requests_total",
        description = "Total number of weather requests"
    )
    
    private val weatherRequestTimer = metricsRegistry.timer(
        name = "weather_request_duration",
        description = "Weather request duration"
    )

    suspend fun getWeather(city: String): WeatherData = withContext(Dispatchers.IO) {
        weatherRequestCounter.increment()
        
        val weather = WeatherData(
            city = city,
            temperature = 20.0 + (-5..5).random(),
            conditions = listOf("Sunny", "Cloudy", "Rainy").random(),
            humidity = (40..80).random(),
            windSpeed = (0..20).random().toDouble()
        )
        
        weatherRequestTimer.record(Runnable {
            // Publish weather update event
            val eventPayload = json.encodeToString(weather)
            runBlocking {
                eventProducer.publish(
                    "weather.update",
                    Event(
                        type = "weather.update",
                        payload = eventPayload
                    )
                )
            }
        })
        
        weather
    }
}
