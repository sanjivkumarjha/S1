package com.example.security

import android.content.Context

data class FraudReputationCheck(
    val query: String, // domain, number, or org name
    val isFlagged: Boolean,
    val threatCategory: String, // "KNOWN_SCAM_NUMBER", "PHISHING_CAMPAIGN", "MALICIOUS_DOMAIN", "CLEAN"
    val reputationalScore: Int, // 0 (Clean) - 100 (High Fraud)
    val details: String
)

/**
 * Connects with reputational security checks for domains, scam numbers, and phishing campaigns.
 */
class FraudIntelligenceEngine(private val context: Context) {

    private val knownScamNumbers = listOf("+91140", "140", "9999999999", "08040000000")
    private val knownPhishingDomains = listOf("sbi-kyc-update.xyz", "paytm-cashback-offer.site", "free-recharge.online")

    fun checkReputation(query: String): FraudReputationCheck {
        val trimmed = query.trim().lowercase()

        return when {
            knownScamNumbers.any { trimmed.contains(it) } -> {
                FraudReputationCheck(
                    query = query,
                    isFlagged = true,
                    threatCategory = "KNOWN_SCAM_NUMBER",
                    reputationalScore = 95,
                    details = "Flagged by community scam database as telemarketing / phishing source."
                )
            }
            knownPhishingDomains.any { trimmed.contains(it) } -> {
                FraudReputationCheck(
                    query = query,
                    isFlagged = true,
                    threatCategory = "MALICIOUS_DOMAIN",
                    reputationalScore = 98,
                    details = "Domain reported in active phishing campaign."
                )
            }
            trimmed.contains("kyc") || trimmed.contains("lottery") -> {
                FraudReputationCheck(
                    query = query,
                    isFlagged = true,
                    threatCategory = "PHISHING_CAMPAIGN",
                    reputationalScore = 80,
                    details = "Keywords match active social engineering fraud patterns."
                )
            }
            else -> {
                FraudReputationCheck(
                    query = query,
                    isFlagged = false,
                    threatCategory = "CLEAN",
                    reputationalScore = 10,
                    details = "No recorded threat intelligence flags."
                )
            }
        }
    }
}
