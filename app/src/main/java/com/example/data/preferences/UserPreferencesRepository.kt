package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "snaper_settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK, AMOLED
}

enum class AiProvider(val displayName: String, val defaultBaseUrl: String) {
    GEMINI("Gemini AI (Google)", "https://generativelanguage.googleapis.com/"),
    OPENROUTER("OpenRouter AI", "https://openrouter.ai/api/v1/"),
    GROK("xAI Grok", "https://api.x.ai/v1/"),
    CLAUDE("Anthropic Claude", "https://api.anthropic.com/v1/"),
    NVIDIA("NVIDIA NIM AI", "https://integrate.api.nvidia.com/v1/"),
    KIMI("Kimi AI (Moonshot)", "https://api.moonshot.cn/v1/"),
    GLM("GLM-5 / Zhipu AI", "https://open.bigmodel.cn/api/paas/v4/"),
    OPENAI("OpenAI", "https://api.openai.com/v1/"),
    CUSTOM("Custom Endpoint", "https://api.openai.com/v1/")
}

data class UserSettings(
    val ownerName: String = "Sanjiv Sir",
    val ownerTitle: String = "Sanjiv Sir", // "Sanjiv Sir", "Sir", etc.
    val ownerBio: String = "Creator & Leader",
    val ownerPhotoUri: String = "",
    val assistantName: String = "Snaper",
    val wakePhrase: String = "Hey Snaper",
    val avatarStyle: String = "anime_female",
    val themeMode: ThemeMode = ThemeMode.DARK,
    val accentColorHex: String = "#8B5CF6", // Electric Violet
    val languageCode: String = "en", // Default English
    val aiProvider: AiProvider = AiProvider.GEMINI,
    val userApiKey: String = "",
    val customBaseUrl: String = "",
    val selectedModel: String = "gemini-3.5-flash",
    val voicePitch: Float = 1.2f, // Gentle female pitch
    val voiceSpeechRate: Float = 1.0f,
    val isAutoListenEnabled: Boolean = false,
    val isVoiceVerified: Boolean = false,
    val isBgListeningServiceActive: Boolean = false,
    val isFaceEnrolled: Boolean = false,
    val faceSignatureHash: String = "",
    val isFaceVerified: Boolean = false,
    val securityMode: String = "OWNER", // "NORMAL", "OWNER", "RESTRICTED", "EMERGENCY"
    val isCameraPrivacyEnabled: Boolean = false,
    val isFocusModeEnabled: Boolean = false,
    val isScreenUnlockEnabled: Boolean = true,
    val selectedAssistantPackage: String = "com.example",
    val isDynamicIslandEnabled: Boolean = true,
    val isActionPreviewEnabled: Boolean = true,
    val isAppPreviewEnabled: Boolean = true,
    val isAnimatedEmojiEnabled: Boolean = true,
    val isMoodReactionEnabled: Boolean = true,
    val isCameraMoodEnabled: Boolean = false,
    val isLockScreenIslandEnabled: Boolean = true,
    val isAodIntegrationEnabled: Boolean = true,
    val isAodEmojiEnabled: Boolean = true,
    val isAodAnimationEnabled: Boolean = true,
    val isAodEventDisplayEnabled: Boolean = true,
    val isBatterySaverModeEnabled: Boolean = false,
    val showPersonalDetailsOnLockScreen: Boolean = false,
    val isIdleSleepingEnabled: Boolean = true,
    val sleepingEmoji: String = "😴",
    val isWakeAnimationEnabled: Boolean = true,
    val idleSleepTimeoutSeconds: Int = 10,
    val isSystemAodProtectionEnforced: Boolean = true,
    val isSystemIslandProtectionEnforced: Boolean = true,
    val isCameraControlEnabled: Boolean = true,
    
    // Splash Screen Customization
    val splashSubtitle: String = "Personal Liquid Glass AI Assistant",
    val splashBgType: String = "DYNAMIC_GLASS", // DYNAMIC_GLASS, SOLID, GRADIENT, GLOSSY_NEON, 3D_AVATAR_SPLASH, 3D_MODEL_SPLASH
    val splashLogoStyle: String = "AUTO_AWESOME", // AUTO_AWESOME, SHIELD, STAR, CROWN
    val splashAnimationSpeed: String = "NORMAL", // SLOW, NORMAL, FAST
    val splashAnimationDuration: Int = 1800, // ms
    val splashAnimationStyle: String = "BOUNCE_SPRING", // BOUNCE_SPRING, FADE_SCALE, PULSE, ROTATE
    val splashIsAnimationEnabled: Boolean = true,
    val splashGlossIntensity: Float = 0.8f,
    val splashCustomImageUri: String = "",

    // 3D Avatar & 3D Model Customization
    val selectedAvatarType: String = "3D_AVATAR", // "PHOTO", "VIDEO", "3D_MODEL", "3D_AVATAR"
    val avatar3DModelUri: String = "",
    val avatar3DHairStyle: String = "LONG_CYBER", // "LONG_CYBER", "SHORT_BOB", "CYBER_MOHAWK", "ROYAL_PONYTAIL"
    val avatar3DHairColorHex: String = "#8B5CF6",
    val avatar3DSkinToneHex: String = "#FFF0EA",
    val avatar3DOutfitStyle: String = "CYBER_SUIT", // "CYBER_SUIT", "DEVOTIONAL_ROBE", "FUTURISTIC_ARMOR", "ROYAL_KIMONO"
    val avatar3DAccessoryStyle: String = "HOLOGRAM_HALO", // "HOLOGRAM_HALO", "CROWN", "CYBER_GLASSES", "DEVOTIONAL_MALA", "NONE"
    val avatar3DRotationY: Float = 0f,
    val avatar3DScale: Float = 1.0f,
    val avatar3DAnimationSpeed: Float = 1.0f,
    val splash3DModelUri: String = "",
    val splash3DRotationY: Float = 0f,
    val splash3DScale: Float = 1.0f,
    val splash3DAnimationSpeed: Float = 1.0f,
    
    // Home Screen Drag & Drop Layout
    val homeScreenLayoutOrder: String = "RADHE_WIDGET,CLOCK_WIDGET,WEATHER_WIDGET,CONTROL_BANNER,HERO_ASSISTANT,QUICK_TOOLS,ASK_SNAPER",
    val homeGreetingEmojiEnabled: Boolean = true,
    val homeGreetingEmojiFrequency: String = "AUTOMATIC",

    // Phone Finder / Where Are You Settings
    val isPhoneFinderEnabled: Boolean = true,
    val isPhoneFinderVoiceDetectionEnabled: Boolean = true,
    val isLastSeenContextEnabled: Boolean = true,
    val isEnvironmentalRecognitionEnabled: Boolean = true,
    val isPhoneFinderDynamicIslandEnabled: Boolean = true,
    val isPhoneFinderLocalProcessingOnly: Boolean = true,

    // Device Preview, Doctor Mode, Vehicle Mode, & Liquid Glass
    val selectedPreviewDevice: String = "AUTO", // AUTO, SAMSUNG_S26_ULTRA, IPHONE_17_PRO_MAX, PIXEL_9_PRO
    val isDoctorModeEnabled: Boolean = false,
    val isVehicleModeEnabled: Boolean = false,
    val isFemaleModeEnabled: Boolean = false,
    val isLegalModeEnabled: Boolean = false,
    val isAllRounderModeEnabled: Boolean = false,
    val isHomeModeEnabled: Boolean = false,
    val isItBusinessModeEnabled: Boolean = false,
    val isLiquidGlassEnabled: Boolean = true,
    val liquidGlassBlurRadius: Float = 16f,
    val isButton3DEffectEnabled: Boolean = true,

    // Cache & Network Optimization Settings
    val isAutoCacheCleanupEnabled: Boolean = true,
    val cacheLimitMb: Int = 250,
    val isNetworkOptimizationEnabled: Boolean = true,
    val weakNetworkMode: String = "AUTOMATIC", // AUTOMATIC, ALWAYS_ON, OFF
    val isDataSaverEnabled: Boolean = false,
    val isStreamingOptimizationEnabled: Boolean = true,
    val offlineModeSetting: String = "AUTOMATIC", // AUTOMATIC, ALWAYS_OFFLINE, ALWAYS_ONLINE

    // Custom App Icon, Auto-Start & Vehicle Connectivity Preferences
    val customAppIconUri: String = "",
    val isAutoStartOnBootEnabled: Boolean = true,
    val primaryVehicleId: String = "veh_001",
    val isVehicleAutoConnectEnabled: Boolean = true,

    // Assistant Resource Monitor Settings
    val isRealTimeMonitoringEnabled: Boolean = true,
    val isResourceRamAlertEnabled: Boolean = true,
    val resourceRamAlertThresholdMb: Int = 500,
    val isResourceCpuAlertEnabled: Boolean = true,
    val resourceCpuAlertThresholdPct: Int = 85,
    val isResourceStorageAlertEnabled: Boolean = true,
    val resourceStorageAlertThresholdMb: Int = 2000,

    // Step 5 Upgrades: Custom Themes & Color Picker, Avatar Custom Image, Screen Vision, Zero Data Privacy
    val customBgColorHex: String = "#0F172A",
    val customChatBubbleUserHex: String = "#8B5CF6",
    val customChatBubbleAiHex: String = "#1E293B",
    val customButtonColorHex: String = "#8B5CF6",
    val customTextColorHex: String = "#F8FAFC",
    val customAvatarBorderHex: String = "#A855F7",
    val customAvatarImageUri: String = "",
    val customAvatarSizeDp: Int = 260,
    val isScreenVisionEnabled: Boolean = false,
    val isOnboardingCompleted: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {

    private val prefDao by lazy { AppDatabase.getDatabase(context).userPreferenceDao() }
    private val secureCredentials by lazy { com.example.security.SecureCredentialsStore(context) }

    private suspend fun saveRoomPref(key: String, value: String) {
        try {
            prefDao.insertOrUpdatePreference(UserPreferenceEntity(key = key, value = value))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private object Keys {
        val OWNER_NAME = stringPreferencesKey("owner_name")
        val OWNER_TITLE = stringPreferencesKey("owner_title")
        val OWNER_BIO = stringPreferencesKey("owner_bio")
        val OWNER_PHOTO_URI = stringPreferencesKey("owner_photo_uri")
        val ASSISTANT_NAME = stringPreferencesKey("assistant_name")
        val WAKE_PHRASE = stringPreferencesKey("wake_phrase")
        val AVATAR_STYLE = stringPreferencesKey("avatar_style")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val USER_API_KEY = stringPreferencesKey("user_api_key")
        val CUSTOM_BASE_URL = stringPreferencesKey("custom_base_url")
        val SELECTED_MODEL = stringPreferencesKey("selected_model")
        val VOICE_PITCH = floatPreferencesKey("voice_pitch")
        val VOICE_SPEECH_RATE = floatPreferencesKey("voice_speech_rate")
        val IS_AUTO_LISTEN = booleanPreferencesKey("is_auto_listen")
        val IS_VOICE_VERIFIED = booleanPreferencesKey("is_voice_verified")
        val IS_BG_LISTENING_ACTIVE = booleanPreferencesKey("is_bg_listening_active")
        val IS_FACE_ENROLLED = booleanPreferencesKey("is_face_enrolled")
        val FACE_SIGNATURE_HASH = stringPreferencesKey("face_signature_hash")
        val IS_FACE_VERIFIED = booleanPreferencesKey("is_face_verified")
        val SECURITY_MODE = stringPreferencesKey("security_mode")
        val IS_CAMERA_PRIVACY_ENABLED = booleanPreferencesKey("is_camera_privacy_enabled")
        val IS_FOCUS_MODE_ENABLED = booleanPreferencesKey("is_focus_mode_enabled")
        val IS_SCREEN_UNLOCK_ENABLED = booleanPreferencesKey("is_screen_unlock_enabled")
        val SELECTED_ASSISTANT_PKG = stringPreferencesKey("selected_assistant_pkg")
        val IS_DYNAMIC_ISLAND_ENABLED = booleanPreferencesKey("is_dynamic_island_enabled")
        val IS_ACTION_PREVIEW_ENABLED = booleanPreferencesKey("is_action_preview_enabled")
        val IS_APP_PREVIEW_ENABLED = booleanPreferencesKey("is_app_preview_enabled")
        val IS_ANIMATED_EMOJI_ENABLED = booleanPreferencesKey("is_animated_emoji_enabled")
        val IS_MOOD_REACTION_ENABLED = booleanPreferencesKey("is_mood_reaction_enabled")
        val IS_CAMERA_MOOD_ENABLED = booleanPreferencesKey("is_camera_mood_enabled")
        val IS_LOCK_SCREEN_ISLAND_ENABLED = booleanPreferencesKey("is_lock_screen_island_enabled")
        val IS_AOD_INTEGRATION_ENABLED = booleanPreferencesKey("is_aod_integration_enabled")
        val IS_AOD_EMOJI_ENABLED = booleanPreferencesKey("is_aod_emoji_enabled")
        val IS_AOD_ANIMATION_ENABLED = booleanPreferencesKey("is_aod_animation_enabled")
        val IS_AOD_EVENT_DISPLAY_ENABLED = booleanPreferencesKey("is_aod_event_display_enabled")
        val IS_BATTERY_SAVER_MODE_ENABLED = booleanPreferencesKey("is_battery_saver_mode_enabled")
        val SHOW_PERSONAL_DETAILS_LOCK_SCREEN = booleanPreferencesKey("show_personal_details_lock_screen")
        val IS_IDLE_SLEEPING_ENABLED = booleanPreferencesKey("is_idle_sleeping_enabled")
        val SLEEPING_EMOJI = stringPreferencesKey("sleeping_emoji")
        val IS_WAKE_ANIMATION_ENABLED = booleanPreferencesKey("is_wake_animation_enabled")
        val IDLE_SLEEP_TIMEOUT_SECONDS = intPreferencesKey("idle_sleep_timeout_seconds")
        val IS_SYSTEM_AOD_PROTECTION = booleanPreferencesKey("is_system_aod_protection")
        val IS_SYSTEM_ISLAND_PROTECTION = booleanPreferencesKey("is_system_island_protection")
        val IS_CAMERA_CONTROL_ENABLED = booleanPreferencesKey("is_camera_control_enabled")
        val SPLASH_SUBTITLE = stringPreferencesKey("splash_subtitle")
        val SPLASH_BG_TYPE = stringPreferencesKey("splash_bg_type")
        val SPLASH_LOGO_STYLE = stringPreferencesKey("splash_logo_style")
        val SPLASH_ANIMATION_SPEED = stringPreferencesKey("splash_animation_speed")
        val SPLASH_ANIMATION_DURATION = intPreferencesKey("splash_animation_duration")
        val SPLASH_ANIMATION_STYLE = stringPreferencesKey("splash_animation_style")
        val SPLASH_IS_ANIMATION_ENABLED = booleanPreferencesKey("splash_is_animation_enabled")
        val SPLASH_GLOSS_INTENSITY = floatPreferencesKey("splash_gloss_intensity")
        val SPLASH_CUSTOM_IMAGE_URI = stringPreferencesKey("splash_custom_image_uri")
        val SELECTED_AVATAR_TYPE = stringPreferencesKey("selected_avatar_type")
        val AVATAR_3D_MODEL_URI = stringPreferencesKey("avatar_3d_model_uri")
        val AVATAR_3D_HAIR_STYLE = stringPreferencesKey("avatar_3d_hair_style")
        val AVATAR_3D_HAIR_COLOR_HEX = stringPreferencesKey("avatar_3d_hair_color_hex")
        val AVATAR_3D_SKIN_TONE_HEX = stringPreferencesKey("avatar_3d_skin_tone_hex")
        val AVATAR_3D_OUTFIT_STYLE = stringPreferencesKey("avatar_3d_outfit_style")
        val AVATAR_3D_ACCESSORY_STYLE = stringPreferencesKey("avatar_3d_accessory_style")
        val AVATAR_3D_ROTATION_Y = floatPreferencesKey("avatar_3d_rotation_y")
        val AVATAR_3D_SCALE = floatPreferencesKey("avatar_3d_scale")
        val AVATAR_3D_ANIMATION_SPEED = floatPreferencesKey("avatar_3d_animation_speed")
        val SPLASH_3D_MODEL_URI = stringPreferencesKey("splash_3d_model_uri")
        val SPLASH_3D_ROTATION_Y = floatPreferencesKey("splash_3d_rotation_y")
        val SPLASH_3D_SCALE = floatPreferencesKey("splash_3d_scale")
        val SPLASH_3D_ANIMATION_SPEED = floatPreferencesKey("splash_3d_animation_speed")
        val HOME_SCREEN_LAYOUT_ORDER = stringPreferencesKey("home_screen_layout_order")
        val HOME_GREETING_EMOJI_ENABLED = booleanPreferencesKey("home_greeting_emoji_enabled")
        val HOME_GREETING_EMOJI_FREQUENCY = stringPreferencesKey("home_greeting_emoji_frequency")
        val SELECTED_PREVIEW_DEVICE = stringPreferencesKey("selected_preview_device")
        val IS_DOCTOR_MODE_ENABLED = booleanPreferencesKey("is_doctor_mode_enabled")
        val IS_VEHICLE_MODE_ENABLED = booleanPreferencesKey("is_vehicle_mode_enabled")
        val IS_FEMALE_MODE_ENABLED = booleanPreferencesKey("is_female_mode_enabled")
        val IS_LEGAL_MODE_ENABLED = booleanPreferencesKey("is_legal_mode_enabled")
        val IS_ALL_ROUNDER_MODE_ENABLED = booleanPreferencesKey("is_all_rounder_mode_enabled")
        val IS_HOME_MODE_ENABLED = booleanPreferencesKey("is_home_mode_enabled")
        val IS_IT_BUSINESS_MODE_ENABLED = booleanPreferencesKey("is_it_business_mode_enabled")
        val IS_LIQUID_GLASS_ENABLED = booleanPreferencesKey("is_liquid_glass_enabled")
        val LIQUID_GLASS_BLUR_RADIUS = floatPreferencesKey("liquid_glass_blur_radius")
        val IS_BUTTON_3D_EFFECT_ENABLED = booleanPreferencesKey("is_button_3d_effect_enabled")
        val IS_AUTO_CACHE_CLEANUP_ENABLED = booleanPreferencesKey("is_auto_cache_cleanup_enabled")
        val CACHE_LIMIT_MB = intPreferencesKey("cache_limit_mb")
        val IS_NETWORK_OPTIMIZATION_ENABLED = booleanPreferencesKey("is_network_optimization_enabled")
        val WEAK_NETWORK_MODE = stringPreferencesKey("weak_network_mode")
        val IS_DATA_SAVER_ENABLED = booleanPreferencesKey("is_data_saver_enabled")
        val IS_STREAMING_OPTIMIZATION_ENABLED = booleanPreferencesKey("is_streaming_optimization_enabled")
        val OFFLINE_MODE_SETTING = stringPreferencesKey("offline_mode_setting")
        val CUSTOM_APP_ICON_URI = stringPreferencesKey("custom_app_icon_uri")
        val IS_AUTO_START_ON_BOOT_ENABLED = booleanPreferencesKey("is_auto_start_on_boot_enabled")
        val PRIMARY_VEHICLE_ID = stringPreferencesKey("primary_vehicle_id")
        val IS_VEHICLE_AUTO_CONNECT_ENABLED = booleanPreferencesKey("is_vehicle_auto_connect_enabled")
        val IS_PHONE_FINDER_ENABLED = booleanPreferencesKey("is_phone_finder_enabled")
        val IS_PHONE_FINDER_VOICE_DETECTION_ENABLED = booleanPreferencesKey("is_phone_finder_voice_detection_enabled")
        val IS_LAST_SEEN_CONTEXT_ENABLED = booleanPreferencesKey("is_last_seen_context_enabled")
        val IS_ENVIRONMENTAL_RECOGNITION_ENABLED = booleanPreferencesKey("is_environmental_recognition_enabled")
        val IS_PHONE_FINDER_DYNAMIC_ISLAND_ENABLED = booleanPreferencesKey("is_phone_finder_dynamic_island_enabled")
        val IS_PHONE_FINDER_LOCAL_PROCESSING_ONLY = booleanPreferencesKey("is_phone_finder_local_processing_only")
        val IS_REAL_TIME_MONITORING_ENABLED = booleanPreferencesKey("is_real_time_monitoring_enabled")
        val IS_RESOURCE_RAM_ALERT_ENABLED = booleanPreferencesKey("is_resource_ram_alert_enabled")
        val RESOURCE_RAM_ALERT_THRESHOLD_MB = intPreferencesKey("resource_ram_alert_threshold_mb")
        val IS_RESOURCE_CPU_ALERT_ENABLED = booleanPreferencesKey("is_resource_cpu_alert_enabled")
        val RESOURCE_CPU_ALERT_THRESHOLD_PCT = intPreferencesKey("resource_cpu_alert_threshold_pct")
        val IS_RESOURCE_STORAGE_ALERT_ENABLED = booleanPreferencesKey("is_resource_storage_alert_enabled")
        val RESOURCE_STORAGE_ALERT_THRESHOLD_MB = intPreferencesKey("resource_storage_alert_threshold_mb")
        val CUSTOM_BG_COLOR_HEX = stringPreferencesKey("custom_bg_color_hex")
        val CUSTOM_CHAT_BUBBLE_USER_HEX = stringPreferencesKey("custom_chat_bubble_user_hex")
        val CUSTOM_CHAT_BUBBLE_AI_HEX = stringPreferencesKey("custom_chat_bubble_ai_hex")
        val CUSTOM_BUTTON_COLOR_HEX = stringPreferencesKey("custom_button_color_hex")
        val CUSTOM_TEXT_COLOR_HEX = stringPreferencesKey("custom_text_color_hex")
        val CUSTOM_AVATAR_BORDER_HEX = stringPreferencesKey("custom_avatar_border_hex")
        val CUSTOM_AVATAR_IMAGE_URI = stringPreferencesKey("custom_avatar_image_uri")
        val CUSTOM_AVATAR_SIZE_DP = intPreferencesKey("custom_avatar_size_dp")
        val IS_SCREEN_VISION_ENABLED = booleanPreferencesKey("is_screen_vision_enabled")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            ownerName = prefs[Keys.OWNER_NAME] ?: "Sanjiv Sir",
            ownerTitle = prefs[Keys.OWNER_TITLE] ?: "Sanjiv Sir",
            ownerBio = prefs[Keys.OWNER_BIO] ?: "Visionary Creator",
            ownerPhotoUri = prefs[Keys.OWNER_PHOTO_URI] ?: "",
            assistantName = prefs[Keys.ASSISTANT_NAME] ?: "Snaper",
            wakePhrase = prefs[Keys.WAKE_PHRASE] ?: "Hey Snaper",
            avatarStyle = prefs[Keys.AVATAR_STYLE] ?: "anime_female",
            themeMode = try {
                ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.DARK.name)
            } catch (e: Exception) {
                ThemeMode.DARK
            },
            accentColorHex = prefs[Keys.ACCENT_COLOR_HEX] ?: "#8B5CF6",
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: "en",
            aiProvider = try {
                AiProvider.valueOf(prefs[Keys.AI_PROVIDER] ?: AiProvider.GEMINI.name)
            } catch (e: Exception) {
                AiProvider.GEMINI
            },
            userApiKey = secureCredentials.getCredential(com.example.security.SecureCredentialsStore.USER_API_KEY)
                ?: prefs[Keys.USER_API_KEY] ?: "",
            customBaseUrl = prefs[Keys.CUSTOM_BASE_URL] ?: "",
            selectedModel = prefs[Keys.SELECTED_MODEL] ?: "gemini-3.5-flash",
            voicePitch = prefs[Keys.VOICE_PITCH] ?: 1.2f,
            voiceSpeechRate = prefs[Keys.VOICE_SPEECH_RATE] ?: 1.0f,
            isAutoListenEnabled = prefs[Keys.IS_AUTO_LISTEN] ?: false,
            isVoiceVerified = prefs[Keys.IS_VOICE_VERIFIED] ?: false,
            isBgListeningServiceActive = prefs[Keys.IS_BG_LISTENING_ACTIVE] ?: false,
            isFaceEnrolled = prefs[Keys.IS_FACE_ENROLLED] ?: false,
            faceSignatureHash = prefs[Keys.FACE_SIGNATURE_HASH] ?: "",
            isFaceVerified = prefs[Keys.IS_FACE_VERIFIED] ?: false,
            securityMode = prefs[Keys.SECURITY_MODE] ?: "OWNER",
            isCameraPrivacyEnabled = prefs[Keys.IS_CAMERA_PRIVACY_ENABLED] ?: false,
            isFocusModeEnabled = prefs[Keys.IS_FOCUS_MODE_ENABLED] ?: false,
            isScreenUnlockEnabled = prefs[Keys.IS_SCREEN_UNLOCK_ENABLED] ?: true,
            selectedAssistantPackage = prefs[Keys.SELECTED_ASSISTANT_PKG] ?: "com.example",
            isDynamicIslandEnabled = prefs[Keys.IS_DYNAMIC_ISLAND_ENABLED] ?: true,
            isActionPreviewEnabled = prefs[Keys.IS_ACTION_PREVIEW_ENABLED] ?: true,
            isAppPreviewEnabled = prefs[Keys.IS_APP_PREVIEW_ENABLED] ?: true,
            isAnimatedEmojiEnabled = prefs[Keys.IS_ANIMATED_EMOJI_ENABLED] ?: true,
            isMoodReactionEnabled = prefs[Keys.IS_MOOD_REACTION_ENABLED] ?: true,
            isCameraMoodEnabled = prefs[Keys.IS_CAMERA_MOOD_ENABLED] ?: false,
            isLockScreenIslandEnabled = prefs[Keys.IS_LOCK_SCREEN_ISLAND_ENABLED] ?: true,
            isAodIntegrationEnabled = prefs[Keys.IS_AOD_INTEGRATION_ENABLED] ?: true,
            isAodEmojiEnabled = prefs[Keys.IS_AOD_EMOJI_ENABLED] ?: true,
            isAodAnimationEnabled = prefs[Keys.IS_AOD_ANIMATION_ENABLED] ?: true,
            isAodEventDisplayEnabled = prefs[Keys.IS_AOD_EVENT_DISPLAY_ENABLED] ?: true,
            isBatterySaverModeEnabled = prefs[Keys.IS_BATTERY_SAVER_MODE_ENABLED] ?: false,
            showPersonalDetailsOnLockScreen = prefs[Keys.SHOW_PERSONAL_DETAILS_LOCK_SCREEN] ?: false,
            isIdleSleepingEnabled = prefs[Keys.IS_IDLE_SLEEPING_ENABLED] ?: true,
            sleepingEmoji = prefs[Keys.SLEEPING_EMOJI] ?: "😴",
            isWakeAnimationEnabled = prefs[Keys.IS_WAKE_ANIMATION_ENABLED] ?: true,
            idleSleepTimeoutSeconds = prefs[Keys.IDLE_SLEEP_TIMEOUT_SECONDS] ?: 10,
            isSystemAodProtectionEnforced = prefs[Keys.IS_SYSTEM_AOD_PROTECTION] ?: true,
            isSystemIslandProtectionEnforced = prefs[Keys.IS_SYSTEM_ISLAND_PROTECTION] ?: true,
            isCameraControlEnabled = prefs[Keys.IS_CAMERA_CONTROL_ENABLED] ?: true,
            splashSubtitle = prefs[Keys.SPLASH_SUBTITLE] ?: "Personal Liquid Glass AI Assistant",
            splashBgType = prefs[Keys.SPLASH_BG_TYPE] ?: "DYNAMIC_GLASS",
            splashLogoStyle = prefs[Keys.SPLASH_LOGO_STYLE] ?: "AUTO_AWESOME",
            splashAnimationSpeed = prefs[Keys.SPLASH_ANIMATION_SPEED] ?: "NORMAL",
            splashAnimationDuration = prefs[Keys.SPLASH_ANIMATION_DURATION] ?: 1800,
            splashAnimationStyle = prefs[Keys.SPLASH_ANIMATION_STYLE] ?: "BOUNCE_SPRING",
            splashIsAnimationEnabled = prefs[Keys.SPLASH_IS_ANIMATION_ENABLED] ?: true,
            splashGlossIntensity = prefs[Keys.SPLASH_GLOSS_INTENSITY] ?: 0.8f,
            splashCustomImageUri = prefs[Keys.SPLASH_CUSTOM_IMAGE_URI] ?: "",
            selectedAvatarType = prefs[Keys.SELECTED_AVATAR_TYPE] ?: "3D_AVATAR",
            avatar3DModelUri = prefs[Keys.AVATAR_3D_MODEL_URI] ?: "",
            avatar3DHairStyle = prefs[Keys.AVATAR_3D_HAIR_STYLE] ?: "LONG_CYBER",
            avatar3DHairColorHex = prefs[Keys.AVATAR_3D_HAIR_COLOR_HEX] ?: "#8B5CF6",
            avatar3DSkinToneHex = prefs[Keys.AVATAR_3D_SKIN_TONE_HEX] ?: "#FFF0EA",
            avatar3DOutfitStyle = prefs[Keys.AVATAR_3D_OUTFIT_STYLE] ?: "CYBER_SUIT",
            avatar3DAccessoryStyle = prefs[Keys.AVATAR_3D_ACCESSORY_STYLE] ?: "HOLOGRAM_HALO",
            avatar3DRotationY = prefs[Keys.AVATAR_3D_ROTATION_Y] ?: 0f,
            avatar3DScale = prefs[Keys.AVATAR_3D_SCALE] ?: 1.0f,
            avatar3DAnimationSpeed = prefs[Keys.AVATAR_3D_ANIMATION_SPEED] ?: 1.0f,
            splash3DModelUri = prefs[Keys.SPLASH_3D_MODEL_URI] ?: "",
            splash3DRotationY = prefs[Keys.SPLASH_3D_ROTATION_Y] ?: 0f,
            splash3DScale = prefs[Keys.SPLASH_3D_SCALE] ?: 1.0f,
            splash3DAnimationSpeed = prefs[Keys.SPLASH_3D_ANIMATION_SPEED] ?: 1.0f,
            homeScreenLayoutOrder = prefs[Keys.HOME_SCREEN_LAYOUT_ORDER] ?: "RADHE_WIDGET,CLOCK_WIDGET,WEATHER_WIDGET,CONTROL_BANNER,HERO_ASSISTANT,QUICK_TOOLS,ASK_SNAPER",
            homeGreetingEmojiEnabled = prefs[Keys.HOME_GREETING_EMOJI_ENABLED] ?: true,
            homeGreetingEmojiFrequency = prefs[Keys.HOME_GREETING_EMOJI_FREQUENCY] ?: "AUTOMATIC",
            selectedPreviewDevice = prefs[Keys.SELECTED_PREVIEW_DEVICE] ?: "AUTO",
            isDoctorModeEnabled = prefs[Keys.IS_DOCTOR_MODE_ENABLED] ?: false,
            isVehicleModeEnabled = prefs[Keys.IS_VEHICLE_MODE_ENABLED] ?: false,
            isFemaleModeEnabled = prefs[Keys.IS_FEMALE_MODE_ENABLED] ?: false,
            isLegalModeEnabled = prefs[Keys.IS_LEGAL_MODE_ENABLED] ?: false,
            isAllRounderModeEnabled = prefs[Keys.IS_ALL_ROUNDER_MODE_ENABLED] ?: false,
            isHomeModeEnabled = prefs[Keys.IS_HOME_MODE_ENABLED] ?: false,
            isItBusinessModeEnabled = prefs[Keys.IS_IT_BUSINESS_MODE_ENABLED] ?: false,
            isLiquidGlassEnabled = prefs[Keys.IS_LIQUID_GLASS_ENABLED] ?: true,
            liquidGlassBlurRadius = prefs[Keys.LIQUID_GLASS_BLUR_RADIUS] ?: 16f,
            isButton3DEffectEnabled = prefs[Keys.IS_BUTTON_3D_EFFECT_ENABLED] ?: true,
            isAutoCacheCleanupEnabled = prefs[Keys.IS_AUTO_CACHE_CLEANUP_ENABLED] ?: true,
            cacheLimitMb = prefs[Keys.CACHE_LIMIT_MB] ?: 250,
            isNetworkOptimizationEnabled = prefs[Keys.IS_NETWORK_OPTIMIZATION_ENABLED] ?: true,
            weakNetworkMode = prefs[Keys.WEAK_NETWORK_MODE] ?: "AUTOMATIC",
            isDataSaverEnabled = prefs[Keys.IS_DATA_SAVER_ENABLED] ?: false,
            isStreamingOptimizationEnabled = prefs[Keys.IS_STREAMING_OPTIMIZATION_ENABLED] ?: true,
            offlineModeSetting = prefs[Keys.OFFLINE_MODE_SETTING] ?: "AUTOMATIC",
            customAppIconUri = prefs[Keys.CUSTOM_APP_ICON_URI] ?: "",
            isAutoStartOnBootEnabled = prefs[Keys.IS_AUTO_START_ON_BOOT_ENABLED] ?: true,
            primaryVehicleId = prefs[Keys.PRIMARY_VEHICLE_ID] ?: "veh_001",
            isVehicleAutoConnectEnabled = prefs[Keys.IS_VEHICLE_AUTO_CONNECT_ENABLED] ?: true,
            isPhoneFinderEnabled = prefs[Keys.IS_PHONE_FINDER_ENABLED] ?: true,
            isPhoneFinderVoiceDetectionEnabled = prefs[Keys.IS_PHONE_FINDER_VOICE_DETECTION_ENABLED] ?: true,
            isLastSeenContextEnabled = prefs[Keys.IS_LAST_SEEN_CONTEXT_ENABLED] ?: true,
            isEnvironmentalRecognitionEnabled = prefs[Keys.IS_ENVIRONMENTAL_RECOGNITION_ENABLED] ?: true,
            isPhoneFinderDynamicIslandEnabled = prefs[Keys.IS_PHONE_FINDER_DYNAMIC_ISLAND_ENABLED] ?: true,
            isPhoneFinderLocalProcessingOnly = prefs[Keys.IS_PHONE_FINDER_LOCAL_PROCESSING_ONLY] ?: true,
            isRealTimeMonitoringEnabled = prefs[Keys.IS_REAL_TIME_MONITORING_ENABLED] ?: true,
            isResourceRamAlertEnabled = prefs[Keys.IS_RESOURCE_RAM_ALERT_ENABLED] ?: true,
            resourceRamAlertThresholdMb = prefs[Keys.RESOURCE_RAM_ALERT_THRESHOLD_MB] ?: 500,
            isResourceCpuAlertEnabled = prefs[Keys.IS_RESOURCE_CPU_ALERT_ENABLED] ?: true,
            resourceCpuAlertThresholdPct = prefs[Keys.RESOURCE_CPU_ALERT_THRESHOLD_PCT] ?: 85,
            isResourceStorageAlertEnabled = prefs[Keys.IS_RESOURCE_STORAGE_ALERT_ENABLED] ?: true,
            resourceStorageAlertThresholdMb = prefs[Keys.RESOURCE_STORAGE_ALERT_THRESHOLD_MB] ?: 2000,
            customBgColorHex = prefs[Keys.CUSTOM_BG_COLOR_HEX] ?: "#0F172A",
            customChatBubbleUserHex = prefs[Keys.CUSTOM_CHAT_BUBBLE_USER_HEX] ?: "#8B5CF6",
            customChatBubbleAiHex = prefs[Keys.CUSTOM_CHAT_BUBBLE_AI_HEX] ?: "#1E293B",
            customButtonColorHex = prefs[Keys.CUSTOM_BUTTON_COLOR_HEX] ?: "#8B5CF6",
            customTextColorHex = prefs[Keys.CUSTOM_TEXT_COLOR_HEX] ?: "#F8FAFC",
            customAvatarBorderHex = prefs[Keys.CUSTOM_AVATAR_BORDER_HEX] ?: "#A855F7",
            customAvatarImageUri = prefs[Keys.CUSTOM_AVATAR_IMAGE_URI] ?: "",
            customAvatarSizeDp = prefs[Keys.CUSTOM_AVATAR_SIZE_DP] ?: 260,
            isScreenVisionEnabled = prefs[Keys.IS_SCREEN_VISION_ENABLED] ?: false,
            isOnboardingCompleted = prefs[Keys.IS_ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun updateCustomThemeColors(
        bgColorHex: String,
        chatUserHex: String,
        chatAiHex: String,
        buttonHex: String,
        textColorHex: String,
        avatarBorderHex: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CUSTOM_BG_COLOR_HEX] = bgColorHex
            prefs[Keys.CUSTOM_CHAT_BUBBLE_USER_HEX] = chatUserHex
            prefs[Keys.CUSTOM_CHAT_BUBBLE_AI_HEX] = chatAiHex
            prefs[Keys.CUSTOM_BUTTON_COLOR_HEX] = buttonHex
            prefs[Keys.CUSTOM_TEXT_COLOR_HEX] = textColorHex
            prefs[Keys.CUSTOM_AVATAR_BORDER_HEX] = avatarBorderHex
        }
        saveRoomPref("custom_bg_color_hex", bgColorHex)
    }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDING_COMPLETED] = completed
        }
        saveRoomPref("is_onboarding_completed", completed.toString())
    }

    suspend fun updateCustomAvatarImage(uri: String, sizeDp: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CUSTOM_AVATAR_IMAGE_URI] = uri
            prefs[Keys.CUSTOM_AVATAR_SIZE_DP] = sizeDp
        }
        saveRoomPref("custom_avatar_image_uri", uri)
        saveRoomPref("custom_avatar_size_dp", sizeDp.toString())
    }

    suspend fun setScreenVisionEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_SCREEN_VISION_ENABLED] = enabled
        }
        saveRoomPref("is_screen_vision_enabled", enabled.toString())
    }

    suspend fun performZeroDataHardReset() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
        try {
            val db = AppDatabase.getDatabase(context)
            db.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun setRealTimeMonitoringEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_REAL_TIME_MONITORING_ENABLED] = enabled }
    }
    suspend fun setResourceRamAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_RESOURCE_RAM_ALERT_ENABLED] = enabled }
    }
    suspend fun setResourceRamAlertThresholdMb(mb: Int) {
        context.dataStore.edit { it[Keys.RESOURCE_RAM_ALERT_THRESHOLD_MB] = mb }
    }
    suspend fun setResourceCpuAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_RESOURCE_CPU_ALERT_ENABLED] = enabled }
    }
    suspend fun setResourceCpuAlertThresholdPct(pct: Int) {
        context.dataStore.edit { it[Keys.RESOURCE_CPU_ALERT_THRESHOLD_PCT] = pct }
    }
    suspend fun setResourceStorageAlertEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_RESOURCE_STORAGE_ALERT_ENABLED] = enabled }
    }
    suspend fun setResourceStorageAlertThresholdMb(mb: Int) {
        context.dataStore.edit { it[Keys.RESOURCE_STORAGE_ALERT_THRESHOLD_MB] = mb }
    }

    suspend fun setPhoneFinderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_PHONE_FINDER_ENABLED] = enabled }
    }
    suspend fun setPhoneFinderVoiceDetectionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_PHONE_FINDER_VOICE_DETECTION_ENABLED] = enabled }
    }
    suspend fun setLastSeenContextEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_LAST_SEEN_CONTEXT_ENABLED] = enabled }
    }
    suspend fun setEnvironmentalRecognitionEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_ENVIRONMENTAL_RECOGNITION_ENABLED] = enabled }
    }
    suspend fun setPhoneFinderDynamicIslandEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_PHONE_FINDER_DYNAMIC_ISLAND_ENABLED] = enabled }
    }
    suspend fun setPhoneFinderLocalProcessingOnly(enabled: Boolean) {
        context.dataStore.edit { it[Keys.IS_PHONE_FINDER_LOCAL_PROCESSING_ONLY] = enabled }
    }

    suspend fun updateOwnerInfo(name: String, bio: String, avatar: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OWNER_NAME] = name
            prefs[Keys.OWNER_BIO] = bio
            prefs[Keys.AVATAR_STYLE] = avatar
        }
        saveRoomPref("owner_name", name)
        saveRoomPref("owner_bio", bio)
        saveRoomPref("avatar_style", avatar)
    }

    suspend fun updateAssistantIdentity(name: String, wakePhrase: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ASSISTANT_NAME] = name
            prefs[Keys.WAKE_PHRASE] = wakePhrase
        }
        saveRoomPref("assistant_name", name)
        saveRoomPref("wake_phrase", wakePhrase)
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
        saveRoomPref("theme_mode", mode.name)
    }

    suspend fun updateAccentColor(colorHex: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCENT_COLOR_HEX] = colorHex
        }
        saveRoomPref("accent_color_hex", colorHex)
    }

    suspend fun updateLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE_CODE] = languageCode
        }
        saveRoomPref("language_code", languageCode)
    }

    suspend fun updateAiProvider(provider: AiProvider, baseUrl: String, model: String, apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AI_PROVIDER] = provider.name
            prefs[Keys.CUSTOM_BASE_URL] = baseUrl
            prefs[Keys.SELECTED_MODEL] = model
            // Clear any legacy plain-text copy so only the encrypted store holds the key.
            prefs.remove(Keys.USER_API_KEY)
        }
        // Persist the API key only in the hardware-backed encrypted store — never in plain
        // DataStore/Room. If encryption is unavailable the key is not persisted (graceful).
        secureCredentials.saveCredential(com.example.security.SecureCredentialsStore.USER_API_KEY, apiKey)
        saveRoomPref("ai_provider", provider.name)
        saveRoomPref("custom_base_url", baseUrl)
        saveRoomPref("selected_model", model)
    }

    suspend fun updateVoiceSettings(pitch: Float, rate: Float, autoListen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VOICE_PITCH] = pitch
            prefs[Keys.VOICE_SPEECH_RATE] = rate
            prefs[Keys.IS_AUTO_LISTEN] = autoListen
        }
        saveRoomPref("voice_pitch", pitch.toString())
        saveRoomPref("voice_speech_rate", rate.toString())
        saveRoomPref("is_auto_listen", autoListen.toString())
    }

    suspend fun setVoiceVerified(verified: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_VOICE_VERIFIED] = verified
        }
        saveRoomPref("is_voice_verified", verified.toString())
    }

    suspend fun setBgListeningActive(active: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_BG_LISTENING_ACTIVE] = active
        }
        saveRoomPref("is_bg_listening_active", active.toString())
    }

    suspend fun updateFaceEnrollment(enrolled: Boolean, hash: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_FACE_ENROLLED] = enrolled
            prefs[Keys.FACE_SIGNATURE_HASH] = hash
            if (!enrolled) {
                prefs[Keys.IS_FACE_VERIFIED] = false
            }
        }
        saveRoomPref("is_face_enrolled", enrolled.toString())
        saveRoomPref("face_signature_hash", hash)
        if (!enrolled) {
            saveRoomPref("is_face_verified", "false")
        }
    }

    suspend fun setFaceVerified(verified: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_FACE_VERIFIED] = verified
        }
        saveRoomPref("is_face_verified", verified.toString())
    }

    suspend fun updateOwnerTitle(title: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.OWNER_TITLE] = title
        }
        saveRoomPref("owner_title", title)
    }

    suspend fun updateSecurityMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SECURITY_MODE] = mode
        }
        saveRoomPref("security_mode", mode)
    }

    suspend fun setCameraPrivacyEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_CAMERA_PRIVACY_ENABLED] = enabled
        }
        saveRoomPref("is_camera_privacy_enabled", enabled.toString())
    }

    suspend fun setFocusModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_FOCUS_MODE_ENABLED] = enabled
        }
        saveRoomPref("is_focus_mode_enabled", enabled.toString())
    }

    suspend fun setScreenUnlockEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_SCREEN_UNLOCK_ENABLED] = enabled
        }
        saveRoomPref("is_screen_unlock_enabled", enabled.toString())
    }

    suspend fun updateSelectedAssistantPackage(packageName: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_ASSISTANT_PKG] = packageName
        }
        saveRoomPref("selected_assistant_pkg", packageName)
    }

    suspend fun setDynamicIslandEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_DYNAMIC_ISLAND_ENABLED] = enabled }
        saveRoomPref("is_dynamic_island_enabled", enabled.toString())
    }

    suspend fun setActionPreviewEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_ACTION_PREVIEW_ENABLED] = enabled }
        saveRoomPref("is_action_preview_enabled", enabled.toString())
    }

    suspend fun setAppPreviewEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_APP_PREVIEW_ENABLED] = enabled }
        saveRoomPref("is_app_preview_enabled", enabled.toString())
    }

    suspend fun setAnimatedEmojiEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_ANIMATED_EMOJI_ENABLED] = enabled }
        saveRoomPref("is_animated_emoji_enabled", enabled.toString())
    }

    suspend fun setMoodReactionEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_MOOD_REACTION_ENABLED] = enabled }
        saveRoomPref("is_mood_reaction_enabled", enabled.toString())
    }

    suspend fun setCameraMoodEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_CAMERA_MOOD_ENABLED] = enabled }
        saveRoomPref("is_camera_mood_enabled", enabled.toString())
    }

    suspend fun setLockScreenIslandEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_LOCK_SCREEN_ISLAND_ENABLED] = enabled }
        saveRoomPref("is_lock_screen_island_enabled", enabled.toString())
    }

    suspend fun setAodIntegrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_AOD_INTEGRATION_ENABLED] = enabled }
        saveRoomPref("is_aod_integration_enabled", enabled.toString())
    }

    suspend fun setAodEmojiEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_AOD_EMOJI_ENABLED] = enabled }
        saveRoomPref("is_aod_emoji_enabled", enabled.toString())
    }

    suspend fun setAodAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_AOD_ANIMATION_ENABLED] = enabled }
        saveRoomPref("is_aod_animation_enabled", enabled.toString())
    }

    suspend fun setAodEventDisplayEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_AOD_EVENT_DISPLAY_ENABLED] = enabled }
        saveRoomPref("is_aod_event_display_enabled", enabled.toString())
    }

    suspend fun setBatterySaverModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_BATTERY_SAVER_MODE_ENABLED] = enabled }
        saveRoomPref("is_battery_saver_mode_enabled", enabled.toString())
    }

    suspend fun setShowPersonalDetailsOnLockScreen(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SHOW_PERSONAL_DETAILS_LOCK_SCREEN] = enabled }
        saveRoomPref("show_personal_details_lock_screen", enabled.toString())
    }

    suspend fun setIdleSleepingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_IDLE_SLEEPING_ENABLED] = enabled }
        saveRoomPref("is_idle_sleeping_enabled", enabled.toString())
    }

    suspend fun setSleepingEmoji(emoji: String) {
        context.dataStore.edit { prefs -> prefs[Keys.SLEEPING_EMOJI] = emoji }
        saveRoomPref("sleeping_emoji", emoji)
    }

    suspend fun setWakeAnimationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_WAKE_ANIMATION_ENABLED] = enabled }
        saveRoomPref("is_wake_animation_enabled", enabled.toString())
    }

    suspend fun setIdleSleepTimeoutSeconds(seconds: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.IDLE_SLEEP_TIMEOUT_SECONDS] = seconds }
        saveRoomPref("idle_sleep_timeout_seconds", seconds.toString())
    }

    suspend fun setCameraControlEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_CAMERA_CONTROL_ENABLED] = enabled }
        saveRoomPref("is_camera_control_enabled", enabled.toString())
    }

    // Splash Screen Customization Setters
    suspend fun updateSplashScreenConfig(
        subtitle: String,
        bgType: String,
        logoStyle: String,
        animSpeed: String,
        animDuration: Int,
        animStyle: String,
        isAnimEnabled: Boolean,
        glossIntensity: Float,
        customImageUri: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SPLASH_SUBTITLE] = subtitle
            prefs[Keys.SPLASH_BG_TYPE] = bgType
            prefs[Keys.SPLASH_LOGO_STYLE] = logoStyle
            prefs[Keys.SPLASH_ANIMATION_SPEED] = animSpeed
            prefs[Keys.SPLASH_ANIMATION_DURATION] = animDuration
            prefs[Keys.SPLASH_ANIMATION_STYLE] = animStyle
            prefs[Keys.SPLASH_IS_ANIMATION_ENABLED] = isAnimEnabled
            prefs[Keys.SPLASH_GLOSS_INTENSITY] = glossIntensity
            prefs[Keys.SPLASH_CUSTOM_IMAGE_URI] = customImageUri
        }
    }

    suspend fun resetSplashScreenToDefault() {
        context.dataStore.edit { prefs ->
            prefs[Keys.SPLASH_SUBTITLE] = "Personal Liquid Glass AI Assistant"
            prefs[Keys.SPLASH_BG_TYPE] = "DYNAMIC_GLASS"
            prefs[Keys.SPLASH_LOGO_STYLE] = "AUTO_AWESOME"
            prefs[Keys.SPLASH_ANIMATION_SPEED] = "NORMAL"
            prefs[Keys.SPLASH_ANIMATION_DURATION] = 1800
            prefs[Keys.SPLASH_ANIMATION_STYLE] = "BOUNCE_SPRING"
            prefs[Keys.SPLASH_IS_ANIMATION_ENABLED] = true
            prefs[Keys.SPLASH_GLOSS_INTENSITY] = 0.8f
            prefs[Keys.SPLASH_CUSTOM_IMAGE_URI] = ""
        }
    }

    // 3D Avatar Customization Setters
    suspend fun update3DAvatarConfig(
        avatarType: String,
        modelUri: String,
        hairStyle: String,
        hairColorHex: String,
        skinToneHex: String,
        outfitStyle: String,
        accessoryStyle: String,
        rotationY: Float,
        scale: Float,
        animSpeed: Float
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_AVATAR_TYPE] = avatarType
            prefs[Keys.AVATAR_3D_MODEL_URI] = modelUri
            prefs[Keys.AVATAR_3D_HAIR_STYLE] = hairStyle
            prefs[Keys.AVATAR_3D_HAIR_COLOR_HEX] = hairColorHex
            prefs[Keys.AVATAR_3D_SKIN_TONE_HEX] = skinToneHex
            prefs[Keys.AVATAR_3D_OUTFIT_STYLE] = outfitStyle
            prefs[Keys.AVATAR_3D_ACCESSORY_STYLE] = accessoryStyle
            prefs[Keys.AVATAR_3D_ROTATION_Y] = rotationY
            prefs[Keys.AVATAR_3D_SCALE] = scale
            prefs[Keys.AVATAR_3D_ANIMATION_SPEED] = animSpeed
        }
        saveRoomPref("selected_avatar_type", avatarType)
        saveRoomPref("avatar_3d_hair_style", hairStyle)
        saveRoomPref("avatar_3d_outfit_style", outfitStyle)
    }

    suspend fun update3DSplashConfig(
        modelUri: String,
        rotationY: Float,
        scale: Float,
        animSpeed: Float
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SPLASH_3D_MODEL_URI] = modelUri
            prefs[Keys.SPLASH_3D_ROTATION_Y] = rotationY
            prefs[Keys.SPLASH_3D_SCALE] = scale
            prefs[Keys.SPLASH_3D_ANIMATION_SPEED] = animSpeed
        }
        saveRoomPref("splash_3d_model_uri", modelUri)
    }

    // Home Screen Layout Setters
    suspend fun setHomeScreenLayoutOrder(order: String) {
        context.dataStore.edit { prefs -> prefs[Keys.HOME_SCREEN_LAYOUT_ORDER] = order }
        saveRoomPref("home_screen_layout_order", order)
    }

    suspend fun resetHomeScreenLayout() {
        val defaultOrder = "RADHE_WIDGET,CLOCK_WIDGET,WEATHER_WIDGET,CONTROL_BANNER,HERO_ASSISTANT,QUICK_TOOLS,ASK_SNAPER"
        context.dataStore.edit { prefs -> prefs[Keys.HOME_SCREEN_LAYOUT_ORDER] = defaultOrder }
        saveRoomPref("home_screen_layout_order", defaultOrder)
    }

    suspend fun setHomeGreetingEmojiEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.HOME_GREETING_EMOJI_ENABLED] = enabled }
        saveRoomPref("home_greeting_emoji_enabled", enabled.toString())
    }

    suspend fun setHomeGreetingEmojiFrequency(frequency: String) {
        context.dataStore.edit { prefs -> prefs[Keys.HOME_GREETING_EMOJI_FREQUENCY] = frequency }
        saveRoomPref("home_greeting_emoji_frequency", frequency)
    }

    suspend fun updateSelectedPreviewDevice(device: String) {
        context.dataStore.edit { prefs -> prefs[Keys.SELECTED_PREVIEW_DEVICE] = device }
        saveRoomPref("selected_preview_device", device)
    }

    suspend fun setDoctorModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_DOCTOR_MODE_ENABLED] = enabled }
        saveRoomPref("is_doctor_mode_enabled", enabled.toString())
    }

    suspend fun setVehicleModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_VEHICLE_MODE_ENABLED] = enabled }
        saveRoomPref("is_vehicle_mode_enabled", enabled.toString())
    }

    suspend fun setFemaleModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_FEMALE_MODE_ENABLED] = enabled }
        saveRoomPref("is_female_mode_enabled", enabled.toString())
    }

    suspend fun setLegalModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_LEGAL_MODE_ENABLED] = enabled }
        saveRoomPref("is_legal_mode_enabled", enabled.toString())
    }

    suspend fun setAllRounderModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_ALL_ROUNDER_MODE_ENABLED] = enabled }
        saveRoomPref("is_all_rounder_mode_enabled", enabled.toString())
    }

    suspend fun setHomeModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_HOME_MODE_ENABLED] = enabled }
        saveRoomPref("is_home_mode_enabled", enabled.toString())
    }

    suspend fun setItBusinessModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_IT_BUSINESS_MODE_ENABLED] = enabled }
        saveRoomPref("is_it_business_mode_enabled", enabled.toString())
    }

    suspend fun setLiquidGlassEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_LIQUID_GLASS_ENABLED] = enabled }
        saveRoomPref("is_liquid_glass_enabled", enabled.toString())
    }

    suspend fun setButton3DEffectEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_BUTTON_3D_EFFECT_ENABLED] = enabled }
        saveRoomPref("is_button_3d_effect_enabled", enabled.toString())
    }

    suspend fun setAutoCacheCleanupEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_AUTO_CACHE_CLEANUP_ENABLED] = enabled }
        saveRoomPref("is_auto_cache_cleanup_enabled", enabled.toString())
    }

    suspend fun setCacheLimitMb(limitMb: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.CACHE_LIMIT_MB] = limitMb }
        saveRoomPref("cache_limit_mb", limitMb.toString())
    }

    suspend fun setNetworkOptimizationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_NETWORK_OPTIMIZATION_ENABLED] = enabled }
        saveRoomPref("is_network_optimization_enabled", enabled.toString())
    }

    suspend fun setWeakNetworkMode(mode: String) {
        context.dataStore.edit { prefs -> prefs[Keys.WEAK_NETWORK_MODE] = mode }
        saveRoomPref("weak_network_mode", mode)
    }

    suspend fun setDataSaverEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_DATA_SAVER_ENABLED] = enabled }
        saveRoomPref("is_data_saver_enabled", enabled.toString())
    }

    suspend fun setStreamingOptimizationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_STREAMING_OPTIMIZATION_ENABLED] = enabled }
        saveRoomPref("is_streaming_optimization_enabled", enabled.toString())
    }

    suspend fun setOfflineModeSetting(setting: String) {
        context.dataStore.edit { prefs -> prefs[Keys.OFFLINE_MODE_SETTING] = setting }
        saveRoomPref("offline_mode_setting", setting)
    }

    suspend fun setCustomAppIconUri(uri: String) {
        context.dataStore.edit { prefs -> prefs[Keys.CUSTOM_APP_ICON_URI] = uri }
        saveRoomPref("custom_app_icon_uri", uri)
    }

    suspend fun setAutoStartOnBootEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_AUTO_START_ON_BOOT_ENABLED] = enabled }
        saveRoomPref("is_auto_start_on_boot_enabled", enabled.toString())
    }

    suspend fun setPrimaryVehicleId(id: String) {
        context.dataStore.edit { prefs -> prefs[Keys.PRIMARY_VEHICLE_ID] = id }
        saveRoomPref("primary_vehicle_id", id)
    }

    suspend fun setVehicleAutoConnectEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.IS_VEHICLE_AUTO_CONNECT_ENABLED] = enabled }
        saveRoomPref("is_vehicle_auto_connect_enabled", enabled.toString())
    }

    suspend fun updateOwnerPhoto(photoUri: String) {
        context.dataStore.edit { prefs -> prefs[Keys.OWNER_PHOTO_URI] = photoUri }
        saveRoomPref("owner_photo_uri", photoUri)
    }
}
