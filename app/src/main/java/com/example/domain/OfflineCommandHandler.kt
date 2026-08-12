package com.example.domain

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.provider.AlarmClock
import android.provider.Settings
import com.example.appcontrol.AppLaunchResult
import com.example.appcontrol.AppRegistry
import com.example.data.preferences.UserSettings
import com.example.data.preferences.UserPreferencesRepository
import com.example.devicecare.DeviceCareManager
import com.example.security.FindMyAssistantManager
import com.example.service.AssistantAccessibilityService
import kotlinx.coroutines.flow.firstOrNull

data class OfflineCommandResult(
    val isHandled: Boolean,
    val responseText: String,
    val shouldStopTts: Boolean = false
)

class OfflineCommandHandler(private val context: Context) {

    private val prefsRepo = UserPreferencesRepository(context)
    private val appRegistry = AppRegistry(context)
    private val deviceCareManager = DeviceCareManager(context)
    private val universalAppSearchManager = com.example.domain.app.UniversalAppSearchManager.getInstance(context)

    suspend fun handleCommand(rawInput: String): OfflineCommandResult {
        val query = rawInput.trim().lowercase()

        // 0. Phone Finder / "Where are you?" Voice Command Handler
        val findManager = FindMyAssistantManager.getInstance(context)
        if (findManager.matchesVoiceFindCommand(query)) {
            val settings = prefsRepo.userSettingsFlow.firstOrNull() ?: UserSettings()
            if (settings.isPhoneFinderEnabled && settings.isPhoneFinderVoiceDetectionEnabled) {
                findManager.startPhoneFindingWorkflow(
                    ownerName = settings.ownerName,
                    ownerTitle = settings.ownerTitle
                ) {}
                val immediateResult = findManager.calculateSensorConfidence(
                    ownerName = settings.ownerName,
                    ownerTitle = settings.ownerTitle
                )
                return OfflineCommandResult(
                    isHandled = true,
                    responseText = immediateResult.hindiText
                )
            }
        }

        // 0B. Universal App Search / Action Handler
        if (universalAppSearchManager.matchesUniversalSearchQuery(query)) {
            val actionResult = universalAppSearchManager.executeUniversalAction(rawInput)
            return OfflineCommandResult(
                isHandled = true,
                responseText = actionResult.responseText
            )
        }

        // 1. "रुको" / "ruko" / "stop"
        if (query.contains("रुको") || query == "ruko" || query == "stop" || query.contains("stop speaking")) {
            return OfflineCommandResult(
                isHandled = true,
                responseText = "राधे राधे! 🙏 जी, रुक गई! Preference saved locally.",
                shouldStopTts = true
            )
        }

        // Doctor Mode & Vehicle Mode Voice Commands
        if (query.contains("doctor mode on") || query.contains("doctor mode चालू") || query.contains("doctor mode में")) {
            val docMgr = DoctorModeManager(context)
            val res = docMgr.enableDoctorMode()
            return OfflineCommandResult(isHandled = true, responseText = res)
        }
        if (query.contains("doctor mode off") || query.contains("doctor mode बंद")) {
            val docMgr = DoctorModeManager(context)
            val res = docMgr.disableDoctorMode()
            return OfflineCommandResult(isHandled = true, responseText = res)
        }
        if (query.contains("vehicle mode on") || query.contains("vehicle mode चालू") || query.contains("car mode on") || query.contains("driving mode on")) {
            prefsRepo.setVehicleModeEnabled(true)
            return OfflineCommandResult(isHandled = true, responseText = "🚗 Vehicle Mode Activated! Large hands-free voice controls and safe driving HUD enabled.")
        }
        if (query.contains("vehicle mode off") || query.contains("vehicle mode बंद") || query.contains("car mode off")) {
            prefsRepo.setVehicleModeEnabled(false)
            return OfflineCommandResult(isHandled = true, responseText = "🚗 Vehicle Mode Deactivated.")
        }

        // Vehicle Telemetry & Action Commands
        val vehicleMgr = com.example.domain.vehicle.VehicleConnectivityManager.getInstance(context)
        val vehicles = vehicleMgr.vehiclesFlow.value
        val primaryVehicle = vehicles.find { it.isPrimary } ?: vehicles.firstOrNull()

        if (query.contains("गाड़ी connected") || query.contains("vehicle connected") || query.contains("car connected")) {
            val statusStr = primaryVehicle?.let {
                "🚗 ${it.name} is currently ${it.connectionStatus.label}. Battery: ${it.batteryPercent}%, Range: ${it.estimatedRangeKm} km."
            } ?: "No vehicle paired."
            return OfflineCommandResult(isHandled = true, responseText = statusStr)
        }
        if (query.contains("बैटरी कितना") || query.contains("battery status") || query.contains("car battery") || query.contains("battery kitni")) {
            val statusStr = primaryVehicle?.let {
                "🔋 ${it.name} Battery: ${it.batteryPercent}% (${it.chargingStatus.label}). Estimated Range: ${it.estimatedRangeKm} km."
            } ?: "No vehicle information available."
            return OfflineCommandResult(isHandled = true, responseText = statusStr)
        }
        if (query.contains("range कितनी") || query.contains("car range") || query.contains("vehicle range") || query.contains("kitni range")) {
            val statusStr = primaryVehicle?.let {
                "🛣️ ${it.name} Estimated Range: ${it.estimatedRangeKm} km."
            } ?: "No vehicle range available."
            return OfflineCommandResult(isHandled = true, responseText = statusStr)
        }
        if (query.contains("lock करो") || query.contains("lock my car") || query.contains("car lock")) {
            val result = primaryVehicle?.let { vehicleMgr.executeVehicleCommand(it.id, "LOCK") } ?: "No vehicle found."
            return OfflineCommandResult(isHandled = true, responseText = result)
        }
        if (query.contains("unlock करो") || query.contains("unlock my car") || query.contains("car unlock")) {
            val result = primaryVehicle?.let { vehicleMgr.executeVehicleCommand(it.id, "UNLOCK") } ?: "No vehicle found."
            return OfflineCommandResult(isHandled = true, responseText = result)
        }
        if (query.contains("चार्जिंग शुरू") || query.contains("start charging")) {
            val result = primaryVehicle?.let { vehicleMgr.executeVehicleCommand(it.id, "START_CHARGING") } ?: "No vehicle found."
            return OfflineCommandResult(isHandled = true, responseText = result)
        }

        // Title update command ("मुझे Boss बोलो", "call me Boss", "call me Sir")
        if (query.contains("boss बोलो") || query.contains("call me boss") || query.contains("boss kahna")) {
            prefsRepo.updateOwnerTitle("Boss")
            return OfflineCommandResult(isHandled = true, responseText = "जी बिल्कुल Boss! 🫡 From now on, I will address you as Boss.")
        }
        if (query.contains("sir बोलो") || query.contains("call me sir")) {
            prefsRepo.updateOwnerTitle("Sir")
            return OfflineCommandResult(isHandled = true, responseText = "Yes Sir! 🫡 I will address you as Sir.")
        }

        // Security Mode voice commands ("security mode on", "restricted mode on", "owner mode on")
        if (query.contains("restricted mode") || query.contains("guest access बंद")) {
            prefsRepo.updateSecurityMode("RESTRICTED")
            return OfflineCommandResult(isHandled = true, responseText = "🔒 Restricted Security Mode Activated. Private owner data is now locked.")
        }
        if (query.contains("owner mode") || query.contains("owner mode on")) {
            prefsRepo.updateSecurityMode("OWNER")
            return OfflineCommandResult(isHandled = true, responseText = "🔓 Owner Mode Activated. Welcome back, Boss!")
        }

        // Mood & Emotional Support ("mood ठीक नहीं है", "मन खराब है", "sad today")
        if (query.contains("mood ठीक नहीं") || query.contains("mood kharab") || query.contains("मन खराब") || query.contains("feeling sad")) {
            return OfflineCommandResult(
                isHandled = true,
                responseText = "Boss, क्या हुआ? आज थोड़ा परेशान लग रहे हो. बताओ, मैं हूँ ना आपके साथ, सब ठीक हो जाएगा! ❤️"
            )
        }

        // Family Ramayan Request ("मम्मी को रामायण दिखाओ", "ramayan for mummy")
        if (query.contains("रामायण") || query.contains("ramayan")) {
            appRegistry.searchYouTube("Ramayan Serial Full Episodes HD")
            return OfflineCommandResult(isHandled = true, responseText = "Opening Ramayan on YouTube for Mummy... 🙏")
        }

        // 2. Device Care / Phone Health ("मेरे फोन का ध्यान रखना", "phone health", "device care")
        if (query.contains("फोन का ध्यान") || query.contains("phone health") || query.contains("device care") || query.contains("phone status")) {
            val status = deviceCareManager.getDeviceHealthStatus()
            val text = "📱 Phone Status:\n" +
                    "🔋 Battery: ${status.batteryPercentage}% (${status.batteryStatus})\n" +
                    "💾 Storage: ${status.usedStoragePercentage}% Used (${status.freeStorageGb} GB Free)\n" +
                    "🌐 Network: ${status.networkState}\n" +
                    "💡 ${status.summaryRecommendation}"
            return OfflineCommandResult(isHandled = true, responseText = text)
        }

        // 3. Installed Apps List Discovery ("मेरे फोन में कौन-कौन से apps हैं")
        if (query.contains("कौन-कौन से apps") || query.contains("apps in my phone") || query.contains("installed apps")) {
            val apps = appRegistry.getInstalledApps().take(12)
            val names = apps.joinToString(", ") { it.applicationLabel }
            return OfflineCommandResult(
                isHandled = true,
                responseText = "📱 Installed Apps on your device include: $names, and more. Open 'App Control' to view all apps and manage aliases."
            )
        }

        // 4. Flashlight / Torch
        if (query.contains("flashlight on") || query.contains("torch on") || query.contains("light on") || query.contains("टॉर्च ऑन")) {
            val success = setFlashlight(true)
            if (success) com.example.ui.glass.DynamicIslandImpressionController.setFlashlightActive(true)
            return OfflineCommandResult(
                isHandled = true,
                responseText = if (success) "राधे राधे! Flashlight turned ON. 🔦" else "Unable to toggle flashlight on this device."
            )
        }
        if (query.contains("flashlight off") || query.contains("torch off") || query.contains("light off") || query.contains("टॉर्च ऑफ")) {
            val success = setFlashlight(false)
            if (success) com.example.ui.glass.DynamicIslandImpressionController.setFlashlightActive(false)
            return OfflineCommandResult(
                isHandled = true,
                responseText = if (success) "राधे राधे! Flashlight turned OFF. 🔦" else "Unable to toggle flashlight."
            )
        }

        // 5. Volume commands
        if (query.contains("volume up") || query.contains("आवाज़ बढ़ाओ") || query.contains("sound up")) {
            adjustVolume(AudioManager.ADJUST_RAISE)
            return OfflineCommandResult(isHandled = true, responseText = "Volume increased! 🔊")
        }
        if (query.contains("volume down") || query.contains("आवाज़ कम करो") || query.contains("sound down")) {
            adjustVolume(AudioManager.ADJUST_LOWER)
            return OfflineCommandResult(isHandled = true, responseText = "Volume decreased. 🔉")
        }
        if (query.contains("mute") || query.contains("silent") || query.contains("म्यूट")) {
            adjustVolume(AudioManager.ADJUST_MUTE)
            return OfflineCommandResult(isHandled = true, responseText = "Audio muted. 🔇")
        }

        // 6. YouTube Commands
        if (query.contains("youtube")) {
            val res = appRegistry.searchYouTube(query)
            return if (res is AppLaunchResult.Success) {
                OfflineCommandResult(isHandled = true, responseText = res.message)
            } else {
                OfflineCommandResult(isHandled = true, responseText = "Opening YouTube...")
            }
        }

        // 7. Open Apps (Uses AppRegistry for exact package resolution & WhatsApp vs WhatsApp Business check)
        if (query.startsWith("open ") || query.contains("खोलो") || query.contains("open ")) {
            val launchRes = appRegistry.launchAppByName(query)
            if (launchRes is AppLaunchResult.Success) {
                return OfflineCommandResult(isHandled = true, responseText = launchRes.message)
            } else if (launchRes is AppLaunchResult.NotInstalled) {
                return OfflineCommandResult(isHandled = true, responseText = launchRes.message)
            }
        }

        // 8. Alarm & Timer
        if (query.contains("alarm") || query.contains("अलार्म")) {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_MESSAGE, "Snaper AI Alarm")
                putExtra(AlarmClock.EXTRA_HOUR, 7)
                putExtra(AlarmClock.EXTRA_MINUTES, 0)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            launchIntent(intent)
            return OfflineCommandResult(isHandled = true, responseText = "Opening Alarm Clock...")
        }
        if (query.contains("timer") || query.contains("टाइमर")) {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, 300)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Snaper AI Timer")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            launchIntent(intent)
            return OfflineCommandResult(isHandled = true, responseText = "Opening Timer...")
        }

        // 9. Call / Dial
        if (query.startsWith("call ") || query.startsWith("dial ") || query.startsWith("कॉल ")) {
            val target = query.replace("call ", "").replace("dial ", "").replace("कॉल ", "").trim()
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$target")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            launchIntent(intent)
            return OfflineCommandResult(isHandled = true, responseText = "Opening Dialer for $target...")
        }

        // 10. Accessibility & Screen Control Commands
        if (query.contains("read screen") || query.contains("screen text") || query.contains("स्क्रीन पढ़ो") || query.contains("screen content")) {
            val service = AssistantAccessibilityService.getInstance()
            return if (service != null) {
                val text = service.readScreenText()
                OfflineCommandResult(isHandled = true, responseText = "📱 Screen Text:\n$text")
            } else {
                launchIntent(AssistantAccessibilityService.openAccessibilitySettingsIntent())
                OfflineCommandResult(isHandled = true, responseText = "Please enable 'Snaper AI Screen Assistant' in Accessibility Settings to allow screen reading.")
            }
        }

        if (query.startsWith("click ") || query.startsWith("tap ") || query.startsWith("दबाओ ")) {
            val targetText = query.replace("click ", "").replace("tap ", "").replace("दबाओ ", "").trim()
            val service = AssistantAccessibilityService.getInstance()
            return if (service != null) {
                val clicked = service.findAndClickText(targetText)
                OfflineCommandResult(
                    isHandled = true,
                    responseText = if (clicked) "Clicked on '$targetText' ✨" else "Could not find clickable element matching '$targetText'."
                )
            } else {
                launchIntent(AssistantAccessibilityService.openAccessibilitySettingsIntent())
                OfflineCommandResult(isHandled = true, responseText = "Enable Accessibility Service to perform clicks.")
            }
        }

        if (query == "go back" || query == "back" || query.contains("पीछे जाओ")) {
            val service = AssistantAccessibilityService.getInstance()
            return if (service != null) {
                val done = service.navigateBack()
                OfflineCommandResult(isHandled = true, responseText = if (done) "Navigated Back ⬅️" else "Failed to navigate back.")
            } else {
                OfflineCommandResult(isHandled = false, responseText = "")
            }
        }

        if (query == "go home" || query == "home screen" || query.contains("होम स्क्रीन")) {
            val service = AssistantAccessibilityService.getInstance()
            return if (service != null) {
                val done = service.navigateHome()
                OfflineCommandResult(isHandled = true, responseText = if (done) "Returned Home 🏠" else "Failed to return home.")
            } else {
                OfflineCommandResult(isHandled = false, responseText = "")
            }
        }

        // 11. Split-Screen Multi-Window Command
        if (query.contains("split screen") || query.contains("स्प्लिट स्क्रीन") || query.contains("multi window")) {
            val service = AssistantAccessibilityService.getInstance()
            return if (service != null) {
                var pkg1 = "com.whatsapp"
                var pkg2 = "com.google.android.youtube"
                if (query.contains("chrome")) pkg2 = "com.android.chrome"
                if (query.contains("maps")) pkg2 = "com.google.android.apps.maps"

                val done = service.launchSplitScreenWithApps(pkg1, pkg2, context)
                OfflineCommandResult(
                    isHandled = true,
                    responseText = if (done) "Triggered Split-Screen Multi-Window Mode 📱📱" else "Toggled split-screen view."
                )
            } else {
                launchIntent(AssistantAccessibilityService.openAccessibilitySettingsIntent())
                OfflineCommandResult(isHandled = true, responseText = "Please enable 'Snaper AI Screen Assistant' in Accessibility Settings for split-screen automation.")
            }
        }

        // 12. Floating Window & Overlay Bubble Command
        if (query.contains("floating window") || query.contains("floating bubble") || query.contains("फ्लोटिंग विंडो")) {
            val floatManager = com.example.appcontrol.FloatingOverlayWindowManager.getInstance(context)
            val shown = floatManager.showFloatingAssistantBubble()
            return OfflineCommandResult(
                isHandled = true,
                responseText = if (shown) "Floating AI Overlay Bubble active on screen! ✨" else "Please grant 'Display over other apps' permission in System Settings."
            )
        }

        // 13. Background Music Command
        if (query.contains("background music") || query.contains("play music") || query.contains("music play") || query.contains("बैकग्राउंड म्यूजिक")) {
            val audioManager = com.example.media.BackgroundAudioManager.getInstance(context)
            audioManager.togglePlayPause()
            return OfflineCommandResult(
                isHandled = true,
                responseText = "Toggled background music stream 🎵"
            )
        }

        return OfflineCommandResult(isHandled = false, responseText = "")
    }

    private fun setFlashlight(enabled: Boolean): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull() ?: return false
            cameraManager.setTorchMode(cameraId, enabled)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun adjustVolume(direction: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun openTargetApp(appName: String): Boolean {
        val pm = context.packageManager
        val packageNameMap = mapOf(
            "youtube" to "com.google.android.youtube",
            "whatsapp" to "com.whatsapp",
            "camera" to "android.media.action.IMAGE_CAPTURE",
            "chrome" to "com.android.chrome",
            "maps" to "com.google.android.apps.maps",
            "gallery" to "com.google.android.apps.photos",
            "messages" to "com.google.android.apps.messaging",
            "calculator" to "com.google.android.calculator"
        )

        val targetPkg = packageNameMap[appName]
        if (targetPkg != null) {
            val intent = pm.getLaunchIntentForPackage(targetPkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        }

        // Try generic search intent or launch
        return try {
            val launchIntent = pm.getLaunchIntentForPackage(appName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    private fun launchIntent(intent: Intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
