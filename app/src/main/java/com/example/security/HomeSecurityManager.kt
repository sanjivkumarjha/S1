package com.example.security

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CctvCameraFeed(
    val id: String,
    val name: String,
    val status: String = "Online",
    val isMotionDetected: Boolean = false,
    val isPersonDetected: Boolean = false,
    val lastEventTime: String = "14:32",
    val hasSnapshot: Boolean = true
)

data class SmartLockDevice(
    val id: String,
    val name: String,
    val isLocked: Boolean = true,
    val batteryPercent: Int = 88,
    val tamperDetected: Boolean = false,
    val lastStateChange: String = "Locked"
)

data class HomeSecurityLogEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestampStr: String,
    val deviceName: String,
    val eventName: String,
    val actionTaken: String,
    val status: String
)

class HomeSecurityManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _cctvCameras = MutableStateFlow(
        listOf(
            CctvCameraFeed("cctv_1", "Front Door CCTV", "Online", isMotionDetected = true, isPersonDetected = true, lastEventTime = "Just now"),
            CctvCameraFeed("cctv_2", "Living Room Cam", "Online", isMotionDetected = false, isPersonDetected = false, lastEventTime = "10 mins ago"),
            CctvCameraFeed("cctv_3", "Backyard Gate Cam", "Online", isMotionDetected = false, isPersonDetected = false, lastEventTime = "1 hour ago")
        )
    )
    val cctvCameras: StateFlow<List<CctvCameraFeed>> = _cctvCameras.asStateFlow()

    private val _smartLocks = MutableStateFlow(
        listOf(
            SmartLockDevice("lock_1", "Main Entrance Smart Lock", isLocked = true, batteryPercent = 92),
            SmartLockDevice("lock_2", "Rear Garden Door Lock", isLocked = true, batteryPercent = 84)
        )
    )
    val smartLocks: StateFlow<List<SmartLockDevice>> = _smartLocks.asStateFlow()

    private val _isSirenActive = MutableStateFlow(false)
    val isSirenActive: StateFlow<Boolean> = _isSirenActive.asStateFlow()

    private val _securityHistory = MutableStateFlow(
        listOf(
            HomeSecurityLogEvent(
                timestampStr = "14:32 Today",
                deviceName = "Front Door CCTV",
                eventName = "Unauthorized Unlock / Motion",
                actionTaken = "Alarm Activated • Lock Secured • Lights ON",
                status = "RESOLVED"
            ),
            HomeSecurityLogEvent(
                timestampStr = "09:15 Yesterday",
                deviceName = "Main Entrance Smart Lock",
                eventName = "Authorized PIN Unlock",
                actionTaken = "Welcome greeting played",
                status = "NORMAL"
            )
        )
    )
    val securityHistory: StateFlow<List<HomeSecurityLogEvent>> = _securityHistory.asStateFlow()

    fun toggleSmartLock(lockId: String) {
        _smartLocks.value = _smartLocks.value.map { lock ->
            if (lock.id == lockId) {
                val newLockState = !lock.isLocked
                val stateText = if (newLockState) "Locked" else "Unlocked"
                logEvent(lock.name, "Manual $stateText Action", "Lock state updated to $stateText", "OK")
                lock.copy(isLocked = newLockState, lastStateChange = stateText)
            } else lock
        }
    }

    fun triggerUnauthorizedEntryWorkflow(deviceName: String = "Front Door") {
        scope.launch {
            _isSirenActive.value = true

            // Lock all smart locks
            _smartLocks.value = _smartLocks.value.map { it.copy(isLocked = true) }

            val timeStr = SimpleDateFormat("HH:mm Today", Locale.getDefault()).format(Date())

            logEvent(
                deviceName = deviceName,
                eventName = "Unauthorized Entry Detected",
                actionTaken = "Snapshot Captured • Siren Activated • All Doors Locked • Lights ON",
                status = "ALARM_ACTIVE"
            )

            CentralizedSecurityManager.getInstance(context).postAlert(
                ActiveSecurityAlert(
                    type = SecurityAlertType.HOME_INTRUSION,
                    title = "🏠 Home Intrusion Alert",
                    message = "Unauthorized entry at $deviceName. Siren active & locks engaged.",
                    priority = 4
                )
            )

            CentralizedSecurityManager.getInstance(context).logSecurityEvent(
                eventType = "HOME_INTRUSION_ALERT",
                description = "Unauthorized entry detected at $deviceName.",
                level = "CRITICAL"
            )
        }
    }

    fun silenceSiren() {
        _isSirenActive.value = false
        CentralizedSecurityManager.getInstance(context).clearAlert()
    }

    private fun logEvent(deviceName: String, eventName: String, actionTaken: String, status: String) {
        val timeStr = SimpleDateFormat("HH:mm Today", Locale.getDefault()).format(Date())
        val newLog = HomeSecurityLogEvent(
            timestampStr = timeStr,
            deviceName = deviceName,
            eventName = eventName,
            actionTaken = actionTaken,
            status = status
        )
        _securityHistory.value = listOf(newLog) + _securityHistory.value
    }

    companion object {
        @Volatile
        private var INSTANCE: HomeSecurityManager? = null

        fun getInstance(context: Context): HomeSecurityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HomeSecurityManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
