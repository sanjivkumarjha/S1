package com.example.domain.app

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

data class AppInfo(
    val packageName: String,
    val displayName: String,
    val isSystemApp: Boolean,
    val iconDrawable: Drawable? = null,
    val categoryLabel: String = "Application"
)

object AppRegistry {
    private val cachedApps = mutableListOf<AppInfo>()

    fun updateApps(apps: List<AppInfo>) {
        synchronized(cachedApps) {
            cachedApps.clear()
            cachedApps.addAll(apps)
        }
    }

    fun getInstalledApps(): List<AppInfo> {
        synchronized(cachedApps) {
            return cachedApps.toList()
        }
    }

    fun findApp(query: String): AppInfo? {
        synchronized(cachedApps) {
            val q = query.lowercase().trim()
            return cachedApps.firstOrNull { 
                it.displayName.lowercase().contains(q) || 
                it.packageName.lowercase().contains(q)
            }
        }
    }
}

class AppDiscoveryManager(private val context: Context) {

    fun refreshInstalledApps(): List<AppInfo> {
        val pm = context.packageManager
        val appsList = mutableListOf<AppInfo>()
        val addedPackages = mutableSetOf<String>()

        try {
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(intent, 0)

            for (ri in resolveInfos) {
                val pkg = ri.activityInfo.packageName
                if (!addedPackages.contains(pkg)) {
                    addedPackages.add(pkg)
                    val label = ri.loadLabel(pm).toString()
                    val isSystem = (ri.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val icon = try { ri.loadIcon(pm) } catch (e: Exception) { null }

                    appsList.add(
                        AppInfo(
                            packageName = pkg,
                            displayName = label,
                            isSystemApp = isSystem,
                            iconDrawable = icon,
                            categoryLabel = if (isSystem) "System App" else "Installed App"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        AppRegistry.updateApps(appsList)
        return appsList
    }

    fun launchAppOrPromptUnlock(
        packageName: String,
        isDeviceLocked: Boolean,
        onRequireUnlock: (String) -> Unit,
        onSuccessLaunch: () -> Unit
    ) {
        val pm = context.packageManager
        val appInfo = AppRegistry.findApp(packageName)
        val appLabel = appInfo?.displayName ?: packageName

        if (isDeviceLocked) {
            onRequireUnlock(appLabel)
            return
        }

        val launchIntent = pm.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            onSuccessLaunch()
        } else {
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(marketIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
