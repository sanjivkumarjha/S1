package com.example.voice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.MorningGreetingScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == "android.intent.action.QUICKBOOT_POWERON") {
            // Schedule daily morning greeting
            MorningGreetingScheduler.scheduleDailyMorningGreeting(context)

            // Check if user has background listening service enabled
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val prefsRepo = UserPreferencesRepository(context)
                    val settings = prefsRepo.userSettingsFlow.first()
                    if (settings.isBgListeningServiceActive) {
                        // Starting a microphone foreground service directly from a background
                        // broadcast is not allowed on Android 12+ and requires POST_NOTIFICATIONS
                        // on Android 13+. Best-effort: only attempt from a foreground-capable
                        // context and swallow the restriction gracefully.
                        try {
                            VoiceAssistantService.start(context)
                        } catch (e: Exception) {
                            android.util.Log.w("voice.BootReceiver", "VoiceAssistantService start skipped: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
