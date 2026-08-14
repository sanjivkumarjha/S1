package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceAssistantManager(private val context: Context) : TextToSpeech.OnInitListener, RecognitionListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _speechAmplitude = MutableStateFlow(0f)
    val speechAmplitude: StateFlow<Float> = _speechAmplitude.asStateFlow()

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                // Set default female voice characteristics
                tts?.setPitch(1.25f)
                tts?.setSpeechRate(1.0f)
                _isTtsReady.value = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        com.example.media.BackgroundAudioManager.getInstance(context).onAiVoiceInteractionStarted()
                        com.example.ui.glass.DynamicIslandImpressionController.setTalking(_spokenText.value.ifBlank { "Speaking..." })
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        com.example.media.BackgroundAudioManager.getInstance(context).onAiVoiceInteractionEnded()
                        com.example.ui.glass.DynamicIslandImpressionController.setSleeping()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        com.example.media.BackgroundAudioManager.getInstance(context).onAiVoiceInteractionEnded()
                        com.example.ui.glass.DynamicIslandImpressionController.setSad("Speech interrupted")
                    }
                })
            }
        }
    }

    fun setVoiceCharacteristics(pitch: Float, rate: Float) {
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)
    }

    fun speak(text: String, languageCode: String = "en", utteranceId: String = "SnaperSpeech") {
        if (_isTtsReady.value) {
            // Stop speech recognition if listening
            stopListening()
            _isSpeaking.value = true

            val locale = when (languageCode.lowercase()) {
                "hi", "mai", "bho" -> Locale("hi", "IN")
                "gu" -> Locale("gu", "IN")
                "mr" -> Locale("mr", "IN")
                "ta" -> Locale("ta", "IN")
                "te" -> Locale("te", "IN")
                "bn" -> Locale("bn", "IN")
                "pa" -> Locale("pa", "IN")
                "kn" -> Locale("kn", "IN")
                "ml" -> Locale("ml", "IN")
                "or" -> Locale("or", "IN")
                else -> Locale("en", "IN")
            }

            tts?.language = locale
            // High-pitched, natural sweet female voice tuning
            tts?.setPitch(1.28f)
            tts?.setSpeechRate(1.0f)

            // Clean markdown tags for natural speech
            val cleanText = text.replace(Regex("\\*.*?\\*"), "")
                .replace("#", "")
                .replace("`", "")
            _spokenText.value = cleanText
            com.example.ui.glass.DynamicIslandImpressionController.setTalking(cleanText)
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
        com.example.ui.glass.DynamicIslandImpressionController.setSleeping()
    }

    fun startListening(languageCode: String = "en") {
        stopSpeaking()
        com.example.ui.glass.DynamicIslandImpressionController.setListening()
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            try {
                speechRecognizer?.startListening(intent)
                _isListening.value = true
            } catch (e: Exception) {
                _isListening.value = false
            }
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // Ignore
        }
        _isListening.value = false
        _speechAmplitude.value = 0f
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }

    // RecognitionListener callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {
        // Normalize RMS dB to 0.0 - 1.0 range for animation
        val norm = ((rmsdB + 2) / 12f).coerceIn(0f, 1f)
        _speechAmplitude.value = norm
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _speechAmplitude.value = 0f
    }

    var onWakeWordDetected: ((String) -> Unit)? = null
    var wakeWords: List<String> = listOf("snaper", "gemini", "hey snaper", "radhe radhe", "alexa", "siri")

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val recognized = matches[0]
            _spokenText.value = recognized
            checkWakeWord(recognized)
        }
        _isListening.value = false
        _speechAmplitude.value = 0f
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val recognized = matches[0]
            _spokenText.value = recognized
            checkWakeWord(recognized)
        }
    }

    private fun checkWakeWord(text: String) {
        val lower = text.lowercase()
        wakeWords.forEach { word ->
            if (lower.contains(word)) {
                onWakeWordDetected?.invoke(text)
                return
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}
