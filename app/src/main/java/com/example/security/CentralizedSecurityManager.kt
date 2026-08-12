package com.example.security

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SecurityEventLogEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class SecurityAlertType {
    NONE,
    EMERGENCY_SOS,
    VEHICLE_THEFT,
    HOME_INTRUSION,
    UNAUTHORIZED_PHONE_ACCESS,
    FINDING_PHONE,
    THREAT_DETECTED
}

data class ActiveSecurityAlert(
    val type: SecurityAlertType = SecurityAlertType.NONE,
    val title: String = "",
    val message: String = "",
    val location: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val priority: Int = 0 // Priority 1 (highest) to 7
)

class CentralizedSecurityManager private constructor(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val securityLogDao = db.securityLogDao()
    private val prefsRepo = UserPreferencesRepository(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    val findMyAssistantManager = FindMyAssistantManager.getInstance(context)
    val familySosManager = FamilySosManager.getInstance(context)
    val homeSecurityManager = HomeSecurityManager.getInstance(context)
    val vehicleSecurityManager = VehicleSecurityManager.getInstance(context)

    private val _activeAlert = MutableStateFlow<ActiveSecurityAlert?>(null)
    val activeAlert: StateFlow<ActiveSecurityAlert?> = _activeAlert.asStateFlow()

    private val _isProtectedModeActive = MutableStateFlow(false)
    val isProtectedModeActive: StateFlow<Boolean> = _isProtectedModeActive.asStateFlow()

    private val _isLostDeviceModeActive = MutableStateFlow(false)
    val isLostDeviceModeActive: StateFlow<Boolean> = _isLostDeviceModeActive.asStateFlow()

    init {
        scope.launch {
            prefsRepo.userSettingsFlow.collectLatest { settings ->
                if (settings.securityMode == "EMERGENCY" || settings.securityMode == "RESTRICTED") {
                    _isProtectedModeActive.value = true
                }
            }
        }
    }

    suspend fun setProtectedMode(active: Boolean, reason: String = "User manual trigger") {
        _isProtectedModeActive.value = active
        if (active) {
            prefsRepo.updateSecurityMode("RESTRICTED")
            logSecurityEvent("PROTECTED_MODE_ENABLED", "Protected mode activated: $reason", "HIGH")
            postAlert(
                ActiveSecurityAlert(
                    type = SecurityAlertType.UNAUTHORIZED_PHONE_ACCESS,
                    title = "🔒 Protected Mode Active",
                    message = "Private data & vehicle/home controls restricted until authenticated.",
                    priority = 5
                )
            )
        } else {
            prefsRepo.updateSecurityMode("OWNER")
            logSecurityEvent("PROTECTED_MODE_DISABLED", "Protected mode deactivated: Owner verified", "MEDIUM")
            if (_activeAlert.value?.type == SecurityAlertType.UNAUTHORIZED_PHONE_ACCESS) {
                clearAlert()
            }
        }
    }

    suspend fun setLostDeviceMode(active: Boolean) {
        _isLostDeviceModeActive.value = active
        if (active) {
            setProtectedMode(true, "Lost device mode enabled")
            logSecurityEvent("LOST_DEVICE_MODE_ENABLED", "Lost Device Mode activated. Memories and controls protected.", "CRITICAL")
            postAlert(
                ActiveSecurityAlert(
                    type = SecurityAlertType.UNAUTHORIZED_PHONE_ACCESS,
                    title = "🚨 Lost Device Mode Active",
                    message = "Phone restricted. Last known location saved.",
                    priority = 2
                )
            )
        } else {
            setProtectedMode(false, "Lost device mode disabled")
            logSecurityEvent("LOST_DEVICE_MODE_DISABLED", "Lost Device Mode deactivated.", "MEDIUM")
        }
    }

    fun postAlert(alert: ActiveSecurityAlert) {
        val current = _activeAlert.value
        // Only override if new alert priority is higher or equal (lower number = higher priority)
        if (current == null || alert.priority <= current.priority) {
            _activeAlert.value = alert
        }
    }

    fun clearAlert() {
        _activeAlert.value = null
    }

    suspend fun logSecurityEvent(eventType: String, description: String, level: String = "MEDIUM") {
        try {
            securityLogDao.insertLog(
                SecurityEventLogEntity(
                    eventType = eventType,
                    description = description,
                    timestamp = System.currentTimeMillis(),
                    securityLevel = level
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CentralizedSecurityManager? = null

        fun getInstance(context: Context): CentralizedSecurityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CentralizedSecurityManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
