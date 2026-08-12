package com.example.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.domain.branding.ProtectedBranding
import kotlinx.coroutines.launch

/**
 * Home Screen Layout Drag & Drop Reordering Editor.
 * Allows users to reorder home screen widgets and save custom order.
 */
@Composable
fun HomeScreenLayoutEditorCard(
    userSettings: UserSettings,
    repo: UserPreferencesRepository
) {
    val scope = rememberCoroutineScope()

    val sectionNames = mapOf(
        "RADHE_WIDGET" to "राधे राधे Animated Widget (${ProtectedBranding.PROTECTED_GREETING})",
        "CLOCK_WIDGET" to "Global Live Clock Widget",
        "WEATHER_WIDGET" to "Weather & Live Updates",
        "CONTROL_BANNER" to "Snaper Control Center Banner",
        "HERO_ASSISTANT" to "Hero Voice Assistant Canvas",
        "QUICK_TOOLS" to "Quick Tools & Intelligence",
        "ASK_SNAPER" to "Ask Snaper AI Prompts"
    )

    var currentOrder by remember {
        mutableStateOf(
            userSettings.homeScreenLayoutOrder.split(",").filter { it.isNotBlank() }
        )
    }

    var isEmojiRotationEnabled by remember { mutableStateOf(userSettings.homeGreetingEmojiEnabled) }
    var emojiFrequency by remember { mutableStateOf(userSettings.homeGreetingEmojiFrequency) }
    var statusMsg by remember { mutableStateOf("Reorder sections using controls below.") }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_layout_editor_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Reorder Layout",
                        tint = LocalGlassAccent.current.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Home Screen Drag & Reorder Layout",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Rearrange Home Screen widgets & customize Radhe greeting",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Radhe Radhe Protected Emoji Settings
            SettingSwitchRow(
                title = "Radhe Radhe Dynamic Emoji Rotation",
                subtitle = "Automatically update greeting emoji based on mood/time",
                icon = Icons.Default.Mood,
                checked = isEmojiRotationEnabled,
                onCheckedChange = {
                    isEmojiRotationEnabled = it
                    scope.launch { repo.setHomeGreetingEmojiEnabled(it) }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text("Emoji Update Frequency", fontSize = 12.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("AUTOMATIC", "SLOW", "NORMAL", "FREQUENT").forEach { freq ->
                    GlassChip(
                        text = freq,
                        isSelected = emojiFrequency == freq,
                        onClick = {
                            emojiFrequency = freq
                            scope.launch { repo.setHomeGreetingEmojiFrequency(freq) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Widget Order List", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                currentOrder.forEachIndexed { index, itemKey ->
                    val displayName = sectionNames[itemKey] ?: itemKey
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1F2232),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (itemKey == "RADHE_WIDGET") Color(0xFFFF2D55).copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.DragIndicator,
                                    contentDescription = "Drag",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = displayName,
                                    fontSize = 12.5.sp,
                                    color = Color.White,
                                    fontWeight = if (itemKey == "RADHE_WIDGET") FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Row {
                                // Move Up
                                IconButton(
                                    onClick = {
                                        if (index > 0) {
                                            val mutable = currentOrder.toMutableList()
                                            val temp = mutable[index]
                                            mutable[index] = mutable[index - 1]
                                            mutable[index - 1] = temp
                                            currentOrder = mutable
                                        }
                                    },
                                    enabled = index > 0,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Move Up",
                                        tint = if (index > 0) Color.White else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Move Down
                                IconButton(
                                    onClick = {
                                        if (index < currentOrder.size - 1) {
                                            val mutable = currentOrder.toMutableList()
                                            val temp = mutable[index]
                                            mutable[index] = mutable[index + 1]
                                            mutable[index + 1] = temp
                                            currentOrder = mutable
                                        }
                                    },
                                    enabled = index < currentOrder.size - 1,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Move Down",
                                        tint = if (index < currentOrder.size - 1) Color.White else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(statusMsg, fontSize = 11.5.sp, color = Color(0xFF34C759))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            repo.resetHomeScreenLayout()
                            currentOrder = listOf("RADHE_WIDGET", "CLOCK_WIDGET", "WEATHER_WIDGET", "CONTROL_BANNER", "HERO_ASSISTANT", "QUICK_TOOLS", "ASK_SNAPER")
                            statusMsg = "Home screen layout reset to default."
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset Layout", fontSize = 12.sp, color = Color.White)
                }

                Button(
                    onClick = {
                        scope.launch {
                            val newOrderStr = currentOrder.joinToString(",")
                            repo.setHomeScreenLayoutOrder(newOrderStr)
                            statusMsg = "Home Screen layout order saved! ✅"
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalGlassAccent.current.color),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Layout", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
