package com.example.domain.emoji

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

enum class EmojiAnimationStyle {
    BOUNCE, PULSE, HEARTBEAT, WOBBLE, FLOAT, AOD_MINIMAL_PULSE, AOD_SLOW_DRIFT, STATIC
}

object EmojiAnimationEngine {

    @Composable
    fun Modifier.animateEmoji(
        style: EmojiAnimationStyle = EmojiAnimationStyle.BOUNCE,
        isAodMode: Boolean = false,
        isBatterySaver: Boolean = false
    ): Modifier {
        if (style == EmojiAnimationStyle.STATIC || (isAodMode && !isAodModeAllowed(style))) {
            return this
        }

        val activeStyle = if (isAodMode || isBatterySaver) {
            when (style) {
                EmojiAnimationStyle.HEARTBEAT -> EmojiAnimationStyle.AOD_MINIMAL_PULSE
                else -> EmojiAnimationStyle.AOD_SLOW_DRIFT
            }
        } else {
            style
        }

        val infiniteTransition = rememberInfiniteTransition(label = "EmojiAnimation")

        return when (activeStyle) {
            EmojiAnimationStyle.BOUNCE -> {
                val translateY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bounce"
                )
                val scaleValue by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                this.graphicsLayer {
                    translationY = translateY
                    scaleX = scaleValue
                    scaleY = scaleValue
                }
            }
            EmojiAnimationStyle.PULSE -> {
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.92f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )
                this.scale(scale)
            }
            EmojiAnimationStyle.HEARTBEAT -> {
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.22f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "heartbeat"
                )
                this.scale(scale)
            }
            EmojiAnimationStyle.WOBBLE -> {
                val rotation by infiniteTransition.animateFloat(
                    initialValue = -10f,
                    targetValue = 10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "wobble"
                )
                this.graphicsLayer { rotationZ = rotation }
            }
            EmojiAnimationStyle.FLOAT -> {
                val floatY by infiniteTransition.animateFloat(
                    initialValue = -6f,
                    targetValue = 6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "float"
                )
                this.graphicsLayer { translationY = floatY }
            }
            EmojiAnimationStyle.AOD_MINIMAL_PULSE -> {
                val aodPulse by infiniteTransition.animateFloat(
                    initialValue = 0.98f,
                    targetValue = 1.04f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "aodPulse"
                )
                this.scale(aodPulse)
            }
            EmojiAnimationStyle.AOD_SLOW_DRIFT -> {
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.75f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 2500, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "aodDrift"
                )
                this.alpha(alpha)
            }
            EmojiAnimationStyle.STATIC -> this
        }
    }

    private fun isAodModeAllowed(style: EmojiAnimationStyle): Boolean {
        return style == EmojiAnimationStyle.AOD_MINIMAL_PULSE || style == EmojiAnimationStyle.AOD_SLOW_DRIFT
    }
}
