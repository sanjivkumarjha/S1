package com.example.appcontrol

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.service.AssistantAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Google Play Store Automated App Download & Installation Engine.
 * Executes automated Play Store searches and AccessibilityNodeInfo button clicks
 * for "Install", "Update", "Download", and permission prompts on com.android.vending.
 */
class PlayStoreAutomationInstaller(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _installationStatus = MutableStateFlow("Idle")
    val installationStatus: StateFlow<String> = _installationStatus.asStateFlow()

    fun downloadAndInstallApp(appName: String, onStatusUpdate: ((String) -> Unit)? = null) {
        scope.launch {
            _installationStatus.value = "🚀 Opening Play Store for '$appName'..."
            onStatusUpdate?.invoke(_installationStatus.value)

            val success = launchPlayStoreSearch(appName)
            if (!success) {
                _installationStatus.value = "❌ Play Store intent failed to launch."
                onStatusUpdate?.invoke(_installationStatus.value)
                return@launch
            }

            delay(2500) // Wait for Play Store UI to populate

            val service = AssistantAccessibilityService.getInstance()
            if (service == null) {
                _installationStatus.value = "⚠️ Accessibility Service inactive. Directing user to Play Store page."
                onStatusUpdate?.invoke(_installationStatus.value)
                return@launch
            }

            // Attempt automated node click for Install / Update / Download
            _installationStatus.value = "⚡ Auto-clicking 'Install' on Play Store..."
            onStatusUpdate?.invoke(_installationStatus.value)

            var clicked = false
            val installKeywords = listOf("Install", "Update", "Download", "Get", "स्थापित करें")

            for (attempt in 1..5) {
                if (AssistantAccessibilityService.currentPackageName.value == "com.android.vending") {
                    clicked = service.findAndClickAnyText(installKeywords)
                    if (clicked) {
                        _installationStatus.value = "✅ 'Install' clicked! Downloading $appName..."
                        onStatusUpdate?.invoke(_installationStatus.value)
                        break
                    }
                }
                delay(1500)
            }

            if (!clicked) {
                _installationStatus.value = "📲 Opened Play Store for '$appName'. Tap Install to proceed."
                onStatusUpdate?.invoke(_installationStatus.value)
            }
        }
    }

    private fun launchPlayStoreSearch(appName: String): Boolean {
        return try {
            val encoded = Uri.encode(appName)
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$encoded")).apply {
                setPackage("com.android.vending")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
            true
        } catch (e: Exception) {
            try {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/search?q=${Uri.encode(appName)}&c=apps")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                true
            } catch (ex: Exception) {
                Log.e("PlayStoreInstaller", "Failed to launch Play Store search", ex)
                false
            }
        }
    }
}
