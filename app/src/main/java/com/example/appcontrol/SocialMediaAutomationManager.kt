package com.example.appcontrol

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import java.net.URLEncoder

enum class SocialPlatform(
    val id: String,
    val displayName: String,
    val packageName: String,
    val webHost: String,
    val iconName: String
) {
    WHATSAPP("whatsapp", "WhatsApp", "com.whatsapp", "https://web.whatsapp.com", "ic_whatsapp"),
    INSTAGRAM("instagram", "Instagram", "com.instagram.android", "https://www.instagram.com", "ic_instagram"),
    YOUTUBE("youtube", "YouTube", "com.google.android.youtube", "https://www.youtube.com", "ic_youtube"),
    FACEBOOK("facebook", "Facebook", "com.facebook.katana", "https://www.facebook.com", "ic_facebook"),
    TWITTER("twitter", "X / Twitter", "com.twitter.android", "https://twitter.com", "ic_twitter"),
    TELEGRAM("telegram", "Telegram", "org.telegram.messenger", "https://t.me", "ic_telegram"),
    LINKEDIN("linkedin", "LinkedIn", "com.linkedin.android", "https://www.linkedin.com", "ic_linkedin"),
    SNAPCHAT("snapchat", "Snapchat", "com.snapchat.android", "https://www.snapchat.com", "ic_snapchat"),
    PINTEREST("pinterest", "Pinterest", "com.pinterest", "https://www.pinterest.com", "ic_pinterest"),
    REDDIT("reddit", "Reddit", "com.reddit.frontpage", "https://www.reddit.com", "ic_reddit"),
    TIKTOK("tiktok", "TikTok", "com.zhiliaoapp.musically", "https://www.tiktok.com", "ic_tiktok")
}

data class SocialActionResult(
    val success: Boolean,
    val platform: SocialPlatform,
    val actionType: String,
    val message: String,
    val launchedUri: String? = null
)

class SocialMediaAutomationManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val permissionManager = SocialAutomationPermissionManager(context)

    fun isAppInstalled(platform: SocialPlatform): Boolean {
        return try {
            packageManager.getPackageInfo(platform.packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Executes deep-search query on specified social media platform with native intent or web fallback.
     */
    fun searchPlatform(platform: SocialPlatform, query: String): SocialActionResult {
        val permissions = permissionManager.getPermissionState()
        if (!permissions.socialSearchEnabled) {
            return SocialActionResult(
                false, platform, "search",
                "Social search permission is disabled in settings. Please grant permission to search."
            )
        }

        val cleanQuery = query.trim()
        val encodedQuery = Uri.encode(cleanQuery)

        val (intent, targetUri) = when (platform) {
            SocialPlatform.WHATSAPP -> {
                val uri = Uri.parse("https://api.whatsapp.com/send?text=$encodedQuery")
                val i = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(platform.packageName)
                }
                Pair(i, uri.toString())
            }

            SocialPlatform.INSTAGRAM -> {
                val nativeUri = Uri.parse("instagram://tag?name=$encodedQuery")
                val webUri = Uri.parse("https://www.instagram.com/explore/search/keyword/?q=$encodedQuery")
                val i = Intent(Intent.ACTION_VIEW, nativeUri).apply {
                    setPackage(platform.packageName)
                }
                if (i.resolveActivity(packageManager) != null) {
                    Pair(i, nativeUri.toString())
                } else {
                    Pair(Intent(Intent.ACTION_VIEW, webUri), webUri.toString())
                }
            }

            SocialPlatform.YOUTUBE -> {
                val nativeUri = Uri.parse("vnd.youtube:results?search_query=$encodedQuery")
                val webUri = Uri.parse("https://www.youtube.com/results?search_query=$encodedQuery")
                val i = Intent(Intent.ACTION_SEARCH).apply {
                    setPackage(platform.packageName)
                    putExtra("query", cleanQuery)
                }
                if (i.resolveActivity(packageManager) != null) {
                    Pair(i, nativeUri.toString())
                } else {
                    Pair(Intent(Intent.ACTION_VIEW, webUri), webUri.toString())
                }
            }

            SocialPlatform.FACEBOOK -> {
                val nativeUri = Uri.parse("fb://search/top/?q=$encodedQuery")
                val webUri = Uri.parse("https://www.facebook.com/search/top/?q=$encodedQuery")
                val i = Intent(Intent.ACTION_VIEW, nativeUri).apply {
                    setPackage(platform.packageName)
                }
                if (i.resolveActivity(packageManager) != null) {
                    Pair(i, nativeUri.toString())
                } else {
                    Pair(Intent(Intent.ACTION_VIEW, webUri), webUri.toString())
                }
            }

            SocialPlatform.TWITTER -> {
                val nativeUri = Uri.parse("twitter://search?query=$encodedQuery")
                val webUri = Uri.parse("https://twitter.com/search?q=$encodedQuery")
                val i = Intent(Intent.ACTION_VIEW, nativeUri).apply {
                    setPackage(platform.packageName)
                }
                if (i.resolveActivity(packageManager) != null) {
                    Pair(i, nativeUri.toString())
                } else {
                    Pair(Intent(Intent.ACTION_VIEW, webUri), webUri.toString())
                }
            }

            SocialPlatform.TELEGRAM -> {
                val uri = Uri.parse("https://t.me/s/$encodedQuery")
                val i = Intent(Intent.ACTION_VIEW, uri)
                Pair(i, uri.toString())
            }

            SocialPlatform.LINKEDIN -> {
                val webUri = Uri.parse("https://www.linkedin.com/search/results/all/?keywords=$encodedQuery")
                Pair(Intent(Intent.ACTION_VIEW, webUri), webUri.toString())
            }

            SocialPlatform.SNAPCHAT -> {
                val nativeUri = Uri.parse("snapchat://")
                val webUri = Uri.parse("https://www.snapchat.com")
                Pair(Intent(Intent.ACTION_VIEW, nativeUri), nativeUri.toString())
            }

            SocialPlatform.PINTEREST -> {
                val nativeUri = Uri.parse("pinterest://search/pins/?q=$encodedQuery")
                val webUri = Uri.parse("https://www.pinterest.com/search/pins/?q=$encodedQuery")
                val i = Intent(Intent.ACTION_VIEW, nativeUri).apply {
                    setPackage(platform.packageName)
                }
                if (i.resolveActivity(packageManager) != null) {
                    Pair(i, nativeUri.toString())
                } else {
                    Pair(Intent(Intent.ACTION_VIEW, webUri), webUri.toString())
                }
            }

            SocialPlatform.REDDIT -> {
                val webUri = Uri.parse("https://www.reddit.com/search/?q=$encodedQuery")
                Pair(Intent(Intent.ACTION_VIEW, webUri), webUri.toString())
            }

            SocialPlatform.TIKTOK -> {
                val nativeUri = Uri.parse("snssdk1128://search?keyword=$encodedQuery")
                val webUri = Uri.parse("https://www.tiktok.com/search?q=$encodedQuery")
                val i = Intent(Intent.ACTION_VIEW, nativeUri).apply {
                    setPackage(platform.packageName)
                }
                if (i.resolveActivity(packageManager) != null) {
                    Pair(i, nativeUri.toString())
                } else {
                    Pair(Intent(Intent.ACTION_VIEW, webUri), webUri.toString())
                }
            }
        }

        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            SocialActionResult(
                true, platform, "search",
                "Deep search launched on ${platform.displayName} for '$cleanQuery' ✨",
                targetUri
            )
        } catch (e: Exception) {
            // Web browser fallback
            try {
                val fallbackUri = Uri.parse("${platform.webHost}?q=$encodedQuery")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                SocialActionResult(
                    true, platform, "search_fallback",
                    "Launched browser search for ${platform.displayName} for '$cleanQuery' 🌐",
                    fallbackUri.toString()
                )
            } catch (ex: Exception) {
                SocialActionResult(
                    false, platform, "search_error",
                    "Could not launch search for ${platform.displayName}: ${ex.localizedMessage}"
                )
            }
        }
    }

    /**
     * Send direct message or chat initiation with contact on supported platforms.
     */
    fun sendDirectMessage(platform: SocialPlatform, recipient: String, messageText: String): SocialActionResult {
        val permissions = permissionManager.getPermissionState()
        if (!permissions.autoMessagingConsent) {
            return SocialActionResult(
                false, platform, "send_message",
                "Auto-messaging consent is turned off. Please enable it in Settings before sending messages."
            )
        }

        val encodedText = Uri.encode(messageText)
        val cleanRecipient = recipient.replace("+", "").replace(" ", "").trim()

        return when (platform) {
            SocialPlatform.WHATSAPP -> {
                val uri = if (cleanRecipient.isNotBlank()) {
                    Uri.parse("https://api.whatsapp.com/send?phone=$cleanRecipient&text=$encodedText")
                } else {
                    Uri.parse("https://api.whatsapp.com/send?text=$encodedText")
                }
                launchUriIntent(platform, uri, "Messaging $recipient on WhatsApp ✨")
            }

            SocialPlatform.TELEGRAM -> {
                val uri = Uri.parse("https://t.me/$cleanRecipient")
                launchUriIntent(platform, uri, "Opening chat with $recipient on Telegram ✨")
            }

            SocialPlatform.INSTAGRAM -> {
                val uri = Uri.parse("instagram://user?username=$cleanRecipient")
                launchUriIntent(platform, uri, "Opening $recipient's Instagram profile to DM ✨")
            }

            else -> {
                searchPlatform(platform, "$recipient $messageText")
            }
        }
    }

    /**
     * Search and play video/shorts/reels on YouTube, Instagram, Facebook, TikTok.
     */
    fun searchAndPlayReelOrVideo(platform: SocialPlatform, videoTopic: String): SocialActionResult {
        val cleanTopic = videoTopic.trim()
        val encodedTopic = Uri.encode(cleanTopic)

        val uri = when (platform) {
            SocialPlatform.YOUTUBE -> Uri.parse("https://www.youtube.com/hashtag/$encodedTopic")
            SocialPlatform.INSTAGRAM -> Uri.parse("https://www.instagram.com/reels/videos/$encodedTopic/")
            SocialPlatform.FACEBOOK -> Uri.parse("https://www.facebook.com/reels/")
            SocialPlatform.TIKTOK -> Uri.parse("https://www.tiktok.com/tag/$encodedTopic")
            else -> Uri.parse("${platform.webHost}/search?q=$encodedTopic")
        }

        return launchUriIntent(platform, uri, "Playing reels/videos for '$cleanTopic' on ${platform.displayName} 🎬")
    }

    /**
     * Multi-platform cross search launcher.
     */
    fun multiPlatformCrossSearch(query: String, selectedPlatforms: List<SocialPlatform>): List<SocialActionResult> {
        val results = mutableListOf<SocialActionResult>()
        for (platform in selectedPlatforms) {
            results.add(searchPlatform(platform, query))
        }
        return results
    }

    private fun launchUriIntent(platform: SocialPlatform, uri: Uri, successMsg: String): SocialActionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            SocialActionResult(true, platform, "launch_uri", successMsg, uri.toString())
        } catch (e: Exception) {
            SocialActionResult(false, platform, "launch_error", "Failed to launch ${platform.displayName}: ${e.message}")
        }
    }
}
