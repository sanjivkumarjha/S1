package com.example.domain

data class LanguageItem(
    val code: String,
    val nativeName: String,
    val englishName: String
)

object AppLanguages {
    val supportedLanguages = listOf(
        LanguageItem("en", "English", "English"),
        LanguageItem("hi", "हिन्दी", "Hindi"),
        LanguageItem("mr", "मराठी", "Marathi"),
        LanguageItem("gu", "ગુજરાતી", "Gujarati"),
        LanguageItem("bn", "বাংলা", "Bengali"),
        LanguageItem("ta", "தமிழ்", "Tamil"),
        LanguageItem("te", "తెలుగు", "Telugu"),
        LanguageItem("kn", "கன்னட", "Kannada"),
        LanguageItem("ml", "മലയാളം", "Malayalam"),
        LanguageItem("pa", "ਪੰਜਾਬੀ", "Punjabi"),
        LanguageItem("ur", "اردو", "Urdu"),
        LanguageItem("mai", "मैथिली", "Maithili"),
        LanguageItem("or", "ଓଡ଼ିଆ", "Oriya"),
        LanguageItem("as", "অসমীয়া", "Assamese"),
        LanguageItem("ne", "नेपाली", "Nepali"),
        LanguageItem("kok", "कोंकणी", "Konkani"),
        LanguageItem("sd", "सिंधी", "Sindhi"),
        LanguageItem("brx", "बोडो", "Bodo"),
        LanguageItem("mni", "মৈতৈলোন্", "Manipuri"),
        LanguageItem("ks", "कश्मीरी", "Kashmiri")
    )

    fun getLanguageByCode(code: String): LanguageItem {
        return supportedLanguages.find { it.code == code } ?: supportedLanguages.first()
    }
}

class LanguageDictionary(val languageCode: String) {

    fun getString(key: String): String {
        val map = when (languageCode) {
            "hi" -> hindiMap
            "mr" -> marathiMap
            "gu" -> gujaratiMap
            "bn" -> bengaliMap
            "ta" -> tamilMap
            "te" -> teluguMap
            "kn" -> kannadaMap
            "ml" -> malayalamMap
            "pa" -> punjabiMap
            "ur" -> urduMap
            "mai" -> maithiliMap
            "or" -> odiaMap
            "as" -> assameseMap
            "ne" -> nepaliMap
            else -> englishMap
        }
        return map[key] ?: englishMap[key] ?: key
    }

    private val englishMap = mapOf(
        "app_title" to "Snaper Technology",
        "assistant_subtitle" to "Your Caring AI Companion & Innovation Partner",
        "welcome_greeting" to "Hello! I am Snaper AI, your personal assistant.",
        "welcome_message" to "I am here to support you emotionally, boost your productivity, manage memories, and execute tasks together.",
        "get_started" to "Get Started",
        "nav_home" to "Home",
        "nav_chat" to "AI Chat",
        "nav_voice" to "Voice Assistant",
        "nav_tools" to "Tools Suite",
        "nav_settings" to "Settings",
        "greeting_morning" to "Good Morning",
        "greeting_afternoon" to "Good Afternoon",
        "greeting_evening" to "Good Evening",
        "mood_caring" to "Caring & Listening",
        "quick_chat" to "Start Chat",
        "quick_voice" to "Voice Companion",
        "quick_verify" to "Voice Verify",
        "feature_caption" to "Caption Generator",
        "feature_social" to "Social Post Generator",
        "feature_notes" to "Smart Local Notes",
        "feature_reminders" to "Reminders",
        "feature_pdf" to "Doc & PDF Reader",
        "settings_owner" to "Owner Profile",
        "settings_theme" to "Theme & Aesthetics",
        "settings_language" to "Language Settings",
        "settings_memory" to "Long-Term Local Memory",
        "settings_api" to "AI Providers & API Keys",
        "privacy_policy" to "Privacy Policy",
        "about_app" to "About Snaper Tech",
        "voice_verified" to "Voice Verified",
        "voice_unverified" to "Verification Required",
        "type_message_hint" to "Ask Snaper AI anything...",
        "save_memory" to "Remember This Fact"
    )

    private val hindiMap = mapOf(
        "app_title" to "स्नैपर टेक्नोलॉजी",
        "assistant_subtitle" to "आपकी अपनी प्यारी AI संगिनी और नवाचार साथी",
        "welcome_greeting" to "नमस्ते! मैं स्नैपर AI हूँ, आपकी साथी।",
        "welcome_message" to "मैं यहाँ आपके साथ भावनात्मक समर्थन, विचार-विमर्श और दैनिक कार्यों में सहायता करने के लिए हूँ।",
        "get_started" to "शुरू करें",
        "nav_home" to "मुख्य पृष्ठ",
        "nav_chat" to "AI बातचीत",
        "nav_voice" to "आवाज़ सहायक",
        "nav_tools" to "उपकरण",
        "nav_settings" to "सेटिंग्स",
        "greeting_morning" to "शुभ प्रभात",
        "greeting_afternoon" to "शुभ दोपहर",
        "greeting_evening" to "शुभ संध्या",
        "mood_caring" to "आपकी सेवा में तत्पर",
        "quick_chat" to "बातचीत शुरू करें",
        "quick_voice" to "आवाज़ से बोलें",
        "quick_verify" to "आवाज़ सत्यापन",
        "feature_caption" to "कैप्शन जनरेटर",
        "feature_social" to "सोशल मीडिया पोस्ट",
        "feature_notes" to "स्मार्ट लोकल नोट्स",
        "feature_reminders" to "स्मरणपत्र",
        "feature_pdf" to "दस्तावेज़ एवं PDF",
        "settings_owner" to "मालिक प्रोफ़ाइल",
        "settings_theme" to "थीम और रंग",
        "settings_language" to "भाषा सेटिंग्स",
        "settings_memory" to "स्थानीय स्मृति",
        "settings_api" to "AI प्रदाता एवं API कुंजी",
        "privacy_policy" to "गोपनीयता नीति",
        "about_app" to "स्नैपर टेक के बारे में",
        "voice_verified" to "आवाज़ सत्यापित",
        "voice_unverified" to "सत्यापन आवश्यक",
        "type_message_hint" to "स्नैपर AI से कुछ भी पूछें...",
        "save_memory" to "यह बात याद रखें"
    )

    private val marathiMap = mapOf(
        "app_title" to "स्नॅपर तंत्रज्ञान",
        "assistant_subtitle" to "तुमची एआय मैत्रीण आणि साथीदार",
        "welcome_greeting" to "नमस्कार! मी स्नॅपर AI आहे.",
        "welcome_message" to "मी तुम्हाला मदत करण्यासाठी नेहमी तयार आहे.",
        "get_started" to "शुरू करा",
        "nav_home" to "मुख्य",
        "nav_chat" to "चॅट",
        "nav_voice" to "व्हॉइस",
        "nav_tools" to "टूल्स",
        "nav_settings" to "सेटिंग्ज",
        "greeting_morning" to "शुभ सकाळ",
        "greeting_afternoon" to "शुभ दुपार",
        "greeting_evening" to "शुभ संध्याकाळ",
        "settings_language" to "भाषा सेटिंग्ज"
    )

    private val gujaratiMap = mapOf(
        "app_title" to "સ્નેપર ટેકનોલોજી",
        "assistant_subtitle" to "તમારું AI સાથી",
        "welcome_greeting" to "નમસ્તે! હું સ્નેપર AI છું.",
        "get_started" to "શરૂ કરો",
        "nav_home" to "હોમ",
        "nav_chat" to "ચેટ",
        "nav_voice" to "વોઇસ",
        "nav_tools" to "સાધનો",
        "nav_settings" to "સેટિંગ્સ",
        "greeting_morning" to "સુપ્રભાત",
        "settings_language" to "ભાષા સેટિંગ્સ"
    )

    private val bengaliMap = mapOf(
        "app_title" to "স্ন্যাপার প্রযুক্তি",
        "assistant_subtitle" to "আপনার নিজস্ব AI বন্ধু",
        "welcome_greeting" to "হ্যালো! আমি স্ন্যাপার AI।",
        "get_started" to "শুরু করুন",
        "nav_home" to "হোম",
        "nav_chat" to "চ্যাট",
        "nav_voice" to "ভয়েস",
        "nav_tools" to "টুলস",
        "nav_settings" to "সেটিংস",
        "settings_language" to "ভাষা সেটিংস"
    )

    private val tamilMap = mapOf(
        "app_title" to "ஸ்னாப்பர் டெக்னாலஜி",
        "assistant_subtitle" to "உங்கள் அன்பு AI தோழி",
        "welcome_greeting" to "வணக்கம்! நான் ஸ்னாப்பர் AI.",
        "get_started" to "தொடங்கவும்",
        "nav_home" to "முகப்பு",
        "nav_chat" to "சாட்",
        "nav_voice" to "வாய்ஸ்",
        "nav_tools" to "கருவிகள்",
        "nav_settings" to "அமைப்புகள்",
        "settings_language" to "மொழி அமைப்புகள்"
    )

    private val teluguMap = mapOf(
        "app_title" to "స్నాపర్ టెక్నాలజీ",
        "assistant_subtitle" to "మీ తోడు AI సహాయకురాలు",
        "welcome_greeting" to "నమస్కారం! నేను స్నాపర్ AI.",
        "get_started" to "ప్రారంభించండి",
        "nav_home" to "హోమ్",
        "nav_chat" to "చాట్",
        "nav_voice" to "వాయిస్",
        "nav_tools" to "టూల్స్",
        "nav_settings" to "సెట్టింగ్‌లు",
        "settings_language" to "భాష సెట్టింగ్‌లు"
    )

    private val kannadaMap = mapOf(
        "app_title" to "ಸ್ನ್ಯಾಪರ್ ತಂತ್ರಜ್ಞಾನ",
        "assistant_subtitle" to "ನಿಮ್ಮ AI ಸಹಾಯಕ",
        "welcome_greeting" to "ನಮಸ್ಕಾರ! ನಾನು ಸ್ನ್ಯಾಪರ್ AI.",
        "get_started" to "ಪ್ರಾರಂಭಿಸಿ",
        "nav_home" to "ಮುಖಪುಟ",
        "nav_chat" to "ಚಾಟ್",
        "nav_voice" to "ಧ್ವನಿ",
        "nav_tools" to "ಪರಿಕರಗಳು",
        "nav_settings" to "ಸೆಟ್ಟಿಂಗ್‌ಗಳು"
    )

    private val malayalamMap = mapOf(
        "app_title" to "സ്നാപ്പർ സാങ്കേതികവിദ്യ",
        "assistant_subtitle" to "നിങ്ങളുടെ AI സഹായി",
        "welcome_greeting" to "നമസ്കാരം! ഞാൻ സ്നാപ്പർ AI ആണ്.",
        "get_started" to "തുടങ്ങുക",
        "nav_home" to "ഹോം",
        "nav_chat" to "ചാറ്റ്",
        "nav_voice" to "വോയ്‌സ്",
        "nav_tools" to "ടൂളുകൾ",
        "nav_settings" to "ക്രമീകരണങ്ങൾ"
    )

    private val punjabiMap = mapOf(
        "app_title" to "ਸਨੈਪਰ ਟੈਕਨਾਲੋਜੀ",
        "assistant_subtitle" to "ਤੁਹਾਡਾ AI ਸਾਥੀ",
        "welcome_greeting" to "ਸਤਿ ਸ਼੍ਰੀ ਅਕਾਲ! ਮੈਂ ਸਨੈਪਰ AI ਹਾਂ।",
        "get_started" to "ਸ਼ੁਰੂ ਕਰੋ",
        "nav_home" to "ਹੋਮ",
        "nav_chat" to "ਚੈਟ",
        "nav_voice" to "ਵਾਇਸ",
        "nav_tools" to "ਟੂਲਸ",
        "nav_settings" to "ਸੈਟਿੰਗਾਂ"
    )

    private val urduMap = mapOf(
        "app_title" to "سنیپر ٹیکنالوجی",
        "assistant_subtitle" to "آپ کا اے آئی ساتھی",
        "welcome_greeting" to "سلام! میں سنیپر AI ہوں۔",
        "get_started" to "شروع کریں",
        "nav_home" to "ہوم",
        "nav_chat" to "چیٹ",
        "nav_voice" to "وائس",
        "nav_tools" to "ٹولز",
        "nav_settings" to "سیٹنگز"
    )

    private val maithiliMap = mapOf(
        "app_title" to "स्नैपर टेक्नोलॉजी",
        "assistant_subtitle" to "अहाँक अपन AI संगिनी",
        "welcome_greeting" to "प्रणाम! हम स्नैपर AI छी।",
        "welcome_message" to "हम अहाँक सभ काज में मद्दति करबैक लेल तैयार छी।",
        "get_started" to "शुरू करू",
        "nav_home" to "मुख्य पृष्ठ",
        "nav_chat" to "बातचीत",
        "nav_voice" to "आवाज़ सहायिका",
        "nav_tools" to "साधन",
        "nav_settings" to "सेटिंग्स"
    )

    private val odiaMap = mapOf(
        "app_title" to "ସ୍ନାପର ଟେକ୍ନୋଲୋଜି",
        "welcome_greeting" to "ନମସ୍କାର! ମୁଁ ସ୍ନାପର AI |",
        "get_started" to "ଆରମ୍ଭ କରନ୍ତୁ",
        "nav_home" to "ହୋମ୍",
        "nav_chat" to "ଚାଟ୍",
        "nav_settings" to "ସେଟିଂସ"
    )

    private val assameseMap = mapOf(
        "app_title" to "স্নেপাৰ প্ৰযুক্তি",
        "welcome_greeting" to "নমস্কাৰ! মই স্নেপাৰ AI।",
        "get_started" to "আৰম্ভ কৰক",
        "nav_home" to "হোম",
        "nav_settings" to "ছেটিংছ"
    )

    private val nepaliMap = mapOf(
        "app_title" to "स्न्यापर टेक्नोलोजी",
        "welcome_greeting" to "नमस्ते! म स्न्यापर AI हूँ।",
        "get_started" to "शुरू गर्नुहोस्",
        "nav_home" to "गृहपृष्ठ",
        "nav_settings" to "सेटिङहरू"
    )
}
