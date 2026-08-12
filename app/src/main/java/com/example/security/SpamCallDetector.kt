package com.example.security

import android.content.Context

enum class CallClassification {
    SAFE, UNKNOWN, MARKETING, SPAM, SCAM_SUSPECTED, HIGH_RISK
}

enum class SpamAction {
    BLOCK, SILENCE, WARN, ASK, ALLOW
}

data class SpamCallResult(
    val callerNumber: String,
    val callerIdentity: String,
    val classification: CallClassification,
    val riskScore: Int,
    val recommendedAction: SpamAction,
    val responseMessage: String
)

/**
 * Detects spam and scam calls analyzing available metadata, caller frequency, and databases.
 */
class SpamCallDetector(private val context: Context) {

    private val knownSpamPrefixes = listOf("+91140", "140", "+911800", "0804", "1800", "+919999", "022", "011")
    private val highRiskKeywords = listOf("lottery", "kbc", "bank verification", "loan offer", "crypto", "telemarketing")

    fun analyzeCall(callerNumber: String, callerIdentity: String = "Unknown Caller", configuredAction: SpamAction = SpamAction.BLOCK): SpamCallResult {
        val cleanNumber = callerNumber.replace(" ", "").replace("-", "")

        var classification = CallClassification.SAFE
        var riskScore = 10

        when {
            knownSpamPrefixes.any { cleanNumber.startsWith(it) } -> {
                classification = CallClassification.SPAM
                riskScore = 75
            }
            highRiskKeywords.any { callerIdentity.lowercase().contains(it) } -> {
                classification = CallClassification.SCAM_SUSPECTED
                riskScore = 90
            }
            cleanNumber.length < 10 || cleanNumber.startsWith("+1800") -> {
                classification = CallClassification.MARKETING
                riskScore = 60
            }
            callerIdentity.lowercase().contains("spam") || callerIdentity.lowercase().contains("suspect") -> {
                classification = CallClassification.HIGH_RISK
                riskScore = 95
            }
            callerIdentity == "Unknown Caller" -> {
                classification = CallClassification.UNKNOWN
                riskScore = 40
            }
        }

        val action = if (riskScore >= 70) configuredAction else SpamAction.ALLOW

        val responseMessage = when (action) {
            SpamAction.BLOCK -> "Boss, एक suspected spam call ($callerNumber) आया था. मैंने आपकी security settings के अनुसार उसे block कर दिया."
            SpamAction.SILENCE -> "Boss, a suspected marketing/spam call ($callerNumber) was silenced as per security rules."
            SpamAction.WARN -> "Boss, incoming call from $callerNumber ($callerIdentity) is flagged as ${classification.name}."
            else -> "Call from $callerNumber processed safely."
        }

        return SpamCallResult(
            callerNumber = callerNumber,
            callerIdentity = callerIdentity,
            classification = classification,
            riskScore = riskScore,
            recommendedAction = action,
            responseMessage = responseMessage
        )
    }
}
