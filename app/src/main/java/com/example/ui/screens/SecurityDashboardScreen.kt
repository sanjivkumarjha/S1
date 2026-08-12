package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.security.*
import com.example.ui.glass.*
import kotlinx.coroutines.launch

enum class SecurityTab {
    DASHBOARD,
    SMARTPHONE,
    VEHICLE,
    HOME,
    FAMILY,
    FIND_MY_PHONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityDashboardScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val centralSecurity = remember { CentralizedSecurityManager.getInstance(context) }
    val findManager = centralSecurity.findMyAssistantManager
    val familyManager = centralSecurity.familySosManager
    val homeManager = centralSecurity.homeSecurityManager
    val vehicleManager = centralSecurity.vehicleSecurityManager
    val secureAuthManager = remember { SecureDeviceAuthManager(context) }
    val prefsRepo = remember { UserPreferencesRepository(context) }

    val userSettings by prefsRepo.userSettingsFlow.collectAsState(initial = UserSettings())
    val activeAlert by centralSecurity.activeAlert.collectAsState()
    val isProtectedMode by centralSecurity.isProtectedModeActive.collectAsState()
    val isLostMode by centralSecurity.isLostDeviceModeActive.collectAsState()

    val isRinging by findManager.isRinging.collectAsState()
    val isFlashlightOn by findManager.isFlashlightOn.collectAsState()
    val findingStatus by findManager.findingStatusText.collectAsState()
    val visualSearchActive by findManager.visualSearchActive.collectAsState()

    val sosProgress by familyManager.sosProgress.collectAsState()
    val familyContacts by familyManager.familyContacts.collectAsState()

    val cctvCameras by homeManager.cctvCameras.collectAsState()
    val smartLocks by homeManager.smartLocks.collectAsState()
    val isSirenActive by homeManager.isSirenActive.collectAsState()
    val homeHistory by homeManager.securityHistory.collectAsState()

    val isVehicleAntiTheft by vehicleManager.isAntiTheftEnabled.collectAsState()
    val trustedDrivers by vehicleManager.trustedDrivers.collectAsState()

    var selectedTab by remember { mutableStateOf(SecurityTab.DASHBOARD) }
    var statusMessage by remember { mutableStateOf("Snaper Centralized Security System Online.") }

    // Dialog state
    var showAddFamilyDialog by remember { mutableStateOf(false) }
    var newContactName by remember { mutableStateOf("") }
    var newContactRel by remember { mutableStateOf("") }
    var newContactPhone by remember { mutableStateOf("") }

    var showAddDriverDialog by remember { mutableStateOf(false) }
    var newDriverName by remember { mutableStateOf("") }
    var newDriverRole by remember { mutableStateOf("Authorized Driver") }

    DynamicLiquidGlassBackground {
        Scaffold(
            topBar = {
                GlassTopBar(
                    title = "Centralized Security Center",
                    subtitle = "Smartphone • Vehicle • Home • Family • Find My Assistant",
                    navigationIcon = {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("security_center_back")) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Security Protection Master Header
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (userSettings.securityMode != "NORMAL") LocalGlassAccent.current.color else Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Security Protection Master", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Status: ${if (userSettings.securityMode != "NORMAL") "ACTIVE & PROTECTED" else "NORMAL"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                            Switch(
                                checked = userSettings.securityMode != "NORMAL",
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        centralSecurity.setProtectedMode(enabled, "Master switch toggle")
                                        statusMessage = if (enabled) "Master Security Protection Enabled" else "Master Security Protection set to Normal"
                                    }
                                },
                                modifier = Modifier.testTag("master_security_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Category Tabs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            SecurityTab.values().forEach { tab ->
                                val isSelected = selectedTab == tab
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) LocalGlassAccent.current.color.copy(alpha = 0.3f) else Color.Transparent)
                                        .clickable { selectedTab = tab }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (tab) {
                                            SecurityTab.DASHBOARD -> "Hub"
                                            SecurityTab.SMARTPHONE -> "Phone"
                                            SecurityTab.VEHICLE -> "Vehicle"
                                            SecurityTab.HOME -> "Home"
                                            SecurityTab.FAMILY -> "Family"
                                            SecurityTab.FIND_MY_PHONE -> "Find"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Alert Banner if present
                activeAlert?.let { alert ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        accentColor = Color(0xFFEF4444)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(alert.title, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                    Text(alert.message, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                }
                            }
                            IconButton(onClick = { centralSecurity.clearAlert() }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = Color.Gray)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (selectedTab) {
                        SecurityTab.DASHBOARD -> {
                            item {
                                Text("SECURITY DASHBOARD OVERVIEW", fontWeight = FontWeight.Bold, color = LocalGlassAccent.current.color, fontSize = 12.sp)
                            }

                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTab = SecurityTab.SMARTPHONE }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Smartphone, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("📱 Smartphone Security", fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = if (isProtectedMode) "Protected Mode Active • Anti-Theft Armed" else "Standard Protection Active",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray
                                            )
                                        }
                                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }

                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTab = SecurityTab.VEHICLE }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("🚗 Vehicle Security", fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = if (isVehicleAntiTheft) "Anti-Theft Active • Movement Monitored" else "Vehicle Connected • Telemetry Normal",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray
                                            )
                                        }
                                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }

                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTab = SecurityTab.HOME }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.HomeWork, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("🏠 Home Security", fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = "${cctvCameras.size} Cameras Live • ${smartLocks.filter { it.isLocked }.size}/${smartLocks.size} Locks Secured",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray
                                            )
                                        }
                                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }

                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTab = SecurityTab.FAMILY }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.People, contentDescription = null, tint = Color(0xFFEC4899), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("👨‍👩‍👧 Family Protection & SOS", fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = "${familyContacts.size} Emergency Contacts Configured • SOS Ready",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray
                                            )
                                        }
                                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }

                            item {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedTab = SecurityTab.FIND_MY_PHONE }
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF8B5CF6), modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("📍 Find My Assistant", fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(
                                                text = "Voice command active • “रोशनी, तुम कहाँ हो?”",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.LightGray
                                            )
                                        }
                                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                                    }
                                }
                            }
                        }

                        SecurityTab.SMARTPHONE -> {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Protected Mode", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("When active: private memories, vehicle & home controls are restricted until owner biometrics/PIN verified.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Enable Protected Mode", color = Color.White)
                                        Switch(
                                            checked = isProtectedMode,
                                            onCheckedChange = { active ->
                                                scope.launch {
                                                    centralSecurity.setProtectedMode(active, "User toggle in settings")
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Lost Device Mode", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Locks private assistant data, preserves authorized GPS location, and sends emergency alerts if lost.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Activate Lost Device Mode", color = Color.White)
                                        Switch(
                                            checked = isLostMode,
                                            onCheckedChange = { active ->
                                                scope.launch {
                                                    centralSecurity.setLostDeviceMode(active)
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Emergency Device Lock & Biometrics", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Authenticate using system Android BiometricPrompt or PIN to unlock restricted capabilities.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    GlassButton(
                                        text = "Authenticate via System Biometrics",
                                        onClick = {
                                            val activity = context as? FragmentActivity
                                            if (activity != null) {
                                                secureAuthManager.authenticateOwner(
                                                    activity = activity,
                                                    title = "Verify Identity • Snaper",
                                                    subtitle = "Biometric & System Keyguard Unlock",
                                                    onSuccess = {
                                                        scope.launch {
                                                            centralSecurity.setProtectedMode(false, "Biometric success")
                                                            statusMessage = "Biometric verification successful! Protected mode unlocked."
                                                        }
                                                    },
                                                    onError = { err -> statusMessage = err }
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        SecurityTab.VEHICLE -> {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Vehicle Anti-Theft Protection", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Monitors unauthorized movement, ignition state, and charging events.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Anti-Theft Mode", color = Color.White)
                                        Switch(
                                            checked = isVehicleAntiTheft,
                                            onCheckedChange = { vehicleManager.setAntiTheftEnabled(it) }
                                        )
                                    }
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Live Vehicle Telemetry & Location", fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("• Current Location: 28.6139° N, 77.2090° E (New Delhi)", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                    Text("• Vehicle State: Parked & Secured", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                                    Text("• Connection: Connected via Smart Gateway", color = Color(0xFF10B981), style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GlassButton(
                                            text = "Lock Vehicle",
                                            onClick = {
                                                vehicleManager.executeRemoteSecurityAction("v1", "LOCK_VEHICLE") { _, msg -> statusMessage = msg }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        GlassButton(
                                            text = "Flash Lights",
                                            onClick = {
                                                vehicleManager.executeRemoteSecurityAction("v1", "FLASH_LIGHTS") { _, msg -> statusMessage = msg }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Authorized Vehicle Drivers", fontWeight = FontWeight.Bold, color = Color.White)
                                        IconButton(onClick = { showAddDriverDialog = true }) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Driver", tint = LocalGlassAccent.current.color)
                                        }
                                    }
                                    trustedDrivers.forEach { driver ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(driver.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                                Text(driver.role, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                            if (driver.role != "Authorized Owner") {
                                                IconButton(onClick = { vehicleManager.removeTrustedDriver(driver.id) }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        SecurityTab.HOME -> {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Smart CCTV Live Camera Feeds", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Automated Motion & Person Detection Active", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    cctvCameras.forEach { cam ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = LocalGlassAccent.current.color)
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(cam.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                                    Text("Status: ${cam.status} • Motion: ${if (cam.isMotionDetected) "YES" else "NO"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                            }
                                            AssistChip(
                                                onClick = { },
                                                label = { Text("Feed Live") }
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Smart Lock Management", fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    smartLocks.forEach { lock ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(lock.name, color = Color.White, fontWeight = FontWeight.SemiBold)
                                                Text("State: ${if (lock.isLocked) "LOCKED 🔒" else "UNLOCKED 🔓"} • Battery: ${lock.batteryPercent}%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                            GlassButton(
                                                text = if (lock.isLocked) "Unlock" else "Lock",
                                                onClick = { homeManager.toggleSmartLock(lock.id) }
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Home Intrusion & Siren Controls", fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GlassButton(
                                            text = "Simulate Intrusion Test",
                                            onClick = { homeManager.triggerUnauthorizedEntryWorkflow("Front Door") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (isSirenActive) {
                                            GlassButton(
                                                text = "Silence Siren",
                                                onClick = { homeManager.silenceSiren() },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Security Event History Log", fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    homeHistory.forEach { log ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text(log.deviceName, color = Color.White, fontWeight = FontWeight.Bold)
                                                Text(log.timestampStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                            Text("Event: ${log.eventName}", style = MaterialTheme.typography.bodySmall, color = LocalGlassAccent.current.color)
                                            Text("Action: ${log.actionTaken}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(top = 4.dp))
                                        }
                                    }
                                }
                            }
                        }

                        SecurityTab.FAMILY -> {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Trusted Family Members", fontWeight = FontWeight.Bold, color = Color.White)
                                        IconButton(onClick = { showAddFamilyDialog = true }) {
                                            Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Add Member", tint = LocalGlassAccent.current.color)
                                        }
                                    }
                                    if (familyContacts.isEmpty()) {
                                        Text("No emergency contacts added yet. Click '+' to configure.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    } else {
                                        familyContacts.forEach { member ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text("${member.name} (${member.relationship})", color = Color.White, fontWeight = FontWeight.SemiBold)
                                                    Text("Phone: ${member.phoneNumber} • Priority: ${member.emergencyPriority}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                                IconButton(onClick = { familyManager.deleteFamilyContact(member) }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    accentColor = if (sosProgress.state != SosState.IDLE) Color(0xFFEF4444) else LocalGlassAccent.current.color
                                ) {
                                    Text("Emergency SOS Trigger & Countdown", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Sends immediate SMS location alert to configured family contacts.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (sosProgress.state == SosState.COUNTDOWN) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Sending SOS alert in: ${sosProgress.secondsRemaining}s",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFEF4444)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { familyManager.cancelSosWorkflow() },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                            ) {
                                                Text("I'm OK (Cancel SOS)")
                                            }
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                familyManager.triggerSosWorkflow(
                                                    ownerName = userSettings.ownerName
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(imageVector = Icons.Default.Warning, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("🚨 TRIGGER SOS EMERGENCY", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        SecurityTab.FIND_MY_PHONE -> {
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Find My Assistant / Find My Phone", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Designed for when you can't remember where you placed your phone (under pillow, blanket, bag, cushions, another room).", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Voice Trigger Phrases:", color = LocalGlassAccent.current.color, fontWeight = FontWeight.SemiBold)
                                    Text("• “रोशनी, तुम कहाँ हो?”", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                    Text("• “मेरे फोन, तुम कहाँ हो?”", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                    Text("• “Hey Snaper, find my phone”", color = Color.White, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Ringtone & Visual Finding Status", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Status: $findingStatus", color = LocalGlassAccent.current.color, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        GlassButton(
                                            text = if (isRinging) "Stop Ringing" else "Ring Phone Now 🔊",
                                            onClick = {
                                                if (isRinging) {
                                                    findManager.stopRinging()
                                                } else {
                                                    findManager.startPhoneFindingWorkflow { res ->
                                                        statusMessage = res.hindiText
                                                    }
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                        GlassButton(
                                            text = if (isFlashlightOn) "Flash OFF" else "Flash ON 🔦",
                                            onClick = { findManager.toggleFlashlight(!isFlashlightOn) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("Sensor Location Confidence Score", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Analyzes proximity, ambient light, and accelerometer without making false location claims.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val sensorResult = remember { findManager.calculateSensorConfidence() }
                                    Text("Confidence Level: ${sensorResult.confidence.name}", color = when(sensorResult.confidence) {
                                        com.example.security.FinderConfidence.CONFIRMED, com.example.security.FinderConfidence.HIGH_CONFIDENCE -> Color(0xFF10B981)
                                        com.example.security.FinderConfidence.LIKELY -> Color(0xFFF59E0B)
                                        else -> Color(0xFFEF4444)
                                    }, fontWeight = FontWeight.Bold)
                                    Text(sensorResult.hindiText, color = Color.White, style = MaterialTheme.typography.bodySmall)
                                    Text(sensorResult.details, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Text("On-Demand Camera Visual Search Mode", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Uses explicit visual scan mode to describe visible surroundings (e.g., pillows, couch). No secret 24/7 background camera recording.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Visual Search Mode", color = Color.White)
                                        Switch(
                                            checked = visualSearchActive,
                                            onCheckedChange = { active ->
                                                findManager.setVisualSearchActive(active)
                                                statusMessage = if (active) "Visual search active: inspecting surrounding objects." else "Visual search ended."
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LocalGlassAccent.current.color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddFamilyDialog) {
        AlertDialog(
            onDismissRequest = { showAddFamilyDialog = false },
            title = { Text("Add Emergency Contact", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newContactName, onValueChange = { newContactName = it }, label = { Text("Name (e.g., Mother)") })
                    OutlinedTextField(value = newContactRel, onValueChange = { newContactRel = it }, label = { Text("Relationship") })
                    OutlinedTextField(value = newContactPhone, onValueChange = { newContactPhone = it }, label = { Text("Phone Number") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newContactName.isNotBlank() && newContactPhone.isNotBlank()) {
                            familyManager.addFamilyContact(newContactName, newContactRel, newContactPhone)
                            newContactName = ""
                            newContactRel = ""
                            newContactPhone = ""
                            showAddFamilyDialog = false
                        }
                    }
                ) {
                    Text("Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFamilyDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAddDriverDialog) {
        AlertDialog(
            onDismissRequest = { showAddDriverDialog = false },
            title = { Text("Add Trusted Driver", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newDriverName, onValueChange = { newDriverName = it }, label = { Text("Driver Name") })
                    OutlinedTextField(value = newDriverRole, onValueChange = { newDriverRole = it }, label = { Text("Role (e.g. Family Driver)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDriverName.isNotBlank()) {
                            vehicleManager.addTrustedDriver(newDriverName, newDriverRole)
                            newDriverName = ""
                            showAddDriverDialog = false
                        }
                    }
                ) {
                    Text("Add Driver")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDriverDialog = false }) { Text("Cancel") }
            }
        )
    }
}
