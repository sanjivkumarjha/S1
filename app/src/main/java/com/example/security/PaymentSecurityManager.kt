package com.example.security

import android.content.Context

data class PaymentThreatResult(
    val paymentType: String, // "UPI", "CARD", "WALLET", "WIRE", "QR", "LINK"
    val recipient: String,
    val amount: String,
    val threatType: String, // "FAKE_REFUND", "URGENT_PRESSURE", "SUSPICIOUS_UPI_HANDLE", "ACCOUNT_VERIFICATION", "OTP_PHISHING", "SAFE"
    val isSuspicious: Boolean,
    val riskScore: Int,
    val warningText: String,
    val requiresOwnerAuthentication: Boolean
)

/**
 * Payment Fraud Protection Manager protecting UPI, Card, Wallet, and QR Payment workflows.
 */
class PaymentSecurityManager(private val context: Context) {

    fun evaluatePaymentRequest(
        recipient: String,
        amount: String,
        paymentType: String = "UPI",
        sourceDetails: String = ""
    ): PaymentThreatResult {
        val lowerRecipient = recipient.lowercase()
        val lowerSource = sourceDetails.lowercase()

        var threat = "SAFE"
        var riskScore = 10
        var isSuspicious = false

        when {
            lowerSource.contains("refund") || lowerRecipient.contains("refund") -> {
                threat = "FAKE_REFUND"
                riskScore = 85
                isSuspicious = true
            }
            lowerSource.contains("urgent") || lowerSource.contains("immediately") -> {
                threat = "URGENT_PRESSURE"
                riskScore = 80
                isSuspicious = true
            }
            lowerSource.contains("otp") || lowerSource.contains("pin") -> {
                threat = "OTP_PHISHING"
                riskScore = 95
                isSuspicious = true
            }
            lowerSource.contains("kyc") || lowerRecipient.contains("verification") -> {
                threat = "ACCOUNT_VERIFICATION"
                riskScore = 90
                isSuspicious = true
            }
            !lowerRecipient.endsWith("@upi") && paymentType == "UPI" && lowerRecipient.contains("pay") -> {
                threat = "SUSPICIOUS_UPI_HANDLE"
                riskScore = 70
                isSuspicious = true
            }
        }

        val formattedAmount = if (amount.isBlank()) "Unknown" else amount

        val warning = if (isSuspicious) {
            "Boss, यह payment request suspicious है. Amount ₹$formattedAmount है और source ($recipient) trusted नहीं लग रहा. Payment करने से पहले verify कर लें."
        } else {
            "Payment request to $recipient for ₹$formattedAmount passed safety checks."
        }

        return PaymentThreatResult(
            paymentType = paymentType,
            recipient = recipient,
            amount = formattedAmount,
            threatType = threat,
            isSuspicious = isSuspicious,
            riskScore = riskScore,
            warningText = warning,
            requiresOwnerAuthentication = isSuspicious || riskScore > 50
        )
    }

    fun verifyOtpSafety(otpMessage: String): String {
        return "Boss, यह OTP है. इसे किसी के साथ share मत करना."
    }
}
