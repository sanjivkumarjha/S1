package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocationOn
import com.example.ui.glass.DevicePreviewSettingsCard
import com.example.ui.glass.DoctorAndModesSettingsCard
import com.example.ui.glass.SplashScreenEditorCard
import com.example.ui.glass.HomeScreenLayoutEditorCard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Work
import com.example.service.AssistantAccessibilityService
import com.example.ui.glass.DefaultAssistantSelector
import com.example.ui.glass.DynamicIslandSettingsCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.domain.LanguageDictionary
import com.example.ui.theme.parseColorHex
import kotlinx.coroutines.launch

@Composable
fun SettingsMainScreen(
    userSettings: UserSettings,
    onNavigateBack: () -> Unit,
    onNavigateToOwner: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToApi: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToAssistantIdentity: () -> Unit = {},
    onNavigateToAppControl: () -> Unit = {},
    onNavigateToSecurityDashboard: () -> Unit = {},
    onNavigateToFamilyProfiles: () -> Unit = {},
    onNavigateToCustomerCrm: () -> Unit = {},
    onNavigateToBirthdayManager: () -> Unit = {},
    onNavigateToGallerySearch: () -> Unit = {},
    onNavigateToSmartHome: () -> Unit = {},
    onNavigateToStorageCache: () -> Unit = {},
    onNavigateToNetwork: () -> Unit = {},
    onNavigateToVehicleConnectivity: () -> Unit = {},
    onNavigateToAppIcon: () -> Unit = {},
    onNavigateToAssistantStartup: () -> Unit = {},
    onNavigateToPhoneFinder: () -> Unit = {},
    onNavigateToResourceMonitor: () -> Unit = {}
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val dict = LanguageDictionary(userSettings.languageCode)
    val scrollState = rememberScrollState()

    var isQuickThemeOpen by remember { mutableStateOf(false) }
    var isQuickColorOpen by remember { mutableStateOf(false) }
    var isQuickLangOpen by remember { mutableStateOf(false) }

    val accentPresets = listOf(
        "#8B5CF6" to "Violet",
        "#06B6D4" to "Cyan",
        "#10B981" to "Emerald",
        "#F43F5E" to "Rose",
        "#F59E0B" to "Amber"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Top Header Bar
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
            Column {
                Text(
                    text = dict.getString("nav_settings"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Personalize Snaper AI & System Control",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
        }

        // Section 1: User & Verification
        Text(
            text = "Owner Identity",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        SettingsTile(
            title = dict.getString("settings_owner"),
            subtitle = "${userSettings.ownerName} • ${userSettings.ownerBio}",
            icon = Icons.Default.Person,
            onClick = onNavigateToOwner
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Owner Verification Mode",
            subtitle = if (userSettings.isVoiceVerified) "Status: Verified & Unlocked ✅" else "Status: Unverified / Restricted",
            icon = Icons.Default.Security,
            onClick = onNavigateToOwner
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Assistant Identity & Wake Name",
            subtitle = "Name: ${userSettings.assistantName} • Wake: '${userSettings.wakePhrase}'",
            icon = Icons.Default.Badge,
            onClick = onNavigateToAssistantIdentity
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Smartphone Device Preview Card (Flagship Auto-Detection)
        DevicePreviewSettingsCard(userSettings = userSettings)

        Spacer(modifier = Modifier.height(16.dp))

        // Doctor Mode & Vehicle Driving Mode Assistant Settings Card
        DoctorAndModesSettingsCard(userSettings = userSettings)

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Island & Always-On Display Glossy Control Settings Card
        DynamicIslandSettingsCard(userSettings = userSettings)

        Spacer(modifier = Modifier.height(16.dp))

        // Default Digital Assistant Selector Component in Liquid Glass Style
        DefaultAssistantSelector(userSettings = userSettings)

        Spacer(modifier = Modifier.height(16.dp))

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "App Control & Custom Aliases",
            subtitle = "Package Manager resolution, WhatsApp vs WhatsApp Business & Aliases",
            icon = Icons.Default.Android,
            onClick = onNavigateToAppControl
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Security Dashboard & Emergency Guard",
            subtitle = "Active Mode: ${userSettings.securityMode} • Intrusion Logs & Emergency Lock",
            icon = Icons.Default.Shield,
            onClick = onNavigateToSecurityDashboard
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Personal Assistant → Phone Finder (“Where Are You?”)",
            subtitle = "Voice trigger, surface recognition, last seen memory & diagnostics test",
            icon = Icons.Default.Search,
            onClick = onNavigateToPhoneFinder
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Personal Assistant → Resource Monitor 📊",
            subtitle = "Process CPU, GPU status, RAM footprint, Storage breakdown & 30-day timeline",
            icon = Icons.Default.Memory,
            onClick = onNavigateToResourceMonitor
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Family Profiles & Privacy Firewall",
            subtitle = "Mother, Father, Sister profiles • App access & Private Firewall",
            icon = Icons.Default.FamilyRestroom,
            onClick = onNavigateToFamilyProfiles
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Customer CRM & Call Assistant",
            subtitle = "Track customer follow-ups, call notes & CRM history",
            icon = Icons.Default.Work,
            onClick = onNavigateToCustomerCrm
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Birthday Manager & Card Generator",
            subtitle = "Save birthdays & generate customized greeting cards with frames",
            icon = Icons.Default.Cake,
            onClick = onNavigateToBirthdayManager
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Gallery Search & Natural Vision",
            subtitle = "Search device photos by name/date & enhance photo filters",
            icon = Icons.Default.Image,
            onClick = onNavigateToGallerySearch
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Universal Smart Home Dashboard",
            subtitle = "Wi-Fi, Bluetooth, Matter, IR, Weather Auto & Scenes",
            icon = Icons.Default.Shield,
            onClick = onNavigateToSmartHome
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Storage & App Cache",
            subtitle = "Clear temporary cache safely without losing user memories or profile",
            icon = Icons.Default.Storage,
            onClick = onNavigateToStorageCache
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Intelligent Network & Offline Mode",
            subtitle = "Zero UI lag, weak network resilience & airplane mode intelligence",
            icon = Icons.Default.NetworkCheck,
            onClick = onNavigateToNetwork
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Universal Smart Vehicle Connectivity",
            subtitle = "Telemetry HUD, smart EV/scooter discovery & remote controls",
            icon = Icons.Default.DirectionsCar,
            onClick = onNavigateToVehicleConnectivity
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "App Icon Customization",
            subtitle = "512×512 custom icon • Persistent launcher icon & device preview sync",
            icon = Icons.Default.PhotoLibrary,
            onClick = onNavigateToAppIcon
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = "Assistant Auto-Start & Boot Settings",
            subtitle = "Device boot background initialization & state restoration",
            icon = Icons.Default.PowerSettingsNew,
            onClick = onNavigateToAssistantStartup
        )


        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: Personalization & Instant Theme
        Text(
            text = "Appearance & Instant Styling",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // Instant Theme Switcher Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isQuickThemeOpen = !isQuickThemeOpen }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Instant Theme Switcher", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Current: ${userSettings.themeMode.name}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isQuickThemeOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            val isSelected = userSettings.themeMode == mode
                            Surface(
                                onClick = {
                                    scope.launch { prefsRepo.updateThemeMode(mode) }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = mode.name.lowercase().capitalize(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Instant Color Picker Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isQuickColorOpen = !isQuickColorOpen }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = parseColorHex(userSettings.accentColorHex),
                            modifier = Modifier.size(20.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Instant Accent Color Picker", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Selected Hex: ${userSettings.accentColorHex}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isQuickColorOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        accentPresets.forEach { (hex, name) ->
                            val isSelected = userSettings.accentColorHex.equals(hex, ignoreCase = true)
                            Surface(
                                onClick = { scope.launch { prefsRepo.updateAccentColor(hex) } },
                                shape = CircleShape,
                                color = parseColorHex(hex),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = name, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Instant Language Change Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isQuickLangOpen = !isQuickLangOpen }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Instant Language Switcher", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Active Code: ${userSettings.languageCode.uppercase()}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isQuickLangOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("en" to "English", "hi" to "Hindi", "ne" to "Nepali").forEach { (code, label) ->
                            val isSelected = userSettings.languageCode == code
                            Surface(
                                onClick = { scope.launch { prefsRepo.updateLanguage(code) } },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Snaper Visual Customization & Layout Editors
        Text(
            text = "Splash & Layout Customization",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // Visual Splash Screen Live Editor
        SplashScreenEditorCard(
            userSettings = userSettings,
            repo = prefsRepo
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Home Screen Drag & Reorder Layout Editor
        HomeScreenLayoutEditorCard(
            userSettings = userSettings,
            repo = prefsRepo
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Intelligence & System Controls
        Text(
            text = "AI Engine & Privacy",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        SettingsTile(
            title = "Screen Control & Accessibility",
            subtitle = if (AssistantAccessibilityService.isAccessibilityServiceEnabled(context)) "Service Enabled: Ready to read screen & perform clicks ✅" else "Service Disabled: Tap to open system settings",
            icon = Icons.Default.TouchApp,
            onClick = {
                context.startActivity(AssistantAccessibilityService.openAccessibilitySettingsIntent())
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = dict.getString("settings_api"),
            subtitle = "Provider: ${userSettings.aiProvider.displayName} (${userSettings.selectedModel})",
            icon = Icons.Default.Api,
            onClick = onNavigateToApi
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = dict.getString("settings_memory"),
            subtitle = "100% On-device Local Room DB Memories",
            icon = Icons.Default.Memory,
            onClick = onNavigateToMemory
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = dict.getString("privacy_policy"),
            subtitle = "Zero tracking, on-device data guarantee",
            icon = Icons.Default.PrivacyTip,
            onClick = onNavigateToPrivacy
        )

        Spacer(modifier = Modifier.height(8.dp))

        SettingsTile(
            title = dict.getString("about_app"),
            subtitle = "Snaper Technology v1.0 • Built with Kotlin & Compose",
            icon = Icons.Default.Info,
            onClick = onNavigateToAbout
        )

        Spacer(modifier = Modifier.height(16.dp))

        // MANDATORY Official Company Branding
        com.example.ui.glass.GlassFooter()
    }
}

@Composable
fun SettingsTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
