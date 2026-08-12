package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay

/**
 * A composable component that renders text with a smooth typewriter/typing animation.
 * Features:
 * - Adaptive character stepping based on text length for consistent typing speed
 * - Blinking cursor effect (▌) while typing
 * - Optional Markdown formatting integration
 * - Smooth resume when streaming response text updates
 */
@Composable
fun TypingAnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    isAnimated: Boolean = true,
    isMarkdown: Boolean = true,
    typingDelayMs: Long = 14L,
    showCursor: Boolean = true,
    textAlign: TextAlign = TextAlign.Start,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onTypingFinished: (() -> Unit)? = null
) {
    if (text.isEmpty()) return

    if (!isAnimated) {
        if (isMarkdown) {
            MarkdownCodeView(text = text, modifier = modifier)
        } else {
            Text(
                text = text,
                style = style,
                color = color,
                textAlign = textAlign,
                modifier = modifier
            )
        }
        return
    }

    var visibleLength by remember { mutableIntStateOf(0) }
    var isTypingComplete by remember { mutableStateOf(false) }

    // Cursor pulse animation
    val cursorAlpha = remember { Animatable(1f) }
    LaunchedEffect(isTypingComplete, showCursor) {
        if (!isTypingComplete && showCursor) {
            cursorAlpha.animateTo(
                targetValue = 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 350, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    LaunchedEffect(text, isAnimated) {
        if (!isAnimated) {
            visibleLength = text.length
            isTypingComplete = true
            return@LaunchedEffect
        }

        if (visibleLength >= text.length) {
            isTypingComplete = true
            return@LaunchedEffect
        }

        isTypingComplete = false

        // Dynamic char step for natural typing cadence
        val step = when {
            text.length > 600 -> 6
            text.length > 300 -> 4
            text.length > 100 -> 2
            else -> 1
        }

        while (visibleLength < text.length) {
            delay(typingDelayMs)
            visibleLength = (visibleLength + step).coerceAtMost(text.length)
        }

        isTypingComplete = true
        onTypingFinished?.invoke()
    }

    val currentText = text.take(visibleLength)
    val cursorChar = if (!isTypingComplete && showCursor) " ▌" else ""
    val displayText = currentText + cursorChar

    if (isMarkdown) {
        MarkdownCodeView(
            text = displayText,
            modifier = modifier
        )
    } else {
        Text(
            text = displayText,
            style = style,
            color = color,
            textAlign = textAlign,
            modifier = modifier
        )
    }
}
