package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.data.api.AiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.MemoryEntity
import com.example.data.preferences.UserSettings
import com.example.domain.AssistantOrchestrator
import com.example.domain.LanguageDictionary
import com.example.domain.OfflineCommandHandler
import com.example.domain.RadheRadheGreetingManager
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun AiChatScreen(
    userSettings: UserSettings,
    initialPrompt: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dict = LanguageDictionary(userSettings.languageCode)

    val db = remember { AppDatabase.getDatabase(context) }
    val chatDao = remember { db.chatMessageDao() }
    val memoryDao = remember { db.memoryDao() }
    val aiRepo = remember { AiRepository(context) }
    val orchestrator = remember { AssistantOrchestrator(context) }
    val voiceManager = remember { VoiceAssistantManager(context) }

    val messages by chatDao.getAllMessages().collectAsState(initial = emptyList())
    val memories by memoryDao.getAllMemories().collectAsState(initial = emptyList())
    val spokenText by voiceManager.spokenText.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }

    // Synchronize voice speech result to inputText if captured
    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank()) {
            inputText = spokenText
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
                val bytes = outputStream.toByteArray()
                selectedImageBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Process initial prompt if passed
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            inputText = initialPrompt
        }
    }

    val offlineHandler = remember { OfflineCommandHandler(context) }

    fun sendMessage() {
        val query = inputText.trim()
        if (query.isEmpty() && selectedImageBase64 == null) return

        val userMessage = ChatMessageEntity(
            sender = "user",
            content = if (query.isNotBlank()) query else "[Image Attached]"
        )

        inputText = ""
        val attachedImg = selectedImageBase64
        selectedImageBase64 = null

        scope.launch {
            chatDao.insertMessage(userMessage)
            isLoading = true

            var activeAttachedImg = attachedImg
            if (userSettings.isScreenVisionEnabled && activeAttachedImg.isNullOrBlank()) {
                activeAttachedImg = com.example.vision.ScreenVisionManager.getInstance(context).captureLiveScreenBase64()
            }

            // Route through AssistantOrchestrator
            val assistantResponse = if (!activeAttachedImg.isNullOrBlank()) {
                aiRepo.generateAssistantResponse(
                    prompt = "[Real-Time Screen Vision Active: Analyze live screen context if applicable]\n" + userMessage.content,
                    history = messages,
                    memories = memories,
                    userSettings = userSettings,
                    attachedImageBase64 = activeAttachedImg
                )
            } else {
                orchestrator.processQuery(
                    query = userMessage.content,
                    userId = "owner",
                    history = messages,
                    userSettings = userSettings
                )
            }

            val assistantMessage = ChatMessageEntity(
                sender = "assistant",
                content = assistantResponse
            )

            chatDao.insertMessage(assistantMessage)
            isLoading = false

            // Auto Speak if enabled
            if (userSettings.isAutoListenEnabled) {
                voiceManager.speak(assistantResponse)
            }
        }
    }


    ChatScreen(
        messages = messages,
        isLoading = isLoading,
        inputText = inputText,
        onInputTextChange = { inputText = it },
        onSendMessage = { sendMessage() },
        selectedImageBase64 = selectedImageBase64,
        onAttachImageClick = { imagePickerLauncher.launch("image/*") },
        onRemoveImageClick = { selectedImageBase64 = null },
        onVoiceInputClick = { voiceManager.startListening(userSettings.languageCode) },
        onSpeakMessage = { text -> voiceManager.speak(text) },
        onBookmarkMessage = { text ->
            scope.launch {
                memoryDao.insertMemory(
                    MemoryEntity(
                        category = "interaction",
                        key = "Chat Saved Fact",
                        content = text
                    )
                )
            }
        },
        onNavigateBack = onNavigateBack,
        assistantName = userSettings.assistantName.ifBlank { "Snaper AI" },
        aiModelInfo = "${userSettings.aiProvider.displayName} (${userSettings.selectedModel})",
        placeholderHint = dict.getString("type_message_hint")
    )
}
