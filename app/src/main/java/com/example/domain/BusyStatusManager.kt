package com.example.domain

/**
 * Work Priority & Reverts Management v14.0
 *
 * BUSY STATUS HANDLING:
 * If the assistant is actively carrying out background tasks/monetization work and
 * receives an external distraction, state: "मैं अभी काम कर रही हूँ, थोड़ी देर में रिवर्ट करती हूँ।"
 *
 * CRITICAL OVERRIDE:
 * Pause current work ONLY if the caller indicates a critical emergency.
 */
class BusyStatusManager {

    /** Current busy state */
    @kotlin.jvm.Volatile
    private var isBusyWithBackgroundTask = false

    @kotlin.jvm.Volatile
    private var currentTaskDescription = ""

    private val pendingRevertQueue = mutableListOf<RevertEntry>()

    data class RevertEntry(
        val callerName: String,
        val callerPhone: String = "",
        val message: String = "",
        val timestamp: Long = System.currentTimeMillis(),
        val isEmergency: Boolean = false
    )

    data class BusyInterruptionResult(
        val shouldPauseCurrentWork: Boolean,
        val responseMessage: String,
        val isEmergency: Boolean
    )

    private val emergencyKeywords = listOf(
        "emergency", "urgent", "critical", "emergency call", "help",
        "आपातकाल", "जरूरी", "emergency", "immediately", "accident",
        "hospital", "ambulance", "police", "fire", "डॉक्टर",
        "heart attack", "chest pain", "bleeding", "unconscious"
    )

    /**
     * Set the assistant's busy state.
     */
    fun setBusy(description: String = "background task") {
        isBusyWithBackgroundTask = true
        currentTaskDescription = description
    }

    /**
     * Clear the assistant's busy state.
     */
    fun setAvailable() {
        isBusyWithBackgroundTask = false
        currentTaskDescription = ""
    }

    /**
     * Check if assistant is currently busy.
     */
    fun isBusy(): Boolean = isBusyWithBackgroundTask

    /**
     * Get current task description.
     */
    fun getCurrentTask(): String = currentTaskDescription

    /**
     * Handle an incoming interaction while busy.
     * Returns the response string and whether to pause current work.
     */
    fun handleBusyInterruption(
        message: String,
        callerName: String = "",
        callerPhone: String = "",
        languageCode: String = "hi"
    ): BusyInterruptionResult {
        val lowerMessage = message.lowercase().trim()

        // Check if message indicates a critical emergency
        val isEmergency = emergencyKeywords.any { lowerMessage.contains(it) }

        if (isEmergency) {
            // Critical override - pause current work
            return BusyInterruptionResult(
                shouldPauseCurrentWork = true,
                responseMessage = getEmergencyResponse(languageCode),
                isEmergency = true
            )
        }

        // Non-emergency - queue the revert
        pendingRevertQueue.add(
            RevertEntry(
                callerName = callerName,
                callerPhone = callerPhone,
                message = message
            )
        )

        return BusyInterruptionResult(
            shouldPauseCurrentWork = false,
            responseMessage = getBusyResponse(languageCode),
            isEmergency = false
        )
    }

    /**
     * Get pending reverts that need to be handled.
     */
    fun getPendingReverts(): List<RevertEntry> = pendingRevertQueue.toList()

    /**
     * Clear processed reverts.
     */
    fun clearProcessedReverts() {
        pendingRevertQueue.clear()
    }

    /**
     * Get the busy status response message.
     */
    private fun getBusyResponse(languageCode: String): String {
        return when (languageCode.lowercase()) {
            "hi" -> "मैं अभी काम कर रही हूँ, थोड़ी देर में आपको रिवर्ट करती हूँ। धन्यवाद!"
            "en" -> "I'm currently working on a task. I'll revert to you shortly. Thank you!"
            "hinglish" -> "मैं अभी काम कर रही हूँ, थोड़ी देर में revert करती हूँ। Thank you!"
            else -> "मैं अभी काम कर रही हूँ, थोड़ी देर में आपको रिवर्ट करती हूँ। धन्यवाद!"
        }
    }

    /**
     * Get the emergency override response.
     */
    private fun getEmergencyResponse(languageCode: String): String {
        return when (languageCode.lowercase()) {
            "hi" -> "आपातकाल! मैं अभी काम रोक रही हूँ। कृपया बताइएं क्या आपात्काल है।"
            "en" -> "Emergency detected! I am pausing my current work. Please tell me about the emergency."
            "hinglish" -> "Emergency! Main abhi kaam rok rahi hoon. Kripaya bataayein kya emergency hai."
            else -> "Emergency detected! I am pausing my current work. Please tell me about the emergency."
        }
    }
}