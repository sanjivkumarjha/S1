package com.example.ui.components

import com.example.domain.mood.MoodManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

import coil.compose.AsyncImage
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize

enum class AssistantExpression {
    HAPPY, THINKING, SPEAKING, LISTENING, CARING
}

@Composable
fun AnimeAssistantCanvas(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 260.dp,
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    expression: AssistantExpression = AssistantExpression.CARING,
    accentColor: Color = Color(0xFF8B5CF6),
    borderColor: Color = Color(0xFFA855F7),
    use3D: Boolean = true,
    hairStyle: String = "LONG_CYBER",
    hairColorHex: String = "#8B5CF6",
    skinToneHex: String = "#FFF0EA",
    outfitStyle: String = "CYBER_SUIT",
    accessoryStyle: String = "HOLOGRAM_HALO",
    custom3DModelUri: String = "",
    customAvatarImageUri: String = "",
    onTap: () -> Unit = {}
) {
    if (customAvatarImageUri.isNotBlank()) {
        val infiniteTransition = rememberInfiniteTransition(label = "custom_avatar_anim")
        val floatOffset by infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float_custom"
        )
        val mouthWave by infiniteTransition.animateFloat(
            initialValue = 4f,
            targetValue = 20f,
            animationSpec = infiniteRepeatable(
                animation = tween(150, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "mouth_custom"
        )

        Box(
            modifier = modifier
                .size(sizeDp)
                .clickable { onTap() },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(sizeDp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f + floatOffset
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(borderColor.copy(alpha = 0.5f), accentColor.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(cx, cy)
                )
            }

            Box(
                modifier = Modifier
                    .size(sizeDp * 0.82f)
                    .clip(CircleShape)
                    .border(4.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = customAvatarImageUri,
                    contentDescription = "Custom Owner Avatar",
                    modifier = Modifier.fillMaxSize()
                )

                if (isSpeaking) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val mouthY = size.height * 0.68f
                        drawOval(
                            color = Color(0xFFFF2A6D).copy(alpha = 0.85f),
                            topLeft = Offset(cx - mouthWave, mouthY - mouthWave * 0.5f),
                            size = Size(mouthWave * 2f, mouthWave)
                        )
                    }
                }
            }
        }
        return
    }

    if (use3D) {
        Glass3DAvatarCanvas(
            modifier = modifier,
            sizeDp = sizeDp,
            mood = MoodManager.getMood(),
            isSpeaking = isSpeaking,
            isListening = isListening,
            hairStyle = hairStyle,
            hairColorHex = hairColorHex,
            skinToneHex = skinToneHex,
            outfitStyle = outfitStyle,
            accessoryStyle = accessoryStyle,
            custom3DModelUri = custom3DModelUri,
            interactiveDrag = true,
            onTap = onTap
        )
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "anime_anim")

    // 1. Idle breathing float (vertical floating oscillation)
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    // 2. Head tilt / sway movement
    val headTiltAngle by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "head_tilt"
    )

    // 3. Eye blinking oscillation
    val blinkScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    // 4. Breathing chest/hair scale
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // 5. Mouth movement during speech
    val mouthAnim by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(160, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mouth"
    )

    // 6. Glowing aura rotation
    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura"
    )

    var touchReaction by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clickable {
                touchReaction = !touchReaction
                onTap()
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f + floatOffset

            // 1. Background Glowing Halo Aura
            val auraRadius = w * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = if (isListening) 0.5f else 0.3f),
                        accentColor.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = auraRadius * 1.25f
                ),
                radius = auraRadius * 1.25f,
                center = Offset(cx, cy)
            )

            // Floating Magic Particles
            for (i in 0..6) {
                val angle = (auraRotation + i * 51.4f) * (Math.PI / 180f)
                val px = cx + (auraRadius * 0.88f * sin(angle)).toFloat()
                val py = cy + (auraRadius * 0.88f * sin(angle * 1.4)).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) accentColor.copy(alpha = 0.8f) else Color(0xFF38BDF8),
                    radius = 4.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }

            // Head Tilt & Sway Transformation
            rotate(degrees = headTiltAngle, pivot = Offset(cx, cy)) {

                // 2. Long Anime Hair (Back layer)
                val hairBackPath = Path().apply {
                    moveTo(cx - w * 0.35f, cy + h * 0.35f)
                    cubicTo(cx - w * 0.4f, cy - h * 0.2f, cx - w * 0.2f, cy - h * 0.4f, cx, cy - h * 0.4f)
                    cubicTo(cx + w * 0.2f, cy - h * 0.4f, cx + w * 0.4f, cy - h * 0.2f, cx + w * 0.35f, cy + h * 0.35f)
                    cubicTo(cx + w * 0.25f, cy + h * 0.4f, cx + w * 0.15f, cy + h * 0.2f, cx, cy + h * 0.25f)
                    cubicTo(cx - w * 0.15f, cy + h * 0.2f, cx - w * 0.25f, cy + h * 0.4f, cx - w * 0.35f, cy + h * 0.35f)
                    close()
                }
                drawPath(
                    path = hairBackPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF31103F), Color(0xFF1E1B4B))
                    )
                )

                // 3. Cute Anime Head Base (Face)
                val faceRadius = w * 0.23f
                drawCircle(
                    color = Color(0xFFFFF0EA), // Warm fair skin tone
                    radius = faceRadius * breathScale,
                    center = Offset(cx, cy)
                )

                // Cute Blush Circles
                val cheekY = cy + faceRadius * 0.15f
                drawCircle(
                    color = Color(0xFFFFB7B2).copy(alpha = 0.65f),
                    radius = 12.dp.toPx(),
                    center = Offset(cx - faceRadius * 0.48f, cheekY)
                )
                drawCircle(
                    color = Color(0xFFFFB7B2).copy(alpha = 0.65f),
                    radius = 12.dp.toPx(),
                    center = Offset(cx + faceRadius * 0.48f, cheekY)
                )

                // 4. Expression & Sparkly Anime Eyes
                val eyeWidth = 18.dp.toPx()
                val eyeHeight = 26.dp.toPx() * (if (touchReaction) 0.3f else blinkScale)
                val eyeY = cy - faceRadius * 0.12f
                val leftEyeX = cx - faceRadius * 0.4f
                val rightEyeX = cx + faceRadius * 0.4f

                // Left Eye
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor, Color(0xFF1E1B4B))
                    ),
                    topLeft = Offset(leftEyeX - eyeWidth / 2, eyeY - eyeHeight / 2),
                    size = Size(eyeWidth, eyeHeight)
                )
                // Left Eye Pupil Sparkle
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(leftEyeX - 3.dp.toPx(), eyeY - 4.dp.toPx())
                )

                // Right Eye
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor, Color(0xFF1E1B4B))
                    ),
                    topLeft = Offset(rightEyeX - eyeWidth / 2, eyeY - eyeHeight / 2),
                    size = Size(eyeWidth, eyeHeight)
                )
                // Right Eye Pupil Sparkle
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(rightEyeX - 3.dp.toPx(), eyeY - 4.dp.toPx())
                )

                // Eyebrows
                val eyebrowPath = Path().apply {
                    moveTo(leftEyeX - 10.dp.toPx(), eyeY - eyeHeight * 0.8f)
                    quadraticTo(leftEyeX, eyeY - eyeHeight * 1.1f, leftEyeX + 10.dp.toPx(), eyeY - eyeHeight * 0.8f)
                    moveTo(rightEyeX - 10.dp.toPx(), eyeY - eyeHeight * 0.8f)
                    quadraticTo(rightEyeX, eyeY - eyeHeight * 1.1f, rightEyeX + 10.dp.toPx(), eyeY - eyeHeight * 0.8f)
                }
                drawPath(
                    path = eyebrowPath,
                    color = Color(0xFF4C1D95),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // 5. Animated Mouth (Lip-sync & Expressions)
                val mouthY = cy + faceRadius * 0.42f
                val currentMouthOpening = if (isSpeaking) mouthAnim.dp.toPx() else 8.dp.toPx()

                val mouthPath = Path().apply {
                    moveTo(cx - 12.dp.toPx(), mouthY)
                    if (isSpeaking) {
                        quadraticTo(cx, mouthY + currentMouthOpening, cx + 12.dp.toPx(), mouthY)
                        quadraticTo(cx, mouthY - 2.dp.toPx(), cx - 12.dp.toPx(), mouthY)
                    } else {
                        quadraticTo(cx, mouthY + currentMouthOpening, cx + 12.dp.toPx(), mouthY)
                    }
                }
                drawPath(
                    path = mouthPath,
                    color = Color(0xFFE11D48),
                    style = if (isSpeaking) Stroke(width = 4.dp.toPx()) else Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                )

                // 6. Anime Bangs & Front Hair Strands
                val bangsPath = Path().apply {
                    moveTo(cx - faceRadius * 1.1f, cy - faceRadius * 0.2f)
                    cubicTo(cx - faceRadius * 0.8f, cy - faceRadius * 0.9f, cx - faceRadius * 0.3f, cy - faceRadius * 0.95f, cx, cy - faceRadius * 0.5f)
                    cubicTo(cx + faceRadius * 0.3f, cy - faceRadius * 0.95f, cx + faceRadius * 0.8f, cy - faceRadius * 0.9f, cx + faceRadius * 1.1f, cy - faceRadius * 0.2f)
                    cubicTo(cx + faceRadius * 0.6f, cy - faceRadius * 1.2f, cx - faceRadius * 0.6f, cy - faceRadius * 1.2f, cx - faceRadius * 1.1f, cy - faceRadius * 0.2f)
                    close()
                }
                drawPath(
                    path = bangsPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(accentColor, Color(0xFF4C1D95))
                    )
                )

                // Hair Ribbon Bow
                val bowCenter = Offset(cx, cy - faceRadius * 0.95f)
                drawCircle(
                    color = Color(0xFFF43F5E),
                    radius = 8.dp.toPx(),
                    center = bowCenter
                )
            }
        }
    }
}
