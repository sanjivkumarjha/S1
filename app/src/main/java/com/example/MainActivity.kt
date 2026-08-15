package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import com.example.domain.MorningGreetingScheduler
import com.example.ui.theme.SnaperTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        MorningGreetingScheduler.scheduleDailyMorningGreeting(applicationContext)
        try {
            com.example.devicecare.SmartSystemMonitorManager.getInstance(applicationContext).startMonitoring()
            com.example.service.AssistantForegroundService.startService(applicationContext)
        } catch (e: Exception) {
            // Handle service start restriction gracefully
        }

        val prefsRepo = UserPreferencesRepository(applicationContext)

        setContent {
            val userSettings by prefsRepo.userSettingsFlow.collectAsState(initial = UserSettings())

            LaunchedEffect(userSettings.languageCode) {
                if (userSettings.languageCode.isNotBlank()) {
                    try {
                        val currentLocales = AppCompatDelegate.getApplicationLocales()
                        if (currentLocales.isEmpty || currentLocales.get(0)?.language != userSettings.languageCode) {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(userSettings.languageCode)
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            SnaperTheme.SnaperTheme(
                themeMode = userSettings.themeMode,
                accentColorHex = userSettings.accentColorHex,
                dynamicColor = false
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "${userSettings.ownerName}'s AI Assistant",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}