package com.example.ui.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.launch

@Composable
fun DoctorAndModesSettingsCard(
    userSettings: UserSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }

    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color(0x2506B6D4),
        borderColor = Color(0x5006B6D4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF06B6D4).copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = Color(0xFF06B6D4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Specialized Assistant Modes & Health",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Doctor Mode, Vehicle Driving Mode & Environment Controllers",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. DOCTOR MODE TILE
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (userSettings.isDoctorModeEnabled) Color(0x3006B6D4) else Color(0x15FFFFFF)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = Color(0xFF06B6D4),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "🩺 Doctor Mode",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Evidence-based health info, medicine precautions, symptom warnings",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Switch(
                            checked = userSettings.isDoctorModeEnabled,
                            onCheckedChange = { isChecked ->
                                scope.launch {
                                    prefsRepo.setDoctorModeEnabled(isChecked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF06B6D4)
                            )
                        )
                    }

                    AnimatedVisibility(visible = userSettings.isDoctorModeEnabled) {
                        Column(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .background(Color(0x2006B6D4), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "✨ Doctor Mode Active (Dynamic Island Badge Enabled)\n• Medical queries answer with precautions & emergency warnings\n• Voice trigger: 'Doctor Mode on' / 'Doctor Mode चालू करो'",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. VEHICLE MODE TILE
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (userSettings.isVehicleModeEnabled) Color(0x3010B981) else Color(0x15FFFFFF)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "🚗 Vehicle Mode (Driving Assistant)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Hands-free large voice buttons, auto navigation & safe audio",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Switch(
                            checked = userSettings.isVehicleModeEnabled,
                            onCheckedChange = { isChecked ->
                                scope.launch {
                                    prefsRepo.setVehicleModeEnabled(isChecked)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981)
                            )
                        )
                    }

                    AnimatedVisibility(visible = userSettings.isVehicleModeEnabled) {
                        Column(
                            modifier = Modifier
                                .padding(top = 10.dp)
                                .background(Color(0x2010B981), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "🚗 Vehicle Mode Active (Dynamic Island Badge Enabled)\n• Large hands-free voice interface for safer driving\n• Voice trigger: 'Vehicle Mode on' / 'Car Mode on'",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. WOMEN'S HEALTH & CARE MODE TILE
            ModeToggleTile(
                title = "🌸 Women's Health & Care Mode",
                subtitle = "Private, respectful support for menstrual, pregnancy & wellness topics",
                activeText = "🌸 Women's Health Mode Active\n• Private & confidential conversations\n• Extra privacy safeguards on notifications & Dynamic Island",
                iconTint = Color(0xFFEC4899),
                isChecked = userSettings.isFemaleModeEnabled,
                onCheckedChange = { scope.launch { prefsRepo.setFemaleModeEnabled(it) } }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. LEGAL / LAWYER MODE TILE
            ModeToggleTile(
                title = "⚖️ Legal / Lawyer Mode",
                subtitle = "Legal info, GST/company/ITR guidance, contract & document analysis (India-first)",
                activeText = "⚖️ Legal Mode Active\n• Jurisdiction-aware legal information\n• Never fabricates laws, sections or citations\n• Recommends a licensed lawyer for representation",
                iconTint = Color(0xFF3B82F6),
                isChecked = userSettings.isLegalModeEnabled,
                onCheckedChange = { scope.launch { prefsRepo.setLegalModeEnabled(it) } }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5. HOME AUTOMATION MODE TILE
            ModeToggleTile(
                title = "🏠 Home Automation Mode",
                subtitle = "Control configured lights, switches, plugs & scenes by voice",
                activeText = "🏠 Home Mode Active\n• Reports real device state only\n• Will not fake an action for unconfigured devices",
                iconTint = Color(0xFFF59E0B),
                isChecked = userSettings.isHomeModeEnabled,
                onCheckedChange = { scope.launch { prefsRepo.setHomeModeEnabled(it) } }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6. IT / BUSINESS AUTOMATION MODE TILE
            ModeToggleTile(
                title = "💼 IT / Business Automation Mode",
                subtitle = "Coding, invoices, customer comms, payments & deployment assistance",
                activeText = "💼 IT/Business Mode Active\n• Confirms before destructive/consequential actions\n• Verifies payments via real providers, not screenshots",
                iconTint = Color(0xFF06B6D4),
                isChecked = userSettings.isItBusinessModeEnabled,
                onCheckedChange = { scope.launch { prefsRepo.setItBusinessModeEnabled(it) } }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 7. ALL-ROUNDER MODE TILE
            ModeToggleTile(
                title = "🧠 All-Rounder Mode",
                subtitle = "Cross-domain tasks — selects the minimum relevant tools & providers",
                activeText = "🧠 All-Rounder Mode Active\n• Dynamically selects relevant tools/AI providers\n• Produces one synthesized final answer",
                iconTint = Color(0xFF8B5CF6),
                isChecked = userSettings.isAllRounderModeEnabled,
                onCheckedChange = { scope.launch { prefsRepo.setAllRounderModeEnabled(it) } }
            )
        }
    }
}

/**
 * Reusable specialist-mode toggle tile. Each toggle flips a persisted mode flag that the
 * [com.example.domain.AssistantMode] resolver reads, so the mode genuinely changes the
 * AI system prompt — not just a UI badge.
 */
@Composable
private fun ModeToggleTile(
    title: String,
    subtitle: String,
    activeText: String,
    iconTint: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) iconTint.copy(alpha = 0.18f) else Color(0x15FFFFFF)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = iconTint
                    )
                )
            }
            AnimatedVisibility(visible = isChecked) {
                Column(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .background(iconTint.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = activeText,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}
