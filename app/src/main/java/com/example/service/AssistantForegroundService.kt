package com.example.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.NotificationHelper
import com.example.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AssistantForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        _isServiceRunning.value = true
        NotificationHelper.createChannel(
            this,
            CHANNEL_ID,
            "Snaper Assistant 24/7 Service",
            importance = android.app.NotificationManager.IMPORTANCE_LOW,
            description = "Runs background automation, wake word monitoring & Dynamic Island overlay"
        )
        startForegroundTyped()
    }

    private fun startForegroundTyped() {
        val notification = buildNotification("Snaper Assistant Active & Monitoring")
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Always re-assert the foreground state so the service does not get killed
        // when notified from a foreground context.
        val statusMsg = intent?.getStringExtra(EXTRA_STATUS_MSG) ?: "Snaper Technology Assistant Running 24/7"
        startForegroundTyped()
        updateNotification(statusMsg)

        val action = intent?.action
        if (action == ACTION_STOP_SERVICE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            stopSelf()
            _isServiceRunning.value = false
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        _isServiceRunning.value = false
    }

    private fun buildNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Snaper Technology AI Assistant")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification_small)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification(contentText: String) {
        NotificationHelper.notifyIfPermitted(this, NOTIFICATION_ID, buildNotification(contentText))
    }

    companion object {
        const val CHANNEL_ID = "snaper_foreground_service_channel"
        const val NOTIFICATION_ID = 8801
        const val ACTION_STOP_SERVICE = "com.example.service.STOP_FOREGROUND"
        const val EXTRA_STATUS_MSG = "extra_status_msg"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun startService(context: Context, statusMessage: String = "Snaper Assistant Active"): Boolean {
            if (!PermissionHelper.hasPostNotifications(context)) {
                android.util.Log.w(
                    "AssistantForegroundService",
                    "POST_NOTIFICATIONS permission missing; foreground service start skipped."
                )
                return false
            }

            val intent = Intent(context, AssistantForegroundService::class.java).apply {
                putExtra(EXTRA_STATUS_MSG, statusMessage)
            }
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                true
            } catch (e: Exception) {
                // Background service start may be restricted on Android 12+ when the app is
                // not in a foregrounded state. Fail gracefully instead of crashing.
                android.util.Log.w("AssistantForegroundService", "start blocked: ${e.message}")
                false
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AssistantForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                android.util.Log.w("AssistantForegroundService", "stop blocked: ${e.message}")
            }
        }
    }
}
