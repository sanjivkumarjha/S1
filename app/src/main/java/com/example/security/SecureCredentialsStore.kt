package com.example.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Secure on-device store for sensitive credentials (API keys / tokens).
 *
 * Uses the hardware-backed Android KeyStore with AES/GCM (NoPadding) — the same
 * approach used for lock credentials — so API keys are never written to plain
 * DataStore/SharedPreferences. Keys never leave the device and are excluded from
 * logs. Falls back gracefully (returns null) if the KeyStore is unavailable so
 * callers can degrade to "no key configured" rather than crash.
 */
class SecureCredentialsStore(private val context: Context) {

    private val prefs =
        context.getSharedPreferences("snaper_secure_credentials_store", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ALIAS = "SnaperApiCredentialsMasterKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val IV_SUFFIX = "_iv"
        const val USER_API_KEY = "user_api_key"
        // Per-provider credential keys (stored encrypted)
        const val GEMINI_API_KEY = "gemini_api_key"
        const val OPENAI_API_KEY = "openai_api_key"
        const val CLAUDE_API_KEY = "claude_api_key"
        const val GROK_API_KEY = "grok_api_key"
        const val NVIDIA_API_KEY = "nvidia_api_key"
        const val OPENROUTER_API_KEY = "openrouter_api_key"
        const val KIMI_API_KEY = "kimi_api_key"
        const val GLM_API_KEY = "glm_api_key"
        const val ELEVENLABS_API_KEY = "elevenlabs_api_key"
    }

    init {
        generateMasterKeyIfNeeded()
    }

    private fun generateMasterKeyIfNeeded() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                val spec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // KeyStore may be unavailable on some emulators/older devices; degrade gracefully.
            e.printStackTrace()
        }
    }

    private fun getMasterKey(): SecretKey? = try {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    /** Encrypt and store a credential under [key]. Pass blank value to clear it. */
    fun saveCredential(key: String, value: String): Boolean {
        if (value.isBlank()) {
            clearCredential(key)
            return true
        }
        return try {
            val masterKey = getMasterKey() ?: return false
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, masterKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            prefs.edit()
                .putString(key, Base64.encodeToString(encryptedBytes, Base64.NO_WRAP))
                .putString(key + IV_SUFFIX, Base64.encodeToString(iv, Base64.NO_WRAP))
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getCredential(key: String): String? {
        val encBase64 = prefs.getString(key, null) ?: return null
        val ivBase64 = prefs.getString(key + IV_SUFFIX, null) ?: return null
        return try {
            val masterKey = getMasterKey() ?: return null
            val encryptedBytes = Base64.decode(encBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.DECRYPT_MODE, masterKey, GCMParameterSpec(128, iv))
            String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun hasCredential(key: String): Boolean = !getCredential(key).isNullOrBlank()

    fun clearCredential(key: String) {
        prefs.edit().remove(key).remove(key + IV_SUFFIX).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
