package com.example.appcontrol

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppAliasEntity

data class AppInfo(
    val packageName: String,
    val applicationLabel: String,
    val normalizedName: String,
    val aliases: List<String> = emptyList(),
    val isSystemApp: Boolean = false,
    val isUserApp: Boolean = true,
    val versionName: String = "28.1.1",
    val versionCode: Long = 28
)

sealed class AppLaunchResult {
    data class Success(val appInfo: AppInfo, val message: String) : AppLaunchResult()
    data class NotInstalled(val requestedApp: String, val message: String) : AppLaunchResult()
    data class Error(val requestedApp: String, val message: String) : AppLaunchResult()
}

class AppRegistry(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val appAliasDao by lazy { AppDatabase.getDatabase(context).appAliasDao() }

    /**
     * Scans installed applications on device.
     */
    suspend fun getInstalledApps(): List<AppInfo> {
        val appList = mutableListOf<AppInfo>()
        val customAliases = appAliasDao.getAllAliasesList()
        val aliasMap = customAliases.groupBy { it.packageName }

        try {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            for (app in installedApps) {
                // Filter out apps that don't have a launch intent unless system apps
                val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent == null && (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                    continue
                }

                val label = packageManager.getApplicationLabel(app).toString()
                val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val aliasesForApp = aliasMap[app.packageName]?.map { it.aliasName } ?: emptyList()

                appList.add(
                    AppInfo(
                        packageName = app.packageName,
                        applicationLabel = label,
                        normalizedName = label.lowercase().trim(),
                        aliases = aliasesForApp,
                        isSystemApp = isSystem,
                        isUserApp = !isSystem
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return appList
    }

    /**
     * Resolves app and launches exact package.
     */
    suspend fun launchAppByName(queryName: String): AppLaunchResult {
        val cleaned = queryName.lowercase()
            .replace("खोलो", "")
            .replace("open", "")
            .replace("app", "")
            .replace("एप्लिकेशन", "")
            .replace("ऐप", "")
            .trim()

        val isWhatsAppBusinessRequested = cleaned.contains("business") || cleaned.contains("बिजनेस") || cleaned.contains("कंपनी")
        val isWhatsAppRequested = cleaned.contains("whatsapp") || cleaned.contains("व्हाट्सऐप") || cleaned.contains("व्हाट्सएप")

        // 1. Strict WhatsApp vs WhatsApp Business handling
        if (isWhatsAppRequested) {
            if (isWhatsAppBusinessRequested) {
                // Target WhatsApp Business explicitly: com.whatsapp.w4b
                val businessPkg = "com.whatsapp.w4b"
                if (isPackageInstalled(businessPkg)) {
                    val intent = packageManager.getLaunchIntentForPackage(businessPkg)
                    return if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        AppLaunchResult.Success(
                            appInfo = AppInfo(businessPkg, "WhatsApp Business", "whatsapp business"),
                            message = "Opening WhatsApp Business ✨"
                        )
                    } else {
                        AppLaunchResult.Error("WhatsApp Business", "Boss, WhatsApp Business launch intent failed.")
                    }
                } else {
                    return AppLaunchResult.NotInstalled("WhatsApp Business", "Boss, WhatsApp Business installed नहीं है।")
                }
            } else {
                // Target Normal WhatsApp explicitly: com.whatsapp
                val normalPkg = "com.whatsapp"
                if (isPackageInstalled(normalPkg)) {
                    val intent = packageManager.getLaunchIntentForPackage(normalPkg)
                    return if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        AppLaunchResult.Success(
                            appInfo = AppInfo(normalPkg, "WhatsApp", "whatsapp"),
                            message = "Opening WhatsApp ✨"
                        )
                    } else {
                        AppLaunchResult.Error("WhatsApp", "Boss, WhatsApp launch intent failed.")
                    }
                } else {
                    return AppLaunchResult.NotInstalled("WhatsApp", "Boss, WhatsApp installed नहीं है।")
                }
            }
        }

        // 2. Check System App Shortcuts (Settings, Camera, YouTube, Gallery, etc.)
        val systemResult = tryLaunchSystemAppShortcut(cleaned)
        if (systemResult != null) {
            return systemResult
        }

        // 3. Scan Installed Apps & Match Aliases
        val allApps = getInstalledApps()

        // Match exact alias
        val matchedAliasPkg = appAliasDao.getPackageByAlias("%$cleaned%")
        if (matchedAliasPkg != null) {
            val appInfo = allApps.find { it.packageName == matchedAliasPkg }
            if (appInfo != null) {
                val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return AppLaunchResult.Success(appInfo, "Opening ${appInfo.applicationLabel} ✨")
                }
            }
        }

        // Exact or fuzzy match label
        val matchedApp = allApps.find { app ->
            app.normalizedName == cleaned ||
            app.normalizedName.contains(cleaned) ||
            app.aliases.any { it.lowercase().contains(cleaned) }
        }

        if (matchedApp != null) {
            val intent = packageManager.getLaunchIntentForPackage(matchedApp.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return AppLaunchResult.Success(matchedApp, "Opening ${matchedApp.applicationLabel} ✨")
            }
        }

        return AppLaunchResult.NotInstalled(cleaned, "Boss, '$cleaned' app installed नहीं है या find नहीं हुआ।")
    }

    /**
     * Try system app intents (Settings, Camera, Calculator, Clock, YouTube, Chrome, etc.)
     */
    private fun tryLaunchSystemAppShortcut(queryName: String): AppLaunchResult? {
        val actionIntent: Intent? = when {
            queryName.contains("settings") || queryName.contains("सेटिंग्स") || queryName.contains("setting") ->
                Intent(Settings.ACTION_SETTINGS)
            queryName.contains("wifi") || queryName.contains("वाईफाई") ->
                Intent(Settings.ACTION_WIFI_SETTINGS)
            queryName.contains("bluetooth") || queryName.contains("ब्लूटूथ") ->
                Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            queryName.contains("display") || queryName.contains("डिस्प्ले") ->
                Intent(Settings.ACTION_DISPLAY_SETTINGS)
            queryName.contains("sound") || queryName.contains("साउंड") ->
                Intent(Settings.ACTION_SOUND_SETTINGS)
            queryName.contains("battery") || queryName.contains("बैटरी") ->
                Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            queryName.contains("accessibility") || queryName.contains("एक्सेसिबिलिटी") ->
                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            queryName.contains("camera") || queryName.contains("कैमरा") ->
                Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
            queryName.contains("dialer") || queryName.contains("phone") || queryName.contains("फोन") ->
                Intent(Intent.ACTION_DIAL)
            queryName.contains("contact") || queryName.contains("कॉन्टेक्ट") ->
                Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
            queryName.contains("clock") || queryName.contains("alarm") || queryName.contains("घड़ी") || queryName.contains("अलार्म") ->
                Intent(AlarmClock.ACTION_SHOW_ALARMS)
            queryName.contains("calendar") || queryName.contains("कैलेण्डर") ->
                Intent(Intent.ACTION_VIEW, CalendarContract.CONTENT_URI)
            queryName.contains("browser") || queryName.contains("chrome") || queryName.contains("क्रोम") ->
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            else -> null
        }

        if (actionIntent != null) {
            try {
                actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(actionIntent)
                return AppLaunchResult.Success(
                    appInfo = AppInfo("system.intent", queryName.capitalize(), queryName, isSystemApp = true),
                    message = "Opening $queryName ✨"
                )
            } catch (e: Exception) {
                return AppLaunchResult.Error(queryName, "Could not launch system component: ${e.message}")
            }
        }
        return null
    }

    /**
     * Checks if a package is installed.
     */
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private val socialManager by lazy { SocialMediaAutomationManager(context) }

    /**
     * Universal social media deep search helper
     */
    fun searchSocialPlatform(platformName: String, query: String): AppLaunchResult {
        val platform = SocialPlatform.values().find {
            it.id.equals(platformName, ignoreCase = true) ||
            it.displayName.contains(platformName, ignoreCase = true) ||
            it.packageName.contains(platformName, ignoreCase = true)
        } ?: SocialPlatform.YOUTUBE

        val result = socialManager.searchPlatform(platform, query)
        return if (result.success) {
            AppLaunchResult.Success(
                AppInfo(platform.packageName, platform.displayName, platform.id),
                result.message
            )
        } else {
            AppLaunchResult.Error(platform.displayName, result.message)
        }
    }

    /**
     * YouTube search intent helper
     */
    fun searchYouTube(query: String): AppLaunchResult {
        return searchSocialPlatform("youtube", query)
    }
}
