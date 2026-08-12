package com.example.security

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SecurityEventLogEntity
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

enum class SecurityMode {
    NORMAL, OWNER, RESTRICTED, EMERGENCY
}

class SecurityManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val securityLogDao = db.securityLogDao()
    private val prefsRepo = UserPreferencesRepository(context)

    val securityLogs: Flow<List<SecurityEventLogEntity>> = securityLogDao.getRecentSecurityLogs()

    suspend fun setSecurityMode(mode: SecurityMode, reason: String = "") {
        prefsRepo.updateSecurityMode(mode.name)
        logSecurityEvent(
            eventType = "MODE_CHANGE_${mode.name}",
            description = "Security mode changed to ${mode.name}. $reason".trim(),
            level = if (mode == SecurityMode.EMERGENCY) "CRITICAL" else "MEDIUM"
        )
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

    suspend fun clearSecurityLogs() {
        securityLogDao.clearLogs()
    }

    fun isPrivateDataAccessible(currentMode: String, isOwnerVerified: Boolean): Boolean {
        if (currentMode == SecurityMode.EMERGENCY.name || currentMode == SecurityMode.RESTRICTED.name) {
            return false
        }
        return isOwnerVerified || currentMode == SecurityMode.OWNER.name
    }

    suspend fun triggerIntrusionAlert(reason: String) {
        setSecurityMode(SecurityMode.RESTRICTED, reason)
        logSecurityEvent("INTRUSION_DETECTED", reason, "CRITICAL")
    }

    fun lockDeviceIfPermitted(): Boolean {
        return try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            if (devicePolicyManager != null) {
                devicePolicyManager.lockNow()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
