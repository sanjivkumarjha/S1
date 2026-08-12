package com.example.agent

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.appcontrol.AppLaunchResult
import com.example.appcontrol.AppRegistry
import com.example.data.local.AppDatabase
import com.example.data.local.entities.MemoryEntity
import com.example.data.local.entities.ReminderEntity
import com.example.devicecare.DeviceCareManager
import com.example.service.AssistantAccessibilityService
import kotlinx.coroutines.flow.firstOrNull

data class ToolResult(
    val success: Boolean,
    val action: String,
    val message: String,
    val data: String? = null,
    val errorCode: String? = null
)

class ToolExecutor(private val context: Context) {

    private val appRegistry by lazy { AppRegistry(context) }
    private val deviceCareManager by lazy { DeviceCareManager(context) }
    private val memoryDao by lazy { AppDatabase.getDatabase(context).memoryDao() }
    private val reminderDao by lazy { AppDatabase.getDatabase(context).reminderDao() }
    private val smartHomeManager by lazy { com.example.domain.SmartHomeManager(context) }

    suspend fun executeTool(action: String, params: Map<String, String>): ToolResult {
        return when (action) {
            "open_app" -> {
                val appName = params["appName"] ?: params["query"] ?: ""
                val result = appRegistry.launchAppByName(appName)
                when (result) {
                    is AppLaunchResult.Success -> ToolResult(
                        success = true,
                        action = "open_app",
                        message = result.message,
                        data = result.appInfo.packageName
                    )
                    is AppLaunchResult.NotInstalled -> ToolResult(
                        success = false,
                        action = "open_app",
                        message = result.message,
                        errorCode = "APP_NOT_INSTALLED"
                    )
                    is AppLaunchResult.Error -> ToolResult(
                        success = false,
                        action = "open_app",
                        message = result.message,
                        errorCode = "LAUNCH_ERROR"
                    )
                }
            }

            "search_youtube" -> {
                val query = params["query"] ?: ""
                val result = appRegistry.searchYouTube(query)
                when (result) {
                    is AppLaunchResult.Success -> ToolResult(true, "search_youtube", result.message)
                    else -> ToolResult(false, "search_youtube", "Failed to search YouTube.", errorCode = "YOUTUBE_ERROR")
                }
            }

            "read_screen" -> {
                val service = AssistantAccessibilityService.getInstance()
                if (service != null) {
                    val text = service.readScreenText()
                    ToolResult(true, "read_screen", "Screen read successfully.", data = text)
                } else {
                    ToolResult(false, "read_screen", "Accessibility Service is disabled. Please enable Snaper Screen Assistant in Settings.", errorCode = "ACCESSIBILITY_DISABLED")
                }
            }

            "screen_tap" -> {
                val targetText = params["targetText"] ?: params["text"] ?: ""
                val service = AssistantAccessibilityService.getInstance()
                if (service != null) {
                    val clicked = service.findAndClickText(targetText)
                    if (clicked) {
                        ToolResult(true, "screen_tap", "Clicked element '$targetText' ✨")
                    } else {
                        ToolResult(false, "screen_tap", "Element '$targetText' not found on screen.", errorCode = "ELEMENT_NOT_FOUND")
                    }
                } else {
                    ToolResult(false, "screen_tap", "Accessibility Service disabled.", errorCode = "ACCESSIBILITY_DISABLED")
                }
            }

            "screen_scroll" -> {
                val forward = params["direction"]?.lowercase() != "backward"
                val service = AssistantAccessibilityService.getInstance()
                if (service != null) {
                    val scrolled = service.scrollScreen(forward)
                    ToolResult(scrolled, "screen_scroll", if (scrolled) "Scrolled successfully." else "Cannot scroll further.")
                } else {
                    ToolResult(false, "screen_scroll", "Accessibility Service disabled.", errorCode = "ACCESSIBILITY_DISABLED")
                }
            }

            "navigate_back" -> {
                val service = AssistantAccessibilityService.getInstance()
                val success = service?.navigateBack() ?: false
                ToolResult(success, "navigate_back", if (success) "Navigated back." else "Accessibility Service not bound.")
            }

            "navigate_home" -> {
                val service = AssistantAccessibilityService.getInstance()
                val success = service?.navigateHome() ?: false
                ToolResult(success, "navigate_home", if (success) "Navigated to Home." else "Accessibility Service not bound.")
            }

            "device_care" -> {
                val status = deviceCareManager.getDeviceHealthStatus()
                val report = "🔋 Battery: ${status.batteryPercentage}% (${status.batteryStatus})\n" +
                        "💾 Storage: ${status.usedStoragePercentage}% Used (${status.freeStorageGb} GB Free)\n" +
                        "🌐 Network: ${status.networkState}\n" +
                        "💡 Recommendation: ${status.summaryRecommendation}"
                ToolResult(true, "device_care", report, data = status.toString())
            }

            "memory_save" -> {
                val key = params["key"] ?: "fact"
                val value = params["value"] ?: params["content"] ?: ""
                val category = params["category"] ?: "preference"
                val tags = params["tags"] ?: ""

                val existing = memoryDao.getMemoryByKey(key)
                val memory = MemoryEntity(
                    id = existing?.id ?: 0,
                    category = category,
                    key = key,
                    content = value,
                    value = value,
                    tags = tags,
                    updatedAt = System.currentTimeMillis()
                )
                memoryDao.insertMemory(memory)
                ToolResult(true, "memory_save", "Saved memory: '$key' = '$value' 🧠")
            }

            "memory_search" -> {
                val query = params["query"] ?: ""
                val memories = memoryDao.searchMemories(query).firstOrNull() ?: emptyList()
                val summary = if (memories.isEmpty()) {
                    "No matching memories found for '$query'."
                } else {
                    memories.joinToString("\n") { "• ${it.key}: ${it.value}" }
                }
                ToolResult(true, "memory_search", summary, data = summary)
            }

            "memory_delete" -> {
                val key = params["key"] ?: ""
                memoryDao.deleteMemoryByKey(key)
                ToolResult(true, "memory_delete", "Deleted memory for key '$key'.")
            }

            "reminder_create" -> {
                val title = params["title"] ?: "Reminder"
                val delayMinutes = params["delayMinutes"]?.toLongOrNull() ?: 10L
                val timeMillis = System.currentTimeMillis() + (delayMinutes * 60 * 1000)

                val reminder = ReminderEntity(
                    title = title,
                    timeMillis = timeMillis
                )
                val id = reminderDao.insertReminder(reminder)
                ToolResult(true, "reminder_create", "Scheduled reminder '$title' for $delayMinutes mins from now. ⏰", data = id.toString())
            }

            "smart_home" -> {
                val command = params["command"] ?: params["query"] ?: "${params["device"] ?: "device"} ${params["state"] ?: "toggle"}"
                val resultMsg = smartHomeManager.processNaturalLanguageCommand(command)
                ToolResult(true, "smart_home", "🏠 $resultMsg", data = resultMsg)
            }

            "call_contact" -> {
                val target = params["name"] ?: ""
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                ToolResult(true, "call_contact", "Opened Phone Dialer for '$target' 📞")
            }

            "social_search" -> {
                val platformName = params["platform"] ?: "youtube"
                val query = params["query"] ?: ""
                val result = appRegistry.searchSocialPlatform(platformName, query)
                when (result) {
                    is AppLaunchResult.Success -> ToolResult(true, "social_search", result.message, data = result.appInfo.packageName)
                    else -> ToolResult(false, "social_search", "Failed to search $platformName.", errorCode = "SOCIAL_SEARCH_ERROR")
                }
            }

            "send_social_message" -> {
                val platformName = params["platform"] ?: "whatsapp"
                val recipient = params["recipient"] ?: params["to"] ?: ""
                val message = params["message"] ?: params["text"] ?: ""
                val manager = com.example.appcontrol.SocialMediaAutomationManager(context)
                val platform = com.example.appcontrol.SocialPlatform.values().find {
                    it.id.equals(platformName, ignoreCase = true)
                } ?: com.example.appcontrol.SocialPlatform.WHATSAPP

                val actionResult = manager.sendDirectMessage(platform, recipient, message)
                ToolResult(actionResult.success, "send_social_message", actionResult.message, data = actionResult.launchedUri)
            }

            "analyze_video_reel" -> {
                val videoUrl = params["url"] ?: params["videoUrl"] ?: "https://www.instagram.com/reels/"
                val userSettings = com.example.data.preferences.UserPreferencesRepository(context).userSettingsFlow.firstOrNull() ?: com.example.data.preferences.UserSettings()
                val engine = com.example.appcontrol.SocialVideoAnalysisEngine(context)
                val result = engine.analyzeReelOrVideo(videoUrl, userSettings)
                ToolResult(true, "analyze_video_reel", "Analyzed Reel at $videoUrl ✨\nSummary: ${result.summary}\nFact Check: ${result.factCheckVerification}", data = result.transcript)
            }

            "fact_check_claim" -> {
                val claim = params["claim"] ?: params["query"] ?: ""
                val userSettings = com.example.data.preferences.UserPreferencesRepository(context).userSettingsFlow.firstOrNull() ?: com.example.data.preferences.UserSettings()
                val engine = com.example.appcontrol.CrossPlatformFactCheckEngine(context)
                val report = engine.performCrossPlatformFactCheck(
                    claim,
                    listOf(
                        com.example.appcontrol.SocialPlatform.YOUTUBE,
                        com.example.appcontrol.SocialPlatform.TWITTER,
                        com.example.appcontrol.SocialPlatform.INSTAGRAM,
                        com.example.appcontrol.SocialPlatform.FACEBOOK
                    ),
                    userSettings
                )
                ToolResult(true, "fact_check_claim", "Fact-Check Report for '$claim': [${report.truthRating.label}]\n${report.synthesisSummary}", data = report.truthRating.label)
            }

            else -> ToolResult(false, action, "Unknown action '$action'.", errorCode = "UNKNOWN_ACTION")
        }
    }
}
