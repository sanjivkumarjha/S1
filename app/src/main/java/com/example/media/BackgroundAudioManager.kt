package com.example.media

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AudioTrackInfo(
    val id: String,
    val title: String,
    val subtitle: String,
    val streamUrl: String
)

class BackgroundAudioManager private constructor(private val context: Context) : AudioManager.OnAudioFocusChangeListener {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<AudioTrackInfo?>(null)
    val currentTrack: StateFlow<AudioTrackInfo?> = _currentTrack.asStateFlow()

    private val _currentVolume = MutableStateFlow(1.0f)
    val currentVolume: StateFlow<Float> = _currentVolume.asStateFlow()

    private val _statusText = MutableStateFlow("Idle")
    val statusText: StateFlow<String> = _statusText.asStateFlow()

    private var wasPlayingBeforeFocusLoss = false
    private var isDuckedByAi = false

    val presetTracks = listOf(
        AudioTrackInfo("1", "Lofi Beats & Study Chills", "Relaxing Background Music", "https://stream.zeno.fm/f3wvbbqmdg8uv"),
        AudioTrackInfo("2", "Zen Meditation & Nature", "Calming Atmosphere", "https://stream.zeno.fm/4v3p3e0y6m8uv"),
        AudioTrackInfo("3", "Deep Focus & Ambient", "Productivity Music", "https://stream.zeno.fm/0r0xa792kwzuv"),
        AudioTrackInfo("4", "Radhe-Radhe Devotional Chants", "Spiritual Harmony", "https://stream.zeno.fm/08m4a792kwzuv")
    )

    init {
        if (presetTracks.isNotEmpty()) {
            _currentTrack.value = presetTracks.first()
        }
    }

    fun playTrack(track: AudioTrackInfo) {
        _currentTrack.value = track
        playStream(track.streamUrl)
    }

    fun playStream(url: String) {
        scope.launch(Dispatchers.IO) {
            try {
                if (requestAudioFocus()) {
                    releasePlayer()
                    mediaPlayer = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .build()
                        )
                        setDataSource(url)
                        setOnPreparedListener { mp ->
                            mp.start()
                            _isPlaying.value = true
                            _statusText.value = "Playing: ${_currentTrack.value?.title ?: "Background Audio"}"
                        }
                        setOnErrorListener { _, what, extra ->
                            Log.e("BackgroundAudio", "MediaPlayer error: $what, $extra")
                            _isPlaying.value = false
                            _statusText.value = "Stream playback error"
                            true
                        }
                        setOnCompletionListener {
                            _isPlaying.value = false
                            _statusText.value = "Completed"
                        }
                        prepareAsync()
                    }
                    _statusText.value = "Connecting stream..."
                }
            } catch (e: Exception) {
                Log.e("BackgroundAudio", "Error starting media player", e)
                _statusText.value = "Failed to load stream"
            }
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                _isPlaying.value = false
                _statusText.value = "Paused"
            }
        } catch (e: Exception) {
            Log.e("BackgroundAudio", "Error pausing player", e)
        }
    }

    fun resume() {
        try {
            if (requestAudioFocus()) {
                mediaPlayer?.start()
                _isPlaying.value = true
                _statusText.value = "Playing: ${_currentTrack.value?.title ?: "Background Audio"}"
            }
        } catch (e: Exception) {
            Log.e("BackgroundAudio", "Error resuming player", e)
        }
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            if (mediaPlayer != null) {
                resume()
            } else {
                _currentTrack.value?.let { playTrack(it) }
            }
        }
    }

    fun stop() {
        releasePlayer()
        abandonAudioFocus()
        _isPlaying.value = false
        _statusText.value = "Stopped"
    }

    // Called when AI starts speaking or listening (Smart Audio Focus Ducking)
    fun onAiVoiceInteractionStarted() {
        if (_isPlaying.value && mediaPlayer?.isPlaying == true) {
            isDuckedByAi = true
            mediaPlayer?.setVolume(0.15f, 0.15f)
            _currentVolume.value = 0.15f
            _statusText.value = "Ducked for AI Voice"
        }
    }

    // Called when AI finishes speaking or listening
    fun onAiVoiceInteractionEnded() {
        if (isDuckedByAi) {
            isDuckedByAi = false
            mediaPlayer?.setVolume(1.0f, 1.0f)
            _currentVolume.value = 1.0f
            _statusText.value = "Playing: ${_currentTrack.value?.title ?: "Background Audio"}"
        }
    }

    // Audio Focus Callback
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                wasPlayingBeforeFocusLoss = _isPlaying.value
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                wasPlayingBeforeFocusLoss = _isPlaying.value
                pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                mediaPlayer?.setVolume(0.2f, 0.2f)
                _currentVolume.value = 0.2f
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                mediaPlayer?.setVolume(1.0f, 1.0f)
                _currentVolume.value = 1.0f
                if (wasPlayingBeforeFocusLoss) {
                    wasPlayingBeforeFocusLoss = false
                    resume()
                }
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(false)
                .setOnAudioFocusChangeListener(this)
                .build()

            audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
    }

    private fun releasePlayer() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("BackgroundAudio", "Error releasing player", e)
        }
    }

    companion object {
        @Volatile
        private var instance: BackgroundAudioManager? = null

        fun getInstance(context: Context): BackgroundAudioManager {
            return instance ?: synchronized(this) {
                instance ?: BackgroundAudioManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
