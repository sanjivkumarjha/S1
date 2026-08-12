package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AiRepository
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserSettings
import com.example.domain.LanguageDictionary
import com.example.domain.OfflineCommandHandler
import com.example.ui.components.AnimeAssistantCanvas
import com.example.ui.components.TypingAnimatedText
import com.example.ui.components.VoiceWaveVisualizer
import com.example.ui.glass.*
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.launch

@Composable
fun VoiceAssistantScreen(
    userSettings: UserSettings,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dict = LanguageDictionary(userSettings.languageCode)

    val db = remember { AppDatabase.getDatabase(context) }
    val memories by db.memoryDao().getAllMemories().collectAsState(initial = emptyList())
    val aiRepo = remember { AiRepository(context) }

    val voiceManager = remember {
        VoiceAssistantManager(context).apply {
            setVoiceCharacteristics(userSettings.voicePitch, userSettings.voiceSpeechRate)
        }
    }

    val isSpeaking by voiceManager.isSpeaking.collectAsState()
    val isListening by voiceManager.isListening.collectAsState()
    val spokenText by voiceManager.spokenText.collectAsState()
    val amplitude by voiceManager.speechAmplitude.collectAsState()

    var assistantReplyText by remember { mutableStateOf("Tap the microphone below to speak to Snaper AI...") }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.shutdown()
        }
    }

    val offlineHandler = remember { OfflineCommandHandler(context) }

    // Process spoken query when speech finishes
    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank() && !isListening) {
            assistantReplyText = "Thinking..."
            scope.launch {
                // 1. Offline Command Check
                val offlineResult = offlineHandler.handleCommand(spokenText)
                if (offlineResult.isHandled) {
                    assistantReplyText = offlineResult.responseText
                    voiceManager.speak(offlineResult.responseText, userSettings.languageCode)
                    return@launch
                }

                // 2. Online AI Response
                val response = aiRepo.generateAssistantResponse(
                    prompt = spokenText,
                    history = emptyList(),
                    memories = memories,
                    userSettings = userSettings
                )
                assistantReplyText = response
                voiceManager.speak(response, userSettings.languageCode)
            }
        }
    }

    DynamicLiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Navigation Bar
            GlassTopBar(
                title = dict.getString("voice_title"),
                subtitle = "Liquid Glass AI Audio Brain",
                navigationIcon = {
                    GlassIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        onClick = onNavigateBack
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Center Voice Visualizer & Assistant Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                VoiceWaveVisualizer(
                    sizeDp = 240.dp,
                    amplitude = if (isSpeaking || isListening) amplitude.coerceAtLeast(0.35f) else 0.1f,
                    accentColor = LocalGlassAccent.current.color
                )

                AnimeAssistantCanvas(
                    sizeDp = 190.dp,
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    accentColor = LocalGlassAccent.current.color
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Voice Transcript Box
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (spokenText.isNotBlank()) {
                        Text(
                            text = "You said: \"$spokenText\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = LocalGlassAccent.current.color,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    TypingAnimatedText(
                        text = assistantReplyText,
                        isAnimated = assistantReplyText != "Thinking..." && !assistantReplyText.startsWith("Tap the microphone"),
                        isMarkdown = false,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Voice Control Actions
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSpeaking) {
                    FloatingActionButton(
                        onClick = { voiceManager.stopSpeaking() },
                        containerColor = Color(0xFFFF3B30),
                        contentColor = Color.White
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop Speech")
                    }
                } else {
                    FloatingActionButton(
                        onClick = {
                            if (isListening) {
                                voiceManager.stopListening()
                            } else {
                                voiceManager.startListening(userSettings.languageCode)
                            }
                        },
                        containerColor = LocalGlassAccent.current.color,
                        contentColor = Color.White,
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MANDATORY Official Company Branding
            GlassFooter()
        }
    }
}
