package com.example.ui.preview

import android.os.Build

enum class PreviewDeviceType(
    val id: String,
    val displayName: String,
    val osType: String,
    val isFlagship: Boolean,
    val screenAspectRatio: Float // height / width
) {
    SAMSUNG_S26_ULTRA("SAMSUNG_S26_ULTRA", "Samsung Galaxy S26 Ultra", "ANDROID", true, 20f / 9f),
    IPHONE_17_PRO_MAX("IPHONE_17_PRO_MAX", "iPhone 17 Pro Max", "IOS", true, 19.5f / 9f),
    PIXEL_9_PRO("PIXEL_9_PRO", "Google Pixel 9 Pro", "ANDROID", true, 20f / 9f)
}

object DeviceDetectionManager {

    /**
     * Detects the operating system using standard platform APIs.
     * In Android runtime environment, returns "ANDROID".
     */
    fun detectOperatingSystem(): String {
        return try {
            if (Build.VERSION.SDK_INT > 0) {
                "ANDROID"
            } else {
                "IOS"
            }
        } catch (e: Exception) {
            "ANDROID"
        }
    }

    /**
     * Determines the active preview device based on user preference and detected OS.
     */
    fun getEffectivePreviewDevice(selectedSetting: String): PreviewDeviceType {
        if (selectedSetting != "AUTO" && selectedSetting.isNotBlank()) {
            return when (selectedSetting) {
                "SAMSUNG_S26_ULTRA" -> PreviewDeviceType.SAMSUNG_S26_ULTRA
                "IPHONE_17_PRO_MAX" -> PreviewDeviceType.IPHONE_17_PRO_MAX
                "PIXEL_9_PRO" -> PreviewDeviceType.PIXEL_9_PRO
                else -> PreviewDeviceType.SAMSUNG_S26_ULTRA
            }
        }

        val os = detectOperatingSystem()
        return if (os == "IOS") {
            PreviewDeviceType.IPHONE_17_PRO_MAX
        } else {
            PreviewDeviceType.SAMSUNG_S26_ULTRA
        }
    }
}
