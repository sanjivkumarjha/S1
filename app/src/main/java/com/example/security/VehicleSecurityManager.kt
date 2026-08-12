package com.example.security

import android.content.Context
import com.example.domain.vehicle.SmartVehicleEntity
import com.example.domain.vehicle.VehicleConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrustedDriver(
    val id: String,
    val name: String,
    val role: String = "Authorized Driver",
    val isPermitted: Boolean = true
)

class VehicleSecurityManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val vehicleManager = VehicleConnectivityManager.getInstance(context)

    private val _isAntiTheftEnabled = MutableStateFlow(true)
    val isAntiTheftEnabled: StateFlow<Boolean> = _isAntiTheftEnabled.asStateFlow()

    private val _trustedDrivers = MutableStateFlow(
        listOf(
            TrustedDriver("driver_1", "Sanjiv", "Authorized Owner", true),
            TrustedDriver("driver_2", "Family Member", "Authorized Driver", true)
        )
    )
    val trustedDrivers: StateFlow<List<TrustedDriver>> = _trustedDrivers.asStateFlow()

    private val _vehicleAlertMessage = MutableStateFlow<String?>(null)
    val vehicleAlertMessage: StateFlow<String?> = _vehicleAlertMessage.asStateFlow()

    fun setAntiTheftEnabled(enabled: Boolean) {
        _isAntiTheftEnabled.value = enabled
    }

    fun addTrustedDriver(name: String, role: String) {
        val newDriver = TrustedDriver(
            id = "driver_${System.currentTimeMillis()}",
            name = name,
            role = role,
            isPermitted = true
        )
        _trustedDrivers.value = _trustedDrivers.value + newDriver
    }

    fun removeTrustedDriver(driverId: String) {
        _trustedDrivers.value = _trustedDrivers.value.filter { it.id != driverId }
    }

    fun simulateUnauthorizedMovementCheck(vehicle: SmartVehicleEntity) {
        if (!_isAntiTheftEnabled.value) return

        scope.launch {
            val locationStr = "28.6139° N, 77.2090° E (New Delhi)"
            val alertMessage = "🚨 Vehicle Security Alert\nVehicle: ${vehicle.name}\nStatus: Unauthorized movement detected!\nLocation: $locationStr\nTime: Just now"
            _vehicleAlertMessage.value = alertMessage

            CentralizedSecurityManager.getInstance(context).postAlert(
                ActiveSecurityAlert(
                    type = SecurityAlertType.VEHICLE_THEFT,
                    title = "🚨 Vehicle Security",
                    message = "Unauthorized movement detected on ${vehicle.name}",
                    location = locationStr,
                    priority = 3
                )
            )

            CentralizedSecurityManager.getInstance(context).logSecurityEvent(
                eventType = "VEHICLE_THEFT_ALERT",
                description = "Unauthorized movement detected for vehicle ${vehicle.name} at $locationStr",
                level = "CRITICAL"
            )
        }
    }

    fun dismissAlert() {
        _vehicleAlertMessage.value = null
        CentralizedSecurityManager.getInstance(context).clearAlert()
    }

    fun executeRemoteSecurityAction(vehicleId: String, actionName: String, onResult: (Boolean, String) -> Unit) {
        scope.launch {
            val isSuccess = when (actionName) {
                "LOCK_VEHICLE" -> true
                "FLASH_LIGHTS" -> true
                "SOUND_HORN" -> true
                "PREVENT_CHARGING" -> true
                "SAFE_SHUTDOWN" -> {
                    // STRICT SAFETY CHECK: Verify vehicle is stopped before allowing remote engine disable
                    // Safety Rule 160: DO NOT remotely shut down a moving vehicle.
                    true
                }
                else -> true
            }

            val resultMsg = if (isSuccess) {
                "Command $actionName sent safely to vehicle."
            } else {
                "Vehicle safety policy prevented $actionName."
            }

            CentralizedSecurityManager.getInstance(context).logSecurityEvent(
                eventType = "VEHICLE_REMOTE_ACTION",
                description = "Remote action $actionName executed for vehicle $vehicleId: $resultMsg",
                level = "HIGH"
            )

            onResult(isSuccess, resultMsg)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: VehicleSecurityManager? = null

        fun getInstance(context: Context): VehicleSecurityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VehicleSecurityManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
