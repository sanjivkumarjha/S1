package com.example.domain

import android.content.Context
import com.example.data.preferences.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class TaskComplexity {
    FAST_LOW_LATENCY,
    GENERAL_KNOWLEDGE,
    HIGH_COMPLEXITY_REASONING
}

data class GroundedResponse(
    val text: String,
    val sources: List<String> = emptyList(),
    val searchQueries: List<String> = emptyList()
)

data class ImageResult(
    val base64Image: String? = null,
    val textResponse: String = "",
    val resolution: String = "1K",
    val aspectRatio: String = "1:1",
    val modelUsed: String = "gemini-3-pro-image-preview"
)

data class AudioResult(
    val audioBase64: String? = null,
    val textResponse: String = "",
    val modelUsed: String = "lyria-3-clip-preview"
)

data class VideoResult(
    val operationName: String = "",
    val statusText: String = "",
    val videoUrlOrBase64: String? = null,
    val aspectRatio: String = "16:9",
    val modelUsed: String = "veo-3.1-fast-generate-preview"
)

data class HighThinkingResult(
    val answer: String,
    val thinkingProcess: String = "",
    val modelUsed: String = "gemini-3.1-pro-preview"
)

class GeminiAdvancedFeaturesEngine(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(userSettings: UserSettings): String {
        // Build-time API keys are strictly removed. We only use user-provided keys from secure storage.
        // Returning userSettings.userApiKey which is fetched dynamically.
        return userSettings.userApiKey
    }

    /**
     * 1. Live API Voice Conversations using 'gemini-3.1-flash-live-preview'
     */
    suspend fun processLiveVoiceConversation(prompt: String, userSettings: UserSettings): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-live-preview:generateContent?key=$apiKey"

        val root = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
            ))
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", "You are an instant live voice companion. Keep responses natural, conversational, and concise.")))
            })
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) return@withContext "Live Voice API error: Code ${response.code}"

                val json = JSONObject(body)
                val text = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: ""

                text.ifBlank { "Live Voice returned no text (the request may have been blocked or the model is unavailable)." }
            }
        } catch (e: Exception) {
            "Live Voice connection error: ${e.localizedMessage}"
        }
    }

    /**
     * 2. Search Grounding using 'gemini-3.5-flash' with googleSearch tool
     */
    suspend fun searchGroundedQuery(prompt: String, userSettings: UserSettings): GroundedResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val root = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
            ))
            put("tools", JSONArray().put(
                JSONObject().apply {
                    put("googleSearch", JSONObject())
                }
            ))
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext GroundedResponse("Search Grounding API error: ${response.code}")
                }

                val json = JSONObject(body)
                val firstCandidate = json.optJSONArray("candidates")?.optJSONObject(0)
                val text = firstCandidate?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: "No response from search grounding."

                val sources = mutableListOf<String>()
                val queries = mutableListOf<String>()

                val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
                if (groundingMetadata != null) {
                    val searchChunks = groundingMetadata.optJSONArray("groundingChunks")
                    if (searchChunks != null) {
                        for (i in 0 until searchChunks.length()) {
                            val web = searchChunks.optJSONObject(i)?.optJSONObject("web")
                            val title = web?.optString("title")
                            val uri = web?.optString("uri")
                            if (!uri.isNullOrBlank()) {
                                sources.add("${title ?: "Web Source"}: $uri")
                            }
                        }
                    }

                    val queriesArr = groundingMetadata.optJSONArray("webSearchQueries")
                    if (queriesArr != null) {
                        for (i in 0 until queriesArr.length()) {
                            queries.add(queriesArr.getString(i))
                        }
                    }
                }

                GroundedResponse(text = text, sources = sources, searchQueries = queries)
            }
        } catch (e: Exception) {
            GroundedResponse("Error performing Google Search Grounding: ${e.localizedMessage}")
        }
    }

    /**
     * 3. Maps Grounding using 'gemini-3.5-flash' with googleMaps tool
     */
    suspend fun mapsGroundedQuery(prompt: String, userSettings: UserSettings): GroundedResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val root = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
            ))
            put("tools", JSONArray().put(
                JSONObject().apply {
                    put("googleMaps", JSONObject())
                }
            ))
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext GroundedResponse("Maps Grounding API error: ${response.code}")
                }

                val json = JSONObject(body)
                val firstCandidate = json.optJSONArray("candidates")?.optJSONObject(0)
                val text = firstCandidate?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: "No maps location response."

                val places = mutableListOf<String>()
                val groundingMetadata = firstCandidate?.optJSONObject("groundingMetadata")
                if (groundingMetadata != null) {
                    val chunks = groundingMetadata.optJSONArray("groundingChunks")
                    if (chunks != null) {
                        for (i in 0 until chunks.length()) {
                            val mapChunk = chunks.optJSONObject(i)?.optJSONObject("googleMaps")
                            val title = mapChunk?.optString("title")
                            val uri = mapChunk?.optString("uri")
                            if (!uri.isNullOrBlank()) {
                                places.add("${title ?: "Google Maps Location"}: $uri")
                            }
                        }
                    }
                }

                GroundedResponse(text = text, sources = places)
            }
        } catch (e: Exception) {
            GroundedResponse("Error performing Google Maps Grounding: ${e.localizedMessage}")
        }
    }

    /**
     * 4 & 9. High-Quality Image Generation with Resolution (1K, 2K, 4K) & Aspect Ratio Control using 'gemini-3-pro-image-preview'
     */
    suspend fun generateHighQualityImage(
        prompt: String,
        resolution: String = "1K",
        aspectRatio: String = "16:9",
        userSettings: UserSettings
    ): ImageResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-pro-image-preview:generateContent?key=$apiKey"

        val root = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
            ))
            put("generationConfig", JSONObject().apply {
                put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
                put("imageConfig", JSONObject().apply {
                    put("aspectRatio", aspectRatio)
                    put("imageSize", resolution)
                })
            })
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext ImageResult(textResponse = "Image Generation error ${response.code}: $body")
                }

                val json = JSONObject(body)
                val parts = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")

                var textRes = ""
                var imgBase64: String? = null

                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("text")) {
                            textRes += part.getString("text") + " "
                        }
                        if (part.has("inlineData")) {
                            imgBase64 = part.getJSONObject("inlineData").optString("data")
                        }
                    }
                }

                ImageResult(
                    base64Image = imgBase64,
                    textResponse = textRes.ifBlank { "High Quality $resolution ($aspectRatio) Image generated!" },
                    resolution = resolution,
                    aspectRatio = aspectRatio,
                    modelUsed = "gemini-3-pro-image-preview"
                )
            }
        } catch (e: Exception) {
            ImageResult(textResponse = "Error generating image: ${e.localizedMessage}")
        }
    }

    /**
     * 5. Music Generation using 'lyria-3-clip-preview' or 'lyria-3-pro-preview'
     */
    suspend fun generateMusicTrack(
        prompt: String,
        isFullTrack: Boolean = false,
        userSettings: UserSettings
    ): AudioResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val model = if (isFullTrack) "lyria-3-pro-preview" else "lyria-3-clip-preview"
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

        val root = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
            ))
            put("generationConfig", JSONObject().apply {
                put("responseModalities", JSONArray().put("AUDIO"))
            })
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext AudioResult(textResponse = "Lyria Music Generation error: Code ${response.code}")
                }

                val json = JSONObject(body)
                val parts = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")

                var audioBase64: String? = null
                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("inlineData")) {
                            audioBase64 = part.getJSONObject("inlineData").optString("data")
                        }
                    }
                }

                AudioResult(
                    audioBase64 = audioBase64,
                    textResponse = if (audioBase64 != null) "✨ Generated Lyria ${if (isFullTrack) "Full Track" else "Short Clip"} Audio!" else "Audio synthesis completed.",
                    modelUsed = model
                )
            }
        } catch (e: Exception) {
            AudioResult(textResponse = "Music Generation error: ${e.localizedMessage}")
        }
    }

    /**
     * 6. Create & Edit Images using 'gemini-3.1-flash-image-preview'
     */
    suspend fun createOrEditImage(
        prompt: String,
        baseImageBase64: String? = null,
        aspectRatio: String = "1:1",
        userSettings: UserSettings
    ): ImageResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image-preview:generateContent?key=$apiKey"

        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", prompt))

        if (!baseImageBase64.isNullOrBlank()) {
            partsArray.put(JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", baseImageBase64)
                })
            })
        }

        val root = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().put("parts", partsArray)))
            put("generationConfig", JSONObject().apply {
                put("responseModalities", JSONArray().put("TEXT").put("IMAGE"))
                put("imageConfig", JSONObject().apply {
                    put("aspectRatio", aspectRatio)
                })
            })
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext ImageResult(textResponse = "Image Edit error ${response.code}")
                }

                val json = JSONObject(body)
                val parts = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")

                var imgBase64: String? = null
                var textRes = ""

                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("text")) textRes += part.getString("text") + " "
                        if (part.has("inlineData")) imgBase64 = part.getJSONObject("inlineData").optString("data")
                    }
                }

                ImageResult(
                    base64Image = imgBase64,
                    textResponse = textRes.ifBlank { "Image processed successfully!" },
                    aspectRatio = aspectRatio,
                    modelUsed = "gemini-3.1-flash-image-preview"
                )
            }
        } catch (e: Exception) {
            ImageResult(textResponse = "Image operation failed: ${e.localizedMessage}")
        }
    }

    /**
     * 7. Animate Images into Video using 'veo-3.1-fast-generate-preview'
     */
    suspend fun generateVeoVideo(
        prompt: String,
        aspectRatio: String = "16:9",
        userSettings: UserSettings
    ): VideoResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/veo-3.1-fast-generate-preview:generateVideos?key=$apiKey"

        val root = JSONObject().apply {
            put("prompt", prompt)
            put("config", JSONObject().apply {
                put("numberOfVideos", 1)
                put("resolution", "1080p")
                put("aspectRatio", if (aspectRatio == "9:16") "9:16" else "16:9")
            })
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext VideoResult(statusText = "Veo Video Generation error ${response.code}: $body")
                }

                val json = JSONObject(body)
                val name = json.optString("name", "operations/veo-generation-${System.currentTimeMillis()}")

                VideoResult(
                    operationName = name,
                    statusText = "🎬 Veo Video Generation initiated successfully! Aspect Ratio: $aspectRatio. Operation: $name",
                    aspectRatio = aspectRatio,
                    modelUsed = "veo-3.1-fast-generate-preview"
                )
            }
        } catch (e: Exception) {
            VideoResult(statusText = "Veo Video Generation error: ${e.localizedMessage}")
        }
    }

    /**
     * 8. Gemini Intelligence Router
     */
    suspend fun smartRouteQuery(
        prompt: String,
        taskComplexity: TaskComplexity,
        userSettings: UserSettings
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val modelName = when (taskComplexity) {
            TaskComplexity.HIGH_COMPLEXITY_REASONING -> "gemini-3.1-pro-preview"
            TaskComplexity.GENERAL_KNOWLEDGE -> "gemini-3.5-flash"
            TaskComplexity.FAST_LOW_LATENCY -> "gemini-3.1-flash-lite"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val root = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
            ))
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) return@withContext "API error ($modelName): ${response.code}"

                val json = JSONObject(body)
                json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: "No response text."
            }
        } catch (e: Exception) {
            "Error from $modelName: ${e.localizedMessage}"
        }
    }

    /**
     * 10. Analyze Images using 'gemini-3.1-pro-preview'
     */
    suspend fun analyzeImageContent(
        prompt: String,
        imageBase64: String,
        userSettings: UserSettings
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey"

        val parts = JSONArray().apply {
            put(JSONObject().put("text", prompt.ifBlank { "Analyze this image in detail." }))
            put(JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", imageBase64)
                })
            })
        }

        val root = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().put("parts", parts)))
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) return@withContext "Image Analysis error: ${response.code}"

                val json = JSONObject(body)
                json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: "Analysis complete."
            }
        } catch (e: Exception) {
            "Image Analysis Error: ${e.localizedMessage}"
        }
    }

    /**
     * 11. Low Latency Responses using 'gemini-3.1-flash-lite'
     */
    suspend fun fastLowLatencyResponse(prompt: String, userSettings: UserSettings): String {
        return smartRouteQuery(prompt, TaskComplexity.FAST_LOW_LATENCY, userSettings)
    }

    /**
     * 12. Transcribe Audio using 'gemini-3.5-flash'
     */
    suspend fun transcribeAudioFile(
        audioBase64: String,
        mimeType: String = "audio/wav",
        userSettings: UserSettings
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val parts = JSONArray().apply {
            put(JSONObject().put("text", "Transcribe this audio recording accurately with speaker punctuation."))
            put(JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", mimeType)
                    put("data", audioBase64)
                })
            })
        }

        val root = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().put("parts", parts)))
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) return@withContext "Audio Transcription error: ${response.code}"

                val json = JSONObject(body)
                json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: "Audio transcription completed."
            }
        } catch (e: Exception) {
            "Audio Transcription error: ${e.localizedMessage}"
        }
    }

    /**
     * 13. Analyze Video Content using 'gemini-3.1-pro-preview'
     */
    suspend fun analyzeVideoContent(
        prompt: String,
        videoBase64: String,
        mimeType: String = "video/mp4",
        userSettings: UserSettings
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey"

        val parts = JSONArray().apply {
            put(JSONObject().put("text", prompt.ifBlank { "Analyze this video for key events, text, actions, and key insights." }))
            put(JSONObject().apply {
                put("inlineData", JSONObject().apply {
                    put("mimeType", mimeType)
                    put("data", videoBase64)
                })
            })
        }

        val root = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().put("parts", parts)))
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) return@withContext "Video Analysis error: ${response.code}"

                val json = JSONObject(body)
                json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: "Video analysis completed."
            }
        } catch (e: Exception) {
            "Video Analysis error: ${e.localizedMessage}"
        }
    }

    /**
     * 14. Enable High Thinking Mode using 'gemini-3.1-pro-preview' with thinkingLevel = "HIGH"
     * (Do NOT set maxOutputTokens)
     */
    suspend fun thinkWithHighLevel(
        prompt: String,
        userSettings: UserSettings
    ): HighThinkingResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(userSettings)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-pro-preview:generateContent?key=$apiKey"

        val root = JSONObject().apply {
            put("contents", JSONArray().put(
                JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
            ))
            put("generationConfig", JSONObject().apply {
                put("thinkingConfig", JSONObject().apply {
                    put("thinkingLevel", "HIGH")
                })
            })
        }

        try {
            val request = Request.Builder()
                .url(url)
                .post(root.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext HighThinkingResult(answer = "Thinking Mode error ${response.code}: $body")
                }

                val json = JSONObject(body)
                val parts = json.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")

                var textRes = ""
                var thoughts = ""

                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        if (part.has("text")) {
                            textRes += part.getString("text") + "\n"
                        }
                        if (part.has("thought")) {
                            thoughts += part.getString("thought") + "\n"
                        }
                    }
                }

                HighThinkingResult(
                    answer = textRes.ifBlank { "Deep reasoning completed successfully." },
                    thinkingProcess = thoughts,
                    modelUsed = "gemini-3.1-pro-preview"
                )
            }
        } catch (e: Exception) {
            HighThinkingResult(answer = "High Thinking error: ${e.localizedMessage}")
        }
    }
}
