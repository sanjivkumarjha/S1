package com.example.ui.glass

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class GlassAccentColor(val color: Color, val displayName: String) {
    PURPLE(Color(0xFFA259FF), "Purple"),
    PINK(Color(0xFFFF2D55), "Pink"),
    BLUE(Color(0xFF007AFF), "Blue"),
    CYAN(Color(0xFF30D158), "Cyan"),
    GREEN(Color(0xFF34C759), "Green"),
    ORANGE(Color(0xFFFF9500), "Orange"),
    RED(Color(0xFFFF3B30), "Red"),
    GOLD(Color(0xFFFFD700), "Gold")
}

enum class GlassThemeMode {
    LIGHT, DARK, AMOLED
}

val LocalGlassAccent = staticCompositionLocalOf { GlassAccentColor.PURPLE }
val LocalGlassThemeMode = staticCompositionLocalOf { GlassThemeMode.DARK }

/**
 * Android 17 Liquid Glass dynamic animated background canvas.
 * Creates a multi-tone translucent glass atmosphere with soft refractions, ambient light pulses, and liquid gradients.
 */
@Composable
fun DynamicLiquidGlassBackground(
    modifier: Modifier = Modifier,
    themeMode: GlassThemeMode = LocalGlassThemeMode.current,
    accentColor: Color = LocalGlassAccent.current.color,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_bg_anim")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_light_pulse"
    )

    val secondaryColor = Color(0xFFFF2D55) // Liquid Pink
    val tertiaryColor = Color(0xFF007AFF)  // Deep Cyan Blue

    val bgColors = when (themeMode) {
        GlassThemeMode.LIGHT -> listOf(
            Color(0xFFF8FAFC),
            accentColor.copy(alpha = 0.14f),
            secondaryColor.copy(alpha = 0.09f),
            Color(0xFFE2E8F0)
        )
        GlassThemeMode.AMOLED -> listOf(
            Color(0xFF000000),
            accentColor.copy(alpha = 0.22f),
            secondaryColor.copy(alpha = 0.16f),
            Color(0xFF07080D)
        )
        GlassThemeMode.DARK -> listOf(
            Color(0xFF0A0C14),
            accentColor.copy(alpha = 0.28f),
            secondaryColor.copy(alpha = 0.20f),
            tertiaryColor.copy(alpha = 0.16f),
            Color(0xFF121524)
        )
    }

    val brush = Brush.radialGradient(
        colors = bgColors,
        center = Offset(220f + animProgress * 380f, 280f + animProgress * 500f),
        radius = 1400f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush)
    ) {
        content()
    }
}

/**
 * Liquid Glass typography style helpers with specular reflective sheen and legibility glows.
 */
object LiquidGlassTypography {
    val titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = (-0.5).sp
    )
    val titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = (-0.2).sp
    )
    val bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
    val labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
}

