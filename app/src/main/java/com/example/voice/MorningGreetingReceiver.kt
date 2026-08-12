package com.example.voice

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.util.NotificationHelper

class MorningGreetingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        showMorningGreetingNotification(context)
    }

    private fun showMorningGreetingNotification(context: Context) {
        val channelId = "snaper_morning_greeting_channel"

        NotificationHelper.createChannel(
            context,
            channelId,
            "Snaper Morning Greetings",
            importance = android.app.NotificationManager.IMPORTANCE_HIGH,
            description = "Daily Morning Greetings from Snaper AI"
        )

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("राधे राधे! 🙏🌸")
            .setContentText("Good morning! Snaper AI wishes you a blessed and joyous day ahead.")
            .setSmallIcon(R.drawable.ic_notification_small)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // On Android 13+ this no-ops if POST_NOTIFICATIONS is not granted (instead of crashing).
        NotificationHelper.notifyIfPermitted(context, 2002, notification)
    }
}
