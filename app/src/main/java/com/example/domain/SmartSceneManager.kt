package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SmartDeviceEntity

class SmartSceneManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val smartDeviceDao = db.smartDeviceDao()

    data class Scene(
        val name: String,
        val iconName: String,
        val description: String
    )

    val scenes = listOf(
        Scene("Good Morning", "WbSunny", "Open curtains, lights 50%, AC 25°C"),
        Scene("Good Night", "NightsStay", "All lights off, TV off, AC 24°C, door locked"),
        Scene("Movie Mode", "Movie", "TV on, ambient lights dimmed, curtains closed"),
        Scene("Work Mode", "Work", "Office light 100%, AC 24°C, fan speed 2"),
        Scene("Leaving Home", "ExitToApp", "All appliances & AC off, security active"),
        Scene("Arriving Home", "Home", "Entrance light on, AC turned on 24°C"),
        Scene("Sleep Mode", "Bedtime", "Silence non-critical alerts, fan speed 1, AC 25°C"),
        Scene("Summer Mode", "Thermostat", "AC high cooling, fans active"),
        Scene("Winter Mode", "AcUnit", "Heater active, AC off")
    )

    suspend fun executeScene(sceneName: String, deviceList: List<SmartDeviceEntity>): String {
        when (sceneName.lowercase()) {
            "good night", "night mode", "sleep mode" -> {
                deviceList.forEach { dev ->
                    if (dev.deviceType == "Light" || dev.deviceType == "TV") {
                        smartDeviceDao.updateSmartDevice(dev.copy(powerState = false, currentValue = "Off"))
                    } else if (dev.deviceType == "AC") {
                        smartDeviceDao.updateSmartDevice(dev.copy(powerState = true, currentValue = "25°C"))
                    }
                }
                return "Good Night mode executed: Lights and TV turned off, AC set to 25°C."
            }
            "good morning", "morning mode" -> {
                deviceList.forEach { dev ->
                    if (dev.deviceType == "Light") {
                        smartDeviceDao.updateSmartDevice(dev.copy(powerState = true, currentValue = "Brightness 60%"))
                    }
                }
                return "Good Morning scene executed: Morning lighting activated."
            }
            "movie mode" -> {
                deviceList.forEach { dev ->
                    if (dev.deviceType == "TV") {
                        smartDeviceDao.updateSmartDevice(dev.copy(powerState = true, currentValue = "HDMI 1"))
                    } else if (dev.deviceType == "Light") {
                        smartDeviceDao.updateSmartDevice(dev.copy(powerState = true, currentValue = "Brightness 20%"))
                    }
                }
                return "Movie Mode active: TV turned on and lighting dimmed."
            }
            "leaving home" -> {
                deviceList.forEach { dev ->
                    smartDeviceDao.updateSmartDevice(dev.copy(powerState = false, currentValue = "Off"))
                }
                return "Leaving Home scene executed: All non-essential devices turned off."
            }
            "arriving home" -> {
                deviceList.forEach { dev ->
                    if (dev.deviceType == "Light" || dev.deviceType == "AC") {
                        smartDeviceDao.updateSmartDevice(dev.copy(powerState = true, currentValue = "24°C"))
                    }
                }
                return "Arriving Home scene executed: Entrance lights & AC powered on."
            }
            else -> {
                return "I don't have a '$sceneName' scene configured. Available scenes are: good morning, good night, leaving home, arriving home, movie mode. Please use one of these or add a custom scene in Smart Home settings."
            }
        }
    }
}
