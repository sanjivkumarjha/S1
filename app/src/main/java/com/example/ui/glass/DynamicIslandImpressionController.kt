package com.example.ui.glass

/**
 * Dynamic Island Impression Controller for the Snaper AI Assistant.
 * Manages the visual state and text displayed on the Dynamic Island overlay.
 */
object DynamicIslandImpressionController {

    private var currentState: DynamicIslandState = DynamicIslandState.IDLE
    private var currentText: String = ""

    fun setTalking(text: String) {
        currentState = DynamicIslandState.SPEAKING
        currentText = text
    }

    fun setListening() {
        currentState = DynamicIslandState.LISTENING
        currentText = "Listening..."
    }

    fun setThinking() {
        currentState = DynamicIslandState.THINKING
        currentText = "Thinking..."
    }

    fun setSleeping() {
        currentState = DynamicIslandState.SLEEPING
        currentText = ""
    }

    fun setSad(reason: String) {
        currentState = DynamicIslandState.SAD
        currentText = reason
    }

    fun setHappy(message: String) {
        currentState = DynamicIslandState.HAPPY
        currentText = message
    }

    fun setProcessing(task: String) {
        currentState = DynamicIslandState.PROCESSING
        currentText = task
    }

    fun getState(): DynamicIslandState = currentState
    fun getText(): String = currentText
}