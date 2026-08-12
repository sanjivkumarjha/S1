package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.FamilyProfileEntity
import kotlinx.coroutines.flow.Flow

class FamilyManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val familyDao = db.familyProfileDao()

    val familyProfiles: Flow<List<FamilyProfileEntity>> = familyDao.getAllFamilyProfiles()

    suspend fun addFamilyMember(name: String, relationship: String, photoUri: String = "", allowedApps: String = "com.google.android.youtube"): Long {
        return familyDao.insertFamilyProfile(
            FamilyProfileEntity(
                name = name.trim(),
                relationship = relationship.trim(),
                photoUri = photoUri,
                allowedApps = allowedApps
            )
        )
    }

    suspend fun updateFamilyPhoto(id: Long, photoUri: String) {
        val current = familyDao.getFamilyProfileById(id)
        if (current != null) {
            familyDao.insertFamilyProfile(current.copy(photoUri = photoUri))
        }
    }

    suspend fun removeFamilyMember(id: Long) {
        familyDao.deleteFamilyProfileById(id)
    }

    fun isPrivateQueryForFamily(query: String): Boolean {
        val q = query.lowercase()
        return q.contains("बात कर रहा") ||
                q.contains("kisse baat") ||
                q.contains("who was calling") ||
                q.contains("chat") ||
                q.contains("message") ||
                q.contains("password") ||
                q.contains("bank") ||
                q.contains("private")
    }

    fun getFamilyPrivacyRefusalResponse(ownerTitle: String = "Boss"): String {
        return "यह $ownerTitle की private information है, मैं share नहीं कर सकती. 😊"
    }
}
