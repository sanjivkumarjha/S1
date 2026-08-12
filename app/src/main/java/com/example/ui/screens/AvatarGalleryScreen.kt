package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.avatar.AgentAvatarManager
import com.example.avatar.AvatarCategory
import com.example.avatar.AvatarExpression
import com.example.avatar.AvatarFormat
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.domain.mood.AssistantMood
import com.example.domain.mood.MoodManager
import com.example.ui.components.Glass3DAvatarCanvas
import com.example.ui.glass.*
import kotlinx.coroutines.launch

@Composable
fun AvatarGalleryScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { UserPreferencesRepository(context) }
    val userSettings by repo.userSettingsFlow.collectAsState(initial = UserSettings())
    val avatarManager = remember { AgentAvatarManager(context) }

    val currentAvatar by avatarManager.currentAvatarState.collectAsState()
    val currentExpression by avatarManager.currentExpressionState.collectAsState()
    val isSpeaking by avatarManager.isSpeakingState.collectAsState()
    val visemeFrame by avatarManager.visemeFrameState.collectAsState()
    val galleryList by avatarManager.galleryListState.collectAsState()

    var statusMessage by remember { mutableStateOf("AI Avatar System initialized.") }
    var selectedCategory by remember { mutableStateOf(AvatarCategory.DEFAULT) }

    // Live 3D Customization States
    var selectedAvatarType by remember { mutableStateOf(userSettings.selectedAvatarType) }
    var hairStyle by remember { mutableStateOf(userSettings.avatar3DHairStyle) }
    var hairColorHex by remember { mutableStateOf(userSettings.avatar3DHairColorHex) }
    var skinToneHex by remember { mutableStateOf(userSettings.avatar3DSkinToneHex) }
    var outfitStyle by remember { mutableStateOf(userSettings.avatar3DOutfitStyle) }
    var accessoryStyle by remember { mutableStateOf(userSettings.avatar3DAccessoryStyle) }
    var rotationY by remember { mutableFloatStateOf(userSettings.avatar3DRotationY) }
    var scaleVal by remember { mutableFloatStateOf(userSettings.avatar3DScale) }
    var custom3DUri by remember { mutableStateOf(userSettings.avatar3DModelUri) }

    // 3D Model File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val uriStr = uri.toString()
            custom3DUri = uriStr
            selectedAvatarType = "3D_MODEL"
            val addedMsg = avatarManager.addCustomAvatar(
                name = "Uploaded 3D Model (${uri.lastPathSegment ?: "Custom"})",
                uri = uriStr,
                format = AvatarFormat.ASSET_3D
            )
            scope.launch {
                repo.update3DAvatarConfig(
                    avatarType = "3D_MODEL",
                    modelUri = uriStr,
                    hairStyle = hairStyle,
                    hairColorHex = hairColorHex,
                    skinToneHex = skinToneHex,
                    outfitStyle = outfitStyle,
                    accessoryStyle = accessoryStyle,
                    rotationY = rotationY,
                    scale = scaleVal,
                    animSpeed = 1.0f
                )
            }
            statusMessage = "3D Model loaded successfully! $addedMsg"
            Toast.makeText(context, "3D Model loaded: $uriStr", Toast.LENGTH_SHORT).show()
        } else {
            statusMessage = "No 3D model selected."
        }
    }

    DynamicLiquidGlassBackground {
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = "AI Avatar & 3D Model Studio",
                    subtitle = "3D Creator • Custom Models • Live Expressions",
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("avatar_gallery_back")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. LIVE 3D / ANIMATED AVATAR PREVIEW CARD
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("avatar_3d_preview_card")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ACTIVE AVATAR VIEWPORT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LocalGlassAccent.current.color,
                            letterSpacing = 1.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Real-Time 3D / 2D Canvas Renderer
                        if (currentAvatar.format == AvatarFormat.ASSET_3D || selectedAvatarType == "3D_AVATAR" || selectedAvatarType == "3D_MODEL") {
                            Glass3DAvatarCanvas(
                                sizeDp = 220.dp,
                                mood = MoodManager.getMood(),
                                expression = currentExpression,
                                isSpeaking = isSpeaking,
                                hairStyle = hairStyle,
                                hairColorHex = hairColorHex,
                                skinToneHex = skinToneHex,
                                outfitStyle = outfitStyle,
                                accessoryStyle = accessoryStyle,
                                custom3DModelUri = custom3DUri,
                                rotationYParam = rotationY,
                                scaleParam = scaleVal,
                                interactiveDrag = true
                            )
                        } else {
                            // 2D Fallback Badge
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                                    .background(LocalGlassAccent.current.color.copy(alpha = 0.2f))
                                    .border(3.dp, LocalGlassAccent.current.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = when (currentExpression) {
                                            AvatarExpression.HAPPY, AvatarExpression.SMILING, AvatarExpression.LAUGHING -> Icons.Default.SentimentVerySatisfied
                                            AvatarExpression.CONCERNED, AvatarExpression.GENTLE_ANGER -> Icons.Default.SentimentDissatisfied
                                            AvatarExpression.THINKING -> Icons.Default.Psychology
                                            AvatarExpression.SECURITY_ALERT -> Icons.Default.Shield
                                            AvatarExpression.SPEAKING -> Icons.Default.RecordVoiceOver
                                            else -> Icons.Default.Face
                                        },
                                        contentDescription = "Avatar Viseme",
                                        tint = Color.White,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Text(
                                        text = if (isSpeaking) "Viseme $visemeFrame" else currentExpression.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentAvatar.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Type: $selectedAvatarType • Drag 3D canvas to orbit camera",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // UPLOAD 3D MODEL BUTTON
                        Button(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LocalGlassAccent.current.color),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("upload_3d_model_button")
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Custom 3D Model (.glb / .gltf / .obj)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. 3D AVATAR CREATOR & CUSTOMIZER
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Brush,
                                contentDescription = null,
                                tint = LocalGlassAccent.current.color,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("3D Avatar Studio Customizer", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hair Style Selection
                        Text("Hair Style", fontSize = 12.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("LONG_CYBER", "SHORT_BOB", "CYBER_MOHAWK", "ROYAL_PONYTAIL")) { hs ->
                                GlassChip(
                                    text = hs.replace("_", " "),
                                    isSelected = hairStyle == hs,
                                    onClick = { hairStyle = hs }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Outfit Style Selection
                        Text("Outfit & Cyber Gear", fontSize = 12.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("CYBER_SUIT", "DEVOTIONAL_ROBE", "FUTURISTIC_ARMOR", "ROYAL_KIMONO")) { os ->
                                GlassChip(
                                    text = os.replace("_", " "),
                                    isSelected = outfitStyle == os,
                                    onClick = { outfitStyle = os }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Accessory Selection
                        Text("Head Accessory", fontSize = 12.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf("HOLOGRAM_HALO", "CROWN", "CYBER_GLASSES", "NONE")) { acc ->
                                GlassChip(
                                    text = acc.replace("_", " "),
                                    isSelected = accessoryStyle == acc,
                                    onClick = { accessoryStyle = acc }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hair Color Swatches
                        Text("Hair Color Accent", fontSize = 12.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("#8B5CF6", "#FF2D55", "#00F0FF", "#FFB703", "#10B981", "#FFFFFF").forEach { hex ->
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            width = if (hairColorHex == hex) 3.dp else 1.dp,
                                            color = if (hairColorHex == hex) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { hairColorHex = hex }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3D Scale Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("3D Model Scale", fontSize = 12.sp, color = Color.LightGray)
                            Text("${(scaleVal * 100).toInt()}%", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = scaleVal,
                            onValueChange = { scaleVal = it },
                            valueRange = 0.6f..1.8f,
                            colors = SliderDefaults.colors(thumbColor = LocalGlassAccent.current.color, activeTrackColor = LocalGlassAccent.current.color)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Save 3D Avatar Customization Button
                        Button(
                            onClick = {
                                scope.launch {
                                    repo.update3DAvatarConfig(
                                        avatarType = if (custom3DUri.isNotEmpty()) "3D_MODEL" else "3D_AVATAR",
                                        modelUri = custom3DUri,
                                        hairStyle = hairStyle,
                                        hairColorHex = hairColorHex,
                                        skinToneHex = skinToneHex,
                                        outfitStyle = outfitStyle,
                                        accessoryStyle = accessoryStyle,
                                        rotationY = rotationY,
                                        scale = scaleVal,
                                        animSpeed = 1.0f
                                    )
                                    statusMessage = "3D Avatar configuration saved & applied across Snaper AI! ✅"
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LocalGlassAccent.current.color),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save & Apply 3D Avatar", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. EXPRESSION & VOICE TEST CONTROLLER
                Text(
                    text = "3D Expression & Lip Sync Test",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AvatarExpression.values()) { expr ->
                        GlassChip(
                            text = expr.name,
                            isSelected = currentExpression == expr,
                            onClick = {
                                avatarManager.setExpression(expr)
                                statusMessage = "Avatar expression set to ${expr.name}"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Simulate Lip Sync Visemes", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isSpeaking,
                        onCheckedChange = { speaking ->
                            avatarManager.setSpeaking(speaking)
                            if (speaking) avatarManager.updateLipSyncVisemeFrame(0.8f)
                        },
                        modifier = Modifier.testTag("lip_sync_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. AVATAR GALLERY PRESETS
                Text(
                    text = "Avatar Gallery Presets",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AvatarCategory.values()) { cat ->
                        GlassChip(
                            text = cat.name.replace("_", " "),
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val filtered = galleryList.filter { it.category == selectedCategory || selectedCategory == AvatarCategory.DEFAULT }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtered.forEach { avatar ->
                        GlassCard(
                            onClick = {
                                val msg = avatarManager.selectAvatarById(avatar.id)
                                selectedAvatarType = if (avatar.format == AvatarFormat.ASSET_3D) "3D_AVATAR" else "PHOTO"
                                statusMessage = msg
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (avatar.format == AvatarFormat.ASSET_3D) Icons.Default.ViewInAr else if (avatar.format == AvatarFormat.VIDEO) Icons.Default.Videocam else Icons.Default.Face,
                                    contentDescription = null,
                                    tint = LocalGlassAccent.current.color,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(avatar.name, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Category: ${avatar.category.name} • Format: ${avatar.format.name}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                if (currentAvatar.id == avatar.id) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Active", tint = LocalGlassAccent.current.color)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalGlassAccent.current.color
                )
            }
        }
    }
}
