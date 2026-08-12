package com.example.domain.mood

import com.example.domain.emoji.EmojiAnimationStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AssistantMood(
    val emoji: String,
    val moodName: String,
    val description: String,
    val animationStyle: EmojiAnimationStyle
) {
    HAPPY("😊", "Happy", "Positive & enthusiastic", EmojiAnimationStyle.BOUNCE),
    SAD("😢", "Sad", "Sympathetic & comforting", EmojiAnimationStyle.FLOAT),
    CRYING("😭", "Crying", "Emotional & compassionate", EmojiAnimationStyle.WOBBLE),
    EXCITED("🤩", "Excited", "High energy & ready", EmojiAnimationStyle.BOUNCE),
    LOVING("🥰", "Loving", "Warm & caring", EmojiAnimationStyle.HEARTBEAT),
    CALM("🧘", "Calm", "Peaceful & serene", EmojiAnimationStyle.FLOAT),
    WORRIED("😟", "Worried", "Attentive & cautious", EmojiAnimationStyle.PULSE),
    ANGRY("😠", "Angry", "Determined & protective", EmojiAnimationStyle.WOBBLE),
    NEUTRAL("😐", "Neutral", "Ready for assistance", EmojiAnimationStyle.PULSE)
}

object MoodManager {

    private val _moodFlow = MutableStateFlow(AssistantMood.HAPPY)
    val moodFlow: StateFlow<AssistantMood> = _moodFlow.asStateFlow()

    private var currentMood: AssistantMood = AssistantMood.HAPPY

    fun getMood(): AssistantMood = currentMood

    fun setMood(mood: AssistantMood) {
        currentMood = mood
        _moodFlow.value = mood
    }

    fun deriveMoodFromText(text: String): AssistantMood {
        val lower = text.lowercase()
        return when {
            lower.contains("happy") || lower.contains("great") || lower.contains("awesome") || lower.contains("yay") -> AssistantMood.HAPPY
            lower.contains("love") || lower.contains("sweet") || lower.contains("heart") -> AssistantMood.LOVING
            lower.contains("sad") || lower.contains("sorry") || lower.contains("upset") -> AssistantMood.SAD
            lower.contains("cry") || lower.contains("terrible") -> AssistantMood.CRYING
            lower.contains("angry") || lower.contains("mad") || lower.contains("stop") -> AssistantMood.ANGRY
            lower.contains("calm") || lower.contains("relax") || lower.contains("peace") -> AssistantMood.CALM
            lower.contains("worry") || lower.contains("scared") || lower.contains("problem") -> AssistantMood.WORRIED
            lower.contains("wow") || lower.contains("amazing") || lower.contains("excited") -> AssistantMood.EXCITED
            else -> AssistantMood.NEUTRAL
        }
    }

    fun estimateMoodFromCameraFeatures(brightness: Float, motionDelta: Float): AssistantMood {
        return when {
            motionDelta > 15.0f -> AssistantMood.EXCITED
            brightness > 0.7f -> AssistantMood.HAPPY
            brightness < 0.2f -> AssistantMood.CALM
            else -> AssistantMood.NEUTRAL
        }
    }
}
