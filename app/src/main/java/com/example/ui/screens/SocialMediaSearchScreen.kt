package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.appcontrol.*
import com.example.data.preferences.UserPreferencesRepository
import com.example.ui.glass.GlassCard
import com.example.ui.glass.LocalGlassAccent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaSearchScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val glassAccent = LocalGlassAccent.current

    val socialManager = remember { SocialMediaAutomationManager(context) }
    val videoEngine = remember { SocialVideoAnalysisEngine(context) }
    val factCheckEngine = remember { CrossPlatformFactCheckEngine(context) }
    val permissionManager = remember { SocialAutomationPermissionManager(context) }
    val userSettingsRepo = remember { UserPreferencesRepository(context) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var permissionsState by remember { mutableStateOf(permissionManager.getPermissionState()) }

    // Tab 1 States (Search & Messaging)
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf(SocialPlatform.YOUTUBE) }
    var recipientName by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var showMessageDialog by remember { mutableStateOf(false) }

    // Tab 2 States (Reel & Short Video AI Analysis)
    var reelUrlInput by remember { mutableStateOf("https://www.instagram.com/reels/") }
    var isAnalyzingVideo by remember { mutableStateOf(false) }
    var videoAnalysisResult by remember { mutableStateOf<VideoAnalysisResult?>(null) }
    var reelQuestionInput by remember { mutableStateOf("") }
    var reelAnswerText by remember { mutableStateOf("") }
    var isAnsweringReelQuestion by remember { mutableStateOf(false) }

    // Tab 3 States (Cross-Platform Fact-Checker)
    var factCheckClaimInput by remember { mutableStateOf("") }
    var selectedFactCheckPlatforms by remember {
        mutableStateOf(
            listOf(
                SocialPlatform.YOUTUBE,
                SocialPlatform.TWITTER,
                SocialPlatform.INSTAGRAM,
                SocialPlatform.FACEBOOK,
                SocialPlatform.REDDIT
            )
        )
    }
    var isCheckingFact by remember { mutableStateOf(false) }
    var factCheckReport by remember { mutableStateOf<FactCheckReport?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = glassAccent.color,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Universal Social Media & Reel AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            // Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 12.dp,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Universal Social Search", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("AI Reel Inspector", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Cross-Platform Fact-Check", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = { Text("Security & Permissions", fontWeight = FontWeight.SemiBold) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTabIndex) {
                0 -> UniversalSocialSearchTab(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedPlatform = selectedPlatform,
                    onSelectPlatform = { selectedPlatform = it },
                    onExecuteSearch = {
                        val res = socialManager.searchPlatform(selectedPlatform, searchQuery)
                        Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                    },
                    onOpenMessagingDialog = { showMessageDialog = true },
                    socialManager = socialManager
                )

                1 -> AiReelInspectorTab(
                    reelUrlInput = reelUrlInput,
                    onReelUrlChange = { reelUrlInput = it },
                    isAnalyzingVideo = isAnalyzingVideo,
                    videoAnalysisResult = videoAnalysisResult,
                    reelQuestionInput = reelQuestionInput,
                    onReelQuestionChange = { reelQuestionInput = it },
                    reelAnswerText = reelAnswerText,
                    isAnsweringQuestion = isAnsweringReelQuestion,
                    onAnalyzeVideo = {
                        coroutineScope.launch {
                            isAnalyzingVideo = true
                            val userSettings = userSettingsRepo.userSettingsFlow.firstOrNull()
                                ?: com.example.data.preferences.UserSettings()
                            videoAnalysisResult = videoEngine.analyzeReelOrVideo(reelUrlInput, userSettings)
                            isAnalyzingVideo = false
                        }
                    },
                    onAskQuestion = {
                        val result = videoAnalysisResult ?: return@AiReelInspectorTab
                        coroutineScope.launch {
                            isAnsweringReelQuestion = true
                            val userSettings = userSettingsRepo.userSettingsFlow.firstOrNull()
                                ?: com.example.data.preferences.UserSettings()
                            reelAnswerText = videoEngine.answerVideoQuestion(result, reelQuestionInput, userSettings)
                            isAnsweringReelQuestion = false
                        }
                    }
                )

                2 -> CrossPlatformFactCheckTab(
                    claimInput = factCheckClaimInput,
                    onClaimChange = { factCheckClaimInput = it },
                    selectedPlatforms = selectedFactCheckPlatforms,
                    onTogglePlatform = { p ->
                        selectedFactCheckPlatforms = if (selectedFactCheckPlatforms.contains(p)) {
                            selectedFactCheckPlatforms - p
                        } else {
                            selectedFactCheckPlatforms + p
                        }
                    },
                    isChecking = isCheckingFact,
                    factCheckReport = factCheckReport,
                    onRunFactCheck = {
                        coroutineScope.launch {
                            isCheckingFact = true
                            val userSettings = userSettingsRepo.userSettingsFlow.firstOrNull()
                                ?: com.example.data.preferences.UserSettings()
                            factCheckReport = factCheckEngine.performCrossPlatformFactCheck(
                                factCheckClaimInput,
                                selectedFactCheckPlatforms,
                                userSettings
                            )
                            isCheckingFact = false
                        }
                    }
                )

                3 -> SecurityAndPermissionsTab(
                    state = permissionsState,
                    onToggleSearch = {
                        permissionManager.updateSocialSearchPermission(it)
                        permissionsState = permissionManager.getPermissionState()
                    },
                    onToggleMessaging = {
                        permissionManager.updateAutoMessagingConsent(it)
                        permissionsState = permissionManager.getPermissionState()
                    },
                    onToggleScreenReading = {
                        permissionManager.updateScreenReadingConsent(it)
                        permissionsState = permissionManager.getPermissionState()
                    },
                    onToggleVideoAnalysis = {
                        permissionManager.updateVideoAnalysisConsent(it)
                        permissionsState = permissionManager.getPermissionState()
                    },
                    onToggleFactCheck = {
                        permissionManager.updateCrossPlatformFactCheckEnabled(it)
                        permissionsState = permissionManager.getPermissionState()
                    }
                )
            }
        }
    }

    // Direct Messaging Dialog
    if (showMessageDialog) {
        AlertDialog(
            onDismissRequest = { showMessageDialog = false },
            title = { Text("Send Message via ${selectedPlatform.displayName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = recipientName,
                        onValueChange = { recipientName = it },
                        label = { Text("Recipient Name or Phone Number") },
                        placeholder = { Text("e.g., +1234567890 or @username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("Message Text") },
                        placeholder = { Text("Enter text to send...") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val res = socialManager.sendDirectMessage(selectedPlatform, recipientName, messageText)
                        Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                        showMessageDialog = false
                    }
                ) {
                    Text("Send / Launch Chat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMessageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun UniversalSocialSearchTab(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedPlatform: SocialPlatform,
    onSelectPlatform: (SocialPlatform) -> Unit,
    onExecuteSearch: () -> Unit,
    onOpenMessagingDialog: () -> Unit,
    socialManager: SocialMediaAutomationManager
) {
    val glassAccent = LocalGlassAccent.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Universal Social Search & Dispatch",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Deep-search posts, reels, shorts, contacts, and profiles across all major social networks.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search reels, posts, topics, or profiles...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = onExecuteSearch) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Search", tint = glassAccent.color)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onExecuteSearch,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Deep Search")
                        }

                        OutlinedButton(
                            onClick = onOpenMessagingDialog,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Message")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Select Platform (11 Supported)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SocialPlatform.values()) { platform ->
                    val isSelected = platform == selectedPlatform
                    val isInstalled = socialManager.isAppInstalled(platform)

                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPlatform(platform) },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(platform.displayName)
                                if (isInstalled) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                }
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (platform) {
                                    SocialPlatform.WHATSAPP, SocialPlatform.TELEGRAM -> Icons.Default.Chat
                                    SocialPlatform.YOUTUBE -> Icons.Default.PlayArrow
                                    SocialPlatform.INSTAGRAM -> Icons.Default.CameraAlt
                                    else -> Icons.Default.Share
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        item {
            Text(
                text = "Quick Intent Actions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(
            listOf(
                Triple("Trending Reels & Shorts", "Search viral Reels across IG, YouTube & Facebook", SocialPlatform.INSTAGRAM),
                Triple("WhatsApp & Telegram Broadcast", "Search contacts and draft direct messages", SocialPlatform.WHATSAPP),
                Triple("X / Twitter Discussion Search", "Deep search viral posts & trending hashtags", SocialPlatform.TWITTER),
                Triple("Reddit Community Analysis", "Search posts, subreddits, and discussions", SocialPlatform.REDDIT)
            )
        ) { (title, subtitle, platform) ->
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelectPlatform(platform)
                        onExecuteSearch()
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(glassAccent.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = null,
                            tint = glassAccent.color
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AiReelInspectorTab(
    reelUrlInput: String,
    onReelUrlChange: (String) -> Unit,
    isAnalyzingVideo: Boolean,
    videoAnalysisResult: VideoAnalysisResult?,
    reelQuestionInput: String,
    onReelQuestionChange: (String) -> Unit,
    reelAnswerText: String,
    isAnsweringQuestion: Boolean,
    onAnalyzeVideo: () -> Unit,
    onAskQuestion: () -> Unit
) {
    val glassAccent = LocalGlassAccent.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = glassAccent.color,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Reel & Short Visual Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Paste any Reel/Short link (Instagram Reel, YouTube Short, Facebook Reel, TikTok). Gemini AI will WATCH visual frames, LISTEN TO spoken audio, detect objects, and summarize the content.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reelUrlInput,
                        onValueChange = onReelUrlChange,
                        label = { Text("Reel / Short URL") },
                        placeholder = { Text("https://www.instagram.com/reel/...") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onAnalyzeVideo,
                        enabled = !isAnalyzingVideo && reelUrlInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isAnalyzingVideo) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini AI Analyzing Visual Frames...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze Video with Gemini AI")
                        }
                    }
                }
            }
        }

        if (videoAnalysisResult != null) {
            val result = videoAnalysisResult
            val emotion = result.emotionalReaction

            if (emotion != null) {
                item {
                    val emotionColor = try {
                        Color(android.graphics.Color.parseColor(emotion.primaryEmotion.colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = emotion.primaryEmotion.emoji,
                                        fontSize = 28.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "AI Emotional Reaction",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = emotion.primaryEmotion.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = emotionColor
                                        )
                                    }
                                }

                                AssistChip(
                                    onClick = {},
                                    label = { Text("${(emotion.emotionalIntensity * 100).toInt()}% Intensity") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = emotionColor.copy(alpha = 0.15f)
                                    )
                                )
                            }

                            // AI Spoken Reaction Text
                            Card(
                                colors = CardDefaults.cardColors(containerColor = emotionColor.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, emotionColor.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "AI Voice Assistant Spoken Reaction:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = emotionColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = emotion.aiVerbalReaction,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Facial Expression & Audio Tone Analysis
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Facial Expressions",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = emotion.facialExpressionAnalysis,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Audio & Music Tone",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = emotion.audioToneAndMusic,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Voice Synthesis & Avatar Animation Parameters
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Voice Synthesis: ${emotion.voiceToneConfig.pitch}x Pitch | ${emotion.voiceToneConfig.speed}x Rate",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Avatar State: ${emotion.avatarVisualState}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = emotionColor
                                )
                            }
                        }
                    }
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text(result.platformName) },
                                leadingIcon = { Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                            Text(
                                text = "Gemini Multimodal Verified",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = glassAccent.color
                            )
                        }

                        Text(text = "Visual Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = result.summary, style = MaterialTheme.typography.bodyMedium)

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(text = "Transcribed Spoken Audio & Speech", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = result.transcript,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(text = "Detected Objects, Scenes & Actions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        FlowRowHorizontal(items = result.detectedObjectsAndActions)

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(text = "Fact-Check Assessment", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = result.factCheckVerification,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Ask AI About This Reel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "Ask specific questions about people, objects, products, or claims inside this Reel.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = reelQuestionInput,
                            onValueChange = onReelQuestionChange,
                            placeholder = { Text("e.g. Is the product shown in the video real?") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = onAskQuestion,
                            enabled = !isAnsweringQuestion && reelQuestionInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isAnsweringQuestion) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Answering...")
                            } else {
                                Text("Ask Question")
                            }
                        }

                        if (reelAnswerText.isNotBlank()) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "AI Answer:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = reelAnswerText, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CrossPlatformFactCheckTab(
    claimInput: String,
    onClaimChange: (String) -> Unit,
    selectedPlatforms: List<SocialPlatform>,
    onTogglePlatform: (SocialPlatform) -> Unit,
    isChecking: Boolean,
    factCheckReport: FactCheckReport?,
    onRunFactCheck: () -> Unit
) {
    val glassAccent = LocalGlassAccent.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = glassAccent.color,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cross-Platform Fact-Checker Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Enter any news topic, viral rumor, or claim. The AI will simultaneously search YouTube, Twitter/X, Instagram, Facebook, and Reddit to synthesize truth verification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = claimInput,
                        onValueChange = onClaimChange,
                        label = { Text("Claim or News Topic") },
                        placeholder = { Text("e.g. Viral rumor about new policy launch") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Target Platforms to Cross-Search:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(
                            listOf(
                                SocialPlatform.YOUTUBE,
                                SocialPlatform.TWITTER,
                                SocialPlatform.INSTAGRAM,
                                SocialPlatform.FACEBOOK,
                                SocialPlatform.REDDIT
                            )
                        ) { platform ->
                            val isSelected = selectedPlatforms.contains(platform)
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTogglePlatform(platform) },
                                label = { Text(platform.displayName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onRunFactCheck,
                        enabled = !isChecking && claimInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cross-Searching & Verifying...")
                        } else {
                            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verify Claim Across Platforms")
                        }
                    }
                }
            }
        }

        if (factCheckReport != null) {
            val report = factCheckReport
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Truth Rating", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            BadgeChip(
                                text = report.truthRating.label,
                                colorHex = report.truthRating.badgeColorHex
                            )
                        }

                        Divider()

                        Text(text = "Fact Synthesis Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = report.synthesisSummary, style = MaterialTheme.typography.bodyMedium)

                        Divider()

                        Text(
                            text = "Key Context Points (${report.verifiedSourcesCount} Sources Analyzed)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        report.keyContextPoints.forEach { point ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("• ", fontWeight = FontWeight.Bold, color = glassAccent.color)
                                Text(text = point, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Divider()

                        Text(text = "User Guidance & Advice", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = report.userRecommendation,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityAndPermissionsTab(
    state: SocialPermissionState,
    onToggleSearch: (Boolean) -> Unit,
    onToggleMessaging: (Boolean) -> Unit,
    onToggleScreenReading: (Boolean) -> Unit,
    onToggleVideoAnalysis: (Boolean) -> Unit,
    onToggleFactCheck: (Boolean) -> Unit
) {
    val glassAccent = LocalGlassAccent.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = glassAccent.color,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Social Automation Safeguards & Consent",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "All social media searches, message dispatches, and video frame analysis require explicit user consent and operate strictly with user visibility.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(
            listOf(
                PermissionToggleItem(
                    title = "Universal Social Media Deep Search",
                    description = "Allow deep-linking search queries on Instagram, YouTube, X, Facebook, TikTok, Reddit.",
                    isChecked = state.socialSearchEnabled,
                    onToggle = onToggleSearch
                ),
                PermissionToggleItem(
                    title = "Automated Direct Messaging Consent",
                    description = "Require explicit user button tap before launching WhatsApp or Telegram messaging intents.",
                    isChecked = state.autoMessagingConsent,
                    onToggle = onToggleMessaging
                ),
                PermissionToggleItem(
                    title = "Screen Reading & Chat Summarization",
                    description = "Grant Accessibility Service permission to read chat text and summarize messages.",
                    isChecked = state.screenReadingConsent,
                    onToggle = onToggleScreenReading
                ),
                PermissionToggleItem(
                    title = "Gemini Multimodal Video Analysis",
                    description = "Allow AI video inspector to extract visual frames and spoken audio for Reels/Shorts.",
                    isChecked = state.videoAnalysisConsent,
                    onToggle = onToggleVideoAnalysis
                ),
                PermissionToggleItem(
                    title = "Cross-Platform Fact-Check Engine",
                    description = "Enable simultaneous multi-platform query checks to verify rumors and viral news.",
                    isChecked = state.crossPlatformFactCheckEnabled,
                    onToggle = onToggleFactCheck
                )
            )
        ) { item ->
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = item.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = item.isChecked,
                        onCheckedChange = item.onToggle
                    )
                }
            }
        }
    }
}

private data class PermissionToggleItem(
    val title: String,
    val description: String,
    val isChecked: Boolean,
    val onToggle: (Boolean) -> Unit
)

@Composable
private fun FlowRowHorizontal(items: List<String>) {
    val glassAccent = LocalGlassAccent.current
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(items) { text ->
            AssistChip(
                onClick = {},
                label = { Text(text, fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = glassAccent.color.copy(alpha = 0.12f)
                )
            )
        }
    }
}

@Composable
private fun BadgeChip(text: String, colorHex: String) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF3B82F6)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(parsedColor.copy(alpha = 0.15f))
            .border(1.dp, parsedColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = parsedColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}
