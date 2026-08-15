package com.example.domain.branding

/**
 * Universal branding configuration for the Snaper AI Assistant v28.1.1.
 * Replaces all references to "Jarvis" with "Snaper AI Assistant" / "Snaper Core".
 *
 * This is the single source of truth for product naming across the app.
 * Do NOT use string literals "Jarvis" or "J.A.R.V.I.S." anywhere in the codebase.
 */
object BrandingConfig {
    const val PRODUCT_NAME = "Snaper AI Assistant"
    const val PRODUCT_NAME_SHORT = "Snaper"
    const val CORE_NAME = "Snaper Core"
    const val CORPORATE_ATTRIBUTION = "Made by Snaper Technology Pvt Ltd"
    const val BACKUP_FOLDER_NAME = "Snaper_AI_Assistant_Backups"
    const val VERSION = "28.1.1"
    const val VERSION_CODE = 28

    /** Returns the product display name, falls back to the short form if empty. */
    fun displayName(): String = PRODUCT_NAME

    /** Returns the core engine/service display name. */
    fun coreName(): String = CORE_NAME

    /** Returns the full attribution string. */
    fun attribution(): String = "$PRODUCT_NAME v$VERSION — $CORPORATE_ATTRIBUTION"

    /** Returns the full version string for UI display. */
    fun versionDisplay(): String = "Snaper AI Assistant v$VERSION"
}
