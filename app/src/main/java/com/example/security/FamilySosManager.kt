package com.example.security

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

enum class SosState {
    IDLE,
    COUNTDOWN,
    ACTIVE,
    CANCELLED
}

data class FamilySosContact(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val relationship: String,
    val phoneNumber: String,
    val emergencyPriority: Int = 1,
    val canShareLocation: Boolean = true
)

data class SosActiveProgress(
    val state: SosState = SosState.IDLE,
    val secondsRemaining: Int = 10,
    val targetContacts: List<FamilySosContact> = emptyList(),
    val preparedMessage: String = "",
    val locationString: String = "GPS: 28.6139° N, 77.2090° E (New Delhi)"
)

class FamilySosManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _familyContacts = MutableStateFlow<List<FamilySosContact>>(
        listOf(
            FamilySosContact(1, "Mother", "Mother", "+91 9876543210", 1, true),
            FamilySosContact(2, "Father", "Father", "+91 9876543211", 2, true)
        )
    )
    val familyContacts: StateFlow<List<FamilySosContact>> = _familyContacts.asStateFlow()

    private val _sosProgress = MutableStateFlow(SosActiveProgress())
    val sosProgress: StateFlow<SosActiveProgress> = _sosProgress.asStateFlow()

    private var sosCountdownJob: Job? = null

    fun addFamilyContact(
        name: String,
        relationship: String,
        phoneNumber: String,
        emergencyPriority: Int = 1,
        canShareLocation: Boolean = true
    ) {
        val contact = FamilySosContact(
            id = System.currentTimeMillis(),
            name = name,
            relationship = relationship,
            phoneNumber = phoneNumber,
            emergencyPriority = emergencyPriority,
            canShareLocation = canShareLocation
        )
        _familyContacts.value = _familyContacts.value + contact
    }

    fun deleteFamilyContact(contact: FamilySosContact) {
        _familyContacts.value = _familyContacts.value.filter { it.id != contact.id }
    }

    fun triggerSosWorkflow(
        ownerName: String = "Sanjiv",
        customLocation: String = "GPS: 28.6139° N, 77.2090° E (New Delhi)",
        onCompleted: () -> Unit = {}
    ) {
        scope.launch {
            val contacts = _familyContacts.value
            val timeStr = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault()).format(Date())
            val message = "Emergency alert from $ownerName. Possible emergency detected. Location: $customLocation. Time: $timeStr. Please contact immediately."

            _sosProgress.value = SosActiveProgress(
                state = SosState.COUNTDOWN,
                secondsRemaining = 10,
                targetContacts = contacts,
                preparedMessage = message,
                locationString = customLocation
            )

            CentralizedSecurityManager.getInstance(context).postAlert(
                ActiveSecurityAlert(
                    type = SecurityAlertType.EMERGENCY_SOS,
                    title = "🚨 SOS COUNTDOWN",
                    message = "Alerting emergency contacts in 10s...",
                    priority = 1
                )
            )

            sosCountdownJob = scope.launch {
                for (sec in 10 downTo 1) {
                    if (_sosProgress.value.state != SosState.COUNTDOWN) break
                    _sosProgress.value = _sosProgress.value.copy(secondsRemaining = sec)
                    delay(1000)
                }

                if (_sosProgress.value.state == SosState.COUNTDOWN) {
                    _sosProgress.value = _sosProgress.value.copy(state = SosState.ACTIVE, secondsRemaining = 0)
                    
                    CentralizedSecurityManager.getInstance(context).postAlert(
                        ActiveSecurityAlert(
                            type = SecurityAlertType.EMERGENCY_SOS,
                            title = "🚨 SOS ACTIVE",
                            message = "Emergency alert sent to ${contacts.size} contacts.",
                            priority = 1
                        )
                    )
                    
                    CentralizedSecurityManager.getInstance(context).logSecurityEvent(
                        eventType = "SOS_TRIGGERED",
                        description = "SOS Emergency alert sent to contacts: ${contacts.map { it.name }}",
                        level = "CRITICAL"
                    )

                    dispatchSosToContacts(contacts, message)
                    onCompleted()
                }
            }
        }
    }

    fun cancelSosWorkflow() {
        sosCountdownJob?.cancel()
        sosCountdownJob = null
        _sosProgress.value = SosActiveProgress(state = SosState.CANCELLED)
        
        scope.launch {
            CentralizedSecurityManager.getInstance(context).logSecurityEvent(
                eventType = "SOS_CANCELLED",
                description = "SOS alert cancelled during countdown by owner.",
                level = "MEDIUM"
            )
            CentralizedSecurityManager.getInstance(context).clearAlert()
            delay(2000)
            _sosProgress.value = SosActiveProgress(state = SosState.IDLE)
        }
    }

    private fun dispatchSosToContacts(contacts: List<FamilySosContact>, message: String) {
        contacts.forEach { contact ->
            try {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${contact.phoneNumber}")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FamilySosManager? = null

        fun getInstance(context: Context): FamilySosManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FamilySosManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
