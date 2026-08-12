package com.example.avatar

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AvatarExpression {
    HAPPY, CONCERNED, LISTENING, THINKING, SECURITY_ALERT, SPEAKING, SMILING, SURPRISED, GENTLE_ANGER, LAUGHING
}

enum class AvatarFormat {
    IMAGE, ANIMATED_IMAGE, VIDEO, ASSET_3D
}

enum class AvatarCategory {
    DEFAULT, MY_AVATARS, VIDEO_AVATARS, FAVORITES, RECENT
}

data class AvatarModel(
    val id: String,
    val name: String,
    val category: AvatarCategory,
    val format: AvatarFormat,
    val resourceUri: String, // local drawable or asset uri
    val isDefault: Boolean = false,
    val isFavorite: Boolean = false
)

/**
 * Manages the AI Avatar System, expressions, lip sync visemes, and Avatar Gallery.
 */
class AgentAvatarManager(private val context: Context) {

    private val defaultAvatars = listOf(
        AvatarModel(
            id = "avatar_3d_cyber",
            name = "Snaper 3D Cyber Avatar (3D)",
            category = AvatarCategory.DEFAULT,
            format = AvatarFormat.ASSET_3D,
            resourceUri = "3d_cyber_snaper",
            isDefault = true,
            isFavorite = true
        ),
        AvatarModel(
            id = "avatar_3d_hologram",
            name = "3D Hologram Guardian (3D)",
            category = AvatarCategory.DEFAULT,
            format = AvatarFormat.ASSET_3D,
            resourceUri = "3d_hologram_guardian",
            isDefault = false
        ),
        AvatarModel(
            id = "avatar_snaper_ai",
            name = "Snaper AI (Animated 2D)",
            category = AvatarCategory.DEFAULT,
            format = AvatarFormat.ANIMATED_IMAGE,
            resourceUri = "snaper_female_ai",
            isDefault = false
        ),
        AvatarModel(
            id = "avatar_cyber_glass",
            name = "Cyber Glass Avatar",
            category = AvatarCategory.DEFAULT,
            format = AvatarFormat.IMAGE,
            resourceUri = "cyber_glass_avatar",
            isDefault = false
        ),
        AvatarModel(
            id = "avatar_hologram_video",
            name = "Hologram Video Agent",
            category = AvatarCategory.VIDEO_AVATARS,
            format = AvatarFormat.VIDEO,
            resourceUri = "hologram_agent_video",
            isDefault = false
        )
    )

    private val _currentAvatarState = MutableStateFlow(defaultAvatars[0])
    val currentAvatarState: StateFlow<AvatarModel> = _currentAvatarState.asStateFlow()

    private val _currentExpressionState = MutableStateFlow(AvatarExpression.HAPPY)
    val currentExpressionState: StateFlow<AvatarExpression> = _currentExpressionState.asStateFlow()

    private val _isSpeakingState = MutableStateFlow(false)
    val isSpeakingState: StateFlow<Boolean> = _isSpeakingState.asStateFlow()

    private val _visemeFrameState = MutableStateFlow(0) // 0: Closed, 1: Half, 2: Open, 3: Wide
    val visemeFrameState: StateFlow<Int> = _visemeFrameState.asStateFlow()

    private val _galleryListState = MutableStateFlow(defaultAvatars)
    val galleryListState: StateFlow<List<AvatarModel>> = _galleryListState.asStateFlow()

    fun setExpression(expression: AvatarExpression) {
        _currentExpressionState.value = expression
    }

    fun setSpeaking(isSpeaking: Boolean) {
        _isSpeakingState.value = isSpeaking
        if (isSpeaking) {
            _currentExpressionState.value = AvatarExpression.SPEAKING
        } else if (_currentExpressionState.value == AvatarExpression.SPEAKING) {
            _currentExpressionState.value = AvatarExpression.HAPPY
            _visemeFrameState.value = 0
        }
    }

    fun updateLipSyncVisemeFrame(audioAmplitude: Float) {
        if (!_isSpeakingState.value) {
            _visemeFrameState.value = 0
            return
        }
        _visemeFrameState.value = when {
            audioAmplitude > 0.7f -> 3
            audioAmplitude > 0.4f -> 2
            audioAmplitude > 0.1f -> 1
            else -> 0
        }
    }

    fun selectAvatarById(id: String): String {
        val match = _galleryListState.value.find { it.id == id }
        if (match != null) {
            _currentAvatarState.value = match
            return "Avatar switched to '${match.name}'."
        }
        return "Avatar not found in gallery."
    }

    fun selectNextAvatar(): String {
        val list = _galleryListState.value
        val currentIndex = list.indexOfFirst { it.id == _currentAvatarState.value.id }
        val nextIndex = (currentIndex + 1) % list.size
        _currentAvatarState.value = list[nextIndex]
        return "Boss, avatar changed to '${list[nextIndex].name}'."
    }

    fun addCustomAvatar(name: String, uri: String, format: AvatarFormat = AvatarFormat.IMAGE): String {
        val newAvatar = AvatarModel(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            category = AvatarCategory.MY_AVATARS,
            format = format,
            resourceUri = uri
        )
        _galleryListState.value = _galleryListState.value + newAvatar
        _currentAvatarState.value = newAvatar
        return "Custom avatar '$name' added and activated!"
    }
}
