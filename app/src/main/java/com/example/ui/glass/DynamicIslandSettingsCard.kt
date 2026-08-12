package com.example.ui.glass

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.domain.app.ActionVisualResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DynamicIslandSettingsCard(
    userSettings: UserSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { UserPreferencesRepository(context) }

    var previewState by remember { mutableStateOf(DynamicIslandState.COLLAPSED) }
    var previewActionResult by remember { mutableStateOf<ActionVisualResult?>(null) }
    var isPreviewActive by remember { mutableStateOf(false) }

    fun triggerPreview(state: DynamicIslandState, actionRes: ActionVisualResult? = null) {
        scope.launch {
            previewActionResult = actionRes
            previewState = state
            isPreviewActive = true
            delay(4000)
            previewState = DynamicIslandState.COLLAPSED
            isPreviewActive = false
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_island_settings_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Live Interactive Preview Box
            AnimatedVisibility(
                visible = isPreviewActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✨ Dynamic Island Live Preview",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    GlossyDynamicIsland(
                        userSettings = userSettings,
                        islandState = previewState,
                        actionResult = previewActionResult,
                        isDeviceLocked = false,
                        onExpandToggle = { },
                        onUnlockRequest = { }
                    )
                }
            }

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF00F2FE).copy(alpha = 0.18f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF00F2FE),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Dynamic Island & AOD",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Primary Glossy Assistant Visual Engine",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }

                Switch(
                    checked = userSettings.isDynamicIslandEnabled,
                    onCheckedChange = { checked ->
                        scope.launch { repo.setDynamicIslandEnabled(checked) }
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF8B5CF6))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Preview Buttons
            Text(
                text = "Interactive Previews",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        triggerPreview(
                            DynamicIslandState.ACTION_PREVIEW,
                            ActionVisualResult(
                                emoji = "🔦",
                                actionTitle = "Flashlight Action",
                                statusText = "Turning on Flashlight...",
                                iconVector = Icons.Default.FlashlightOn
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🔦 Action", fontSize = 11.5.sp)
                }

                Button(
                    onClick = {
                        triggerPreview(DynamicIslandState.EXPANDED)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F2FE)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("✨ Expand", fontSize = 11.5.sp, color = Color.Black)
                }

                Button(
                    onClick = {
                        triggerPreview(DynamicIslandState.DYNAMIC_ISLAND_AOD)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🌙 AOD", fontSize = 11.5.sp, color = Color.White)
                }

                Button(
                    onClick = {
                        triggerPreview(DynamicIslandState.IDLE_SLEEPING)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("😴 Sleep", fontSize = 11.5.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Switches List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // UPDATE 1: System Display Coexistence
                SettingSwitchRow(
                    title = "System AOD Protection (Coexistence)",
                    subtitle = "Never disable manufacturer's native AOD (Realme, Samsung, Xiaomi, etc.)",
                    icon = Icons.Default.Lock,
                    checked = userSettings.isSystemAodProtectionEnforced,
                    onCheckedChange = { }
                )

                SettingSwitchRow(
                    title = "System Dynamic Island Protection",
                    subtitle = "Coexist seamlessly with native OS status bars & display features",
                    icon = Icons.Default.Smartphone,
                    checked = userSettings.isSystemIslandProtectionEnforced,
                    onCheckedChange = { }
                )

                // UPDATE 2: Assistant Idle Sleeping State
                SettingSwitchRow(
                    title = "Assistant Idle Sleeping Mode",
                    subtitle = "Show gentle sleeping emoji (😴) when resting/idle",
                    icon = Icons.Default.Mood,
                    checked = userSettings.isIdleSleepingEnabled,
                    onCheckedChange = { scope.launch { repo.setIdleSleepingEnabled(it) } }
                )

                SettingSwitchRow(
                    title = "Wake Animation & Greeting",
                    subtitle = "Smooth wake-up transition on 'Hi Snap' or voice trigger",
                    icon = Icons.Default.AutoAwesome,
                    checked = userSettings.isWakeAnimationEnabled,
                    onCheckedChange = { scope.launch { repo.setWakeAnimationEnabled(it) } }
                )

                // UPDATE 3: Camera & Vision Integration
                SettingSwitchRow(
                    title = "Voice Camera & Photo Capture",
                    subtitle = "Front/Rear camera voice commands & AI vision processing",
                    icon = Icons.Default.CameraAlt,
                    checked = userSettings.isCameraControlEnabled,
                    onCheckedChange = { scope.launch { repo.setCameraControlEnabled(it) } }
                )

                SettingSwitchRow(
                    title = "Action & App Open Previews",
                    subtitle = "Show animated previews before executing commands",
                    icon = Icons.Default.PlayArrow,
                    checked = userSettings.isActionPreviewEnabled,
                    onCheckedChange = { scope.launch { repo.setActionPreviewEnabled(it) } }
                )

                SettingSwitchRow(
                    title = "Animated Emoji System",
                    subtitle = "3,954+ scalable Unicode emojis with motion",
                    icon = Icons.Default.Mood,
                    checked = userSettings.isAnimatedEmojiEnabled,
                    onCheckedChange = { scope.launch { repo.setAnimatedEmojiEnabled(it) } }
                )

                SettingSwitchRow(
                    title = "Lock Screen Dynamic Island",
                    subtitle = "Show Island on lock screen with security unlock prompt",
                    icon = Icons.Default.Lock,
                    checked = userSettings.isLockScreenIslandEnabled,
                    onCheckedChange = { scope.launch { repo.setLockScreenIslandEnabled(it) } }
                )

                SettingSwitchRow(
                    title = "Always-On Display (AOD) Mode",
                    subtitle = "Minimal OLED/AMOLED display integration",
                    icon = Icons.Default.Smartphone,
                    checked = userSettings.isAodIntegrationEnabled,
                    onCheckedChange = { scope.launch { repo.setAodIntegrationEnabled(it) } }
                )

                SettingSwitchRow(
                    title = "Camera Mood Detection",
                    subtitle = "Estimate mood locally from facial features (Privacy safe)",
                    icon = Icons.Default.CameraAlt,
                    checked = userSettings.isCameraMoodEnabled,
                    onCheckedChange = {
                        scope.launch { repo.setCameraMoodEnabled(it) }
                        if (it) {
                            Toast.makeText(context, "Camera Mood Detection enabled (Local & Privacy-Safe)", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                SettingSwitchRow(
                    title = "Battery Saver Mode",
                    subtitle = "Reduce animation frequency on AOD & background",
                    icon = Icons.Default.BatterySaver,
                    checked = userSettings.isBatterySaverModeEnabled,
                    onCheckedChange = { scope.launch { repo.setBatterySaverModeEnabled(it) } }
                )
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
