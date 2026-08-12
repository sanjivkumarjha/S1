package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.network.NetworkOptimizationManager
import com.example.domain.vehicle.VehicleConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            Log.d("BootReceiver", "Device boot completed or package updated. Initializing Snaper Assistant background services...")

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefsRepo = UserPreferencesRepository(context)
                    val settings = prefsRepo.userSettingsFlow.first()

                    if (settings.isAutoStartOnBootEnabled) {
                        Log.d("BootReceiver", "Auto-Start is ENABLED. Restoring lightweight assistant services...")

                        // Launch 24/7 Foreground Service
                        AssistantForegroundService.startService(context, "Snaper 24/7 Background Service Running")

                        // Restore Network Optimization
                        NetworkOptimizationManager.getInstance(context)

                        // Restore Vehicle Connectivity
                        val vehicleManager = VehicleConnectivityManager.getInstance(context)
                        if (settings.isVehicleAutoConnectEnabled && settings.primaryVehicleId.isNotEmpty()) {
                            vehicleManager.connectVehicle(settings.primaryVehicleId)
                        }

                        Log.d("BootReceiver", "Snaper Assistant background initialization complete.")
                    } else {
                        Log.d("BootReceiver", "Auto-Start is DISABLED by user preference.")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error during boot initialization", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
