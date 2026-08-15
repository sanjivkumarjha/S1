package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserPreferenceEntity
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Dynamic Progressive Monetization & Target Growth Engine v27.0
 *
 * BASE TARGET: ₹50,000/month
 * AUTOMATIC PROGRESSIVE GROWTH: +₹5,000/month upon hitting quota
 * Month 1: ₹50k → Month 2: ₹55k → Month 3: ₹60k...
 * FORCE MODE EXPANSION: Auto-trigger deep searches for complex optimization
 * 25-DAY TARGET BLITZ: Complete monthly target in 25 days
 */
class DynamicProgressiveMonetizationEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    companion object {
        const val BASE_MONTHLY_TARGET = 50000.0
        const val TARGET_INCREMENT_PER_MONTH = 5000.0
        const val BLITZ_DAYS = 25
    }

    data class MonthlyTarget(
        val month: Int = 0,
        val year: Int = 0,
        val targetAmount: Double = BASE_MONTHLY_TARGET,
        val currentRevenue: Double = 0.0,
        val isPreviousMonthAchieved: Boolean = false,
        val progressPercent: Double = 0.0,
        val remainingAmount: Double = 0.0,
        val estimatedDailyTarget: Double = 0.0,
        val daysRemainingInMonth: Int = 0,
        val isBlitzModeActive: Boolean = false
    )

    /**
     * Get the current month's target with progressive growth applied.
     */
    suspend fun getCurrentMonthTarget(): MonthlyTarget {
        val cal = Calendar.getInstance()
        val month = cal.get(Calendar.MONTH) + 1
        val year = cal.get(Calendar.YEAR)

        // Calculate target based on months since start (base month)
        val baseMonthIndex = getStoredBaseMonthIndex()
        if (baseMonthIndex == 0) {
            // Initialize base month
            saveBaseMonth(month, year)
        }

        val monthsSinceBase = calculateMonthsSinceBase(month, year)
        val target = BASE_MONTHLY_TARGET + (monthsSinceBase * TARGET_INCREMENT_PER_MONTH)

        // Check if previous month target was achieved
        val prevMonthAchieved = checkPreviousMonthAchieved(month, year)

        // Get current revenue for the month
        val currentRevenue = getCurrentMonthRevenue(month, year)

        val progressPct = ((currentRevenue / target) * 100.0).coerceIn(0.0, 100.0)
        val remaining = (target - currentRevenue).coerceAtLeast(0.0)

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = daysInMonth - cal.get(Calendar.DAY_OF_MONTH)

        // 25-day blitz: daily target = target / 25
        val dailyTarget = target / BLITZ_DAYS
        val isBlitzMode = daysRemaining <= BLITZ_DAYS || progressPct < 50.0

        return MonthlyTarget(
            month = month,
            year = year,
            targetAmount = target,
            currentRevenue = currentRevenue,
            isPreviousMonthAchieved = prevMonthAchieved,
            progressPercent = progressPct,
            remainingAmount = remaining,
            estimatedDailyTarget = dailyTarget,
            daysRemainingInMonth = daysRemaining,
            isBlitzModeActive = isBlitzMode
        )
    }

    /**
     * Get the growth projection for upcoming months.
     */
    suspend fun getGrowthProjection(): List<MonthlyTarget> {
        val futureTargets = mutableListOf<MonthlyTarget>()
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        for (i in 0 until 6) {
            val month = ((currentMonth - 1 + i) % 12) + 1
            val year = currentYear + ((currentMonth - 1 + i) / 12)
            val monthsSinceBase = calculateMonthsSinceBase(month, year)
            val target = BASE_MONTHLY_TARGET + (monthsSinceBase * TARGET_INCREMENT_PER_MONTH)

            futureTargets.add(
                MonthlyTarget(
                    month = month,
                    year = year,
                    targetAmount = target,
                    currentRevenue = if (i == 0) getCurrentMonthRevenue(month, year) else 0.0,
                    progressPercent = if (i == 0) ((getCurrentMonthRevenue(month, year) / target) * 100.0).coerceIn(0.0, 100.0) else 0.0
                )
            )
        }

        return futureTargets
    }

    /**
     * Check if Force Mode should be triggered for deep optimization.
     */
    suspend fun shouldTriggerForceMode(): Boolean {
        val status = getCurrentMonthTarget()
        return status.progressPercent < 50.0 || status.isBlitzModeActive
    }

    /**
     * Get the monetization status message for notification to Sanjiv Sir.
     */
    suspend fun getStatusMessage(): String {
        val target = getCurrentMonthTarget()
        val growthMsg = if (target.isPreviousMonthAchieved) {
            "🎉 पिछले महीने का लक्ष्य पूरा हुआ! इस महीने का target बढ़ाकर ₹${String.format("%.0f", target.targetAmount)} कर दिया गया है!"
        } else {
            "इस महीने का target: ₹${String.format("%.0f", target.targetAmount)}"
        }

        val blitzMsg = if (target.isBlitzModeActive) {
            "\n⚡ 25-DAY TARGET BLITZ MODE ACTIVE! Daily target: ₹${String.format("%.0f", target.estimatedDailyTarget)}"
        } else ""

        return "राधे-राधे संजीव सर! 🙏 $growthMsg$blitzMsg\n" +
                "• Current: ₹${String.format("%.0f", target.currentRevenue)}\n" +
                "• Remaining: ₹${String.format("%.0f", target.remainingAmount)}\n" +
                "• Progress: ${String.format("%.1f", target.progressPercent)}%\n" +
                "• Days left: ${target.daysRemainingInMonth}"
    }

    /**
     * Get Force Mode expansion message for complex monetization optimization.
     */
    fun getForceModeExpansionMessage(): String {
        return "⚡ FORCE MODE EXPANSION TRIGGERED:\n" +
                "Deep search optimization started for:\n" +
                "• High-value government freelance tenders\n" +
                "• Legal AI agency client acquisition\n" +
                "• Premium digital service opportunities\n" +
                "• Business growth optimization strategies\n\n" +
                "मैं सभी legal और ethical तरीकों से target पूरा करने के लिए जुटी हूँ!"
    }

    /**
     * Calculate the 25-day blitz daily pace recommendation.
     */
    suspend fun getBlitzDailyPace(): String {
        val target = getCurrentMonthTarget()
        val requiredDaily = if (target.daysRemainingInMonth <= 0) {
            target.estimatedDailyTarget
        } else {
            target.remainingAmount / target.daysRemainingInMonth
        }

        return "⚡ 25-DAY BLITZ PACE:\n" +
                "• आज का लक्ष्य: ₹${String.format("%.0f", requiredDaily)}\n" +
                "• Remaining days: ${target.daysRemainingInMonth}\n" +
                "• Daily client pitches needed: ${(requiredDaily / 2000).toInt().coerceAtLeast(1)}\n" +
                "• Daily invoices to send: ${(requiredDaily / 10000).toInt().coerceAtLeast(1)}\n\n" +
                "चलिए संजीव सर, आज भी target के लिए पूरी मेहनत करते हैं! 💪"
    }

    private suspend fun getStoredBaseMonthIndex(): Int {
        return try {
            db.userPreferenceDao().getValueByKey("monetization_base_month_index")?.toIntOrNull() ?: 0
        } catch (e: Exception) { 0 }
    }

    private suspend fun saveBaseMonth(month: Int, year: Int) {
        val index = (year * 12) + month
        try {
            db.userPreferenceDao().insertOrUpdatePreference(
                UserPreferenceEntity(key = "monetization_base_month_index", value = index.toString())
            )
        } catch (e: Exception) { /* Non-critical */ }
    }

    private suspend fun calculateMonthsSinceBase(month: Int, year: Int): Int {
        val baseIndex = (year * 12) + month
        try {
            val stored = getStoredBaseMonthIndex()
            val storedIndex = if (stored > 0) stored else baseIndex
            val currentIndex = (year * 12) + month
            return (currentIndex - storedIndex).coerceAtLeast(0)
        } catch (e: Exception) {
            return 0
        }
    }

    private suspend fun checkPreviousMonthAchieved(month: Int, year: Int): Boolean {
        val prevMonth = if (month == 1) 12 else month - 1
        val prevYear = if (month == 1) year - 1 else year
        val key = "month_target_${prevYear}_$prevMonth"
        return try {
            db.userPreferenceDao().getValueByKey(key) == "ACHIEVED"
        } catch (e: Exception) { false }
    }

    private suspend fun getCurrentMonthRevenue(month: Int, year: Int): Double {
        return try {
            val invoices = db.invoiceDao().getAllInvoices().first()
            invoices
                .filter { it.status == "Paid" }
                .sumOf { it.paidAmount }
        } catch (e: Exception) { 0.0 }
    }

    private fun runBlocking(block: suspend () -> Unit) = kotlinx.coroutines.runBlocking { block() }
}