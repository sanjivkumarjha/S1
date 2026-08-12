package com.example.communication

import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import android.util.Log
import com.example.data.preferences.UserPreferencesRepository
import com.example.service.AssistantAccessibilityService
import com.example.ui.glass.DynamicIslandImpressionController
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Intelligent Call Screening & Announcement Agent Controller.
 * Announces caller by name ("Boss / [Owner Name], call from [Caller Name]")
 * and provides smart AI screening for relatives & spam callers, with automated disconnect (TelecomManager.endCall).
 */
class CallScreeningAgentController(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val prefsRepo = UserPreferencesRepository(context)

    fun announceIncomingCall(callerName: String, voiceManager: VoiceAssistantManager) {
        scope.launch {
            val settings = prefsRepo.userSettingsFlow.first()
            val ownerName = settings.ownerName.ifBlank { "Boss" }
            val announcement = "राधे-राधे! $ownerName, call from $callerName. Accept or reject?"

            DynamicIslandImpressionController.setIncomingCall(callerName)
            voiceManager.speak(announcement, "en")
        }
    }

    fun screenAndHandleCall(
        callerName: String,
        isRelative: Boolean,
        isSpam: Boolean,
        voiceManager: VoiceAssistantManager,
        onCallFinished: (() -> Unit)? = null
    ) {
        scope.launch {
            val settings = prefsRepo.userSettingsFlow.first()
            val ownerName = settings.ownerName.ifBlank { "Boss" }

            DynamicIslandImpressionController.setIncomingCall(callerName)

            if (isSpam) {
                Log.i("CallScreeningAgent", "Blocking spam caller $callerName automatically.")
                voiceManager.speak("राधे-राधे! Blocking spam call from $callerName.", "en")
                delay(1500)
                endCallNow()
                DynamicIslandImpressionController.setSleeping()
                onCallFinished?.invoke()
                return@launch
            }

            if (isRelative) {
                val relativeMsg = "राधे-राधे! $ownerName is currently busy in a meeting. I am $ownerName's AI Assistant. I will relay your message immediately."
                voiceManager.speak(relativeMsg, "en")
                delay(4000)
                endCallNow()
                DynamicIslandImpressionController.setSleeping()
                onCallFinished?.invoke()
                return@launch
            }

            // Standard business or unknown caller screening
            val defaultMsg = "राधे-राधे! $ownerName is currently unavailable. Your call request is being recorded for $ownerName."
            voiceManager.speak(defaultMsg, "en")
            delay(4000)
            endCallNow()
            DynamicIslandImpressionController.setSleeping()
            onCallFinished?.invoke()
        }
    }

    /**
     * Attempts to end the current call. On Android 9+ the TelecomManager.endCall() path requires
     * the ANSWER_PHONE_CALLS runtime permission and is heavily restricted; on devices where that
     * is unavailable we fall back to the accessibility service's end-call gesture. Never throws.
     */
    @Suppress("DEPRECATION")
    fun endCallNow(): Boolean {
        return try {
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            if (telecomManager != null &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                com.example.util.PermissionHelper.hasAnswerPhoneCalls(context)
            ) {
                telecomManager.endCall()
            } else {
                // Fallback: use the accessibility service to perform a back/end-call gesture on
                // the in-call screen. Best-effort; returns false if accessibility is inactive.
                fallbackEndCallViaAccessibility()
            }
        } catch (e: Exception) {
            Log.e("CallScreeningAgent", "Failed to end call: ${e.message}")
            fallbackEndCallViaAccessibility()
        }
    }

    private fun fallbackEndCallViaAccessibility(): Boolean {
        return try {
            val service = AssistantAccessibilityService.getInstance() ?: return false
            // Pressing BACK on the in-call screen ends the call on most OEM dialers.
            service.findAndClickAnyText(listOf("End", "End call", "Decline", "Hang up", "Cut"))
        } catch (e: Exception) {
            Log.e("CallScreeningAgent", "Accessibility end-call fallback failed: ${e.message}")
            false
        }
    }
}
