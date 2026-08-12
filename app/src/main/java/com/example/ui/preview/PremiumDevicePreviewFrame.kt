package com.example.ui.preview

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumDevicePreviewFrame(
    deviceType: PreviewDeviceType,
    modifier: Modifier = Modifier,
    frameWidth: Dp = 260.dp,
    showDeviceBadge: Boolean = true,
    content: @Composable () -> Unit
) {
    val frameHeight = frameWidth * deviceType.screenAspectRatio
    val isIphone = deviceType.osType == "IOS"
    val cornerRadius = if (isIphone) 36.dp else 28.dp

    val transition = rememberInfiniteTransition(label = "device_glass_shine")
    val shineOffset by transition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Outer Premium Smartphone Frame with Depth & Shadow
        Box(
            modifier = Modifier
                .width(frameWidth)
                .height(frameHeight)
                .shadow(24.dp, shape = RoundedCornerShape(cornerRadius), ambientColor = Color(0xFF8B5CF6), spotColor = Color.Black)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2C2D3A),
                            Color(0xFF14151F),
                            Color(0xFF090A12)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .border(
                    width = 2.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x808B5CF6),
                            Color(0x30FFFFFF),
                            Color(0x6006B6D4)
                        )
                    ),
                    shape = RoundedCornerShape(cornerRadius)
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Screen Bezel Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius - 6.dp))
                    .background(Color.Black)
            ) {
                // Live Screen Content
                content()

                // Top Cutout / Punch Hole / Dynamic Island
                if (isIphone) {
                    // iPhone 17 Pro Max Dynamic Island Pill
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(85.dp)
                            .height(22.dp)
                            .background(Color.Black, shape = RoundedCornerShape(12.dp))
                            .border(0.5.dp, Color(0x30FFFFFF), RoundedCornerShape(12.dp))
                            .align(Alignment.TopCenter)
                    )
                } else {
                    // Samsung Galaxy S26 Ultra Punch Hole Camera
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .size(10.dp)
                            .background(Color.Black, shape = CircleShape)
                            .border(1.dp, Color(0x50FFFFFF), CircleShape)
                            .align(Alignment.TopCenter)
                    )
                }

                // Glass Specular Reflection Highlight Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.08f * shineOffset.coerceIn(0f, 1f)),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.04f * (1f - shineOffset).coerceIn(0f, 1f))
                                )
                            )
                        )
                )
            }
        }

        if (showDeviceBadge) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0x208B5CF6), shape = RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0x408B5CF6), shape = RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Smartphone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${deviceType.displayName} (${if (deviceType.osType == "IOS") "iOS Flagship" else "Android Flagship"})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }
    }
}
