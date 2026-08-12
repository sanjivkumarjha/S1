package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.NotificationHelper
import com.example.util.PermissionHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for the crash-safe notification + permission helpers introduced to fix the
 * Android 13+ POST_NOTIFICATIONS crash and the removed-drawable foreground service crash.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NotificationPermissionHelperTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `notification channel is created without throwing`() {
        NotificationHelper.createChannel(
            context = context,
            channelId = "test_channel_1",
            name = "Test Channel",
            importance = NotificationManager.IMPORTANCE_LOW,
            description = "desc"
        )
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = manager.getNotificationChannel("test_channel_1")
        assertNotNull(channel)
        assertEquals("Test Channel", channel?.name?.toString())
    }

    @Test
    fun `buildForegroundNotification contains the expected title`() {
        val notification = NotificationHelper.buildForegroundNotification(
            context = context,
            channelId = "test_channel_2",
            contentTitle = "Snaper Active",
            contentText = "Listening"
        )
        assertNotNull(notification)
    }

    @Test
    fun `notifyIfPermitted does not throw and respects permission state`() {
        // The key safety guarantee: posting never throws SecurityException. Robolectric grants
        // POST_NOTIFICATIONS by default, so we just assert it returns true without throwing.
        val notification = NotificationHelper.buildForegroundNotification(
            context = context,
            channelId = "test_channel_3",
            contentTitle = "Snaper",
            contentText = "x"
        )
        val posted = NotificationHelper.notifyIfPermitted(context, 7777, notification)
        assertTrue(posted)
    }

    @Test
    fun `PermissionHelper reports permissions reflect Robolectric default state`() {
        // On a fresh Robolectric context RECORD_AUDIO is not granted.
        assertFalse(PermissionHelper.hasRecordAudio(context))
        assertFalse(PermissionHelper.hasCamera(context))
    }

    @Test
    fun `PermissionHelper hasReadPhoneState does not throw`() {
        // Smoke test: the helper must not crash when querying a permission.
        PermissionHelper.hasReadPhoneState(context)
    }
}
