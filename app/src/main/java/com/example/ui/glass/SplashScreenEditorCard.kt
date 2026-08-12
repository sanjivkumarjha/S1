package com.example.ui.glass

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.domain.branding.ProtectedBranding
import kotlinx.coroutines.launch

/**
 * Visual Splash Screen Editor with Real-Time Device Live Preview.
 * 
 * Enforces strict immutability for Protected Text #1 ("राधे राधे") 
 * and Protected Text #2 ("Made by Snaper Technology Private Limited").
 */
import com.example.ui.components.AppLogo

@Composable
fun SplashScreenEditorCard(
    userSettings: UserSettings,
    repo: UserPreferencesRepository
) {
    val scope = rememberCoroutineScope()

    // Live State for Splash Customization
    var liveSubtitle by remember { mutableStateOf(userSettings.splashSubtitle) }
    var liveBgType by remember { mutableStateOf(userSettings.splashBgType) }
    var liveLogoStyle by remember { mutableStateOf(userSettings.splashLogoStyle) }
    var liveAnimStyle by remember { mutableStateOf(userSettings.splashAnimationStyle) }
    var liveAnimSpeed by remember { mutableStateOf(userSettings.splashAnimationSpeed) }
    var liveAnimDuration by remember { mutableStateOf(userSettings.splashAnimationDuration) }
    var liveGlossIntensity by remember { mutableStateOf(userSettings.splashGlossIntensity) }
    var isAnimationEnabled by remember { mutableStateOf(userSettings.splashIsAnimationEnabled) }

    var statusMessage by remember { mutableStateOf("Ready to customize Splash Screen.") }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("splash_screen_editor_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Splash Editor",
                        tint = LocalGlassAccent.current.color,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Splash Screen Live Editor",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Real-time preview with protected Snaper branding",
                            fontSize = 11.5.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // DEVICE REAL-TIME LIVE PREVIEW FRAME
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black,
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    LocalGlassAccent.current.color.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Background rendering based on selection
                    when (liveBgType) {
                        "SOLID" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F1A)))
                        "GRADIENT" -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF1E0A3C), Color(0xFF0A1E3C), Color(0xFF000000))
                                    )
                                )
                        )
                        "GLOSSY_NEON" -> Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFFFF2D55).copy(alpha = 0.35f * liveGlossIntensity), Color(0xFF090A10))
                                    )
                                )
                        )
                        "3D_AVATAR_SPLASH", "3D_MODEL_SPLASH" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF090A12)),
                                contentAlignment = Alignment.Center
                            ) {
                                com.example.ui.components.Glass3DAvatarCanvas(
                                    sizeDp = 180.dp,
                                    custom3DModelUri = userSettings.splash3DModelUri.ifEmpty { userSettings.avatar3DModelUri },
                                    hairStyle = userSettings.avatar3DHairStyle,
                                    hairColorHex = userSettings.avatar3DHairColorHex,
                                    skinToneHex = userSettings.avatar3DSkinToneHex,
                                    outfitStyle = userSettings.avatar3DOutfitStyle,
                                    accessoryStyle = userSettings.avatar3DAccessoryStyle,
                                    rotationYParam = userSettings.splash3DRotationY,
                                    scaleParam = userSettings.splash3DScale,
                                    interactiveDrag = false
                                )
                            }
                        }
                        else -> { // DYNAMIC_GLASS
                            DynamicLiquidGlassBackground {
                                Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }

                    // Simulated Particle Ambient Canvas
                    val infiniteTransition = rememberInfiniteTransition(label = "editor_preview")
                    val pulseAnim by infiniteTransition.animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                when (liveAnimSpeed) {
                                    "FAST" -> 800
                                    "SLOW" -> 2500
                                    else -> 1500
                                },
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_editor"
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFA259FF).copy(alpha = 0.25f * pulseAnim * liveGlossIntensity),
                                    Color(0xFFFF2D55).copy(alpha = 0.15f * pulseAnim * liveGlossIntensity),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.width * 0.6f
                            )
                        )
                    }

                    // Content Layout
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Logo Badge
                        AppLogo(size = 72.dp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "SNAPER TECHNOLOGY",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // LOCKED PROTECTED TEXT #1: "राधे राधे"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = ProtectedBranding.PROTECTED_GREETING, // "राधे राधे"
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF2D55)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Protected",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Editable Tagline
                        Text(
                            text = liveSubtitle,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Bottom Protected Company Branding Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.40f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = ProtectedBranding.PROTECTED_BRAND_CREDIT, // "Made by Snaper Technology Private Limited"
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Protected Credit",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PROTECTED BRANDING ALERT BOX
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFF2D55).copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF2D55).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Protected Branding",
                        tint = Color(0xFFFF2D55),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Permanent Protected Branding Enforced",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Text 'राधे राधे' and credit 'Made by Snaper Technology Private Limited' are permanently immutable.",
                            fontSize = 10.5.sp,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CUSTOMIZATION EDITABLE CONTROLS
            Text("Editable Options", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle Input
            OutlinedTextField(
                value = liveSubtitle,
                onValueChange = { liveSubtitle = it },
                label = { Text("Splash Tagline / Subtitle", color = Color.LightGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = LocalGlassAccent.current.color,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Background Type Chips
            Text("Background Theme", fontSize = 12.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("DYNAMIC_GLASS", "3D_AVATAR_SPLASH", "3D_MODEL_SPLASH", "SOLID", "GRADIENT", "GLOSSY_NEON").forEach { bg ->
                    GlassChip(
                        text = bg.replace("_", " "),
                        isSelected = liveBgType == bg,
                        onClick = { liveBgType = bg }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logo Style Chips
            Text("Logo Badge Icon", fontSize = 12.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("AUTO_AWESOME", "SHIELD", "STAR", "CROWN").forEach { logo ->
                    GlassChip(
                        text = logo.replace("_", " "),
                        isSelected = liveLogoStyle == logo,
                        onClick = { liveLogoStyle = logo }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animation Speed
            Text("Animation Speed", fontSize = 12.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("SLOW", "NORMAL", "FAST").forEach { spd ->
                    GlassChip(
                        text = spd,
                        isSelected = liveAnimSpeed == spd,
                        onClick = { liveAnimSpeed = spd }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gloss Intensity Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gloss & Glow Intensity", fontSize = 12.sp, color = Color.LightGray)
                Text("${(liveGlossIntensity * 100).toInt()}%", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = liveGlossIntensity,
                onValueChange = { liveGlossIntensity = it },
                valueRange = 0.2f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = LocalGlassAccent.current.color,
                    activeTrackColor = LocalGlassAccent.current.color
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status message
            Text(statusMessage, fontSize = 11.5.sp, color = Color(0xFF34C759))

            Spacer(modifier = Modifier.height(12.dp))

            // SAVE & RESET BUTTONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            repo.resetSplashScreenToDefault()
                            liveSubtitle = "Personal Liquid Glass AI Assistant"
                            liveBgType = "DYNAMIC_GLASS"
                            liveLogoStyle = "AUTO_AWESOME"
                            liveAnimSpeed = "NORMAL"
                            liveAnimDuration = 1800
                            liveAnimStyle = "BOUNCE_SPRING"
                            isAnimationEnabled = true
                            liveGlossIntensity = 0.8f
                            statusMessage = "Splash Screen reset to default. Protected branding intact."
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3A3C)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset Default", fontSize = 12.sp, color = Color.White)
                }

                Button(
                    onClick = {
                        scope.launch {
                            repo.updateSplashScreenConfig(
                                subtitle = liveSubtitle,
                                bgType = liveBgType,
                                logoStyle = liveLogoStyle,
                                animSpeed = liveAnimSpeed,
                                animDuration = liveAnimDuration,
                                animStyle = liveAnimStyle,
                                isAnimEnabled = isAnimationEnabled,
                                glossIntensity = liveGlossIntensity,
                                customImageUri = ""
                            )
                            statusMessage = "Splash Screen settings saved & applied! ✅"
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LocalGlassAccent.current.color),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save & Apply", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
