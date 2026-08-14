package com.example.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Runtime permission helpers.
 *
 * The app declares POST_NOTIFICATIONS in the manifest, but on Android 13+ it must be
 * requested at runtime before any foreground service posts a notification, otherwise
 * the service start crashes with SecurityException / ForegroundServiceStartNotAllowedException.
 */
object PermissionHelper {

    fun hasPostNotifications(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true
            else ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            android.util.Log.e("PermissionHelper", "Failed to check post notifications permission: ${e.message}")
            false
        }
    }

    fun hasRecordAudio(context: Context): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            android.util.Log.e("PermissionHelper", "Failed to check record audio permission: ${e.message}")
            false
        }
    }

    fun hasCamera(context: Context): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            android.util.Log.e("PermissionHelper", "Failed to check camera permission: ${e.message}")
            false
        }
    }

    fun hasCallPhone(context: Context): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            android.util.Log.e("PermissionHelper", "Failed to check call phone permission: ${e.message}")
            false
        }
    }

    fun hasReadContacts(context: Context): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            android.util.Log.e("PermissionHelper", "Failed to check read contacts permission: ${e.message}")
            false
        }
    }

    fun hasWriteContacts(context: Context): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            android.util.Log.e("PermissionHelper", "Failed to check write contacts permission: ${e.message}")
            false
        }
    }

    fun hasReadPhoneState(context: Context): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            android.util.Log.e("PermissionHelper", "Failed to check read phone state permission: ${e.message}")
            false
        }
    }

    fun hasAnswerPhoneCalls(context: Context): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ANSWER_PHONE_CALLS
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            android.util.Log.e("PermissionHelper", "Failed to check answer phone calls permission: ${e.message}")
            false
        }
    }

    /** Requests POST_NOTIFICATIONS at runtime (Android 13+). No-op on lower APIs. */
    fun requestPostNotifications(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !hasPostNotifications(activity)
            ) {
                activity.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 9101)
            }
        } catch (e: Exception) {
            android.util.Log.e("PermissionHelper", "Failed to request post notifications: ${e.message}")
        }
    }
}

/**
 * Composable that automatically requests POST_NOTIFICATIONS permission once when the
 * app first reaches a screen hosting a foreground service / notification path.
 * Renders nothing — preserves the host UI exactly as is.
 */
@Composable
fun EnsureNotificationPermissionEffect(active: Boolean = true) {
    val context = LocalContext.current
    var requested by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> /* Result is best-effort; the service start is guarded by PermissionHelper. */ }

    LaunchedEffect(active) {
        if (active && !requested) {
            requested = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !PermissionHelper.hasPostNotifications(context)) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
