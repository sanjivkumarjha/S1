package com.example.security

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.local.AppDatabase

/**
 * Entity representing a logged security or threat incident.
 */
@Entity(tableName = "security_incidents")
data class SecurityIncidentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val source: String, // "Call", "SMS", "URL", "Payment", "QR", "Email"
    val threatType: String, // "Spam Call", "Phishing SMS", "Malicious Link", "Payment Scam", "OTP Phishing"
    val riskScore: Int, // 0 - 100
    val classification: String, // "SAFE", "UNKNOWN", "MARKETING", "SPAM", "SCAM_SUSPECTED", "HIGH_RISK"
    val actionTaken: String, // "BLOCKED", "SILENCED", "WARNED", "ALLOWED"
    val details: String, // URL, Phone number, or message snippet
    val ownerResponse: String = "PENDING" // "PENDING", "CONFIRMED_SPAM", "DISMISSED", "BLOCKED"
)

/**
 * Manages local security incident logging, exporting, and clearing.
 */
class SecurityIncidentReportManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    suspend fun logIncident(
        source: String,
        threatType: String,
        riskScore: Int,
        classification: String,
        actionTaken: String,
        details: String
    ): Long {
        val incident = SecurityIncidentEntity(
            source = source,
            threatType = threatType,
            riskScore = riskScore,
            classification = classification,
            actionTaken = actionTaken,
            details = details
        )
        // Log into existing security event table as well
        db.securityLogDao().insertLog(
            com.example.data.local.entities.SecurityEventLogEntity(
                eventType = threatType,
                description = "[$classification] $details - Action: $actionTaken",
                securityLevel = if (riskScore > 70) "HIGH" else "MEDIUM"
            )
        )
        return incident.id
    }
}
