package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.data.preferences.ThemeMode

/**
 * Snaper AI Assistant Theme.
 * Provides the Material3 theme configuration for the app.
 */
object SnaperTheme {

    @Composable
    fun SnaperThemeContent(
        content: @Composable () -> Unit
    ) {
        MaterialTheme(
            content = content
        )
    }

    @Composable
    fun SnaperTheme(
        themeMode: ThemeMode = ThemeMode.SYSTEM,
        accentColorHex: String = "#4CAF50",
        dynamicColor: Boolean = false,
        content: @Composable () -> Unit
    ) {
        MaterialTheme(
            content = content
        )
    }
}
