package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.ui.theme.parseColorHex
import com.example.vision.ScreenVisionManager
import kotlinx.coroutines.launch

@Composable
fun SettingsThemeScreen(
    userSettings: UserSettings,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val screenVisionManager = remember { ScreenVisionManager.getInstance(context) }

    var showResetDialog by remember { mutableStateOf(false) }

    // Color Pickers State
    var bgColorHex by remember { mutableStateOf(userSettings.customBgColorHex) }
    var chatUserHex by remember { mutableStateOf(userSettings.customChatBubbleUserHex) }
    var chatAiHex by remember { mutableStateOf(userSettings.customChatBubbleAiHex) }
    var buttonHex by remember { mutableStateOf(userSettings.customButtonColorHex) }
    var textColorHex by remember { mutableStateOf(userSettings.customTextColorHex) }
    var avatarBorderHex by remember { mutableStateOf(userSettings.customAvatarBorderHex) }
    var avatarSizeDp by remember { mutableFloatStateOf(userSettings.customAvatarSizeDp.toFloat()) }

    // Avatar Image Picker Launcher
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                prefsRepo.updateCustomAvatarImage(it.toString(), avatarSizeDp.toInt())
                Toast.makeText(context, "Custom avatar updated from Gallery!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Screen Projection Launcher
    val screenProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            screenVisionManager.onScreenCapturePermissionResult(result.resultCode, result.data)
            scope.launch { prefsRepo.setScreenVisionEnabled(true) }
            Toast.makeText(context, "Real-Time Screen Vision Activated!", Toast.LENGTH_SHORT).show()
        } else {
            scope.launch { prefsRepo.setScreenVisionEnabled(false) }
            Toast.makeText(context, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Owner Settings & Custom Theme",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Section 1: Theme Mode
        Text(
            text = "System Theme Mode",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeMode.entries.forEach { mode ->
                val isSelected = userSettings.themeMode == mode
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { scope.launch { prefsRepo.updateThemeMode(mode) } }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Custom Color Selector
        Text(
            text = "Custom UI Color Selector",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Pick custom HEX colors for background, bubbles, buttons, text, and avatar borders.",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ColorSelectorRow("App Background", bgColorHex) { hex ->
                    bgColorHex = hex
                    scope.launch { prefsRepo.updateCustomThemeColors(bgColorHex, chatUserHex, chatAiHex, buttonHex, textColorHex, avatarBorderHex) }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))

                ColorSelectorRow("User Chat Bubble", chatUserHex) { hex ->
                    chatUserHex = hex
                    scope.launch { prefsRepo.updateCustomThemeColors(bgColorHex, chatUserHex, chatAiHex, buttonHex, textColorHex, avatarBorderHex) }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))

                ColorSelectorRow("Assistant Chat Bubble", chatAiHex) { hex ->
                    chatAiHex = hex
                    scope.launch { prefsRepo.updateCustomThemeColors(bgColorHex, chatUserHex, chatAiHex, buttonHex, textColorHex, avatarBorderHex) }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))

                ColorSelectorRow("Primary Action Buttons", buttonHex) { hex ->
                    buttonHex = hex
                    scope.launch { prefsRepo.updateCustomThemeColors(bgColorHex, chatUserHex, chatAiHex, buttonHex, textColorHex, avatarBorderHex) }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))

                ColorSelectorRow("Primary Text Color", textColorHex) { hex ->
                    textColorHex = hex
                    scope.launch { prefsRepo.updateCustomThemeColors(bgColorHex, chatUserHex, chatAiHex, buttonHex, textColorHex, avatarBorderHex) }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))

                ColorSelectorRow("Avatar Border Halo", avatarBorderHex) { hex ->
                    avatarBorderHex = hex
                    scope.launch { prefsRepo.updateCustomThemeColors(bgColorHex, chatUserHex, chatAiHex, buttonHex, textColorHex, avatarBorderHex) }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Custom Owner Avatar Photo & Dimensions
        Text(
            text = "Custom Owner Avatar & Scale",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Upload Custom Photo Avatar", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (userSettings.customAvatarImageUri.isNotBlank()) "Custom Photo Active" else "Default Anime Canvas Active",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Button(
                        onClick = { avatarPickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gallery")
                    }
                }

                if (userSettings.customAvatarImageUri.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch { prefsRepo.updateCustomAvatarImage("", avatarSizeDp.toInt()) }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Remove Custom Photo (Reset to Anime)")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Avatar Size Dimension: ${avatarSizeDp.toInt()} dp", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = avatarSizeDp,
                    onValueChange = {
                        avatarSizeDp = it
                        scope.launch { prefsRepo.updateCustomAvatarImage(userSettings.customAvatarImageUri, avatarSizeDp.toInt()) }
                    },
                    valueRange = 160f..360f
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 4: Real-Time Screen Vision & Analysis
        Text(
            text = "Real-Time Screen Vision Engine",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Enable Live Screen Vision", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Allows Assistant to capture and analyze items visible on your screen via AI.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = userSettings.isScreenVisionEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            screenProjectionLauncher.launch(screenVisionManager.createScreenCaptureIntent())
                        } else {
                            screenVisionManager.stopScreenVision()
                            scope.launch { prefsRepo.setScreenVisionEnabled(false) }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section 5: 100% Local Storage & Zero-Data Hard Reset
        Text(
            text = "100% Local Privacy Engine",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Zero-Cloud Local Storage Guaranteed",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "All chat history, memories, settings, and media files are stored 100% locally on your device in Room DB and DataStore. Zero data is sent to external cloud servers.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showResetDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ZERO-DATA PRIVACY HARD RESET")
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Confirm Zero-Data Hard Reset") },
            text = { Text("This action will instantly wipe ALL local chat history, memories, settings, and preferences. This action is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        scope.launch {
                            prefsRepo.performZeroDataHardReset()
                            Toast.makeText(context, "All local data wiped successfully!", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("ERASE EVERYTHING", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ColorSelectorRow(
    label: String,
    currentHex: String,
    onColorPicked: (String) -> Unit
) {
    val presets = listOf("#8B5CF6", "#06B6D4", "#10B981", "#F43F5E", "#F59E0B", "#0F172A", "#1E293B", "#F8FAFC", "#A855F7")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(parseColorHex(currentHex))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = currentHex, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            presets.forEach { hex ->
                val color = parseColorHex(hex)
                val isSelected = currentHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable { onColorPicked(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
