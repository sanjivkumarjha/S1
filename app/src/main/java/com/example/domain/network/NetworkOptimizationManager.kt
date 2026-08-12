package com.example.domain.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

enum class NetworkQualityState(
    val id: String,
    val displayName: String,
    val isOnline: Boolean,
    val badgeIcon: String
) {
    FAST("FAST", "High Speed (5G/Wi-Fi)", true, "📡⚡"),
    NORMAL("NORMAL", "Standard (4G/LTE)", true, "📡✓"),
    WEAK("WEAK", "Weak Connection", true, "📡⚠️"),
    VERY_WEAK("VERY_WEAK", "Very Weak Network", true, "📡🔻"),
    OFFLINE("OFFLINE", "Offline (Airplane Mode)", false, "📡❌")
}

data class DeferredAction(
    val id: String,
    val actionName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val execute: suspend () -> Boolean
)

class NetworkOptimizationManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkQualityFlow = MutableStateFlow(detectInitialNetworkQuality())
    val networkQualityFlow: StateFlow<NetworkQualityState> = _networkQualityFlow.asStateFlow()

    private val _isOnlineFlow = MutableStateFlow(_networkQualityFlow.value.isOnline)
    val isOnlineFlow: StateFlow<Boolean> = _isOnlineFlow.asStateFlow()

    private val deferredQueue = mutableListOf<DeferredAction>()
    private val _queuedActionsCount = MutableStateFlow(0)
    val queuedActionsCount: StateFlow<Int> = _queuedActionsCount.asStateFlow()

    init {
        registerNetworkCallback()
    }

    private fun detectInitialNetworkQuality(): NetworkQualityState {
        return try {
            val activeNet = connectivityManager.activeNetwork
            val caps = connectivityManager.getNetworkCapabilities(activeNet) ?: return NetworkQualityState.OFFLINE

            val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            val isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            val isEthernet = caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

            when {
                isWifi || isEthernet -> NetworkQualityState.FAST
                isCellular -> {
                    val downKbps = caps.linkDownstreamBandwidthKbps
                    if (downKbps > 10000) NetworkQualityState.FAST
                    else if (downKbps > 2000) NetworkQualityState.NORMAL
                    else NetworkQualityState.WEAK
                }
                else -> NetworkQualityState.NORMAL
            }
        } catch (e: Exception) {
            NetworkQualityState.OFFLINE
        }
    }

    private fun registerNetworkCallback() {
        try {
            val builder = NetworkRequest.Builder()
            connectivityManager.registerNetworkCallback(
                builder.build(),
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        val newQuality = detectInitialNetworkQuality()
                        val wasOffline = !_isOnlineFlow.value
                        _networkQualityFlow.value = newQuality
                        _isOnlineFlow.value = true

                        if (wasOffline) {
                            processDeferredQueue()
                        }
                    }

                    override fun onLost(network: Network) {
                        _networkQualityFlow.value = NetworkQualityState.OFFLINE
                        _isOnlineFlow.value = false
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        _networkQualityFlow.value = detectInitialNetworkQuality()
                        _isOnlineFlow.value = _networkQualityFlow.value.isOnline
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes a network operation with intelligent timeout, exponential backoff, and weak network protection.
     * Prevents main thread lag and crashes.
     */
    suspend fun <T> executeOptimizedRequest(
        maxRetries: Int = 3,
        timeoutMs: Long = if (_networkQualityFlow.value == NetworkQualityState.WEAK) 10000L else 5000L,
        requestBlock: suspend () -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        if (!_isOnlineFlow.value) {
            return@withContext Result.failure(IllegalStateException("App is currently in Offline Mode."))
        }

        var currentAttempt = 0
        var lastException: Exception? = null

        while (currentAttempt < maxRetries) {
            currentAttempt++
            try {
                val result = withTimeoutOrNull(timeoutMs) {
                    requestBlock()
                }
                if (result != null) {
                    return@withContext Result.success(result)
                } else {
                    lastException = TimeoutException("Network request timed out after $timeoutMs ms")
                }
            } catch (e: Exception) {
                lastException = e
            }

            if (currentAttempt < maxRetries) {
                val backoffDelay = (1000L * currentAttempt) // 1s, 2s, 3s
                delay(backoffDelay)
            }
        }

        return@withContext Result.failure(
            lastException ?: IllegalStateException("Request failed after $maxRetries retries")
        )
    }

    /**
     * Enqueues a non-urgent action to be automatically run when network connectivity is restored.
     */
    fun enqueueDeferredAction(actionName: String, action: suspend () -> Boolean) {
        synchronized(deferredQueue) {
            deferredQueue.add(DeferredAction(id = java.util.UUID.randomUUID().toString(), actionName = actionName, execute = action))
            _queuedActionsCount.value = deferredQueue.size
        }
        if (_isOnlineFlow.value) {
            processDeferredQueue()
        }
    }

    private fun processDeferredQueue() {
        scope.launch {
            val actionsToRun = synchronized(deferredQueue) {
                val list = ArrayList(deferredQueue)
                deferredQueue.clear()
                _queuedActionsCount.value = 0
                list
            }

            for (item in actionsToRun) {
                try {
                    item.execute()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    class TimeoutException(msg: String) : Exception(msg)

    companion object {
        @Volatile
        private var INSTANCE: NetworkOptimizationManager? = null

        fun getInstance(context: Context): NetworkOptimizationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkOptimizationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
