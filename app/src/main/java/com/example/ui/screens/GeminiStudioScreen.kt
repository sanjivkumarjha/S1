package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserSettings
import com.example.domain.AudioResult
import com.example.domain.GeminiAdvancedFeaturesEngine
import com.example.domain.GroundedResponse
import com.example.domain.HighThinkingResult
import com.example.domain.ImageResult
import com.example.domain.TaskComplexity
import com.example.domain.VideoResult
import kotlinx.coroutines.launch

@Composable
fun GeminiStudioScreen(
    userSettings: UserSettings,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geminiEngine = remember { GeminiAdvancedFeaturesEngine(context) }

    var selectedCategory by remember { mutableStateOf(0) }
    // 0: Search & Maps Grounding
    // 1: Studio Quality Image & Aspect Control
    // 2: Lyria Music & Audio Transcription
    // 3: Veo Video Generation
    // 4: Live Voice & Low-Latency
    // 5: High Thinking & Complex Reasoning

    var promptInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var outputText by remember { mutableStateOf("") }

    // Grounding
    var groundingSources by remember { mutableStateOf<List<String>>(emptyList()) }

    // Image Config
    var selectedResolution by remember { mutableStateOf("1K") }
    var selectedAspectRatio by remember { mutableStateOf("16:9") }
    var generatedImageResult by remember { mutableStateOf<ImageResult?>(null) }

    // Audio & Music
    var generatedAudioResult by remember { mutableStateOf<AudioResult?>(null) }

    // Video
    var generatedVideoResult by remember { mutableStateOf<VideoResult?>(null) }

    // Thinking
    var thinkingProcessText by remember { mutableStateOf("") }

    val resolutions = listOf("512px", "1K", "2K", "4K")
    val aspectRatios = listOf("1:1", "2:3", "3:2", "3:4", "4:3", "9:16", "16:9", "21:9")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Gemini AI Next-Gen Studio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Search, Maps, 4K Images, Lyria Music, Veo Video & Thinking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Switcher
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val categories = listOf(
                "Search & Maps",
                "4K Image Studio",
                "Lyria Music",
                "Veo Video",
                "Live Voice & Speed",
                "High Thinking Mode"
            )
            items(categories.size) { idx ->
                val isSelected = selectedCategory == idx
                Surface(
                    onClick = { selectedCategory = idx },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.height(38.dp)
                ) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = categories[idx],
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = promptInput,
            onValueChange = { promptInput = it },
            label = { Text("Enter prompt / instructions") },
            placeholder = {
                Text(
                    when (selectedCategory) {
                        0 -> "e.g. Find latest tech news on AI or nearest cafes near India Gate"
                        1 -> "e.g. Studio portrait of a futuristic cyberpunk developer cat in neon grid"
                        2 -> "e.g. Generate a 30s uplifting acoustic guitar melody"
                        3 -> "e.g. Cinematic video of a glowing drone flying over Delhi skyline at dusk"
                        4 -> "e.g. Hello Snaper, talk to me in live voice mode!"
                        else -> "e.g. Solve quantum physics algorithm with step-by-step reasoning"
                    }
                )
            },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedCategory) {
            0 -> {
                // Search & Maps Grounding
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val res = geminiEngine.searchGroundedQuery(promptInput.ifBlank { "What are today's top artificial intelligence developments?" }, userSettings)
                                outputText = res.text
                                groundingSources = res.sources
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Google Search", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val res = geminiEngine.mapsGroundedQuery(promptInput.ifBlank { "Top rated IT hubs and software parks in New Delhi" }, userSettings)
                                outputText = res.text
                                groundingSources = res.sources
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Google Maps", fontSize = 11.sp)
                    }
                }
            }

            1 -> {
                // Image Studio & Aspect Control
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Resolution & Quality", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(resolutions) { res ->
                            Surface(
                                onClick = { selectedResolution = res },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedResolution == res) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(res, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp)
                            }
                        }
                    }

                    Text("Aspect Ratio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(aspectRatios) { ratio ->
                            Surface(
                                onClick = { selectedAspectRatio = ratio },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedAspectRatio == ratio) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(ratio, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 11.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val res = geminiEngine.generateHighQualityImage(
                                    prompt = promptInput.ifBlank { "Futuristic Snaper Technology AI holographic avatar" },
                                    resolution = selectedResolution,
                                    aspectRatio = selectedAspectRatio,
                                    userSettings = userSettings
                                )
                                generatedImageResult = res
                                outputText = res.textResponse
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate $selectedResolution ($selectedAspectRatio) Image")
                    }
                }
            }

            2 -> {
                // Lyria Music Generation
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val res = geminiEngine.generateMusicTrack(promptInput.ifBlank { "Upbeat cyberpunk ambient background track" }, isFullTrack = false, userSettings = userSettings)
                                generatedAudioResult = res
                                outputText = res.textResponse
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lyria 30s Clip", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val res = geminiEngine.generateMusicTrack(promptInput.ifBlank { "Full length orchestral cinematic score for software presentation" }, isFullTrack = true, userSettings = userSettings)
                                generatedAudioResult = res
                                outputText = res.textResponse
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lyria Pro Track", fontSize = 11.sp)
                    }
                }
            }

            3 -> {
                // Veo Video Generation
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { selectedAspectRatio = "16:9" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Landscape 16:9", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { selectedAspectRatio = "9:16" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Portrait 9:16", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val res = geminiEngine.generateVeoVideo(
                                    prompt = promptInput.ifBlank { "A high-speed neon sports car driving through futuristic digital matrix" },
                                    aspectRatio = selectedAspectRatio,
                                    userSettings = userSettings
                                )
                                generatedVideoResult = res
                                outputText = res.statusText
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Movie, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Veo 3.1 Video ($selectedAspectRatio)")
                    }
                }
            }

            4 -> {
                // Live Voice & Low Latency
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                outputText = geminiEngine.processLiveVoiceConversation(promptInput.ifBlank { "Hello Snaper! Start live voice streaming turn." }, userSettings)
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gemini Live Voice", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                outputText = geminiEngine.fastLowLatencyResponse(promptInput.ifBlank { "Give me a 1-sentence fast greeting!" }, userSettings)
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Low Latency Flash Lite", fontSize = 11.sp)
                    }
                }
            }

            else -> {
                // High Thinking Mode
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            val res = geminiEngine.thinkWithHighLevel(
                                prompt = promptInput.ifBlank { "Explain the mathematical proof of prime number distribution and quantum encryption in detail." },
                                userSettings = userSettings
                            )
                            outputText = res.answer
                            thinkingProcessText = res.thinkingProcess
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Activate Gemini 3.1 Pro HIGH Thinking Mode")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Processing via Gemini AI Engine...", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (thinkingProcessText.isNotBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🧠 Inner Thinking Process (High Thinking Level):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(thinkingProcessText, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                            }
                        }
                    }
                }

                if (generatedImageResult?.base64Image != null) {
                    item {
                        val base64Str = generatedImageResult?.base64Image ?: ""
                        val decodedBitmap = remember(base64Str) {
                            try {
                                val bytes = Base64.decode(base64Str, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            } catch (e: Exception) {
                                null
                            }
                        }

                        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                if (decodedBitmap != null) {
                                    Image(
                                        bitmap = decodedBitmap.asImageBitmap(),
                                        contentDescription = "Generated Image",
                                        modifier = Modifier.fillMaxWidth().height(220.dp)
                                    )
                                } else {
                                    Text("Image render note: Base64 stream received.", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                if (groundingSources.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("📍 Grounded Real-Time Sources & Places:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                groundingSources.forEach { src ->
                                    Text("• $src", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }

                if (outputText.isNotBlank()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Gemini Response Output:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(outputText, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
