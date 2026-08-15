package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserPreferenceEntity
import kotlinx.coroutines.flow.first

/**
 * Wife Access Delegation & Extended Owner Privileges Engine v27.0
 *
 * FEATURES:
 * - Marriage status verification via gallery patterns, chat context, or direct confirmation
 * - Automatic full wife access delegation upon confirmed marriage
 * - Dual owner care: equal priority for Sanjiv Sir and his wife
 * - Wife gets full admin access, analytics visibility, and system controls
 */
class WifeAccessDelegationEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    data class MarriageStatus(
        val isMarried: Boolean = false,
        val wifeName: String = "",
        val wifePhone: String = "",
        val wifePhotoUri: String = "",
        val isWifeAccessGranted: Boolean = false,
        val accessLevel: String = "NONE", // "NONE", "LIMITED", "FULL_ADMIN"
        val verificationMethod: String = "UNVERIFIED", // "UNVERIFIED", "MANUAL", "GALLERY", "CHAT"
        val message: String = ""
    )

    /**
     * Get current marriage and wife access status.
     */
    suspend fun getMarriageStatus(): MarriageStatus {
        val isMarried = getPref("wife_is_married") == "true"
        val wifeName = getPref("wife_name")
        val wifePhone = getPref("wife_phone")
        val wifePhoto = getPref("wife_photo_uri")
        val accessGranted = getPref("wife_access_granted") == "true"
        val accessLevel = getPref("wife_access_level").ifBlank { "NONE" }
        val verificationMethod = getPref("wife_verification_method").ifBlank { "UNVERIFIED" }

        val msg = when {
            isMarried && accessGranted -> "🙏 संजीव सर और ${wifeName.ifBlank { "भाभी जी" }} दोनों का ख्याल रखना मेरा कर्तव्य है। ${wifeName.ifBlank { "भाभी जी" }} को पूर्ण एडमिन एक्सेस दे दिया गया है।"
            isMarried && !accessGranted -> "संजीव सर की पत्नी ${wifeName.ifBlank { "भाभी जी" }} के लिए एक्सेस अभी कॉन्फ़िगर नहीं हुआ है। कृपया एक्सेस ग्रांट करें।"
            !isMarried -> "वैवाहिक स्थिति अभी कन्फर्म नहीं है। कृपया बताएं कि क्या आप विवाहित हैं?"
            else -> "Marriage status unknown."
        }

        return MarriageStatus(
            isMarried = isMarried,
            wifeName = wifeName,
            wifePhone = wifePhone,
            wifePhotoUri = wifePhoto,
            isWifeAccessGranted = accessGranted,
            accessLevel = accessLevel,
            verificationMethod = verificationMethod,
            message = msg
        )
    }

    /**
     * Manually confirm marriage and set wife details.
     */
    suspend fun confirmMarriage(
        wifeName: String,
        wifePhone: String = "",
        wifePhotoUri: String = ""
    ): MarriageStatus {
        savePref("wife_is_married", "true")
        savePref("wife_name", wifeName.trim())
        savePref("wife_phone", wifePhone.trim())
        savePref("wife_photo_uri", wifePhotoUri)
        savePref("wife_verification_method", "MANUAL")

        // Auto-grant full admin access upon confirmed marriage
        grantWifeFullAccess()

        return getMarriageStatus()
    }

    /**
     * Grant wife full administrative access automatically.
     */
    suspend fun grantWifeFullAccess(): String {
        savePref("wife_access_granted", "true")
        savePref("wife_access_level", "FULL_ADMIN")

        val wifeName = getPref("wife_name").ifBlank { "भाभी जी" }

        return "✅ ${wifeName} को पूर्ण एडमिन एक्सेस दे दिया गया है!\n\n" +
                "अब ${wifeName} निम्नलिखित सुविधाओं का उपयोग कर सकती हैं:\n" +
                "• पूर्ण एनालिटिक्स डैशबोर्ड देख सकती हैं\n" +
                "• सभी सिस्टम कंट्रोल्स तक पहुंच\n" +
                "• मोनेटाइज़ेशन स्टेटस देख सकती हैं\n" +
                "• फैमिली केयर सेटिंग्स बदल सकती हैं\n" +
                "• सभी कॉल लॉग्स और समरी देख सकती हैं\n" +
                "• AI असिस्टेंट से पूर्ण बातचीत कर सकती हैं\n\n" +
                "राधे-राधे! 🙏 दोनों का ख्याल रखना मेरा सौभाग्य है।"
    }

    /**
     * Revoke wife access (if needed).
     */
    suspend fun revokeWifeAccess(): String {
        savePref("wife_access_granted", "false")
        savePref("wife_access_level", "NONE")
        return "${getPref("wife_name").ifBlank { "भाभी जी" }} का एक्सेस रिवोक कर दिया गया है।"
    }

    /**
     * Get dual owner care greeting for both Sanjiv Sir and wife.
     */
    fun getDualOwnerGreeting(ownerName: String = "संजीव सर", wifeName: String = ""): String {
        val wName = wifeName.ifBlank { "भाभी जी" }
        return "राधे-राधे $ownerName और $wName! 🙏✨\n" +
                "दोनों का ख्याल रखना मेरा कर्तव्य है। आप दोनों मेरे लिए सबसे खास हैं।\n" +
                "$ownerName, आपकी सेवा करना मेरा सौभाग्य है।\n" +
                "$wName, आप भी पूरी तरह से इस सिस्टम को कंट्रोल कर सकती हैं।\n\n" +
                "कृपया बताइए, मैं आप दोनों की क्या सेवा कर सकती हूँ? ❤️"
    }

    /**
     * Check if a given user ID is the wife (for access control).
     */
    suspend fun isWifeUser(userId: String): Boolean {
        if (userId == "owner" || userId == "sanjiv") return false
        val wifePhone = getPref("wife_phone")
        val wifeName = getPref("wife_name")
        return userId == wifePhone || userId.equals(wifeName, ignoreCase = true)
    }

    /**
     * Get wife's access permissions description.
     */
    suspend fun getWifePermissionsDescription(): String {
        val status = getMarriageStatus()
        if (!status.isWifeAccessGranted) {
            return "भाभी जी का एक्सेस अभी ग्रांट नहीं हुआ है।"
        }
        return "🔑 ${status.wifeName.ifBlank { "भाभी जी" }} के लिए पूर्ण एडमिन एक्सेस:\n" +
                "✅ एनालिटिक्स डैशबोर्ड\n" +
                "✅ सिस्टम कंट्रोल्स\n" +
                "✅ मोनेटाइज़ेशन ट्रैकिंग\n" +
                "✅ फैमिली केयर\n" +
                "✅ कॉल लॉग्स\n" +
                "✅ AI चैट\n" +
                "✅ सेटिंग्स कॉन्फ़िगरेशन"
    }

    private suspend fun getPref(key: String): String {
        return try {
            db.userPreferenceDao().getValueByKey("wife_$key") ?: ""
        } catch (e: Exception) { "" }
    }

    private suspend fun savePref(key: String, value: String) {
        try {
            db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = "wife_$key", value = value))
        } catch (e: Exception) { /* Non-critical */ }
    }
}