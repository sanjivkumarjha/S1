package com.example.communication

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.SystemClock
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Human-Centric UI Interaction Pacing Engine.
 * Formulates realistic interaction intervals, subtle gesture curve variations,
 * and ergonomic touch gestures to ensure reliable, smooth Accessibility UI automation.
 */
object HumanInteractionPacingEngine {

    /**
     * Calculates a natural human-like delay before triggering next UI action.
     * @param baseMs Base expected delay in milliseconds.
     * @param varianceMs Random variance range.
     */
    suspend fun applyNaturalPacingDelay(baseMs: Long = 300L, varianceMs: Long = 150L) {
        val randomJitter = Random.nextLong(-varianceMs, varianceMs)
        val finalDelay = (baseMs + randomJitter).coerceAtLeast(80L)
        delay(finalDelay)
    }

    /**
     * Generates a curved, natural scroll/swipe gesture path.
     */
    fun createHumanScrollGesture(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 250L
    ): GestureDescription {
        val path = Path().apply {
            moveTo(startX, startY)
            // Add subtle mid-point curve deviation for ergonomic gesture arc
            val controlX = (startX + endX) / 2f + Random.nextFloat() * 20f - 10f
            val controlY = (startY + endY) / 2f + Random.nextFloat() * 15f - 7.5f
            quadTo(controlX, controlY, endX, endY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        return GestureDescription.Builder().addStroke(stroke).build()
    }

    /**
     * Simulates natural text input pacing per character string.
     */
    suspend fun executeNaturalTypingSimulation(text: String, onCharTyped: suspend (String) -> Unit) {
        val currentSb = StringBuilder()
        for (char in text) {
            currentSb.append(char)
            onCharTyped(currentSb.toString())
            // Variable typing delay between 30ms and 90ms per character
            val charDelay = Random.nextLong(30L, 90L)
            delay(charDelay)
        }
    }
}
