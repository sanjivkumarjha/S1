package com.example.domain

import android.content.Context
import com.example.data.preferences.UserPreferencesRepository

class FocusModeManager(private val context: Context) {

    private val prefsRepo = UserPreferencesRepository(context)

    suspend fun setFocusMode(enabled: Boolean) {
        prefsRepo.setFocusModeEnabled(enabled)
    }

    fun getGentleFocusReminder(ownerTitle: String = "Boss"): String {
        return "$ownerTitle, customer का काम pending है. पहले वो कर लेते हैं, फिर आराम से video देखेंगे. 😊"
    }
}
