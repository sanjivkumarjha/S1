package com.example.ui.glass

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserSettings
import com.example.domain.app.ActionVisualResult
import com.example.domain.emoji.EmojiAnimationEngine.animateEmoji
import com.example.domain.emoji.EmojiAnimationStyle
import com.example.domain.mood.MoodManager
import kotlinx.coroutines.delay

object SanatanTimeEmojiEngine {
    data class TimeEmojiState(
        val leftEmoji: String,
        val rightEmoji: String,
        val slotName: String
    )

    fun getCurrentSanatanEmojis(): TimeEmojiState {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)

        return when (hour) {
            in 5..11 -> {
                // Morning (05:00 AM - 12:00 PM): Lotus, dawn, and prayer emojis (🌅, 🪷, 🕉️)
                val rotate = (minute / 2) % 3
                val left = if (rotate == 0) "🌅" else if (rotate == 1) "🪷" else "🕉️"
                val right = if (rotate == 0) "🪷" else if (rotate == 1) "🕉️" else "🌅"
                TimeEmojiState(left, right, "Pratah - Morning")
            }
            in 12..15 -> {
                // Afternoon (12:00 PM - 04:00 PM): Flag and sun flare emojis (🚩, ✨, 📿)
                val rotate = (minute / 2) % 3
                val left = if (rotate == 0) "🚩" else if (rotate == 1) "✨" else "📿"
                val right = if (rotate == 0) "✨" else if (rotate == 1) "📿" else "🚩"
                TimeEmojiState(left, right, "Madhyahna - Afternoon")
            }
            in 16..19 -> {
                // Evening / Sandhya (04:00 PM - 08:00 PM): Diya, beads, and evening glow (🪔, 📿, ✨)
                val rotate = (minute / 2) % 3
                val left = if (rotate == 0) "🪔" else if (rotate == 1) "📿" else "✨"
                val right = if (rotate == 0) "📿" else if (rotate == 1) "✨" else "🪔"
                TimeEmojiState(left, right, "Sandhya - Evening")
            }
            else -> {
                // Night / Dinner Time (08:00 PM - 05:00 AM): Moon, sacred symbols, and peaceful elements (🌙, 🕉️, 🪷)
                val rotate = (minute / 2) % 3
                val left = if (rotate == 0) "🌙" else if (rotate == 1) "🕉️" else "🪷"
                val right = if (rotate == 0) "🕉️" else if (rotate == 1) "🪷" else "🌙"
                TimeEmojiState(left, right, "Ratri - Night")
            }
        }
    }
}

enum class DynamicIslandState {
    COLLAPSED,
    EXPANDED,
    LISTENING,
    PROCESSING,
    RESPONDING,
    ACTION_PREVIEW,
    APP_PREVIEW,
    MOOD_DETECTED,
    DYNAMIC_ISLAND_AOD,
    IDLE_SLEEPING,
    WAKE_UP
}

@Composable
fun GlossyDynamicIsland(
    userSettings: UserSettings,
    islandState: DynamicIslandState,
    actionResult: ActionVisualResult?,
    isDeviceLocked: Boolean,
    onExpandToggle: () -> Unit,
    onUnlockRequest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!userSettings.isDynamicIslandEnabled) return

    val context = LocalContext.current
    val networkManager = remember { com.example.domain.network.NetworkOptimizationManager.getInstance(context) }
    val vehicleManager = remember { com.example.domain.vehicle.VehicleConnectivityManager.getInstance(context) }
    val securityManager = remember { com.example.security.CentralizedSecurityManager.getInstance(context) }
    val activeSecurityAlert by securityManager.activeAlert.collectAsState()
    val networkQuality by networkManager.networkQualityFlow.collectAsState()
    val isOnline by networkManager.isOnlineFlow.collectAsState()
    val vehicles by vehicleManager.vehiclesFlow.collectAsState()
    val primaryVehicle = vehicles.find { it.isPrimary } ?: vehicles.firstOrNull()

    val currentMood by MoodManager.moodFlow.collectAsState(initial = MoodManager.getMood())
    val impressionState by DynamicIslandImpressionController.impressionState.collectAsState()

    var sanatanEmojis by remember { mutableStateOf(SanatanTimeEmojiEngine.getCurrentSanatanEmojis()) }

    LaunchedEffect(Unit) {
        while (true) {
            sanatanEmojis = SanatanTimeEmojiEngine.getCurrentSanatanEmojis()
            delay(10000L)
        }
    }

    val islandWidth by animateDpAsState(
        targetValue = when (islandState) {
            DynamicIslandState.COLLAPSED -> 175.dp
            DynamicIslandState.IDLE_SLEEPING -> 150.dp
            DynamicIslandState.WAKE_UP -> 190.dp
            DynamicIslandState.DYNAMIC_ISLAND_AOD -> 130.dp
            DynamicIslandState.EXPANDED,
            DynamicIslandState.ACTION_PREVIEW,
            DynamicIslandState.APP_PREVIEW -> 340.dp
            DynamicIslandState.LISTENING,
            DynamicIslandState.PROCESSING,
            DynamicIslandState.RESPONDING -> 310.dp
            DynamicIslandState.MOOD_DETECTED -> 220.dp
        },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "islandWidth"
    )

    val islandHeight by animateDpAsState(
        targetValue = when (islandState) {
            DynamicIslandState.COLLAPSED,
            DynamicIslandState.IDLE_SLEEPING,
            DynamicIslandState.WAKE_UP -> 38.dp
            DynamicIslandState.DYNAMIC_ISLAND_AOD -> 32.dp
            DynamicIslandState.EXPANDED -> 120.dp
            DynamicIslandState.ACTION_PREVIEW,
            DynamicIslandState.APP_PREVIEW -> 85.dp
            DynamicIslandState.LISTENING,
            DynamicIslandState.PROCESSING,
            DynamicIslandState.RESPONDING -> 68.dp
            DynamicIslandState.MOOD_DETECTED -> 48.dp
        },
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "islandHeight"
    )

    val isAod = islandState == DynamicIslandState.DYNAMIC_ISLAND_AOD

    val backgroundBrush = if (isAod) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF08080A), Color(0xFF000000))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xEE1E1B2E),
                Color(0xDD0D0B14),
                Color(0xFF000000)
            )
        )
    }

    val borderBrush = if (isAod) {
        Brush.verticalGradient(
            colors = listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0x808B5CF6),
                Color(0x3300F2FE),
                Color(0x668B5CF6)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            onClick = onExpandToggle,
            shape = RoundedCornerShape(26.dp),
            color = Color.Transparent,
            modifier = Modifier
                .width(islandWidth)
                .height(islandHeight)
                .shadow(
                    elevation = if (isAod) 2.dp else 16.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0xFF8B5CF6)
                )
                .clip(RoundedCornerShape(26.dp))
                .background(backgroundBrush)
                .border(
                    width = if (isAod) 0.8.dp else 1.5.dp,
                    brush = borderBrush,
                    shape = RoundedCornerShape(26.dp)
                )
                .testTag("glossy_dynamic_island_surface")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                when (islandState) {
                    DynamicIslandState.COLLAPSED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = sanatanEmojis.leftEmoji,
                                fontSize = 14.sp,
                                modifier = Modifier.animateEmoji(EmojiAnimationStyle.FLOAT)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = activeSecurityAlert?.title ?: "राधे राधे",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = sanatanEmojis.rightEmoji,
                                fontSize = 14.sp,
                                modifier = Modifier.animateEmoji(EmojiAnimationStyle.PULSE)
                            )
                        }
                    }

                    DynamicIslandState.IDLE_SLEEPING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Resting...",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = userSettings.sleepingEmoji,
                                fontSize = 15.sp,
                                modifier = Modifier.animateEmoji(
                                    style = EmojiAnimationStyle.AOD_MINIMAL_PULSE,
                                    isAodMode = false,
                                    isBatterySaver = userSettings.isBatterySaverModeEnabled
                                )
                            )
                        }
                    }

                    DynamicIslandState.WAKE_UP -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Awakening",
                                    tint = Color(0xFF00F2FE),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Awakening...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "👀",
                                fontSize = 16.sp,
                                modifier = Modifier.animateEmoji(EmojiAnimationStyle.BOUNCE)
                            )
                        }
                    }
                    DynamicIslandState.DYNAMIC_ISLAND_AOD -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (userSettings.isAodEmojiEnabled) {
                                Text(
                                    text = currentMood.emoji,
                                    fontSize = 13.sp,
                                    modifier = Modifier.animateEmoji(
                                        style = EmojiAnimationStyle.AOD_MINIMAL_PULSE,
                                        isAodMode = true,
                                        isBatterySaver = userSettings.isBatterySaverModeEnabled
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = "Snaper AOD",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    DynamicIslandState.LISTENING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Listening",
                                    tint = Color(0xFFFF2D55),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Listening...",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "🎙️",
                                fontSize = 16.sp,
                                modifier = Modifier.animateEmoji(EmojiAnimationStyle.PULSE)
                            )
                        }
                    }

                    DynamicIslandState.PROCESSING -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Processing",
                                    tint = Color(0xFF00F2FE),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Analyzing Request...",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "🧠",
                                fontSize = 16.sp,
                                modifier = Modifier.animateEmoji(EmojiAnimationStyle.FLOAT)
                            )
                        }
                    }

                    DynamicIslandState.ACTION_PREVIEW, DynamicIslandState.APP_PREVIEW -> {
                        val act = actionResult
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = act?.emoji ?: "✨",
                                        fontSize = 20.sp,
                                        modifier = Modifier.animateEmoji(EmojiAnimationStyle.BOUNCE)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = act?.actionTitle ?: "Assistant Preview",
                                            color = Color.White,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isDeviceLocked && act?.isAppLaunch == true) {
                                                "🔒 Unlock to open ${act.actionTitle}"
                                            } else {
                                                act?.statusText ?: "Executing action..."
                                            },
                                            color = if (isDeviceLocked && act?.isAppLaunch == true) Color(0xFFFF9500) else Color.White.copy(alpha = 0.8f),
                                            fontSize = 11.5.sp
                                        )
                                    }
                                }

                                if (isDeviceLocked && act?.isAppLaunch == true) {
                                    Surface(
                                        onClick = { onUnlockRequest(act.actionTitle) },
                                        shape = CircleShape,
                                        color = Color(0xFFFF9500).copy(alpha = 0.25f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Unlock Required",
                                            tint = Color(0xFFFF9500),
                                            modifier = Modifier
                                                .padding(6.dp)
                                                .size(16.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF34C759),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    DynamicIslandState.EXPANDED -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentMood.emoji,
                                        fontSize = 22.sp,
                                        modifier = Modifier.animateEmoji(currentMood.animationStyle)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${userSettings.assistantName} AI Engine",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Mood: ${currentMood.moodName} • ${currentMood.description}",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Text(
                                    text = "✨ Active",
                                    color = Color(0xFF00F2FE),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(
                                    text = "💡 Wake Phrase: ${userSettings.wakePhrase}",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }

                    else -> {
                        Text(
                            text = "${userSettings.assistantName} AI",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
