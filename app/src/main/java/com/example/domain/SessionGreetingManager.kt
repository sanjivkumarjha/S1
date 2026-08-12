package com.example.domain

import android.content.Context
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first

object SessionGreetingManager {
    @Volatile
    private var hasGreetedInThisSession = false

    fun shouldPrependRadheRadhe(): Boolean {
        if (!hasGreetedInThisSession) {
            hasGreetedInThisSession = true
            return true
        }
        return false
    }

    fun resetSession() {
        hasGreetedInThisSession = false
    }
}
