package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.GuestPermissionTokenEntity
import kotlinx.coroutines.flow.Flow

class GuestPermissionManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val guestDao = db.guestPermissionDao()

    val activeGuestTokens: Flow<List<GuestPermissionTokenEntity>> = guestDao.getActiveGuestPermissions()

    suspend fun grantGuestToken(
        target: String,
        type: String = "READ_ONLY",
        durationMinutes: Int = 30
    ): Long {
        val now = System.currentTimeMillis()
        val expiresAt = now + (durationMinutes * 60 * 1000L)
        return guestDao.insertToken(
            GuestPermissionTokenEntity(
                targetAppOrFeature = target,
                permissionType = type,
                durationMinutes = durationMinutes,
                grantedAtMillis = now,
                expiresAtMillis = expiresAt,
                isRevoked = false
            )
        )
    }

    suspend fun revokeToken(id: Long) {
        guestDao.revokeTokenById(id)
    }

    suspend fun revokeAllTokens() {
        guestDao.revokeAllTokens()
    }
}
