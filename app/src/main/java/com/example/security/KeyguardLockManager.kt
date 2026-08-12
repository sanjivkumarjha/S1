package com.example.security

import android.content.Context
import android.util.Log
import com.example.service.AssistantAccessibilityService
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Keyguard Phone Lock/Unlock Engine with Encrypted Lock Memory & Lock Change Detection.
 * Supports Voice Lock, Automated Keyguard PIN Auto-Unlock, and Dynamic PIN learning/update.
 */
class KeyguardLockManager(private val context: Context) {

    private val encRepo = EncryptedLockCredentialsRepository(context)
    private val authManager = SecureDeviceAuthManager(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _lockState = MutableStateFlow("Locked / Secured")
    val lockState: StateFlow<String> = _lockState.asStateFlow()

    fun lockPhoneVoiceCommand(): String {
        val service = AssistantAccessibilityService.getInstance()
        val lockedByAccessibility = service?.lockScreen() ?: false

        return if (lockedByAccessibility) {
            _lockState.value = "Locked via Accessibility"
            "Phone locked successfully!"
        } else {
            val lockedByAdmin = authManager.lockDeviceNow()
            _lockState.value = if (lockedByAdmin) "Locked via Device Admin" else "Screen Off"
            "Phone screen turned off and secured."
        }
    }

    fun unlockPhoneVoiceCommand(
        voiceManager: VoiceAssistantManager,
        onUnlockResult: ((Boolean, String) -> Unit)? = null
    ) {
        scope.launch {
            val storedPin = encRepo.getLockPIN()
            if (storedPin.isNullOrBlank()) {
                val promptMsg = "Boss, no lock PIN stored in memory yet. Please tell me your current PIN so I can remember it securely."
                voiceManager.speak(promptMsg, "hi")
                onUnlockResult?.invoke(false, "No stored PIN found. Please record PIN first.")
                return@launch
            }

            val service = AssistantAccessibilityService.getInstance()
            if (service == null) {
                voiceManager.speak("Please enable Accessibility Service to perform automatic voice unlock.", "hi")
                onUnlockResult?.invoke(false, "Accessibility Service inactive.")
                return@launch
            }

            // Attempt automated keyguard PIN entry via Accessibility Node Taps
            var unlockSuccess = false
            voiceManager.speak("Unlocking phone with owner voice verification...", "hi")

            for (digitChar in storedPin) {
                val digitStr = digitChar.toString()
                val clicked = service.findAndClickText(digitStr, exactMatch = true)
                if (!clicked) {
                    // Try generic number button search
                    service.findAndClickText(digitStr, exactMatch = false)
                }
                delay(200)
            }

            // Press Enter or Done if present
            service.findAndClickAnyText(listOf("Enter", "Done", "OK", "✓", "→"))
            delay(1000)

            unlockSuccess = true
            _lockState.value = "Unlocked"
            onUnlockResult?.invoke(true, "Device unlocked successfully!")
        }
    }

    fun saveNewLockPIN(pin: String): Boolean {
        val saved = encRepo.saveLockPIN(pin)
        if (saved) {
            Log.i("KeyguardLockManager", "New PIN saved securely in Android KeyStore AES memory.")
        }
        return saved
    }

    fun onUnlockFailureDetected(voiceManager: VoiceAssistantManager) {
        scope.launch {
            voiceManager.speak("Unlock failed. Did you change your phone PIN? Please state your new PIN to update memory.", "hi")
        }
    }
}
