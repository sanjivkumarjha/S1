package com.example.security

import android.content.Context

data class QRScanResult(
    val rawContent: String,
    val destinationType: String, // "UPI_PAYMENT", "URL", "WIFI_CONFIG", "TEXT", "UNKNOWN"
    val recipientOrTarget: String,
    val amountIfAny: String?,
    val isSuspicious: Boolean,
    val warningText: String
)

/**
 * Decodes, identifies, and scans QR codes before taking action.
 */
class QRCodeSecurityScanner(private val context: Context) {

    private val linkScanner = LinkThreatScanner(context)

    fun decodeAndAnalyzeQR(qrPayload: String): QRScanResult {
        val trimmed = qrPayload.trim()

        return when {
            trimmed.startsWith("upi://pay") -> {
                val params = parseQueryParams(trimmed)
                val recipient = params["pa"] ?: "Unknown UPI ID"
                val amount = params["am"]
                val note = params["tn"] ?: ""

                val isSuspicious = note.lowercase().contains("refund") || recipient.lowercase().contains("lottery")

                val warning = "यह QR code payment request ($recipient${if (amount != null) " - ₹$amount" else ""}) खोल रहा है. Receiver details verify कर लें."

                QRScanResult(
                    rawContent = trimmed,
                    destinationType = "UPI_PAYMENT",
                    recipientOrTarget = recipient,
                    amountIfAny = amount,
                    isSuspicious = isSuspicious,
                    warningText = warning
                )
            }
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> {
                val scan = linkScanner.scanUrl(trimmed)
                QRScanResult(
                    rawContent = trimmed,
                    destinationType = "URL",
                    recipientOrTarget = scan.domain,
                    amountIfAny = null,
                    isSuspicious = scan.isSuspicious,
                    warningText = scan.warningText
                )
            }
            trimmed.startsWith("WIFI:") -> {
                QRScanResult(
                    rawContent = trimmed,
                    destinationType = "WIFI_CONFIG",
                    recipientOrTarget = "Wi-Fi Network Config",
                    amountIfAny = null,
                    isSuspicious = false,
                    warningText = "QR Code contains Wi-Fi network configuration."
                )
            }
            else -> {
                QRScanResult(
                    rawContent = trimmed,
                    destinationType = "TEXT",
                    recipientOrTarget = trimmed.take(30),
                    amountIfAny = null,
                    isSuspicious = false,
                    warningText = "Decoded plain text QR payload."
                )
            }
        }
    }

    private fun parseQueryParams(url: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val query = url.substringAfter("?", "")
        if (query.isNotEmpty()) {
            query.split("&").forEach { pair ->
                val parts = pair.split("=")
                if (parts.size == 2) {
                    map[parts[0]] = parts[1]
                }
            }
        }
        return map
    }
}
