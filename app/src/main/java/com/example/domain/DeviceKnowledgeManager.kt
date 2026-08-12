package com.example.domain

import android.content.Context

class DeviceKnowledgeManager(private val context: Context) {

    fun getDeviceSetupGuide(deviceName: String, brand: String): String {
        return """
            Setup Pairing Guide for $brand $deviceName:
            1. Ensure device is powered ON and in pairing mode (hold power button for 5s until LED blinks).
            2. Connect phone to 2.4GHz home Wi-Fi network.
            3. Tap 'Discover Devices' in Snaper Smart Home Dashboard.
            4. Verify local IP assignment and assign device to a room.
        """.trimIndent()
    }
}
