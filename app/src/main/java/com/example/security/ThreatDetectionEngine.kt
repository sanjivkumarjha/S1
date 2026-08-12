package com.example.security

import android.content.Context

enum class ThreatSource {
    CALL, SMS, WHATSAPP, EMAIL, LINK, PAYMENT, QR
}

data class UnifiedThreatResult(
    val source: ThreatSource,
    val identifier: String,
    val isThreat: Boolean,
    val threatCategory: String,
    val riskScore: Int,
    val actionTakenOrRecommended: String,
    val ownerMessage: String
)

/**
 * Universal Spam & Scam Threat Detection Engine coordinating calls, SMS, WhatsApp, emails, links, payments, and QR codes.
 */
class ThreatDetectionEngine(private val context: Context) {

    val callDetector = SpamCallDetector(context)
    val messageDetector = SpamMessageDetector(context)
    val linkScanner = LinkThreatScanner(context)
    val paymentManager = PaymentSecurityManager(context)
    val qrScanner = QRCodeSecurityScanner(context)
    val fraudIntelligence = FraudIntelligenceEngine(context)
    val incidentReportManager = SecurityIncidentReportManager(context)

    suspend fun evaluateInput(textOrQuery: String): UnifiedThreatResult {
        val trimmed = textOrQuery.trim()
        val lower = trimmed.lowercase()

        return when {
            // Payment / UPI evaluation
            lower.contains("pay") || lower.contains("upi") || lower.contains("₹") || lower.contains("rupees") -> {
                val paymentRes = paymentManager.evaluatePaymentRequest("Receiver", "1000", "UPI", trimmed)
                if (paymentRes.isSuspicious) {
                    incidentReportManager.logIncident("Payment", paymentRes.threatType, paymentRes.riskScore, "SCAM_SUSPECTED", "WARNED", trimmed)
                }
                UnifiedThreatResult(
                    source = ThreatSource.PAYMENT,
                    identifier = paymentRes.recipient,
                    isThreat = paymentRes.isSuspicious,
                    threatCategory = paymentRes.threatType,
                    riskScore = paymentRes.riskScore,
                    actionTakenOrRecommended = "REQUIRE_AUTHENTICATION",
                    ownerMessage = paymentRes.warningText
                )
            }
            // URL / Link evaluation
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> {
                val linkRes = linkScanner.scanUrl(trimmed)
                if (linkRes.isSuspicious) {
                    incidentReportManager.logIncident("URL", "PHISHING_LINK", linkRes.riskScore, "HIGH_RISK", "WARNED", trimmed)
                }
                UnifiedThreatResult(
                    source = ThreatSource.LINK,
                    identifier = linkRes.domain,
                    isThreat = linkRes.isSuspicious,
                    threatCategory = if (linkRes.isLookalikeDomain) "LOOKALIKE_PHISHING" else "SUSPICIOUS_LINK",
                    riskScore = linkRes.riskScore,
                    actionTakenOrRecommended = if (linkRes.riskScore > 80) "BLOCK" else "WARN",
                    ownerMessage = linkRes.warningText
                )
            }
            // Call evaluation
            trimmed.startsWith("+") || trimmed.matches(Regex("""^[0-9\-\s]{10,15}$""")) -> {
                val callRes = callDetector.analyzeCall(trimmed)
                if (callRes.riskScore > 60) {
                    incidentReportManager.logIncident("Call", callRes.classification.name, callRes.riskScore, callRes.classification.name, callRes.recommendedAction.name, trimmed)
                }
                UnifiedThreatResult(
                    source = ThreatSource.CALL,
                    identifier = callRes.callerNumber,
                    isThreat = callRes.riskScore > 60,
                    threatCategory = callRes.classification.name,
                    riskScore = callRes.riskScore,
                    actionTakenOrRecommended = callRes.recommendedAction.name,
                    ownerMessage = callRes.responseMessage
                )
            }
            // Message / SMS evaluation
            else -> {
                val msgRes = messageDetector.analyzeMessage("Incoming", trimmed)
                if (msgRes.isSuspicious) {
                    incidentReportManager.logIncident("SMS", msgRes.threatCategory, msgRes.riskScore, "SCAM_SUSPECTED", "WARNED", trimmed)
                }
                UnifiedThreatResult(
                    source = ThreatSource.SMS,
                    identifier = "Message",
                    isThreat = msgRes.isSuspicious,
                    threatCategory = msgRes.threatCategory,
                    riskScore = msgRes.riskScore,
                    actionTakenOrRecommended = if (msgRes.riskScore > 80) "SILENCE" else "WARN",
                    ownerMessage = msgRes.warningText
                )
            }
        }
    }
}
