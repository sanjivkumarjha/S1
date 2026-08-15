package com.example.ui.glass

/**
 * Dynamic Island State for the Snaper AI Assistant overlay.
 * Controls the visual state of the Dynamic Island on the lock screen/AOD.
 */
enum class DynamicIslandState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    SLEEPING,
    SAD,
    HAPPY,
    PROCESSING
}