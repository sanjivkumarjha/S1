package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.data.preferences.UserSettings
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.AppControlScreen
import com.example.ui.screens.AssistantIdentityScreen
import com.example.ui.screens.AvatarGalleryScreen
import com.example.ui.screens.AssistantResourceMonitorScreen
import com.example.ui.screens.BirthdayScreen
import com.example.ui.screens.BusinessAutomationScreen
import com.example.ui.screens.CallSummaryScreen
import com.example.ui.screens.CameraVisionScreen
import com.example.ui.screens.ControlCenterScreen
import com.example.ui.screens.CustomerCrmScreen
import com.example.ui.screens.DeviceCareScreen
import com.example.ui.screens.FaceEnrollmentScreen
import com.example.ui.screens.FamilyProfileScreen
import com.example.ui.screens.GallerySearchScreen
import com.example.ui.screens.GeminiStudioScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MultiTaskingControlScreen
import com.example.ui.screens.OwnerVerificationScreen
import com.example.ui.screens.PhoneFinderSettingsScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.SecurityDashboardScreen
import com.example.ui.screens.SettingsApiScreen
import com.example.ui.screens.SettingsAssistantStartupScreen
import com.example.ui.screens.SettingsAppIconScreen
import com.example.ui.screens.SettingsLanguageScreen
import com.example.ui.screens.SettingsMainScreen
import com.example.ui.screens.SettingsMemoryScreen
import com.example.ui.screens.SettingsNetworkScreen
import com.example.ui.screens.SettingsOwnerScreen
import com.example.ui.screens.SettingsStorageCacheScreen
import com.example.ui.screens.SettingsThemeScreen
import com.example.ui.screens.VehicleConnectivityScreen
import com.example.ui.screens.SmartHomeScreen
import com.example.ui.screens.SocialMediaSearchScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VoiceAssistantScreen
import com.example.ui.screens.VoiceVerificationScreen
import com.example.ui.screens.WelcomeScreen

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val AI_CHAT = "ai_chat"
    const val VOICE_ASSISTANT = "voice_assistant"
    const val VOICE_VERIFICATION = "voice_verification"
    const val FACE_ENROLLMENT = "face_enrollment"
    const val OWNER_VERIFICATION = "owner_verification"
    const val TOOLS = "tools"
    const val SETTINGS_MAIN = "settings_main"
    const val SETTINGS_OWNER = "settings_owner"
    const val SETTINGS_THEME = "settings_theme"
    const val SETTINGS_LANGUAGE = "settings_language"
    const val SETTINGS_MEMORY = "settings_memory"
    const val SETTINGS_API = "settings_api"
    const val PRIVACY_POLICY = "privacy_policy"
    const val ABOUT = "about"
    const val APP_CONTROL = "app_control"
    const val ASSISTANT_IDENTITY = "assistant_identity"
    const val CAMERA_VISION = "camera_vision"
    const val DEVICE_CARE = "device_care"
    const val SECURITY_DASHBOARD = "security_dashboard"
    const val FAMILY_PROFILES = "family_profiles"
    const val CUSTOMER_CRM = "customer_crm"
    const val BIRTHDAY_MANAGER = "birthday_manager"
    const val GALLERY_SEARCH = "gallery_search"
    const val SMART_HOME = "smart_home"
    const val CONTROL_CENTER = "control_center"
    const val AVATAR_GALLERY = "avatar_gallery"
    const val SETTINGS_STORAGE_CACHE = "settings_storage_cache"
    const val SETTINGS_NETWORK = "settings_network"
    const val VEHICLE_CONNECTIVITY = "vehicle_connectivity"
    const val SETTINGS_APP_ICON = "settings_app_icon"
    const val SETTINGS_ASSISTANT_STARTUP = "settings_assistant_startup"
    const val PHONE_FINDER_SETTINGS = "phone_finder_settings"
    const val RESOURCE_MONITOR = "resource_monitor"
    const val SOCIAL_MEDIA_SEARCH = "social_media_search"
    const val BUSINESS_AUTOMATION = "business_automation"
    const val AI_MODELS = "ai_models"
    const val GEMINI_STUDIO = "gemini_studio"
    const val CALL_SUMMARIES = "call_summaries"
    const val MULTITASKING_CONTROL = "multitasking_control"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    userSettings: UserSettings
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                userSettings = userSettings,
                onSplashFinished = {
                    val targetRoute = if (userSettings.isOnboardingCompleted) Routes.HOME else Routes.WELCOME
                    navController.navigate(targetRoute) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.WELCOME) {
            WelcomeScreen(
                languageCode = userSettings.languageCode,
                onGetStarted = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                userSettings = userSettings,
                onNavigateToChat = { prompt ->
                    if (prompt != null) {
                        navController.navigate("${Routes.AI_CHAT}?prompt=$prompt")
                    } else {
                        navController.navigate(Routes.AI_CHAT)
                    }
                },
                onNavigateToVoice = { navController.navigate(Routes.VOICE_ASSISTANT) },
                onNavigateToVoiceVerify = { navController.navigate(Routes.VOICE_VERIFICATION) },
                onNavigateToTools = { navController.navigate(Routes.TOOLS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS_MAIN) },
                onNavigateToControlCenter = { navController.navigate(Routes.CONTROL_CENTER) },
                onNavigateToCallSummaries = { navController.navigate(Routes.CALL_SUMMARIES) },
                onNavigateToMultiTasking = { navController.navigate(Routes.MULTITASKING_CONTROL) }
            )
        }

        composable(Routes.CALL_SUMMARIES) {
            CallSummaryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MULTITASKING_CONTROL) {
            MultiTaskingControlScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${Routes.AI_CHAT}?prompt={prompt}",
            arguments = listOf(navArgument("prompt") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val initialPrompt = backStackEntry.arguments?.getString("prompt")
            AiChatScreen(
                userSettings = userSettings,
                initialPrompt = initialPrompt,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VOICE_ASSISTANT) {
            VoiceAssistantScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VOICE_VERIFICATION) {
            VoiceVerificationScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFaceEnrollment = { navController.navigate(Routes.FACE_ENROLLMENT) }
            )
        }

        composable(Routes.TOOLS) {
            ToolsScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.GEMINI_STUDIO) {
            GeminiStudioScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_MAIN) {
            SettingsMainScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToOwner = { navController.navigate(Routes.SETTINGS_OWNER) },
                onNavigateToTheme = { navController.navigate(Routes.SETTINGS_THEME) },
                onNavigateToLanguage = { navController.navigate(Routes.SETTINGS_LANGUAGE) },
                onNavigateToMemory = { navController.navigate(Routes.SETTINGS_MEMORY) },
                onNavigateToApi = { navController.navigate(Routes.SETTINGS_API) },
                onNavigateToPrivacy = { navController.navigate(Routes.PRIVACY_POLICY) },
                onNavigateToAbout = { navController.navigate(Routes.ABOUT) },
                onNavigateToAssistantIdentity = { navController.navigate(Routes.ASSISTANT_IDENTITY) },
                onNavigateToAppControl = { navController.navigate(Routes.APP_CONTROL) },
                onNavigateToSecurityDashboard = { navController.navigate(Routes.SECURITY_DASHBOARD) },
                onNavigateToFamilyProfiles = { navController.navigate(Routes.FAMILY_PROFILES) },
                onNavigateToCustomerCrm = { navController.navigate(Routes.CUSTOMER_CRM) },
                onNavigateToBirthdayManager = { navController.navigate(Routes.BIRTHDAY_MANAGER) },
                onNavigateToGallerySearch = { navController.navigate(Routes.GALLERY_SEARCH) },
                onNavigateToSmartHome = { navController.navigate(Routes.SMART_HOME) },
                onNavigateToStorageCache = { navController.navigate(Routes.SETTINGS_STORAGE_CACHE) },
                onNavigateToNetwork = { navController.navigate(Routes.SETTINGS_NETWORK) },
                onNavigateToVehicleConnectivity = { navController.navigate(Routes.VEHICLE_CONNECTIVITY) },
                onNavigateToAppIcon = { navController.navigate(Routes.SETTINGS_APP_ICON) },
                onNavigateToAssistantStartup = { navController.navigate(Routes.SETTINGS_ASSISTANT_STARTUP) },
                onNavigateToPhoneFinder = { navController.navigate(Routes.PHONE_FINDER_SETTINGS) },
                onNavigateToResourceMonitor = { navController.navigate(Routes.RESOURCE_MONITOR) }
            )
        }

        composable(Routes.SETTINGS_STORAGE_CACHE) {
            SettingsStorageCacheScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_NETWORK) {
            SettingsNetworkScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VEHICLE_CONNECTIVITY) {
            VehicleConnectivityScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_APP_ICON) {
            SettingsAppIconScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_ASSISTANT_STARTUP) {
            SettingsAssistantStartupScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CONTROL_CENTER) {
            ControlCenterScreen(
                onNavigateToRoute = { route -> navController.navigate(route) }
            )
        }

        composable(Routes.AVATAR_GALLERY) {
            AvatarGalleryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_OWNER) {
            SettingsOwnerScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFaceEnrollment = { navController.navigate(Routes.FACE_ENROLLMENT) },
                onNavigateToOwnerVerification = { navController.navigate(Routes.OWNER_VERIFICATION) }
            )
        }

        composable(Routes.FACE_ENROLLMENT) {
            FaceEnrollmentScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.OWNER_VERIFICATION) {
            OwnerVerificationScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() },
                onVerificationSuccess = { }
            )
        }

        composable(Routes.SETTINGS_THEME) {
            SettingsThemeScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_LANGUAGE) {
            SettingsLanguageScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_MEMORY) {
            SettingsMemoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS_API) {
            SettingsApiScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AI_MODELS) {
            SettingsApiScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.APP_CONTROL) {
            AppControlScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ASSISTANT_IDENTITY) {
            AssistantIdentityScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CAMERA_VISION) {
            CameraVisionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DEVICE_CARE) {
            DeviceCareScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SECURITY_DASHBOARD) {
            SecurityDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FAMILY_PROFILES) {
            FamilyProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CUSTOMER_CRM) {
            CustomerCrmScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.BUSINESS_AUTOMATION) {
            BusinessAutomationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.BIRTHDAY_MANAGER) {
            BirthdayScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.GALLERY_SEARCH) {
            GallerySearchScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SMART_HOME) {
            SmartHomeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PHONE_FINDER_SETTINGS) {
            PhoneFinderSettingsScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.RESOURCE_MONITOR) {
            AssistantResourceMonitorScreen(
                userSettings = userSettings,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SOCIAL_MEDIA_SEARCH) {
            SocialMediaSearchScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
