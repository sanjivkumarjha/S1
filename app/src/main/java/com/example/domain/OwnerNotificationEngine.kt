package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase

/**
 * Proactive Owner Notification & Transparent Problem Sharing Engine v27.0
 *
 * FEATURES:
 * - Instant earnings alert as soon as payment is generated/verified
 * - Transparent issue reporting for blockers, API limitations, glitches
 * - Detailed amount, client, and effort involved reporting
 * - Priority-based notification routing
 */
class OwnerNotificationEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    data class EarningsAlert(
        val amount: Double = 0.0,
        val clientName: String = "",
        val serviceDescription: String = "",
        val paymentMethod: String = "UPI",
        val effortHours: Double = 0.0,
        val timestamp: Long = System.currentTimeMillis(),
        val message: String = ""
    )

    data class IssueReport(
        val severity: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH", "CRITICAL"
        val issueType: String = "", // "API_LIMIT", "BLOCKER", "GLITCH", "PROJECT_HURDLE"
        val description: String = "",
        val suggestedAction: String = "",
        val isResolved: Boolean = false,
        val timestamp: Long = System.currentTimeMillis(),
        val message: String = ""
    )

    /**
     * Generate instant earnings alert message.
     */
    fun generateEarningsAlert(
        amount: Double,
        clientName: String,
        serviceDescription: String,
        effortHours: Double = 0.0
    ): EarningsAlert {
        val msg = "🎉 नई पेमेंट प्राप्त हुई!\n\n" +
                "💰 Amount: ₹${String.format("%.2f", amount)}\n" +
                "👤 Client: $clientName\n" +
                "🛠️ Service: $serviceDescription\n" +
                "⏱️ Effort: ${String.format("%.1f", effortHours)} hours\n\n" +
                "संजीव सर, आपकी मेहनत रंग ला रही है! राधे-राधे! 🙏✨"

        return EarningsAlert(
            amount = amount,
            clientName = clientName,
            serviceDescription = serviceDescription,
            effortHours = effortHours,
            message = msg
        )
    }

    /**
     * Generate transparent issue report message.
     */
    fun generateIssueReport(
        severity: String,
        issueType: String,
        description: String,
        suggestedAction: String
    ): IssueReport {
        val severityEmoji = when (severity.uppercase()) {
            "CRITICAL" -> "🚨"
            "HIGH" -> "🔴"
            "MEDIUM" -> "🟡"
            else -> "🟢"
        }

        val msg = "$severityEmoji समस्या रिपोर्ट - $severity\n\n" +
                "प्रकार: $issueType\n" +
                "विवरण: $description\n\n" +
                "सुझाए गए समाधान: $suggestedAction\n\n" +
                "संजीव सर, कृपया इस समस्या का समाधान करने में मार्गदर्शन करें। " +
                "मैं पारदर्शिता के साथ आपको हर बाधा बताती रहूंगी। राधे-राधे! 🙏"

        return IssueReport(
            severity = severity,
            issueType = issueType,
            description = description,
            suggestedAction = suggestedAction,
            message = msg
        )
    }

    /**
     * Get daily earnings summary.
     */
    fun generateDailySummary(dailyEarnings: Double, monthlyTarget: Double): String {
        val progress = ((dailyEarnings / monthlyTarget) * 100.0).coerceIn(0.0, 100.0)
        return "📊 दैनिक एनालिटिक्स सारांश:\n" +
                "• आज की कमाई: ₹${String.format("%.0f", dailyEarnings)}\n" +
                "• मासिक लक्ष्य: ₹${String.format("%.0f", monthlyTarget)}\n" +
                "• प्रगति: ${String.format("%.1f", progress)}%\n\n" +
                when {
                    progress >= 100f -> "🎉 लक्ष्य पूरा! बहुत-बहुत बधाई संजीव सर!"
                    progress >= 75f -> "💪 लगभग तैयार! और मेहनत करते रहें!"
                    progress >= 50f -> "👍 अच्छी प्रगति! जारी रखें!"
                    progress < 25f -> "⚡ आज की रणनीति बदलनी होगी। चलिए और प्रयास करते हैं!"
                    else -> "📈 धीरे-धीरे आगे बढ़ रहे हैं। हार नहीं मानेंगे!"
                }
    }

    /**
     * Get the notification priority based on earnings/issue.
     */
    fun getNotificationPriority(earnings: Double = 0.0, isIssue: Boolean = false): String {
        return when {
            isIssue -> "HIGH"
            earnings >= 10000 -> "HIGH"
            earnings >= 5000 -> "MEDIUM"
            earnings > 0 -> "NORMAL"
            else -> "LOW"
        }
    }
}