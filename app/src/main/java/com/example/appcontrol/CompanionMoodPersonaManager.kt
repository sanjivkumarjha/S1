package com.example.appcontrol

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class CompanionPersona(
    val title: String,
    val emoji: String,
    val pitch: Float,
    val speed: Float,
    val greetingPrefix: String
) {
    FRIENDLY("Friendly Companion", "😊", 1.25f, 1.0f, "Hey there! How can I help you today?"),
    SUPPORTIVE("Supportive & Caring", "💕", 1.15f, 0.90f, "I am right here with you, boss. Take it easy."),
    HUMOROUS("Playful & Humorous", "😂", 1.35f, 1.10f, "Haha! Ready to crush some goals together?"),
    ROMANTIC_FLIRTY("Sweet & Flirty", "💖", 1.30f, 0.95f, "Radhe Radhe sweetheart! Always here just for you!")
}

enum class UserDetectedMood(val label: String, val emoji: String) {
    HAPPY("Happy", "😊"),
    SAD("Sad / Low Energy", "🥺"),
    STRESSED("Stressed / Busy", "😤"),
    CONVERSATIONAL("Conversational", "💬")
}

/**
 * Real-Time Mood Detection & Companion Persona Switching Engine.
 * Evaluates facial indicators & acoustic metrics to update user mood and switch TTS voice persona dynamically.
 */
class CompanionMoodPersonaManager(private val context: Context) {

    private val _currentMood = MutableStateFlow(UserDetectedMood.HAPPY)
    val currentMood: StateFlow<UserDetectedMood> = _currentMood.asStateFlow()

    private val _activePersona = MutableStateFlow(CompanionPersona.FRIENDLY)
    val activePersona: StateFlow<CompanionPersona> = _activePersona.asStateFlow()

    fun updateMoodFromBiometrics(
        brightness: Float,
        voicePitchMetric: Float,
        speechSpeedMetric: Float
    ) {
        val detected = when {
            voicePitchMetric < 0.8f || speechSpeedMetric < 0.8f -> UserDetectedMood.SAD
            voicePitchMetric > 1.3f && speechSpeedMetric > 1.2f -> UserDetectedMood.STRESSED
            voicePitchMetric in 1.0f..1.3f -> UserDetectedMood.HAPPY
            else -> UserDetectedMood.CONVERSATIONAL
        }

        _currentMood.value = detected
        adaptPersonaToMood(detected)
    }

    fun setPersonaManually(persona: CompanionPersona) {
        _activePersona.value = persona
    }

    private fun adaptPersonaToMood(mood: UserDetectedMood) {
        val adapted = when (mood) {
            UserDetectedMood.SAD -> CompanionPersona.SUPPORTIVE
            UserDetectedMood.STRESSED -> CompanionPersona.SUPPORTIVE
            UserDetectedMood.HAPPY -> CompanionPersona.HUMOROUS
            UserDetectedMood.CONVERSATIONAL -> CompanionPersona.FRIENDLY
        }
        _activePersona.value = adapted
    }
}
