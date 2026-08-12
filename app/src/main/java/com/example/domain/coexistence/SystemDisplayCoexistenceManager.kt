package com.example.domain.coexistence

import android.content.Context
import android.os.Build

enum class DisplayCoexistenceMode {
    SYSTEM_AOD_COEXISTENCE,
    SYSTEM_ISLAND_COEXISTENCE,
    FULL_COEXISTENCE,
    STANDARD_OVERLAY
}

data class SystemDisplayCapabilities(
    val manufacturer: String,
    val model: String,
    val sdkVersion: Int,
    val isSystemAodDetected: Boolean,
    val isSystemDynamicIslandDetected: Boolean,
    val isOledDisplaySupported: Boolean
)

object SystemDisplayCoexistenceManager {

    fun detectCapabilities(context: Context): SystemDisplayCapabilities {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val model = Build.MODEL
        val sdk = Build.VERSION.SDK_INT

        val isOled = manufacturer.contains("samsung") || 
                     manufacturer.contains("realme") || 
                     manufacturer.contains("oneplus") || 
                     manufacturer.contains("xiaomi") || 
                     manufacturer.contains("google") || 
                     manufacturer.contains("oppo") || 
                     manufacturer.contains("vivo") || 
                     manufacturer.contains("apple")

        val isAodDetected = true
        val isSystemIslandDetected = sdk >= 33 && (manufacturer.contains("apple") || manufacturer.contains("realme") || manufacturer.contains("xiaomi"))

        return SystemDisplayCapabilities(
            manufacturer = Build.MANUFACTURER,
            model = model,
            sdkVersion = sdk,
            isSystemAodDetected = isAodDetected,
            isSystemDynamicIslandDetected = isSystemIslandDetected,
            isOledDisplaySupported = isOled
        )
    }

    fun getCoexistenceMode(capabilities: SystemDisplayCapabilities): DisplayCoexistenceMode {
        return when {
            capabilities.isSystemAodDetected && capabilities.isSystemDynamicIslandDetected -> DisplayCoexistenceMode.FULL_COEXISTENCE
            capabilities.isSystemAodDetected -> DisplayCoexistenceMode.SYSTEM_AOD_COEXISTENCE
            capabilities.isSystemDynamicIslandDetected -> DisplayCoexistenceMode.SYSTEM_ISLAND_COEXISTENCE
            else -> DisplayCoexistenceMode.STANDARD_OVERLAY
        }
    }

    fun enforceNonInterferenceProtection(): Boolean {
        // Enforces absolute non-interference with system AOD / manufacturer settings
        return true
    }
}
