package com.example.appcontrol

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.service.DynamicIslandOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FloatingOverlayWindowManager private constructor(private val context: Context) {

    private val _isOverlayShowing = MutableStateFlow(false)
    val isOverlayShowing: StateFlow<Boolean> = _isOverlayShowing.asStateFlow()

    fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun getOverlayPermissionIntent(): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun launchAppInFloatingWindow(packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
                }
            }
            if (intent != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("FloatingOverlay", "Error launching floating app window for $packageName", e)
            false
        }
    }

    /**
     * Replaces old floating badge with top-center System Dynamic Island Overlay Service.
     */
    fun showFloatingAssistantBubble(): Boolean {
        if (!canDrawOverlays()) {
            return false
        }
        return try {
            DynamicIslandOverlayService.start(context)
            _isOverlayShowing.value = true
            true
        } catch (e: Exception) {
            Log.e("FloatingOverlay", "Failed to launch Dynamic Island Overlay", e)
            false
        }
    }

    fun removeFloatingBubble() {
        try {
            DynamicIslandOverlayService.stop(context)
            _isOverlayShowing.value = false
        } catch (e: Exception) {
            Log.e("FloatingOverlay", "Failed to stop Dynamic Island Overlay", e)
        }
    }

    companion object {
        @Volatile
        private var instance: FloatingOverlayWindowManager? = null

        fun getInstance(context: Context): FloatingOverlayWindowManager {
            return instance ?: synchronized(this) {
                instance ?: FloatingOverlayWindowManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
