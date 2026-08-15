package com.example.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.domain.branding.BrandingConfig
import com.example.util.NotificationHelper
import com.example.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Emergency Lockdown Foreground Service v28.1.1
 *
 * Activated during:
 * - SIM card extraction
 * - Power-off/reboot attempts during emergency
 * - Flight mode override
 * - Active threat/assault detection
 *
 * Maintains a persistent foreground notification to prevent OS kill,
 * acquires wake locks to prevent sleep, and runs background distress
 * protocols even when the screen appears to be off/fake-shutdown.
 */
class EmergencyLockdownService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var powerWakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        _isLockdownActive.value = true
        Log.w(TAG, "🚨 EmergencyLockdownService CREATED")

        // Create notification channel
        NotificationHelper.createChannel(
            this,
            CHANNEL_ID,
            "Snaper Emergency Lockdown",
            importance = android.app.NotificationManager.IMPORTANCE_HIGH,
            description = "Critical emergency lockdown service - DO NOT STOP"
        )

        // Start foreground with high-priority notification
        startForegroundTyped()

        // Acquire wake lock to prevent sleep
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reason = intent?.getStringExtra("emergency_reason") ?: "unknown"
        val lockdownMode = intent?.getStringExtra("lockdown_mode") ?: "normal"
        val emergencyType = intent?.getStringExtra("emergency_type") ?: "general"

        Log.w(TAG, "🚨 Lockdown command: reason=$reason, mode=$lockdownMode, type=$emergencyType")

        // Update notification with reason
        updateNotification("🚨 EMERGENCY: $reason")

        // Handle specific lockdown modes
        when (lockdownMode) {
            "fake_shutdown" -> {
                // In fake shutdown mode, we keep the service running
                // while the UI layer shows a fake shutdown screen
                Log.w(TAG, "🔒 Fake shutdown mode engaged - background protocols active")
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        releaseWakeLock()
        _isLockdownActive.value = false
        Log.i(TAG, "EmergencyLockdownService destroyed")
    }

    private fun startForegroundTyped() {
        val notification = buildEmergencyNotification("🚨 Emergency Lockdown Active")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } catch (e: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildEmergencyNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 ${BrandingConfig.PRODUCT_NAME} Emergency")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun updateNotification(contentText: String) {
        NotificationHelper.notifyIfPermitted(this, NOTIFICATION_ID, buildEmergencyNotification(contentText))
    }

    private fun acquireWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null && powerWakeLock == null) {
                powerWakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                    "${BrandingConfig.PRODUCT_NAME}:EmergencyLockdown"
                )
                powerWakeLock?.acquire(30 * 60 * 1000L) // 30 minutes max
                Log.d(TAG, "Wake lock acquired for lockdown")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to acquire wake lock: ${e.message}")
        }
    }

    private fun releaseWakeLock() {
        try {
            powerWakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to release wake lock: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "EmergencyLockdown"
        const val CHANNEL_ID = "snaper_emergency_lockdown_channel"
        const val NOTIFICATION_ID = 9901

        private val _isLockdownActive = MutableStateFlow(false)
        val isLockdownActive: StateFlow<Boolean> = _isLockdownActive.asStateFlow()

        fun startLockdown(context: Context, reason: String, mode: String = "normal") {
            val intent = Intent(context, EmergencyLockdownService::class.java).apply {
                putExtra("emergency_reason", reason)
                putExtra("lockdown_mode", mode)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start lockdown: ${e.message}")
            }
        }

        fun stopLockdown(context: Context) {
            try {
                context.stopService(Intent(context, EmergencyLockdownService::class.java))
            } catch (e: Exception) {
                Log.w(TAG, "Failed to stop lockdown: ${e.message}")
            }
        }
    }
}