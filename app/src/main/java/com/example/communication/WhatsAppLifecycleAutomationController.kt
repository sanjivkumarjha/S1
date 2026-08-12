package com.example.communication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.service.AssistantAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WhatsApp Full Lifecycle Automation Controller.
 * Executes native communication workflows: searching contacts, sending text messages,
 * handling media attachments, starting voice/video calls, and reading incoming chats
 * in real-time via AccessibilityService & NotificationListeners.
 */
class WhatsAppLifecycleAutomationController(private val context: Context) {

    /**
     * Send WhatsApp message directly via Intent URL or AccessibilityService node clicks.
     */
    suspend fun sendWhatsAppMessage(phoneNumber: String, messageText: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleanNum = phoneNumber.replace(Regex("[^0-9]"), "")
            val encodedMsg = Uri.encode(messageText)
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNum&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)

            // Apply natural interaction pacing before auto-clicking send button
            HumanInteractionPacingEngine.applyNaturalPacingDelay(800L, 200L)

            val service = AssistantAccessibilityService.getInstance()
            if (service != null) {
                // Click the send button naturally in WhatsApp UI
                service.findAndClickText("Send") || service.findAndClickText("send")
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e("WhatsAppAutomation", "Error opening WhatsApp intent: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Initiate WhatsApp Voice or Video Call to contact.
     */
    suspend fun startWhatsAppCall(contactName: String, isVideo: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val service = AssistantAccessibilityService.getInstance() ?: return@withContext false

            // 1. Launch WhatsApp
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            } ?: return@withContext false
            context.startActivity(launchIntent)

            HumanInteractionPacingEngine.applyNaturalPacingDelay(1000L, 300L)

            // 2. Click Search and type contact name
            if (service.findAndClickText("Search")) {
                HumanInteractionPacingEngine.applyNaturalPacingDelay(400L, 100L)
                service.typeTextIntoFocusedField(contactName)
                HumanInteractionPacingEngine.applyNaturalPacingDelay(600L, 200L)

                // 3. Open contact chat
                service.findAndClickText(contactName)
                HumanInteractionPacingEngine.applyNaturalPacingDelay(800L, 200L)

                // 4. Trigger Voice or Video Call icon
                val targetAction = if (isVideo) "Video call" else "Voice call"
                return@withContext service.findAndClickText(targetAction)
            }
            return@withContext false
        } catch (e: Exception) {
            Log.e("WhatsAppAutomation", "Error initiating WhatsApp call: ${e.message}")
            return@withContext false
        }
    }

    /**
     * Read currently active WhatsApp chat text on screen.
     */
    fun readActiveWhatsAppChat(): String {
        val service = AssistantAccessibilityService.getInstance() ?: return "Accessibility Service inactive."
        val currentPkg = AssistantAccessibilityService.currentPackageName.value
        if (!currentPkg.contains("whatsapp", ignoreCase = true)) {
            return "WhatsApp is not currently open on active screen."
        }
        return service.readScreenText()
    }

    /**
     * Share file/media attachment via WhatsApp.
     */
    fun shareMediaFileWhatsApp(fileUri: Uri, mimeType: String = "image/*"): Boolean {
        return try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                setPackage("com.whatsapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(shareIntent)
            true
        } catch (e: Exception) {
            Log.e("WhatsAppAutomation", "Failed to share media: ${e.message}")
            false
        }
    }
}
