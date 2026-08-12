package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.preferences.UserPreferencesRepository
import com.example.service.AssistantForegroundService
import com.example.util.EnsureNotificationPermissionEffect
import com.example.util.PermissionHelper
import com.example.voice.VoiceAssistantService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantIdentityScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val userSettings by prefsRepo.userSettingsFlow.collectAsState(initial = null)

    // Automatically request POST_NOTIFICATIONS (Android 13+) before any foreground service
    // that posts a notification is started, so enabling the listening engine does not crash.
    EnsureNotificationPermissionEffect(active = true)

    var assistantName by remember(userSettings) { mutableStateOf(userSettings?.assistantName ?: "Snaper") }
    var wakePhrase by remember(userSettings) { mutableStateOf(userSettings?.wakePhrase ?: "Hey Snaper") }

    val presetNames = listOf("Sultan", "Jarvis", "Rocky", "Snaper", "Roshni", "Friday", "Nova", "Alexa")

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Microphone Permission Granted! 🎙️", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Microphone Access Required for Background Voice Listening", Toast.LENGTH_LONG).show()
        }
    }

    // Helper: start the always-on voice engine only when the required runtime permissions
    // (POST_NOTIFICATIONS for the FGS notification + RECORD_AUDIO for the mic) are granted.
    fun startAlwaysOnVoiceSafely(): Boolean {
        if (!PermissionHelper.hasRecordAudio(context)) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return false
        }
        if (!PermissionHelper.hasPostNotifications(context)) {
            Toast.makeText(
                context,
                "Please allow Notifications to run the background voice engine.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return VoiceAssistantService.start(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assistant Identity & Always-On Engine", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Banner Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Custom Assistant Name & Dynamic Wake Word",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Customize your assistant's wake name (e.g., 'Sultan', 'Jarvis', 'Rocky', 'Snaper'). The listening engine will continuously capture audio in the background and respond immediately.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            // Input Fields
            OutlinedTextField(
                value = assistantName,
                onValueChange = { assistantName = it },
                label = { Text("Assistant Name") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Quick Name & Wake-Word Presets", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetNames.take(4).forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                assistantName = preset
                                wakePhrase = "Hey $preset"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(preset, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetNames.drop(4).forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                assistantName = preset
                                wakePhrase = "Hey $preset"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(preset, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = wakePhrase,
                onValueChange = { wakePhrase = it },
                label = { Text("Configured Wake Phrase") },
                leadingIcon = { Icon(Icons.Default.Mic, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (assistantName.isNotBlank() && wakePhrase.isNotBlank()) {
                        coroutineScope.launch {
                            prefsRepo.updateAssistantIdentity(assistantName.trim(), wakePhrase.trim())
                            startAlwaysOnVoiceSafely()
                            Toast.makeText(context, "Saved Identity & Updated Wake Word: '$assistantName' ✨", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Assistant Identity & Wake Word", fontWeight = FontWeight.Bold)
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // System Permission & Background Execution Cards
            Text("Always-On Background Engine Permissions", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            val powerManager = remember { context.getSystemService(android.content.Context.POWER_SERVICE) as? PowerManager }
            val isBatteryIgnoring = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
            val isMicGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            val isOverlayGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("1. Battery Optimization Bypass", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(if (isBatteryIgnoring) "✅ Active" else "⚠️ Action Needed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Prevents Android OS from killing background voice listening when the screen is locked.", fontSize = 12.sp)
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Please disable battery optimization in System Settings", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isBatteryIgnoring) "Unrestricted Battery Granted" else "Request Unrestricted Battery Access", fontSize = 12.sp)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("2. Background Microphone Access", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(if (isMicGranted) "✅ Active" else "⚠️ Action Needed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Required for 24/7 continuous wake-word listening.", fontSize = 12.sp)
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                Toast.makeText(context, "Microphone Permission Already Granted! ✅", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isMicGranted) "Microphone Granted" else "Grant Microphone Access", fontSize = 12.sp)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("3. Display Over Other Apps (Dynamic Island)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(if (isOverlayGranted) "✅ Active" else "⚠️ Action Needed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Allows the Dynamic Island system overlay to display above other apps and lock screen.", fontSize = 12.sp)
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isOverlayGranted) "Overlay Granted" else "Grant Overlay Permission", fontSize = 12.sp)
                    }
                }
            }

            Button(
                onClick = {
                    val voiceStarted = startAlwaysOnVoiceSafely()
                    val assistantServiceStarted = if (PermissionHelper.hasPostNotifications(context)) {
                        AssistantForegroundService.startService(context, "Always-On Voice Listening Active")
                    } else {
                        false
                    }
                    val message = if (voiceStarted || assistantServiceStarted) {
                        "Voice Foreground Service Started! 🚀"
                    } else {
                        "Please grant microphone and notification permissions first."
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start / Restart Always-On Listening Service", fontWeight = FontWeight.Bold)
            }
        }
    }
}
