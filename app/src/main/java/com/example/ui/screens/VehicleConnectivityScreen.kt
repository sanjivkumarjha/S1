package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserSettings
import com.example.domain.vehicle.ChargingStatus
import com.example.domain.vehicle.ConnectionStatus
import com.example.domain.vehicle.FuelType
import com.example.domain.vehicle.SmartVehicleEntity
import com.example.domain.vehicle.VehicleConnectivityManager
import com.example.domain.vehicle.VehicleType
import com.example.ui.glass.LiquidGlassButton
import com.example.ui.glass.LiquidGlassCard
import kotlinx.coroutines.launch

@Composable
fun VehicleConnectivityScreen(
    userSettings: UserSettings = UserSettings(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vehicleManager = remember { VehicleConnectivityManager.getInstance(context) }

    val vehicles by vehicleManager.vehiclesFlow.collectAsState()
    val isScanning by vehicleManager.isScanning.collectAsState()
    val eventMessage by vehicleManager.lastEventMessage.collectAsState()

    var selectedVehicleForSpecs by remember { mutableStateOf<SmartVehicleEntity?>(null) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }

    val primaryVehicle = vehicles.find { it.isPrimary } ?: vehicles.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vehicle Connectivity",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Discover, telemetry HUD, remote controls & trusted pairing",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Toolbar: Scan & Add
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiquidGlassButton(
                text = if (isScanning) "Scanning..." else "Scan Nearby",
                icon = Icons.Default.Radar,
                accentColor = MaterialTheme.colorScheme.primary,
                enabled = !isScanning,
                onClick = { vehicleManager.scanForVehicles() },
                modifier = Modifier.weight(1f)
            )

            LiquidGlassButton(
                text = "Add Vehicle",
                icon = Icons.Default.Add,
                accentColor = Color(0xFF10B981),
                onClick = { showAddVehicleDialog = true },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status / Event Message Banner
        AnimatedVisibility(visible = eventMessage.isNotEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0x208B5CF6)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = eventMessage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // PRIMARY VEHICLE DASHBOARD HUD
        primaryVehicle?.let { vehicle ->
            Text(
                text = "PRIMARY VEHICLE HUD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x258B5CF6),
                borderColor = Color(0x508B5CF6)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Vehicle Name & Connection Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = vehicle.vehicleType.emoji, fontSize = 26.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = vehicle.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${vehicle.manufacturer} ${vehicle.model} • ${vehicle.fuelType.displayName}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Connection Status Pill
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(vehicle.connectionStatus.colorHex).copy(alpha = 0.2f),
                            modifier = Modifier.border(
                                1.dp,
                                Color(vehicle.connectionStatus.colorHex).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            )
                        ) {
                            Text(
                                text = vehicle.connectionStatus.badge,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(vehicle.connectionStatus.colorHex),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Battery / Fuel & Range Gauge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (vehicle.fuelType == FuelType.ELECTRIC) "Battery Status" else "Fuel & Battery",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${vehicle.batteryPercent}%",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (vehicle.chargingStatus == ChargingStatus.CHARGING) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("⚡ Charging", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Estimated Range",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${vehicle.estimatedRangeKm} km",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { vehicle.batteryPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0x30FFFFFF)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Telemetry Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Odometer: ${vehicle.odometerKm} km", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Text("Source: ${vehicle.dataSource}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Supported Remote Command Buttons
                    Text(
                        text = "SUPPORTED REMOTE CONTROLS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (vehicle.supportedCapabilities.contains("LOCK")) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (vehicle.isLocked) Color(0x3010B981) else Color(0x30EF4444),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        val cmd = if (vehicle.isLocked) "UNLOCK" else "LOCK"
                                        vehicleManager.executeVehicleCommand(vehicle.id, cmd)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (vehicle.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (vehicle.isLocked) "Locked" else "Unlocked",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (vehicle.supportedCapabilities.contains("CHARGING")) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (vehicle.chargingStatus == ChargingStatus.CHARGING) Color(0x303B82F6) else Color(0x20FFFFFF),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        val cmd = if (vehicle.chargingStatus == ChargingStatus.CHARGING) "STOP_CHARGING" else "START_CHARGING"
                                        vehicleManager.executeVehicleCommand(vehicle.id, cmd)
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryChargingFull,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (vehicle.chargingStatus == ChargingStatus.CHARGING) "Stop Chg" else "Start Chg",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (vehicle.supportedCapabilities.contains("HORN")) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0x20FFFFFF), shape = RoundedCornerShape(12.dp))
                                    .clickable { vehicleManager.executeVehicleCommand(vehicle.id, "SOUND_HORN") }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Horn", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Trusted Vehicle Switch & Spec Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Trusted Vehicle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }

                        Switch(
                            checked = vehicle.isTrusted,
                            onCheckedChange = { vehicleManager.toggleTrustedVehicle(vehicle.id) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF10B981))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (vehicles.isEmpty()) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color(0x15FFFFFF),
                borderColor = Color(0x30FFFFFF)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No compatible vehicle connected",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Scan nearby Bluetooth BLE / Wi-Fi Direct devices or tap 'Add Vehicle' above to pair your Car, EV, or Electric Scooter.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // ALL VEHICLES LIST
            Text(
                text = "MY VEHICLES (${vehicles.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            vehicles.forEach { item ->
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                backgroundColor = if (item.isPrimary) Color(0x258B5CF6) else Color(0x15FFFFFF),
                borderColor = if (item.isPrimary) Color(0x508B5CF6) else Color(0x30FFFFFF)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = item.vehicleType.emoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (item.isPrimary) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("⭐ Primary", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                Text(
                                    text = "${item.manufacturer} ${item.model} • ${item.connectionType}",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Text(
                            text = item.connectionStatus.badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(item.connectionStatus.colorHex)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Battery/Fuel: ${item.batteryPercent}%  •  Range: ${item.estimatedRangeKm} km",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (item.connectionStatus == ConnectionStatus.CONNECTED) {
                                TextButton(onClick = { vehicleManager.disconnectVehicle(item.id) }) {
                                    Text("Disconnect", fontSize = 11.sp, color = Color(0xFFEF4444))
                                }
                            } else {
                                TextButton(onClick = { vehicleManager.connectVehicle(item.id) }) {
                                    Text("Connect", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }

                            TextButton(onClick = { selectedVehicleForSpecs = item }) {
                                Text("Specs", fontSize = 11.sp, color = Color(0xFF06B6D4))
                            }

                            if (!item.isPrimary) {
                                TextButton(onClick = { vehicleManager.setPrimaryVehicle(item.id) }) {
                                    Text("Make Primary", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // VEHICLE SPECS / INTERNET ENRICHMENT DIALOG
    selectedVehicleForSpecs?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { selectedVehicleForSpecs = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(vehicle.vehicleType.emoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${vehicle.manufacturer} ${vehicle.model} Specs")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Live Telemetry Source: ${vehicle.dataSource}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = vehicleManager.getGeneralVehicleSpecs(vehicle.manufacturer, vehicle.model),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedVehicleForSpecs = null }) {
                    Text("Close")
                }
            }
        )
    }

    // ADD VEHICLE DIALOG
    if (showAddVehicleDialog) {
        var newName by remember { mutableStateOf("") }
        var newMake by remember { mutableStateOf("") }
        var newModel by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddVehicleDialog = false },
            title = { Text("Pair / Add Custom Vehicle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Vehicle Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newMake,
                        onValueChange = { newMake = it },
                        label = { Text("Manufacturer (e.g. Tesla, Ather, BMW)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newModel,
                        onValueChange = { newModel = it },
                        label = { Text("Model Name (e.g. Model 3, 450X)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            val newEntity = SmartVehicleEntity(
                                id = "veh_${System.currentTimeMillis()}",
                                name = newName,
                                manufacturer = if (newMake.isBlank()) "Smart" else newMake,
                                model = if (newModel.isBlank()) "Custom" else newModel,
                                vehicleType = VehicleType.CAR,
                                fuelType = FuelType.ELECTRIC,
                                connectionStatus = ConnectionStatus.CONNECTED,
                                isTrusted = true
                            )
                            vehicleManager.addCustomVehicle(newEntity)
                            showAddVehicleDialog = false
                        }
                    }
                ) {
                    Text("Add Vehicle", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVehicleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
}
