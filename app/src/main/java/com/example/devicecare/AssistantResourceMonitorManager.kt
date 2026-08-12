package com.example.devicecare

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.Process
import android.os.StatFs
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class AssistantResourceMetrics(
    val cpuPercentage: Float,
    val cpuStatusMessage: String,
    val gpuPercentage: Float?,
    val gpuStatusMessage: String,
    val ramUsedMb: Float,
    val totalDeviceRamMb: Float,
    val usedDeviceRamMb: Float,
    val ramPeakMb: Float,
    val ramAverageMb: Float,
    val storageAppApkMb: Float,
    val storageAppDataMb: Float,
    val storageCacheMb: Float,
    val storageAiModelAndDbMb: Float,
    val storageTotalMb: Float,
    val deviceStorageTotalGb: Float,
    val deviceStorageFreeGb: Float,
    val deviceStorageUsedGb: Float,
    val batteryPercentage: Int?,
    val batteryStatus: String,
    val batteryTemperatureCelsius: Float?,
    val thermalStatusMessage: String,
    val timestampMs: Long = System.currentTimeMillis()
)

data class DailyResourceStats(
    val dayKey: String, // e.g. "2026-08-08"
    val dateLabel: String, // e.g. "Today", "Yesterday", "MMM dd"
    val cpuAvgPct: Float,
    val gpuAvgPct: Float?,
    val ramAvgMb: Float,
    val storageTotalMb: Float,
    val peakRamMb: Float,
    val peakCpuPct: Float,
    val batteryAvgPct: Int?,
    val isRecorded: Boolean // True if actual sample history was recorded for this day
)

data class PeriodResourceSummary(
    val periodLabel: String, // "Today", "Yesterday", "Last 7 Days", "Last 30 Days"
    val daysCount: Int,
    val totalCpuActivityPct: Float?,
    val totalGpuActivityPct: Float?,
    val ramAverageMb: Float?,
    val ramPeakMb: Float?,
    val storageTotalMb: Float?,
    val storageGrowthMb: Float?,
    val batteryAvgPct: Int?,
    val highestUsageDayLabel: String,
    val lowestUsageDayLabel: String,
    val hasRecordedData: Boolean,
    val dailyList: List<DailyResourceStats>
)

class AssistantResourceMonitorManager private constructor(private val context: Context) {

    private val historyFile = File(context.filesDir, "assistant_resource_history.json")

    private var lastCpuTimeMs: Long = 0L
    private var lastRealtimeMs: Long = 0L
    private var peakRamSeenMb: Float = 0f

    private val _currentMetrics = MutableStateFlow(createEmptyMetrics())
    val currentMetrics: StateFlow<AssistantResourceMetrics> = _currentMetrics.asStateFlow()

    init {
        try {
            lastCpuTimeMs = Process.getElapsedCpuTime()
            lastRealtimeMs = SystemClock.elapsedRealtime()
            sampleCurrentMetrics()
            recordCurrentSampleToHistory()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createEmptyMetrics(): AssistantResourceMetrics {
        return AssistantResourceMetrics(
            cpuPercentage = 0f,
            cpuStatusMessage = "Initializing...",
            gpuPercentage = null,
            gpuStatusMessage = "Not available (Restricted by OS)",
            ramUsedMb = 0f,
            totalDeviceRamMb = 0f,
            usedDeviceRamMb = 0f,
            ramPeakMb = 0f,
            ramAverageMb = 0f,
            storageAppApkMb = 0f,
            storageAppDataMb = 0f,
            storageCacheMb = 0f,
            storageAiModelAndDbMb = 0f,
            storageTotalMb = 0f,
            deviceStorageTotalGb = 0f,
            deviceStorageFreeGb = 0f,
            deviceStorageUsedGb = 0f,
            batteryPercentage = null,
            batteryStatus = "Not available",
            batteryTemperatureCelsius = null,
            thermalStatusMessage = "Not available"
        )
    }

    fun sampleCurrentMetrics(): AssistantResourceMetrics {
        // 1. RAM Usage (Real system & Assistant process)
        var totalDeviceRamMb = 0f
        var usedDeviceRamMb = 0f
        var ramUsedMb = 0f

        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (activityManager != null) {
                val memInfo = ActivityManager.MemoryInfo()
                activityManager.getMemoryInfo(memInfo)
                totalDeviceRamMb = sanitizeFloat(memInfo.totalMem / (1024f * 1024f))
                val availRamMb = sanitizeFloat(memInfo.availMem / (1024f * 1024f))
                usedDeviceRamMb = sanitizeFloat(totalDeviceRamMb - availRamMb)

                val pssArray = activityManager.getProcessMemoryInfo(intArrayOf(Process.myPid()))
                val pssKb = pssArray?.firstOrNull()?.totalPss ?: 0
                ramUsedMb = sanitizeFloat(pssKb / 1024f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (ramUsedMb <= 0f) {
            try {
                val runtime = Runtime.getRuntime()
                ramUsedMb = sanitizeFloat((runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f))
            } catch (_: Exception) {}
        }

        if (ramUsedMb > peakRamSeenMb) {
            peakRamSeenMb = ramUsedMb
        }

        // 2. CPU Usage (Real calculated delta)
        var cpuPercentage = 0f
        try {
            val currentCpuTimeMs = Process.getElapsedCpuTime()
            val currentRealtimeMs = SystemClock.elapsedRealtime()

            val cpuDelta = currentCpuTimeMs - lastCpuTimeMs
            val timeDelta = currentRealtimeMs - lastRealtimeMs

            if (lastRealtimeMs > 0L && timeDelta > 0L && cpuDelta >= 0L) {
                val numCores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
                val rawCpuPct = ((cpuDelta.toFloat() / timeDelta.toFloat()) * 100f) / numCores
                cpuPercentage = sanitizeFloat(rawCpuPct).coerceIn(0.0f, 100.0f)
            }

            lastCpuTimeMs = currentCpuTimeMs
            lastRealtimeMs = currentRealtimeMs
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cpuStatusMessage = if (cpuPercentage > 0f) "${formatFloat(cpuPercentage)}% Process CPU" else "Idle (0.0%)"

        // 3. GPU Usage - Check OS capability
        val gpuPercentage: Float? = null
        val gpuStatusMessage = "Not available (Restricted by OS)"

        // 4. Battery Usage & Temperature / Thermal Status
        var batteryPct: Int? = null
        var batteryStatusStr = "Not available"
        var batteryTempC: Float? = null
        var thermalStatusStr = "Not available"

        try {
            val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (batteryIntent != null) {
                val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    batteryPct = ((level.toFloat() / scale.toFloat()) * 100f).toInt().coerceIn(0, 100)
                }

                val statusExtra = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                batteryStatusStr = when (statusExtra) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging ⚡"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging 📱"
                    BatteryManager.BATTERY_STATUS_FULL -> "Full 🔋"
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                    else -> if (batteryPct != null) "Active" else "Not available"
                }

                val tempTenths = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                if (tempTenths > 0) {
                    batteryTempC = sanitizeFloat(tempTenths / 10.0f)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            var thermalLevel: String? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                thermalLevel = when (powerManager?.currentThermalStatus) {
                    PowerManager.THERMAL_STATUS_NONE -> "Normal"
                    PowerManager.THERMAL_STATUS_LIGHT -> "Light Throttling"
                    PowerManager.THERMAL_STATUS_MODERATE -> "Moderate Throttling"
                    PowerManager.THERMAL_STATUS_SEVERE -> "Severe Throttling"
                    PowerManager.THERMAL_STATUS_CRITICAL -> "Critical Throttling"
                    PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency Throttling"
                    PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown Imminent"
                    else -> null
                }
            }

            thermalStatusStr = when {
                batteryTempC != null && thermalLevel != null -> "${formatFloat(batteryTempC)} °C ($thermalLevel)"
                batteryTempC != null -> "${formatFloat(batteryTempC)} °C (Normal)"
                thermalLevel != null -> thermalLevel
                else -> "Not available"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 5. Storage Usage (Device total + Assistant storage breakdown)
        var totalDeviceStorageGb = 0f
        var freeDeviceStorageGb = 0f
        var usedDeviceStorageGb = 0f

        try {
            val dataDir = Environment.getDataDirectory()
            if (dataDir != null && dataDir.exists()) {
                val statFs = StatFs(dataDir.path)
                val blockSize = statFs.blockSizeLong
                val totalBlocks = statFs.blockCountLong
                val availableBlocks = statFs.availableBlocksLong

                totalDeviceStorageGb = sanitizeFloat((totalBlocks * blockSize) / (1024f * 1024f * 1024f))
                freeDeviceStorageGb = sanitizeFloat((availableBlocks * blockSize) / (1024f * 1024f * 1024f))
                usedDeviceStorageGb = sanitizeFloat(totalDeviceStorageGb - freeDeviceStorageGb)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        var storageAppApkMb = 0f
        try {
            val appInfo = context.applicationInfo
            val apkFile = File(appInfo.sourceDir)
            if (apkFile.exists()) {
                storageAppApkMb = sanitizeFloat(apkFile.length() / (1024f * 1024f))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val dataDirMb = calculateFolderSizeMb(context.dataDir)
        val cacheDirMb = calculateFolderSizeMb(context.cacheDir) + calculateFolderSizeMb(context.externalCacheDir)

        var storageAiModelAndDbMb = 0f
        try {
            val dbFiles = listOfNotNull(
                context.getDatabasePath("assistant_db"),
                context.getDatabasePath("room_database"),
                File(context.filesDir, "assistant_resource_history.json")
            )
            dbFiles.forEach { f ->
                if (f.exists()) storageAiModelAndDbMb += sanitizeFloat(f.length() / (1024f * 1024f))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val storageAppDataMb = sanitizeFloat(dataDirMb - storageAiModelAndDbMb)
        val storageCacheMb = sanitizeFloat(cacheDirMb)
        val storageTotalMb = sanitizeFloat(storageAppApkMb + storageAppDataMb + storageCacheMb + storageAiModelAndDbMb)

        val metrics = AssistantResourceMetrics(
            cpuPercentage = cpuPercentage,
            cpuStatusMessage = cpuStatusMessage,
            gpuPercentage = gpuPercentage,
            gpuStatusMessage = gpuStatusMessage,
            ramUsedMb = ramUsedMb,
            totalDeviceRamMb = totalDeviceRamMb,
            usedDeviceRamMb = usedDeviceRamMb,
            ramPeakMb = peakRamSeenMb,
            ramAverageMb = ramUsedMb,
            storageAppApkMb = storageAppApkMb,
            storageAppDataMb = storageAppDataMb,
            storageCacheMb = storageCacheMb,
            storageAiModelAndDbMb = storageAiModelAndDbMb,
            storageTotalMb = storageTotalMb,
            deviceStorageTotalGb = totalDeviceStorageGb,
            deviceStorageFreeGb = freeDeviceStorageGb,
            deviceStorageUsedGb = usedDeviceStorageGb,
            batteryPercentage = batteryPct,
            batteryStatus = batteryStatusStr,
            batteryTemperatureCelsius = batteryTempC,
            thermalStatusMessage = thermalStatusStr
        )

        _currentMetrics.value = metrics
        return metrics
    }

    private fun calculateFolderSizeMb(dir: File?): Float {
        if (dir == null || !dir.exists()) return 0f
        var totalBytes = 0L
        try {
            dir.walkTopDown()
                .maxDepth(6)
                .forEach { file ->
                    try {
                        if (file.isFile) {
                            totalBytes += file.length()
                        }
                    } catch (_: Throwable) {}
                }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return sanitizeFloat(totalBytes / (1024f * 1024f))
    }

    fun recordCurrentSampleToHistory() {
        try {
            val metrics = sampleCurrentMetrics()
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val historyArr = loadHistoryArray()

            var foundToday = false
            for (i in 0 until historyArr.length()) {
                val obj = historyArr.optJSONObject(i) ?: continue
                if (obj.optString("dayKey", "") == todayKey) {
                    val samplesCount = obj.optInt("samplesCount", 1) + 1
                    val prevCpuAvg = sanitizeFloat(obj.optDouble("cpuAvgPct", 0.0).toFloat())
                    val prevRamAvg = sanitizeFloat(obj.optDouble("ramAvgMb", 0.0).toFloat())

                    val newCpuAvg = sanitizeFloat((prevCpuAvg * (samplesCount - 1) + metrics.cpuPercentage) / samplesCount)
                    val newRamAvg = sanitizeFloat((prevRamAvg * (samplesCount - 1) + metrics.ramUsedMb) / samplesCount)
                    val peakRam = sanitizeFloat(Math.max(obj.optDouble("peakRamMb", 0.0).toFloat(), metrics.ramUsedMb))
                    val peakCpu = sanitizeFloat(Math.max(obj.optDouble("peakCpuPct", 0.0).toFloat(), metrics.cpuPercentage))

                    obj.put("samplesCount", samplesCount)
                    obj.put("cpuAvgPct", newCpuAvg.toDouble())
                    obj.put("ramAvgMb", newRamAvg.toDouble())
                    obj.put("storageTotalMb", metrics.storageTotalMb.toDouble())
                    obj.put("peakRamMb", peakRam.toDouble())
                    obj.put("peakCpuPct", peakCpu.toDouble())
                    metrics.batteryPercentage?.let { obj.put("batteryAvgPct", it) }
                    foundToday = true
                    break
                }
            }

            if (!foundToday) {
                val newObj = JSONObject().apply {
                    put("dayKey", todayKey)
                    put("samplesCount", 1)
                    put("cpuAvgPct", metrics.cpuPercentage.toDouble())
                    put("ramAvgMb", metrics.ramUsedMb.toDouble())
                    put("storageTotalMb", metrics.storageTotalMb.toDouble())
                    put("peakRamMb", metrics.ramUsedMb.toDouble())
                    put("peakCpuPct", metrics.cpuPercentage.toDouble())
                    metrics.batteryPercentage?.let { put("batteryAvgPct", it) }
                }
                historyArr.put(newObj)
            }

            val tempFile = File(context.filesDir, "assistant_resource_history.json.tmp")
            tempFile.writeText(historyArr.toString())
            if (tempFile.exists()) {
                tempFile.renameTo(historyFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadHistoryArray(): JSONArray {
        return try {
            if (historyFile.exists()) {
                val text = historyFile.readText().trim()
                if (text.isNotEmpty()) JSONArray(text) else JSONArray()
            } else {
                JSONArray()
            }
        } catch (e: Exception) {
            try { historyFile.delete() } catch (_: Exception) {}
            JSONArray()
        }
    }

    fun getDailyHistory(days: Int): List<DailyResourceStats> {
        val safeDays = days.coerceIn(1, 365)
        val historyMap = mutableMapOf<String, JSONObject>()
        try {
            val rawArr = loadHistoryArray()
            for (i in 0 until rawArr.length()) {
                val obj = rawArr.optJSONObject(i) ?: continue
                val key = obj.optString("dayKey", "")
                if (key.isNotEmpty()) {
                    historyMap[key] = obj
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val result = mutableListOf<DailyResourceStats>()
        val cal = Calendar.getInstance()

        for (i in 0 until safeDays) {
            try {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                val label = when (i) {
                    0 -> "Today"
                    1 -> "Yesterday"
                    2 -> "2 Days Ago"
                    else -> SimpleDateFormat("MMM dd", Locale.US).format(cal.time)
                }

                val savedObj = historyMap[dateStr]
                if (savedObj != null) {
                    val batOpt = if (savedObj.has("batteryAvgPct") && !savedObj.isNull("batteryAvgPct")) {
                        savedObj.optInt("batteryAvgPct")
                    } else null

                    val rawCpu = sanitizeFloat(savedObj.optDouble("cpuAvgPct", 0.0).toFloat())
                    val rawRam = sanitizeFloat(savedObj.optDouble("ramAvgMb", 0.0).toFloat())
                    val rawStorage = sanitizeFloat(savedObj.optDouble("storageTotalMb", 0.0).toFloat())
                    val rawPeakRam = sanitizeFloat(savedObj.optDouble("peakRamMb", 0.0).toFloat())
                    val rawPeakCpu = sanitizeFloat(savedObj.optDouble("peakCpuPct", 0.0).toFloat())

                    result.add(
                        DailyResourceStats(
                            dayKey = dateStr,
                            dateLabel = label,
                            cpuAvgPct = rawCpu.coerceIn(0f, 100f),
                            gpuAvgPct = null,
                            ramAvgMb = rawRam,
                            storageTotalMb = rawStorage,
                            peakRamMb = rawPeakRam,
                            peakCpuPct = rawPeakCpu.coerceIn(0f, 100f),
                            batteryAvgPct = batOpt,
                            isRecorded = true
                        )
                    )
                } else {
                    result.add(
                        DailyResourceStats(
                            dayKey = dateStr,
                            dateLabel = label,
                            cpuAvgPct = 0f,
                            gpuAvgPct = null,
                            ramAvgMb = 0f,
                            storageTotalMb = 0f,
                            peakRamMb = 0f,
                            peakCpuPct = 0f,
                            batteryAvgPct = null,
                            isRecorded = false
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return result
    }

    fun getSummary(days: Int, isYesterdayOnly: Boolean = false): PeriodResourceSummary {
        try {
            val dailyList = getDailyHistory(if (isYesterdayOnly) 2 else days)

            val targetList = when {
                isYesterdayOnly -> dailyList.filter { it.dateLabel == "Yesterday" }
                days == 1 -> dailyList.filter { it.dateLabel == "Today" }
                else -> dailyList
            }

            val recordedItems = targetList.filter { it.isRecorded }
            val periodLabel = when {
                isYesterdayOnly -> "Yesterday"
                days == 1 -> "Today"
                days == 7 -> "Last 7 Days"
                else -> "Last 30 Days"
            }

            if (recordedItems.isEmpty()) {
                return PeriodResourceSummary(
                    periodLabel = periodLabel,
                    daysCount = if (days == 1 || isYesterdayOnly) 1 else days,
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
                    dailyList = targetList
                )
            }

            val cpuList = recordedItems.map { it.cpuAvgPct }.filter { !it.isNaN() && !it.isInfinite() }
            val avgCpu = if (cpuList.isNotEmpty()) cpuList.average().toFloat() else 0f

            val ramList = recordedItems.map { it.ramAvgMb }.filter { !it.isNaN() && !it.isInfinite() }
            val avgRam = if (ramList.isNotEmpty()) ramList.average().toFloat() else 0f

            val peakRamList = recordedItems.map { it.peakRamMb }.filter { !it.isNaN() && !it.isInfinite() }
            val peakRam = if (peakRamList.isNotEmpty()) peakRamList.maxOrNull() ?: avgRam else avgRam

            val currentStorage = recordedItems.firstOrNull()?.storageTotalMb ?: 0f
            val oldestStorage = recordedItems.lastOrNull()?.storageTotalMb ?: currentStorage
            val storageGrowth = currentStorage - oldestStorage

            val validBatteryList = recordedItems.mapNotNull { it.batteryAvgPct }
            val avgBattery = if (validBatteryList.isNotEmpty()) validBatteryList.average().toInt() else null

            val maxCpuDay = recordedItems.filter { !it.peakCpuPct.isNaN() }.maxByOrNull { it.peakCpuPct }?.dateLabel ?: "N/A"
            val minCpuDay = recordedItems.filter { !it.cpuAvgPct.isNaN() }.minByOrNull { it.cpuAvgPct }?.dateLabel ?: "N/A"

            return PeriodResourceSummary(
                periodLabel = periodLabel,
                daysCount = if (days == 1 || isYesterdayOnly) 1 else days,
                totalCpuActivityPct = sanitizeFloat(avgCpu),
                totalGpuActivityPct = null,
                ramAverageMb = sanitizeFloat(avgRam),
                ramPeakMb = sanitizeFloat(peakRam),
                storageTotalMb = sanitizeFloat(currentStorage),
                storageGrowthMb = sanitizeFloat(storageGrowth),
                batteryAvgPct = avgBattery,
                highestUsageDayLabel = maxCpuDay,
                lowestUsageDayLabel = minCpuDay,
                hasRecordedData = true,
                dailyList = targetList
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return PeriodResourceSummary(
                periodLabel = if (isYesterdayOnly) "Yesterday" else if (days == 1) "Today" else if (days == 7) "Last 7 Days" else "Last 30 Days",
                daysCount = if (days == 1 || isYesterdayOnly) 1 else days,
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

    fun clearResourceHistory(): Boolean {
        return try {
            if (historyFile.exists()) {
                historyFile.delete()
            }
            peakRamSeenMb = 0f
            sampleCurrentMetrics()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun sanitizeFloat(value: Float): Float {
        if (value.isNaN() || value.isInfinite() || value < 0f) return 0f
        return value
    }

    private fun formatFloat(value: Float): String {
        val safe = sanitizeFloat(value)
        return String.format(Locale.US, "%.1f", safe)
    }

    companion object {
        @Volatile
        private var INSTANCE: AssistantResourceMonitorManager? = null

        fun getInstance(context: Context): AssistantResourceMonitorManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AssistantResourceMonitorManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
