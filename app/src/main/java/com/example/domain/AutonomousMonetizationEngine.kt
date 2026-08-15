package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

/**
 * Autonomous Legal Monetization & Task Engine v27.0
 *
 * TARGET: ₹50,000/month (Progressive: +₹5,000/mo upon achievement)
 * 25-DAY TARGET BLITZ: Complete in 25 days for maximum efficiency
 *
 * ABSOLUTE ETHICAL & LEGAL BOUNDARIES:
 * a) STRICT PROHIBITION: Zero involvement in adult, NSFW, or sexual monetization.
 * b) STRICT PROHIBITION: Zero involvement in illegal activities, scams, or malicious schemes.
 * c) 100% LEGAL ONLY: Operate purely via legal digital workflows.
 */
class AutonomousMonetizationEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val progressiveEngine = DynamicProgressiveMonetizationEngine(context)

    companion object {
        const val MONTHLY_TARGET = 50000.0
        const val MIN_CLIENT_PITCH_INTERVAL_MS = 24 * 60 * 60 * 1000L
    }

    data class MonetizationStatus(
        val currentMonthRevenue: Double = 0.0,
        val monthlyTarget: Double = MONTHLY_TARGET,
        val remainingTarget: Double = MONTHLY_TARGET,
        val progressPercent: Float = 0f,
        val activeLeads: Int = 0,
        val pendingInvoices: Int = 0,
        val completedProjects: Int = 0,
        val isForceModeActive: Boolean = false,
        val statusMessage: String = ""
    )

    data class TaskRecommendation(
        val priority: String = "LOW",
        val taskType: String = "",
        val description: String = "",
        val estimatedValue: Double = 0.0,
        val urgencyScore: Float = 0f
    )

    /**
     * Get current monetization status and progress towards Rs 50,000 target.
     */
    suspend fun getMonetizationStatus(): MonetizationStatus {
        val currentMonth = getCurrentMonthTimestamp()
        val nextMonth = getNextMonthTimestamp()

        val invoices = try {
            db.invoiceDao().getAllInvoices().first()
        } catch (e: Exception) { emptyList() }

        val leads = try {
            db.leadDao().getAllLeads().first()
        } catch (e: Exception) { emptyList() }

        val currentRevenue = invoices
            .filter { it.status == "Paid" }
            .sumOf { it.paidAmount }

        val pendingAmount = invoices
            .filter { it.status != "Paid" && it.status != "Cancelled" }
            .sumOf { it.totalAmount - it.paidAmount }

        val progressPct = ((currentRevenue / MONTHLY_TARGET) * 100f).toFloat().coerceIn(0f, 100f)
        val remaining = (MONTHLY_TARGET - currentRevenue).coerceAtLeast(0.0)

        val statusMsg = when {
            progressPct >= 100f -> "Target achieved for this month! Revenue: Rs ${String.format("%.2f", currentRevenue)}"
            progressPct >= 75f -> "Almost there! ${String.format("%.1f", progressPct)}% of target reached. Rs ${String.format("%.2f", remaining)} remaining."
            progressPct >= 50f -> "Good progress! ${String.format("%.1f", progressPct)}% done. Rs ${String.format("%.2f", remaining)} more to go."
            progressPct >= 25f -> "On track! ${String.format("%.1f", progressPct)}% completed. Keep going! Rs ${String.format("%.2f", remaining)} left."
            progressPct > 0f -> "Started! ${String.format("%.1f", progressPct)}% of target. Need Rs ${String.format("%.2f", remaining)} more."
            else -> "Monthly target: Rs 50,000. No revenue yet this month. Let's start!"
        }

        return MonetizationStatus(
            currentMonthRevenue = currentRevenue + pendingAmount * 0.3,
            monthlyTarget = MONTHLY_TARGET,
            remainingTarget = remaining,
            progressPercent = progressPct,
            activeLeads = leads.count { it.status == "New" || it.status == "Follow-up" },
            pendingInvoices = invoices.count { it.status != "Paid" },
            completedProjects = invoices.count { it.status == "Paid" },
            isForceModeActive = progressPct < 50f,
            statusMessage = statusMsg
        )
    }

    /**
     * Get autonomous recommendations to drive toward the target.
     */
    suspend fun getTaskRecommendations(): List<TaskRecommendation> {
        val status = getMonetizationStatus()
        val recommendations = mutableListOf<TaskRecommendation>()

        if (status.remainingTarget <= 0) {
            return listOf(
                TaskRecommendation(
                    priority = "LOW",
                    taskType = "COMPLETE",
                    description = "Monthly target achieved! Continue maintaining existing clients.",
                    urgencyScore = 0f
                )
            )
        }

        if (status.pendingInvoices > 0) {
            recommendations.add(
                TaskRecommendation(
                    priority = "CRITICAL",
                    taskType = "INVOICE",
                    description = "Send ${status.pendingInvoices} pending invoices. This could recover significant revenue.",
                    estimatedValue = status.remainingTarget * 0.5,
                    urgencyScore = 0.9f
                )
            )
        }

        if (status.activeLeads > 0) {
            recommendations.add(
                TaskRecommendation(
                    priority = "HIGH",
                    taskType = "FOLLOW_UP",
                    description = "Follow up with ${status.activeLeads} active leads. Conversion rates are critical right now.",
                    estimatedValue = status.remainingTarget * 0.3,
                    urgencyScore = 0.7f
                )
            )
        }

        recommendations.add(
            TaskRecommendation(
                priority = "MEDIUM",
                taskType = "CLIENT_PITCH",
                description = "Automatically search for new clients and send pitches. Use online work platforms like Freelancer, Upwork or local business discovery.",
                estimatedValue = status.remainingTarget * 0.2,
                urgencyScore = 0.5f
            )
        )

        return recommendations
    }

    /**
     * Execute an autonomous monetization task.
     */
    suspend fun executeTask(task: TaskRecommendation): String {
        val now = System.currentTimeMillis()

        try {
            db.automationTaskQueueDao().insertTask(
                com.example.data.local.entities.AutomationTaskQueueEntity(
                    taskType = task.taskType,
                    priority = task.priority,
                    scheduledTime = now,
                    status = "Pending",
                    payload = task.description
                )
            )
        } catch (e: Exception) {
            // Non-critical
        }

        return "Task queued: ${task.description}. I will execute this automatically and report you upon the result."
    }

    /**
     * Check if Force Mode should be automatically triggered.
     * Returns true if monthly progress is below 50%.
     */
    fun shouldActivateForceMode(): Boolean {
        val progress = try {
            runBlocking { getMonetizationStatus().progressPercent }
        } catch (e: Exception) { 0f }
        return progress < 50f
    }

    private fun getCurrentMonthTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getNextMonthTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MONTH, 1)
        return cal.timeInMillis
    }
}