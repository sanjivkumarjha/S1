package com.example.security

import android.content.Context

data class MessageThreatResult(
    val messageText: String,
    val sender: String,
    val threatCategory: String, // "FAKE_BANK", "FAKE_DELIVERY", "KYC_SCAM", "LOTTERY", "PHISHING_LINK", "URGENT_PAYMENT", "IMPERSONATION", "SAFE"
    val isSuspicious: Boolean,
    val riskScore: Int,
    val extractedLinks: List<String>,
    val warningText: String
)

/**
 * Analyzes incoming SMS, WhatsApp, and email messages for fraud, phishing, and scam patterns.
 */
class SpamMessageDetector(private val context: Context) {

    private val suspiciousKeywords = mapOf(
        "KYC_SCAM" to listOf("kyc update", "account suspended", "pancard blocked", "electricity bill unpaid", "power cutoff"),
        "FAKE_BANK" to listOf("bank account blocked", "debit card expire", "update pan card", "netbanking expired"),
        "LOTTERY" to listOf("won lottery", "kbc lucky draw", "prize money", "claim reward points"),
        "URGENT_PAYMENT" to listOf("urgent transfer", "send money now", "pay immediately", "refund pending"),
        "PHISHING_LINK" to listOf("bit.ly/", "tinyurl.com/", ".xyz/", "click here to claim", "login immediately")
    )

    fun analyzeMessage(sender: String, text: String): MessageThreatResult {
        val lowerText = text.lowercase()
        var matchedCategory = "SAFE"
        var riskScore = 0
        var isSuspicious = false

        for ((category, keywords) in suspiciousKeywords) {
            if (keywords.any { lowerText.contains(it) }) {
                matchedCategory = category
                isSuspicious = true
                riskScore = when (category) {
                    "KYC_SCAM", "FAKE_BANK" -> 90
                    "PHISHING_LINK" -> 85
                    "LOTTERY" -> 80
                    "URGENT_PAYMENT" -> 75
                    else -> 60
                }
                break
            }
        }

        val urlRegex = Regex("""https?://[^\s]+""")
        val links = urlRegex.findAll(text).map { it.value }.toList()

        if (links.isNotEmpty() && matchedCategory == "SAFE") {
            if (links.any { it.contains(".xyz") || it.contains("bit.ly") || it.contains("free-") }) {
                matchedCategory = "PHISHING_LINK"
                isSuspicious = true
                riskScore = 80
            }
        }

        val warning = if (isSuspicious) {
            "Boss, इस message में suspected $matchedCategory detected हुआ है ($sender से). कृपया किसी link पर click न करें और OTP share न करें."
        } else {
            "Message appears safe."
        }

        return MessageThreatResult(
            messageText = text,
            sender = sender,
            threatCategory = matchedCategory,
            isSuspicious = isSuspicious,
            riskScore = riskScore,
            extractedLinks = links,
            warningText = warning
        )
    }
}
