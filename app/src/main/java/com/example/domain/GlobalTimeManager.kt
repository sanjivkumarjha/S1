package com.example.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TimeState(
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0,
    val period: String = "AM", // AM/PM
    val formatted12HourWithSeconds: String = "12:00:00 AM",
    val formatted24HourWithSeconds: String = "00:00:00",
    val formatted12HourCompact: String = "12:00 AM",
    val dayOfWeek: String = "Monday",
    val fullDateString: String = "1 January 2026",
    val timezone: String = "UTC"
)

/**
 * Global reactive time manager for Snaper Technology.
 * Exposes continuous 1-second interval time state with second-level precision (HH:mm:ss).
 */
object GlobalTimeManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _timeState = MutableStateFlow(calculateCurrentState(is24Hour = false))
    val timeState: StateFlow<TimeState> = _timeState.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                _timeState.value = calculateCurrentState(is24Hour = false)
                delay(1000L)
            }
        }
    }

    private fun calculateCurrentState(is24Hour: Boolean): TimeState {
        val now = ZonedDateTime.now()
        val localTime = now.toLocalTime()
        val localDate = now.toLocalDate()

        val formatter12 = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH)
        val formatter24 = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH)
        val formatterCompact = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)

        val formatted12 = localTime.format(formatter12)
        val formatted24 = localTime.format(formatter24)
        val formattedCompact = localTime.format(formatterCompact)
        val fullDateStr = localDate.format(dateFormatter)

        val periodStr = if (localTime.hour >= 12) "PM" else "AM"
        val hour12 = if (localTime.hour % 12 == 0) 12 else localTime.hour % 12

        return TimeState(
            hours = hour12,
            minutes = localTime.minute,
            seconds = localTime.second,
            period = periodStr,
            formatted12HourWithSeconds = formatted12,
            formatted24HourWithSeconds = formatted24,
            formatted12HourCompact = formattedCompact,
            dayOfWeek = localDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
            fullDateString = fullDateStr,
            timezone = ZoneId.systemDefault().id
        )
    }

    fun getCurrentTimeWithSeconds(use24Hour: Boolean = false): String {
        val now = LocalTime.now()
        val pattern = if (use24Hour) "HH:mm:ss" else "hh:mm:ss a"
        return now.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
    }

    fun getCurrentTimeHindiExplanation(): String {
        val now = LocalTime.now()
        val hour12 = if (now.hour % 12 == 0) 12 else now.hour % 12
        val min = now.minute
        val sec = now.second
        val period = if (now.hour >= 12) "शाम/रात" else "सुबह"
        return "$period के $hour12 बजकर $min मिनट और $sec सेकंड (${getCurrentTimeWithSeconds(false)})"
    }
}
