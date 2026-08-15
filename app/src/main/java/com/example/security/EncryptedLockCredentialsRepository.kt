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
 * Encrypted Lock Credentials Repository using hardware-backed Android KeyStore AES/GCM encryption.
 * Securely stores PIN / Pattern / Password lock credentials with zero plain-text leaks.
 */
class EncryptedLockCredentialsRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("snaper_secure_lock_credentials_store", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ALIAS = "SnaperLockCredentialsMasterKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val PREF_KEY_PIN_CIPHER = "enc_pin_cipher"
        private const val PREF_KEY_PIN_IV = "enc_pin_iv"
    }

    init {
        generateMasterKeyIfNeeded()
    }

    private fun generateMasterKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
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
    }

    private fun getMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    fun saveLockPIN(pin: String): Boolean {
        return try {
            val cipher = Cipher.getInstance(AES_MODE)
            cipher.init(Cipher.ENCRYPT_MODE, getMasterKey())
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(pin.toByteArray(Charsets.UTF_8))

            val encBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)

            prefs.edit()
                .putString(PREF_KEY_PIN_CIPHER, encBase64)
                .putString(PREF_KEY_PIN_IV, ivBase64)
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getLockPIN(): String? {
        val encBase64 = prefs.getString(PREF_KEY_PIN_CIPHER, null) ?: return null
        val ivBase64 = prefs.getString(PREF_KEY_PIN_IV, null) ?: return null

        return try {
            val encryptedBytes = Base64.decode(encBase64, Base64.NO_WRAP)
            val iv = Base64.decode(ivBase64, Base64.NO_WRAP)

            val cipher = Cipher.getInstance(AES_MODE)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun hasStoredCredentials(): Boolean {
        return !getLockPIN().isNullOrBlank()
    }

    fun verifyLockCredential(credential: String): Boolean {
        val storedPin = getLockPIN() ?: return false
        return storedPin == credential
    }

    fun clearLockPIN() {
        prefs.edit().remove(PREF_KEY_PIN_CIPHER).remove(PREF_KEY_PIN_IV).apply()
    }
}
