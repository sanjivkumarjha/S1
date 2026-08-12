package com.example.domain

import java.util.Calendar

/**
 * Smart Greeting Engine for Snaper Technology.
 * Generates personalized, time-aware greetings combining "राधे राधे",
 * configured owner title (e.g. "संजिव सर"), and time of day (Good Morning/Afternoon/Evening/Night).
 */
object GreetingEngine {

    fun getTimeBasedSalutation(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    fun getTimeBasedSalutationHindi(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "शुभ प्रभात"
            in 12..16 -> "शुभ दोपहर"
            in 17..21 -> "शुभ संध्या"
            else -> "शुभ रात्रि"
        }
    }

    fun getFullGreeting(ownerName: String = "संजिव सर", ownerTitle: String = "Boss"): String {
        val name = if (ownerName.isNotBlank() && ownerName != "User") ownerName else "संजिव सर"
        val salutation = getTimeBasedSalutation()
        return "राधे राधे, $name ❤️ \n$salutation"
    }

    fun getFullGreetingCardText(ownerName: String = "संजिव सर"): String {
        val name = if (ownerName.isNotBlank() && ownerName != "User") ownerName else "संजिव सर"
        val timeGreeting = getTimeBasedSalutation()
        val timeWithSec = GlobalTimeManager.getCurrentTimeWithSeconds(false)
        return "राधे राधे, $name ✨\n$timeGreeting • $timeWithSec"
    }
}
