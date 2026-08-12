package com.example.domain

import android.content.Context
import com.example.data.local.entities.SmartDeviceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Discovers Smart IoT devices on authorized Wi-Fi, Bluetooth, Matter, and Home Assistant endpoints using StateFlow.
 */
class SmartDeviceDiscoveryManager(private val context: Context) {

    sealed class DiscoveryStatus {
        object Idle : DiscoveryStatus()
        object Scanning : DiscoveryStatus()
        data class Success(val devices: List<SmartDeviceEntity>) : DiscoveryStatus()
        data class Error(val message: String) : DiscoveryStatus()
    }

    private val _discoveryStatus = MutableStateFlow<DiscoveryStatus>(DiscoveryStatus.Idle)
    val discoveryStatus: StateFlow<DiscoveryStatus> = _discoveryStatus.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<SmartDeviceEntity>>(emptyList())
    val discoveredDevices: StateFlow<List<SmartDeviceEntity>> = _discoveredDevices.asStateFlow()

    private val sampleDiscoveredList = listOf(
        SmartDeviceEntity(
            deviceName = "Smart OLED TV",
            manufacturer = "LG",
            model = "WebOS 2026",
            protocol = "wifi",
            ipAddress = "192.168.1.112",
            capabilities = "power,volume,mute,input,app_launch",
            room = "Living Room",
            deviceType = "TV",
            isOnline = true
        ),
        SmartDeviceEntity(
            deviceName = "Inverter AC 1.5T",
            manufacturer = "Daikin",
            model = "FTKF Series",
            protocol = "matter",
            ipAddress = "192.168.1.145",
            capabilities = "power,temperature,fan_speed,mode,swing",
            room = "Bedroom",
            deviceType = "AC",
            isOnline = true
        ),
        SmartDeviceEntity(
            deviceName = "Smart RGB Bulb",
            manufacturer = "Philips Hue",
            model = "Zigbee/Matter",
            protocol = "matter",
            ipAddress = "192.168.1.189",
            capabilities = "power,brightness,color,temperature",
            room = "Office",
            deviceType = "Light",
            isOnline = true
        ),
        SmartDeviceEntity(
            deviceName = "Smart Air Purifier Pro",
            manufacturer = "Xiaomi",
            model = "Mi Air 4 Pro",
            protocol = "home_assistant",
            ipAddress = "192.168.1.201",
            capabilities = "power,fan_speed,mode,pm25_sensor",
            room = "Living Room",
            deviceType = "Plug",
            isOnline = true
        )
    )

    fun startDiscovery(scope: CoroutineScope) {
        if (_isDiscovering.value) return
        _isDiscovering.value = true
        _discoveryStatus.value = DiscoveryStatus.Scanning

        scope.launch(Dispatchers.IO) {
            try {
                // Simulate local network discovery latency (mDNS/UPnP/BLE scan)
                delay(1200)
                _discoveredDevices.value = sampleDiscoveredList
                _discoveryStatus.value = DiscoveryStatus.Success(sampleDiscoveredList)
            } catch (e: Exception) {
                _discoveryStatus.value = DiscoveryStatus.Error("Discovery failed: ${e.localizedMessage}")
            } finally {
                _isDiscovering.value = false
            }
        }
    }

    fun discoverLocalNetworkDevices(): List<SmartDeviceEntity> {
        val current = _discoveredDevices.value
        return if (current.isNotEmpty()) current else sampleDiscoveredList
    }
}

