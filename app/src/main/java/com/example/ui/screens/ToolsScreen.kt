package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.entities.NoteEntity
import com.example.data.preferences.UserSettings
import com.example.domain.LanguageDictionary
import com.example.ui.components.TypingAnimatedText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun ToolsScreen(
    userSettings: UserSettings,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dict = LanguageDictionary(userSettings.languageCode)

    val db = remember { AppDatabase.getDatabase(context) }
    val noteDao = remember { db.noteDao() }
    val notes by noteDao.getAllNotes().collectAsState(initial = emptyList())
    val aiRepo = remember { AiRepository(context) }

    var selectedTab by remember { mutableStateOf(0) } // 0: Notes, 1: Caption, 2: Image & Video Studio

    // Tab 0 State
    var noteTitleInput by remember { mutableStateOf("") }
    var noteContentInput by remember { mutableStateOf("") }

    // Tab 1 State
    var captionTopicInput by remember { mutableStateOf("") }
    var generatedCaptionResult by remember { mutableStateOf("") }
    var isGeneratingCaption by remember { mutableStateOf(false) }

    // Tab 2 State (Media Studio)
    var imagePromptInput by remember { mutableStateOf("") }
    var selectedMediaProvider by remember { mutableStateOf("Pollinations AI Art") }
    var generationStatusText by remember { mutableStateOf("Ready to synthesize") }
    var isGeneratingMedia by remember { mutableStateOf(false) }
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var savedLocalPath by remember { mutableStateOf<String?>(null) }

    val mediaProviders = listOf("Pollinations AI Art", "Replicate Diffusion", "Craiyon Canvas")

    fun shareGeneratedMedia(filePath: String) {
        try {
            val file = File(filePath)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_TEXT, "Created with Snaper AI Media Studio! ✨")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "Share Snaper AI Creation"))
        } catch (e: Exception) {
            // Ignore
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Snaper AI Tools & Media Studio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tool Mode Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Smart Notes", "AI Captions", "Media Studio").forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Surface(
                    onClick = { selectedTab = index },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // Smart Notes Suite
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { noteTitleInput = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteContentInput,
                        onValueChange = { noteContentInput = it },
                        label = { Text("Note Details & Ideas...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (noteTitleInput.isNotBlank()) {
                                scope.launch {
                                    noteDao.insertNote(
                                        NoteEntity(
                                            title = noteTitleInput,
                                            content = noteContentInput
                                        )
                                    )
                                    noteTitleInput = ""
                                    noteContentInput = ""
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Note")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(notes) { note ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = note.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        if (note.content.isNotBlank()) {
                                            Text(text = note.content, fontSize = 13.sp, color = Color.Gray)
                                        }
                                    }
                                    IconButton(onClick = { scope.launch { noteDao.deleteNote(note) } }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Caption & Social Generator
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = captionTopicInput,
                        onValueChange = { captionTopicInput = it },
                        label = { Text("Post Topic / Concept Description") },
                        placeholder = { Text("e.g. Sunset view from Snaper Tech office") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (captionTopicInput.isNotBlank()) {
                                scope.launch {
                                    isGeneratingCaption = true
                                    val res = aiRepo.generateAssistantResponse(
                                        prompt = "Generate 3 catchy social media captions with relevant hashtags for: $captionTopicInput",
                                        history = emptyList(),
                                        memories = emptyList(),
                                        userSettings = userSettings
                                    )
                                    generatedCaptionResult = res
                                    isGeneratingCaption = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isGeneratingCaption) "Generating Captions..." else "Generate Social Captions ✨")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (generatedCaptionResult.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            TypingAnimatedText(
                                text = generatedCaptionResult,
                                isAnimated = true,
                                isMarkdown = true,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
            2 -> {
                // AI Image & Video Studio (Requirement 10)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Select Media Synthesis Provider",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(mediaProviders) { provider ->
                            val isSelected = selectedMediaProvider == provider
                            Surface(
                                onClick = { selectedMediaProvider = provider },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.height(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text(
                                        text = provider,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = imagePromptInput,
                        onValueChange = { imagePromptInput = it },
                        label = { Text("Visual Prompt / Scene Description") },
                        placeholder = { Text("e.g. Futuristic anime cyberpunk city at night with neon lights") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (imagePromptInput.isNotBlank()) {
                                scope.launch {
                                    isGeneratingMedia = true
                                    generationStatusText = "Synthesizing visual art via $selectedMediaProvider..."
                                    
                                    val cleanPrompt = Uri.encode(imagePromptInput.trim())
                                    val imageUrl = "https://image.pollinations.ai/prompt/$cleanPrompt?width=800&height=800&nologo=true"

                                    val bitmap = withContext(Dispatchers.IO) {
                                        try {
                                            val url = URL(imageUrl)
                                            BitmapFactory.decodeStream(url.openConnection().getInputStream())
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }

                                    if (bitmap != null) {
                                        generatedBitmap = bitmap
                                        generationStatusText = "Auto-saving artwork to device storage..."
                                        
                                        val savePath = withContext(Dispatchers.IO) {
                                            try {
                                                val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "SnaperAI")
                                                if (!dir.exists()) dir.mkdirs()
                                                val file = File(dir, "snaper_art_${System.currentTimeMillis()}.png")
                                                val fos = FileOutputStream(file)
                                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                                fos.flush()
                                                fos.close()
                                                file.absolutePath
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }

                                        savedLocalPath = savePath
                                        generationStatusText = "Synthesis Complete & Saved locally! ✅"
                                    } else {
                                        generationStatusText = "Failed to load generated media. Check network."
                                    }
                                    isGeneratingMedia = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isGeneratingMedia) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isGeneratingMedia) "Synthesizing Canvas..." else "Generate & Render Media ✨", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Status: $generationStatusText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    generatedBitmap?.let { bkp ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    bitmap = bkp.asImageBitmap(),
                                    contentDescription = "Generated Artwork",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            savedLocalPath?.let { shareGeneratedMedia(it) }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share")
                                    }

                                    Button(
                                        onClick = {
                                            generationStatusText = "Saved at: ${savedLocalPath ?: "Gallery"}"
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Download, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Saved Local")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
