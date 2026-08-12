package com.example.ui.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.GlobalTimeManager

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    accentColor: Color = LocalGlassAccent.current.color,
    themeMode: GlassThemeMode = LocalGlassThemeMode.current,
    content: @Composable BoxScope.() -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = accentColor,
        borderColor = accentColor,
        elevationDp = 10.dp,
        themeMode = themeMode,
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    accentColor: Color = LocalGlassAccent.current.color,
    themeMode: GlassThemeMode = LocalGlassThemeMode.current,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = accentColor,
        borderColor = accentColor,
        elevationDp = 10.dp,
        onClick = onClick,
        themeMode = themeMode
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalGlassAccent.current.color,
    icon: ImageVector? = null,
    testTag: String = "glass_button"
) {
    LiquidGlassButton(
        onClick = onClick,
        modifier = modifier.testTag(testTag),
        text = text,
        icon = icon,
        accentColor = accentColor,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = LocalGlassAccent.current.color
) {
    LiquidGlassIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        accentColor = accentColor
    )
}

@Composable
fun GlassTopBar(
    title: String,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    LiquidGlassTopBar(
        title = title,
        subtitle = subtitle,
        navigationIcon = navigationIcon,
        actions = actions
    )
}

@Composable
fun GlassBottomBar(
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF10121D).copy(alpha = 0.92f),
                        Color(0xFF08090F).copy(alpha = 0.98f)
                    )
                )
            )
            .border(width = 1.2.dp, brush = Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.35f), LocalGlassAccent.current.color.copy(alpha = 0.60f), Color.White.copy(alpha = 0.20f))), shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null,
    testTag: String = "glass_text_field"
) {
    LiquidGlassTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier.testTag(testTag),
        trailingIcon = trailingIcon
    )
}

@Composable
fun GlassChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = LocalGlassAccent.current.color
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSelected) accent else Color.White.copy(alpha = 0.14f))
            .border(1.2.dp, if (isSelected) Brush.linearGradient(listOf(Color.White.copy(alpha = 0.6f), accent)) else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.3f), Color.Transparent)), CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun GlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun GlassSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LocalGlassAccent.current.color,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

/**
 * MANDATORY Official Branding Footer on Every Major Screen.
 * Renders "Made by Snaper Technology Private Limited" in an elegant Liquid Glass badge.
 */
@Composable
fun GlassFooter(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.30f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                LocalGlassAccent.current.color.copy(alpha = 0.25f)
            )
        ) {
            Text(
                text = com.example.domain.branding.ProtectedBranding.PROTECTED_BRAND_CREDIT,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Live Clock Widget displaying HOURS : MINUTES : SECONDS continuously.
 */
@Composable
fun GlassLiveClockWidget(
    modifier: Modifier = Modifier
) {
    val timeState by GlobalTimeManager.timeState.collectAsState()

    GlassSurface(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = timeState.dayOfWeek.uppercase() + " • " + timeState.fullDateString,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalGlassAccent.current.color,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = timeState.formatted12HourWithSeconds,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Live Weather Widget displaying temperature, location, condition & last updated time WITH SECONDS.
 */
@Composable
fun GlassWeatherWidget(
    temperature: String = "29°C",
    condition: String = "Clear Sky",
    location: String = "Haridwar, IN",
    modifier: Modifier = Modifier
) {
    val timeState by GlobalTimeManager.timeState.collectAsState()

    GlassSurface(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Weather",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "$temperature • $condition",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$location • Updated ${timeState.formatted12HourWithSeconds}",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Radha Naam Jap Routine & Counter Widget
 */
@Composable
fun GlassRadhaJapWidget(
    modifier: Modifier = Modifier
) {
    var count by rememberSaveable { mutableStateOf(0) }
    val malaCount = count / 108
    val remainder = count % 108

    GlassSurface(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF43F5E).copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFB7185))
                ) {
                    Box(
                        modifier = Modifier.size(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Radha Naam Jap",
                            tint = Color(0xFFFB7185),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "श्री राधा नाम जप • $count",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (malaCount > 0) "Mala completed: $malaCount ($remainder/108)" else "Daily Jap Goal: 108 ($remainder/108)",
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { count = 0 },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { count++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF43F5E)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tap Jap", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Jap", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
