package com.example.ui.glass

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Android 17 Liquid Glass Card container with multi-layered Glassmorphism,
 * organic light refractions, specular highlights, and spring press micro-animations.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    backgroundColor: Color = LocalGlassAccent.current.color,
    borderColor: Color = LocalGlassAccent.current.color,
    elevationDp: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    themeMode: GlassThemeMode = LocalGlassThemeMode.current,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleAnim = remember { Animatable(1.0f) }

    LaunchedEffect(isPressed) {
        if (isPressed && onClick != null) {
            scaleAnim.animateTo(0.96f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
        } else {
            scaleAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    val glassContainerColor = when (themeMode) {
        GlassThemeMode.LIGHT -> Color.White.copy(alpha = 0.75f)
        GlassThemeMode.AMOLED -> Color(0xFF08090F).copy(alpha = 0.88f)
        GlassThemeMode.DARK -> Color(0xFF121422).copy(alpha = 0.82f)
    }

    val glassBorderColors = when (themeMode) {
        GlassThemeMode.LIGHT -> listOf(
            Color.White.copy(alpha = 0.95f),
            backgroundColor.copy(alpha = 0.40f),
            Color.White.copy(alpha = 0.30f)
        )
        else -> listOf(
            borderColor.copy(alpha = 0.65f),
            Color.White.copy(alpha = 0.25f),
            borderColor.copy(alpha = 0.35f)
        )
    }

    val baseModifier = modifier
        .scale(scaleAnim.value)
        .shadow(elevationDp, shape = shape, ambientColor = borderColor, spotColor = Color.Black)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    glassContainerColor,
                    glassContainerColor.copy(alpha = 0.60f)
                )
            ),
            shape = shape
        )
        .border(
            width = 1.2.dp,
            brush = Brush.linearGradient(colors = glassBorderColors),
            shape = shape
        )
        .clip(shape)

    val finalModifier = if (onClick != null) {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else baseModifier

    Box(modifier = finalModifier) {
        // Specular Top Refraction Highlight Line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.40f),
                            Color.Transparent
                        )
                    )
                )
        )
        content()
    }
}

/**
 * Android 17 Liquid Glass Button with tactile spring physics press scale (`scale(0.94)`).
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "",
    icon: ImageVector? = null,
    accentColor: Color = LocalGlassAccent.current.color,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(18.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleAnim = remember { Animatable(1.0f) }

    LaunchedEffect(isPressed) {
        if (isPressed && enabled) {
            scaleAnim.animateTo(0.94f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
        } else {
            scaleAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    val buttonBg = if (enabled) accentColor else Color.Gray.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .scale(scaleAnim.value)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = shape,
                ambientColor = buttonBg,
                spotColor = Color.Black
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        buttonBg.copy(alpha = if (isPressed) 0.95f else 0.85f),
                        buttonBg.copy(alpha = if (isPressed) 0.75f else 0.65f)
                    )
                ),
                shape = shape
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.50f),
                        buttonBg.copy(alpha = 0.80f),
                        Color.White.copy(alpha = 0.20f)
                    )
                ),
                shape = shape
            )
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                if (text.isNotEmpty()) Spacer(modifier = Modifier.width(8.dp))
            }
            if (text.isNotEmpty()) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Liquid Glass Icon Button with spring scale feedback.
 */
@Composable
fun LiquidGlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalGlassAccent.current.color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scaleAnim = remember { Animatable(1.0f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            scaleAnim.animateTo(0.90f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
        } else {
            scaleAnim.animateTo(1.0f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    Box(
        modifier = modifier
            .scale(scaleAnim.value)
            .size(44.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.22f))
            .border(1.2.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.6f), accentColor.copy(alpha = 0.4f))), CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Android 17 Liquid Glass animated Switch with liquid morphing thumb.
 */
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalGlassAccent.current.color
) {
    val trackWidth = 52.dp
    val trackHeight = 28.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 2.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "switch_thumb"
    )

    val trackBg = if (checked) accentColor else Color.White.copy(alpha = 0.18f)

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(trackHeight)
            .clip(CircleShape)
            .background(trackBg)
            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .shadow(4.dp, CircleShape, spotColor = Color.Black)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFE2E8F0))
                    )
                )
        )
    }
}

/**
 * Liquid Glass Text Input Field with focus border refraction and translucent background.
 */
@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val accent = LocalGlassAccent.current.color

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.30f),
            focusedContainerColor = Color.Black.copy(alpha = 0.40f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.22f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), ambientColor = accent.copy(alpha = 0.3f))
    )
}

/**
 * Liquid Glass Translucent Top App Bar with specular bottom line.
 */
@Composable
fun LiquidGlassTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = Color(0xFF0A0C14).copy(alpha = 0.70f),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navigationIcon?.invoke()
                if (navigationIcon != null) Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = LiquidGlassTypography.titleMedium,
                        color = Color.White
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = LiquidGlassTypography.labelSmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
                actions()
            }
            // Specular Bottom Line Refraction
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                LocalGlassAccent.current.color.copy(alpha = 0.50f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

