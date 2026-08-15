package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

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
        themeMode: String = "system",
        accentColorHex: String = "#4CAF50",
        dynamicColor: Boolean = false,
        content: @Composable () -> Unit
    ) {
        MaterialTheme(
            content = content
        )
    }
}