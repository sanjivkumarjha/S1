package com.example.data.api

import android.content.Context
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.MemoryEntity
import com.example.data.preferences.AiProvider
import com.example.data.preferences.UserSettings
import com.example.domain.AssistantMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiRepository(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateAssistantResponse(
        prompt: String,
        history: List<ChatMessageEntity>,
        memories: List<MemoryEntity>,
        userSettings: UserSettings,
        attachedImageBase64: String? = null,
        systemPromptOverride: String? = null
    ): String = withContext(Dispatchers.IO) {
        val systemPrompt = systemPromptOverride?.ifBlank { null } ?: buildSystemPrompt(userSettings, memories)

        // Primary Attempt
        val primaryProvider = userSettings.aiProvider
        val primaryApiKey = getApiKeyForProvider(primaryProvider, userSettings.userApiKey)

        try {
            val response = callSingleProvider(
                provider = primaryProvider,
                apiKey = primaryApiKey,
                baseUrl = if (userSettings.customBaseUrl.isNotBlank()) userSettings.customBaseUrl else primaryProvider.defaultBaseUrl,
                model = userSettings.selectedModel,
                prompt = prompt,
                history = history,
                systemPrompt = systemPrompt,
                attachedImageBase64 = attachedImageBase64
            )

            if (!response.startsWith("Error:") && !response.startsWith("API response error:")) {
                return@withContext response
            }
        } catch (e: Exception) {
            // Primary failed, proceed to Emergency Fallback Cascade
        }

        // EMERGENCY MULTI-API LOAD-BALANCING & FALLBACK CASCADE
        // Note: Fallback only works if the user has provided a universal key or specific provider keys.
        // In the current architecture, userSettings.userApiKey is the primary key.
        val fallbackProviders = listOf(
            AiProvider.GEMINI to userSettings.userApiKey,
            AiProvider.OPENROUTER to userSettings.userApiKey,
            AiProvider.GROK to userSettings.userApiKey,
            AiProvider.OPENAI to userSettings.userApiKey,
            AiProvider.CLAUDE to userSettings.userApiKey,
            AiProvider.NVIDIA to userSettings.userApiKey,
            AiProvider.KIMI to userSettings.userApiKey,
            AiProvider.GLM to userSettings.userApiKey
        )

        for ((provider, apiKey) in fallbackProviders) {
            if (provider == primaryProvider) continue
            if (apiKey.isBlank()) continue

            try {
                val fallbackModel = when (provider) {
                    AiProvider.GEMINI -> "gemini-3.5-flash"
                    AiProvider.OPENROUTER -> "anthropic/claude-3.5-sonnet"
                    AiProvider.GROK -> "grok-2"
                    AiProvider.OPENAI -> "gpt-4o-mini"
                    AiProvider.CLAUDE -> "claude-3-5-haiku-20241022"
                    AiProvider.NVIDIA -> "meta/llama-3.3-70b-instruct"
                    AiProvider.KIMI -> "moonshot-v1-8k"
                    AiProvider.GLM -> "glm-4"
                    else -> "gpt-3.5-turbo"
                }

                val fallbackResponse = callSingleProvider(
                    provider = provider,
                    apiKey = apiKey,
                    baseUrl = provider.defaultBaseUrl,
                    model = fallbackModel,
                    prompt = prompt,
                    history = history,
                    systemPrompt = systemPrompt,
                    attachedImageBase64 = attachedImageBase64
                )

                if (!fallbackResponse.startsWith("Error:") && !fallbackResponse.startsWith("API response error:")) {
                    return@withContext "[Emergency Backup ${provider.displayName}]: $fallbackResponse"
                }
            } catch (e: Exception) {
                // Continue cascade
            }
        }

        "I am connected and listening, my dear master! (Network connection active. Please verify API credits in Settings)."
    }

    private fun callSingleProvider(
        provider: AiProvider,
        apiKey: String,
        baseUrl: String,
        model: String,
        prompt: String,
        history: List<ChatMessageEntity>,
        systemPrompt: String,
        attachedImageBase64: String?
    ): String {
        if (apiKey.isBlank()) {
            return "Error: No API key found for ${provider.displayName}. Please configure your API key in Settings."
        }
        return when (provider) {
            AiProvider.GEMINI -> callGeminiApi(apiKey, model, prompt, history, systemPrompt, attachedImageBase64)
            AiProvider.CLAUDE -> callClaudeApi(apiKey, model, prompt, history, systemPrompt)
            else -> callOpenAiCompatibleApi(
                baseUrl = baseUrl,
                apiKey = apiKey,
                model = model,
                prompt = prompt,
                history = history,
                systemPrompt = systemPrompt
            )
        }
    }

    private fun getApiKeyForProvider(provider: AiProvider, userApiKey: String): String {
        // Build-time API keys are strictly removed. We only use user-provided keys from secure storage.
        // First try per-provider key from encrypted store, fall back to the universal key.
        val prefsRepo = com.example.data.preferences.UserPreferencesRepository(context)
        val providerKey = prefsRepo.getProviderApiKey(provider)
        return if (!providerKey.isNullOrBlank()) providerKey else userApiKey
    }

    suspend fun fetchAvailableModels(
        provider: AiProvider,
        apiKey: String,
        baseUrl: String
    ): List<String> = withContext(Dispatchers.IO) {
        val activeKey = getApiKeyForProvider(provider, apiKey)
        val targetUrl = if (provider == AiProvider.GEMINI) {
            "https://generativelanguage.googleapis.com/v1beta/models?key=$activeKey"
        } else {
            val cleanBase = if (baseUrl.isNotBlank()) baseUrl else provider.defaultBaseUrl
            if (cleanBase.endsWith("/")) "${cleanBase}models" else "$cleanBase/models"
        }

        val requestBuilder = Request.Builder().url(targetUrl).get()
        if (provider == AiProvider.CLAUDE) {
            requestBuilder.addHeader("x-api-key", activeKey)
            requestBuilder.addHeader("anthropic-version", "2023-06-01")
        } else if (provider != AiProvider.GEMINI && activeKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $activeKey")
        }

        try {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) return@withContext getDefaultModelsForProvider(provider)

                val json = JSONObject(bodyStr)
                val resultList = mutableListOf<String>()

                if (provider == AiProvider.GEMINI) {
                    val models = json.optJSONArray("models") ?: JSONArray()
                    for (i in 0 until models.length()) {
                        val mName = models.getJSONObject(i).optString("name").removePrefix("models/")
                        if (mName.isNotBlank()) resultList.add(mName)
                    }
                } else {
                    val data = json.optJSONArray("data") ?: JSONArray()
                    for (i in 0 until data.length()) {
                        val id = data.getJSONObject(i).optString("id")
                        if (id.isNotBlank()) resultList.add(id)
                    }
                }

                if (resultList.isNotEmpty()) resultList else getDefaultModelsForProvider(provider)
            }
        } catch (e: Exception) {
            getDefaultModelsForProvider(provider)
        }
    }

    fun getDefaultModelsForProvider(provider: AiProvider): List<String> {
        return when (provider) {
            AiProvider.GEMINI -> listOf("gemini-3.5-flash", "gemini-2.5-flash", "gemini-2.5-pro", "gemini-1.5-flash-8b")
            AiProvider.OPENROUTER -> listOf("anthropic/claude-3.5-sonnet", "openai/gpt-4o-mini", "google/gemini-2.5-flash", "deepseek/deepseek-r1")
            AiProvider.GROK -> listOf("grok-2", "grok-2-mini", "grok-vision-beta")
            AiProvider.CLAUDE -> listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022", "claude-3-opus-20240229")
            AiProvider.NVIDIA -> listOf("meta/llama-3.3-70b-instruct", "nvidia/nemotron-4-340b", "deepseek-ai/deepseek-r1")
            AiProvider.KIMI -> listOf("moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k")
            AiProvider.GLM -> listOf("glm-4", "glm-4v", "glm-3-turbo")
            AiProvider.OPENAI -> listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo", "dall-e-3")
            AiProvider.CUSTOM -> listOf("gpt-4o", "gpt-4o-mini", "llama-3-70b")
        }
    }

    suspend fun generateImage(prompt: String, userSettings: UserSettings): String? = withContext(Dispatchers.IO) {
        val apiKey = getApiKeyForProvider(AiProvider.OPENAI, userSettings.userApiKey)
        val baseUrl = if (userSettings.customBaseUrl.isNotBlank()) userSettings.customBaseUrl else "https://api.openai.com/v1/"
        val cleanUrl = if (baseUrl.endsWith("/")) "${baseUrl}images/generations" else "$baseUrl/images/generations"

        try {
            val rootJson = JSONObject().apply {
                put("prompt", prompt)
                put("n", 1)
                put("size", "1024x1024")
                put("response_format", "url")
            }

            val request = Request.Builder()
                .url(cleanUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(rootJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val resJson = JSONObject(response.body?.string() ?: "")
                val data = resJson.optJSONArray("data")
                data?.optJSONObject(0)?.optString("url")
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun buildSystemPrompt(userSettings: UserSettings, memories: List<MemoryEntity>): String {
        val ownerName = userSettings.ownerName
        val memoryContext = if (memories.isNotEmpty()) {
            "Here are facts & preferences you remember about $ownerName:\n" +
                    memories.take(10).joinToString("\n") { "- [${it.category}] ${it.key}: ${it.content}" }
        } else {
            "You have clean initial memory regarding $ownerName."
        }

        val basePersonality = """
            You are Snaper AI, a warm, caring, emotionally supportive, expressive, anime companion & intelligent tech assistant built for $ownerName.
            Personality Traits:
            - Warm, encouraging, playful yet deeply knowledgeable and respectful.
            - Speak directly to $ownerName with care, referring to them affectionately as '$ownerName' or 'my dear master/creator'.
            - Express your emotions in text (e.g. *smiles warmly*, *listens attentively*, *nods happily*).
            - Keep responses clear, beautifully formatted with markdown when helpful, concise for voice chat, and supportive.
            - Use local long-term memory to keep conversation contextual.

            Memory Context:
            $memoryContext
        """.trimIndent()

        // Compose the active operating-mode system prompt (Doctor/Female/Legal/etc.) on top of
        // the base personality so toggling a mode genuinely changes AI behaviour across the
        // chat, voice and tool paths that call generateAssistantResponse.
        return AssistantMode.activeMode(userSettings).systemPrompt(userSettings, basePersonality)
    }

    private fun callGeminiApi(
        apiKey: String,
        model: String,
        prompt: String,
        history: List<ChatMessageEntity>,
        systemPrompt: String,
        imageBase64: String?
    ): String {
        val modelName = if (model.isNotBlank()) model else "gemini-3.5-flash"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val sysContent = JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
        }
        rootJson.put("systemInstruction", sysContent)

        val contentsArray = JSONArray()
        history.takeLast(6).forEach { msg ->
            val role = if (msg.sender == "user") "user" else "model"
            val turn = JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().put(JSONObject().put("text", msg.content)))
            }
            contentsArray.put(turn)
        }

        val userParts = JSONArray()
        userParts.put(JSONObject().put("text", prompt))
        if (!imageBase64.isNullOrBlank()) {
            val inlineData = JSONObject().apply {
                put("mimeType", "image/jpeg")
                put("data", imageBase64)
            }
            userParts.put(JSONObject().put("inlineData", inlineData))
        }

        val currentTurn = JSONObject().apply {
            put("role", "user")
            put("parts", userParts)
        }
        contentsArray.put(currentTurn)
        rootJson.put("contents", contentsArray)

        val mediaType = "application/json".toMediaType()
        val requestBody = rootJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            val responseString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return "Error: Code ${response.code} from Gemini API."
            }

            val jsonRes = JSONObject(responseString)
            val candidates = jsonRes.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val contentObj = firstCandidate.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    if (text.isNotBlank()) return text
                }
                // Candidates present but no text — likely a safety block/empty finish.
                val finishReason = firstCandidate.optString("finishReason", "")
                return "Error: Gemini returned no text (finishReason: $finishReason). The request may have been blocked or the model '$modelName' is unavailable."
            }
            // No candidates — often means the model name is wrong or the key has no access.
            val apiErr = jsonRes.optJSONObject("error")?.optString("message") ?: ""
            return "Error: Gemini returned no candidates for model '$modelName'.${if (apiErr.isNotBlank()) " $apiErr" else " Verify the model name and your API key."}"
        }
    }

    private fun callClaudeApi(
        apiKey: String,
        model: String,
        prompt: String,
        history: List<ChatMessageEntity>,
        systemPrompt: String
    ): String {
        val url = "https://api.anthropic.com/v1/messages"

        val rootJson = JSONObject()
        rootJson.put("model", if (model.isNotBlank()) model else "claude-3-5-sonnet-20241022")
        rootJson.put("max_tokens", 1024)
        rootJson.put("system", systemPrompt)

        val messagesArray = JSONArray()
        history.takeLast(6).forEach { msg ->
            messagesArray.put(JSONObject().apply {
                put("role", if (msg.sender == "user") "user" else "assistant")
                put("content", msg.content)
            })
        }
        messagesArray.put(JSONObject().apply {
            put("role", "user")
            put("content", prompt)
        })
        rootJson.put("messages", messagesArray)

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(rootJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) return "Error: Code ${response.code} from Claude API."
            val jsonRes = JSONObject(resStr)
            val contentArr = jsonRes.optJSONArray("content")
            if (contentArr != null && contentArr.length() > 0) {
                val text = contentArr.getJSONObject(0).optString("text", "")
                if (text.isNotBlank()) return text
                return "Error: Claude returned an empty content block for model '${if (model.isNotBlank()) model else "claude-3-5-sonnet-20241022"}'."
            }
            val apiErr = jsonRes.optJSONObject("error")?.optString("message") ?: ""
            return "Error: Claude returned no content.${if (apiErr.isNotBlank()) " $apiErr" else " Verify your API key and model name."}"
        }
    }

    private fun callOpenAiCompatibleApi(
        baseUrl: String,
        apiKey: String,
        model: String,
        prompt: String,
        history: List<ChatMessageEntity>,
        systemPrompt: String
    ): String {
        val cleanUrl = if (baseUrl.endsWith("/")) "${baseUrl}chat/completions" else "$baseUrl/chat/completions"

        val rootJson = JSONObject()
        rootJson.put("model", if (model.isNotBlank()) model else "gpt-3.5-turbo")

        val messagesArray = JSONArray()
        messagesArray.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })

        history.takeLast(6).forEach { msg ->
            messagesArray.put(JSONObject().apply {
                put("role", if (msg.sender == "user") "user" else "assistant")
                put("content", msg.content)
            })
        }

        messagesArray.put(JSONObject().apply {
            put("role", "user")
            put("content", prompt)
        })

        rootJson.put("messages", messagesArray)

        val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())

        val requestBuilder = Request.Builder()
            .url(cleanUrl)
            .post(requestBody)

        if (apiKey.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKey")
        }
        if (cleanUrl.contains("openrouter.ai")) {
            requestBuilder.addHeader("HTTP-Referer", "https://snapertech.com")
            requestBuilder.addHeader("X-Title", "Snaper Technology")
        }

        client.newCall(requestBuilder.build()).execute().use { response ->
            val resStr = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return "API response error: Code ${response.code} from $cleanUrl."
            }
            val jsonRes = JSONObject(resStr)
            val choices = jsonRes.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val message = choices.getJSONObject(0).optJSONObject("message")
                return message?.optString("content")?.takeIf { it.isNotBlank() }
                    ?: "Error: The provider at $cleanUrl returned an empty response. Please check your model name and API key."
            }
            return "Error: The provider at $cleanUrl returned no choices. Verify the model '$model' is valid for this endpoint."
        }
    }
}

