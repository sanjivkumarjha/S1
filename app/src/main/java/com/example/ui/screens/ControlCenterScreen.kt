package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.avatar.AgentAvatarManager
import com.example.ui.glass.*

data class ControlCenterModule(
    val title: String,
    val category: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun ControlCenterScreen(
    onNavigateToRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val avatarManager = remember { AgentAvatarManager(context) }
    val currentAvatar by avatarManager.currentAvatarState.collectAsState()
    val currentExpression by avatarManager.currentExpressionState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var searchFeedback by remember { mutableStateOf("") }

    val modules = remember {
        listOf(
            ControlCenterModule("App Settings & Voice Engine", "SETTINGS", Icons.Default.Settings, "settings_main"),
            ControlCenterModule("AI Chat Assistant", "AI", Icons.Default.Chat, "ai_chat"),
            ControlCenterModule("Gemini AI Studio", "STUDIO", Icons.Default.AutoAwesome, "gemini_studio"),
            ControlCenterModule("Voice Assistant", "VOICE", Icons.Default.Mic, "voice_assistant"),
            ControlCenterModule("Universal Social & Reel AI", "SOCIAL", Icons.Default.Share, "social_media_search"),
            ControlCenterModule("App Control & Aliases", "APPS", Icons.Default.Apps, "app_control"),
            ControlCenterModule("Spam & Security", "SECURITY", Icons.Default.Shield, "security_dashboard"),
            ControlCenterModule("Smart Home", "SMART HOME", Icons.Default.Home, "smart_home"),
            ControlCenterModule("Agent Avatar", "AVATAR", Icons.Default.Face, "avatar_gallery"),
            ControlCenterModule("Gallery & Photos", "GALLERY", Icons.Default.PhotoLibrary, "gallery_search"),
            ControlCenterModule("Snaper Tech Automation", "BUSINESS", Icons.Default.BusinessCenter, "business_automation"),
            ControlCenterModule("Customer CRM", "CUSTOMERS", Icons.Default.People, "customer_crm"),
            ControlCenterModule("Birthday Manager", "BIRTHDAY", Icons.Default.Cake, "birthday_manager"),
            ControlCenterModule("Memory & Facts", "MEMORY", Icons.Default.Psychology, "settings_memory"),
            ControlCenterModule("Family Profiles", "GUEST", Icons.Default.FamilyRestroom, "family_profiles"),
            ControlCenterModule("Tools & Utilities", "TOOLS", Icons.Default.Build, "tools"),
            ControlCenterModule("Owner & Identity", "SETTINGS", Icons.Default.Person, "settings_owner"),
            ControlCenterModule("Theme & Accent", "UI", Icons.Default.Palette, "settings_theme"),
            ControlCenterModule("AI Models Router", "MODELS", Icons.Default.SmartToy, "settings_api")
        )
    }

    fun handleUniversalSearch(query: String) {
        val lower = query.trim().lowercase()
        when {
            lower.contains("photo") || lower.contains("gallery") || lower.contains("find my old photo") -> {
                onNavigateToRoute("gallery_search")
            }
            lower.contains("reel") || lower.contains("short") || lower.contains("instagram") || lower.contains("youtube") || lower.contains("facebook") || lower.contains("tiktok") || lower.contains("fact check") || lower.contains("social") -> {
                onNavigateToRoute("social_media_search")
            }
            lower.contains("ac") || lower.contains("tv") || lower.contains("turn ac") || lower.contains("light") || lower.contains("smart home") -> {
                onNavigateToRoute("smart_home")
            }
            lower.contains("spam") || lower.contains("scam") || lower.contains("check spam") || lower.contains("security") -> {
                onNavigateToRoute("security_dashboard")
            }
            lower.contains("business") || lower.contains("lead") || lower.contains("target") || lower.contains("revenue") || lower.contains("invoice") || lower.contains("automation") -> {
                onNavigateToRoute("business_automation")
            }
            lower.contains("customer") || lower.contains("crm") || lower.contains("followup") -> {
                onNavigateToRoute("customer_crm")
            }
            lower.contains("avatar") || lower.contains("change avatar") -> {
                onNavigateToRoute("avatar_gallery")
            }
            lower.contains("birthday") || lower.contains("greeting") -> {
                onNavigateToRoute("birthday_manager")
            }
            lower.contains("voice") || lower.contains("speak") -> {
                onNavigateToRoute("voice_assistant")
            }
            lower.contains("memory") || lower.contains("fact") -> {
                onNavigateToRoute("settings_memory")
            }
            else -> {
                onNavigateToRoute("ai_chat")
            }
        }
    }

    DynamicLiquidGlassBackground {
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = "Snaper Control Center",
                    subtitle = "Central AI, Security, Communication & Automation Hub"
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val isWideScreen = maxWidth > 600.dp
                val columnsCount = if (isWideScreen) 3 else 2

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Universal Command Search Bar
                    GlassTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Type command: 'Turn AC to 24', 'Check spam', 'Change avatar'...",
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = { handleUniversalSearch(searchQuery) },
                            modifier = Modifier.testTag("universal_search_button")
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = LocalGlassAccent.current.color)
                        }
                    },
                    testTag = "universal_search_input"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar & Security Header Widget
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigateToRoute("avatar_gallery") }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(LocalGlassAccent.current.color.copy(alpha = 0.2f))
                                .border(2.dp, LocalGlassAccent.current.color, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Face, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Agent: ${currentAvatar.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Expression: ${currentExpression.name} • Spam Guard Active",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }

                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "System Modules",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Module Grid
                val filteredModules = if (searchQuery.isBlank()) modules else modules.filter {
                    it.title.contains(searchQuery, ignoreCase = true) || it.category.contains(searchQuery, ignoreCase = true)
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columnsCount),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredModules) { module ->
                        GlassCard(
                            onClick = { onNavigateToRoute(module.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("control_center_module_${module.route}")
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Icon(
                                    imageVector = module.icon,
                                    contentDescription = module.title,
                                    tint = LocalGlassAccent.current.color,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = module.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = module.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
                }
            }
        }
    }
}
