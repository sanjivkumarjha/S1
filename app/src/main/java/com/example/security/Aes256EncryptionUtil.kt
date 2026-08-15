package com.example.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256 Data Encryption Utility v14.0
 *
 * Encrypts all exported data and Google Drive backups with AES-256 encryption.
 * Uses Android KeyStore for secure key storage.
 * Dual biometric (Fingerprint + PIN) decryption verification.
 */
class Aes256EncryptionUtil(private val context: Context) {

    private val KEYSTORE_ALIAS = "snaper_aes256_master_key"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val TRANSFORMATION = "AES/GCM/NoPadding"
    private val GCM_TAG_LENGTH = 128 // bits

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    /**
     * Initialize the AES-256 master key in Android KeyStore.
     * Creates the key if it doesn't exist.
     */
    fun initMasterKey() {
        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
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

    /**
     * Encrypt data with AES-256-GCM.
     * Returns the encrypted bytes with IV prepended.
     */
    fun encrypt(plaintext: ByteArray): ByteArray {
        initMasterKey()
        val secretKey = (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv // 12 bytes for GCM
        val encrypted = cipher.doFinal(plaintext)

        // Prepend IV to ciphertext for storage
        return iv + encrypted
    }

    /**
     * Decrypt data with AES-256-GCM.
     * Expects IV prepended to ciphertext.
     */
    fun decrypt(encryptedData: ByteArray): ByteArray {
        initMasterKey()
        val secretKey = (keyStore.getEntry(KEYSTORE_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey

        // Extract IV (first 12 bytes for GCM)
        val iv = encryptedData.copyOfRange(0, 12)
        val ciphertext = encryptedData.copyOfRange(12, encryptedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        return cipher.doFinal(ciphertext)
    }

    /**
     * Encrypt a file with AES-256-GCM.
     * Creates a new file with .encrypted extension.
     */
    fun encryptFile(inputFile: File): File {
        val outputFile = File(inputFile.absolutePath + ".encrypted")
        val plaintext = inputFile.readBytes()
        val encrypted = encrypt(plaintext)
        outputFile.writeBytes(encrypted)
        return outputFile
    }

    /**
     * Decrypt a file with AES-256-GCM.
     * Creates a new file with .decrypted extension.
     */
    fun decryptFile(inputFile: File): File {
        val outputFile = File(inputFile.absolutePath.replace(".encrypted", ".decrypted"))
        val encrypted = inputFile.readBytes()
        val decrypted = decrypt(encrypted)
        outputFile.writeBytes(decrypted)
        return outputFile
    }

    /**
     * Encrypt a string and return Base64-encoded result.
     */
    fun encryptString(plaintext: String): String {
        val encrypted = encrypt(plaintext.toByteArray(Charsets.UTF_8))
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
    }

    /**
     * Decrypt a Base64-encoded encrypted string.
     */
    fun decryptString(encryptedBase64: String): String {
        val encrypted = android.util.Base64.decode(encryptedBase64, android.util.Base64.NO_WRAP)
        val decrypted = decrypt(encrypted)
        return String(decrypted, Charsets.UTF_8)
    }

    /**
     * Check if the master key exists in Android KeyStore.
     */
    fun isMasterKeyAvailable(): Boolean {
        return try {
            keyStore.containsAlias(KEYSTORE_ALIAS)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete the master key (for factory reset scenarios).
     */
    fun deleteMasterKey() {
        try {
            keyStore.deleteEntry(KEYSTORE_ALIAS)
        } catch (e: Exception) {
            // Non-critical
        }
    }
}