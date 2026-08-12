package com.example.security

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

/**
 * Foreground Service for Screen Lock & Authentication Interaction Flows.
 *
 * Compliant with Android 13+ (API 33+) foreground service policies.
 * Ensures privacy and official OS-supported keyguard & device admin operations.
 */
class ScreenLockService : Service() {

    companion object {
        const val CHANNEL_ID = "snaper_screen_lock_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_LOCK_SCREEN = "com.example.security.ACTION_LOCK_SCREEN"
        const val ACTION_START_FLOW = "com.example.security.ACTION_START_FLOW"
        const val ACTION_STOP_SERVICE = "com.example.security.ACTION_STOP_SERVICE"

        fun startInteractionFlow(context: Context) {
            val intent = Intent(context, ScreenLockService::class.java).apply {
                action = ACTION_START_FLOW
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.w("ScreenLockService", "startInteractionFlow blocked: ${e.message}")
            }
        }

        fun executeScreenLock(context: Context) {
            val intent = Intent(context, ScreenLockService::class.java).apply {
                action = ACTION_LOCK_SCREEN
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                android.util.Log.w("ScreenLockService", "executeScreenLock blocked: ${e.message}")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildForegroundNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } catch (e: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        when (intent?.action) {
            ACTION_LOCK_SCREEN -> {
                performDeviceLock()
            }
            ACTION_STOP_SERVICE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
            ACTION_START_FLOW -> {
                // Keep foreground service active for screen interaction flow
            }
        }

        return START_NOT_STICKY
    }

    private fun performDeviceLock() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val adminComponent = ComponentName(this, SnaperDeviceAdminReceiver::class.java)

        if (devicePolicyManager != null && devicePolicyManager.isAdminActive(adminComponent)) {
            try {
                devicePolicyManager.lockNow()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Snaper Security Screen Interaction"
            val descriptionText = "Monitors device lock/unlock interaction flows safely"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Snaper Security Assistant")
            .setContentText("Screen lock & authentication flow active")
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
