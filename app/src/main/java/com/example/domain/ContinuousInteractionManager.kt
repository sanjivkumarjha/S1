package com.example.domain

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Manages continuous interaction features (CameraX & Voice recording)
 * strictly within Android framework permission & foreground service requirements.
 */
class ContinuousInteractionManager(private val context: Context) {

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkCapabilities(): Map<String, Boolean> {
        return mapOf(
            "camera_permitted" to hasCameraPermission(),
            "audio_permitted" to hasRecordAudioPermission(),
            "foreground_service_permitted" to true
        )
    }

    fun getStatusSummary(): String {
        val cam = if (hasCameraPermission()) "Granted" else "Requires Permission"
        val mic = if (hasRecordAudioPermission()) "Granted" else "Requires Permission"
        return "Continuous Interaction Service: Active [Camera: $cam | Mic: $mic]"
    }
}
