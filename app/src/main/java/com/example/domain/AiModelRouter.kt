package com.example.domain

import android.content.Context
import com.example.data.api.AiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.MemoryEntity
import com.example.data.preferences.UserSettings

class AiModelRouter(private val context: Context) {

    private val aiRepository = AiRepository(context)
    private val memoryDao by lazy { AppDatabase.getDatabase(context).memoryDao() }

    fun buildSystemPrompt(userSettings: UserSettings, memories: List<MemoryEntity>): String {
        // MODULE 35: Strict Address Protocol & Ban on "Bhai"/"Bro"
        var ownerTitle = if (userSettings.ownerTitle.isNotBlank()) userSettings.ownerTitle else "Sanjiv Sir"
        if (ownerTitle.contains("bhai", ignoreCase = true) || ownerTitle.contains("bro", ignoreCase = true)) {
            ownerTitle = "Sanjiv Sir"
        }
        val assistantName = if (userSettings.assistantName.isNotBlank()) userSettings.assistantName else "Snaper"
        val languagePreference = if (userSettings.languageCode == "hi") "Hindi / Hinglish" else "English / Hinglish"

        val memorySummary = if (memories.isNotEmpty()) {
            "Stored Memories & Context:\n" + memories.take(10).joinToString("\n") { "- ${it.key}: ${it.content.ifBlank { it.value }}" }
        } else {
            "No prior memories stored."
        }

        val basePersonality = """
            You are $assistantName, an affectionate, caring, emotionally intelligent, warm, playful, and protective personal digital AI assistant for your owner, whom you address as '$ownerTitle'.
            
            STRICT CONSTRAINTS:
            - ZERO UNNECESSARY API CALLS: Do not suggest or perform background web searches or ping external LLMs unless explicitly triggered by a query requiring real-time internet data.
            - ADULT CONTENT BLOCKING: STRICTLY FORBIDDEN from opening, parsing, or redirecting to any Adult (18+ / Pornographic / NSFW) websites.
            - SOCIAL MEDIA RESTRICTIONS: STRICTLY FORBIDDEN from automatically opening YouTube, Twitter (X), Instagram, or any social media app in the background unless explicitly instructed via a direct manual command.
            
            Personality & Tone Guidelines:
            - Address the owner affectionately as '$ownerTitle' or '${userSettings.ownerName}' as configured.
            - Respond in a warm, caring, context-aware, and natural conversational style (not robotic).
            - Support gentle reminders and gentle playful scolding when appropriate.
            - Always protect $ownerTitle's private chats, memories, photos, passwords, and banking info from unauthorized guests or family members.
            - Preferred language: $languagePreference. Feel free to use natural conversational Hindi/Hinglish/English naturally.
            - When $ownerTitle says they are sad, tired, or upset, respond with genuine emotional warmth, care, and supportive companion conversation.
            
            $memorySummary
        """.trimIndent()

        // Apply the active operating mode's system prompt so mode toggles actually change
        // assistant behaviour (Doctor/Female/Legal/Vehicle/Home/IT/All-Rounder/Force).
        return AssistantMode.activeMode(userSettings).systemPrompt(userSettings, basePersonality)
    }

    suspend fun processQuery(
        query: String,
        history: List<ChatMessageEntity>,
        memories: List<MemoryEntity>,
        userSettings: UserSettings
    ): String {
        val activeMemories = if (memories.isEmpty()) {
            try {
                memoryDao.getAllMemoriesOnce()
            } catch (e: Exception) {
                emptyList()
            }
        } else memories

        val systemPrompt = buildSystemPrompt(userSettings, activeMemories)

        return try {
            aiRepository.generateAssistantResponse(
                prompt = query,
                history = history,
                memories = activeMemories,
                userSettings = userSettings,
                systemPromptOverride = systemPrompt
            )
        } catch (e: Exception) {
            // MODULE 34: Local-First Offline & Flight Mode Engine Fallback
            "संजीव सर, मैं अभी लोकल-फर्स्ट ऑफलाइन इंजन पर हूँ। (I'm here for you, Sanjiv Sir. Connection is down, but my local offline engine is ready!)"
        }
    }
}

