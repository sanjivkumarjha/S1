package com.example.domain

import android.content.Context
import com.example.avatar.AgentAvatarManager
import com.example.communication.RealTimeLanguageAssistant
import com.example.communication.UniversalCommunicationManager
import com.example.data.api.AiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.preferences.UserSettings
import com.example.security.SecurityCameraMode
import com.example.security.SecurityManager
import com.example.security.SecurityMode
import com.example.security.SecureDeviceAuthManager
import com.example.security.ThreatDetectionEngine

/**
 * Central Brain Orchestrator Pipeline for Snaper Technology.
 * Coordinates Context Clock, Radhe Radhe Greeting Engine, Universal Threat Protection,
 * Smart Home, Media Control, Automation Engine, Multi-channel Communication, Avatar System, Call Summaries, and AI Models.
 */
class AssistantOrchestrator(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val securityManager = SecurityManager(context)
    private val securityCameraMode = SecurityCameraMode(context)
    private val mediaControlManager = MediaControlManager(context)
    private val smartHomeManager = SmartHomeManager(context)
    private val smartSceneManager = SmartSceneManager(context)
    private val irRemoteManager = IRRemoteManager(context)
    private val weatherManager = WeatherAutomationManager(context)
    private val offlineCommandHandler = OfflineCommandHandler(context)
    private val aiModelRouter = AiModelRouter(context)
    val toolExecutor = com.example.agent.ToolExecutor(context)
    val secureDeviceAuthManager = SecureDeviceAuthManager(context)

    // Upgraded Engines
    val threatDetectionEngine = ThreatDetectionEngine(context)
    val avatarManager = AgentAvatarManager(context)
    val languageAssistant = RealTimeLanguageAssistant(context)
    val communicationManager = UniversalCommunicationManager(context)

    suspend fun processQuery(
        query: String,
        userId: String = "owner",
        history: List<ChatMessageEntity> = emptyList(),
        userSettings: UserSettings
    ): String {
        val trimmedQuery = query.trim()
        val ownerTitle = if (userSettings.ownerTitle.isNotBlank()) userSettings.ownerTitle else "Boss"
        val lower = trimmedQuery.lowercase()

        // 0. Unlock / Lock Phone / Screen Intent
        if (secureDeviceAuthManager.isUnlockCommand(trimmedQuery)) {
            if (!userSettings.isScreenUnlockEnabled) {
                return "राधे राधे ${userSettings.ownerName}! Screen Unlock Assistant feature is currently disabled in your app Settings. You can enable it under Settings -> Security."
            }
            return secureDeviceAuthManager.getUnlockExplanationMessage(userSettings.ownerName)
        }

        if (secureDeviceAuthManager.isLockCommand(trimmedQuery)) {
            if (!userSettings.isScreenUnlockEnabled) {
                return "राधे राधे ${userSettings.ownerName}! Screen Lock Assistant feature is currently disabled in your app Settings. You can enable it under Settings -> Security."
            }
            return secureDeviceAuthManager.getLockExplanationMessage(userSettings.ownerName)
        }

        // 1. Security Check & Restricted Mode
        if (trimmedQuery.contains("gallery", ignoreCase = true) || trimmedQuery.contains("photos", ignoreCase = true)) {
            val isRestricted = userSettings.securityMode == SecurityMode.RESTRICTED.name
            if (isRestricted) {
                val warning = securityCameraMode.getSecurityWarningText("Gallery", ownerTitle)
                return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, warning)
            }
        }

        // 2. Avatar Intent Routing ("avatar बदलो", "change avatar")
        if (lower.contains("avatar") || lower.contains("अवतार")) {
            val avatarMsg = avatarManager.selectNextAvatar()
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, avatarMsg)
        }

        // 3. Spam / Scam / Security Threat Evaluation
        if (lower.contains("spam") || lower.contains("scam") || lower.contains("phishing") ||
            lower.contains("http://") || lower.contains("https://") || lower.contains("check link") ||
            lower.contains("check call") || lower.contains("check message")) {
            val threatResult = threatDetectionEngine.evaluateInput(trimmedQuery)
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, threatResult.ownerMessage)
        }

        // 4. Media / Song Intent
        if (lower.contains("song") || lower.contains("गाना") || lower.contains("music") || lower.contains("youtube पर")) {
            val mediaResponse = mediaControlManager.processMediaCommand(trimmedQuery)
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, mediaResponse)
        }

        // 5. Smart Home & Scene Intent
        if (lower.contains("ac") || lower.contains("tv") || lower.contains("light") || lower.contains("fan") ||
            lower.contains("lights") || lower.contains("scene") || lower.contains("mode") || lower.contains("remote")) {

            when {
                lower.contains("night mode") || lower.contains("good night") || lower.contains("sleep mode") -> {
                    val sceneRes = smartSceneManager.executeScene("Good Night", emptyList())
                    return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, sceneRes)
                }
                lower.contains("good morning") || lower.contains("morning mode") -> {
                    val sceneRes = smartSceneManager.executeScene("Good Morning", emptyList())
                    return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, sceneRes)
                }
                lower.contains("movie mode") -> {
                    val sceneRes = smartSceneManager.executeScene("Movie Mode", emptyList())
                    return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, sceneRes)
                }
                lower.contains("leaving home") -> {
                    val sceneRes = smartSceneManager.executeScene("Leaving Home", emptyList())
                    return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, sceneRes)
                }
                lower.contains("ir") || lower.contains("remote") -> {
                    val irRes = if (irRemoteManager.hasIREmitter()) {
                        "IR Signal sent to device."
                    } else {
                        "No IR hardware on device. Operating via local Wi-Fi / Bluetooth smart connection."
                    }
                    return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, irRes)
                }
                else -> {
                    // Honest smart-home handling: only toggle a device that actually exists in the
                    // user's configured device list. Never fake an "action processed" success for
                    // hardware that is not configured/connected.
                    val devices = try { smartHomeManager.devicesState.value } catch (e: Exception) { emptyList() }
                    val matched = devices.firstOrNull { dev ->
                        lower.contains(dev.deviceName.lowercase()) || dev.deviceName.lowercase().let { lower.contains(it) }
                    }
                    val homeRes = when {
                        matched == null && devices.isEmpty() -> "I don't have any smart-home devices configured yet, $ownerTitle. Add your lights/switches/plugs in Smart Home settings, and I'll control them by name."
                        matched == null -> "I couldn't find a device matching your request, $ownerTitle. Your configured devices are: ${devices.joinToString { it.deviceName }}. Please name the device you want to control."
                        else -> smartHomeManager.togglePower(matched)
                    }
                    return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, homeRes)
                }
            }
        }

        // 6. Time Intent (Including Seconds)
        if (lower.contains("time") || lower.contains("टाइम") || lower.contains("समय") || lower.contains("clock")) {
            val name = if (userSettings.ownerName.isNotBlank() && userSettings.ownerName != "User") userSettings.ownerName else "संजिव सर"
            val timeText = "राधे राधे $name Sir. " + GlobalTimeManager.getCurrentTimeHindiExplanation()
            return timeText
        }

        // 6b. Weather / Automation Intent
        if (lower.contains("weather") || lower.contains("मौसम") || lower.contains("temperature") || lower.contains("तापमान")) {
            val weather = weatherManager.getCurrentWeather()
            val weatherRes = "Current Weather: ${weather.temperatureCelsius}°C (${weather.condition}), Humidity ${weather.humidityPercent}%. Smart cooling is active."
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, weatherRes)
        }

        // 7. Offline Direct Command Handler
        val offlineResult = offlineCommandHandler.handleCommand(trimmedQuery)
        if (offlineResult.isHandled) {
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, offlineResult.responseText)
        }

        // 7b. Agent Tool Execution Engine
        if (lower.startsWith("open ") || lower.startsWith("launch ") || lower.contains("खोल ") || lower.contains("चालू करो ")) {
            val appTarget = trimmedQuery
                .replace("open ", "", ignoreCase = true)
                .replace("launch ", "", ignoreCase = true)
                .replace("खोल", "", ignoreCase = true)
                .replace("चालू करो", "", ignoreCase = true)
                .trim()
            if (appTarget.isNotBlank()) {
                val toolResult = toolExecutor.executeTool("open_app", mapOf("appName" to appTarget))
                return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, toolResult.message)
            }
        }

        if (lower.contains("device care") || lower.contains("device health") || lower.contains("battery status") || lower.contains("storage check") || lower.contains("phone status")) {
            val toolResult = toolExecutor.executeTool("device_care", emptyMap())
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, toolResult.message)
        }

        if (lower.contains("remember that ") || lower.contains("याद रखो ")) {
            val fact = trimmedQuery.substringAfter("remember that ").substringAfter("याद रखो ").trim()
            if (fact.isNotBlank()) {
                val toolResult = toolExecutor.executeTool("memory_save", mapOf("key" to "User Fact", "value" to fact))
                return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, toolResult.message)
            }
        }

        if (lower.contains("search memory") || lower.contains("what do you remember") || lower.contains("याद है")) {
            val searchQuery = trimmedQuery.substringAfter("about ").substringAfter("memory ").trim()
            val toolResult = toolExecutor.executeTool("memory_search", mapOf("query" to searchQuery))
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, toolResult.message)
        }

        if (lower.contains("remind me to ") || lower.contains("याद दिलाना ")) {
            val reminderTitle = trimmedQuery.substringAfter("remind me to ").substringAfter("याद दिलाना ").trim()
            val toolResult = toolExecutor.executeTool("reminder_create", mapOf("title" to reminderTitle))
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, toolResult.message)
        }

        if (lower.contains("read screen") || lower.contains("स्क्रीन पढ़ो")) {
            val toolResult = toolExecutor.executeTool("read_screen", emptyMap())
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, toolResult.message)
        }

        if (lower.contains("fact check ") || lower.contains("factcheck ")) {
            val claim = trimmedQuery.substringAfter("check ").trim()
            val toolResult = toolExecutor.executeTool("fact_check_claim", mapOf("claim" to claim))
            return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, toolResult.message)
        }

        // 8. General AI Query through AI Model Router
        val rawAiResponse = aiModelRouter.processQuery(
            query = trimmedQuery,
            history = history,
            memories = emptyList(),
            userSettings = userSettings
        )

        // 9. Process Permanent "राधे राधे" Greeting Engine
        return RadheRadheGreetingManager.processGreeting(userId, ownerTitle, rawAiResponse)
    }
}
