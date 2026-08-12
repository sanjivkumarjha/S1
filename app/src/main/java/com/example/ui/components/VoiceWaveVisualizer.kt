package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VoiceWaveVisualizer(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 180.dp,
    amplitude: Float = 0.5f,
    accentColor: Color = Color(0xFF8B5CF6)
) {
    val transition = rememberInfiniteTransition(label = "wave_anim")

    val pulseScale1 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val pulseScale2 by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = (size.width / 3f) * (0.8f + amplitude * 0.4f)

        // Outer Ring
        drawCircle(
            color = accentColor.copy(alpha = 0.25f),
            radius = baseRadius * pulseScale2,
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )

        // Middle Ring
        drawCircle(
            color = accentColor.copy(alpha = 0.45f),
            radius = baseRadius * pulseScale1,
            center = center,
            style = Stroke(width = 4.dp.toPx())
        )

        // Core Glowing Sphere
        drawCircle(
            color = accentColor.copy(alpha = 0.85f),
            radius = baseRadius * 0.7f,
            center = center
        )
    }
}
