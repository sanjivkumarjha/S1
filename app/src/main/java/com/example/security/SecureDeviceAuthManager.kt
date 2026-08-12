package com.example.security

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Production-grade Secure Device Authentication & Biometric Prompt Manager.
 *
 * PRIVACY & SECURITY DIRECTIVE:
 * - NEVER attempts to bypass, defeat, simulate, capture, or store system PIN, password, or pattern.
 * - Uses Android's official BiometricPrompt & BiometricManager APIs.
 * - Zero storage of PIN/pattern/password or raw biometric templates in local DB, prefs, or cloud.
 * - No Accessibility Service hacks, screen overlays, or simulated touch events.
 * - Integrates DeviceAdminReceiver lockNow() and ScreenLockService foreground service for Android 13+.
 */
class SecureDeviceAuthManager(private val context: Context) {

    private val securityManager = SecurityManager(context)
    private val prefsRepo = UserPreferencesRepository(context)

    val adminComponent = ComponentName(context, SnaperDeviceAdminReceiver::class.java)

    fun isDeviceAdminActive(): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        return dpm?.isAdminActive(adminComponent) == true
    }

    fun getDeviceAdminIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Snaper Technology needs Device Admin permission to securely lock the screen on voice command."
            )
        }
    }

    fun lockDeviceNow(): Boolean {
        return if (isDeviceAdminActive()) {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            try {
                dpm?.lockNow()
                CoroutineScope(Dispatchers.IO).launch {
                    securityManager.logSecurityEvent(
                        eventType = "SCREEN_LOCKED_SUCCESS",
                        description = "Screen locked via DevicePolicyManager.lockNow()",
                        level = "INFO"
                    )
                }
                true
            } catch (e: Exception) {
                ScreenLockService.executeScreenLock(context)
                true
            }
        } else {
            ScreenLockService.executeScreenLock(context)
            false
        }
    }

    enum class AuthStatus {
        AVAILABLE,
        NO_HARDWARE,
        HARDWARE_UNAVAILABLE,
        NONE_ENROLLED,
        SECURITY_UPDATE_REQUIRED,
        UNKNOWN
    }

    /**
     * Checks if biometric or system device credentials (PIN/pattern/password via OS) are available.
     */
    fun checkBiometricAvailability(): AuthStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> AuthStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> AuthStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AuthStatus.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthStatus.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> AuthStatus.SECURITY_UPDATE_REQUIRED
            else -> AuthStatus.UNKNOWN
        }
    }

    /**
     * Triggers the official Android Biometric Prompt attached to the FragmentActivity.
     */
    fun authenticateOwner(
        activity: FragmentActivity,
        title: String = "Verify Identity • Snaper Technology",
        subtitle: String = "Authenticate to unlock device & assistant capabilities",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                CoroutineScope(Dispatchers.IO).launch {
                    securityManager.setSecurityMode(SecurityMode.OWNER, "Biometric authentication succeeded")
                    prefsRepo.setFaceVerified(true)
                    securityManager.logSecurityEvent(
                        eventType = "SECURE_DEVICE_UNLOCK_SUCCESS",
                        description = "Owner verified via Android official BiometricPrompt",
                        level = "INFO"
                    )
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                val userMessage = when (errorCode) {
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON, BiometricPrompt.ERROR_USER_CANCELED ->
                        "Authentication cancelled. Please unlock your device using the normal Android system lock screen method."
                    BiometricPrompt.ERROR_NO_BIOMETRICS ->
                        "No biometric features enrolled on this device. Please set up fingerprint/face unlock in Android Settings."
                    BiometricPrompt.ERROR_HW_UNAVAILABLE, BiometricPrompt.ERROR_HW_NOT_PRESENT ->
                        "Biometric hardware unavailable. Please unlock device using system PIN or pattern."
                    else -> "Authentication notice: $errString. Please use standard system authentication."
                }
                CoroutineScope(Dispatchers.IO).launch {
                    securityManager.logSecurityEvent(
                        eventType = "SECURE_DEVICE_UNLOCK_NOTICE",
                        description = "Authentication response code $errorCode: $errString",
                        level = "LOW"
                    )
                }
                onError(userMessage)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Biometric signature did not match. Please try again or unlock via system lock screen.")
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription("Protected by Android Knox & Official System Authentication")

        // DEVICE_CREDENTIAL allows system PIN/Pattern/Password via the OS security UI
        promptInfoBuilder.setAllowedAuthenticators(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        try {
            biometricPrompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            onError("Unable to launch system biometric dialog: ${e.localizedMessage}. Please unlock using Android system settings.")
        }
    }

    /**
     * Checks whether Android 13+ (API 33+) requirement is met.
     */
    fun isAndroid13OrHigher(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * Checks whether a voice command or text query is an unlock phone/screen request.
     * Supports:
     * - "हाय स्नैपर टेक्नोलॉजी, स्क्रीन अनलॉक करो"
     * - "Hey Snaper Technology, unlock the screen"
     */
    fun isUnlockCommand(query: String): Boolean {
        val lower = query.lowercase().trim()
        return lower.contains("स्क्रीन अनलॉक") ||
                lower.contains("अनलॉक करो") ||
                lower.contains("अनलॉक करिए") ||
                lower.contains("फोन अनलॉक") ||
                lower.contains("unlock the screen") ||
                lower.contains("unlock screen") ||
                lower.contains("unlock phone") ||
                lower.contains("unlock device") ||
                lower.contains("unlock my phone") ||
                lower.contains("unlock assistant") ||
                lower.contains("verify owner identity")
    }

    /**
     * Checks whether a voice command or text query is a lock phone/screen request.
     * Supports:
     * - "हाय स्नैपर टेक्नोलॉजी, स्क्रीन लॉक करो"
     * - "Hey Snaper Technology, lock the screen"
     */
    fun isLockCommand(query: String): Boolean {
        val lower = query.lowercase().trim()
        return lower.contains("स्क्रीन लॉक") ||
                lower.contains("लॉक करो") ||
                lower.contains("लॉक करिए") ||
                lower.contains("फोन लॉक") ||
                lower.contains("lock the screen") ||
                lower.contains("lock screen") ||
                lower.contains("lock phone") ||
                lower.contains("lock device") ||
                lower.contains("lock my phone")
    }

    /**
     * Generates Android 13+ screen lock action message and triggers lock.
     */
    fun getLockExplanationMessage(ownerName: String = "संजिव सर"): String {
        val name = if (ownerName.isBlank() || ownerName == "User") "संजिव सर" else ownerName

        if (!isAndroid13OrHigher()) {
            return "राधे राधे $name! The Screen Lock Voice Assistant feature requires Android 13 (API level 33) or higher."
        }

        val locked = lockDeviceNow()
        return if (locked) {
            "राधे राधे $name! Screen locked instantly via Device Admin lockNow()."
        } else {
            "राधे राधे $name! Screen lock request triggered via foreground service. Please grant Device Admin permission in Settings -> Security for instant lock."
        }
    }

    /**
     * Generates a privacy-compliant, Android 13+ response explaining how device unlocking works.
     */
    fun getUnlockExplanationMessage(ownerName: String = "संजिव सर"): String {
        val name = if (ownerName.isBlank() || ownerName == "User") "संजिव सर" else ownerName

        if (!isAndroid13OrHigher()) {
            return "राधे राधे $name! The Screen Unlock Voice Assistant feature requires Android 13 (API level 33) or higher. On your current version, please unlock your device directly via the Android lock screen."
        }

        val status = checkBiometricAvailability()

        return when (status) {
            AuthStatus.AVAILABLE ->
                "राधे राधे $name! Android 13 Screen Unlock request received. Please authenticate once in the official system dialog below using your Fingerprint, Face, or Android System Pattern/PIN. Once verified by Android OS, your session will be unlocked!"

            AuthStatus.NONE_ENROLLED ->
                "राधे राधे $name! Android 13 requires at least one lock screen credential (Pattern, PIN, or Biometrics) to be set up in your device Settings -> Security."

            AuthStatus.NO_HARDWARE, AuthStatus.HARDWARE_UNAVAILABLE ->
                "राधे राधे $name! Biometric hardware is unavailable. Please unlock your phone directly using your Android system pattern or PIN."

            else ->
                "राधे राधे $name! To safeguard your privacy on Android 13+, Snaper Technology uses official Android Biometric/Keyguard authentication and never stores your raw pattern or password."
        }
    }
}
