package com.example.domain

import android.content.Context
import java.util.concurrent.ConcurrentHashMap

/**
 * Permanent Core System "राधे राधे" Greeting Engine.
 * Application logic controls the mandatory greeting behavior independently of AI model prompts.
 * Automatically resets daily based on local calendar date.
 */
object RadheRadheGreetingManager {

    private const val CORE_GREETING = "राधे राधे"
    
    // Maps userId/profileId -> Last Date Key ("YYYY-MM-DD")
    private val userGreetingMap = ConcurrentHashMap<String, String>()

    fun shouldGreetUserToday(userId: String = "owner"): Boolean {
        val currentDateKey = ContextClockManager.getDateKey()
        val lastGreetedDate = userGreetingMap[userId]
        return ContextClockManager.hasDayChanged(lastGreetedDate)
    }

    fun markUserGreetedToday(userId: String = "owner") {
        userGreetingMap[userId] = ContextClockManager.getDateKey()
    }

    /**
     * Formats output response with mandatory "राधे राधे" prefix if it's the user's first interaction today.
     */
    fun processGreeting(userId: String = "owner", ownerTitle: String = "Boss", responseText: String): String {
        if (shouldGreetUserToday(userId)) {
            markUserGreetedToday(userId)
            val title = if (ownerTitle.isNotBlank()) ownerTitle else "Boss"
            if (!responseText.contains(CORE_GREETING)) {
                return "$CORE_GREETING, $title. $responseText"
            }
        }
        return responseText
    }

    /**
     * Gets mandatory call greeting text for incoming/outgoing answered call assistance.
     */
    fun getCallGreeting(assistantName: String = "Snaper Technology"): String {
        return "$CORE_GREETING। नमस्ते, मैं $assistantName की personal assistant हूँ। आप किससे बात करना चाहते हैं?"
    }

    /**
     * Core greeting text constant.
     */
    fun getCoreGreeting(): String = CORE_GREETING
}
