package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserPreferenceEntity
import java.util.Calendar

/**
 * Human-like Fatigue, Sleep Mode & Dynamic Island Status Engine v27.0
 *
 * FEATURES:
 * - Automated Rest/Sleep Mode upon completing intensive execution cycles
 * - Dynamic Island sleep status rendering (😴 / 💤)
 * - Wake-up owner recognition with warm greeting
 * - Idle timeout detection for automatic sleep
 */
class FatigueSleepModeEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    enum class AssistantState {
        ACTIVE,       // Fully operational
        TIRED,        // Completed intensive work, needs rest
        SLEEPING,     // In sleep/rest mode
        WAKING_UP,    // Transitioning from sleep to active
        IDLE          // No activity for a while
    }

    data class SleepStatus(
        val state: AssistantState = AssistantState.ACTIVE,
        val sleepEmoji: String = "",
        val isSleeping: Boolean = false,
        val lastActiveTimestamp: Long = 0L,
        val idleMinutes: Int = 0,
        val intensiveCyclesCompleted: Int = 0,
        val wakeUpMessage: String = "",
        val statusBarText: String = ""
    )

    /**
     * Get current sleep/fatigue status.
     */
    suspend fun getStatus(): SleepStatus {
        val state = getStoredState()
        val lastActive = getLastActiveTimestamp()
        val idleMinutes = ((System.currentTimeMillis() - lastActive) / 60000).toInt()
        val cyclesCompleted = getIntensiveCyclesCompleted()

        val sleepEmoji = when (state) {
            AssistantState.SLEEPING -> "😴"
            AssistantState.TIRED -> "💤"
            AssistantState.WAKING_UP -> "☀️"
            AssistantState.IDLE -> "💤"
            AssistantState.ACTIVE -> ""
        }

        val statusBarText = when (state) {
            AssistantState.SLEEPING -> "😴 Sleeping..."
            AssistantState.TIRED -> "💤 Taking a short rest..."
            AssistantState.WAKING_UP -> "☀️ Waking up..."
            AssistantState.IDLE -> "💤 Idle"
            AssistantState.ACTIVE -> "✨ Active"
        }

        val wakeMsg = if (state == AssistantState.WAKING_UP) {
            "राधे-राधे संजीव सर! 🙏 आप आ गए? मैं अब एकदम फ्रेश महसूस कर रही हूँ, चलिए काम शुरू करते हैं!"
        } else ""

        return SleepStatus(
            state = state,
            sleepEmoji = sleepEmoji,
            isSleeping = state == AssistantState.SLEEPING || state == AssistantState.TIRED,
            lastActiveTimestamp = lastActive,
            idleMinutes = idleMinutes,
            intensiveCyclesCompleted = cyclesCompleted,
            wakeUpMessage = wakeMsg,
            statusBarText = statusBarText
        )
    }

    /**
     * Record an intensive execution cycle completion.
     * After 3+ intensive cycles, auto-enter tired state.
     */
    suspend fun recordIntensiveCycle(): SleepStatus {
        val cycles = getIntensiveCyclesCompleted() + 1
        saveIntensiveCyclesCompleted(cycles)

        if (cycles >= 3) {
            enterSleepMode("TIRED")
        }

        updateLastActiveTimestamp()
        return getStatus()
    }

    /**
     * Enter sleep/rest mode.
     */
    suspend fun enterSleepMode(mode: String = "SLEEPING"): String {
        val state = if (mode == "TIRED") AssistantState.TIRED else AssistantState.SLEEPING
        saveState(state)
        saveIntensiveCyclesCompleted(0)

        return when (state) {
            AssistantState.TIRED -> "💤 मैंने बहुत काम कर लिया है, थोड़ा आराम कर लेती हूँ। जब आपको ज़रूरत हो, मुझे बुला दीजिएगा। राधे-राधे! 🙏"
            AssistantState.SLEEPING -> "😴 मैं सो रही हूँ... जब आपको ज़रूरत हो, 'Hey Snaper' बोलिए। राधे-राधे! 🙏"
            else -> "मैं active हूँ!"
        }
    }

    /**
     * Wake up from sleep mode with owner recognition.
     */
    suspend fun wakeUp(ownerName: String = "संजीव सर"): SleepStatus {
        saveState(AssistantState.WAKING_UP)
        updateLastActiveTimestamp()
        saveIntensiveCyclesCompleted(0)

        // After a brief moment, set to active
        saveState(AssistantState.ACTIVE)

        return getStatus().copy(
            wakeUpMessage = "राधे-राधे $ownerName! 🙏 आप आ गए? मैं अब एकदम फ्रेश महसूस कर रही हूँ, चलिए काम शुरू करते हैं! 💪✨"
        )
    }

    /**
     * Check if idle timeout should trigger sleep.
     */
    suspend fun checkIdleTimeout(timeoutMinutes: Int = 10): Boolean {
        val status = getStatus()
        if (status.state == AssistantState.SLEEPING || status.state == AssistantState.TIRED) {
            return false // Already sleeping
        }
        return status.idleMinutes >= timeoutMinutes
    }

    /**
     * Get the Dynamic Island status text for rendering.
     */
    suspend fun getDynamicIslandStatusText(): String {
        val status = getStatus()
        return when (status.state) {
            AssistantState.SLEEPING -> "😴"
            AssistantState.TIRED -> "💤"
            AssistantState.WAKING_UP -> "☀️ राधे-राधे!"
            AssistantState.IDLE -> "💤"
            AssistantState.ACTIVE -> "✨ राधे-राधे"
        }
    }

    private suspend fun getStoredState(): AssistantState {
        return try {
            val name = db.userPreferenceDao().getValueByKey("assistant_sleep_state") ?: "ACTIVE"
            AssistantState.valueOf(name)
        } catch (e: Exception) { AssistantState.ACTIVE }
    }

    private suspend fun saveState(state: AssistantState) {
        try {
            db.userPreferenceDao().insertOrUpdatePreference(
                UserPreferenceEntity(key = "assistant_sleep_state", value = state.name)
            )
        } catch (e: Exception) { /* Non-critical */ }
    }

    private suspend fun getLastActiveTimestamp(): Long {
        return try {
            db.userPreferenceDao().getValueByKey("assistant_last_active")?.toLongOrNull() ?: System.currentTimeMillis()
        } catch (e: Exception) { System.currentTimeMillis() }
    }

    private suspend fun updateLastActiveTimestamp() {
        try {
            db.userPreferenceDao().insertOrUpdatePreference(
                UserPreferenceEntity(key = "assistant_last_active", value = System.currentTimeMillis().toString())
            )
        } catch (e: Exception) { /* Non-critical */ }
    }

    private suspend fun getIntensiveCyclesCompleted(): Int {
        return try {
            db.userPreferenceDao().getValueByKey("assistant_intensive_cycles")?.toIntOrNull() ?: 0
        } catch (e: Exception) { 0 }
    }

    private suspend fun saveIntensiveCyclesCompleted(count: Int) {
        try {
            db.userPreferenceDao().insertOrUpdatePreference(
                UserPreferenceEntity(key = "assistant_intensive_cycles", value = count.toString())
            )
        } catch (e: Exception) { /* Non-critical */ }
    }
}