package com.example.security

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.launch

/**
 * Production-grade DeviceAdminReceiver for Snaper Technology Personal Assistant.
 *
 * Security & Privacy Guidelines (Android 13+):
 * - Used exclusively for official system screen locking via DevicePolicyManager.lockNow().
 * - NEVER captures, inspects, stores, or transmits system PINs, patterns, or passwords.
 * - Fully privacy-preserving and compliant with modern Android device administrator guidelines.
 */
class SnaperDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        val securityManager = SecurityManager(context)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            securityManager.logSecurityEvent(
                eventType = "DEVICE_ADMIN_ENABLED",
                description = "Snaper Device Admin privilege granted for secure screen locking",
                level = "INFO"
            )
        }
        Toast.makeText(
            context,
            "Snaper Security: Screen lock permission enabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        val securityManager = SecurityManager(context)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            securityManager.logSecurityEvent(
                eventType = "DEVICE_ADMIN_DISABLED",
                description = "Snaper Device Admin privilege revoked by user",
                level = "MEDIUM"
            )
        }
        Toast.makeText(
            context,
            "Snaper Security: Screen lock permission disabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPasswordChanged(context: Context, intent: Intent) {
        super.onPasswordChanged(context, intent)
        // Privacy Directive: Do NOT attempt to read, store, or process password details.
        val securityManager = SecurityManager(context)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            securityManager.logSecurityEvent(
                eventType = "DEVICE_SECURITY_CREDENTIAL_UPDATED",
                description = "System credential updated in Android OS Keyguard",
                level = "INFO"
            )
        }
    }
}
