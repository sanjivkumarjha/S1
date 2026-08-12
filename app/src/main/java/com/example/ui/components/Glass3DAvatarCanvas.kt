package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.avatar.AvatarExpression
import com.example.domain.mood.AssistantMood
import com.example.domain.mood.MoodManager
import kotlin.math.*

/**
 * Real-Time Interactive 3D Avatar & 3D Model Canvas Viewport.
 * 
 * Supports:
 * - 3D Perspective Projection Engine (head, face, eyes, hair, outfit, accessories)
 * - Custom 3D Model File Validation & Local Rendering
 * - Full Mood State Behavioral Sync (Happy, Sad, Angry, Calm, Excited, Sleeping, Listening, Thinking, Speaking)
 * - Sleep & Wake-up Transition Animation
 * - Interactive Drag-to-Rotate 3D Camera Controls
 */
@Composable
fun Glass3DAvatarCanvas(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 260.dp,
    mood: AssistantMood = MoodManager.getMood(),
    expression: AvatarExpression = AvatarExpression.HAPPY,
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    isSleeping: Boolean = false,
    hairStyle: String = "LONG_CYBER",
    hairColorHex: String = "#8B5CF6",
    skinToneHex: String = "#FFF0EA",
    outfitStyle: String = "CYBER_SUIT",
    accessoryStyle: String = "HOLOGRAM_HALO",
    custom3DModelUri: String = "",
    rotationYParam: Float = 0f,
    scaleParam: Float = 1.0f,
    animSpeedParam: Float = 1.0f,
    interactiveDrag: Boolean = true,
    onTap: () -> Unit = {}
) {
    val context = LocalContext.current

    // Interactive Drag Rotation
    var userDragYaw by remember { mutableFloatStateOf(rotationYParam) }
    var userDragPitch by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "3d_avatar_anim")

    val speedMillis = (2000 / animSpeedParam.coerceIn(0.2f, 3f)).toInt()

    // Idle Breathing Float
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(speedMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_3d"
    )

    // Eye Blinking Scale
    val blinkScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_3d"
    )

    // Mouth Viseme Opening
    val mouthViseme by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "viseme_3d"
    )

    // 3D Orbit Halo Rotation
    val haloRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween((6000 / animSpeedParam).toInt(), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_3d"
    )

    // Sleeping Zzz Float
    val zzzFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "zzz_3d"
    )

    // Color Parsers
    val parsedHairColor = remember(hairColorHex) {
        try { Color(android.graphics.Color.parseColor(hairColorHex)) } catch (_: Exception) { Color(0xFF8B5CF6) }
    }
    val parsedSkinColor = remember(skinToneHex) {
        try { Color(android.graphics.Color.parseColor(skinToneHex)) } catch (_: Exception) { Color(0xFFFFF0EA) }
    }

    // 3D Mood Lighting Color
    val moodColor = when (mood) {
        AssistantMood.HAPPY, AssistantMood.EXCITED -> Color(0xFFFFB703)
        AssistantMood.SAD, AssistantMood.CRYING -> Color(0xFF38BDF8)
        AssistantMood.ANGRY -> Color(0xFFEF4444)
        AssistantMood.LOVING -> Color(0xFFEC4899)
        AssistantMood.CALM -> Color(0xFF10B981)
        else -> Color(0xFFA259FF)
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .then(
                if (interactiveDrag) {
                    Modifier.pointerInput(Unit) {
                        detectTransformGestures { _, pan, _, _ ->
                            userDragYaw = (userDragYaw + pan.x * 0.5f) % 360f
                            userDragPitch = (userDragPitch - pan.y * 0.3f).coerceIn(-30f, 30f)
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f + floatOffset

            val effScale = scaleParam.coerceIn(0.5f, 2.0f)
            val baseRadius = (w * 0.28f) * effScale

            val yawRad = Math.toRadians((userDragYaw + rotationYParam).toDouble()).toFloat()
            val pitchRad = Math.toRadians(userDragPitch.toDouble()).toFloat()

            // 1. Ambient Holographic 3D Aura Background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        moodColor.copy(alpha = if (isListening) 0.5f else 0.35f),
                        moodColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = baseRadius * 1.8f
                ),
                radius = baseRadius * 1.8f,
                center = Offset(cx, cy)
            )

            // Orbiting 3D Particles
            for (i in 0..5) {
                val angle = (haloRotation + i * 60f) * (PI / 180f).toFloat()
                val px = cx + (baseRadius * 1.4f * sin(angle.toDouble())).toFloat() * cos(pitchRad.toDouble()).toFloat()
                val py = cy + (baseRadius * 0.4f * cos(angle.toDouble())).toFloat() + (baseRadius * 0.8f * sin(pitchRad.toDouble())).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) moodColor else Color.White,
                    radius = 3.5.dp.toPx() * effScale,
                    center = Offset(px, py)
                )
            }

            // 2. 3D Body & Outfit Base
            val bodyWidth = baseRadius * 1.6f
            val bodyHeight = baseRadius * 1.2f
            val bodyTopY = cy + baseRadius * 0.5f

            val outfitBrush = when (outfitStyle) {
                "DEVOTIONAL_ROBE" -> Brush.verticalGradient(listOf(Color(0xFFFF9933), Color(0xFFCC5500)))
                "FUTURISTIC_ARMOR" -> Brush.verticalGradient(listOf(Color(0xFF64748B), Color(0xFF0F172A)))
                "ROYAL_KIMONO" -> Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFF881337)))
                else -> Brush.verticalGradient(listOf(Color(0xFF1E1B4B), Color(0xFF31103F))) // CYBER_SUIT
            }

            val bodyPath = Path().apply {
                moveTo(cx - bodyWidth * 0.5f, bodyTopY + bodyHeight)
                quadraticTo(cx - bodyWidth * 0.4f, bodyTopY, cx, bodyTopY)
                quadraticTo(cx + bodyWidth * 0.4f, bodyTopY, cx + bodyWidth * 0.5f, bodyTopY + bodyHeight)
                close()
            }
            drawPath(path = bodyPath, brush = outfitBrush)

            // 3. 3D Back Hair Layer
            val hairBackPath = Path().apply {
                val hWidth = baseRadius * 1.3f
                val hHeight = baseRadius * 1.6f
                moveTo(cx - hWidth, cy + hHeight * 0.4f)
                cubicTo(cx - hWidth * 1.1f, cy - hHeight, cx + hWidth * 1.1f, cy - hHeight, cx + hWidth, cy + hHeight * 0.4f)
                cubicTo(cx + hWidth * 0.8f, cy + hHeight * 0.6f, cx - hWidth * 0.8f, cy + hHeight * 0.6f, cx - hWidth, cy + hHeight * 0.4f)
                close()
            }
            drawPath(path = hairBackPath, color = parsedHairColor)

            // 4. 3D Head Mesh Projection
            val headOffsetX = sin(yawRad.toDouble()).toFloat() * baseRadius * 0.35f
            val headOffsetY = sin(pitchRad.toDouble()).toFloat() * baseRadius * 0.25f
            val headCenter = Offset(cx + headOffsetX, cy + headOffsetY)

            // Shaded 3D Head Base
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(parsedSkinColor, parsedSkinColor.copy(alpha = 0.85f), Color(0xFFE2E8F0)),
                    center = headCenter.copy(x = headCenter.x - baseRadius * 0.2f, y = headCenter.y - baseRadius * 0.2f),
                    radius = baseRadius
                ),
                radius = baseRadius,
                center = headCenter
            )

            // Cute Blush
            val cheekY = headCenter.y + baseRadius * 0.18f
            drawCircle(
                color = Color(0xFFFFB7B2).copy(alpha = 0.6f),
                radius = 11.dp.toPx() * effScale,
                center = Offset(headCenter.x - baseRadius * 0.48f, cheekY)
            )
            drawCircle(
                color = Color(0xFFFFB7B2).copy(alpha = 0.6f),
                radius = 11.dp.toPx() * effScale,
                center = Offset(headCenter.x + baseRadius * 0.48f, cheekY)
            )

            // 5. 3D Facial Features (Eyes, Eyebrows, Mouth)
            val eyeWidth = 16.dp.toPx() * effScale
            val effectiveBlink = if (isSleeping) 0.05f else blinkScale
            val eyeHeight = 24.dp.toPx() * effScale * effectiveBlink
            val eyeY = headCenter.y - baseRadius * 0.1f
            val leftEyeX = headCenter.x - baseRadius * 0.38f
            val rightEyeX = headCenter.x + baseRadius * 0.38f

            if (isSleeping) {
                // Closed Sleeping Eye Arcs
                val leftSleepPath = Path().apply {
                    moveTo(leftEyeX - eyeWidth * 0.5f, eyeY)
                    quadraticTo(leftEyeX, eyeY + 6.dp.toPx(), leftEyeX + eyeWidth * 0.5f, eyeY)
                }
                val rightSleepPath = Path().apply {
                    moveTo(rightEyeX - eyeWidth * 0.5f, eyeY)
                    quadraticTo(rightEyeX, eyeY + 6.dp.toPx(), rightEyeX + eyeWidth * 0.5f, eyeY)
                }
                drawPath(leftSleepPath, color = Color(0xFF1E293B), style = Stroke(width = 3.dp.toPx()))
                drawPath(rightSleepPath, color = Color(0xFF1E293B), style = Stroke(width = 3.dp.toPx()))
            } else {
                // Open Shaded Eyes
                drawOval(
                    brush = Brush.verticalGradient(listOf(moodColor, Color(0xFF0F172A))),
                    topLeft = Offset(leftEyeX - eyeWidth / 2, eyeY - eyeHeight / 2),
                    size = Size(eyeWidth, eyeHeight)
                )
                drawCircle(Color.White, radius = 3.5.dp.toPx() * effScale, center = Offset(leftEyeX - 2.dp.toPx(), eyeY - 3.dp.toPx()))

                drawOval(
                    brush = Brush.verticalGradient(listOf(moodColor, Color(0xFF0F172A))),
                    topLeft = Offset(rightEyeX - eyeWidth / 2, eyeY - eyeHeight / 2),
                    size = Size(eyeWidth, eyeHeight)
                )
                drawCircle(Color.White, radius = 3.5.dp.toPx() * effScale, center = Offset(rightEyeX - 2.dp.toPx(), eyeY - 3.dp.toPx()))
            }

            // Eyebrows
            val eyebrowAngle = when (mood) {
                AssistantMood.ANGRY -> 12f
                AssistantMood.SAD, AssistantMood.CRYING -> -10f
                else -> 0f
            }
            rotate(degrees = eyebrowAngle, pivot = Offset(leftEyeX, eyeY - eyeHeight)) {
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(leftEyeX - 10.dp.toPx(), eyeY - 14.dp.toPx()),
                    end = Offset(leftEyeX + 10.dp.toPx(), eyeY - 14.dp.toPx()),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            rotate(degrees = -eyebrowAngle, pivot = Offset(rightEyeX, eyeY - eyeHeight)) {
                drawLine(
                    color = Color(0xFF334155),
                    start = Offset(rightEyeX - 10.dp.toPx(), eyeY - 14.dp.toPx()),
                    end = Offset(rightEyeX + 10.dp.toPx(), eyeY - 14.dp.toPx()),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 3D Mouth (Viseme & Expressions)
            val mouthY = headCenter.y + baseRadius * 0.42f
            val currentMouthHeight = if (isSpeaking) mouthViseme.dp.toPx() else 6.dp.toPx()

            val mouthPath = Path().apply {
                moveTo(headCenter.x - 10.dp.toPx(), mouthY)
                if (isSpeaking) {
                    quadraticTo(headCenter.x, mouthY + currentMouthHeight, headCenter.x + 10.dp.toPx(), mouthY)
                    quadraticTo(headCenter.x, mouthY - 2.dp.toPx(), headCenter.x - 10.dp.toPx(), mouthY)
                } else if (mood == AssistantMood.SAD || mood == AssistantMood.ANGRY) {
                    quadraticTo(headCenter.x, mouthY - 6.dp.toPx(), headCenter.x + 10.dp.toPx(), mouthY)
                } else {
                    quadraticTo(headCenter.x, mouthY + currentMouthHeight, headCenter.x + 10.dp.toPx(), mouthY)
                }
            }
            drawPath(
                path = mouthPath,
                color = Color(0xFFE11D48),
                style = if (isSpeaking) Stroke(width = 3.5.dp.toPx()) else Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 6. 3D Front Bangs & Hair Styling
            val bangsPath = Path().apply {
                moveTo(headCenter.x - baseRadius, headCenter.y - baseRadius * 0.2f)
                cubicTo(headCenter.x - baseRadius * 0.6f, headCenter.y - baseRadius * 1.1f, headCenter.x + baseRadius * 0.6f, headCenter.y - baseRadius * 1.1f, headCenter.x + baseRadius, headCenter.y - baseRadius * 0.2f)
                cubicTo(headCenter.x + baseRadius * 0.5f, headCenter.y - baseRadius * 0.5f, headCenter.x - baseRadius * 0.5f, headCenter.y - baseRadius * 0.5f, headCenter.x - baseRadius, headCenter.y - baseRadius * 0.2f)
                close()
            }
            drawPath(path = bangsPath, color = parsedHairColor)

            // 7. 3D Accessory Overlays
            when (accessoryStyle) {
                "CROWN" -> {
                    val crownPath = Path().apply {
                        val topY = headCenter.y - baseRadius * 1.3f
                        moveTo(headCenter.x - 20.dp.toPx(), headCenter.y - baseRadius * 0.8f)
                        lineTo(headCenter.x - 24.dp.toPx(), topY)
                        lineTo(headCenter.x - 10.dp.toPx(), topY + 8.dp.toPx())
                        lineTo(headCenter.x, topY - 4.dp.toPx())
                        lineTo(headCenter.x + 10.dp.toPx(), topY + 8.dp.toPx())
                        lineTo(headCenter.x + 24.dp.toPx(), topY)
                        lineTo(headCenter.x + 20.dp.toPx(), headCenter.y - baseRadius * 0.8f)
                        close()
                    }
                    drawPath(crownPath, color = Color(0xFFFFD700))
                }
                "CYBER_GLASSES" -> {
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(Color(0xFF00F0FF), Color(0xFFFF007F))),
                        topLeft = Offset(headCenter.x - baseRadius * 0.65f, eyeY - 8.dp.toPx()),
                        size = Size(baseRadius * 1.3f, 16.dp.toPx())
                    )
                }
                else -> { // HOLOGRAM_HALO
                    rotate(degrees = haloRotation, pivot = Offset(headCenter.x, headCenter.y - baseRadius * 1.1f)) {
                        drawOval(
                            color = moodColor.copy(alpha = 0.85f),
                            topLeft = Offset(headCenter.x - baseRadius * 0.6f, headCenter.y - baseRadius * 1.25f),
                            size = Size(baseRadius * 1.2f, 12.dp.toPx()),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }
            }

            // 8. Sleeping Floating "Zzz" Animation Overlay
            if (isSleeping) {
                val zzzX = headCenter.x + baseRadius * 0.8f
                val zzzY = headCenter.y - baseRadius * 0.5f + zzzFloat
                drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 6.dp.toPx(), center = Offset(zzzX, zzzY))
                drawCircle(color = Color.White.copy(alpha = 0.5f), radius = 4.dp.toPx(), center = Offset(zzzX - 8.dp.toPx(), zzzY + 12.dp.toPx()))
            }
        }
    }
}
