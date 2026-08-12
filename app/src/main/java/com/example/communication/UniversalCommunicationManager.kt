package com.example.communication

import android.content.Context

enum class CommunicationChannel {
    PHONE, SMS, WHATSAPP, INSTAGRAM, MESSENGER, TELEGRAM, EMAIL
}

data class MessagePayload(
    val channel: CommunicationChannel,
    val recipient: String,
    val textContent: String,
    val mediaUri: String? = null
)

data class CommunicationResult(
    val channel: CommunicationChannel,
    val isSuccess: Boolean,
    val statusMessage: String
)

interface ChannelAdapter {
    fun send(payload: MessagePayload): CommunicationResult
}

class PhoneAdapter(private val context: Context) : ChannelAdapter {
    override fun send(payload: MessagePayload): CommunicationResult {
        // Real intent: open the dialer with the number pre-filled. Requires the user to
        // press call, so we report "dialer opened" honestly rather than faking a connected call.
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                data = android.net.Uri.parse("tel:${payload.recipient}")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommunicationResult(CommunicationChannel.PHONE, true, "Phone dialer opened for ${payload.recipient}. Tap call to connect.")
        } catch (e: Exception) {
            CommunicationResult(CommunicationChannel.PHONE, false, "Could not open the dialer: ${e.message}")
        }
    }
}

class SmsAdapter(private val context: Context) : ChannelAdapter {
    override fun send(payload: MessagePayload): CommunicationResult {
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("smsto:${payload.recipient}")
                putExtra("sms_body", payload.textContent)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            CommunicationResult(CommunicationChannel.SMS, true, "SMS composer opened for ${payload.recipient}. Press send to deliver.")
        } catch (e: Exception) {
            CommunicationResult(CommunicationChannel.SMS, false, "Could not open SMS: ${e.message}")
        }
    }
}

class WhatsAppAdapter(private val context: Context) : ChannelAdapter {
    override fun send(payload: MessagePayload): CommunicationResult {
        // Real WhatsApp share intent. If WhatsApp is not installed, fall back to a plain
        // share chooser so the user can still pick an app — never fake a "message sent".
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, payload.textContent)
                setPackage("com.whatsapp")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
                CommunicationResult(CommunicationChannel.WHATSAPP, true, "WhatsApp opened. Tap send to deliver your message to ${payload.recipient}.")
            } catch (e: android.content.ActivityNotFoundException) {
                val fallback = android.content.Intent.createChooser(
                    android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, payload.textContent)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    }, "WhatsApp not installed — choose another app"
                ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(fallback)
                CommunicationResult(CommunicationChannel.WHATSAPP, false, "WhatsApp is not installed. Opened the share sheet so you can pick another app.")
            }
        } catch (e: Exception) {
            CommunicationResult(CommunicationChannel.WHATSAPP, false, "Could not open WhatsApp: ${e.message}")
        }
    }
}

class InstagramAdapter(private val context: Context) : ChannelAdapter {
    override fun send(payload: MessagePayload): CommunicationResult {
        // Instagram has no public direct-DM intent. Open the app profile/share; do not claim a DM was sent.
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, payload.textContent)
                setPackage("com.instagram.android")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
                CommunicationResult(CommunicationChannel.INSTAGRAM, true, "Instagram opened. Note: Android does not expose a direct-DM intent, so please send the message manually inside the app.")
            } catch (e: android.content.ActivityNotFoundException) {
                CommunicationResult(CommunicationChannel.INSTAGRAM, false, "Instagram is not installed.")
            }
        } catch (e: Exception) {
            CommunicationResult(CommunicationChannel.INSTAGRAM, false, "Could not open Instagram: ${e.message}")
        }
    }
}

class EmailAdapter(private val context: Context) : ChannelAdapter {
    override fun send(payload: MessagePayload): CommunicationResult {
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                data = android.net.Uri.parse("mailto:${payload.recipient}")
                putExtra(android.content.Intent.EXTRA_TEXT, payload.textContent)
                putExtra(android.content.Intent.EXTRA_SUBJECT, "From Snaper Assistant")
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(android.content.Intent.createChooser(intent, "Send email").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
            CommunicationResult(CommunicationChannel.EMAIL, true, "Email composer opened for ${payload.recipient}. Press send to deliver.")
        } catch (e: Exception) {
            CommunicationResult(CommunicationChannel.EMAIL, false, "Could not open email: ${e.message}")
        }
    }
}

/**
 * Universal Multi-Channel Communication Manager orchestrating messages across Phone, SMS, WhatsApp, Instagram, Email.
 */
class UniversalCommunicationManager(private val context: Context) {

    private val adapters = mapOf(
        CommunicationChannel.PHONE to PhoneAdapter(context),
        CommunicationChannel.SMS to SmsAdapter(context),
        CommunicationChannel.WHATSAPP to WhatsAppAdapter(context),
        CommunicationChannel.INSTAGRAM to InstagramAdapter(context),
        CommunicationChannel.EMAIL to EmailAdapter(context)
    )

    fun dispatchMessage(payload: MessagePayload): CommunicationResult {
        val adapter = adapters[payload.channel] ?: SmsAdapter(context)
        return adapter.send(payload)
    }
}
