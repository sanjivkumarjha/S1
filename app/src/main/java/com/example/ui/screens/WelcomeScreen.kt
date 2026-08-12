package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import com.example.domain.LanguageDictionary
import com.example.ui.components.AnimeAssistantCanvas
import com.example.ui.glass.*
import com.example.ui.components.AppLogo
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    languageCode: String,
    onGetStarted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repo = remember { UserPreferencesRepository(context) }
    val dict = LanguageDictionary(languageCode)

    DynamicLiquidGlassBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppLogo(size = 90.dp, showGlow = true)

                Spacer(modifier = Modifier.height(16.dp))

                AnimeAssistantCanvas(
                    sizeDp = 180.dp,
                    isSpeaking = false,
                    accentColor = LocalGlassAccent.current.color
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "राधे राधे ✨",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF2D55)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = dict.getString("welcome_greeting"),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = dict.getString("welcome_message"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(36.dp))

                GlassButton(
                    text = dict.getString("get_started"),
                    onClick = {
                        scope.launch {
                            repo.setOnboardingCompleted(true)
                            onGetStarted()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(54.dp),
                    testTag = "welcome_get_started_button"
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                GlassFooter()
            }
        }
    }
}
