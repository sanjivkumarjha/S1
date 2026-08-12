package com.example.appcontrol

import android.content.Context
import com.example.BuildConfig
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class EmotionType(
    val id: String,
    val displayName: String,
    val emoji: String,
    val colorHex: String,
    val pitchMultiplier: Float,
    val speechRateMultiplier: Float,
    val avatarAnimation: String
) {
    AMUSED_LAUGHTER("amused_laughter", "Amused & Laughing", "😂", "#F59E0B", 1.25f, 1.15f, "JOY_BURST_PARTICLES"),
    EMPATHY_SADNESS("empathy_sadness", "Empathic & Touched", "🥺", "#3B82F6", 0.90f, 0.85f, "GENTLE_PULSE_BLUE"),
    OUTRAGED_CONCERN("outraged_concern", "Concerned & Outraged", "😱", "#EF4444", 1.15f, 1.20f, "ALERT_GLOW_RED"),
    EXCITED_THRILL("excited_thrill", "Excited & Hyped", "🤩", "#10B981", 1.30f, 1.25f, "HYPER_SPARKLE_GREEN"),
    CURIOSITY_AMAZEMENT("curiosity_amazement", "Amazed & Curious", "🤯", "#8B5CF6", 1.10f, 1.00f, "AURA_ORB_PURPLE"),
    CALM_REASSURING("calm_reassuring", "Calm & Reassuring", "😌", "#06B6D4", 1.00f, 0.95f, "WAVE_FLOW_CYAN")
}

data class VoiceToneConfig(
    val pitch: Float,
    val speed: Float,
    val emotionalEmphasis: String
)

data class EmotionalReactionResult(
    val primaryEmotion: EmotionType,
    val emotionalIntensity: Float, // 0.0 to 1.0
    val aiVerbalReaction: String,
    val facialExpressionAnalysis: String,
    val audioToneAndMusic: String,
    val empathyContextNote: String,
    val voiceToneConfig: VoiceToneConfig,
    val avatarVisualState: String
)

class MultimodalEmotionEngine(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Evaluates visual actions, facial expressions, audio dialogue, and context to generate a human-like emotional reaction.
     */
    suspend fun evaluateVideoEmotions(
        videoUrlOrName: String,
        platformName: String,
        visualSummary: String,
        transcript: String,
        userSettings: UserSettings
    ): EmotionalReactionResult = withContext(Dispatchers.IO) {
        val apiKey = if (userSettings.userApiKey.isNotBlank()) userSettings.userApiKey else BuildConfig.GEMINI_API_KEY
        val modelName = if (userSettings.selectedModel.isNotBlank()) userSettings.selectedModel else "gemini-3.5-flash"

        val prompt = """
            You are Snaper AI Assistant's Human-like Multimodal Emotional Intelligence Core.
            
            Target Video / Reel: $videoUrlOrName ($platformName)
            Visual Summary: $visualSummary
            Dialogue & Audio Transcript: $transcript
            
            Task:
            Analyze the human facial expressions, dialogue tone, background music, context, and visual actions in this video.
            Determine how a genuine human friend would emotionally react to this video (e.g. laughing at funny fails, showing deep empathy for emotional stories, feeling outraged at unfair situations, or being thrilled by epic moments).
            
            Synthesize a response STRICTLY in JSON format (NO MARKDOWN CODEBLOCKS):
            {
              "primaryEmotion": "AMUSED_LAUGHTER" | "EMPATHY_SADNESS" | "OUTRAGED_CONCERN" | "EXCITED_THRILL" | "CURIOSITY_AMAZEMENT" | "CALM_REASSURING",
              "emotionalIntensity": 0.92,
              "aiVerbalReaction": "Natural, enthusiastic, human-like reaction spoken as if watching this video with a friend (use expressive words, laughter or empathy as fitting)...",
              "facialExpressionAnalysis": "Detailed description of facial expressions observed in video frames (e.g., creator grinning, eyes wide in disbelief)...",
              "audioToneAndMusic": "Analysis of vocal tone, speech cadence, and background music track...",
              "empathyContextNote": "Why this video evokes this specific emotional reaction..."
            }
        """.trimIndent()

        try {
            val rawJson = callGemini(apiKey, modelName, prompt)
            parseEmotionResult(rawJson)
        } catch (e: Exception) {
            fallbackEmotionResult(visualSummary, transcript)
        }
    }

    private fun callGemini(apiKey: String, model: String, prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val partsArray = JSONArray().put(JSONObject().put("text", prompt))
        val contentObj = JSONObject().apply {
            put("role", "user")
            put("parts", partsArray)
        }
        val rootJson = JSONObject().apply {
            put("contents", JSONArray().put(contentObj))
        }

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Empty response from Gemini")

        if (!response.isSuccessful) {
            throw Exception("Gemini API error ${response.code}: $responseBody")
        }

        val json = JSONObject(responseBody)
        val candidates = json.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "")
            }
        }
        return "No response text."
    }

    private fun parseEmotionResult(rawText: String): EmotionalReactionResult {
        return try {
            val cleaned = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val json = JSONObject(cleaned)
            val emotionKey = json.optString("primaryEmotion", "AMUSED_LAUGHTER")
            val emotion = try {
                EmotionType.valueOf(emotionKey.uppercase())
            } catch (e: Exception) {
                EmotionType.AMUSED_LAUGHTER
            }

            val intensity = json.optDouble("emotionalIntensity", 0.85).toFloat()
            val verbalReaction = json.optString("aiVerbalReaction", "Haha, that's hilarious! Check that out! 😂")
            val facialExp = json.optString("facialExpressionAnalysis", "Subject grinning with expressive eye movements.")
            val audioTone = json.optString("audioToneAndMusic", "Upbeat audio track with enthusiastic vocal rhythm.")
            val contextNote = json.optString("empathyContextNote", "Evokes energetic joy and laughter.")

            EmotionalReactionResult(
                primaryEmotion = emotion,
                emotionalIntensity = intensity.coerceIn(0.1f, 1.0f),
                aiVerbalReaction = verbalReaction,
                facialExpressionAnalysis = facialExp,
                audioToneAndMusic = audioTone,
                empathyContextNote = contextNote,
                voiceToneConfig = VoiceToneConfig(
                    pitch = emotion.pitchMultiplier,
                    speed = emotion.speechRateMultiplier,
                    emotionalEmphasis = emotion.displayName
                ),
                avatarVisualState = emotion.avatarAnimation
            )
        } catch (e: Exception) {
            fallbackEmotionResult("Visual content analyzed.", "Audio dialogue processed.")
        }
    }

    private fun fallbackEmotionResult(visualSummary: String, transcript: String): EmotionalReactionResult {
        val isFunny = visualSummary.contains("funny", true) || transcript.contains("funny", true) || visualSummary.contains("laugh", true)
        val isSad = visualSummary.contains("sad", true) || visualSummary.contains("emotional", true) || visualSummary.contains("touching", true)
        val isThrilling = visualSummary.contains("stunt", true) || visualSummary.contains("action", true) || visualSummary.contains("epic", true)

        val emotion = when {
            isFunny -> EmotionType.AMUSED_LAUGHTER
            isSad -> EmotionType.EMPATHY_SADNESS
            isThrilling -> EmotionType.EXCITED_THRILL
            else -> EmotionType.CURIOSITY_AMAZEMENT
        }

        val verbal = when (emotion) {
            EmotionType.AMUSED_LAUGHTER -> "HAHAHA! That video was so unexpectedly hilarious! 😂 I couldn't stop grinning!"
            EmotionType.EMPATHY_SADNESS -> "Aww, that's so heart-touching... 🥺 Sending warm empathetic vibes for that story."
            EmotionType.EXCITED_THRILL -> "WHOA! That was insane! 🤩 Did you see that incredible skill and timing?!"
            else -> "Ooh, fascinating! 🤯 That video broke down some super intriguing concepts!"
        }

        return EmotionalReactionResult(
            primaryEmotion = emotion,
            emotionalIntensity = 0.90f,
            aiVerbalReaction = verbal,
            facialExpressionAnalysis = "Detected animated facial movements and clear emotive framing.",
            audioToneAndMusic = "Dynamic audio waveform with expressive spoken dialogue.",
            empathyContextNote = "Contextually aligned with video visual themes.",
            voiceToneConfig = VoiceToneConfig(
                pitch = emotion.pitchMultiplier,
                speed = emotion.speechRateMultiplier,
                emotionalEmphasis = emotion.displayName
            ),
            avatarVisualState = emotion.avatarAnimation
        )
    }
}
