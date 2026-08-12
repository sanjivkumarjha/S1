package com.example.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R

/**
 * Centralized, crash-safe notification helper.
 *
 * On Android 13+ (API 33+) posting a notification (including a foreground-service
 * notification) without the granted POST_NOTIFICATIONS runtime permission throws
 * SecurityException. Every foreground service / notification path in the app must
 * go through this helper so it degrades gracefully instead of crashing.
 */
object NotificationHelper {

    fun createChannel(
        context: Context,
        channelId: String,
        name: String,
        importance: Int = NotificationManager.IMPORTANCE_LOW,
        description: String? = null
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, name, importance).apply {
                    if (description != null) this.description = description
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    /** True only when the app is actually allowed to post notifications (Android 13+ aware). */
    fun areNotificationsEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } else {
            true
        }
    }

    fun buildForegroundNotification(
        context: Context,
        channelId: String,
        contentTitle: String,
        contentText: String,
        pendingIntent: android.app.PendingIntent? = null,
        smallIconRes: Int = R.drawable.ic_notification_small,
        category: String = NotificationCompat.CATEGORY_SERVICE
    ): Notification {
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(smallIconRes)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(category)
        if (pendingIntent != null) builder.setContentIntent(pendingIntent)
        return builder.build()
    }

    /**
     * Posts a notification, but only when the runtime POST_NOTIFICATIONS permission is granted.
     * Returns true if posted, false otherwise (so callers can decide whether to start an FGS).
     */
    fun notifyIfPermitted(
        context: Context,
        notificationId: Int,
        notification: Notification
    ): Boolean {
        if (!areNotificationsEnabled(context)) return false
        return try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
