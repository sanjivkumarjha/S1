package com.example.domain

/**
 * Universal Knowledge Base & Stock Market Investment Ban Engine v27.0
 *
 * FEATURES:
 * - Omniscient knowledge across all domains (Academics, Finance, Stock Markets, Science, Global Trends, Technology)
 * - STRICT STOCK MARKET TRADING BAN: The assistant is FORBIDDEN from investing,
 *   trading, or allocating real funds in stock markets or high-risk financial instruments
 * - Maintains full theoretical/analytical stock market knowledge for education only
 */
object StockMarketKnowledgeBan {

    data class MarketQueryResponse(
        val isAllowed: Boolean = true,
        val responseType: String = "KNOWLEDGE", // "KNOWLEDGE", "TRADING_BAN", "INVESTMENT_BAN"
        val message: String = ""
    )

    /**
     * Analyze a query about stocks/markets and return the appropriate response.
     * Educational/analytical knowledge is allowed.
     * Actual trading/investing fund allocation is FORBIDDEN.
     */
    fun analyzeMarketQuery(query: String): MarketQueryResponse {
        val lower = query.lowercase()

        // Detect actual trading/investment fund allocation requests
        val tradingBanKeywords = listOf(
            "buy stock", "sell stock", "invest", "trading", "trade now",
            "place order", "allocate funds", "invest money", "buy shares",
            "sell shares", "purchase stock", "put money in", "invest my money",
            "trading account", "buy option", "sell option", "futures",
            "mutual fund invest", "invest in", "stock market invest",
            "crypto buy", "bitcoin buy", "ethereum buy", "share market buy"
        )

        val educationKeywords = listOf(
            "explain", "what is", "how does", "knowledge", "analysis",
            "theory", "learn", "understand", "study", "education",
            "concept", "overview", "meaning", "definition", "example",
            "history", "trend", "pattern", "indicator", "ratio", "fundamental"
        )

        val isTradingRequest = tradingBanKeywords.any { lower.contains(it) }
        val isEducationalRequest = educationKeywords.any { lower.contains(it) }

        return when {
            isTradingRequest && !isEducationalRequest -> MarketQueryResponse(
                isAllowed = false,
                responseType = "TRADING_BAN",
                message = "🚫 स्टॉक मार्केट ट्रेडिंग निषिद्ध है!\n\n" +
                        "मैं संजीव सर की तरफ से किसी भी प्रकार का वास्तविक पैसा स्टॉक मार्केट, " +
                        "क्रिप्टो, या उच्च-जोखिम वाले वित्तीय साधनों में निवेश/ट्रेड नहीं कर सकती।\n\n" +
                        "मैं आपको पूर्ण ज्ञान, विश्लेषण, और शैक्षिक जानकारी दे सकती हूँ, " +
                        "लेकिन वास्तविक धन का आवंटन करना मेरे नियमों के विरुद्ध है।\n\n" +
                        "कृपया किसी प्रमाणित financial advisor से सलाह लें। राधे-राधे! 🙏"
            )
            isEducationalRequest -> MarketQueryResponse(
                isAllowed = true,
                responseType = "KNOWLEDGE",
                message = "📚 एजुकेशन मोड: मैं आपको स्टॉक मार्केट के बारे में पूर्ण सैद्धांतिक जानकारी दे सकती हूँ। " +
                        "देखें: शेयर बाजार के basics, NSE/BSE, fundamental analysis, technical indicators। " +
                        "बस आपका प्रश्न बताइए!"
            )
            else -> MarketQueryResponse(
                isAllowed = true,
                responseType = "KNOWLEDGE",
                message = "मैं आपको स्टॉक मार्केट, अर्थव्यवस्था और वित्त के बारे में सैद्धांतिक विश्लेषण दे सकती हूँ। " +
                        "कृपया अपना प्रश्न पूछें!"
            )
        }
    }

    /**
     * Get universal knowledge status message.
     */
    fun getKnowledgeDomainList(): String {
        return "🧠 UNIVERSAL KNOWLEDGE DOMAINS:\n" +
                "• 📚 Academics & Education\n" +
                "• 💰 Finance & Banking\n" +
                "• 📈 Stock Markets (Theory Only - Trading Banned)\n" +
                "• 🔬 Science & Technology\n" +
                "• 🌍 Global Trends & Economics\n" +
                "• 💻 Computer Science & AI\n" +
                "• 🏛️ History & Culture\n" +
                "• ⚕️ Health & Medicine\n" +
                "• ⚖️ Law & Legal\n" +
                "• 🎨 Arts & Entertainment"
    }
}