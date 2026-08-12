package com.example.domain.app

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.example.appcontrol.AppRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URLEncoder

data class UniversalAppActionResult(
    val isSuccess: Boolean,
    val appName: String,
    val packageName: String,
    val actionType: String, // "SEARCH", "OPEN_CHAT", "OPEN_APP"
    val queryOrTarget: String,
    val responseText: String,
    val islandStatusText: String
)

class UniversalAppSearchManager private constructor(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val appRegistry = AppRegistry(context)

    private val _appActionState = MutableStateFlow("Ready")
    val appActionState: StateFlow<String> = _appActionState.asStateFlow()

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Auto-detects all installed launcher applications on the device dynamically.
     */
    fun detectInstalledApps(): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedList = packageManager.queryIntentActivities(mainIntent, 0)
            for (resolveInfo in resolvedList) {
                val label = resolveInfo.loadLabel(packageManager).toString()
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName.isNotBlank() && !list.any { it.second == pkgName }) {
                    list.add(Pair(label, pkgName))
                }
            }
            // Fallback if query returns empty list
            if (list.isEmpty()) {
                val installed = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                for (app in installed) {
                    val label = packageManager.getApplicationLabel(app).toString()
                    list.add(Pair(label, app.packageName))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun matchesUniversalSearchQuery(rawInput: String): Boolean {
        val lower = rawInput.lowercase().trim()
        val hasAppKeyword = lower.contains("instagram") ||
                lower.contains("facebook") ||
                lower.contains("youtube") ||
                lower.contains("x") || lower.contains("twitter") ||
                lower.contains("reddit") ||
                lower.contains("whatsapp") ||
                lower.contains("telegram") ||
                lower.contains("tiktok") ||
                lower.contains("pinterest") ||
                lower.contains("linkedin") ||
                lower.contains("chatgpt") ||
                lower.contains("grok") ||
                lower.contains("snapchat")

        val hasActionKeyword = lower.contains("search") ||
                lower.contains("खोलो") ||
                lower.contains("खोजो") ||
                lower.contains("वीडियो") ||
                lower.contains("video") ||
                lower.contains("chat") ||
                lower.contains("चैट") ||
                lower.contains("group") ||
                lower.contains("ग्रुप") ||
                lower.contains("पर") ||
                lower.contains("में")

        return hasAppKeyword && hasActionKeyword
    }

    suspend fun executeUniversalAction(rawInput: String): UniversalAppActionResult {
        val cleaned = rawInput.lowercase().trim()
        _appActionState.value = "🔎 Extracting Intent..."

        if (cleaned.contains("download") || cleaned.contains("install") || cleaned.contains("play store") || cleaned.contains("डाउनलोड")) {
            val playInstaller = com.example.appcontrol.PlayStoreAutomationInstaller(context)
            val appToInstall = extractQueryText(cleaned, "playstore").ifBlank { "WhatsApp" }
            playInstaller.downloadAndInstallApp(appToInstall)
            return UniversalAppActionResult(
                isSuccess = true,
                appName = "Play Store",
                packageName = "com.android.vending",
                actionType = "DOWNLOAD",
                queryOrTarget = appToInstall,
                responseText = "Play Store पर '$appToInstall' download/install प्रक्रिया चालू कर दी है! 🚀",
                islandStatusText = "⚡ Installing $appToInstall"
            )
        }

        if (cleaned.contains("lock phone") || cleaned.contains("फोन लॉक करो") || cleaned.contains("screen lock")) {
            val lockManager = com.example.security.KeyguardLockManager(context)
            val msg = lockManager.lockPhoneVoiceCommand()
            return UniversalAppActionResult(
                isSuccess = true,
                appName = "System Lock",
                packageName = "android",
                actionType = "LOCK_PHONE",
                queryOrTarget = "lock",
                responseText = msg,
                islandStatusText = "🔒 Device Secured"
            )
        }

        // 1. Identify target app name & target query
        val targetApp = detectTargetApp(cleaned)
        val targetQuery = extractQueryText(cleaned, targetApp)

        _appActionState.value = "📱 $targetApp"

        return when (targetApp.lowercase()) {
            "instagram" -> handleInstagramSearch(targetQuery)
            "facebook" -> handleFacebookSearch(targetQuery)
            "youtube" -> handleYouTubeSearch(targetQuery)
            "x", "twitter" -> handleTwitterSearch(targetQuery)
            "reddit" -> handleRedditSearch(targetQuery)
            "whatsapp" -> handleWhatsAppAction(targetQuery)
            "telegram" -> handleTelegramAction(targetQuery)
            "tiktok" -> handleTikTokSearch(targetQuery)
            "pinterest" -> handlePinterestSearch(targetQuery)
            "linkedin" -> handleLinkedInSearch(targetQuery)
            else -> handleGenericOrAutoDetectedApp(targetApp, targetQuery)
        }
    }

    private fun detectTargetApp(input: String): String {
        return when {
            input.contains("instagram") || input.contains("इंस्टाग्राम") -> "Instagram"
            input.contains("facebook") || input.contains("फेसबुक") -> "Facebook"
            input.contains("youtube") || input.contains("यूट्यूब") -> "YouTube"
            input.contains("twitter") || input.contains("x") || input.contains("ट्विटर") -> "X"
            input.contains("reddit") || input.contains("रेडिट") -> "Reddit"
            input.contains("whatsapp") || input.contains("व्हाट्सएप") || input.contains("व्हाट्सऐप") -> "WhatsApp"
            input.contains("telegram") || input.contains("टेलीग्राम") -> "Telegram"
            input.contains("tiktok") || input.contains("टिकटॉक") -> "TikTok"
            input.contains("pinterest") || input.contains("पिनटरेस्ट") -> "Pinterest"
            input.contains("linkedin") || input.contains("लिंक्डइन") -> "LinkedIn"
            input.contains("chatgpt") -> "ChatGPT"
            input.contains("grok") -> "Grok"
            else -> {
                // Auto-detect installed app label from text
                val installed = detectInstalledApps()
                val matched = installed.find { (label, _) -> input.contains(label.lowercase()) }
                matched?.first ?: "App"
            }
        }
    }

    private fun extractQueryText(input: String, appName: String): String {
        var text = input
            .replace(appName.lowercase(), "")
            .replace("instagram", "").replace("इंस्टाग्राम", "")
            .replace("facebook", "").replace("फेसबुक", "")
            .replace("youtube", "").replace("यूट्यूब", "")
            .replace("twitter", "").replace("x", "").replace("ट्विटर", "")
            .replace("reddit", "").replace("रेडिट", "")
            .replace("whatsapp", "").replace("व्हाट्सएप", "")
            .replace("telegram", "").replace("टेलीग्राम", "")
            .replace(" search करो", "").replace(" search", "")
            .replace("खोजो", "").replace("खोलो", "")
            .replace(" का वीडियो", "").replace(" video", "")
            .replace(" में ", " ").replace(" पर ", " ")
            .replace("की chat", "").replace("चैट", "")
            .trim()

        if (text.isEmpty()) text = "trending"
        return text
    }

    private fun handleInstagramSearch(query: String): UniversalAppActionResult {
        val pkg = "com.instagram.android"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val appUri = Uri.parse("https://www.instagram.com/search/top/?q=$encodedQuery")

        return try {
            val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                if (isAppInstalled(pkg)) setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            UniversalAppActionResult(
                isSuccess = true,
                appName = "Instagram",
                packageName = pkg,
                actionType = "SEARCH",
                queryOrTarget = query,
                responseText = "Instagram પર '$query' search कर दिया है! ✨",
                islandStatusText = "✅ Instagram search completed"
            )
        } catch (e: Exception) {
            fallbackWebSearch("Instagram", "https://www.instagram.com/search/top/?q=$encodedQuery", query)
        }
    }

    private fun handleFacebookSearch(query: String): UniversalAppActionResult {
        val pkg = "com.facebook.katana"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val appUri = Uri.parse("fb://search/top?q=$encodedQuery")

        return try {
            val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                if (isAppInstalled(pkg)) setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            UniversalAppActionResult(
                isSuccess = true,
                appName = "Facebook",
                packageName = pkg,
                actionType = "SEARCH",
                queryOrTarget = query,
                responseText = "Facebook पर '$query' search कर दिया है! ✨",
                islandStatusText = "✅ Facebook search completed"
            )
        } catch (e: Exception) {
            fallbackWebSearch("Facebook", "https://www.facebook.com/search/top/?q=$encodedQuery", query)
        }
    }

    private fun handleYouTubeSearch(query: String): UniversalAppActionResult {
        val result = appRegistry.searchYouTube(query)
        return UniversalAppActionResult(
            isSuccess = true,
            appName = "YouTube",
            packageName = "com.google.android.youtube",
            actionType = "SEARCH",
            queryOrTarget = query,
            responseText = "YouTube पर '$query' search कर दिया है! 🎬",
            islandStatusText = "✅ YouTube search completed"
        )
    }

    private fun handleTwitterSearch(query: String): UniversalAppActionResult {
        val pkg = "com.twitter.android"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val appUri = Uri.parse("https://x.com/search?q=$encodedQuery")

        return try {
            val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                if (isAppInstalled(pkg)) setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            UniversalAppActionResult(
                isSuccess = true,
                appName = "X",
                packageName = pkg,
                actionType = "SEARCH",
                queryOrTarget = query,
                responseText = "X / Twitter पर '$query' search कर दिया है! 🐦",
                islandStatusText = "✅ X search completed"
            )
        } catch (e: Exception) {
            fallbackWebSearch("X", "https://x.com/search?q=$encodedQuery", query)
        }
    }

    private fun handleRedditSearch(query: String): UniversalAppActionResult {
        val pkg = "com.reddit.frontpage"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val appUri = Uri.parse("https://www.reddit.com/search/?q=$encodedQuery")

        return try {
            val intent = Intent(Intent.ACTION_VIEW, appUri).apply {
                if (isAppInstalled(pkg)) setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            UniversalAppActionResult(
                isSuccess = true,
                appName = "Reddit",
                packageName = pkg,
                actionType = "SEARCH",
                queryOrTarget = query,
                responseText = "Reddit पर '$query' search कर दिया है! 🤖",
                islandStatusText = "✅ Reddit search completed"
            )
        } catch (e: Exception) {
            fallbackWebSearch("Reddit", "https://www.reddit.com/search/?q=$encodedQuery", query)
        }
    }

    private fun handleWhatsAppAction(target: String): UniversalAppActionResult {
        val pkg = "com.whatsapp"
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                UniversalAppActionResult(
                    isSuccess = true,
                    appName = "WhatsApp",
                    packageName = pkg,
                    actionType = "OPEN_CHAT",
                    queryOrTarget = target,
                    responseText = "WhatsApp open कर दिया है। '$target' search कर सकते हैं! 💬",
                    islandStatusText = "✅ Opened WhatsApp chat"
                )
            } else {
                UniversalAppActionResult(
                    isSuccess = false,
                    appName = "WhatsApp",
                    packageName = pkg,
                    actionType = "OPEN_CHAT",
                    queryOrTarget = target,
                    responseText = "WhatsApp phone में installed नहीं है।",
                    islandStatusText = "⚠️ WhatsApp not installed"
                )
            }
        } catch (e: Exception) {
            UniversalAppActionResult(
                isSuccess = false,
                appName = "WhatsApp",
                packageName = pkg,
                actionType = "OPEN_CHAT",
                queryOrTarget = target,
                responseText = "WhatsApp opening failed: ${e.message}",
                islandStatusText = "❌ WhatsApp launch error"
            )
        }
    }

    private fun handleTelegramAction(target: String): UniversalAppActionResult {
        val pkg = "org.telegram.messenger"
        val encodedQuery = URLEncoder.encode(target, "UTF-8")
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$encodedQuery")).apply {
                if (isAppInstalled(pkg)) setPackage(pkg)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            UniversalAppActionResult(
                isSuccess = true,
                appName = "Telegram",
                packageName = pkg,
                actionType = "SEARCH",
                queryOrTarget = target,
                responseText = "Telegram में '$target' open/search कर दिया है! ✈️",
                islandStatusText = "✅ Telegram action completed"
            )
        } catch (e: Exception) {
            fallbackWebSearch("Telegram", "https://t.me/$encodedQuery", target)
        }
    }

    private fun handleTikTokSearch(query: String): UniversalAppActionResult {
        val pkg = "com.zhiliaoapp.musically"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        return fallbackWebSearch("TikTok", "https://www.tiktok.com/search?q=$encodedQuery", query)
    }

    private fun handlePinterestSearch(query: String): UniversalAppActionResult {
        val pkg = "com.pinterest"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        return fallbackWebSearch("Pinterest", "https://www.pinterest.com/search/pins/?q=$encodedQuery", query)
    }

    private fun handleLinkedInSearch(query: String): UniversalAppActionResult {
        val pkg = "com.linkedin.android"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        return fallbackWebSearch("LinkedIn", "https://www.linkedin.com/search/results/all/?keywords=$encodedQuery", query)
    }

    private fun handleGenericOrAutoDetectedApp(appName: String, query: String): UniversalAppActionResult {
        val installedList = detectInstalledApps()
        val matched = installedList.find { it.first.lowercase().contains(appName.lowercase()) }

        return if (matched != null) {
            val pkg = matched.second
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    UniversalAppActionResult(
                        isSuccess = true,
                        appName = matched.first,
                        packageName = pkg,
                        actionType = "OPEN_APP",
                        queryOrTarget = query,
                        responseText = "${matched.first} open कर दिया है! ✨",
                        islandStatusText = "✅ Opened ${matched.first}"
                    )
                } else {
                    fallbackWebSearch(appName, "https://www.google.com/search?q=${URLEncoder.encode("$appName $query", "UTF-8")}", query)
                }
            } catch (e: Exception) {
                fallbackWebSearch(appName, "https://www.google.com/search?q=${URLEncoder.encode("$appName $query", "UTF-8")}", query)
            }
        } else {
            fallbackWebSearch(appName, "https://www.google.com/search?q=${URLEncoder.encode("$appName $query", "UTF-8")}", query)
        }
    }

    private fun fallbackWebSearch(appName: String, webUrl: String, query: String): UniversalAppActionResult {
        return try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            UniversalAppActionResult(
                isSuccess = true,
                appName = appName,
                packageName = "browser",
                actionType = "SEARCH",
                queryOrTarget = query,
                responseText = "$appName '$query' web search open कर दिया है! 🌐",
                islandStatusText = "✅ $appName web search opened"
            )
        } catch (e: Exception) {
            UniversalAppActionResult(
                isSuccess = false,
                appName = appName,
                packageName = "browser",
                actionType = "SEARCH",
                queryOrTarget = query,
                responseText = "Action failed: ${e.message}",
                islandStatusText = "❌ $appName search failed"
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UniversalAppSearchManager? = null

        fun getInstance(context: Context): UniversalAppSearchManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UniversalAppSearchManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
