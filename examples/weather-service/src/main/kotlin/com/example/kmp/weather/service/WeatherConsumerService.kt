package com.example.kmp.weather.service

import com.example.kmp.messaging.Event
import com.example.kmp.messaging.EventConsumer
import com.example.kmp.messaging.EventProcessor
import com.example.kmp.monitoring.MetricsRegistry
import com.example.kmp.weather.model.WeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

class WeatherConsumerService(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
    private val consumer: EventConsumer = EventConsumer.create()
) {
    private val metricsRegistry = MetricsRegistry.getInstance()
    private val json = Json { ignoreUnknownKeys = true }
    
    private val weatherEventsProcessed = metricsRegistry.counter(
        name = "weather_events_processed_total",
        description = "Total number of weather events processed"
    )
    
    private val weatherEventProcessingTimer = metricsRegistry.timer(
        name = "weather_event_processing_duration",
        description = "Weather event processing duration"
    )

    private val topic = "weather.update"

    suspend fun start() {
        consumer.consume(topic, object : EventProcessor {
            override suspend fun process(event: Event): Boolean {
                return try {
                    processEvent(event)
                    true
                } catch (e: Exception) {
                    handleError(event, e)
                    false
                }
            }

            override suspend fun handleError(event: Event, error: Throwable) {
                println("Error processing event: ${error.message}")
                error.printStackTrace()
            }

            override suspend fun onStart() {
                println("Starting weather event consumer for topic: $topic")
            }

            override suspend fun onStop() {
                println("Stopping weather event consumer for topic: $topic")
            }
        })
    }

    private suspend fun processEvent(event: Event) {
        weatherEventsProcessed.increment()
        
        weatherEventProcessingTimer.record(Runnable {
            // Parse the weather event
            val weatherData = json.decodeFromString<WeatherData>(event.payload)
            
            // Process the weather event
            println("""
                Received weather update:
                City: ${weatherData.city}
                Temperature: ${weatherData.temperature}°C
                Conditions: ${weatherData.conditions}
                Humidity: ${weatherData.humidity}%
                Wind Speed: ${weatherData.windSpeed} km/h
                Timestamp: ${weatherData.timestamp}
            """.trimIndent())
        })
    }

    suspend fun stop() {
        consumer.stop(topic)
    }
}
