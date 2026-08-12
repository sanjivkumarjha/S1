package com.example.security

import android.content.Context

data class LinkScanResult(
    val url: String,
    val domain: String,
    val isSecureHttps: Boolean,
    val isShortenedUrl: Boolean,
    val isLookalikeDomain: Boolean,
    val isSuspicious: Boolean,
    val riskScore: Int,
    val warningText: String
)

/**
 * Scans web links before opening for phishing, domain spoofing, and malicious redirects.
 */
class LinkThreatScanner(private val context: Context) {

    private val trustedDomains = listOf("google.com", "github.com", "youtube.com", "amazon.in", "wikipedia.org", "sbi.co.in", "hdfcbank.com", "icicibank.com")
    private val shortenedPrefixes = listOf("bit.ly", "tinyurl.com", "cutt.ly", "t.co", "rb.gy", "is.gd")
    private val highRiskTLDs = listOf(".xyz", ".top", ".club", ".online", ".site", ".tk", ".work", ".cc")

    fun scanUrl(urlStr: String): LinkScanResult {
        val cleanUrl = urlStr.trim()
        val lowerUrl = cleanUrl.lowercase()

        val isHttps = lowerUrl.startsWith("https://")
        val domain = extractDomain(cleanUrl)

        val isShortened = shortenedPrefixes.any { domain.contains(it) }
        val hasRiskTLD = highRiskTLDs.any { domain.endsWith(it) }
        val isLookalike = (domain.contains("paytm") || domain.contains("sbi") || domain.contains("amazon") || domain.contains("hdfc")) &&
                          !trustedDomains.any { domain.endsWith(it) }

        var riskScore = 0
        if (!isHttps) riskScore += 25
        if (isShortened) riskScore += 30
        if (hasRiskTLD) riskScore += 35
        if (isLookalike) riskScore += 50

        val isSuspicious = riskScore >= 50

        val warning = if (isSuspicious) {
            "Boss, यह link suspicious लग रहा है ($domain). इसे खोलने से पहले सावधानी रखें."
        } else {
            "Link verified safe."
        }

        return LinkScanResult(
            url = cleanUrl,
            domain = domain,
            isSecureHttps = isHttps,
            isShortenedUrl = isShortened,
            isLookalikeDomain = isLookalike,
            isSuspicious = isSuspicious,
            riskScore = riskScore,
            warningText = warning
        )
    }

    private fun extractDomain(url: String): String {
        val cleaned = url.replace("https://", "").replace("http://", "").split("/")[0]
        return cleaned.split(":")[0]
    }
}
