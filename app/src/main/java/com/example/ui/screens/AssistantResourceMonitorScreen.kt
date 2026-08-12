package com.example.ui.screens

import androidx.compose.animation.core.*
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
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.devicecare.AssistantResourceMetrics
import com.example.devicecare.AssistantResourceMonitorManager
import com.example.devicecare.DailyResourceStats
import com.example.devicecare.PeriodResourceSummary
import com.example.ui.glass.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

enum class SystemMonitorPeriod {
    TODAY,
    YESTERDAY,
    LAST_7_DAYS,
    LAST_30_DAYS
}

@Composable
fun AssistantResourceMonitorScreen(
    userSettings: UserSettings,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val monitorManager = remember { AssistantResourceMonitorManager.getInstance(context) }

    var currentMetrics by remember { mutableStateOf(monitorManager.currentMetrics.value) }
    var selectedPeriod by remember { mutableStateOf(SystemMonitorPeriod.LAST_7_DAYS) }
    var statusText by remember { mutableStateOf("Real-Time System Monitoring Active") }

    val scrollState = rememberScrollState()

    // Real-time polling loop off main thread
    LaunchedEffect(userSettings.isRealTimeMonitoringEnabled) {
        while (isActive) {
            if (userSettings.isRealTimeMonitoringEnabled) {
                try {
                    val metrics = withContext(Dispatchers.IO) {
                        monitorManager.sampleCurrentMetrics()
                    }
                    currentMetrics = metrics
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            delay(2000)
        }
    }

    DynamicLiquidGlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            GlassTopBar(
                title = "Assistant System Monitor",
                subtitle = "Real Device Performance & Footprint Diagnostics",
                navigationIcon = {
                    GlassIconButton(
                        icon = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        onClick = onNavigateBack
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Real-time Resource Gauges Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-Time Device Gauges",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (userSettings.isRealTimeMonitoringEnabled) Color(0xFF10B981) else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (userSettings.isRealTimeMonitoringEnabled) "Live 2s" else "Paused",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CPU Bar
                ResourceBarItem(
                    label = "CPU Usage (Personal AI Process)",
                    valueText = "${formatFloat(currentMetrics.cpuPercentage)}%",
                    progress = (currentMetrics.cpuPercentage / 100f),
                    barColor = LocalGlassAccent.current.color
                )

                Spacer(modifier = Modifier.height(12.dp))

                // RAM Bar (Assistant RAM + Device RAM)
                val ramProgress = if (currentMetrics.totalDeviceRamMb > 0f) {
                    currentMetrics.ramUsedMb / currentMetrics.totalDeviceRamMb
                } else 0.05f

                val ramTotalStr = if (currentMetrics.totalDeviceRamMb > 0f) {
                    "${formatFloat(currentMetrics.ramUsedMb)} MB (Assistant) • ${formatFloat(currentMetrics.usedDeviceRamMb / 1024f)} / ${formatFloat(currentMetrics.totalDeviceRamMb / 1024f)} GB Device"
                } else {
                    "${formatFloat(currentMetrics.ramUsedMb)} MB"
                }

                ResourceBarItem(
                    label = "RAM Usage",
                    valueText = ramTotalStr,
                    progress = ramProgress,
                    barColor = Color(0xFF10B981)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Device Storage Bar
                val storageProgress = if (currentMetrics.deviceStorageTotalGb > 0f) {
                    currentMetrics.deviceStorageUsedGb / currentMetrics.deviceStorageTotalGb
                } else 0.05f

                val storageText = if (currentMetrics.deviceStorageTotalGb > 0f) {
                    "${formatFloat(currentMetrics.deviceStorageUsedGb)} GB / ${formatFloat(currentMetrics.deviceStorageTotalGb)} GB Device (${formatFloat(currentMetrics.deviceStorageFreeGb)} GB Free)"
                } else {
                    "Not available"
                }

                ResourceBarItem(
                    label = "Storage Usage (Overall Device)",
                    valueText = storageText,
                    progress = storageProgress,
                    barColor = Color(0xFFF59E0B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GPU Metric Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("GPU Usage", style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = if (currentMetrics.gpuPercentage != null) "${formatFloat(currentMetrics.gpuPercentage!!)}%" else currentMetrics.gpuStatusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Battery & Temperature / Thermal Status Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Battery & Thermal Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Battery Box
                    val batteryStr = if (currentMetrics.batteryPercentage != null) {
                        "${currentMetrics.batteryPercentage}% • ${currentMetrics.batteryStatus}"
                    } else "Not available"

                    MetricStatusTile(
                        title = "Battery Level",
                        valueText = batteryStr,
                        icon = Icons.Default.BatteryChargingFull,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Thermal / Temp Box
                    MetricStatusTile(
                        title = "Device Temperature / Thermal",
                        valueText = currentMetrics.thermalStatusMessage,
                        icon = Icons.Default.Thermostat,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Storage Footprint Breakdown (Assistant Specific)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Storage Used by Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Real storage footprint consumed by Personal AI Assistant",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(10.dp))

                StorageRowItem("App Package (APK)", "${formatFloat(currentMetrics.storageAppApkMb)} MB", Icons.Default.Android)
                StorageRowItem("Application Data", "${formatFloat(currentMetrics.storageAppDataMb)} MB", Icons.Default.Folder)
                StorageRowItem("Temporary Cache", "${formatFloat(currentMetrics.storageCacheMb)} MB", Icons.Default.CleaningServices)
                StorageRowItem("Local AI Models & DB", "${formatFloat(currentMetrics.storageAiModelAndDbMb)} MB", Icons.Default.Psychology)

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Assistant Storage Used", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${formatFloat(currentMetrics.storageTotalMb)} MB", fontWeight = FontWeight.Bold, color = LocalGlassAccent.current.color, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Usage Period Selector & Historical Summary
            val periodSummary = remember(selectedPeriod, currentMetrics) {
                try {
                    when (selectedPeriod) {
                        SystemMonitorPeriod.TODAY -> monitorManager.getSummary(1, false)
                        SystemMonitorPeriod.YESTERDAY -> monitorManager.getSummary(2, true)
                        SystemMonitorPeriod.LAST_7_DAYS -> monitorManager.getSummary(7, false)
                        SystemMonitorPeriod.LAST_30_DAYS -> monitorManager.getSummary(30, false)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    PeriodResourceSummary(
                        periodLabel = when (selectedPeriod) {
                            SystemMonitorPeriod.TODAY -> "Today"
                            SystemMonitorPeriod.YESTERDAY -> "Yesterday"
                            SystemMonitorPeriod.LAST_7_DAYS -> "Last 7 Days"
                            SystemMonitorPeriod.LAST_30_DAYS -> "Last 30 Days"
                        },
                        daysCount = if (selectedPeriod == SystemMonitorPeriod.TODAY || selectedPeriod == SystemMonitorPeriod.YESTERDAY) 1 else 7,
                        totalCpuActivityPct = null,
                        totalGpuActivityPct = null,
                        ramAverageMb = null,
                        ramPeakMb = null,
                        storageTotalMb = null,
                        storageGrowthMb = null,
                        batteryAvgPct = null,
                        highestUsageDayLabel = "N/A",
                        lowestUsageDayLabel = "N/A",
                        hasRecordedData = false,
                        dailyList = emptyList()
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Usage History & Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Period Selector Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PeriodTabButton("Today", selectedPeriod == SystemMonitorPeriod.TODAY) {
                        selectedPeriod = SystemMonitorPeriod.TODAY
                    }
                    PeriodTabButton("Yesterday", selectedPeriod == SystemMonitorPeriod.YESTERDAY) {
                        selectedPeriod = SystemMonitorPeriod.YESTERDAY
                    }
                    PeriodTabButton("7 Days", selectedPeriod == SystemMonitorPeriod.LAST_7_DAYS) {
                        selectedPeriod = SystemMonitorPeriod.LAST_7_DAYS
                    }
                    PeriodTabButton("30 Days", selectedPeriod == SystemMonitorPeriod.LAST_30_DAYS) {
                        selectedPeriod = SystemMonitorPeriod.LAST_30_DAYS
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (periodSummary.hasRecordedData) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryMetricBox(
                            title = "Avg CPU",
                            value = if (periodSummary.totalCpuActivityPct != null) "${formatFloat(periodSummary.totalCpuActivityPct)}%" else "Not available",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        SummaryMetricBox(
                            title = "Avg RAM",
                            value = if (periodSummary.ramAverageMb != null) "${formatFloat(periodSummary.ramAverageMb)} MB" else "Not available",
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        SummaryMetricBox(
                            title = "Assistant Storage",
                            value = if (periodSummary.storageTotalMb != null) "${formatFloat(periodSummary.storageTotalMb)} MB" else "Not available",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (periodSummary.daysCount > 1) {
                        Text("• Highest CPU Day: ${periodSummary.highestUsageDayLabel}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        Text("• Lowest CPU Day: ${periodSummary.lowestUsageDayLabel}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        if (periodSummary.storageGrowthMb != null) {
                            val growthStr = if (periodSummary.storageGrowthMb >= 0f) "+${formatFloat(periodSummary.storageGrowthMb)}" else formatFloat(periodSummary.storageGrowthMb)
                            Text("• Storage Growth (${periodSummary.periodLabel}): $growthStr MB", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Not available - No recorded metrics history for ${periodSummary.periodLabel}. Keep the app running to record usage over time.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Daily Usage Animated Graph/Timeline Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Daily Breakdown (${periodSummary.periodLabel})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (periodSummary.dailyList.isNotEmpty()) {
                    periodSummary.dailyList.forEach { daily ->
                        DailyTimelineItem(daily)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                } else {
                    Text(
                        text = "No history recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Resource Monitor Controls & Alert Settings Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Monitoring Controls",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Real-Time Monitoring
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Real-Time Background Monitoring", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text("Low-overhead background sampling", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Switch(
                        checked = userSettings.isRealTimeMonitoringEnabled,
                        onCheckedChange = { checked ->
                            scope.launch { prefsRepo.setRealTimeMonitoringEnabled(checked) }
                        }
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 6.dp))

                // Clear Resource History Button
                GlassButton(
                    text = "Clear Resource History 🧹",
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val success = monitorManager.clearResourceHistory()
                            withContext(Dispatchers.Main) {
                                statusText = if (success) "Resource history cleared. Live diagnostics active." else "Clear failed."
                                currentMetrics = monitorManager.currentMetrics.value
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalGlassAccent.current.color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RowScope.PeriodTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) LocalGlassAccent.current.color else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun MetricStatusTile(
    title: String,
    valueText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(valueText, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
fun ResourceBarItem(
    label: String,
    valueText: String,
    progress: Float,
    barColor: Color
) {
    val safeProgress = if (progress.isNaN() || progress.isInfinite() || progress < 0f) 0.01f else progress.coerceIn(0.01f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(valueText, color = barColor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            val widthFraction = if (animatedProgress.isNaN() || animatedProgress.isInfinite() || animatedProgress <= 0f) 0.01f else animatedProgress.coerceIn(0.01f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(widthFraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun StorageRowItem(
    title: String,
    sizeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
        Text(sizeText, color = Color.Gray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SummaryMetricBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
fun DailyTimelineItem(daily: DailyResourceStats) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(daily.dateLabel, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
            if (daily.isRecorded) {
                Text("Peak RAM: ${formatFloat(daily.peakRamMb)} MB", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            } else {
                Text("Not recorded", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }

        if (daily.isRecorded) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("CPU: ${formatFloat(daily.cpuAvgPct)}%", color = LocalGlassAccent.current.color, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("RAM: ${formatFloat(daily.ramAvgMb)}MB", color = Color(0xFF10B981), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("Storage: ${formatFloat(daily.storageTotalMb)}MB", color = Color(0xFFF59E0B), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Text("No recorded data", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun formatFloat(value: Float?): String {
    if (value == null || value.isNaN() || value.isInfinite() || value < 0f) return "0.0"
    return String.format(Locale.US, "%.1f", value)
}
