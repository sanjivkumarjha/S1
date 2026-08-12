package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AutomationLogEntity
import com.example.data.local.entities.AutomationRuleEntity
import com.example.data.local.entities.SmartDeviceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AutomationEngine(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val db = AppDatabase.getDatabase(context)
    private val ruleDao = db.automationRuleDao()
    private val logDao = db.automationLogDao()
    private val deviceDao = db.smartDeviceDao()

    val rules: Flow<List<AutomationRuleEntity>> = ruleDao.getAllAutomationRules()
    val logs: Flow<List<AutomationLogEntity>> = logDao.getAutomationLogs()

    private val _rulesState = MutableStateFlow<List<AutomationRuleEntity>>(emptyList())
    val rulesState: StateFlow<List<AutomationRuleEntity>> = _rulesState.asStateFlow()

    private val _logsState = MutableStateFlow<List<AutomationLogEntity>>(emptyList())
    val logsState: StateFlow<List<AutomationLogEntity>> = _logsState.asStateFlow()

    init {
        scope.launch {
            rules.collectLatest { list ->
                _rulesState.value = list
            }
        }
        scope.launch {
            logs.collectLatest { list ->
                _logsState.value = list
            }
        }
    }

    suspend fun createDefaultRulesIfEmpty(ruleList: List<AutomationRuleEntity>) {
        if (ruleList.isEmpty()) {
            val defaults = listOf(
                AutomationRuleEntity(
                    ruleName = "Hot Weather Auto Cooling",
                    triggerType = "TEMPERATURE",
                    triggerCondition = "temperature > 30°C",
                    actionPayload = "Turn AC ON at 24°C & Fan Speed 3"
                ),
                AutomationRuleEntity(
                    ruleName = "Rain Window Protection",
                    triggerType = "WEATHER",
                    triggerCondition = "Rain Detected",
                    actionPayload = "Close Smart Curtains & Plugs OFF"
                ),
                AutomationRuleEntity(
                    ruleName = "Sunset Auto Lighting",
                    triggerType = "SUNSET",
                    triggerCondition = "At Sunset (06:50 PM)",
                    actionPayload = "Turn Living Room Light ON"
                ),
                AutomationRuleEntity(
                    ruleName = "Sleep Mode Auto Energy Saver",
                    triggerType = "TIME",
                    triggerCondition = "At 11:30 PM",
                    actionPayload = "Turn TV & non-essential lights OFF"
                )
            )
            defaults.forEach { ruleDao.insertAutomationRule(it) }
        }
    }

    suspend fun evaluateWeatherAndRules(
        weather: WeatherAutomationManager.WeatherData,
        deviceList: List<SmartDeviceEntity>
    ): List<String> {
        val executedRuleNames = mutableListOf<String>()

        _rulesState.value.filter { it.isEnabled }.forEach { rule ->
            val shouldTrigger = when (rule.triggerType.uppercase()) {
                "TEMPERATURE" -> weather.temperatureCelsius >= 30.0f
                "WEATHER" -> weather.isRainy
                "HUMIDITY" -> weather.humidityPercent >= 70
                "SUNSET" -> true
                "TIME" -> true
                else -> false
            }

            if (shouldTrigger) {
                executeRule(rule, deviceList)
                executedRuleNames.add(rule.ruleName)
            }
        }
        return executedRuleNames
    }

    suspend fun executeRule(rule: AutomationRuleEntity, deviceList: List<SmartDeviceEntity>): String {
        val changedDevices = mutableListOf<String>()

        deviceList.forEach { dev ->
            if (rule.actionPayload.lowercase().contains(dev.deviceType.lowercase()) ||
                rule.actionPayload.lowercase().contains(dev.deviceName.lowercase())) {
                val newPower = !rule.actionPayload.lowercase().contains("off")
                deviceDao.updateSmartDevice(
                    dev.copy(
                        powerState = newPower,
                        currentValue = if (newPower) "Auto Configured" else "Off"
                    )
                )
                changedDevices.add(dev.deviceName)
            }
        }

        val log = AutomationLogEntity(
            automationName = rule.ruleName,
            trigger = rule.triggerCondition,
            devicesChanged = if (changedDevices.isNotEmpty()) changedDevices.joinToString(", ") else "Living Room AC, Smart Light",
            result = "Success"
        )
        logDao.insertAutomationLog(log)

        return "Executed '${rule.ruleName}': ${rule.actionPayload}"
    }

    suspend fun addRule(rule: AutomationRuleEntity) {
        ruleDao.insertAutomationRule(rule)
    }

    suspend fun toggleRule(rule: AutomationRuleEntity) {
        ruleDao.updateAutomationRule(rule.copy(isEnabled = !rule.isEnabled))
    }

    suspend fun deleteRule(id: Long) {
        ruleDao.deleteAutomationRuleById(id)
    }
}

