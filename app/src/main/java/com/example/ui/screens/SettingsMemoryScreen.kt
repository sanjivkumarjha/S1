package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.entities.MemoryEntity
import kotlinx.coroutines.launch

import com.example.data.preferences.UserSettings
import com.example.domain.memory.MemoryImportManager

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.voice.VoiceAssistantManager
import com.example.domain.memory.MemoryCategory
import com.example.domain.memory.ScannedFileInfo

@Composable
fun SettingsMemoryScreen(
    userSettings: UserSettings = UserSettings(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val db = remember { AppDatabase.getDatabase(context) }
    val memoryDao = remember { db.memoryDao() }
    val memories by memoryDao.getAllMemories().collectAsState(initial = emptyList())
    val memoryImportManager = remember { MemoryImportManager(context) }
    val voiceManager = remember { VoiceAssistantManager(context) }

    var keyInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var confirmationStatusMessage by remember { mutableStateOf("") }

    // Real File Pickers using Android Storage Activity Result Contracts
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val fileInfos = uris.map { uri ->
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Document_${System.currentTimeMillis()}"
                    ScannedFileInfo(
                        uri = uri,
                        name = fileName,
                        sizeBytes = 1024L * 50,
                        mimeType = "application/octet-stream",
                        category = MemoryCategory.DOCUMENTS
                    )
                }
                memoryImportManager.importFiles(fileInfos, skipDuplicates = false)
                confirmationStatusMessage = "Successfully indexed ${uris.size} document file(s) into long memory."
                voiceManager.speak("Successfully indexed ${uris.size} document files into long memory.")
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val fileInfos = uris.map { uri ->
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Photo_${System.currentTimeMillis()}"
                    ScannedFileInfo(
                        uri = uri,
                        name = fileName,
                        sizeBytes = 1024L * 200,
                        mimeType = "image/*",
                        category = MemoryCategory.PHOTOS
                    )
                }
                memoryImportManager.importFiles(fileInfos, skipDuplicates = false)
                confirmationStatusMessage = "Successfully imported ${uris.size} photo(s)/media item(s) for long term vision indexing."
                voiceManager.speak("Successfully imported ${uris.size} photo items for long memory indexing.")
            }
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
                .padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Long-Term Local Memory & Indexing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Persistent 5+ Year Retention • Multi-Format Learning",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Multi-format upload & Link Memory Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Multi-Format Learning & Link Indexing", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Upload or link Google Drive, Web URLs, Photos, Videos, or ZIP archives for autonomous indexing:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            try {
                                docPickerLauncher.launch("*/*")
                            } catch (e: Exception) {
                                keyInput = "Google Drive / Web Link"
                                contentInput = "https://drive.google.com/file/d/sample_snaper_project_archive.zip"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Pick Document / Drive File", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            try {
                                imagePickerLauncher.launch("image/*")
                            } catch (e: Exception) {
                                keyInput = "Media Photo Index"
                                contentInput = "Photo_Gallery_Memory.jpg"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Pick Gallery / ZIP Photos", fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = keyInput,
            onValueChange = { keyInput = it },
            label = { Text("Topic / Memory Key (e.g. Favorite Drink)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = contentInput,
            onValueChange = { contentInput = it },
            label = { Text("Memory Fact (e.g. Prefers Iced Green Tea)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (keyInput.isNotBlank() && contentInput.isNotBlank()) {
                    scope.launch {
                        val msg = memoryImportManager.saveTextMemory(
                            key = keyInput,
                            content = contentInput,
                            category = "preference",
                            ownerName = userSettings.ownerName,
                            ownerTitle = userSettings.ownerTitle
                        )
                        confirmationStatusMessage = msg
                        keyInput = ""
                        contentInput = ""
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Store Memory Fact")
        }

        if (confirmationStatusMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x308B5CF6)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = confirmationStatusMessage,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Stored Local Memories (${memories.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(memories) { mem ->
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
                            Text(
                                text = mem.key,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = mem.content,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = {
                            scope.launch { memoryDao.deleteMemory(mem) }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}
