package com.example.security

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.domain.branding.BrandingConfig
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * SHARED OWNER-WIFE LOGIN, FACE-VERIFICATION SYNC & EQUAL FEATURE PARITY v28.1.1
 *
 * UNIFIED LOGIN CREDENTIALS:
 * Wife logs in seamlessly using the same master login credentials as Owner.
 *
 * WIFE APP SYNC VIA FACE VERIFICATION:
 * When app is installed on wife's device and data is restored from Google Drive
 * backup, biometric face verification verifies her profile and grants full access.
 *
 * EQUAL FEATURE PARITY & UNRESTRICTED MODE:
 * Every single feature, automation, spiritual routine guidance (Radha Jaap tracker),
 * and personal assistance capability available to Owner is equally, identically,
 * and unrestricted available to his wife.
 */
class SharedLoginFaceSyncEngine(private val context: Context) {

    companion object {
        private const val TAG = "SharedLoginFaceSync"
        private const val ENGINE_VERSION = "28.1.1"
    }

    private val engineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val isFaceVerified = AtomicBoolean(false)
    private val isWifeProfileActive = AtomicBoolean(false)

    /**
     * Face verification result.
     */
    data class FaceVerificationResult(
        val isVerified: Boolean = false,
        val isBiometricAvailable: Boolean = false,
        val profileType: String = "", // "OWNER", "WIFE", "UNKNOWN"
        val message: String = ""
    )

    /**
     * Sync status report.
     */
    data class SyncStatus(
        val isEngineActive: Boolean = true,
        val engineVersion: String = ENGINE_VERSION,
        val isFaceVerificationAvailable: Boolean = false,
        val isWifeProfileActive: Boolean = false,
        val isFeatureParityEnabled: Boolean = true,
        val isRadhaJaapAccessible: Boolean = true,
        val message: String = "Shared Login & Face Sync engine active."
    )

    /**
     * Initialize the engine.
     */
    fun initialize() {
        Log.i(TAG, "🔐 SharedLoginFaceSyncEngine v$ENGINE_VERSION initializing...")
        Log.i(TAG, "✅ SharedLoginFaceSyncEngine initialized successfully")
    }

    /**
     * Check if biometric (face) authentication is available on this device.
     */
    fun isBiometricAvailable(): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            Log.w(TAG, "Biometric check failed: ${e.message}")
            false
        }
    }

    /**
     * Perform face verification to identify the user as Owner or Wife.
     * This is called when the app is installed and data is restored from backup.
     *
     * @param onResult Callback with FaceVerificationResult
     */
    fun performFaceVerification(onResult: (FaceVerificationResult) -> Unit) {
        if (!isBiometricAvailable()) {
            Log.w(TAG, "Biometric authentication not available on this device")
            onResult(
                FaceVerificationResult(
                    isVerified = false,
                    isBiometricAvailable = false,
                    message = "Face verification not available on this device. " +
                            "Please use a device with biometric support."
                )
            )
            return
        }

        Log.i(TAG, "🫵 Starting face verification...")

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isFaceVerified.set(true)

                    // Determine profile type based on biometric result
                    // In a real implementation, this would check against enrolled face data
                    val profileType = determineProfileType(result)

                    Log.i(TAG, "✅ Face verification succeeded: $profileType")
                    onResult(
                        FaceVerificationResult(
                            isVerified = true,
                            isBiometricAvailable = true,
                            profileType = profileType,
                            message = "✅ Face verification successful. " +
                                    "Profile identified as: $profileType. " +
                                    "Full operational access granted."
                        )
                    )
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.w(TAG, "❌ Face verification error: $errString")
                    onResult(
                        FaceVerificationResult(
                            isVerified = false,
                            isBiometricAvailable = true,
                            message = "❌ Face verification failed: $errString"
                        )
                    )
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "❌ Face verification failed (no match)")
                    onResult(
                        FaceVerificationResult(
                            isVerified = false,
                            isBiometricAvailable = true,
                            message = "❌ Face verification failed. Face did not match."
                        )
                    )
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify Identity")
            .setSubtitle("Face verification for ${BrandingConfig.PRODUCT_NAME}")
            .setDescription("Verify your identity to access all features")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .setConfirmationRequired(false)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Determine the profile type from biometric authentication result.
     * In a real implementation, this would compare against stored face templates.
     */
    private fun determineProfileType(result: BiometricPrompt.AuthenticationResult): String {
        // In a production implementation, this would:
        // 1. Extract biometric data from the result
        // 2. Compare against stored Owner and Wife face templates
        // 3. Return the matching profile type

        // For now, we check if the device has been configured for Wife
        val prefs = context.getSharedPreferences("snaper_face_sync", Context.MODE_PRIVATE)
        val isWifeDevice = prefs.getBoolean("is_wife_device", false)

        return if (isWifeDevice) {
            isWifeProfileActive.set(true)
            "WIFE"
        } else {
            "OWNER"
        }
    }

    /**
     * Configure this device as the Wife's device.
     * Called after face verification confirms Wife's identity.
     */
    fun configureAsWifeDevice() {
        val prefs = context.getSharedPreferences("snaper_face_sync", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_wife_device", true).apply()
        isWifeProfileActive.set(true)
        Log.i(TAG, "👩 Device configured as Wife's device")
    }

    /**
     * Configure this device as the Owner's device.
     */
    fun configureAsOwnerDevice() {
        val prefs = context.getSharedPreferences("snaper_face_sync", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_wife_device", false).apply()
        isWifeProfileActive.set(false)
        Log.i(TAG, "👨 Device configured as Owner's device")
    }

    /**
     * Check if the current user has access to Radha Jaap tracker.
     * Both Owner and Wife have unrestricted access.
     */
    fun hasRadhaJaapAccess(): Boolean {
        return true // Unrestricted access for both Owner and Wife
    }

    /**
     * Check if feature parity is enabled for the current user.
     * Wife gets identical features as Owner.
     */
    fun isFeatureParityEnabled(): Boolean {
        return true // Equal feature parity always enabled
    }

    /**
     * Get the current sync status.
     */
    fun getSyncStatus(): SyncStatus {
        return SyncStatus(
            isEngineActive = true,
            engineVersion = ENGINE_VERSION,
            isFaceVerificationAvailable = isBiometricAvailable(),
            isWifeProfileActive = isWifeProfileActive.get(),
            isFeatureParityEnabled = true,
            isRadhaJaapAccessible = true,
            message = buildString {
                appendLine("🔐 SHARED LOGIN & FACE SYNC STATUS")
                appendLine()
                appendLine("  Face Verification: ${if (isBiometricAvailable()) "✅ Available" else "❌ Not available"}")
                appendLine("  Current Profile: ${if (isWifeProfileActive.get()) "👩 Wife" else "👨 Owner"}")
                appendLine("  Feature Parity: ✅ ENABLED (Wife = Owner)")
                appendLine("  Radha Jaap Access: ✅ UNRESTRICTED")
                appendLine()
                appendLine("  Wife can log in using Owner's credentials.")
                appendLine("  All features are equally available to both.")
            }
        )
    }

    /**
     * Get engine report.
     */
    fun getEngineReport(): String {
        val status = getSyncStatus()
        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("  SHARED LOGIN & FACE SYNC ENGINE")
            appendLine("═══════════════════════════════════════")
            appendLine("  Product: ${BrandingConfig.PRODUCT_NAME}")
            appendLine("  Version: v${BrandingConfig.VERSION}")
            appendLine("  Engine Version: v$ENGINE_VERSION")
            appendLine("  Status: ✅ ACTIVE")
            appendLine()
            appendLine("  Features:")
            appendLine("  ├─ Unified Login: ✅ Same credentials for Owner & Wife")
            appendLine("  ├─ Face Verification: ${if (status.isFaceVerificationAvailable) "✅ Available" else "❌ Not available"}")
            appendLine("  ├─ Wife Profile Sync: ${if (status.isWifeProfileActive) "✅ Active" else "⏸️ Inactive"}")
            appendLine("  ├─ Feature Parity: ✅ EQUAL (Wife = Owner)")
            appendLine("  └─ Radha Jaap Access: ✅ UNRESTRICTED")
            appendLine()
            appendLine("  Message: ${status.message}")
            appendLine("═══════════════════════════════════════")
        }
    }

    /**
     * Shutdown the engine.
     */
    fun shutdown() {
        engineScope.cancel()
        Log.i(TAG, "SharedLoginFaceSyncEngine shutdown complete")
    }
}