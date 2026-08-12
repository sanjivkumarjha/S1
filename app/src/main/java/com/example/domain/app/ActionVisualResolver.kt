package com.example.domain.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

data class ActionVisualResult(
    val emoji: String,
    val actionTitle: String,
    val statusText: String,
    val iconVector: ImageVector,
    val targetPackage: String? = null,
    val isAppLaunch: Boolean = false
)

object ActionVisualResolver {

    fun resolveAction(command: String): ActionVisualResult {
        val lower = command.lowercase().trim()

        return when {
            lower.contains("flashlight") || lower.contains("torch") -> {
                ActionVisualResult(
                    emoji = "🔦",
                    actionTitle = "Flashlight Toggle",
                    statusText = "Turning on Flashlight...",
                    iconVector = Icons.Default.FlashlightOn
                )
            }
            lower.contains("camera") || lower.contains("photo") -> {
                ActionVisualResult(
                    emoji = "📷",
                    actionTitle = "Camera Vision",
                    statusText = "Opening Camera...",
                    iconVector = Icons.Default.CameraAlt,
                    targetPackage = "com.android.camera",
                    isAppLaunch = true
                )
            }
            lower.contains("whatsapp") -> {
                ActionVisualResult(
                    emoji = "💬",
                    actionTitle = "WhatsApp",
                    statusText = "Opening WhatsApp...",
                    iconVector = Icons.Default.Chat,
                    targetPackage = "com.whatsapp",
                    isAppLaunch = true
                )
            }
            lower.contains("grok") || lower.contains("xai") -> {
                ActionVisualResult(
                    emoji = "🚀",
                    actionTitle = "xAI Grok",
                    statusText = "Launching Grok...",
                    iconVector = Icons.Default.SmartToy,
                    targetPackage = "com.x.grok",
                    isAppLaunch = true
                )
            }
            lower.contains("chatgpt") || lower.contains("openai") -> {
                ActionVisualResult(
                    emoji = "🤖",
                    actionTitle = "ChatGPT Assistant",
                    statusText = "Launching ChatGPT...",
                    iconVector = Icons.Default.SmartToy,
                    targetPackage = "com.openai.chatgpt",
                    isAppLaunch = true
                )
            }
            lower.contains("music") || lower.contains("song") || lower.contains("spotify") -> {
                ActionVisualResult(
                    emoji = "🎵",
                    actionTitle = "Music Audio",
                    statusText = "Opening Music Player...",
                    iconVector = Icons.Default.MusicNote,
                    isAppLaunch = true
                )
            }
            lower.contains("call") || lower.contains("phone") || lower.contains("dial") -> {
                ActionVisualResult(
                    emoji = "📞",
                    actionTitle = "Phone Dialer",
                    statusText = "Opening Dialer...",
                    iconVector = Icons.Default.Phone,
                    isAppLaunch = true
                )
            }
            lower.contains("map") || lower.contains("navigation") || lower.contains("gps") -> {
                ActionVisualResult(
                    emoji = "🗺️",
                    actionTitle = "Maps & Navigation",
                    statusText = "Opening Maps...",
                    iconVector = Icons.Default.Map,
                    isAppLaunch = true
                )
            }
            lower.contains("alarm") || lower.contains("timer") || lower.contains("clock") -> {
                ActionVisualResult(
                    emoji = "⏱️",
                    actionTitle = "Clock & Timer",
                    statusText = "Setting Alarm...",
                    iconVector = Icons.Default.Timer
                )
            }
            lower.contains("setting") || lower.contains("config") -> {
                ActionVisualResult(
                    emoji = "⚙️",
                    actionTitle = "System Settings",
                    statusText = "Opening Settings...",
                    iconVector = Icons.Default.Settings,
                    isAppLaunch = true
                )
            }
            lower.contains("open ") || lower.contains("launch ") -> {
                val appName = lower.replace("open ", "").replace("launch ", "").trim()
                val matchedApp = AppRegistry.findApp(appName)
                ActionVisualResult(
                    emoji = "📱",
                    actionTitle = matchedApp?.displayName ?: appName.replaceFirstChar { it.uppercase() },
                    statusText = "Opening ${matchedApp?.displayName ?: appName}...",
                    iconVector = Icons.Default.Star,
                    targetPackage = matchedApp?.packageName,
                    isAppLaunch = true
                )
            }
            else -> {
                ActionVisualResult(
                    emoji = "✨",
                    actionTitle = "Assistant Action",
                    statusText = "Processing request...",
                    iconVector = Icons.Default.Star
                )
            }
        }
    }
}
