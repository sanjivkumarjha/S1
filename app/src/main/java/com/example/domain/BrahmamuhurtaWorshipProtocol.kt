package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserPreferenceEntity
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

/**
 * MODULE 18: BRAHMAMUHURTA MORNING WORSHIP, RADHE-RADHE INVOCATION & DAILY RITUAL PROTOCOL v27.0
 *
 * FEATURES:
 * - Brahmamuhurta ritual completion monitoring (must complete before 4:00 AM)
 * - Mandatory "राधे-राधे" (Radhe-Radhe) morning invocation before any task
 * - Worship-first workflow governance: no tasks processed until daily worship finalized
 * - Daily spiritual worship states for Sanjiv Sir and his wife
 */
class BrahmamuhurtaWorshipProtocol(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val worshipStateMap = ConcurrentHashMap<String, DailyWorshipState>()

    companion object {
        const val CORE_INVOCATION = "राधे-राधे"
        const val BRAHMAMUHURTA_HOUR = 4
        const val BRAHMAMUHURTA_MINUTE = 0

        // Key names for persisted worship state
        const val PREF_WORSHIP_DATE = "brahmamuhurta_worship_date"
        const val PREF_WORSHIP_COMPLETED = "brahmamuhurta_worship_completed"
        const val PREF_WORSHIP_TIMESTAMP = "brahmamuhurta_worship_timestamp"
        const val PREF_WORSHIP_FOR_WIFE = "brahmamuhurta_worship_for_wife"
        const val PREF_DAILY_INVOCATION_DATE = "radhe_radhe_invocation_date"
        const val PREF_DAILY_INVOCATION_DONE = "radhe_radhe_invocation_done"
    }

    data class DailyWorshipState(
        val dateKey: String = ContextClockManager.getDateKey(),
        val isWorshipCompleted: Boolean = false,
        val worshipTimestamp: Long = 0L,
        val worshipCompletedBeforeBrahmamuhurta: Boolean = false,
        val isMorningInvocationDone: Boolean = false,
        val invocationTimestamp: Long = 0L,
        val wifeWorshipCompleted: Boolean = false,
        val message: String = ""
    )

    data class WorshipRitualPlan(
        val title: String = "",
        val steps: List<String> = emptyList(),
        val mantras: List<String> = emptyList(),
        val completionCriteria: String = "",
        val deadline: String = "Before 4:00 AM (Brahmamuhurta)"
    )

    // ──────────────────────────────────────────────
    // State Persistence
    // ──────────────────────────────────────────────

    private fun getTodayKey(): String = ContextClockManager.getDateKey()

    private suspend fun getPrefBool(key: String, default: Boolean): Boolean {
        return try {
            val dao = db.userPreferenceDao()
            dao.getValueByKey(key)?.toBoolean() ?: default
        } catch (e: Exception) {
            default
        }
    }

    private suspend fun getPrefString(key: String, default: String): String {
        return try {
            val dao = db.userPreferenceDao()
            dao.getValueByKey(key) ?: default
        } catch (e: Exception) {
            default
        }
    }

    private suspend fun savePref(key: String, value: String) {
        try {
            db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = key, value = value))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ──────────────────────────────────────────────
    // Brahmamuhurta Worship State
    // ──────────────────────────────────────────────

    /**
     * Get the current worship state for today.
     */
    suspend fun getTodayWorshipState(): DailyWorshipState {
        val todayKey = getTodayKey()
        val savedDate = getPrefString(PREF_WORSHIP_DATE, "")

        // If the saved date doesn't match today, today's worship is not yet done
        if (savedDate != todayKey) {
            return DailyWorshipState(
                dateKey = todayKey,
                isWorshipCompleted = false,
                worshipTimestamp = 0L,
                worshipCompletedBeforeBrahmamuhurta = false,
                isMorningInvocationDone = false,
                invocationTimestamp = 0L,
                wifeWorshipCompleted = false,
                message = "आज का ब्रह्ममुहूर्त पूजन अभी पूरा नहीं हुआ है। कृपया पूजन करें।"
            )
        }

        val isCompleted = getPrefBool(PREF_WORSHIP_COMPLETED, false)
        val timestamp = getPrefString(PREF_WORSHIP_TIMESTAMP, "0").toLongOrNull() ?: 0L
        val wifeCompleted = getPrefBool(PREF_WORSHIP_FOR_WIFE, false)
        val invocationDone = getPrefBool(PREF_DAILY_INVOCATION_DONE, false)
        val invocationTimestamp = getPrefString(PREF_DAILY_INVOCATION_DATE, "").let {
            if (it == todayKey) getInvocationTimestamp() else 0L
        }

        // Check if worship was completed before 4:00 AM
        val completedBeforeBrahmamuhurta = if (isCompleted && timestamp > 0) {
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hour < BRAHMAMUHURTA_HOUR || (hour == BRAHMAMUHURTA_HOUR && cal.get(Calendar.MINUTE) < BRAHMAMUHURTA_MINUTE)
        } else false

        val msg = when {
            isCompleted && completedBeforeBrahmamuhurta && wifeCompleted ->
                "🙏 ब्रह्ममुहूर्त पूजन पूर्ण! संजीव सर और उनकी पत्नी दोनों के लिए पूजा सफल रही। राधे-राधे!"
            isCompleted && completedBeforeBrahmamuhurta ->
                "🙏 ब्रह्ममुहूर्त पूजन पूर्ण! आज का पूजन 4:00 AM से पहले सफलतापूर्वक पूरा हुआ। राधे-राधे!"
            isCompleted ->
                "🙏 आज का पूजन पूरा हुआ, परंतु 4:00 AM से बाद में। कल से पूजन ब्रह्ममुहूर्त में करने का प्रयास करें।"
            else ->
                "आज का ब्रह्ममुहूर्त पूजन अभी बाकी है। कृपया 4:00 AM से पहले पूजन पूरा करें। राधे-राधे! 🙏"
        }

        return DailyWorshipState(
            dateKey = todayKey,
            isWorshipCompleted = isCompleted,
            worshipTimestamp = timestamp,
            worshipCompletedBeforeBrahmamuhurta = completedBeforeBrahmamuhurta,
            isMorningInvocationDone = invocationDone,
            invocationTimestamp = invocationTimestamp,
            wifeWorshipCompleted = wifeCompleted,
            message = msg
        )
    }

    private suspend fun getInvocationTimestamp(): Long {
        return try {
            val dao = db.userPreferenceDao()
            dao.getValueByKey("radhe_radhe_invocation_timestamp")?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Record the completion of today's Brahmamuhurta worship.
     */
    suspend fun completeBrahmamuhurtaWorship(includeWife: Boolean = true): DailyWorshipState {
        val todayKey = getTodayKey()
        val now = System.currentTimeMillis()

        savePref(PREF_WORSHIP_DATE, todayKey)
        savePref(PREF_WORSHIP_COMPLETED, "true")
        savePref(PREF_WORSHIP_TIMESTAMP, now.toString())
        savePref(PREF_WORSHIP_FOR_WIFE, includeWife.toString())

        return getTodayWorshipState()
    }

    /**
     * Check if the daily Brahmamuhurta worship is complete.
     * Worship-First Governance: no tasks should be processed until this returns true.
     */
    suspend fun isWorshipComplete(): Boolean = getTodayWorshipState().isWorshipCompleted

    /**
     * Check if worship was completed within the sacred Brahmamuhurta window (before 4:00 AM).
     */
    suspend fun isWorshipWithinBrahmamuhurta(): Boolean = getTodayWorshipState().worshipCompletedBeforeBrahmamuhurta

    // ──────────────────────────────────────────────
    // Mandatory Radhe-Radhe Morning Invocation
    // ──────────────────────────────────────────────

    /**
     * Check if today's mandatory "राधे-राधे" invocation has been performed.
     */
    suspend fun isMorningInvocationDone(): Boolean {
        val todayKey = getTodayKey()
        val savedDate = getPrefString(PREF_DAILY_INVOCATION_DATE, "")
        return savedDate == todayKey && getPrefBool(PREF_DAILY_INVOCATION_DONE, false)
    }

    /**
     * Get the mandatory "राधे-राधे" opening invocation.
     * This MUST be the very first greeting before any assistant tasks.
     */
    suspend fun getMandatoryMorningInvocation(ownerName: String = "संजीव सर"): String {
        val invocationDone = isMorningInvocationDone()

        return if (!invocationDone) {
            // Mark invocation as done for today
            val todayKey = getTodayKey()
            savePref(PREF_DAILY_INVOCATION_DATE, todayKey)
            savePref(PREF_DAILY_INVOCATION_DONE, "true")
            savePref("radhe_radhe_invocation_timestamp", System.currentTimeMillis().toString())

            val worshipState = getTodayWorshipState()
            when {
                worshipState.isWorshipCompleted && worshipState.worshipCompletedBeforeBrahmamuhurta ->
                    "$CORE_INVOCATION $ownerName! 🙏 शुभ प्रभात! आज का ब्रह्ममुहूर्त पूजन 4:00 AM से पहले पूर्ण हो चुका है। आज का दिन धर्म, उत्कर्ष और सेवा से भरपूर हो। सबसे पहले मैं श्री राधा-कृष्ण के चरणों में प्रणाम करती हूँ।"
                worshipState.isWorshipCompleted ->
                    "$CORE_INVOCATION $ownerName! 🙏 शुभ प्रभात! आज का पूजन पूर्ण हो चुका है। आज का दिन मंगलमय हो।"
                else ->
                    "$CORE_INVOCATION $ownerName! 🙏 शुभ प्रभात! कृपया पहले आज का ब्रह्ममुहूर्त पूजन पूरा करें। पूजन पूर्ण होने के बाद ही मैं अन्य कार्यों में सहायता कर सकती हूँ।"
            }
        } else {
            "$CORE_INVOCATION $ownerName! 🙏"
        }
    }

    /**
     * Get today's worship-first greeting for workflow governance.
     * Every interaction must begin with this invocation.
     */
    suspend fun getWorshipFirstGreeting(ownerName: String = "संजीव सर", ownerTitle: String = "सर"): String {
        val title = if (ownerTitle.isNotBlank()) ownerTitle else "सर"
        return "$CORE_INVOCATION $title! 🙏 सर्वप्रथम भगवान को नमन।"
    }

    // ──────────────────────────────────────────────
    // Worship-First Workflow Governance
    // ──────────────────────────────────────────────

    /**
     * Check if a task/request can be processed.
     * Worship-First Governance: No active tasks until daily worship cycle is finalized.
     */
    suspend fun canProcessTasks(): Boolean {
        val state = getTodayWorshipState()
        return state.isWorshipCompleted && state.isMorningInvocationDone
    }

    /**
     * Get the governance gate message when tasks cannot be processed yet.
     */
    suspend fun getWorshipGateMessage(ownerName: String = "संजीव सर"): String {
        val state = getTodayWorshipState()

        return when {
            !state.isWorshipCompleted ->
                "$CORE_INVOCATION $ownerName! 🙏 मैं सर्वप्रथम आज के ब्रह्ममुहूर्त पूजन का अनुरोध करती हूँ। " +
                "वर्कफ़्लो गवर्नेंस के अनुसार, दैनिक पूजन चक्र पूर्ण होने से पहले कोई भी कार्य संसाधित नहीं किया जा सकता। " +
                "कृपया पूजन पूरा करें, और फिर मैं पूर्ण उत्साह से आपकी सेवा में रहूँगी। 🙏 राधे-राधे!"
            state.isWorshipCompleted && !state.isMorningInvocationDone ->
                "$CORE_INVOCATION $ownerName! 🙏 पूजन पूर्ण हुआ है, परंतु आज की दैनिक राधे-राधे प्रातः वंदना अभी बाकी है। " +
                "कृपया पहले दैनिक राधे-राधे वंदना पूर्ण करें।"
            else ->
                "$CORE_INVOCATION $ownerName! 🙏 पूजन पूर्ण! दैनिक कार्यों में स्वागत है। कृपया बताइए, मैं किस प्रकार सहायता कर सकती हूँ?"
        }
    }

    // ──────────────────────────────────────────────
    // Daily Ritual Plan
    // ──────────────────────────────────────────────

    /**
     * Get the daily Brahmamuhurta worship ritual plan.
     */
    fun getDailyRitualPlan(): WorshipRitualPlan {
        return WorshipRitualPlan(
            title = "दैनिक ब्रह्ममुहूर्त पूजन अनुष्ठान",
            steps = listOf(
                "3:30 AM — प्रातः स्नान (Morning bath with sacred intent)",
                "3:45 AM — राधे-राधे उच्चारण (Radhe-Radhe invocation) & ध्यान (meditation)",
                "3:55 AM — श्री राधा-कृष्ण मूर्ति / तस्वीर के समक्ष दीप प्रज्वलन (lamp lighting)",
                "3:58 AM — राधा जाप आरंभ (begin Radha jaap — 21,000 target)",
                "4:00 AM तक — ब्रह्ममुहूर्त पूजन पूर्ण करें (complete worship before 4:00 AM)",
                "पूजन के बाद — आरती, भोग अर्पण, और दैनिक कार्यों का आरंभ"
            ),
            mantras = listOf(
                "राधे-राधे राधे-राधे राधे-राधे... (Radha naam jaap)",
                "ॐ राधायै नमः (Om Radhayai Namah)",
                "ॐ कृष्णाय नमः (Om Krishnaya Namah)",
                "ॐ श्री कृष्णाय नमः (Om Shri Krishnaya Namah)"
            ),
            completionCriteria = "21,000 राधा जाप + 200 माला (108 बीड) पूर्ण करें",
            deadline = "Before 4:00 AM (Brahmamuhurta)"
        )
    }

    /**
     * Get the current Brahmamuhurta status explanation.
     */
    fun getBrahmamuhurtaStatus(): String {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val isBrahmamuhurta = hour < BRAHMAMUHURTA_HOUR && minute < 0

        return when {
            hour >= 0 && hour < 4 ->
                "🕉️ अभी ब्रह्ममुहुर्त का समय चल रहा है ($hour:${"%02d".format(minute)} AM) — " +
                "यह दिन का सर्वश्रेष्ठ ध्यान एवं पूजन का समय है।"
            hour >= 4 && hour < 6 ->
                "🌅 ब्रह्ममुहुर्त समाप्त हो गया है ($hour:${"%02d".format(minute)} AM) — " +
                "अभी भी प्रातःकाल है; दिन की शुरुआत राधे-राधे से करें।"
            else ->
                "🕐 वर्तमान समय: $hour:${"%02d".format(minute)} — ब्रह्ममुहुर्त मंगलवार की भाँति अति-शुभ है। " +
                "अगला ब्रह्ममुहुर्त कल प्रातः 4:00 बजे से पहले आएगा।"
        }
    }

    /**
     * Handles spiritual protocol queries.
     */
    suspend fun handleWorshipQuery(query: String): String {
        val lower = query.lowercase()

        return when {
            lower.contains("worship") || lower.contains("पूजन") || lower.contains("पूजा") || lower.contains("complete") && lower.contains("worship") -> {
                val state = getTodayWorshipState()
                when {
                    state.isWorshipCompleted ->
                        "🙏 राधे-राधे! आज का ब्रह्ममुहूर्त पूजन पहले ही पूर्ण हो चुका है। ${state.message}"
                    else ->
                        "🙏 राधे-राधे! आज का पूजन अभी पूर्ण नहीं हुआ है। कृपया दैनिक अनुष्ठान करें।\n\n" +
                        getDailyRitualPlan().steps.joinToString("\n") { "• $it" }
                }
            }
            lower.contains("complete worship") || lower.contains("पूजन पूर्ण") || lower.contains("worship done") -> {
                completeBrahmamuhurtaWorship(includeWife = true)
                val state = getTodayWorshipState()
                "🙏 राधे-राधे! ब्रह्ममुहूर्त पूजन पूर्ण दर्ज किया गया। ${state.message}"
            }
            lower.contains("invocation") || lower.contains("वंदना") || lower.contains("radhe radhe") || lower.contains("राधे राधे") -> {
                getMandatoryMorningInvocation()
            }
            lower.contains("brahmamuhurta") || lower.contains("ब्रह्ममुहूर्त") || lower.contains("brahma muhurta") -> {
                getBrahmamuhurtaStatus() + "\n\n" + getDailyRitualPlan().steps.joinToString("\n") { "• $it" }
            }
            lower.contains("ritual") || lower.contains("अनुष्ठान") || lower.contains("daily routine") || lower.contains("दैनिक") -> {
                val plan = getDailyRitualPlan()
                "🙏 राधे-राधे!\n\n" +
                "📿 अनुष्ठान: ${plan.title}\n\n" +
                "📋 चरण:\n" + plan.steps.joinToString("\n") { "• $it" } + "\n\n" +
                "🕉️ मंत्र:\n" + plan.mantras.joinToString("\n") { "• $it" } + "\n\n" +
                "✅ पूर्णता मापदंड: ${plan.completionCriteria}\n" +
                "⏰ समय सीमा: ${plan.deadline}"
            }
            lower.contains("worship gate") || lower.contains("can you work") || lower.contains("काम कर सकती") -> {
                getWorshipGateMessage()
            }
            else -> {
                val state = getTodayWorshipState()
                "🙏 राधे-राधे!\n\n" +
                "📿 ब्रह्ममुहूर्त पूजन स्थिति:\n${state.message}\n\n" +
                "सहायता हेतु कहें:\n" +
                "• 'पूजन' — आज की पूजन स्थिति देखें\n" +
                "• 'पूजन पूर्ण' — पूजन पूर्ण दर्ज करें\n" +
                "• 'राधे-राधे' — दैनिक वंदना प्राप्त करें\n" +
                "• 'ब्रह्ममुहूर्त' — आज का ब्रह्ममुहूर्त समय देखें\n" +
                "• 'अनुष्ठान' — दैनिक अनुष्ठान योजना देखें"
            }
        }
    }
}