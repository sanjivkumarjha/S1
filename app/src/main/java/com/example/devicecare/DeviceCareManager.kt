package com.example.devicecare

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import com.example.service.AssistantAccessibilityService
import java.util.Locale

data class DeviceHealthStatus(
    val batteryPercentage: Int,
    val isCharging: Boolean,
    val batteryStatus: String,
    val freeStorageGb: Double,
    val totalStorageGb: Double,
    val usedStoragePercentage: Int,
    val memoryUsageMb: Double,
    val totalMemoryMb: Double,
    val networkState: String,
    val isWifiConnected: Boolean,
    val isAccessibilityEnabled: Boolean,
    val overallHealthScore: Int, // 0-100
    val summaryRecommendation: String
)

class DeviceCareManager(private val context: Context) {

    fun getDeviceHealthStatus(): DeviceHealthStatus {
        // 1. Battery Info
        var batteryPct = 0
        var isCharging = false
        var batteryStatusStr = "Not available"

        try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level != -1 && scale > 0) {
                batteryPct = ((level.toFloat() / scale.toFloat()) * 100).toInt()
            }
            val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            batteryStatusStr = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging ⚡"
                BatteryManager.BATTERY_STATUS_FULL -> "Full 🔋"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging 📱"
                else -> if (level != -1) "Healthy" else "Not available"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Storage Info
        var totalStorageGb = 0.0
        var freeStorageGb = 0.0
        var usedStoragePct = 0

        try {
            val statFs = StatFs(Environment.getDataDirectory().path)
            val blockSize = statFs.blockSizeLong
            val totalBlocks = statFs.blockCountLong
            val availableBlocks = statFs.availableBlocksLong

            val totalStorageBytes = totalBlocks * blockSize
            val freeStorageBytes = availableBlocks * blockSize

            totalStorageGb = (totalStorageBytes / (1024.0 * 1024.0 * 1024.0)).coerceAtLeast(0.1)
            freeStorageGb = (freeStorageBytes / (1024.0 * 1024.0 * 1024.0)).coerceAtLeast(0.0)
            usedStoragePct = (((totalStorageGb - freeStorageGb) / totalStorageGb) * 100).toInt().coerceIn(0, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Memory Info
        var totalMemoryMb = 0.0
        var usedMemoryMb = 0.0
        try {
            val runtime = Runtime.getRuntime()
            totalMemoryMb = (runtime.maxMemory() / (1024.0 * 1024.0))
            usedMemoryMb = ((runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 4. Network Info
        var networkStateStr = "Offline / No Internet ⚠️"
        var isWifi = false
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
            val isCellular = caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
            networkStateStr = when {
                isWifi -> "Wi-Fi Connected 🌐"
                isCellular -> "Mobile Data Connected 📶"
                else -> "Offline / No Internet ⚠️"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 5. Accessibility Status
        val isAccessibilityEnabled = AssistantAccessibilityService.isAccessibilityServiceEnabled(context)

        // Health score calculation
        var score = 100
        if (batteryPct in 1..19 && !isCharging) score -= 20
        if (usedStoragePct > 90) score -= 25
        if (!isAccessibilityEnabled) score -= 15

        val recommendation = when {
            batteryPct in 1..19 && !isCharging -> "Connect your charger soon. Battery is low."
            usedStoragePct > 85 -> "Storage is getting full. Consider cleaning unused files."
            !isAccessibilityEnabled -> "Enable Snaper Accessibility Service to unlock full screen control features."
            else -> "Your phone is in excellent condition! All systems running smoothly. ✨"
        }

        return DeviceHealthStatus(
            batteryPercentage = batteryPct,
            isCharging = isCharging,
            batteryStatus = batteryStatusStr,
            freeStorageGb = String.format(Locale.US, "%.1f", freeStorageGb).toDoubleOrNull() ?: 0.0,
            totalStorageGb = String.format(Locale.US, "%.1f", totalStorageGb).toDoubleOrNull() ?: 0.0,
            usedStoragePercentage = usedStoragePct,
            memoryUsageMb = String.format(Locale.US, "%.1f", usedMemoryMb).toDoubleOrNull() ?: 0.0,
            totalMemoryMb = String.format(Locale.US, "%.1f", totalMemoryMb).toDoubleOrNull() ?: 0.0,
            networkState = networkStateStr,
            isWifiConnected = isWifi,
            isAccessibilityEnabled = isAccessibilityEnabled,
            overallHealthScore = score,
            summaryRecommendation = recommendation
        )
    }
}
