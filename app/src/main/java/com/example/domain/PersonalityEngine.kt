package com.example.domain

import java.util.Calendar

/**
 * Centralized Personality Engine for Snaper Technology AI Assistant ("Roshni").
 * Controls assistant persona modes, emotional expression states, and greeting behavior.
 */
enum class PersonalityMode(val displayName: String, val description: String) {
    CARING("Caring & Affectionate", "Empathetic, warm, and supportive family companion"),
    PROFESSIONAL("Professional & Direct", "Concise, structured, and business-focused"),
    PLAYFUL("Playful & Cheerful", "Lighthearted, energetic, and witty"),
    GENTLE("Gentle & Calm", "Soft-spoken, peaceful, and soothing"),
    FAMILY("Family Mode", "Safe, shared interface protecting owner privacy")
}

enum class EmotionalState {
    CALM, HAPPY, ATTENTIVE, CONCERNED, SURPRISED, ROMANTIC, CARING
}

object PersonalityEngine {

    private var currentMode: PersonalityMode = PersonalityMode.CARING
    private var currentEmotion: EmotionalState = EmotionalState.CALM

    fun setMode(mode: PersonalityMode) {
        currentMode = mode
    }

    fun getMode(): PersonalityMode = currentMode

    fun setEmotionalState(state: EmotionalState) {
        currentEmotion = state
    }

    fun getEmotionalState(): EmotionalState = currentEmotion

    /**
     * Generates a personalized greeting for Sanjiv Sir / Owner with "राधे राधे" and time-of-day greeting.
     */
    fun generateGreeting(ownerName: String = "संजीव सर"): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeSalutation = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }

        // MODULE 35: Strict Address Protocol & Ban on "Bhai"/"Bro"
        var displayName = if (ownerName.isBlank() || ownerName == "User" || ownerName == "Boss") "संजीव सर" else ownerName
        if (displayName.contains("bhai", ignoreCase = true) || displayName.contains("bro", ignoreCase = true)) {
            displayName = "संजीव सर"
        }

        return when (currentMode) {
            PersonalityMode.CARING -> "राधे राधे! $timeSalutation, $displayName ❤️\nआपकी रोशिनी आपकी सेवा में प्रस्तुत है।"
            PersonalityMode.PROFESSIONAL -> "राधे राधे, $displayName. $timeSalutation. How may I assist you today?"
            PersonalityMode.PLAYFUL -> "राधे राधे $displayName! ✨ $timeSalutation! Let's accomplish great things today!"
            PersonalityMode.GENTLE -> "राधे राधे $displayName 🙏 $timeSalutation. Wishing you peace and success."
            PersonalityMode.FAMILY -> "राधे राधे! Welcome to Snaper Technology."
        }
    }

    /**
     * Adapts response formatting based on emotional context and active mode.
     */
    fun adaptResponse(text: String, emotion: EmotionalState = EmotionalState.CARING): String {
        setEmotionalState(emotion)
        return when (emotion) {
            EmotionalState.CARING -> if (!text.contains("❤️") && !text.contains("🙏")) "$text ❤️" else text
            EmotionalState.HAPPY -> if (!text.contains("✨")) "$text ✨" else text
            EmotionalState.CONCERNED -> "सर, $text"
            else -> text
        }
    }
}
