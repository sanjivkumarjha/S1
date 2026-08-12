package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcontrol.FloatingOverlayWindowManager
import com.example.devicecare.SmartSystemMonitorManager
import com.example.media.BackgroundAudioManager
import com.example.service.AssistantAccessibilityService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiTaskingControlScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val systemMonitor = remember { SmartSystemMonitorManager.getInstance(context) }
    val audioManager = remember { BackgroundAudioManager.getInstance(context) }
    val floatingWindowManager = remember { FloatingOverlayWindowManager.getInstance(context) }

    val batteryLevel by systemMonitor.batteryLevel.collectAsState()
    val isCharging by systemMonitor.isCharging.collectAsState()
    val isCablePlugged by systemMonitor.isCablePlugged.collectAsState()
    val isSwitchOffWarning by systemMonitor.isSwitchOffWarning.collectAsState()
    val isNetworkConnected by systemMonitor.isNetworkConnected.collectAsState()
    val lastAlertMessage by systemMonitor.lastAlertMessage.collectAsState()

    val isAudioPlaying by audioManager.isPlaying.collectAsState()
    val currentTrack by audioManager.currentTrack.collectAsState()
    val audioStatusText by audioManager.statusText.collectAsState()
    val isOverlayShowing by floatingWindowManager.isOverlayShowing.collectAsState()

    var statusFeedback by remember { mutableStateOf("System Monitor & Multi-Tasking active.") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "⚡ System & Audio Multi-Tasking",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        containerColor = Color(0xFF090D16)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Status Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF3B82F6))),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "STATUS FEEDBACK",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusFeedback,
                            color = Color(0xFF10B981),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (lastAlertMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Latest Alert: $lastAlertMessage",
                                color = Color(0xFFF59E0B),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // SECTION 1: SMART BATTERY, CABLE & NETWORK DETECTOR
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.BatteryChargingFull,
                                    contentDescription = "Battery",
                                    tint = if (batteryLevel < 15) Color.Red else Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Battery & Cable Monitor",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isNetworkConnected) Color(0xFF10B981).copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (isNetworkConnected) "Online 🌐" else "Offline ⚠️",
                                    color = if (isNetworkConnected) Color(0xFF10B981) else Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Level", color = Color.Gray, fontSize = 12.sp)
                                Text(
                                    text = "$batteryLevel%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Column {
                                Text(text = "Status", color = Color.Gray, fontSize = 12.sp)
                                Text(
                                    text = if (isCharging) "Charging ⚡" else if (isCablePlugged) "Cable Plugged 🔌" else "Discharging 🔋",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        if (isSwitchOffWarning) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Red.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = Color.Red
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Boss, cable is plugged in, but switch is off! Please turn on power.",
                                        color = Color.Red,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 2: BACKGROUND MUSIC & MULTI-TASKING AUDIO ENGINE
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Audio Engine",
                                tint = Color(0xFF8B5CF6)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Background Music & Smart Focus",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Current Stream: ${currentTrack?.title ?: "Select Track"}",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Status: $audioStatusText • Smart Ducking Active",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { audioManager.togglePlayPause() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play"
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isAudioPlaying) "Pause Stream" else "Play Stream")
                            }

                            Button(
                                onClick = { audioManager.stop() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Preset Background Streams:",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        audioManager.presetTracks.forEach { track ->
                            val isCurrent = currentTrack?.id == track.id
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) Color(0xFF8B5CF6).copy(alpha = 0.2f) else Color(0xFF0F172A),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) Color(0xFF8B5CF6) else Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = track.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = track.subtitle,
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    IconButton(onClick = { audioManager.playTrack(track) }) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircleFilled,
                                            contentDescription = "Play",
                                            tint = Color(0xFF8B5CF6)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SECTION 3: FLOATING WINDOW & SPLIT-SCREEN AUTOMATION
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Floating Window",
                                tint = Color(0xFF3B82F6)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Split-Screen & Floating Overlay",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Floating Overlay Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Floating AI Bubble",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isOverlayShowing) "Active on top of apps" else "Inactive",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    if (isOverlayShowing) {
                                        floatingWindowManager.removeFloatingBubble()
                                        statusFeedback = "Floating bubble removed."
                                    } else {
                                        if (floatingWindowManager.canDrawOverlays()) {
                                            floatingWindowManager.showFloatingAssistantBubble()
                                            statusFeedback = "Floating bubble launched!"
                                        } else {
                                            context.startActivity(floatingWindowManager.getOverlayPermissionIntent())
                                            statusFeedback = "Opening System Overlay Permission settings..."
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (isOverlayShowing) "Remove" else "Enable Bubble")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Split-Screen Launcher
                        Button(
                            onClick = {
                                val service = AssistantAccessibilityService.getInstance()
                                if (service != null) {
                                    val done = service.launchSplitScreenWithApps(
                                        "com.whatsapp",
                                        "com.google.android.youtube",
                                        context
                                    )
                                    statusFeedback = if (done) "Launched Split Screen (WhatsApp + YouTube)!" else "Toggled split-screen view."
                                } else {
                                    context.startActivity(AssistantAccessibilityService.openAccessibilitySettingsIntent())
                                    statusFeedback = "Enable Accessibility Service in settings to execute split-screen commands."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.PictureInPicture, contentDescription = "Split Screen")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Launch Split-Screen Mode (WhatsApp + YouTube)")
                        }
                    }
                }
            }
        }
    }
}
