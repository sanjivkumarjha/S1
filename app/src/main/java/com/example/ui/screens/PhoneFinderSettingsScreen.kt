package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.security.FindMyAssistantManager
import com.example.security.FinderConfidence
import com.example.security.PhoneFinderDiagnosticReport
import com.example.ui.glass.*
import kotlinx.coroutines.launch

@Composable
fun PhoneFinderSettingsScreen(
    userSettings: UserSettings,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val findManager = remember { FindMyAssistantManager.getInstance(context) }

    val finderHistory by findManager.finderHistory.collectAsState()
    val lastSeenContext by findManager.lastSeenContext.collectAsState()
    val isRinging by findManager.isRinging.collectAsState()
    val findingStatusText by findManager.findingStatusText.collectAsState()

    var statusMessage by remember { mutableStateOf("Phone Finder System Normal") }
    var diagnosticReport by remember { mutableStateOf<PhoneFinderDiagnosticReport?>(null) }
    var isDiagnosticRunning by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    DynamicLiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            GlassTopBar(
                title = "Phone Finder Settings",
                subtitle = "“Where Are You?” Device Location Awareness",
                navigationIcon = {
                    GlassIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        onClick = onNavigateBack
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Master Toggle Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                accentColor = if (userSettings.isPhoneFinderEnabled) LocalGlassAccent.current.color else Color.Gray
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Phone Finder Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (userSettings.isPhoneFinderEnabled) "Active • Voice & sensor recognition enabled" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = userSettings.isPhoneFinderEnabled,
                        onCheckedChange = { checked ->
                            scope.launch { prefsRepo.setPhoneFinderEnabled(checked) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feature Controls List
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Phone Finder Capabilities", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Configure voice detection, last seen memory, and local privacy.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                // Toggle 1: Voice Detection
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Voice “Where are you?” Detection", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Triggers on phrases like “तुम कहाँ हो?”, “Where are you?”", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = userSettings.isPhoneFinderVoiceDetectionEnabled,
                        onCheckedChange = { checked ->
                            scope.launch { prefsRepo.setPhoneFinderVoiceDetectionEnabled(checked) }
                        }
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))

                // Toggle 2: Last Seen Context
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Last-Seen Context Memory", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Remembers last observed room, surface & orientation", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = userSettings.isLastSeenContextEnabled,
                        onCheckedChange = { checked ->
                            scope.launch { prefsRepo.setLastSeenContextEnabled(checked) }
                        }
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))

                // Toggle 3: Environmental Recognition
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Environmental Surface Recognition", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Analyzes ambient light, proximity & motion multi-signals", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = userSettings.isEnvironmentalRecognitionEnabled,
                        onCheckedChange = { checked ->
                            scope.launch { prefsRepo.setEnvironmentalRecognitionEnabled(checked) }
                        }
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))

                // Toggle 4: Dynamic Island Integration
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic Island Status Display", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Shows searching & location status inside Dynamic Island overlay", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = userSettings.isPhoneFinderDynamicIslandEnabled,
                        onCheckedChange = { checked ->
                            scope.launch { prefsRepo.setPhoneFinderDynamicIslandEnabled(checked) }
                        }
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))

                // Toggle 5: Local Processing Preference
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Strict Local Processing", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("100% local sensor & audio evaluation with zero cloud dependency", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = userSettings.isPhoneFinderLocalProcessingOnly,
                        onCheckedChange = { checked ->
                            scope.launch { prefsRepo.setPhoneFinderLocalProcessingOnly(checked) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Test & Live Location Estimation Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Live Location Estimation & Ring Test", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Simulate or trigger real-time location estimation using owner name: ${userSettings.ownerName}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                val liveEval = remember {
                    findManager.calculateSensorConfidence(
                        ownerName = userSettings.ownerName,
                        ownerTitle = userSettings.ownerTitle
                    )
                }

                Text("Current Estimate: ${liveEval.locationName}", fontWeight = FontWeight.Bold, color = LocalGlassAccent.current.color)
                Text("Response: “${liveEval.hindiText}”", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("Technical: ${liveEval.details}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassButton(
                        text = if (isRinging) "Stop Sound" else "Test Finding Workflow 🔊",
                        onClick = {
                            if (isRinging) {
                                findManager.stopRinging()
                                statusMessage = "Finding sound stopped."
                            } else {
                                findManager.startPhoneFindingWorkflow(
                                    ownerName = userSettings.ownerName,
                                    ownerTitle = userSettings.ownerTitle
                                ) { res ->
                                    statusMessage = res.hindiText
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step-by-Step Diagnostic Verification Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Diagnostic Step-by-Step Verification", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Verifies permissions, sensors, camera vision, local pipeline & Dynamic Island.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                GlassButton(
                    text = if (isDiagnosticRunning) "Running Diagnostic..." else "Run Phone Finder Diagnostic 🛠️",
                    onClick = {
                        isDiagnosticRunning = true
                        diagnosticReport = findManager.runPhoneFinderDiagnostic(context)
                        isDiagnosticRunning = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                diagnosticReport?.let { report ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (report.overallPass) "DIAGNOSTIC STATUS: ALL SYSTEMS PASSED ✅" else "DIAGNOSTIC STATUS: PARTIAL CHECKS PASSED ⚠️",
                        fontWeight = FontWeight.Bold,
                        color = if (report.overallPass) Color(0xFF10B981) else Color(0xFFF59E0B)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    report.stepsLog.forEach { step ->
                        Text("• $step", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Last Seen Context Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("Last Seen Context Snapshot", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text("• Last Room: ${lastSeenContext.room}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("• Last Surface: ${lastSeenContext.surface}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("• Orientation: ${lastSeenContext.orientation}", style = MaterialTheme.typography.bodySmall, color = Color.White)
                Text("• Charging Status: ${if (lastSeenContext.isCharging) "Charging Active ⚡" else "Unplugged"}", style = MaterialTheme.typography.bodySmall, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Finder History Log Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Phone Finder Activity Log", fontWeight = FontWeight.Bold, color = Color.White)
                    if (finderHistory.isNotEmpty()) {
                        TextButton(onClick = { findManager.clearFinderHistory() }) {
                            Text("Clear", color = LocalGlassAccent.current.color)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (finderHistory.isEmpty()) {
                    Text("No finder queries logged yet.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                } else {
                    finderHistory.take(5).forEach { log ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("Query: \"${log.query}\"", fontWeight = FontWeight.SemiBold, color = Color.White)
                            Text("Location: ${log.locationName} (${log.confidence.name})", style = MaterialTheme.typography.bodySmall, color = LocalGlassAccent.current.color)
                            Text("Reply: ${log.hindiResponseText}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalGlassAccent.current.color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
