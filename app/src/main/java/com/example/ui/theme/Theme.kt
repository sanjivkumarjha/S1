package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.preferences.ThemeMode

fun parseColorHex(hex: String, defaultColor: Color = VioletPrimary): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = cleanHex.toLong(16)
        if (cleanHex.length == 6) {
            Color(colorInt or 0xFF000000)
        } else if (cleanHex.length == 8) {
            Color(colorInt)
        } else defaultColor
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun SnaperTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    accentColorHex: String = "#8B5CF6",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val primaryColor = parseColorHex(accentColorHex, VioletPrimary)
    val isSystemDark = isSystemInDarkTheme()

    val effectiveDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == ThemeMode.AMOLED -> darkColorScheme(
            primary = primaryColor,
            secondary = VioletSecondary,
            tertiary = CyanSecondary,
            background = AmoledBackground,
            surface = AmoledSurface,
            surfaceVariant = AmoledSurfaceVariant,
            onBackground = AmoledOnBackground,
            onSurface = AmoledOnSurface
        )
        effectiveDark -> darkColorScheme(
            primary = primaryColor,
            secondary = VioletSecondary,
            tertiary = CyanSecondary,
            background = DarkBackground,
            surface = DarkSurface,
            surfaceVariant = DarkSurfaceVariant,
            onBackground = DarkOnBackground,
            onSurface = DarkOnSurface
        )
        else -> lightColorScheme(
            primary = primaryColor,
            secondary = VioletSecondary,
            tertiary = CyanPrimary,
            background = LightBackground,
            surface = LightSurface,
            surfaceVariant = LightSurfaceVariant,
            onBackground = LightOnBackground,
            onSurface = LightOnSurface
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
