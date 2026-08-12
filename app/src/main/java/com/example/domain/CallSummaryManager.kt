package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CallSummaryEntity
import com.example.data.local.entities.ReminderEntity
import kotlinx.coroutines.flow.Flow

class CallSummaryManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val callSummaryDao = db.callSummaryDao()
    private val reminderDao = db.reminderDao()

    val allSummaries: Flow<List<CallSummaryEntity>> = callSummaryDao.getAllCallSummaries()

    suspend fun saveCallSummary(
        callerName: String,
        callerPhone: String = "",
        purpose: String,
        importantPoints: String,
        requestedAction: String = "",
        followUpDate: String = "",
        summaryHindi: String = "",
        summaryEnglish: String = "",
        transcript: String = ""
    ): Long {
        val formattedHindi = summaryHindi.ifBlank {
            "कॉलर: $callerName | उद्देश्य: $purpose | मुख्य बिंदु: $importantPoints | आवश्यक कार्यवाही: ${requestedAction.ifBlank { "कोई नहीं" }}"
        }
        val formattedEnglish = summaryEnglish.ifBlank {
            "Caller: $callerName | Purpose: $purpose | Key Points: $importantPoints | Requested Action: ${requestedAction.ifBlank { "None" }}"
        }

        val summary = CallSummaryEntity(
            callerName = callerName,
            callerPhone = callerPhone,
            purpose = purpose,
            importantPoints = importantPoints,
            requestedAction = requestedAction,
            followUpDate = followUpDate,
            summaryHindi = formattedHindi,
            summaryEnglish = formattedEnglish,
            transcript = transcript,
            timestamp = System.currentTimeMillis()
        )
        val id = callSummaryDao.insertCallSummary(summary)

        // Automatically create reminder if requested action or follow-up date exists
        if (requestedAction.isNotBlank() || followUpDate.isNotBlank()) {
            val reminderTitle = "Follow-up Call with $callerName"
            val reminderDesc = if (requestedAction.isNotBlank()) requestedAction else purpose
            reminderDao.insertReminder(
                ReminderEntity(
                    title = reminderTitle,
                    description = reminderDesc,
                    timeMillis = System.currentTimeMillis() + (24 * 3600 * 1000L) // Default tomorrow
                )
            )
        }

        return id
    }

    suspend fun deleteSummary(id: Long) {
        callSummaryDao.deleteCallSummaryById(id)
    }

    suspend fun clearAllSummaries() {
        callSummaryDao.clearAllCallSummaries()
    }

    fun getCallDisclosureNotice(): String {
        return "राधे राधे। मैं Snaper Technology की assistant हूँ। यह बातचीत owner-configured call assistance के अनुसार संभाली जा रही है।"
    }
}
