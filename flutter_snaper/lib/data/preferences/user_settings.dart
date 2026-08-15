import 'package:flutter/material.dart';

enum AssistantState {
  LISTENING,
  THINKING,
  SPEAKING,
  HUMOR,
  ANGER,
  CAMERA_ACTIVE,
  TORCH,
  AUTOMATION,
  EMERGENCY
}

class UserSettings extends ChangeNotifier {
  // Real-time Assistant State
  AssistantState assistantState = AssistantState.LISTENING;
  String currentExpressionEmoji = "🎙️";
  String lastResponseText = "";
  // Brand Configuration
  final String productName = "Snaper AI Assistant";
  final String corporateAttribution = "Made by Snaper Technology Pvt Ltd";
  final String backupFolderName = "Snaper_AI_Assistant_Backups";

  // State Variables
  String ownerName = "Sanjiv Sir";
  String ownerFullNameEn = "Sanjiv Kumar Jha";
  String ownerFullNameHi = "संजीव कुमार झा";
  bool isExplicitBhaiAllowed = false;
  bool isOnline = true;
  String languageCode = "en";
  bool isAutoStartOnBootEnabled = true;
  bool homeGreetingEmojiEnabled = true;
  int homeGreetingEmojiFrequency = 5;
  String homeScreenLayoutOrder = "RADHE_WIDGET,CLOCK_WIDGET,WEATHER_WIDGET,CONTROL_BANNER,HERO_ASSISTANT,QUICK_TOOLS,ASK_SNAPER";
  bool isVoiceVerified = false;

  // Operating Modes
  String activeMode = "All-Rounder"; // All-Rounder, Doctor/Health, Women's Health, Legal, Security & Force, Vehicle, Smart Home, IT & Business Automation
  
  // Radha Naam Jap Widget
  int radhaJapCount = 108;
  int radhaJapTarget = 108;
  bool bhaktiModeEnabled = false;
  bool shownJapOverlay = false;
  bool complainedToFamily = false;

  // High-Precision App Selector Map
  Map<String, bool> enabledApps = {
    "WhatsApp": true,
    "WhatsApp Business": false,
    "Instagram": true,
    "Instagram Lite": false,
    "Facebook": true,
    "YouTube": true,
    "Google Play Store": true,
    "System Files": true,
  };

  // Avatar & App Icon Studios
  String activeAvatar = "3D Hologram Classic"; // Classic 3D, Cyberpunk, Friendly 2D, Spark Sparkle
  String appIconStyle = "Liquid Glass Default"; // Gold Edition, Cyber Glow, Traditional Saffron, Liquid Glass Default

  // Memory Vault (Local-First Memory Editing)
  List<String> localMemories = [
    "Owner prefers to be addressed as 'Sanjiv Sir'.",
    "Favorite greeting is 'Radhe Radhe'.",
    "Smart Home configured via Matter Protocol in Study Room.",
    "EV Range Optimizer is set to 20% Reserve alert.",
  ];

  // AI Provider & Key State
  String geminiApiKey = "";
  String openaiApiKey = "";
  String anthropicApiKey = "";
  String grokApiKey = "";
  String nvidiaApiKey = "";
  String openRouterApiKey = "";
  String customBaseUrl = "";
  String activeModel = "None";
  String detectedProvider = "No Key Provided";

  // Localization Dictionary
  static const Map<String, Map<String, String>> localizedStrings = {
    "en": {
      "app_title": "Snaper AI Assistant",
      "made_by": "Made by Snaper Technology Pvt Ltd",
      "greeting": "Radhe Radhe",
      "model_status": "Auto-Detect AI Model",
      "settings": "Settings",
      "active_mode": "Active Mode",
      "radha_jap": "Radha Naam Jap",
      "avatar_studio": "Avatar Studio",
      "app_icon_studio": "App Icon Studio",
      "memory_vault": "Memory Vault",
      "device_transfer": "Device Transfer",
      "backup_restore": "Backup & Disaster Recovery",
      "owner_profile": "Owner Profile",
      "select_language": "Select Language",
    },
    "hi": {
      "app_title": "स्नैपर एआई असिस्टेंट",
      "made_by": "स्नैपर टेक्नोलॉजी प्राइवेट लिमिटेड द्वारा निर्मित",
      "greeting": "राधे राधे",
      "model_status": "एआई मॉडल ऑटो-डिटेक्ट",
      "settings": "सेटिंग्स",
      "active_mode": "सक्रिय मोड",
      "radha_jap": "राधा नाम जप",
      "avatar_studio": "अवतार स्टूडियो",
      "app_icon_studio": "ऐप आइकन स्टूडियो",
      "memory_vault": "स्मृति कोष",
      "device_transfer": "डिवाइस ट्रांसफर",
      "backup_restore": "बैकअप और रिकवरी",
      "owner_profile": "प्रोफाइल",
      "select_language": "भाषा चुनें",
    },
    "mai": {
      "app_title": "स्नैपर एआई असिस्टेंट",
      "made_by": "स्नैपर टेक्नोलॉजी प्राइवेट लिमिटेड द्वारा निर्मित",
      "greeting": "राधे राधे",
      "model_status": "एआई मॉडल ऑटो-डिटेक्ट",
      "settings": "सेटिंग्स",
      "active_mode": "सक्रिय मोड",
      "radha_jap": "राधा नाम जप",
      "avatar_studio": "अवतार स्टूडियो",
      "app_icon_studio": "ऐप आइकन स्टूडियो",
      "memory_vault": "स्मृति कोष",
      "device_transfer": "डिवाइस ट्रांसफर",
      "backup_restore": "बैकअप और रिकवरी",
      "owner_profile": "प्रोफाइल",
      "select_language": "भाषा चुनें",
    },
    "ur": {
      "app_title": "سنیپر اے آئی اسسٹنٹ",
      "made_by": "سنیپر ٹیکنالوجی پرائیویٹ لمیٹڈ کا تیار کردہ",
      "greeting": "رادھے رادھے",
      "model_status": "خودکار ماڈل کی شناخت",
      "settings": "ترتیبات",
      "active_mode": "سرگرم موڈ",
      "radha_jap": "رادھا نام جپ",
      "avatar_studio": "اوتار اسٹوڈیو",
      "app_icon_studio": "آئیکن اسٹوڈیو",
      "memory_vault": "میموری والٹ",
      "device_transfer": "ڈیوائس ٹرانسفر",
      "backup_restore": "بیک اپ اور ریکوری",
      "owner_profile": "پروفائل",
      "select_language": "زبان منتخب کریں",
    },
    "bn": {
      "app_title": "স্ন্যাপার এআই অ্যাসিস্ট্যান্ট",
      "made_by": "স্ন্যাপার টেকনোলজি প্রাইভেট লিমিটেড দ্বারা তৈরি",
      "greeting": "রাধে রাধে",
      "model_status": "এআই মডেল অটো-ডিটেক্ট",
      "settings": "সেটিংস",
      "active_mode": "সক্রিয় মোড",
      "radha_jap": "রাধা নাম জপ",
      "avatar_studio": "অবতার স্টুডিও",
      "app_icon_studio": "অ্যাপ আইকন স্টুডিও",
      "memory_vault": "মেমরি ভল্ট",
      "device_transfer": "ডিভাইস স্থানান্তর",
      "backup_restore": "ব্যাকআপ ও পুনরুদ্ধার",
      "owner_profile": "প্রোফাইল",
      "select_language": "ভাষা নির্বাচন করুন",
    },
    "mr": {
      "app_title": "स्नॅपर एआय असिस्टंट",
      "made_by": "स्नॅपर टेक्नॉलॉजी प्रायव्हेट लिमिटेड द्वारे निर्मित",
      "greeting": "राधे राधे",
      "model_status": "एआय मॉडेल ऑटो-डिटेक्ट",
      "settings": "सेटिंग्ज",
      "active_mode": "सक्रिय मोड",
      "radha_jap": "राधा नाम जप",
      "avatar_studio": "अवतार स्टुडिओ",
      "app_icon_studio": "अ‍ॅप आयकॉन स्टुडिओ",
      "memory_vault": "मेमरी व्हॉल्ट",
      "device_transfer": "डिव्हाइस ट्रान्सफर",
      "backup_restore": "बॅकअप आणि रिकव्हरी",
      "owner_profile": "प्रोफाइल",
      "select_language": "भाषा निवडा",
    },
    "pa": {
      "app_title": "ਸਨੈਪਰ ਏਆਈ ਸਹਾਇਕ",
      "made_by": "ਸਨੈਪਰ ਟੈਕਨਾਲੋਜੀ ਪ੍ਰਾਈਵੇਟ ਲਿਮਟਿਡ ਦੁਆਰਾ ਬਣਾਇਆ ਗਿਆ",
      "greeting": "ਰਾਧੇ ਰਾਧੇ",
      "model_status": "ਏਆਈ ਮਾਡਲ ਆਟੋ-ਡਿਟੈਕਟ",
      "settings": "ਸੈਟਿੰਗਾਂ",
      "active_mode": "ਐਕਟਿਵ ਮੋਡ",
      "radha_jap": "ਰਾਧਾ ਨਾਮ ਜਪ",
      "avatar_studio": "ਅਵਤਾਰ ਸਟੂਡੀਓ",
      "app_icon_studio": "ਐਪ ਆਈਕਨ ਸਟੂਡੀਓ",
      "memory_vault": "ਮੈਮੋਰੀ ਵਾਲਟ",
      "device_transfer": "ਡਿਵਾਈਸ ਟ੍ਰਾਂਸਫਰ",
      "backup_restore": "ਬੈਕਅੱਪ ਅਤੇ ਰਿਕਵਰੀ",
      "owner_profile": "ਪ੍ਰੋਫਾਈਲ",
      "select_language": "ਭਾਸ਼ਾ ਚੁਣੋ",
    },
    "ta": {
      "app_title": "ஸ்னாப்பர் ஏஐ அசிஸ்டண்ட்",
      "made_by": "ஸ்னாப்பர் டெக்னாலஜி பிரைவேட் லிமிடெட் தயாரிப்பு",
      "greeting": "ராதே ராதே",
      "model_status": "ஏஐ மாதிரி தானியங்கி கண்டறிதல்",
      "settings": "அமைப்புகள்",
      "active_mode": "செயலில் உள்ள பயன்முறை",
      "radha_jap": "ராதா நாம ஜெபம்",
      "avatar_studio": "அவதார ஸ்டுடியோ",
      "app_icon_studio": "ஆப் ஐகான் ஸ்டுடியோ",
      "memory_vault": "நினைவக பெட்டகம்",
      "device_transfer": "சாதன பரிமாற்றம்",
      "backup_restore": "காப்பு மற்றும் மீட்பு",
      "owner_profile": "சுயவிவரம்",
      "select_language": "மொழியைத் தேர்ந்தெடுக்கவும்",
    },
    "te": {
      "app_title": "స్నాపర్ ఐ అసిస్టెంట్",
      "made_by": "స్నాపర్ టెక్నాలజీ ప్రైవేట్ లిమిటెడ్ చేత తయారు చేయబడింది",
      "greeting": "రాధే రాధే",
      "model_status": "AI మోడల్ ఆటో-డిటెక్ట్",
      "settings": "సెట్టింగులు",
      "active_mode": "యాక్టివ్ మోడ్",
      "radha_jap": "రాధా నామ జపం",
      "avatar_studio": "అవతార్ స్టూడియో",
      "app_icon_studio": "యాప్ ఐకాన్ స్టూడియో",
      "memory_vault": "మెమరీ వాల్ట్",
      "device_transfer": "పరికర బదిలీ",
      "backup_restore": "బ్యాకప్ & రికవరీ",
      "owner_profile": "ప్రొఫైల్",
      "select_language": "భాషను ఎంచుకోండి",
    },
    "gu": {
      "app_title": "સ્નેપર એઆઈ આસિસ્ટન્ટ",
      "made_by": "સ્નેપર ટેકનોલોજી પ્રાઇવેટ લિમિટેડ દ્વારા બનાવવામાં આવેલ",
      "greeting": "રાધે રાધે",
      "model_status": "AI મોડેલ ઓટો-ડિટેક્ટ",
      "settings": "સેટિંગ્સ",
      "active_mode": "સક્રિય મોડ",
      "radha_jap": "રાધા નામ જાપ",
      "avatar_studio": "અવતાર સ્ટુડિયો",
      "app_icon_studio": "એપ્લિકેશન આયકન સ્ટુડિયો",
      "memory_vault": "મેમरी વૉલ્ટ",
      "device_transfer": "ડિવાઇસ ટ્રાન્સફર",
      "backup_restore": "બેકઅપ અને રિકવરી",
      "owner_profile": "પ્રોફાઇલ",
      "select_language": "ભાષા પસંદ કરો",
    },
    "bho": {
      "app_title": "स्नैपर एआई असिस्टेंट",
      "made_by": "स्नैपर टेक्नोलॉजी प्राइवेट लिमिटेड द्वारा बनल",
      "greeting": "राधे राधे",
      "model_status": "एआई मॉडल ऑटो-डिटेक्ट",
      "settings": "सेटिंग्स",
      "active_mode": "सक्रिय मोड",
      "radha_jap": "राधा नाम जप",
      "avatar_studio": "अवतार स्टूडियो",
      "app_icon_studio": "ऐप आइकन स्टूडियो",
      "memory_vault": "यादगार तिजोरी",
      "device_transfer": "डिवाइस ट्रांसफर",
      "backup_restore": "बैकअप आ रिकवरी",
      "owner_profile": "प्रोफाइल",
      "select_language": "भाषा चुनीं",
    },
    "kn": {
      "app_title": "ಸ್ನ್ಯಾಪರ್ ಎಐ ಅಸಿಸ್ಟೆಂಟ್",
      "made_by": "ಸ್ನ್ಯಾಪರ್ ಟೆಕ್ನಾಲಜಿ ಪ್ರೈವೇಟ್ ಲಿಮಿಟೆಡ್‌ನಿಂದ ತಯಾರಿಸಲ್ಪಟ್ಟಿದೆ",
      "greeting": "ರಾಧೇ ರಾಧೇ",
      "model_status": "ಎಐ ಮಾಡೆಲ್ ಆಟೋ-ಡಿಟೆಕ್ಟ್",
      "settings": "ಸೆಟ್ಟಿಂಗ್‌ಗಳು",
      "active_mode": "ಸಕ್ರಿಯ ಮೋಡ್",
      "radha_jap": "ರಾಧಾ ನಾಮ ಜಪ",
      "avatar_studio": "ಅವತಾರ್ ಸ್ಟುಡಿಯೋ",
      "app_icon_studio": "ಆಪ್ ಐಕಾನ್ ಸ್ಟುಡಿಯೋ",
      "memory_vault": "ಮೆಮೊರಿ ವಾಲ್ಟ್",
      "device_transfer": "ಸಾಧನ ವರ್ಗಾವಣೆ",
      "backup_restore": "ಬ್ಯಾಕಪ್ ಮತ್ತು ಮರುಪಡೆಯುವಿಕೆ",
      "owner_profile": "ಪ್ರೊಫೈಲ್",
      "select_language": "ಭಾಷೆಯನ್ನು ಆರಿಸಿ",
    },
    "ml": {
      "app_title": "സ്നാപ്പർ എഐ അസിസ്റ്റന്റ്",
      "made_by": "സ്നാപ്പർ ടെക്നോളജി പ്രൈവറ്റ് ലിമിറ്റഡ് നിർമ്മിച്ചത്",
      "greeting": "രാധേ രാധേ",
      "model_status": "AI മോഡൽ ഓട്ടോ-ഡിറ്റക്റ്റ്",
      "settings": "ക്രമീകരണങ്ങൾ",
      "active_mode": "സജീവ മോഡ്",
      "radha_jap": "രാധാ നാമ ജപം",
      "avatar_studio": "അവതാർ സ്റ്റുഡിയോ",
      "app_icon_studio": "ആപ്പ് ഐക്കൺ സ്റ്റുഡിയോ",
      "memory_vault": "മെമ്മറി വോൾട്ട്",
      "device_transfer": "ഉപകരണ കൈമാറ്റം",
      "backup_restore": "ബാക്കപ്പും വീണ്ടെടുക്കലും",
      "owner_profile": "പ്രൊഫൈൽ",
      "select_language": "ഭാഷ തിരഞ്ഞെടുക്കുക",
    },
    "or": {
      "app_title": "ସ୍ନାପର୍ ଏଆଇ ଆସିଷ୍ଟାଣ୍ଟ",
      "made_by": "ସ୍ନାପର୍ ଟେକ୍ନୋଲୋଜି ପ୍ରାଇଭେଟ୍ ଲିମିଟେଡ୍ ଦ୍ୱାରା ନିର୍ମିତ",
      "greeting": "ରାଧେ ରାଧେ",
      "model_status": "ଏଆଇ ମଡେଲ୍ ସ୍ୱୟଂ-ଚିହ୍ନଟ",
      "settings": "ସେଟିଙ୍ଗ୍ସ",
      "active_mode": "ସକ୍ରିୟ ମୋଡ୍",
      "radha_jap": "ରାଧା ନାମ ଜପ",
      "avatar_studio": "ଅବତାର ଷ୍ଟୁଡିଓ",
      "app_icon_studio": "ଆପ୍ ଆଇକନ୍ ଷ୍ଟୁଡିଓ",
      "memory_vault": "ସ୍ମୃତି କୋଷ",
      "device_transfer": "ଡିଭାଇସ୍ ସ୍ଥାନାନ୍ତର",
      "backup_restore": "ବ୍ୟାକଅପ୍ ଏବଂ ପୁନରୁଦ୍ଧାର",
      "owner_profile": "ପ୍ରୋଫାଇଲ୍",
      "select_language": "ଭାଷା ବାଛନ୍ତୁ",
    }
  };

  String getLocalizedText(String key) {
    final lang = localizedStrings[languageCode] ?? localizedStrings["en"]!;
    return lang[key] ?? key;
  }

  void updateLanguage(String code) {
    if (localizedStrings.containsKey(code)) {
      languageCode = code;
      notifyListeners();
    }
  }

  void updateOwnerName(String name) {
    // ABSOLUTE BAN ON "BHAI" / "BRO" (Module 35 Directive)
    if (!isExplicitBhaiAllowed && (name.toLowerCase().contains("bhai") || name.toLowerCase().contains("bro"))) {
       ownerName = "Sanjiv Sir";
    } else {
       ownerName = name;
    }
    notifyListeners();
  }

  void toggleOnlineMode(bool online) {
    isOnline = online;
    notifyListeners();
  }

  void toggleExplicitBhai(bool allowed) {
    isExplicitBhaiAllowed = allowed;
    notifyListeners();
  }

  void updateActiveMode(String mode) {
    activeMode = mode;
    notifyListeners();
  }

  void incrementRadhaJap() {
    radhaJapCount++;
    notifyListeners();
  }

  void resetRadhaJap() {
    radhaJapCount = 0;
    notifyListeners();
  }

  void updateAvatar(String avatar) {
    activeAvatar = avatar;
    notifyListeners();
  }

  void updateAppIcon(String style) {
    appIconStyle = style;
    notifyListeners();
  }

  // Add/Edit/Delete memory
  void addMemory(String memory) {
    localMemories.add(memory);
    notifyListeners();
  }

  void deleteMemory(int index) {
    if (index >= 0 && index < localMemories.length) {
      localMemories.removeAt(index);
      notifyListeners();
    }
  }

  void updateMemory(int index, String newMemory) {
    if (index >= 0 && index < localMemories.length) {
      localMemories[index] = newMemory;
      notifyListeners();
    }
  }

  // AI Provider Key update & Auto-detection
  void setApiKey(String provider, String key) {
    String detectedModel = "Unknown Model";
    String resolvedProvider = "Unknown";

    if (provider == "Google Gemini" || key.startsWith("AIzaSy")) {
      geminiApiKey = key;
      detectedModel = "gemini-2.5-pro-exp-08";
      resolvedProvider = "Google Gemini";
    } else if (provider == "OpenAI" || key.startsWith("sk-proj-")) {
      openaiApiKey = key;
      detectedModel = "gpt-4o-2026-08";
      resolvedProvider = "OpenAI";
    } else if (provider == "Anthropic" || key.startsWith("sk-ant-")) {
      anthropicApiKey = key;
      detectedModel = "claude-3-7-sonnet";
      resolvedProvider = "Anthropic Claude";
    } else if (provider == "xAI Grok" || key.startsWith("xai-")) {
      grokApiKey = key;
      detectedModel = "grok-beta";
      resolvedProvider = "xAI Grok";
    } else {
      // General custom or fallback
      detectedModel = "custom-llama3-70b-instruct";
      resolvedProvider = provider;
    }

    detectedProvider = resolvedProvider;
    activeModel = detectedModel;
    notifyListeners();
  }

  void updateAssistantState(AssistantState newState) {
    assistantState = newState;
    switch (newState) {
      case AssistantState.LISTENING:
        currentExpressionEmoji = "🎙️";
        break;
      case AssistantState.THINKING:
        currentExpressionEmoji = "💭";
        break;
      case AssistantState.SPEAKING:
        currentExpressionEmoji = "🗣️";
        break;
      case AssistantState.HUMOR:
        currentExpressionEmoji = "😂";
        break;
      case AssistantState.ANGER:
        currentExpressionEmoji = "💢";
        break;
      case AssistantState.CAMERA_ACTIVE:
        currentExpressionEmoji = "📷";
        break;
      case AssistantState.TORCH:
        currentExpressionEmoji = "🔦";
        break;
      case AssistantState.AUTOMATION:
        currentExpressionEmoji = "⚙️";
        break;
      case AssistantState.EMERGENCY:
        currentExpressionEmoji = "🚨";
        break;
    }
    notifyListeners();
  }

  void updateExpressionEmoji(String emoji) {
    currentExpressionEmoji = emoji;
    notifyListeners();
  }

  void updateLastResponseText(String text) {
    lastResponseText = text;
    notifyListeners();
  }

  void toggleApp(String appName, bool isEnabled) {
    enabledApps[appName] = isEnabled;
    notifyListeners();
  }
}
