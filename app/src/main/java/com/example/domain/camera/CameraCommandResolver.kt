package com.example.domain.camera

import android.content.Context
import androidx.camera.core.CameraSelector

enum class CameraActionType {
    TAKE_PHOTO,
    TAKE_SELFIE,
    SWITCH_FRONT,
    SWITCH_BACK,
    OPEN_CAMERA,
    REALTIME_VISION,
    NONE
}

data class CameraCommandResult(
    val actionType: CameraActionType,
    val cameraSelector: CameraSelector,
    val description: String,
    val statusMessage: String,
    val auditNote: String = "Existing camera capability detected. Reusing existing implementation. No duplicate camera subsystem created."
)

object CameraCommandResolver {

    fun resolveCommand(command: String): CameraCommandResult {
        val lower = command.lowercase().trim()

        return when {
            lower.contains("selfie") || lower.contains("front camera") -> {
                CameraCommandResult(
                    actionType = CameraActionType.TAKE_SELFIE,
                    cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
                    description = "Take Selfie with Front Camera",
                    statusMessage = "Activating Front Camera for Selfie..."
                )
            }
            lower.contains("back camera") || lower.contains("rear camera") -> {
                CameraCommandResult(
                    actionType = CameraActionType.SWITCH_BACK,
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                    description = "Switch to Back Camera",
                    statusMessage = "Activating Rear Primary Camera..."
                )
            }
            lower.contains("take a photo") || lower.contains("snap photo") || lower.contains("capture photo") -> {
                CameraCommandResult(
                    actionType = CameraActionType.TAKE_PHOTO,
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                    description = "Capture Photo",
                    statusMessage = "Capturing Photo..."
                )
            }
            lower.contains("what do you see") || lower.contains("describe scene") || lower.contains("live vision") -> {
                CameraCommandResult(
                    actionType = CameraActionType.REALTIME_VISION,
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                    description = "Real-Time Vision Processing",
                    statusMessage = "Analyzing Live Scene with AI Vision..."
                )
            }
            lower.contains("camera") -> {
                CameraCommandResult(
                    actionType = CameraActionType.OPEN_CAMERA,
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                    description = "Open Camera Preview",
                    statusMessage = "Opening Camera Preview..."
                )
            }
            else -> {
                CameraCommandResult(
                    actionType = CameraActionType.NONE,
                    cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
                    description = "None",
                    statusMessage = "No camera action detected."
                )
            }
        }
    }
}
