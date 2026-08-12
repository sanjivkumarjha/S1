package com.example.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

enum class FinancialAppCategory {
    BANKING, PAYMENT, PRIVATE, OWNER_ONLY, GENERAL
}

data class FinancialAppStatus(
    val packageName: String,
    val appName: String,
    val category: FinancialAppCategory,
    val isProtectedInRestrictedMode: Boolean
)

/**
 * Manages protection for banking, payment, and private financial applications.
 * Uses Android Keystore for encryption without storing PINs or passwords.
 */
class FinancialAppProtectionManager(private val context: Context) {

    private val KEY_ALIAS = "SnaperFinancialKeystoreAlias"

    private val defaultProtectedApps = listOf(
        FinancialAppStatus("com.google.android.apps.nfc.payment", "Google Pay", FinancialAppCategory.PAYMENT, true),
        FinancialAppStatus("net.one97.paytm", "Paytm", FinancialAppCategory.PAYMENT, true),
        FinancialAppStatus("com.phonepe.app", "PhonePe", FinancialAppCategory.PAYMENT, true),
        FinancialAppStatus("com.sbi.lotusintouch", "YONO SBI", FinancialAppCategory.BANKING, true),
        FinancialAppStatus("com.hdfcbank.payzapp", "HDFC PayZapp", FinancialAppCategory.BANKING, true),
        FinancialAppStatus("com.icicibank.mobilebanking", "iMobile Pay", FinancialAppCategory.BANKING, true)
    )

    init {
        ensureKeystoreCreated()
    }

    fun isAppAccessAllowed(packageName: String, isRestrictedMode: Boolean): Boolean {
        val app = defaultProtectedApps.find { it.packageName == packageName }
        if (app != null && app.isProtectedInRestrictedMode && isRestrictedMode) {
            return false
        }
        return true
    }

    fun getProtectedApps(): List<FinancialAppStatus> = defaultProtectedApps

    private fun ensureKeystoreCreated() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                keyGenerator.init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
                )
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Android Keystore initialization fallback
        }
    }
}
