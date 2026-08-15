package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserPreferenceEntity
import java.util.Calendar

/**
 * Radha Naam Jaap & Spiritual Routine Counter Engine v27.0
 *
 * MANDATORY DAILY ROUTINE:
 * - 21,000 "Radha" naam jaaps daily
 * - 200 malas (108 beads each = 21,600)
 * - Guides Sanjiv Sir before business/monetization tasks
 * - Family spiritual guidance & devotional thoughts
 */
class RadhaJaapCounterEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    companion object {
        const val DAILY_JAAP_TARGET = 21000
        const val DAILY_MALA_TARGET = 200
        const val BEADS_PER_MALA = 108
    }

    data class JaapStatus(
        val todayCount: Int = 0,
        val todayMalaCount: Int = 0,
        val dailyTargetReached: Boolean = false,
        val malaTargetReached: Boolean = false,
        val progressPercent: Float = 0f,
        val malaProgressPercent: Float = 0f,
        val streakDays: Int = 0,
        val totalLifetimeJaaps: Long = 0,
        val lastJaapTimestamp: Long = 0,
        val isComplete: Boolean = false,
        val message: String = ""
    )

    data class SpiritualGuidance(
        val thought: String = "",
        val verse: String = "",
        val meaning: String = ""
    )

    suspend fun getTodayStatus(): JaapStatus {
        val todayKey = getTodayKey()
        val todayCount = getDailyCount(todayKey)
        val todayMalaCount = getDailyMalaCount(todayKey)
        val totalLifetime = getTotalLifetimeCount()
        val streak = getStreakDays()
        val progressPct = ((todayCount.toFloat() / DAILY_JAAP_TARGET) * 100f).coerceIn(0f, 100f)
        val malaProgressPct = ((todayMalaCount.toFloat() / DAILY_MALA_TARGET) * 100f).coerceIn(0f, 100f)
        val isComplete = todayCount >= DAILY_JAAP_TARGET && todayMalaCount >= DAILY_MALA_TARGET

        val msg = when {
            isComplete -> "राधे-राधे! आज का 21,000 जाप और 200 माला पूरा हो गया! 🙏✨ बहुत-बहुत बधाई संजीव सर!"
            todayCount >= DAILY_JAAP_TARGET -> "21,000 जाप पूरे! अब कृपया 200 माला भी पूरी करें। राधे-राधे! 🙏"
            todayMalaCount >= DAILY_MALA_TARGET -> "200 माला पूरी! अब कृपया 21,000 जाप भी पूरे करें। राधे-राधे! 🙏"
            todayCount > 0 || todayMalaCount > 0 -> "आज $todayCount जाप, $todayMalaCount माला हुई। $DAILY_JAAP_TARGET जाप और $DAILY_MALA_TARGET माला का लक्ष्य है। चलिए शुरू करते हैं! 🙏"
            else -> "राधे-राधे संजीव सर! आज का 21,000 राधा जाप और 200 माला का लक्ष्य है। कृपया शुरू करें! 🙏✨"
        }

        return JaapStatus(
            todayCount = todayCount, todayMalaCount = todayMalaCount,
            dailyTargetReached = todayCount >= DAILY_JAAP_TARGET,
            malaTargetReached = todayMalaCount >= DAILY_MALA_TARGET,
            progressPercent = progressPct, malaProgressPercent = malaProgressPct,
            streakDays = streak, totalLifetimeJaaps = totalLifetime,
            lastJaapTimestamp = getLastJaapTimestamp(), isComplete = isComplete, message = msg
        )
    }

    suspend fun recordJaap(count: Int = 1): JaapStatus {
        val todayKey = getTodayKey()
        val current = getDailyCount(todayKey)
        val newCount = (current + count).coerceAtMost(DAILY_JAAP_TARGET)
        saveDailyCount(todayKey, newCount)
        incrementLifetimeCount(count.toLong())
        updateLastJaapTimestamp()
        updateStreak()
        return getTodayStatus()
    }

    suspend fun recordMala(): JaapStatus {
        val todayKey = getTodayKey()
        val currentMala = getDailyMalaCount(todayKey)
        val newMalaCount = (currentMala + 1).coerceAtMost(DAILY_MALA_TARGET)
        saveDailyMalaCount(todayKey, newMalaCount)
        recordJaap(BEADS_PER_MALA)
        return getTodayStatus()
    }

    fun getDailySpiritualGuidance(): SpiritualGuidance {
        val thoughts = listOf(
            SpiritualGuidance("राधा नाम का जाप करने से मन शुद्ध होता है और ईश्वर की कृपा प्राप्त होती है।", "राधे राधे राधे राधे, राधे राधे राधे राधे।", "जितनी बार राधा नाम लें, उतनी ही बार प्रेम बढ़ता है।"),
            SpiritualGuidance("प्रभु की भक्ति में समय बिताने से जीवन में शांति और संतोष आता है।", "हरे कृष्ण हरे कृष्ण, कृष्ण कृष्ण हरे हरे।", "भगवान के नाम का जाप करने से मन की सभी चिंताएं दूर हो जाती हैं।"),
            SpiritualGuidance("राधा-कृष्ण की भक्ति ही सच्चा धर्म है। निष्काम भाव से सेवा करो।", "राधा कृष्ण प्रणाम, जय राधे कृष्णा।", "राधा के बिना कृष्ण अधूरे हैं, और कृष्ण के बिना राधा।"),
            SpiritualGuidance("प्रतिदिन नियमित रूप से जाप करने से आध्यात्मिक ऊर्जा बढ़ती है।", "राधे राधे जप करो, राधे राधे भजो।", "कलियुग में राधा-कृष्ण नाम का जाप ही सबसे सरल साधना है।"),
            SpiritualGuidance("भक्ति ही सबसे बड़ी शक्ति है। प्रेम से ही ईश्वर को पाया जा सकता है।", "वहां जहां राधा-कृष्ण की लीला होती है, वहां भक्ति स्वतः प्रकट होती है।", "सच्ची भक्ति में कोई दिखावा नहीं, केवल प्रेम और समर्पण है।")
        )
        return thoughts[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % thoughts.size]
    }

    suspend fun getMorningRoutinePrompt(): String {
        val status = getTodayStatus()
        if (status.isComplete) {
            return "राधे-राधे संजीव सर! आज का 21,000 जाप और 200 माला पूरी हो चुकी है। 🙏✨ अब हम मोनेटाइज़ेशन के काम शुरू कर सकते हैं।"
        }
        val remainingJaap = (DAILY_JAAP_TARGET - status.todayCount).coerceAtLeast(0)
        val remainingMala = (DAILY_MALA_TARGET - status.todayMalaCount).coerceAtLeast(0)
        return "राधे-राधे संजीव सर! 🙏 काम शुरू करने से पहले, कृपया आज का राधा जाप पूरा कर लें।\n\nआज: जाप ${status.todayCount}/$DAILY_JAAP_TARGET (बाकी: $remainingJaap) | माला ${status.todayMalaCount}/$DAILY_MALA_TARGET (बाकी: $remainingMala)\n\n${getDailySpiritualGuidance().thought}\n\nजैसे ही जाप पूरा होगा, मैं सभी बिज़नेस टास्क शुरू कर दूंगी। राधे-राधे! 🙏"
    }

    fun getFamilySpiritualGuidance(familyMemberName: String): String {
        return "राधे-राधे $familyMemberName! 🙏 आपके लिए आज का भक्ति विचार:\n\n${getDailySpiritualGuidance().thought}\n\nईश्वर की भक्ति से जीवन में शांति और खुशियां आती हैं। राधे-राधे! 🙏"
    }

    private suspend fun getDailyCount(key: String): Int {
        return try { db.userPreferenceDao().getValueByKey(key)?.toIntOrNull() ?: 0 } catch (e: Exception) { 0 }
    }

    private suspend fun getDailyMalaCount(key: String): Int {
        return try { db.userPreferenceDao().getValueByKey("${key}_mala")?.toIntOrNull() ?: 0 } catch (e: Exception) { 0 }
    }

    private suspend fun saveDailyCount(key: String, count: Int) {
        try { db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = key, value = count.toString())) } catch (e: Exception) {}
    }

    private suspend fun saveDailyMalaCount(key: String, count: Int) {
        try { db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = "${key}_mala", value = count.toString())) } catch (e: Exception) {}
    }

    private suspend fun getTotalLifetimeCount(): Long {
        return try { db.userPreferenceDao().getValueByKey("jaap_lifetime_total")?.toLongOrNull() ?: 0L } catch (e: Exception) { 0L }
    }

    private suspend fun incrementLifetimeCount(amount: Long) {
        try {
            val current = getTotalLifetimeCount()
            db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = "jaap_lifetime_total", value = (current + amount).toString()))
        } catch (e: Exception) {}
    }

    private suspend fun getLastJaapTimestamp(): Long {
        return try { db.userPreferenceDao().getValueByKey("jaap_last_timestamp")?.toLongOrNull() ?: 0L } catch (e: Exception) { 0L }
    }

    private suspend fun updateLastJaapTimestamp() {
        try { db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = "jaap_last_timestamp", value = System.currentTimeMillis().toString())) } catch (e: Exception) {}
    }

    private suspend fun getStreakDays(): Int {
        return try { db.userPreferenceDao().getValueByKey("jaap_streak_days")?.toIntOrNull() ?: 0 } catch (e: Exception) { 0 }
    }

    private suspend fun updateStreak() {
        try {
            val todayKey = getTodayKey()
            val yesterdayKey = getYesterdayKey()
            val yesterdayCompleted = getDailyCount(yesterdayKey) >= DAILY_JAAP_TARGET
            val todayCompleted = getDailyCount(todayKey) >= DAILY_JAAP_TARGET
            val currentStreak = getStreakDays()
            val newStreak = when {
                todayCompleted && yesterdayCompleted -> currentStreak + 1
                todayCompleted && !yesterdayCompleted -> 1
                else -> currentStreak
            }
            db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = "jaap_streak_days", value = newStreak.toString()))
        } catch (e: Exception) {}
    }

    private fun getTodayKey(): String {
        val cal = Calendar.getInstance()
        return "jaap_${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun getYesterdayKey(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return "jaap_${cal.get(Calendar.YEAR)}_${cal.get(Calendar.DAY_OF_YEAR)}"
    }
}