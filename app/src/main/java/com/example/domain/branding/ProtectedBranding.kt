package com.example.domain.branding

/**
 * IMMUTABLE APPLICATION BRANDING
 * 
 * Absolute Immutability Requirement:
 * These two values are permanently protected application-level branding constants.
 * They must NEVER be editable, changeable, deleted, or overridden from Splash Screen,
 * Settings, Themes, Home Screen, Widgets, Import/Export, AI Customization, or any
 * other user-accessible interface.
 */
object ProtectedBranding {
    const val PROTECTED_GREETING: String = "राधे राधे"
    const val PROTECTED_BRAND_CREDIT: String = "Made by Snaper Technology Private Limited"

    /**
     * Enforces non-bypassable validation for imported or custom configurations.
     * Always restores protected original values if an edit attempt occurs.
     */
    fun sanitizeGreeting(value: String?): String {
        return PROTECTED_GREETING
    }

    fun sanitizeBrandCredit(value: String?): String {
        return PROTECTED_BRAND_CREDIT
    }
}
