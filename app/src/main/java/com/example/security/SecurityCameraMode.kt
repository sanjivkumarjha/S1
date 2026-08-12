package com.example.security

import android.content.Context
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first

class SecurityCameraMode(private val context: Context) {

    private val securityManager = SecurityManager(context)
    private val prefsRepo = UserPreferencesRepository(context)

    suspend fun handleSuspiciousActivity(reason: String, intruderType: String = "Guest") {
        // 1. Enter Restricted Security Mode
        securityManager.setSecurityMode(SecurityMode.RESTRICTED, reason)

        // 2. Log Critical Security Event
        securityManager.logSecurityEvent(
            eventType = "UNAUTHORIZED_ACCESS_ATTEMPT",
            description = "Suspicious activity detected: $reason ($intruderType)",
            level = "CRITICAL"
        )
    }

    suspend fun getSecurityWarningText(targetAppOrFeature: String, ownerTitle: String = "Boss"): String {
        val title = if (ownerTitle.isNotBlank()) ownerTitle else "Boss"
        return when (targetAppOrFeature.lowercase()) {
            "gallery", "photos", "camera" ->
                "कृपया रुकिए। यह फोन $title का है और $targetAppOrFeature application restricted है।"
            else ->
                "कृपया रुकिए। यह फोन $title का है और आपको $targetAppOrFeature की permission नहीं दी गई है।"
        }
    }
}
