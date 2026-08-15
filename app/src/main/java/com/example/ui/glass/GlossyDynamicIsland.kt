package com.example.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.data.preferences.UserSettings

/**
 * Glossy Dynamic Island composable for the Snaper AI Assistant.
 * Renders an interactive, AMOLED-optimized Dynamic Island overlay.
 */
object GlossyDynamicIsland {

    @Composable
    fun DynamicIslandOverlay(
        state: DynamicIslandState,
        text: String,
        onDismiss: () -> Unit = {}
    ) {
        // Basic Compose UI rendering for the Dynamic Island overlay
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = text.ifBlank { state.name })
            Button(onClick = onDismiss) {
                Text("Dismiss")
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
        // Render the Dynamic Island using the provided state
        val displayText = when (islandState) {
            DynamicIslandState.DYNAMIC_ISLAND_AOD -> "Locked"
            DynamicIslandState.EXPANDED -> "Expanded"
            DynamicIslandState.COLLAPSED -> "Collapsed"
            else -> islandState.name
        }
        DynamicIslandOverlay(
            state = islandState,
            text = displayText,
            onDismiss = {
                onUnlockRequest(islandState)
                onExpandToggle()
            }
        )
    }
}