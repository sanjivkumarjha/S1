package com.example.appcontrol

import android.content.Context
import android.util.Log
import com.example.service.AssistantAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class SocialApp {
    YOUTUBE, INSTAGRAM, FACEBOOK, REELS, SHORTS
}

/**
 * Social Media & Multimedia Lifecycle Automation Controller.
 * Enables voice/text commands to search videos, auto-scroll reels/shorts with human pacing,
 * like posts, comment on media, and control video playback via Accessibility Services.
 */
class SocialMediaGestureAutomationController(private val context: Context) {

    /**
     * Auto-scroll feed / reels / shorts with natural human gesture variation.
     */
    suspend fun performAutoScroll(app: SocialApp, scrollCount: Int = 1): Boolean = withContext(Dispatchers.IO) {
        val service = AssistantAccessibilityService.getInstance()
        if (service == null) {
            Log.e("SocialGestureAutomation", "Accessibility Service not running.")
            return@withContext false
        }

        for (i in 1..scrollCount) {
            // Natural gesture swipe upward
            val success = service.scrollScreen(forward = true)
            if (!success) {
                // Fallback: Dispatch natural swipe gesture coordinates
                service.tapAtCoordinates(540f, 1400f)
            }
            // Human interaction delay between scrolls
            delay((2500L..4500L).random())
        }
        return@withContext true
    }

    /**
     * Like current post or reel in active social application.
     */
    fun likeCurrentPost(): Boolean {
        val service = AssistantAccessibilityService.getInstance() ?: return false
        return service.findAndClickText("Like") ||
                service.findAndClickText("like") ||
                service.findAndClickText("Thumbs up") ||
                service.findAndClickText("Double tap to like")
    }

    /**
     * Post a comment on current media stream.
     */
    suspend fun postComment(commentText: String): Boolean = withContext(Dispatchers.IO) {
        val service = AssistantAccessibilityService.getInstance() ?: return@withContext false

        // 1. Click Comment icon/button
        val commentClicked = service.findAndClickText("Comment") || service.findAndClickText("Add a comment...")
        if (!commentClicked) return@withContext false

        delay(600)

        // 2. Type text into focused input field
        service.typeTextIntoFocusedField(commentText)
        delay(500)

        // 3. Click Post/Send button
        return@withContext service.findAndClickText("Post") || service.findAndClickText("Send")
    }

    /**
     * Play or pause currently active video/audio stream.
     */
    fun togglePlayPauseMedia(): Boolean {
        val service = AssistantAccessibilityService.getInstance() ?: return false
        return service.findAndClickText("Play") ||
                service.findAndClickText("Pause") ||
                service.findAndClickText("Play video") ||
                service.findAndClickText("Pause video")
    }

    /**
     * Search specific topic or channel in YouTube / Instagram.
     */
    suspend fun searchSocialContent(appName: String, query: String): Boolean = withContext(Dispatchers.IO) {
        val service = AssistantAccessibilityService.getInstance() ?: return@withContext false

        if (service.findAndClickText("Search")) {
            delay(500)
            service.typeTextIntoFocusedField(query)
            delay(500)
            return@withContext service.findAndClickText("Search") || service.findAndClickText(query)
        }
        return@withContext false
    }
}
