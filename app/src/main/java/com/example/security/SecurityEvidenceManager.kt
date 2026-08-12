package com.example.security

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SecurityEventLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages security evidence capture (photos/videos/timestamps) legally & with explicit owner permission.
 * Default is OFF.
 */
class SecurityEvidenceManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val securityLogDao = db.securityLogDao()

    data class EvidenceConfig(
        val isEnabled: Boolean = false,
        val photoCount: Int = 3, // 1 to 10
        val videoDurationSeconds: Int = 10, // 5 to 60 seconds
        val retentionDays: Int = 7 // 1, 7, 30, or 0 (forever)
    )

    suspend fun recordEvidenceEvent(
        eventType: String,
        details: String,
        config: EvidenceConfig
    ) = withContext(Dispatchers.IO) {
        if (!config.isEnabled) return@withContext

        val description = "Evidence Captured ($eventType) - ${config.photoCount} photo(s), ${config.videoDurationSeconds}s video. Details: $details"
        
        securityLogDao.insertLog(
            SecurityEventLogEntity(
                eventType = "EVIDENCE_CAPTURED_$eventType",
                description = description,
                timestamp = System.currentTimeMillis(),
                securityLevel = "HIGH"
            )
        )
    }
}
