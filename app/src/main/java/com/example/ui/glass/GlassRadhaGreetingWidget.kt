package com.example.ui.glass

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.branding.ProtectedBranding
import com.example.domain.mood.MoodManager
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * Animated "राधे राधे" Home Screen Widget with Glossy Design System.
 * 
 * Implements permanently protected text "राधे राधे" with intelligent,
 * mood-aware and time-aware dynamic emoji updates.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GlassRadhaGreetingWidget(
    modifier: Modifier = Modifier,
    isEmojiAutoUpdateEnabled: Boolean = true,
    updateFrequency: String = "AUTOMATIC",
    onClick: (() -> Unit)? = null
) {
    val protectedText = ProtectedBranding.PROTECTED_GREETING // Always "राधे राधे"

    var sanatanState by remember { mutableStateOf(SanatanTimeEmojiEngine.getCurrentSanatanEmojis()) }

    // Infinite breathing glow animation for glossy look
    val infiniteTransition = rememberInfiniteTransition(label = "radha_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_pulse"
    )

    // Automatic coroutine time-based emoji update loop
    LaunchedEffect(isEmojiAutoUpdateEnabled) {
        if (!isEmojiAutoUpdateEnabled) return@LaunchedEffect
        while (true) {
            sanatanState = SanatanTimeEmojiEngine.getCurrentSanatanEmojis()
            delay(10000L)
        }
    }

    val accentColor = Color(0xFFFF2D55) // Crimson Pink Glass Accent

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF16101E).copy(alpha = 0.82f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    accentColor.copy(alpha = glowAlpha + 0.3f),
                    Color(0xFFA259FF).copy(alpha = 0.25f),
                    accentColor.copy(alpha = 0.10f)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .scale(scalePulse)
            .testTag("radha_radhe_animated_widget")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Ambient Liquid Glass Background Glow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = sanatanState.leftEmoji,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        // PERMANENT IMMUTABLE TEXT: "राधे राधे"
                        Text(
                            text = protectedText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.2.sp
                        )

                        Text(
                            text = "${sanatanState.slotName} • Snaper Devotional AI",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }

                // Dynamic Time-Based Emoji Right Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        text = "${sanatanState.rightEmoji} 🙏🏻",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
