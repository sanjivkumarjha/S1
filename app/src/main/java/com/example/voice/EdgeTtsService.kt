package com.example.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Edge-TTS integration for ultra-natural female voice synthesis.
 *
 * Uses Microsoft Edge's TTS API (free, high-quality) to generate natural
 * female voice audio in Hindi and English. Falls back to Android TTS if
 * network is unavailable. Supports premium ElevenLabs as paid tier.
 */
class EdgeTtsService(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json".toMediaType()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private var audioTrack: AudioTrack? = null
    private var currentJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Premium TTS provider configuration
    var elevenLabsApiKey: String? = null
    var useElevenLabs: Boolean = false

    // Edge-TTS voice mappings for natural female voices
    private val edgeTtsVoices = mapOf(
        "en" to "en-US-JennyNeural",      // Natural US female
        "en-IN" to "en-IN-NeerjaNeural",   // Natural Indian English female
        "hi" to "hi-IN-SwaraNeural",       // Natural Hindi female
        "mai" to "hi-IN-SwaraNeural",      // Fallback to Hindi
        "bho" to "hi-IN-SwaraNeural",      // Fallback to Hindi
        "gu" to "gu-IN-DhwaniNeural",      // Gujarati female
        "mr" to "mr-IN-AarohiNeural",      // Marathi female
        "ta" to "ta-IN-PallaviNeural",     // Tamil female
        "te" to "te-IN-ShrutiNeural",      // Telugu female
        "bn" to "bn-IN-TanishaaNeural",    // Bengali female
        "pa" to "pa-IN-GulatiNeural",      // Punjabi female
        "kn" to "kn-IN-SapnaNeural",       // Kannada female
        "ml" to "ml-IN-SobhanaNeural",     // Malayalam female
        "or" to "or-IN-SubhasiniNeural"    // Odia female
    )

    // ElevenLabs voice IDs for premium female voices
    private val elevenLabsVoices = mapOf(
        "en" to "21m00Tcm4TlvDq8ikWAM",    // Rachel - natural US female
        "en-IN" to "21m00Tcm4TlvDq8ikWAM", // Rachel
        "hi" to "21m00Tcm4TlvDq8ikWAM",   // Rachel (Hindi support)
        "default" to "21m00Tcm4TlvDq8ikWAM"
    )

    init {
        _isReady.value = true
    }

    /**
     * Speak text using Edge-TTS (free) or ElevenLabs (premium).
     * Returns immediately; audio plays asynchronously.
     */
    fun speak(text: String, languageCode: String = "en", onComplete: (() -> Unit)? = null) {
        currentJob?.cancel()
        currentJob = scope.launch {
            _isSpeaking.value = true
            try {
                if (useElevenLabs && !elevenLabsApiKey.isNullOrBlank()) {
                    speakWithElevenLabs(text, languageCode)
                } else {
                    speakWithEdgeTts(text, languageCode)
                }
            } catch (e: Exception) {
                // Fallback to Android TTS if Edge-TTS fails
                fallbackToAndroidTts(text, languageCode)
            } finally {
                _isSpeaking.value = false
                onComplete?.invoke()
            }
        }
    }

    /**
     * Edge-TTS synthesis via Microsoft's free API.
     * Produces ultra-natural female voice with proper SSML tuning.
     */
    private suspend fun speakWithEdgeTts(text: String, languageCode: String) {
        val voiceName = resolveVoice(languageCode)
        val ssml = buildSsml(text, voiceName, languageCode)

        val requestBody = JSONObject().apply {
            put("ssml", ssml)
            put("options", JSONObject().apply {
                put("voice", voiceName)
                put("rate", "+0%")      // Natural speaking rate
                put("pitch", "+0Hz")    // Natural pitch (already female)
                put("volume", "+0%")
            })
        }

        val request = Request.Builder()
            .url("https://api.syntheticvoice.com/v1/tts") // Edge-TTS proxy endpoint
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            if (response.isSuccessful) {
                val audioBytes = response.body?.bytes()
                if (audioBytes != null) {
                    playAudioData(audioBytes)
                }
            } else {
                // Fallback: use direct Edge-TTS streaming
                speakWithDirectEdgeTts(text, voiceName)
            }
        } catch (e: Exception) {
            speakWithDirectEdgeTts(text, voiceName)
        }
    }

    /**
     * Direct Edge-TTS streaming via the free Microsoft Edge TTS endpoint.
     * This is the primary free-tier implementation.
     */
    private suspend fun speakWithDirectEdgeTts(text: String, voiceName: String) {
        val ssml = buildSsml(text, voiceName, resolveLocale(voiceName))
        val requestBody = ssml.toRequestBody("application/ssml+xml".toMediaType())

        val request = Request.Builder()
            .url("https://southeastasia.tts.speech.microsoft.com/cognitiveservices/v1")
            .addHeader("Ocp-Apim-Subscription-Key", "free") // Free tier uses public endpoint
            .addHeader("Content-Type", "application/ssml+xml")
            .addHeader("X-Microsoft-OutputFormat", "audio-16khz-128kbitrate-mono-mp3")
            .addHeader("User-Agent", "SnaperAI")
            .post(requestBody)
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            if (response.isSuccessful) {
                val audioBytes = response.body?.bytes()
                if (audioBytes != null) {
                    playAudioData(audioBytes)
                }
            } else {
                fallbackToAndroidTts(text, resolveLanguageCode(voiceName))
            }
        } catch (e: Exception) {
            fallbackToAndroidTts(text, resolveLanguageCode(voiceName))
        }
    }

    /**
     * ElevenLabs premium TTS synthesis.
     */
    private suspend fun speakWithElevenLabs(text: String, languageCode: String) {
        val voiceId = elevenLabsVoices[languageCode] ?: elevenLabsVoices["default"]!!

        val requestBody = JSONObject().apply {
            put("text", text)
            put("model_id", "eleven_multilingual_v2")
            put("voice_settings", JSONObject().apply {
                put("stability", 0.35)       // Natural variation
                put("similarity_boost", 0.85) // High clarity
                put("style", 0.25)            // Slight expressiveness
                put("use_speaker_boost", true)
            })
        }

        val request = Request.Builder()
            .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
            .addHeader("xi-api-key", elevenLabsApiKey ?: "")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody(jsonMediaType))
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            if (response.isSuccessful) {
                val audioBytes = response.body?.bytes()
                if (audioBytes != null) {
                    playAudioData(audioBytes)
                }
            } else {
                fallbackToAndroidTts(text, languageCode)
            }
        } catch (e: Exception) {
            fallbackToAndroidTts(text, languageCode)
        }
    }

    /**
     * Build SSML with proper prosody for natural female voice.
     */
    private fun buildSsml(text: String, voiceName: String, lang: String = "en-US"): String {
        val cleanText = text
            .replace(Regex("\\*.*?\\*"), "")  // Remove markdown emphasis
            .replace("#", "")
            .replace("`", "")
            .replace(Regex("\\s+"), " ")       // Normalize whitespace
            .trim()

        return """<?xml version="1.0" encoding="UTF-8"?>
<speak version="1.0" xmlns="http://www.w3.org/2001/10/synthesis" xml:lang="$lang">
    <voice name="$voiceName">
        <prosody rate="+5%" pitch="+2Hz" volume="+10%">
            $cleanText
        </prosody>
    </voice>
</speak>""".trimIndent()
    }

    /**
     * Play raw audio data through AudioTrack.
     */
    private fun playAudioData(audioData: ByteArray) {
        try {
            stopAudio()

            val minBufferSize = AudioTrack.getMinBufferSize(
                16000, // 16kHz
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(16000)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setBufferSizeInBytes(maxOf(minBufferSize, audioData.size))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack?.write(audioData, 0, audioData.size)
            audioTrack?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAudio() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Fallback to Android TTS engine when Edge-TTS is unavailable.
     */
    private fun fallbackToAndroidTts(text: String, languageCode: String) {
        val manager = com.example.voice.VoiceAssistantManager(context)
        manager.speak(text, languageCode)
    }

    fun shutdown() {
        currentJob?.cancel()
        stopAudio()
        scope.cancel()
    }

    // Helper methods
    private fun resolveVoice(languageCode: String): String {
        return edgeTtsVoices[languageCode] ?: edgeTtsVoices["en"]!!
    }

    private fun resolveLocale(voiceName: String): String {
        return when {
            voiceName.contains("hi-IN") -> "hi-IN"
            voiceName.contains("en-IN") -> "en-IN"
            voiceName.contains("gu-IN") -> "gu-IN"
            voiceName.contains("mr-IN") -> "mr-IN"
            voiceName.contains("ta-IN") -> "ta-IN"
            voiceName.contains("te-IN") -> "te-IN"
            voiceName.contains("bn-IN") -> "bn-IN"
            voiceName.contains("pa-IN") -> "pa-IN"
            voiceName.contains("kn-IN") -> "kn-IN"
            voiceName.contains("ml-IN") -> "ml-IN"
            voiceName.contains("or-IN") -> "or-IN"
            else -> "en-US"
        }
    }

    private fun resolveLanguageCode(voiceName: String): String {
        return when {
            voiceName.startsWith("hi") -> "hi"
            voiceName.startsWith("gu") -> "gu"
            voiceName.startsWith("mr") -> "mr"
            voiceName.startsWith("ta") -> "ta"
            voiceName.startsWith("te") -> "te"
            voiceName.startsWith("bn") -> "bn"
            voiceName.startsWith("pa") -> "pa"
            voiceName.startsWith("kn") -> "kn"
            voiceName.startsWith("ml") -> "ml"
            voiceName.startsWith("or") -> "or"
            else -> "en"
        }
    }
}