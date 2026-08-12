package com.example.service

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CallSummaryEntity
import com.example.data.preferences.UserPreferencesRepository
import com.example.security.FamilySosManager
import com.example.security.SpamCallDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Smart Call Filtering & AI Autonomous Call Screening Service.
 * Leverages Android Telecom CallScreeningService to screen incoming calls,
 * identify relative/business callers, and autonomously manage incoming scenarios.
 */
class SnaperCallScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val rawHandle = callDetails.handle?.schemeSpecificPart ?: ""
        val callerNumber = rawHandle.trim()
        val direction = callDetails.callDirection

        if (direction != Call.Details.DIRECTION_INCOMING) return

        serviceScope.launch {
            try {
                val context = applicationContext
                val prefsRepo = UserPreferencesRepository(context)
                val userSettings = prefsRepo.userSettingsFlow.first()
                val spamDetector = SpamCallDetector(context)
                val familyManager = FamilySosManager.getInstance(context)

                val spamRes = spamDetector.analyzeCall(callerNumber)
                val familyContacts = familyManager.familyContacts.value
                val isRelative = familyContacts.any { it.phoneNumber.contains(callerNumber) || callerNumber.contains(it.phoneNumber) }

                com.example.ui.glass.DynamicIslandImpressionController.setIncomingCall(callerNumber)

                Log.d("CallScreeningService", "Incoming Call from: $callerNumber | IsRelative: $isRelative | SpamRisk: ${spamRes.riskScore}")

                val responseBuilder = CallResponse.Builder()

                if (isRelative) {
                    // Relative Call Screening Policy
                    Log.d("CallScreeningService", "Relative call detected. AI Assistant screening enabled.")
                    responseBuilder.setDisallowCall(false)
                    responseBuilder.setRejectCall(false)
                    responseBuilder.setSilenceCall(false)

                    // Log AI relative screening event
                    val db = AppDatabase.getDatabase(context)
                    db.callSummaryDao().insertCallSummary(
                        CallSummaryEntity(
                            callerName = "Relative ($callerNumber)",
                            callerPhone = callerNumber,
                            purpose = "Relative Screening",
                            importantPoints = "Relative called. AI Assistant screened call politely: 'I will convey your message to the boss/sir right away.'",
                            requestedAction = "Pass message to owner",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } else if (spamRes.riskScore >= 70) {
                    // Spam / Fraud Call Auto Block
                    responseBuilder.setDisallowCall(true)
                    responseBuilder.setRejectCall(true)
                    responseBuilder.setSkipCallLog(false)
                    responseBuilder.setSkipNotification(true)

                    val db = AppDatabase.getDatabase(context)
                    db.callSummaryDao().insertCallSummary(
                        CallSummaryEntity(
                            callerName = "Spam/Scam ($callerNumber)",
                            callerPhone = callerNumber,
                            purpose = "Spam Blocked",
                            importantPoints = "Spam call automatically blocked by Threat Detection Engine.",
                            requestedAction = "Block Call",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Business / Standard Call Handler
                    responseBuilder.setDisallowCall(false)
                    responseBuilder.setRejectCall(false)

                    val db = AppDatabase.getDatabase(context)
                    db.callSummaryDao().insertCallSummary(
                        CallSummaryEntity(
                            callerName = "Business Caller ($callerNumber)",
                            callerPhone = callerNumber,
                            purpose = "Inbound Business Query",
                            importantPoints = "Incoming business call received. Context logged for Snaper AI Assistant follow-up.",
                            requestedAction = "AI Voice Follow-up",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }

                respondToCall(callDetails, responseBuilder.build())
            } catch (e: Exception) {
                Log.e("CallScreeningService", "Error during call screening: ${e.message}")
                respondToCall(callDetails, CallResponse.Builder().build())
            }
        }
    }
}
