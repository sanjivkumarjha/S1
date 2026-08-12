package com.example.communication

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CallSummaryEntity

enum class SupportedLanguage(val code: String, val localeTag: String, val isDialect: Boolean = false) {
    HINDI("hi", "hi-IN"),
    ENGLISH_IN("en", "en-IN"),
    GUJARATI("gu", "gu-IN"),
    MARATHI("mr", "mr-IN"),
    TAMIL("ta", "ta-IN"),
    TELUGU("te", "te-IN"),
    BENGALI("bn", "bn-IN"),
    PUNJABI("pa", "pa-IN"),
    KANNADA("kn", "kn-IN"),
    MALAYALAM("ml", "ml-IN"),
    ODIA("or", "or-IN"),
    MAITHILI("mai", "hi-IN", true),
    BHOJPURI("bho", "hi-IN", true),
    UNKNOWN("en", "en-IN")
}

data class RealtimeCallSession(
    val callerName: String,
    val callerPhone: String,
    val detectedLanguage: SupportedLanguage,
    val initialGreeting: String,
    val disclosureNotice: String
)

/**
 * Real-Time Multilingual Call Assistant supporting Hindi, English, Gujarati, Marathi, Tamil,
 * Telugu, Bengali, Punjabi, Kannada, Malayalam, Odia, Maithili, Bhojpuri.
 */
class RealTimeLanguageAssistant(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    val recordingDisclosureNotice = "इस call को owner-configured assistant feature के अनुसार process किया जा सकता है."

    fun detectLanguage(spokenText: String): SupportedLanguage {
        val lower = spokenText.lowercase()
        return when {
            // Regional Dialects
            lower.contains("अहाँ") || lower.contains("कतय") || lower.contains("अछि") || lower.contains("मैथिली") -> SupportedLanguage.MAITHILI
            lower.contains("रऊआ") || lower.contains("का हो") || lower.contains("भोजपुरी") || lower.contains("बा") -> SupportedLanguage.BHOJPURI
            // Standard Regional Languages
            lower.contains("नमस्ते") || lower.contains("बात") || lower.contains("काम") || lower.contains("कौन") -> SupportedLanguage.HINDI
            lower.contains("வணக்கம்") || lower.contains("பேச") || lower.contains("நன்றி") -> SupportedLanguage.TAMIL
            lower.contains("నమస్కారం") || lower.contains("ఏంటి") || lower.contains("చేయాలి") -> SupportedLanguage.TELUGU
            lower.contains("કેમ છો") || lower.contains("શાંતી") || lower.contains("તમે") -> SupportedLanguage.GUJARATI
            lower.contains("नमस्कार") || lower.contains("काय") || lower.contains("कसा") -> SupportedLanguage.MARATHI
            lower.contains("নমস্কার") || lower.contains("কেমন") || lower.contains("ধন্যবাদ") -> SupportedLanguage.BENGALI
            lower.contains("ਸਤਿ ਸ਼੍ਰੀ ਅਕਾਲ") || lower.contains("ਕਿਵੇਂ") -> SupportedLanguage.PUNJABI
            lower.contains("ನಮಸ್ಕಾರ") || lower.contains("ಹೇಗಿದ್ದೀರ") -> SupportedLanguage.KANNADA
            lower.contains("നമസ്കാരം") || lower.contains("സുഖമാണോ") -> SupportedLanguage.MALAYALAM
            lower.contains("ନମସ୍କାର") || lower.contains("କେମିତି") -> SupportedLanguage.ODIA
            lower.contains("hello") || lower.contains("speak") || lower.contains("call") || lower.contains("want") -> SupportedLanguage.ENGLISH_IN
            else -> SupportedLanguage.HINDI
        }
    }

    fun generateInitialCallGreeting(callerName: String, detectedLang: SupportedLanguage): String {
        return when (detectedLang) {
            SupportedLanguage.HINDI -> "राधे राधे। मैं Snaper Technology की personal assistant हूँ. आप किससे बात करना चाहते हैं?"
            SupportedLanguage.GUJARATI -> "રાધે રાધે! હું Snaper Technology ની AI Assistant છું. આપ કોની સાથે વાત કરવા માંગો છો?"
            SupportedLanguage.MARATHI -> "राधे राधे! मी Snaper Technology ची AI सहाय्यक आहे. आपल्याला कोणाशी बोलायचे आहे?"
            SupportedLanguage.TAMIL -> "ராதே ராதே! நான் Snaper Technology இன் AI உதவியாளர். நீங்கள் யாரிடம் பேச வேண்டும்?"
            SupportedLanguage.TELUGU -> "రాధే రాధే! నేను Snaper Technology AI అసిస్టెంట్‌ని. మీరు ఎవరితో మాట్లాడాలనుకుంటున్నారు?"
            SupportedLanguage.BENGALI -> "রাধে রাধে! আমি Snaper Technology-র AI অ্যাসিস্ট্যান্ট। আপনি কার সাথে কথা বলতে চান?"
            SupportedLanguage.PUNJABI -> "ਰਾਧੇ ਰਾਧੇ! ਮੈਂ Snaper Technology ਦੀ AI ਅਸਿਸਟੈਂਟ ਹਾਂ। ਤੁਸੀਂ ਕਿਸ ਨਾਲ ਗੱਲ ਕਰਨਾ ਚਾਹੁੰਦੇ ਹੋ?"
            SupportedLanguage.MAITHILI -> "राधे राधे! हम Snaper Technology के AI पर्सनल असिस्टेंट छी। अहाँ केकरा सं बात करए चाहैत छी?"
            SupportedLanguage.BHOJPURI -> "राधे राधे! हम Snaper Technology के AI पर्सनल असिस्टेंट बानी। रऊआ केकरा से बात कइल चाहत बानी?"
            else -> "Radhe Radhe! I am the personal assistant for Snaper Technology. How may I direct your call?"
        }
    }

    fun generateOwnerUnavailableMessage(detectedLang: SupportedLanguage, ownerTitle: String = "Boss"): String {
        return when (detectedLang) {
            SupportedLanguage.HINDI -> "$ownerTitle अभी available नहीं हैं. मैं message या callback reminder दे सकती हूँ."
            SupportedLanguage.GUJARATI -> "$ownerTitle અત્યારે ઉપલબ્ધ નથી. હું મેસેજ અથવા કૉલબેક રિમાઇન્ડર આપી શકું છું."
            SupportedLanguage.MARATHI -> "$ownerTitle सध्या उपलब्ध नाहीत. मी संदेश किंवा कॉलस्क्रीन नोंदवू शकते."
            else -> "$ownerTitle is currently unavailable. I can take a message or schedule a callback reminder."
        }
    }

    suspend fun saveCallSummary(
        callerName: String,
        callerPhone: String,
        purpose: String,
        importantPoints: String,
        requestedAction: String,
        followUpDate: String = "Today"
    ): Long {
        val summary = CallSummaryEntity(
            callerName = callerName,
            callerPhone = callerPhone,
            purpose = purpose,
            importantPoints = importantPoints,
            requestedAction = requestedAction,
            followUpDate = followUpDate
        )
        return db.callSummaryDao().insertCallSummary(summary)
    }
}
