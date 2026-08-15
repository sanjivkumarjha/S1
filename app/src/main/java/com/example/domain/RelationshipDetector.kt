package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CallSummaryEntity
import kotlinx.coroutines.flow.first

/**
 * Smart Relationship Sensing v14.0
 *
 * AUTOMATIC RELATIONSHIP DETECTOR:
 * Analyzes interactions/bonding levels to detect who Sanjiv Sir is closest to
 * (Girlfriend, Sister, Best Friend, Family, Partner).
 */
class RelationshipDetector(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    enum class RelationshipType(val displayName: String) {
        PARTNER("Partner / Girlfriend"),
        SISTER("Sister"),
        BEST_FRIEND("Best Friend"),
        FAMILY("Family Member"),
        COLLEAGUE("Colleague"),
        PARENT("Parent"),
        UNKNOWN("Unknown")
    }

    data class ClosestContact(
        val name: String,
        val relationship: RelationshipType,
        val phone: String = "",
        val interactionScore: Float = 0f,
        val lastInteractionTimestamp: Long = 0L
    )

    data class RelationshipProfile(
        val closestContact: ClosestContact,
        val allContacts: List<ClosestContact>,
        val interactionHistorySummary: String
    )

    /**
     * Detect the closest contact based on call history and interaction patterns.
     */
    suspend fun detectClosestContact(): RelationshipProfile {
        val callSummaries = try {
            db.callSummaryDao().getAllCallSummaries().first()
        } catch (e: Exception) {
            emptyList()
        }

        val familyProfiles = try {
            db.familyProfileDao().getAllFamilyProfiles().first()
        } catch (e: Exception) {
            emptyList()
        }

        // Build contact scores from call data
        val contactScores = mutableMapOf<String, ContactScoreData>()

        for (summary in callSummaries) {
            val callerName = summary.callerName.ifBlank { summary.callerPhone }
            val data = contactScores.getOrPut(callerName) {
                ContactScoreData(
                    name = callerName,
                    phone = summary.callerPhone
                )
            }
            data.interactionCount++
            data.lastTimestamp = maxOf(data.lastTimestamp, summary.timestamp)
            data.totalImportance += calculateCallImportance(summary)
        }

        // Match with family profiles for relationship type
        val allContacts = contactScores.map { (entryName, entryData) ->
            val familyProfile = familyProfiles.find { profile ->
                entryName.contains(profile.name, ignoreCase = true) || entryData.phone == profile.photoUri
            }
            val relationship = when {
                familyProfile != null -> mapRelationship(familyProfile.relationship)
                entryName.contains("sister", ignoreCase = true) ||
                    entryName.contains("behen", ignoreCase = true) -> RelationshipType.SISTER
                entryName.contains("girlfriend", ignoreCase = true) ||
                    entryName.contains("girl friend", ignoreCase = true) ||
                    entryName.contains("premika", ignoreCase = true) -> RelationshipType.PARTNER
                entryName.contains("friend", ignoreCase = true) ||
                    entryName.contains("dost", ignoreCase = true) -> RelationshipType.BEST_FRIEND
                entryName.contains("mom", ignoreCase = true) ||
                    entryName.contains("mummy", ignoreCase = true) ||
                    entryName.contains("maa", ignoreCase = true) ||
                    entryName.contains("father", ignoreCase = true) ||
                    entryName.contains("papa", ignoreCase = true) ||
                    entryName.contains("pita", ignoreCase = true) -> RelationshipType.PARENT
                entryName.contains("family", ignoreCase = true) ||
                    entryName.contains("bhai", ignoreCase = true) ||
                    entryName.contains("brother", ignoreCase = true) -> RelationshipType.FAMILY
                else -> RelationshipType.COLLEAGUE
            }

            ClosestContact(
                name = entryName,
                relationship = relationship,
                phone = entryData.phone,
                interactionScore = calculateInteractionScore(entryData, contactScores.size),
                lastInteractionTimestamp = entryData.lastTimestamp
            )
        }.sortedByDescending { it.interactionScore }

        val closest = allContacts.firstOrNull() ?: ClosestContact(
            name = "Sanjiv Sir's Close Contact",
            relationship = RelationshipType.UNKNOWN,
            interactionScore = 0f
        )

        return RelationshipProfile(
            closestContact = closest,
            allContacts = allContacts,
            interactionHistorySummary = buildSummary(closest, allContacts.size)
        )
    }

    /**
     * Get the complaint threat message directed at the closest contact.
     */
    fun getComplaintThreatMessage(
        closestContact: ClosestContact,
        languageCode: String = "hi"
    ): String {
        val contactName = closestContact.name
        val relationship = when (closestContact.relationship) {
            RelationshipType.PARTNER -> if (languageCode == "hi") "girlfriend" else "girlfriend"
            RelationshipType.SISTER -> if (languageCode == "hi") "behen" else "sister"
            RelationshipType.BEST_FRIEND -> if (languageCode == "hi") "best friend" else "best friend"
            RelationshipType.FAMILY -> if (languageCode == "hi") "family member" else "family member"
            RelationshipType.PARENT -> if (languageCode == "hi") "maa-baap" else "parent"
            RelationshipType.COLLEAGUE -> if (languageCode == "hi") "colleague" else "colleague"
            RelationshipType.UNKNOWN -> if (languageCode == "hi") "close person" else "close person"
        }

        return when (languageCode.lowercase()) {
            "hi" -> "Sir! Main aapki $relationship $contactName ko bata doongi ki aap unki baat ignore kar rahe hain. Aap unse baat karein, warna main complain kar doongi!"
            "en" -> "Sir! I'm going to tell your $relationship $contactName that you're ignoring them. Please talk to them, or I'll have to report you!"
            "hinglish" -> "Sir! Main aapki $relationship $contactName ko bata doongi ki aap unki baat ignore kar rahe hain. Please unse baat karein, warna main complain kar doongi!"
            else -> "Sir! I will tell your $relationship $contactName that you are ignoring them. Please talk to them, or I'll report you!"
        }
    }

    private data class ContactScoreData(
        val name: String,
        val phone: String = "",
        var interactionCount: Int = 0,
        var lastTimestamp: Long = 0L,
        var totalImportance: Float = 0f
    )

    private fun calculateCallImportance(summary: CallSummaryEntity): Float {
        var importance = 1.0f
        if (summary.purpose.isNotBlank()) importance += 0.2f
        if (summary.requestedAction.isNotBlank()) importance += 0.3f
        if (summary.followUpDate.isNotBlank()) importance += 0.5f
        return importance
    }

    private fun mapRelationship(relationship: String): RelationshipType {
        return when (relationship.lowercase()) {
            "sister", "behen" -> RelationshipType.SISTER
            "spouse", "partner", "girlfriend", "boyfriend" -> RelationshipType.PARTNER
            "mother", "father", "maa", "pita" -> RelationshipType.PARENT
            "friend", "best friend", "dost" -> RelationshipType.BEST_FRIEND
            "brother", "bhai", "child" -> RelationshipType.FAMILY
            "colleague" -> RelationshipType.COLLEAGUE
            else -> RelationshipType.UNKNOWN
        }
    }

    private fun calculateInteractionScore(data: ContactScoreData, totalContacts: Int): Float {
        val frequencyScore = (data.interactionCount.toFloat() / 50f).coerceIn(0f, 1f)
        val recencyScore = if (data.lastTimestamp > 0) {
            val daysSinceLast = (System.currentTimeMillis() - data.lastTimestamp) / (86400000f)
            (1f / (1f + daysSinceLast)).coerceIn(0f, 1f)
        } else 0f
        val importanceScore = (data.totalImportance / 10f).coerceIn(0f, 1f)

        return (frequencyScore * 0.4f + recencyScore * 0.3f + importanceScore * 0.3f).coerceIn(0f, 1f)
    }

    private fun buildSummary(closest: ClosestContact, totalContacts: Int): String {
        return "Closest contact detected: ${closest.name} (${closest.relationship.displayName}), " +
                "score: ${String.format("%.2f", closest.interactionScore)}, " +
                "monitored contacts: $totalContacts"
    }
}