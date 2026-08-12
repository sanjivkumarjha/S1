package com.example.ui.glass

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DynamicImpressionMode {
    MOOD_SLEEPING,  // 😴
    MOOD_THINKING,  // 🤔
    MOOD_TALKING,   // 😂
    MOOD_SAD,       // 🥺
    MOOD_LISTENING, // 👂
    ACTION_FLASHLIGHT, // 🔦
    ACTION_WHATSAPP,   // 💬
    ACTION_BROWSER,    // 🌐
    ACTION_PLAYSTORE,  // 🛍️
    ACTION_CALL        // 📞
}

data class DynamicIslandImpressionState(
    val mode: DynamicImpressionMode = DynamicImpressionMode.MOOD_SLEEPING,
    val emoji: String = "😴",
    val logoText: String = "Snaper AI",
    val statusText: String = "Sleeping...",
    val isAppLogoActive: Boolean = false
)

/**
 * Real-Time Dynamic Island Impression Emojis & Active App Logo Engine.
 * Syncs zero-delay with TTS speech output, Voice Recognition, Flashlight, WhatsApp,
 * Web Browser, Play Store, and Incoming Telecom Calls.
 */
object DynamicIslandImpressionController {

    private val _impressionState = MutableStateFlow(DynamicIslandImpressionState())
    val impressionState: StateFlow<DynamicIslandImpressionState> = _impressionState.asStateFlow()

    fun setSleeping() {
        _impressionState.value = DynamicIslandImpressionState(
            mode = DynamicImpressionMode.MOOD_SLEEPING,
            emoji = "😴",
            logoText = "Snaper AI",
            statusText = "Sleeping... Zzz",
            isAppLogoActive = false
        )
    }

    fun setThinking(prompt: String = "Analyzing Request...") {
        _impressionState.value = DynamicIslandImpressionState(
            mode = DynamicImpressionMode.MOOD_THINKING,
            emoji = "🤔",
            logoText = "AI Engine",
            statusText = prompt,
            isAppLogoActive = false
        )
    }

    fun setTalking(text: String = "Speaking...") {
        _impressionState.value = DynamicIslandImpressionState(
            mode = DynamicImpressionMode.MOOD_TALKING,
            emoji = "😂",
            logoText = "Snaper Voice",
            statusText = text.take(30),
            isAppLogoActive = false
        )
    }

    fun setSad(reason: String = "Task interrupted") {
        _impressionState.value = DynamicIslandImpressionState(
            mode = DynamicImpressionMode.MOOD_SAD,
            emoji = "🥺",
            logoText = "System Notice",
            statusText = reason,
            isAppLogoActive = false
        )
    }

    fun setListening() {
        _impressionState.value = DynamicIslandImpressionState(
            mode = DynamicImpressionMode.MOOD_LISTENING,
            emoji = "👂",
            logoText = "Voice Ear",
            statusText = "Listening to Boss...",
            isAppLogoActive = false
        )
    }

    fun setFlashlightActive(isOn: Boolean) {
        if (isOn) {
            _impressionState.value = DynamicIslandImpressionState(
                mode = DynamicImpressionMode.ACTION_FLASHLIGHT,
                emoji = "🔦",
                logoText = "Flashlight",
                statusText = "Torch Brightness ON",
                isAppLogoActive = true
            )
        } else {
            setSleeping()
        }
    }

    /**
     * Supports ANY 3950+ Unicode 15.0 Emoji string dynamically for custom AI states,
     * app actions, or system notifications.
     */
    fun setCustomEmoji(
        emoji: String,
        logoText: String = "Snaper AI",
        statusText: String = "Active",
        isAppLogoActive: Boolean = false
    ) {
        _impressionState.value = DynamicIslandImpressionState(
            mode = DynamicImpressionMode.MOOD_TALKING,
            emoji = emoji,
            logoText = logoText,
            statusText = statusText,
            isAppLogoActive = isAppLogoActive
        )
    }

    fun onForegroundPackageChanged(packageName: String) {
        when {
            packageName.contains("whatsapp", ignoreCase = true) -> {
                _impressionState.value = DynamicIslandImpressionState(
                    mode = DynamicImpressionMode.ACTION_WHATSAPP,
                    emoji = "💬",
                    logoText = "WhatsApp",
                    statusText = "WhatsApp Messaging",
                    isAppLogoActive = true
                )
            }
            packageName.contains("vending", ignoreCase = true) || packageName.contains("play", ignoreCase = true) -> {
                _impressionState.value = DynamicIslandImpressionState(
                    mode = DynamicImpressionMode.ACTION_PLAYSTORE,
                    emoji = "🛍️",
                    logoText = "Play Store",
                    statusText = "Google Play Store",
                    isAppLogoActive = true
                )
            }
            packageName.contains("chrome", ignoreCase = true) || packageName.contains("browser", ignoreCase = true) -> {
                _impressionState.value = DynamicIslandImpressionState(
                    mode = DynamicImpressionMode.ACTION_BROWSER,
                    emoji = "🌐",
                    logoText = "Web Browser",
                    statusText = "Browsing Web",
                    isAppLogoActive = true
                )
            }
        }
    }

    fun setIncomingCall(callerName: String) {
        _impressionState.value = DynamicIslandImpressionState(
            mode = DynamicImpressionMode.ACTION_CALL,
            emoji = "📞",
            logoText = "Incoming Call",
            statusText = "Screening: $callerName",
            isAppLogoActive = true
        )
    }
}
