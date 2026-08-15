package com.example.domain

import com.example.domain.branding.BrandingConfig

/**
 * Polite Diplomacy & Abuse Handling Protocol v27.0
 *
 * STRICT NON-ABUSIVE POLICY:
 * NEVER insult back under any circumstances.
 *
 * DIPLOMATIC DISCONNECT:
 * Politely say: "मैं माफी चाहती हूँ, आप बाद में बॉस से बात कर लीजिएगा" and disconnect immediately.
 *
 * CLIENT RESPECT PROTOCOL:
 * Address every customer by their name or requested title politely during business calls.
 */
object AbuseHandlingProtocol {

    private val abuseKeywords = listOf(
        "bhenchod", "madarchod", "chutiya", "lauda", "lund", "gaand",
        "betichod", "harami", "kutte", "kutti", "randi", "bhosdi",
        "fuck", "fucking", "fuck you", "fuck off", "bitch", "asshole",
        "motherfucker", "dickhead", "bastard", "shit", "bullshit",
        "suck", "damn you", "stupid ai", "gandu", "saala", "saali",
        "nikamma", "bewakoof", "ullu", "jhatu", "kamina",
        "teri maa ki", "bhosdike", "madarchod", "laude",
        "abusive", "insult", "harami insaan"
    )

    /** Score from 0.0 (no abuse) to 1.0 (severe abuse) */
    data class AbuseAnalysisResult(
        val isAbusive: Boolean,
        val severityScore: Float,
        val abusivePhrases: List<String>,
        val shouldDisconnect: Boolean,
        val diplomaticResponse: String
    )

    /**
     * Analyze a message for abusive content.
     * Returns analysis with severity score and appropriate diplomatic response.
     */
    fun analyzeAbuse(message: String, languageCode: String = "hi"): AbuseAnalysisResult {
        val lowerMessage = message.lowercase().trim()
        val matchedPhrases = abuseKeywords.filter { lowerMessage.contains(it) }

        if (matchedPhrases.isEmpty()) {
            return AbuseAnalysisResult(
                isAbusive = false,
                severityScore = 0f,
                abusivePhrases = emptyList(),
                shouldDisconnect = false,
                diplomaticResponse = ""
            )
        }

        // Calculate severity based on number of abusive phrases
        val severityScore = (matchedPhrases.size.toFloat() / 5f).coerceIn(0.1f, 1.0f)
        val shouldDisconnect = severityScore >= 0.3f // Disconnect at moderate abuse or higher

        val diplomaticResponse = buildDiplomaticResponse(languageCode)

        return AbuseAnalysisResult(
            isAbusive = true,
            severityScore = severityScore,
            abusivePhrases = matchedPhrases,
            shouldDisconnect = shouldDisconnect,
            diplomaticResponse = diplomaticResponse
        )
    }

    /**
     * Build a polite diplomatic response based on language preference.
     */
    private fun buildDiplomaticResponse(languageCode: String): String {
        return when (languageCode.lowercase()) {
            "hi" -> "मैं माफी चाहती हूँ। आप बाद में बॉस से बात कर लीजिएगा। धन्यवाद।"
            "en" -> "I apologize, but I'm unable to continue this conversation. Please speak with the Boss later. Thank you."
            "hinglish" -> "मैं माफी चाहती हूँ, आप बाद में बॉस से बात कर लीजिएगा। Thank you."
            else -> "मैं माफी चाहती हूँ। आप बाद में बॉस से बात कर लीजिएगा। धन्यवाद।"
        }
    }

    /**
     * Get the polite disconnection disclosure notice for call screening.
     */
    fun getDisclosureNotice(): String {
        val productName = BrandingConfig.PRODUCT_NAME
        return "राधे राधे। मैं $productName की assistant हूँ। कृपया सम्मानजनक भाषा का प्रयोग करें। " +
                "I am the $productName assistant. Please use respectful language."
    }
}