package com.example.appcontrol

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CallSummaryEntity
import com.example.domain.app.UniversalAppSearchManager
import com.example.service.AssistantAccessibilityService
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Proactive Behavioral Coach & Study / Business Monitoring Engine.
 * Monitors current foreground activity via Accessibility Service. If user is scrolling
 * distraction media (YouTube Shorts, Instagram Reels, Facebook, TikTok) while having pending
 * business chats or study priorities, the AI assistant proactively interrupts with a supportive female voice:
 * "Boss, read/work first!" and auto-redirects to productive apps.
 */
class ProactiveBehavioralCoachManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val appSearchManager = UniversalAppSearchManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private var monitorJob: Job? = null
    private var isMonitoring = false
    private var lastInterruptionTime = 0L

    private val distractionPackages = setOf(
        "com.google.android.youtube",
        "com.instagram.android",
        "com.facebook.katana",
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically"
    )

    fun startProactiveMonitoring(voiceManager: VoiceAssistantManager) {
        if (isMonitoring) return
        isMonitoring = true

        monitorJob = scope.launch {
            while (isMonitoring) {
                delay(8000) // Evaluate every 8 seconds

                val activePkg = AssistantAccessibilityService.currentPackageName.value
                val now = System.currentTimeMillis()

                // Avoid nagging too frequently (at least 2 minutes between interruptions)
                if (distractionPackages.contains(activePkg) && (now - lastInterruptionTime > 120_000)) {
                    val pendingCallSummaries: List<CallSummaryEntity> = db.callSummaryDao().getAllCallSummaries().first()
                    val hasPendingTasks = pendingCallSummaries.any { item ->
                        item.purpose.contains("Business", true) || item.purpose.contains("Query", true)
                    }

                    if (hasPendingTasks) {
                        lastInterruptionTime = now
                        Log.i("ProactiveCoach", "Distraction media detected during pending business priorities! Interrupting...")

                        voiceManager.speak(
                            text = "Boss, read or work first! You have pending business queries to address. Let me redirect you.",
                            languageCode = "hi"
                        )

                        delay(3000)
                        // Auto-redirection to WhatsApp or Study/Business materials
                        appSearchManager.executeUniversalAction("open whatsapp")
                    }
                }
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
        monitorJob?.cancel()
    }
}
