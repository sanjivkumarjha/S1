package com.example.appcontrol

import android.content.Context
import android.content.SharedPreferences

data class SocialPermissionState(
    val socialSearchEnabled: Boolean = true,
    val autoMessagingConsent: Boolean = true,
    val screenReadingConsent: Boolean = true,
    val videoAnalysisConsent: Boolean = true,
    val crossPlatformFactCheckEnabled: Boolean = true
)

class SocialAutomationPermissionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("social_automation_prefs", Context.MODE_PRIVATE)

    fun getPermissionState(): SocialPermissionState {
        return SocialPermissionState(
            socialSearchEnabled = prefs.getBoolean("social_search_enabled", true),
            autoMessagingConsent = prefs.getBoolean("auto_messaging_consent", true),
            screenReadingConsent = prefs.getBoolean("screen_reading_consent", true),
            videoAnalysisConsent = prefs.getBoolean("video_analysis_consent", true),
            crossPlatformFactCheckEnabled = prefs.getBoolean("cross_platform_factcheck_enabled", true)
        )
    }

    fun updateSocialSearchPermission(enabled: Boolean) {
        prefs.edit().putBoolean("social_search_enabled", enabled).apply()
    }

    fun updateAutoMessagingConsent(enabled: Boolean) {
        prefs.edit().putBoolean("auto_messaging_consent", enabled).apply()
    }

    fun updateScreenReadingConsent(enabled: Boolean) {
        prefs.edit().putBoolean("screen_reading_consent", enabled).apply()
    }

    fun updateVideoAnalysisConsent(enabled: Boolean) {
        prefs.edit().putBoolean("video_analysis_consent", enabled).apply()
    }

    fun updateCrossPlatformFactCheckEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("cross_platform_factcheck_enabled", enabled).apply()
    }
}
