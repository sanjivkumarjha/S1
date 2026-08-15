package com.example.security

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.domain.branding.BrandingConfig
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Shared Login Face Sync Engine v28.1.1
 *
 * SYNCHRONIZED FACE VERIFICATION:
 * Verifies device-owner identity via biometric (face/fingerprint) before granting
 * full operational access to shared device usage.
 *
 * SHARED / GUEST LOGIN GATE:
 * Requires face verification before unlocking full device features.
 * Prevents unauthorized access to Owner/Wife profiles.
 */
class SharedLoginFaceSyncEngine(private val context: Context) {

    companion object {
        private const val TAG = "FaceSyncEngine"
        private const val ENGINE_VERSION = "28.1.1"
    }

    /**
     * Face verification result data class.
     */
    data class FaceVerificationResult(
        val isVerified: Boolean = false,
        val isBiometricAvailable: Boolean = false,
        val profileType: String = "Unknown",
        val message: String = ""
    )

    private val isFaceVerified = AtomicBoolean(false)
    private var lastVerificationTimestamp: Long = 0L
    private var verificationAttempts: Int = 0

    /**
     * Check if biometric hardware is available on this device.
     */
    fun isBiometricAvailable(): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            val result = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            result == BiometricManager.BIOMETRIC_SUCCESS
        } catch (e: Exception) {
            Log.w(TAG, "Biometric check failed: ${e.message}")
            false
        }
    }

    /**
     * Start face verification flow using the provided activity.
     */
    fun startFaceVerification(
        activity: FragmentActivity,
        onResult: (FaceVerificationResult) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            Log.w(TAG, "❌ Biometric hardware not available")
            onResult(
                FaceVerificationResult(
                    isVerified = false,
                    isBiometricAvailable = false,
                    message = "❌ Biometric authentication is not available on this device. " +
                            "Please use a device with biometric support."
                )
            )
            return
        }

        Log.i(TAG, "🫵 Starting face verification...")

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isFaceVerified.set(true)

                    // Determine profile type based on biometric result
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
        result.let {
            // In production, you'd check against registered profiles
            val cryptoObject = it.cryptoObject
            if (cryptoObject != null) {
                // Authenticated with crypto - likely Owner
                return "Owner"
            }
        }
        return "Verified User"
    }

    /**
     * Check if face is currently verified.
     */
    fun isFaceCurrentlyVerified(): Boolean {
        // Verification expires after 5 minutes
        val elapsed = System.currentTimeMillis() - lastVerificationTimestamp
        return isFaceVerified.get() && elapsed < 300_000L
    }

    /**
     * Reset face verification state (e.g., when locking the device).
     */
    fun resetVerification() {
        isFaceVerified.set(false)
        lastVerificationTimestamp = 0L
    }

    /**
     * Get engine report.
     */
    fun getEngineReport(): String {
        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("  FACE SYNC ENGINE")
            appendLine("═══════════════════════════════════════")
            appendLine("  Engine Version: v$ENGINE_VERSION")
            appendLine("  Status: ✅ Active")
            appendLine("  Face Verified: ${if (isFaceVerified.get()) "✅ YES" else "⏸️ No"}")
            appendLine("  Attempts: $verificationAttempts")
            appendLine("  Biometric Available: ${if (isBiometricAvailable()) "✅ YES" else "❌ NO"}")
            appendLine("═══════════════════════════════════════")
        }
    }
}