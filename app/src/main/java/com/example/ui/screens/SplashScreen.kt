package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.domain.branding.ProtectedBranding
import com.example.ui.glass.DynamicLiquidGlassBackground
import com.example.ui.glass.GlassFooter
import com.example.ui.glass.GlassSurface
import kotlinx.coroutines.delay

import com.example.ui.components.AppLogo

@Composable
fun SplashScreen(
    userSettings: UserSettings? = null,
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { UserPreferencesRepository(context) }
    val currentSettings by if (userSettings != null) {
        rememberUpdatedState(userSettings)
    } else {
        repo.userSettingsFlow.collectAsState(initial = UserSettings())
    }

    var startAnimation by remember { mutableStateOf(false) }

    val animDuration = currentSettings.splashAnimationDuration.toLong().coerceIn(1000L, 3500L)
    val animSpeedMs = when (currentSettings.splashAnimationSpeed.uppercase()) {
        "FAST" -> 800
        "SLOW" -> 2500
        else -> 1500
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(animSpeedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.75f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "splash_scale"
    )

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        val targetDelay = if (currentSettings.isOnboardingCompleted) 400L else animDuration
        delay(targetDelay)
        onSplashFinished()
    }

    val bgType = currentSettings.splashBgType
    val glossIntensity = currentSettings.splashGlossIntensity

    Box(modifier = Modifier.fillMaxSize()) {
        when (bgType) {
            "SOLID" -> Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0E15)))
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
                            listOf(Color(0xFFFF2D55).copy(alpha = 0.4f * glossIntensity), Color(0xFF090A10))
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
                        sizeDp = 280.dp,
                        custom3DModelUri = currentSettings.splash3DModelUri.ifEmpty { currentSettings.avatar3DModelUri },
                        hairStyle = currentSettings.avatar3DHairStyle,
                        hairColorHex = currentSettings.avatar3DHairColorHex,
                        skinToneHex = currentSettings.avatar3DSkinToneHex,
                        outfitStyle = currentSettings.avatar3DOutfitStyle,
                        accessoryStyle = currentSettings.avatar3DAccessoryStyle,
                        rotationYParam = currentSettings.splash3DRotationY,
                        scaleParam = currentSettings.splash3DScale,
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

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Ambient particle glow canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA259FF).copy(alpha = 0.25f * pulseAnim * glossIntensity),
                            Color(0xFFFF2D55).copy(alpha = 0.15f * pulseAnim * glossIntensity),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width * 0.6f
                    ),
                    center = center,
                    radius = size.width * 0.6f
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .scale(scaleAnim)
                    .alpha(alphaAnim)
                    .padding(24.dp)
            ) {
                // Glass Logo Badge
                AppLogo(size = 112.dp)

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Title
                Text(
                    text = "SNAPER TECHNOLOGY",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // PERMANENT PROTECTED BRANDING GREETING: "राधे राधे"
                Text(
                    text = ProtectedBranding.PROTECTED_GREETING, // Always "राधे राधे"
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF2D55)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Customizable Tagline
                Text(
                    text = currentSettings.splashSubtitle,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            // Bottom PERMANENT PROTECTED COMPANY BRANDING: GlassFooter()
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp)
            ) {
                GlassFooter()
            }
        }
    }
}
