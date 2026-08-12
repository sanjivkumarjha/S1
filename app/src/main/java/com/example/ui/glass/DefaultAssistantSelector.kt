package com.example.ui.glass

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.launch

data class AssistantAppOption(
    val packageName: String,
    val displayName: String,
    val subtitle: String,
    val badgeText: String,
    val isInstalled: Boolean,
    val iconVector: ImageVector,
    val isSnaper: Boolean = false
)

fun openSystemAssistantSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
            if (!roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)) {
                try {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    Toast.makeText(context, "Select 'Snaper Technology' as your Digital Assistant App", Toast.LENGTH_LONG).show()
                    return
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    try {
        val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        Toast.makeText(context, "Select 'Snaper Technology' as your Digital Assistant App", Toast.LENGTH_LONG).show()
    } catch (e1: Exception) {
        try {
            val fallbackIntent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
            Toast.makeText(context, "Open Default Apps > Digital Assistant to select Snaper Technology", Toast.LENGTH_LONG).show()
        } catch (e2: Exception) {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(settingsIntent)
            Toast.makeText(context, "Open Apps > Default Apps > Assistant to set Snaper Technology", Toast.LENGTH_LONG).show()
        }
    }
}

fun launchAssistantApp(context: Context, packageName: String) {
    if (packageName == context.packageName || packageName == "com.example") {
        Toast.makeText(context, "Snaper AI Assistant is active!", Toast.LENGTH_SHORT).show()
        return
    }

    val pm = context.packageManager
    val launchIntent = pm.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
    } else {
        // Try generic voice command intent or open Play Store
        try {
            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            storeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(storeIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }
}

@Composable
fun DefaultAssistantSelector(
    userSettings: UserSettings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }
    var isGuideVisible by remember { mutableStateOf(false) }

    val pm = context.packageManager

    val isGoogleInstalled = remember {
        try {
            pm.getPackageInfo("com.google.android.googlequicksearchbox", 0)
            true
        } catch (e: Exception) {
            try {
                pm.getPackageInfo("com.google.android.apps.googleassistant", 0)
                true
            } catch (e2: Exception) {
                false
            }
        }
    }

    val isChatGPTInstalled = remember {
        try {
            pm.getPackageInfo("com.openai.chatgpt", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Query other installed assistant apps
    val otherAssistants = remember {
        val list = mutableListOf<AssistantAppOption>()
        val addedPackages = mutableSetOf<String>()
        try {
            val assistIntents = listOf(
                Intent(Intent.ACTION_ASSIST),
                Intent(Intent.ACTION_VOICE_COMMAND)
            )
            for (intent in assistIntents) {
                val resolvedList = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                for (info in resolvedList) {
                    val pkg = info.activityInfo.packageName
                    if (pkg != context.packageName &&
                        pkg != "com.example" &&
                        pkg != "com.google.android.googlequicksearchbox" &&
                        pkg != "com.google.android.apps.googleassistant" &&
                        pkg != "com.openai.chatgpt" &&
                        !addedPackages.contains(pkg)
                    ) {
                        addedPackages.add(pkg)
                        val appLabel = info.loadLabel(pm).toString()
                        list.add(
                            AssistantAppOption(
                                packageName = pkg,
                                displayName = appLabel,
                                subtitle = "Installed Digital Assistant App",
                                badgeText = "System App",
                                isInstalled = true,
                                iconVector = Icons.Default.SmartToy
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    val defaultOptions = listOf(
        AssistantAppOption(
            packageName = "com.example",
            displayName = "Snaper Technology",
            subtitle = "Custom Multimodal Voice & On-Device Assistant",
            badgeText = "Recommended",
            isInstalled = true,
            iconVector = Icons.Default.Psychology,
            isSnaper = true
        ),
        AssistantAppOption(
            packageName = "com.google.android.googlequicksearchbox",
            displayName = "Google Assistant / Gemini",
            subtitle = if (isGoogleInstalled) "Installed System Voice Service" else "Tap to install or launch",
            badgeText = "Google AI",
            isInstalled = isGoogleInstalled,
            iconVector = Icons.Default.Star
        ),
        AssistantAppOption(
            packageName = "com.openai.chatgpt",
            displayName = "ChatGPT Assistant",
            subtitle = if (isChatGPTInstalled) "Installed OpenAI Voice Service" else "Tap to view on Play Store",
            badgeText = "OpenAI",
            isInstalled = isChatGPTInstalled,
            iconVector = Icons.Default.Android
        )
    )

    val allOptions = defaultOptions + otherAssistants

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("default_assistant_selector_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Default Digital Assistant",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Android 13+ System Voice Engine Switcher",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { isGuideVisible = !isGuideVisible }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "System Guide",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // One-Tap "Set as Default Assistant" Button for Snaper Technology
            Button(
                onClick = {
                    scope.launch {
                        prefsRepo.updateSelectedAssistantPackage("com.example")
                    }
                    openSystemAssistantSettings(context)
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("set_snaper_default_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Set Snaper as Default Assistant",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp
                    )
                }
            }

            // Android 13+ Instructions guide dropdown
            AnimatedVisibility(
                visible = isGuideVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "💡 How to enable Snaper Technology as Default:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "1. Tap 'Set Snaper as Default Assistant' above.\n2. Tap 'Digital assistant app' in Android System Settings.\n3. Choose 'Snaper Technology' to complete system-wide integration.",
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector list of options
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                allOptions.forEach { option ->
                    val isSelected = userSettings.selectedAssistantPackage == option.packageName

                    Surface(
                        onClick = {
                            scope.launch {
                                prefsRepo.updateSelectedAssistantPackage(option.packageName)
                            }
                            if (option.isSnaper) {
                                openSystemAssistantSettings(context)
                            } else {
                                launchAssistantApp(context, option.packageName)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.8.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = option.displayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (option.isSnaper) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = option.badgeText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (option.isSnaper) Color.White else MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = option.subtitle,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            if (!option.isSnaper) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = "Open App",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssistantSelectorComponent(
    userSettings: UserSettings,
    modifier: Modifier = Modifier
) {
    DefaultAssistantSelector(userSettings = userSettings, modifier = modifier)
}

