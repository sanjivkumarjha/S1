package com.example.service

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.R
import com.example.data.preferences.UserPreferencesRepository
import com.example.ui.glass.DynamicIslandState
import com.example.ui.glass.GlossyDynamicIsland
import com.example.ui.theme.SnaperTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Dynamic Island Overlay Service for Always-On Display (AOD) & Keyguard Parity.
 * Renders an interactive, floating AMOLED-optimized Dynamic Island overlay over lock screen,
 * keyguard, and system screens using TYPE_APPLICATION_OVERLAY and FLAG_SHOW_WHEN_LOCKED.
 */
class DynamicIslandOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null
    private var islandState by mutableStateOf(DynamicIslandState.COLLAPSED)

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d("DynamicIslandService", "Screen OFF -> Activating Dynamic Island AOD Mode")
                    islandState = DynamicIslandState.DYNAMIC_ISLAND_AOD
                }
                Intent.ACTION_SCREEN_ON -> {
                    Log.d("DynamicIslandService", "Screen ON -> Updating Dynamic Island Keyguard View")
                    if (islandState == DynamicIslandState.DYNAMIC_ISLAND_AOD) {
                        islandState = DynamicIslandState.COLLAPSED
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    Log.d("DynamicIslandService", "Device Unlocked")
                    islandState = DynamicIslandState.COLLAPSED
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        createNotificationChannel()
        val notification = createNotification()
        // Manifest declares this service as specialUse; on Android 14+ we must call the typed
        // startForeground so the system accepts the foreground service type.
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

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        try {
            registerReceiver(screenReceiver, filter)
        } catch (e: Exception) {
            Log.e("DynamicIslandService", "Error registering screen receiver: ${e.message}")
        }

        setupFloatingOverlay()
    }

    private fun setupFloatingOverlay() {
        // Overlay drawing requires SYSTEM_ALERT_WINDOW. If not granted, do not attempt addView
        // (it would throw BadTokenException and crash the service).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !android.provider.Settings.canDrawOverlays(this)) {
            Log.w("DynamicIslandService", "SYSTEM_ALERT_WINDOW not granted; overlay not attached.")
            return
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 20
        }

        val prefsRepo = UserPreferencesRepository(applicationContext)

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@DynamicIslandOverlayService)
            setViewTreeViewModelStoreOwner(this@DynamicIslandOverlayService)
            setViewTreeSavedStateRegistryOwner(this@DynamicIslandOverlayService)

            setContent {
                val userSettings by prefsRepo.userSettingsFlow.collectAsState(initial = com.example.data.preferences.UserSettings())
                SnaperTheme.SnaperTheme(themeMode = userSettings.themeMode, accentColorHex = userSettings.accentColorHex) {
                    GlossyDynamicIsland.GlossyDynamicIsland(
                        userSettings = userSettings,
                        islandState = islandState,
                        actionResult = null,
                        isDeviceLocked = (islandState == DynamicIslandState.DYNAMIC_ISLAND_AOD),
                        onExpandToggle = {
                            islandState = if (islandState == DynamicIslandState.COLLAPSED) DynamicIslandState.EXPANDED else DynamicIslandState.COLLAPSED
                        },
                        onUnlockRequest = { _ -> }
                    )
                }
            }
        }

        try {
            windowManager?.addView(overlayView, layoutParams)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        } catch (e: Exception) {
            Log.e("DynamicIslandService", "Error attaching overlay window: ${e.message}")
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dynamic Island Active")
            .setContentText("AMOLED AOD & Lockscreen Dynamic Island Running")
            .setSmallIcon(R.drawable.ic_notification_small)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        com.example.util.NotificationHelper.createChannel(
            this,
            CHANNEL_ID,
            "Dynamic Island AOD Channel",
            importance = android.app.NotificationManager.IMPORTANCE_LOW
        )
    }

    override fun onDestroy() {
        serviceScope.cancel()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            Log.e("DynamicIslandService", "Error unregistering receiver: ${e.message}")
        }
        try {
            if (overlayView != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
            }
        } catch (e: Exception) {
            Log.e("DynamicIslandService", "Error destroying overlay: ${e.message}")
        }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "dynamic_island_aod_channel"
        const val NOTIFICATION_ID = 1002

        fun start(context: Context) {
            val intent = Intent(context, DynamicIslandOverlayService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.w("DynamicIslandService", "start blocked: ${e.message}")
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, DynamicIslandOverlayService::class.java)
            context.stopService(intent)
        }
    }
}
