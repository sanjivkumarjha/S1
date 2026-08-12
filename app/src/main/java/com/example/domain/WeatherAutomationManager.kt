package com.example.domain

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WeatherAutomationManager(private val context: Context) {

    data class WeatherData(
        val temperatureCelsius: Float = 32.0f,
        val feelsLikeCelsius: Float = 35.0f,
        val humidityPercent: Int = 68,
        val condition: String = "Sunny",
        val isRainy: Boolean = false,
        val sunriseTime: String = "05:45 AM",
        val sunsetTime: String = "06:50 PM"
    )

    private val _weatherState = MutableStateFlow(WeatherData())
    val weatherState: StateFlow<WeatherData> = _weatherState.asStateFlow()

    fun getCurrentWeather(): WeatherData {
        return _weatherState.value
    }

    fun updateWeather(
        temp: Float? = null,
        feelsLike: Float? = null,
        humidity: Int? = null,
        condition: String? = null,
        isRainy: Boolean? = null
    ) {
        val current = _weatherState.value
        _weatherState.value = current.copy(
            temperatureCelsius = temp ?: current.temperatureCelsius,
            feelsLikeCelsius = feelsLike ?: current.feelsLikeCelsius,
            humidityPercent = humidity ?: current.humidityPercent,
            condition = condition ?: current.condition,
            isRainy = isRainy ?: current.isRainy
        )
    }

    fun calculateSmartFanSpeed(tempCelsius: Float): Int {
        return when {
            tempCelsius >= 31.0f -> 3
            tempCelsius >= 28.0f -> 2
            tempCelsius >= 25.0f -> 1
            else -> 0
        }
    }

    fun evaluateWeatherTriggers(weather: WeatherData): List<String> {
        val triggeredActions = mutableListOf<String>()
        if (weather.temperatureCelsius >= 30.0f) {
            triggeredActions.add("TEMPERATURE_HOT: Activate AC at 24°C and set Fan to Speed ${calculateSmartFanSpeed(weather.temperatureCelsius)}")
        }
        if (weather.isRainy) {
            triggeredActions.add("WEATHER_RAIN: Close smart windows/curtains & turn off lawn sprinklers")
        }
        if (weather.humidityPercent >= 75) {
            triggeredActions.add("HUMIDITY_HIGH: Activate Air Purifier / Dehumidifier mode")
        }
        return triggeredActions
    }
}

