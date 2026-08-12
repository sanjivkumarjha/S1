package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserSettings
import com.example.domain.GreetingEngine
import com.example.domain.LanguageDictionary
import com.example.ui.components.AnimeAssistantCanvas
import com.example.ui.glass.*
import com.example.util.EnsureNotificationPermissionEffect

import com.example.ui.components.AppLogo

@Composable
fun HomeScreen(
    userSettings: UserSettings,
    onNavigateToChat: (String?) -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToVoiceVerify: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToControlCenter: (() -> Unit)? = null,
    onNavigateToCallSummaries: (() -> Unit)? = null,
    onNavigateToMultiTasking: (() -> Unit)? = null
) {
    val dict = LanguageDictionary(userSettings.languageCode)
    val scrollState = rememberScrollState()

    // Request POST_NOTIFICATIONS (Android 13+) on first home entry so background services
    // that the app starts from MainActivity can post their foreground notifications safely.
    EnsureNotificationPermissionEffect(active = userSettings.isAutoStartOnBootEnabled)

    var showPrivacyNotice by remember { mutableStateOf(true) }

    val quickPrompts = listOf(
        "How are you feeling today, Snaper?",
        "Help me plan my day & set goals",
        "Check my spam call logs",
        "Turn AC to 24 degrees",
        "सभी models से analyze करो"
    )

    var islandState by remember { mutableStateOf(DynamicIslandState.COLLAPSED) }

    DynamicLiquidGlassBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Primary Glossy Dynamic Island Visual Assistant System
                GlossyDynamicIsland(
                    userSettings = userSettings,
                    islandState = islandState,
                    actionResult = null,
                    isDeviceLocked = false,
                    onExpandToggle = {
                        islandState = if (islandState == DynamicIslandState.COLLAPSED) DynamicIslandState.EXPANDED else DynamicIslandState.COLLAPSED
                    },
                    onUnlockRequest = { }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Top Header: Smart Greeting & Settings Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(size = 46.dp, showGlow = true)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "राधे-राधे ${if (userSettings.ownerName.isNotBlank() && userSettings.ownerName != "User") userSettings.ownerName else "Sanjiv Sir"}! ✨",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = dict.getString("assistant_subtitle"),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    GlassIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = "Settings",
                        onClick = onNavigateToSettings
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Parse User Custom Section Order
                val layoutOrder = userSettings.homeScreenLayoutOrder.split(",").filter { it.isNotBlank() }

                layoutOrder.forEach { sectionKey ->
                    when (sectionKey) {
                        "RADHE_WIDGET" -> {
                            // Animated Radhe Radhe Protected Widget
                            GlassRadhaGreetingWidget(
                                isEmojiAutoUpdateEnabled = userSettings.homeGreetingEmojiEnabled,
                                updateFrequency = userSettings.homeGreetingEmojiFrequency,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        "CLOCK_WIDGET" -> {
                            GlassLiveClockWidget(modifier = Modifier.padding(bottom = 12.dp))
                        }
                        "WEATHER_WIDGET" -> {
                            GlassWeatherWidget(modifier = Modifier.padding(bottom = 12.dp))
                        }
                        "CONTROL_BANNER" -> {
                            // Snaper Control Center Launcher Banner
                            GlassCard(
                                onClick = { (onNavigateToControlCenter ?: onNavigateToTools).invoke() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .testTag("launch_control_center_card")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Dashboard,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Snaper Control Center",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Access Multi-AI, Security, Avatar, Smart Home & Tools",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "HERO_ASSISTANT" -> {
                            // Hero Anime Assistant Card
                            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    AnimeAssistantCanvas(
                                        sizeDp = 190.dp,
                                        isSpeaking = false,
                                        isListening = false,
                                        accentColor = LocalGlassAccent.current.color,
                                        onTap = { onNavigateToVoice() }
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = LocalGlassAccent.current.color.copy(alpha = 0.25f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(LocalGlassAccent.current.color)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Awaiting Your Voice, ${userSettings.ownerName}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        GlassButton(
                                            text = dict.getString("quick_chat"),
                                            icon = Icons.Default.ChatBubbleOutline,
                                            onClick = { onNavigateToChat(null) },
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            testTag = "home_quick_chat_button"
                                        )

                                        GlassButton(
                                            text = dict.getString("quick_voice"),
                                            icon = Icons.Default.Mic,
                                            onClick = onNavigateToVoice,
                                            accentColor = Color(0xFFFF2D55),
                                            modifier = Modifier.weight(1f).height(48.dp),
                                            testTag = "home_quick_voice_button"
                                        )
                                    }
                                }
                            }
                        }
                        "QUICK_TOOLS" -> {
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                Text(
                                    text = "Quick Tools & Intelligence",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    GlassCard(
                                        onClick = onNavigateToVoiceVerify,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column {
                                            Icon(
                                                imageVector = Icons.Default.Security,
                                                contentDescription = null,
                                                tint = if (userSettings.isVoiceVerified) Color(0xFF34C759) else LocalGlassAccent.current.color
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Owner Verification",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = if (userSettings.isVoiceVerified) "Verified & Unlocked ✅" else "Tap to Verify Owner",
                                                fontSize = 11.sp,
                                                color = if (userSettings.isVoiceVerified) Color(0xFF34C759) else Color.LightGray
                                            )
                                        }
                                    }

                                    GlassCard(
                                        onClick = onNavigateToTools,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column {
                                            Icon(
                                                imageVector = Icons.Default.Build,
                                                contentDescription = null,
                                                tint = LocalGlassAccent.current.color
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Tools & Media",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Notes & AI Gen",
                                                fontSize = 11.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                    }

                                    GlassCard(
                                        onClick = { onNavigateToCallSummaries?.invoke() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Call Summaries",
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 14.sp,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Hindi & Eng Logs",
                                                fontSize = 11.sp,
                                                color = Color(0xFF10B981)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "ASK_SNAPER" -> {
                            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                GlassCard(
                                    onClick = { onNavigateToMultiTasking?.invoke() },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.BatteryChargingFull,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "System & Audio Multi-Tasking",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "Battery, Cable, Music & Split Screen",
                                                    fontSize = 11.sp,
                                                    color = Color.LightGray
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = LocalGlassAccent.current.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Ask Snaper AI",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(quickPrompts) { prompt ->
                                        GlassChip(
                                            text = prompt,
                                            isSelected = false,
                                            onClick = { onNavigateToChat(prompt) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Radha Naam Jap Counter & Routine Widget
                GlassRadhaJapWidget(modifier = Modifier.padding(bottom = 12.dp))
            }
        }
    }
}
