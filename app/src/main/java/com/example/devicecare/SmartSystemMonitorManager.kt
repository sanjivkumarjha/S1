package com.example.devicecare

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.voice.VoiceAssistantManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmartSystemMonitorManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var voiceManager: VoiceAssistantManager? = null

    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging.asStateFlow()

    private val _isCablePlugged = MutableStateFlow(false)
    val isCablePlugged: StateFlow<Boolean> = _isCablePlugged.asStateFlow()

    private val _isSwitchOffWarning = MutableStateFlow(false)
    val isSwitchOffWarning: StateFlow<Boolean> = _isSwitchOffWarning.asStateFlow()

    private val _isNetworkConnected = MutableStateFlow(true)
    val isNetworkConnected: StateFlow<Boolean> = _isNetworkConnected.asStateFlow()

    private val _lastAlertMessage = MutableStateFlow("")
    val lastAlertMessage: StateFlow<String> = _lastAlertMessage.asStateFlow()

    private var hasLowBatteryAlerted = false
    private var hasOfflineAlerted = false
    private var isMonitoring = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(cntx: Context?, intent: Intent?) {
            intent?.let { handleBatteryIntent(it) }
        }
    }

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        voiceManager = VoiceAssistantManager(context)

        // Register Battery & Power Receivers
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(batteryReceiver, filter)

        // Register Connectivity Callback
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isNetworkConnected.value = true
                    hasOfflineAlerted = false
                }

                override fun onLost(network: Network) {
                    _isNetworkConnected.value = false
                    if (!hasOfflineAlerted) {
                        hasOfflineAlerted = true
                        val alert = "Boss, internet disconnected, please check!"
                        _lastAlertMessage.value = alert
                        speakAlert(alert)
                    }
                }
            }

            try {
                connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            } catch (e: Exception) {
                Log.e("SmartSystemMonitor", "Error registering network callback", e)
            }
        }
    }

    fun stopMonitoring() {
        if (!isMonitoring) return
        isMonitoring = false
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            Log.e("SmartSystemMonitor", "Receiver unregister error", e)
        }

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e("SmartSystemMonitor", "Network callback unregister error", e)
            }
        }
    }

    private fun handleBatteryIntent(intent: Intent) {
        val action = intent.action
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val rawPct = if (level >= 0 && scale > 0) (level * 100) / scale else 100
        _batteryLevel.value = rawPct

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val isChargingNow = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val isPluggedNow = plugged != 0

        _isCharging.value = isChargingNow
        _isCablePlugged.value = isPluggedNow

        // Low battery check (<15%)
        if (rawPct in 1..14 && !isChargingNow && !hasLowBatteryAlerted) {
            hasLowBatteryAlerted = true
            val alert = "Boss, battery low, please check! Current level is $rawPct%."
            _lastAlertMessage.value = alert
            speakAlert(alert)
        } else if (rawPct >= 20 || isChargingNow) {
            hasLowBatteryAlerted = false
        }

        // Handle Cable Plugged event with 5 second switch check
        if (action == Intent.ACTION_POWER_CONNECTED) {
            _isSwitchOffWarning.value = false
            scope.launch {
                delay(5000) // Wait 5 seconds as per requirement
                checkCableSwitchStatus()
            }
        } else if (action == Intent.ACTION_POWER_DISCONNECTED) {
            _isSwitchOffWarning.value = false
        }
    }

    private fun checkCableSwitchStatus() {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        if (batteryIntent != null) {
            val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val isPlugged = plugged != 0
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL

            if (isPlugged && !isCharging) {
                _isSwitchOffWarning.value = true
                val alert = "Boss, cable is plugged in, but switch is off! Please turn on the power."
                _lastAlertMessage.value = alert
                speakAlert(alert)
            } else {
                _isSwitchOffWarning.value = false
            }
        }
    }

    private fun speakAlert(message: String) {
        scope.launch {
            voiceManager?.speak(message, "en")
        }
    }

    companion object {
        @Volatile
        private var instance: SmartSystemMonitorManager? = null

        fun getInstance(context: Context): SmartSystemMonitorManager {
            return instance ?: synchronized(this) {
                instance ?: SmartSystemMonitorManager(context.applicationContext).also {
                    instance = it
                    it.startMonitoring()
                }
            }
        }
    }
}
