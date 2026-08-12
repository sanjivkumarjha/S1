package com.example.voice

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.service.DynamicIslandOverlayService
import com.example.util.NotificationHelper
import com.example.util.PermissionHelper

class VoiceAssistantService : Service() {

    private var voiceAnalyzer: VoiceBiometricAnalyzer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(
            this,
            CHANNEL_ID,
            "Snaper Voice Assistant Channel",
            importance = android.app.NotificationManager.IMPORTANCE_LOW,
            description = "Foreground Service for Snaper AI Background Voice Assistant"
        )
        voiceAnalyzer = VoiceBiometricAnalyzer(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        // Use the typed startForeground on Android 14+ for the microphone FGS type so the
        // system accepts the foreground service. On older versions the 2-arg overload is fine.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                )
            } catch (e: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Launch Always-On Display Dynamic Island Overlay Service (only when overlay permission granted)
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || android.provider.Settings.canDrawOverlays(this)) {
                DynamicIslandOverlayService.start(applicationContext)
            }
        } catch (e: Exception) {
            android.util.Log.e("VoiceAssistantService", "Failed to start DynamicIslandOverlayService: ${e.message}")
        }

        // Start 24/7 Voice Biometric Background Verification (only when microphone permission granted)
        if (com.example.util.PermissionHelper.hasRecordAudio(this)) {
            voiceAnalyzer?.startContinuousBiometricVerification { isOwner, confidence ->
                if (!isOwner) {
                    android.util.Log.w("VoiceAssistantService", "Unauthorized voice detected! Biometric Confidence: $confidence. Assistant remaining silent.")
                } else {
                    android.util.Log.i("VoiceAssistantService", "Owner voice verified! Confidence: $confidence")
                }
            }
        } else {
            android.util.Log.w("VoiceAssistantService", "RECORD_AUDIO permission not granted; biometric verification skipped.")
        }

        return START_STICKY
    }

    override fun onDestroy() {
        // Always release the microphone when the service is torn down.
        try {
            voiceAnalyzer?.stopVerification()
        } catch (e: Exception) {
            android.util.Log.e("VoiceAssistantService", "Error stopping voice analyzer: ${e.message}")
        }
        voiceAnalyzer = null
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Snaper Technology Active")
            .setContentText("Listening in background... Tap to talk to Snaper AI.")
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "snaper_voice_channel"
        const val NOTIFICATION_ID = 1001

        fun start(context: Context): Boolean {
            if (!PermissionHelper.hasPostNotifications(context)) {
                android.util.Log.w("VoiceAssistantService", "POST_NOTIFICATIONS permission missing; service start skipped.")
                return false
            }
            if (!PermissionHelper.hasRecordAudio(context)) {
                android.util.Log.w("VoiceAssistantService", "RECORD_AUDIO permission missing; microphone FGS start skipped.")
                return false
            }

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    context.startForegroundService(Intent(context, VoiceAssistantService::class.java))
                    true
                } catch (e: Exception) {
                    android.util.Log.w("VoiceAssistantService", "startForegroundService blocked: ${e.message}")
                    false
                }
            } else {
                try {
                    context.startService(Intent(context, VoiceAssistantService::class.java))
                    true
                } catch (e: Exception) {
                    android.util.Log.w("VoiceAssistantService", "startService blocked: ${e.message}")
                    false
                }
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, VoiceAssistantService::class.java))
        }
    }
}
