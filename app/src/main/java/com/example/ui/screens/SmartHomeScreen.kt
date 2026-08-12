package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.local.entities.AutomationRuleEntity
import com.example.data.local.entities.IRCommandEntity
import com.example.data.local.entities.SmartDeviceEntity
import com.example.domain.*
import com.example.ui.glass.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHomeScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val smartHomeManager = remember { SmartHomeManager(context, scope) }
    val smartSceneManager = remember { SmartSceneManager(context) }
    val irRemoteManager = remember { IRRemoteManager(context) }
    val weatherManager = remember { WeatherAutomationManager(context) }
    val automationEngine = remember { AutomationEngine(context, scope) }
    val discoveryManager = remember { SmartDeviceDiscoveryManager(context) }

    val devicesState by smartHomeManager.devicesState.collectAsState()
    val activeCount by smartHomeManager.activeCountState.collectAsState()
    val onlineCount by smartHomeManager.onlineCountState.collectAsState()
    val offlineCount = devicesState.size - onlineCount

    val weatherState by weatherManager.weatherState.collectAsState()
    val rulesState by automationEngine.rulesState.collectAsState()
    val irCommandsState by irRemoteManager.allIRCommands.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        smartHomeManager.populateDefaultSampleDevicesIfEmpty(devicesState)
        automationEngine.createDefaultRulesIfEmpty(rulesState)
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Voice & Hub", "All Devices", "By Room", "Scenes", "Automations", "IR & Gateways")

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedProtocolFilter by remember { mutableStateOf("All Protocols") }
    var selectedRoomFilter by remember { mutableStateOf("All Rooms") }

    var statusMessage by remember { mutableStateOf("") }
    var voiceInputText by remember { mutableStateOf("") }

    var selectedCameraForStream by remember { mutableStateOf<SmartDeviceEntity?>(null) }

    DynamicLiquidGlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LiquidGlassTopBar(
                    title = "Smart Home Control Hub",
                    subtitle = "$onlineCount Online • $offlineCount Offline • $activeCount Active Devices",
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("smart_home_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    statusMessage = "Pinging local & cloud IoT network endpoints..."
                                    val (online, offline) = smartHomeManager.pingAndVerifyDeviceReachability()
                                    statusMessage = "Real-time Verification Complete: $online Devices Online, $offline Unreachable / Offline."
                                }
                            },
                            modifier = Modifier.testTag("ping_devices_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Ping Reachability",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    statusMessage = "Scanning Wi-Fi, Matter, Zigbee & mDNS..."
                                    val added = smartHomeManager.scanAndAutoRegisterDiscoveredDevices(discoveryManager)
                                    statusMessage = if (added > 0) "Discovered & registered $added new IoT devices!" else "Network scan complete. All local devices registered."
                                }
                            },
                            modifier = Modifier.testTag("discover_devices_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Discover Devices",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status Banner Message
            if (statusMessage.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { statusMessage = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss")
                        }
                    }
                }
            }

            // Navigation Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewAndVoiceTabContent(
                    weather = weatherState,
                    devices = devicesState,
                    activeCount = activeCount,
                    onlineCount = onlineCount,
                    offlineCount = offlineCount,
                    scenes = smartSceneManager.scenes,
                    voiceInputText = voiceInputText,
                    onVoiceInputChange = { voiceInputText = it },
                    onSendVoiceCommand = { cmd ->
                        scope.launch {
                            val res = smartHomeManager.processNaturalLanguageCommand(cmd)
                            statusMessage = res
                        }
                    },
                    onToggleDevice = { dev ->
                        scope.launch {
                            val msg = smartHomeManager.togglePower(dev)
                            statusMessage = msg
                        }
                    },
                    onExecuteScene = { sceneName ->
                        scope.launch {
                            val msg = smartSceneManager.executeScene(sceneName, devicesState)
                            statusMessage = msg
                        }
                    },
                    onTriggerWeatherAutomation = {
                        scope.launch {
                            val triggered = automationEngine.evaluateWeatherAndRules(weatherState, devicesState)
                            statusMessage = if (triggered.isNotEmpty()) "Weather Rules Triggered: ${triggered.joinToString()}" else "Weather conditions evaluated. System optimal."
                        }
                    },
                    onOpenCameraStream = { cameraDev ->
                        selectedCameraForStream = cameraDev
                    }
                )

                1 -> DevicesTabContent(
                    devices = devicesState,
                    selectedCategory = selectedCategoryFilter,
                    selectedProtocol = selectedProtocolFilter,
                    onSelectCategory = { selectedCategoryFilter = it },
                    onSelectProtocol = { selectedProtocolFilter = it },
                    onToggleDevice = { dev ->
                        scope.launch {
                            val msg = smartHomeManager.togglePower(dev)
                            statusMessage = msg
                        }
                    },
                    onSetOperationalMode = { dev, mode ->
                        scope.launch {
                            val msg = smartHomeManager.setOperationalMode(dev, mode)
                            statusMessage = msg
                        }
                    },
                    onOpenCameraStream = { cameraDev ->
                        selectedCameraForStream = cameraDev
                    }
                )

                2 -> DevicesByRoomTabContent(
                    devices = devicesState,
                    selectedRoom = selectedRoomFilter,
                    onSelectRoom = { selectedRoomFilter = it },
                    onToggleDevice = { dev ->
                        scope.launch {
                            val msg = smartHomeManager.togglePower(dev)
                            statusMessage = msg
                        }
                    },
                    onSetOperationalMode = { dev, mode ->
                        scope.launch {
                            val msg = smartHomeManager.setOperationalMode(dev, mode)
                            statusMessage = msg
                        }
                    },
                    onOpenCameraStream = { cameraDev ->
                        selectedCameraForStream = cameraDev
                    }
                )

                3 -> ScenesTabContent(
                    scenes = smartSceneManager.scenes,
                    onExecuteScene = { sceneName ->
                        scope.launch {
                            val msg = smartSceneManager.executeScene(sceneName, devicesState)
                            statusMessage = msg
                        }
                    }
                )

                4 -> AutomationsTabContent(
                    rules = rulesState,
                    onToggleRule = { rule ->
                        scope.launch {
                            automationEngine.toggleRule(rule)
                            statusMessage = "Updated automation rule: ${rule.ruleName}"
                        }
                    },
                    onExecuteRule = { rule ->
                        scope.launch {
                            val msg = automationEngine.executeRule(rule, devicesState)
                            statusMessage = msg
                        }
                    }
                )

                5 -> IRAndGatewaysTabContent(
                    hasIR = irRemoteManager.hasIREmitter(),
                    irCommands = irCommandsState,
                    onTransmit = { cmd ->
                        scope.launch {
                            val msg = irRemoteManager.transmitIRCommand(cmd)
                            statusMessage = msg
                        }
                    },
                    onAddCommand = { name ->
                        scope.launch {
                            irRemoteManager.saveIRCommand(name)
                            statusMessage = "Added IR command: $name"
                        }
                    }
                )
            }
        }
    }
    }

    // Live Camera Stream Modal Dialog
    selectedCameraForStream?.let { dev ->
        CameraStreamModalDialog(
            device = dev,
            onDismiss = { selectedCameraForStream = null }
        )
    }
}

@Composable
private fun OverviewAndVoiceTabContent(
    weather: WeatherAutomationManager.WeatherData,
    devices: List<SmartDeviceEntity>,
    activeCount: Int,
    onlineCount: Int,
    offlineCount: Int,
    scenes: List<SmartSceneManager.Scene>,
    voiceInputText: String,
    onVoiceInputChange: (String) -> Unit,
    onSendVoiceCommand: (String) -> Unit,
    onToggleDevice: (SmartDeviceEntity) -> Unit,
    onExecuteScene: (String) -> Unit,
    onTriggerWeatherAutomation: () -> Unit,
    onOpenCameraStream: (SmartDeviceEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Voice & Text Command Assistant Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Assistant",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart Home AI Voice Command",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = voiceInputText,
                        onValueChange = onVoiceInputChange,
                        placeholder = { Text("e.g. 'Turn off lights', 'Set AC to 22°C', 'Lock front door', 'Start vacuum'") },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (voiceInputText.isNotBlank()) {
                                        onSendVoiceCommand(voiceInputText)
                                        onVoiceInputChange("")
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = "Send Command", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("smart_home_voice_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val sampleCmds = listOf(
                            "Turn off all lights",
                            "Set AC to 22°C",
                            "Lock front door",
                            "Start robot vacuum",
                            "Ping devices"
                        )
                        items(sampleCmds) { sample ->
                            AssistChip(
                                onClick = {
                                    onVoiceInputChange(sample)
                                    onSendVoiceCommand(sample)
                                },
                                label = { Text(sample, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Weather Automation Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (weather.isRainy) Icons.Default.WaterDrop else Icons.Default.WbSunny,
                            contentDescription = "Weather",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${weather.temperatureCelsius}°C • ${weather.condition}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Feels like ${weather.feelsLikeCelsius}°C • Humidity ${weather.humidityPercent}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        FilledTonalButton(
                            onClick = onTriggerWeatherAutomation,
                            modifier = Modifier.testTag("run_weather_automation_button")
                        ) {
                            Text("Evaluate Rules")
                        }
                    }
                }
            }
        }

        // Network Status Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Online Reachable", style = MaterialTheme.typography.labelSmall)
                        Text("$onlineCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (offlineCount > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Offline / Unreachable", style = MaterialTheme.typography.labelSmall)
                        Text("$offlineCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (offlineCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Active Power", style = MaterialTheme.typography.labelSmall)
                        Text("$activeCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }

        // Quick Scene Row
        item {
            Text("Quick Scenes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(scenes) { scene ->
                    Card(
                        onClick = { onExecuteScene(scene.name) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .width(150.dp)
                            .testTag("scene_card_${scene.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(scene.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text(scene.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Highlighted Devices List
        item {
            Text("Featured Connected Devices", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(devices.take(6)) { dev ->
            DeviceListItem(
                dev = dev,
                onToggleDevice = onToggleDevice,
                onOpenCameraStream = onOpenCameraStream
            )
        }
    }
}

@Composable
private fun DevicesTabContent(
    devices: List<SmartDeviceEntity>,
    selectedCategory: String,
    selectedProtocol: String,
    onSelectCategory: (String) -> Unit,
    onSelectProtocol: (String) -> Unit,
    onToggleDevice: (SmartDeviceEntity) -> Unit,
    onSetOperationalMode: (SmartDeviceEntity, String) -> Unit,
    onOpenCameraStream: (SmartDeviceEntity) -> Unit
) {
    val categories = listOf("All", "Climate", "Lighting", "Security", "Automation", "Appliances", "Entertainment")
    val protocols = listOf("All Protocols", "Matter", "Zigbee", "Z-Wave", "Google Home", "HomeKit", "Alexa", "Tuya", "SmartThings", "MQTT", "WiFi/BLE")

    val filteredDevices = devices.filter { dev ->
        val catMatches = when (selectedCategory) {
            "Climate" -> dev.deviceType in listOf("AC", "Fan", "Thermostat", "Purifier", "Humidifier") || dev.deviceName.contains("AC", true) || dev.deviceName.contains("Purifier", true)
            "Lighting" -> dev.deviceType in listOf("Light", "Plug", "Switch", "Bulb") || dev.deviceName.contains("Light", true) || dev.deviceName.contains("Strip", true)
            "Security" -> dev.deviceType in listOf("Lock", "Camera", "Doorbell", "Alarm", "Sensor") || dev.deviceName.contains("Lock", true) || dev.deviceName.contains("Camera", true)
            "Automation" -> dev.deviceType in listOf("Curtain", "Vacuum", "Blinds", "Sprinkler") || dev.deviceName.contains("Vacuum", true) || dev.deviceName.contains("Curtain", true)
            "Appliances" -> dev.deviceName.contains("Coffee", true) || dev.deviceName.contains("Washing", true) || dev.deviceName.contains("Fridge", true)
            "Entertainment" -> dev.deviceType in listOf("TV", "Speaker", "Media") || dev.deviceName.contains("TV", true) || dev.deviceName.contains("Speaker", true)
            else -> true
        }

        val protoMatches = if (selectedProtocol == "All Protocols") true else {
            dev.protocol.contains(selectedProtocol.lowercase().replace(" ", "_"), true)
        }

        catMatches && protoMatches
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Filter Row
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { onSelectCategory(cat) },
                    label = { Text(cat) }
                )
            }
        }

        // Protocol Filter Row
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(protocols) { proto ->
                SuggestionChip(
                    onClick = { onSelectProtocol(proto) },
                    label = { Text(proto, fontSize = 11.sp) },
                    colors = if (selectedProtocol == proto) {
                        SuggestionChipDefaults.suggestionChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    } else SuggestionChipDefaults.suggestionChipColors()
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredDevices.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.WifiOff, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No devices found for selected filters.", fontWeight = FontWeight.Bold)
                            Text("Try selecting 'All' categories or running a network scan.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                items(filteredDevices) { dev ->
                    DeviceListItem(
                        dev = dev,
                        onToggleDevice = onToggleDevice,
                        onSetOperationalMode = onSetOperationalMode,
                        onOpenCameraStream = onOpenCameraStream
                    )
                }
            }
        }
    }
}

@Composable
private fun DevicesByRoomTabContent(
    devices: List<SmartDeviceEntity>,
    selectedRoom: String,
    onSelectRoom: (String) -> Unit,
    onToggleDevice: (SmartDeviceEntity) -> Unit,
    onSetOperationalMode: (SmartDeviceEntity, String) -> Unit,
    onOpenCameraStream: (SmartDeviceEntity) -> Unit
) {
    val rooms = RoomManager.defaultRooms
    val filteredDevices = if (selectedRoom == "All Rooms") devices else devices.filter { it.room == selectedRoom }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rooms) { room ->
                FilterChip(
                    selected = selectedRoom == room,
                    onClick = { onSelectRoom(room) },
                    label = { Text(room) },
                    leadingIcon = {
                        if (selectedRoom == room) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredDevices) { dev ->
                DeviceListItem(
                    dev = dev,
                    onToggleDevice = onToggleDevice,
                    onSetOperationalMode = onSetOperationalMode,
                    onOpenCameraStream = onOpenCameraStream
                )
            }
        }
    }
}

@Composable
private fun ScenesTabContent(
    scenes: List<SmartSceneManager.Scene>,
    onExecuteScene: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(scenes) { scene ->
            Card(
                onClick = { onExecuteScene(scene.name) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("full_scene_card_${scene.name}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scene.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = scene.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = { onExecuteScene(scene.name) }) {
                        Text("Activate")
                    }
                }
            }
        }
    }
}

@Composable
private fun AutomationsTabContent(
    rules: List<AutomationRuleEntity>,
    onToggleRule: (AutomationRuleEntity) -> Unit,
    onExecuteRule: (AutomationRuleEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rules) { rule ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rule.ruleName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Trigger: ${rule.triggerCondition}", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = rule.isEnabled,
                            onCheckedChange = { onToggleRule(rule) }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Action: ${rule.actionPayload}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onExecuteRule(rule) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Test Execution")
                    }
                }
            }
        }
    }
}

@Composable
private fun IRAndGatewaysTabContent(
    hasIR: Boolean,
    irCommands: List<IRCommandEntity>,
    onTransmit: (IRCommandEntity) -> Unit,
    onAddCommand: (String) -> Unit
) {
    var newCmdName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (hasIR) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (hasIR) Icons.Default.Sensors else Icons.Default.Wifi,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (hasIR) "Hardware IR Blaster Active" else "IR Hardware Fallback Mode Active",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasIR) "Transmit raw IR code frequencies (38kHz) to legacy legacy TVs/ACs." else "Controlling via WiFi / Matter / Home Assistant local bridge fallback.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        item {
            Text("Ecosystem Gateway Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GatewayStatusRow("Matter / Thread Bridge", "Online (mDNS)", true)
                    GatewayStatusRow("Tuya / Smart Life Cloud API", "Connected (WebSocket)", true)
                    GatewayStatusRow("Google Home / Nest Local API", "Active", true)
                    GatewayStatusRow("Apple HomeKit / Homebridge", "Linked", true)
                    GatewayStatusRow("Home Assistant Local Server", "Connected (http://192.168.1.5:8123)", true)
                    GatewayStatusRow("MQTT Broker (Mosquitto)", "Subscribed (1883)", true)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCmdName,
                    onValueChange = { newCmdName = it },
                    label = { Text("New Universal IR Command") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newCmdName.isNotBlank()) {
                            onAddCommand(newCmdName)
                            newCmdName = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            }
        }

        items(irCommands) { cmd ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(cmd.commandName, fontWeight = FontWeight.Bold)
                        Text("${cmd.manufacturer} • ${cmd.room}", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { onTransmit(cmd) }) {
                        Text("Transmit IR")
                    }
                }
            }
        }
    }
}

@Composable
private fun GatewayStatusRow(gatewayName: String, statusText: String, isOk: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(gatewayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isOk) Color(0xFF4CAF50) else Color(0xFFE53935))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(statusText, style = MaterialTheme.typography.labelSmall, color = if (isOk) Color(0xFF2E7D32) else Color(0xFFC62828))
        }
    }
}

@Composable
private fun DeviceListItem(
    dev: SmartDeviceEntity,
    onToggleDevice: (SmartDeviceEntity) -> Unit,
    onSetOperationalMode: ((SmartDeviceEntity, String) -> Unit)? = null,
    onOpenCameraStream: ((SmartDeviceEntity) -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Icon, Title, Strict Real-time Online/Offline Badge, Power Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (dev.powerState) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (dev.deviceType.uppercase()) {
                            "TV" -> Icons.Default.Tv
                            "AC" -> Icons.Default.Thermostat
                            "LIGHT" -> Icons.Default.Lightbulb
                            "LOCK" -> if (dev.currentValue.contains("Unlocked", true)) Icons.Default.LockOpen else Icons.Default.Lock
                            "CAMERA" -> Icons.Default.Videocam
                            else -> Icons.Default.Power
                        },
                        contentDescription = null,
                        tint = if (dev.powerState) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(dev.deviceName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(6.dp))
                        // Protocol Pill Tag
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = dev.protocol.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Strict Real-time Reachability Verification Indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (dev.isOnline) Color(0xFF4CAF50) else Color(0xFFE53935))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (dev.isOnline) "Online (${dev.ipAddress})" else "Device Offline / Unreachable",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (dev.isOnline) Color(0xFF2E7D32) else Color(0xFFC62828),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Switch(
                    checked = dev.powerState,
                    onCheckedChange = { onToggleDevice(dev) },
                    modifier = Modifier.testTag("device_switch_${dev.id}")
                )
            }

            // Interactive Controls Section
            if (dev.powerState && dev.isOnline && onSetOperationalMode != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current State: ${dev.currentValue}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                // Custom controls per category
                when {
                    dev.deviceType.equals("AC", true) || dev.deviceName.contains("AC", true) -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(onClick = { onSetOperationalMode(dev, "Cooling 20°C") }, label = { Text("20°C") })
                            AssistChip(onClick = { onSetOperationalMode(dev, "Cooling 22°C") }, label = { Text("22°C") })
                            AssistChip(onClick = { onSetOperationalMode(dev, "Cooling 24°C") }, label = { Text("24°C") })
                            AssistChip(onClick = { onSetOperationalMode(dev, "Eco Mode") }, label = { Text("Eco") })
                        }
                    }

                    dev.deviceType.equals("Light", true) || dev.deviceName.contains("Light", true) -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(onClick = { onSetOperationalMode(dev, "Warm 2700K 50%") }, label = { Text("Warm 50%") })
                            AssistChip(onClick = { onSetOperationalMode(dev, "Daylight 100%") }, label = { Text("Bright 100%") })
                            AssistChip(onClick = { onSetOperationalMode(dev, "RGB Red") }, label = { Text("🔴 Red") })
                            AssistChip(onClick = { onSetOperationalMode(dev, "RGB Blue") }, label = { Text("🔵 Blue") })
                        }
                    }

                    dev.deviceType.equals("Lock", true) || dev.deviceName.contains("Lock", true) -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onSetOperationalMode(dev, "Locked") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                            ) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock")
                            }
                            OutlinedButton(
                                onClick = { onSetOperationalMode(dev, "Unlocked") }
                            ) {
                                Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unlock")
                            }
                        }
                    }

                    dev.deviceType.equals("Camera", true) || dev.deviceName.contains("Camera", true) || dev.deviceName.contains("Doorbell", true) -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onOpenCameraStream?.invoke(dev) }
                            ) {
                                Icon(imageVector = Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Stream Feed")
                            }
                        }
                    }

                    dev.deviceName.contains("Vacuum", true) -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = { onSetOperationalMode(dev, "Cleaning Room") }) {
                                Text("Start Cleaning")
                            }
                            OutlinedButton(onClick = { onSetOperationalMode(dev, "Docked & Charging") }) {
                                Text("Return to Dock")
                            }
                        }
                    }

                    else -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AssistChip(onClick = { onSetOperationalMode(dev, "Mode 1") }, label = { Text("Standard") })
                            AssistChip(onClick = { onSetOperationalMode(dev, "Turbo Mode") }, label = { Text("Turbo") })
                        }
                    }
                }
            } else if (!dev.isOnline) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Device is powered off or disconnected from network gateway. Cannot send remote commands.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun CameraStreamModalDialog(
    device: SmartDeviceEntity,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${device.deviceName} • Live")
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.Green, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("HD 1080p Stream Connected", color = Color.White, style = MaterialTheme.typography.bodySmall)
                        Text("${device.ipAddress} • 30 fps", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(onClick = {}, label = { Text("📸 Snapshot") })
                    AssistChip(onClick = {}, label = { Text("🎙️ Intercom") })
                    AssistChip(onClick = {}, label = { Text("🚨 Siren Alert") })
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close Feed")
            }
        }
    )
}
