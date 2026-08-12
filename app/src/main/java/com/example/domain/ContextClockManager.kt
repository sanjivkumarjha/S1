package com.example.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Manages device local clock context, date transitions, timezones, and calendar days.
 */
object ContextClockManager {

    fun getCurrentDate(): LocalDate {
        return LocalDate.now()
    }

    fun getCurrentDateFormatted(): String {
        val now = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
        return now.format(formatter)
    }

    fun getCurrentTimeFormatted(): String {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("hh:mm a")
        return now.format(formatter)
    }

    fun getTimeZoneId(): String {
        return ZoneId.systemDefault().id
    }

    fun getDateKey(): String {
        return LocalDate.now().toString() // "YYYY-MM-DD"
    }

    fun hasDayChanged(lastDateKey: String?): Boolean {
        if (lastDateKey.isNullOrBlank()) return true
        return getDateKey() != lastDateKey
    }
}
