package com.example.ui.glass

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserSettings
import kotlin.math.cos
import kotlin.math.sin

/**
 * Glossy Dynamic Island composable for the Snaper AI Assistant.
 * Renders an interactive, AMOLED-optimized 3D Glassmorphism Dynamic Island overlay
 * with realistic 3D reflections, ambient light glow, rotating particles, and touch responsiveness.
 */
object GlossyDynamicIsland {

    @Composable
    fun DynamicIslandOverlay(
        state: DynamicIslandState,
        text: String,
        accentColor: Color = Color(0xFF4CAF50),
        onDismiss: () -> Unit = {}
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "glass_effects")
        
        // 1. Realistic 3D Glass Reflection Animation (diagonal glare sweep)
        val reflectionSweep by infiniteTransition.animateFloat(
            initialValue = -1.5f,
            targetValue = 2.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(3500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "reflection"
        )

        // 2. Dynamic Ambient Lighting Glow (pulsating glow scale)
        val glowScale by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )

        // 3. Smooth Particle Rotation Animation
        val particleAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "particles"
        )

        // 4. Interactive Touch/Tilt Response state
        var isPressed by remember { mutableStateOf(false) }
        var touchOffset by remember { mutableStateOf(Offset.Zero) }
        val interactiveScale by animateFloatAsState(
            targetValue = if (isPressed) 0.94f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "interactive_scale"
        )

        // Subtle automatic 3D tilt sway + touch tilt
        val autoTiltX by infiniteTransition.animateFloat(
            initialValue = -4f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "auto_tilt_x"
        )
        val autoTiltY by infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "auto_tilt_y"
        )

        val tiltX = autoTiltX + (touchOffset.y / 100f).coerceIn(-8f, 8f)
        val tiltY = autoTiltY - (touchOffset.x / 100f).coerceIn(-8f, 8f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Ambient Lighting Glow Behind the Glass
            Box(
                modifier = Modifier
                    .size(width = 320.dp, height = 180.dp)
                    .drawBehind {
                        val center = Offset(size.width / 2, size.height / 2)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.35f * glowScale),
                                    accentColor.copy(alpha = 0.1f * glowScale),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.width * 0.75f * glowScale
                            )
                        )
                    }
            )

            // Main 3D Glass Card Container
            Box(
                modifier = Modifier
                    .size(width = 300.dp, height = 160.dp)
                    .graphicsLayer {
                        rotationX = tiltX
                        rotationY = tiltY
                        scaleX = interactiveScale
                        scaleY = interactiveScale
                        cameraDistance = 12f * density
                        shadowElevation = 24f
                        shape = RoundedCornerShape(24.dp)
                        clip = true
                    }
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.03f)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.08f),
                                accentColor.copy(alpha = 0.2f),
                                Color.White.copy(alpha = 0.1f)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                isPressed = true
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                touchOffset = Offset(offset.x - centerX, offset.y - centerY)
                                tryAwaitRelease()
                                isPressed = false
                                touchOffset = Offset.Zero
                            }
                        )
                    }
            ) {
                // Background Particle Layer (Smooth orbiting celestial bubbles)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val centerX = width / 2
                    val centerY = height / 2

                    // Orbiting Particle 1
                    rotate(particleAngle, pivot = Offset(centerX, centerY)) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.18f),
                            radius = 12f,
                            center = Offset(centerX - 80f, centerY - 30f)
                        )
                    }

                    // Orbiting Particle 2 (reverse direction & speed)
                    rotate(-particleAngle * 1.5f, pivot = Offset(centerX, centerY)) {
                        drawCircle(
                            color = accentColor.copy(alpha = 0.25f),
                            radius = 8f,
                            center = Offset(centerX + 90f, centerY + 40f)
                        )
                    }

                    // Floating Particle 3
                    val driftY = sin(Math.toRadians(particleAngle.toDouble())).toFloat() * 15f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        radius = 15f,
                        center = Offset(centerX - 10f, centerY + driftY)
                    )
                }

                // Glass Glare / 3D Reflection Sweep Overlay
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val sweepPos = reflectionSweep * width

                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.0f),
                                Color.White.copy(alpha = 0.18f),
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.18f),
                                Color.Transparent
                            ),
                            start = Offset(sweepPos - 80f, 0f),
                            end = Offset(sweepPos + 120f, height)
                        )
                    )
                }

                // Foreground Content Area
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Status Badge with soft glowing backdrop
                    Box(
                        modifier = Modifier
                            .background(
                                color = accentColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = state.name.replace("_", " "),
                            color = Color.White,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    // Dynamic message text
                    Text(
                        text = text,
                        color = Color.White,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Interactive Glossy Dismiss Button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Text("Dismiss", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    @Composable
    fun GlossyDynamicIsland(
        userSettings: UserSettings,
        islandState: DynamicIslandState,
        actionResult: Any? = null,
        isDeviceLocked: Boolean = false,
        onExpandToggle: () -> Unit = {},
        onUnlockRequest: (DynamicIslandState) -> Unit = {}
    ) {
        actionResult
        
        // Parse the custom user accent color, fallback safely
        val accentColor = try {
            Color(android.graphics.Color.parseColor(userSettings.accentColorHex ?: "#4CAF50"))
        } catch (e: Exception) {
            Color(0xFF4CAF50)
        }

        // Render the high fidelity 3D glassmorphic overlay
        val displayText = when (islandState) {
            DynamicIslandState.DYNAMIC_ISLAND_AOD -> "Locked / AOD Mode"
            DynamicIslandState.EXPANDED -> "Active Connection"
            DynamicIslandState.COLLAPSED -> "Ready to Assist"
            else -> islandState.name
        }

        DynamicIslandOverlay(
            state = islandState,
            text = displayText,
            accentColor = accentColor,
            onDismiss = {
                onUnlockRequest(islandState)
                onExpandToggle()
            }
        )
    }
}
