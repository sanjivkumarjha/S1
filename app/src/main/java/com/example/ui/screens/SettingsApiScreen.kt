package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.preferences.AiProvider
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

@Composable
fun SettingsApiScreen(
    userSettings: UserSettings,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefsRepo = remember { UserPreferencesRepository(context) }

    var selectedProvider by remember { mutableStateOf(userSettings.aiProvider) }
    var userApiKeyInput by remember { mutableStateOf(userSettings.userApiKey) }
    var customBaseUrlInput by remember { mutableStateOf(userSettings.customBaseUrl) }
    var selectedModelInput by remember { mutableStateOf(userSettings.selectedModel) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf("") }
    var isTestSuccess by remember { mutableStateOf(false) }

    var isRefreshingModels by remember { mutableStateOf(false) }
    var availableModelsList by remember {
        mutableStateOf(
            listOf(
                "gemini-3.5-flash",
                "gemini-2.5-flash",
                "gemini-2.5-pro",
                "anthropic/claude-3.5-sonnet",
                "grok-2",
                "meta/llama-3.3-70b-instruct",
                "gpt-4o"
            )
        )
    }

    val scrollState = rememberScrollState()

    fun performConnectionTest() {
        scope.launch {
            isTestingConnection = true
            testResultText = "Testing network latency to ${selectedProvider.displayName}..."

            val activeKey = if (userApiKeyInput.isNotBlank()) userApiKeyInput else when (selectedProvider) {
                AiProvider.GEMINI -> BuildConfig.GEMINI_API_KEY
                AiProvider.GROK -> BuildConfig.GROK_API_KEY
                AiProvider.OPENAI -> BuildConfig.OPENAI_API_KEY
                AiProvider.CLAUDE -> BuildConfig.CLAUDE_API_KEY
                AiProvider.NVIDIA -> BuildConfig.NVIDIA_API_KEY
                AiProvider.OPENROUTER -> BuildConfig.OPENROUTER_API_KEY
                else -> BuildConfig.GEMINI_API_KEY
            }
            val startTime = System.currentTimeMillis()

            val success = withContext(Dispatchers.IO) {
                try {
                    val client = OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .build()

                    val targetUrl = if (selectedProvider == AiProvider.GEMINI) {
                        "https://generativelanguage.googleapis.com/v1beta/models?key=$activeKey"
                    } else {
                        val base = if (customBaseUrlInput.isNotBlank()) customBaseUrlInput else selectedProvider.defaultBaseUrl
                        if (base.endsWith("/")) "${base}models" else "$base/models"
                    }

                    val reqBuilder = Request.Builder().url(targetUrl).get()
                    if (selectedProvider == AiProvider.CLAUDE) {
                        reqBuilder.addHeader("x-api-key", activeKey)
                        reqBuilder.addHeader("anthropic-version", "2023-06-01")
                    } else if (selectedProvider != AiProvider.GEMINI && activeKey.isNotBlank()) {
                        reqBuilder.addHeader("Authorization", "Bearer $activeKey")
                    }
                    val req = reqBuilder.build()

                    client.newCall(req).execute().use { response ->
                        // A real auth test: 2xx = key valid & reachable. 401/403 = bad/missing key.
                        // 5xx/network errors = provider-side or network issue. Do NOT treat a 401
                        // as "endpoint reachable, key ready" — that lied about credentials.
                        Triple(response.isSuccessful, response.code, response.body?.string()?.take(200) ?: "")
                    }
                } catch (e: Exception) {
                    Triple(false, -1, e.message ?: "Network error")
                }
            }

            val elapsed = System.currentTimeMillis() - startTime
            isTestingConnection = false
            val (isOk, code, detail) = success
            when {
                isOk -> {
                    isTestSuccess = true
                    testResultText = "Connection Successful! Authenticated with ${selectedProvider.displayName}. Latency: ${elapsed}ms ✅"
                }
                code == 401 || code == 403 -> {
                    isTestSuccess = false
                    testResultText = "Authentication failed (HTTP $code). Your API key is missing, invalid, or lacks permission. Please verify the key for ${selectedProvider.displayName}."
                }
                code == -1 -> {
                    isTestSuccess = false
                    testResultText = "Connection failed: $detail"
                }
                else -> {
                    isTestSuccess = false
                    testResultText = "Connection failed (HTTP $code). ${selectedProvider.displayName} did not accept the request. $detail"
                }
            }
        }
    }

    val aiRepo = remember { com.example.data.api.AiRepository(context) }

    fun refreshAvailableModels() {
        scope.launch {
            isRefreshingModels = true
            val fetched = aiRepo.fetchAvailableModels(
                provider = selectedProvider,
                apiKey = userApiKeyInput,
                baseUrl = customBaseUrlInput
            )
            if (fetched.isNotEmpty()) {
                availableModelsList = fetched
                if (!fetched.contains(selectedModelInput)) {
                    selectedModelInput = fetched.first()
                }
            }
            isRefreshingModels = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Multi-API & Provider Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Select Primary AI Provider",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Clean Provider Cards
        AiProvider.entries.forEach { provider ->
            val isSelected = selectedProvider == provider
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        selectedProvider = provider
                        selectedModelInput = when (provider) {
                            AiProvider.GEMINI -> "gemini-3.5-flash"
                            AiProvider.OPENROUTER -> "anthropic/claude-3.5-sonnet"
                            AiProvider.GROK -> "grok-2"
                            AiProvider.CLAUDE -> "claude-3-5-haiku-20241022"
                            AiProvider.NVIDIA -> "meta/llama-3.3-70b-instruct"
                            AiProvider.KIMI -> "moonshot-v1-8k"
                            AiProvider.GLM -> "glm-4"
                            AiProvider.OPENAI -> "gpt-4o-mini"
                            AiProvider.CUSTOM -> "gpt-4o"
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = provider.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = provider.defaultBaseUrl, fontSize = 11.sp, color = Color.Gray)
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = userApiKeyInput,
            onValueChange = { userApiKeyInput = it },
            label = { Text("User Custom API Key (Optional)") },
            placeholder = { Text("Leave blank to use pre-configured Secrets") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = selectedModelInput,
            onValueChange = { selectedModelInput = it },
            label = { Text("Selected Model Identifier") },
            placeholder = { Text("e.g. gemini-3.5-flash") },
            modifier = Modifier.fillMaxWidth()
        )

        if (selectedProvider == AiProvider.CUSTOM) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = customBaseUrlInput,
                onValueChange = { customBaseUrlInput = it },
                label = { Text("Custom Base Endpoint URL") },
                placeholder = { Text("https://api.openai.com/v1/") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Connection Test & Refresh Models Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { performConnectionTest() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                if (isTestingConnection) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                } else {
                    Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Connection", fontSize = 12.sp)
                }
            }

            OutlinedButton(
                onClick = { refreshAvailableModels() },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                if (isRefreshingModels) {
                    CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refresh Models", fontSize = 12.sp)
                }
            }
        }

        if (testResultText.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = testResultText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isTestSuccess) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Available Models Chips
        Text(
            text = "Available Models for ${selectedProvider.name}",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(availableModelsList) { modelName ->
                val isSelected = selectedModelInput == modelName
                Surface(
                    onClick = { selectedModelInput = modelName },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.height(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text(
                            text = modelName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                scope.launch {
                    prefsRepo.updateAiProvider(
                        provider = selectedProvider,
                        apiKey = userApiKeyInput,
                        baseUrl = customBaseUrlInput,
                        model = selectedModelInput.ifBlank { "gemini-3.5-flash" }
                    )
                    onNavigateBack()
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Save API & Provider Configuration", fontWeight = FontWeight.Bold)
        }
    }
}
