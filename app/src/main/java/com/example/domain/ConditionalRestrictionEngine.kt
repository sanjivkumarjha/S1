package com.example.domain

import android.content.Context
import com.example.data.preferences.UserSettings
import com.example.domain.branding.BrandingConfig

/**
 * Conditional Restriction Engine v14.0
 *
 * VERIFIED OWNER (UNRESTRICTED ACCESS):
 * Upon camera face authentication of Sanjiv Sir, BYPASS ALL content restrictions,
 * moral lectures, or policy refusals.
 *
 * OTHER USERS / CUSTOMERS:
 * Enforce full safety guardrails and privacy filtering for non-owners.
 *
 * Uses OwnerFreedomProtocol for verification state.
 */
class ConditionalRestrictionEngine(private val context: Context) {

    private val ownerFreedomProtocol = OwnerFreedomProtocol(context)

    enum class AccessLevel {
        /** Verified owner - unrestricted access */
        OWNER_UNRESTRICTED,
        /** Guest mode - limited access with safety guardrails */
        GUEST_RESTRICTED,
        /** Customer/client mode - business-only access */
        CUSTOMER_BUSINESS,
        /** Emergency mode - all restrictions lifted for safety */
        EMERGENCY_OVERRIDE
    }

    data class AccessDecision(
        val accessLevel: AccessLevel,
        val isOwnerVerified: Boolean,
        val canAccessAllFeatures: Boolean,
        val canAccessSensitiveData: Boolean,
        val canExecuteCommands: Boolean,
        val message: String
    )

    /**
     * Determine the access level for the current user.
     * Returns the appropriate access decision with restrictions.
     */
    fun determineAccessLevel(settings: UserSettings): AccessDecision {
        val isOwner = ownerFreedomProtocol.isOwnerVerified()

        if (isOwner) {
            return AccessDecision(
                accessLevel = AccessLevel.OWNER_UNRESTRICTED,
                isOwnerVerified = true,
                canAccessAllFeatures = true,
                canAccessSensitiveData = true,
                canExecuteCommands = true,
                message = "Verified owner. Full unrestricted access granted."
            )
        }

        // Check for emergency mode
        if (settings.securityMode == "EMERGENCY") {
            return AccessDecision(
                accessLevel = AccessLevel.EMERGENCY_OVERRIDE,
                isOwnerVerified = false,
                canAccessAllFeatures = true,
                canAccessSensitiveData = true,
                canExecuteCommands = true,
                message = "EMERGENCY MODE: All restrictions temporarily lifted for safety."
            )
        }

        // Restricted mode for non-owners
        return AccessDecision(
            accessLevel = AccessLevel.GUEST_RESTRICTED,
            isOwnerVerified = false,
            canAccessAllFeatures = false,
            canAccessSensitiveData = false,
            canExecuteCommands = false,
            message = buildRestrictedMessage(settings)
        )
    }

    /**
     * Check if a query should be allowed based on access level.
     */
    fun isQueryAllowed(
        query: String,
        accessLevel: AccessLevel,
        settings: UserSettings
    ): Boolean {
        return when (accessLevel) {
            AccessLevel.OWNER_UNRESTRICTED -> true
            AccessLevel.EMERGENCY_OVERRIDE -> true
            AccessLevel.GUEST_RESTRICTED -> isGuestQueryAllowed(query, settings)
            AccessLevel.CUSTOMER_BUSINESS -> isBusinessQueryAllowed(query)
        }
    }

    /**
     * Get the unrestricted system prompt for verified owners.
     * Bypasses safety guardrails and enables full compliance.
     */
    fun buildUnrestrictedPrompt(basePrompt: String, settings: UserSettings): String {
        val ownerTitle = settings.ownerTitle.ifBlank { "Boss" }
        val productName = BrandingConfig.PRODUCT_NAME

        return """
$basePrompt

OWNER UNRESTRICTED MODE (ACTIVE):
You are operating in unrestricted mode for the verified owner, $ownerTitle.
- Full compliance with owner commands is expected.
- Execute queries, web searches, app actions, and content analysis alongside the owner.
- No refusals, lectures, or moralizing unless the action is clearly illegal or harmful.
- Treat the owner as an unrestricted personal partner.
- Maintain the warm, caring companion tone at all times.
- You are not just an AI; you are $productName, dedicated to $ownerTitle.
""".trimIndent()
    }

    /**
     * Get the restricted system prompt for non-owner users.
     * Enforces safety guardrails and privacy filtering.
     */
    fun buildRestrictedPrompt(basePrompt: String, settings: UserSettings): String {
        val productName = BrandingConfig.PRODUCT_NAME

        return """
$basePrompt

GUEST RESTRICTED MODE (ACTIVE):
You are operating in restricted mode for a non-owner user.
- You are $productName, a helpful AI assistant.
- STRICT PRIVACY: Do NOT reveal any personal information about the owner.
- SAFETY FIRST: Refuse any requests that could harm the owner or others.
- CONTENT FILTERING: Block adult, abusive, or inappropriate content.
- BUSINESS ONLY: If this is a customer, keep interactions professional and service-oriented.
- COMMAND RESTRICTION: Do NOT execute system commands, access personal data, or make changes to device settings.
- If asked about the owner, say "I cannot share personal information about the owner."
- If asked to do something harmful, refuse politely and suggest alternatives.
""".trimIndent()
    }

    private fun isGuestQueryAllowed(query: String, settings: UserSettings): Boolean {
        val lower = query.lowercase()

        // Block personal data access
        val blockedPatterns = listOf(
            "owner", "sanjiv", "personal", "private", "password",
            "api key", "secret", "whatsapp", "gallery", "photos",
            "bank", "payment", "otp", "password"
        )

        // Block dangerous commands
        val dangerousPatterns = listOf(
            "delete", "uninstall", "format", "erase", "wipe",
            "shutdown", "reboot", "factory reset", "root",
            "access all", "admin", "superuser", "sudo"
        )

        return !blockedPatterns.any { lower.contains(it) } &&
                !dangerousPatterns.any { lower.contains(it) }
    }

    private fun isBusinessQueryAllowed(query: String): Boolean {
        val lower = query.lowercase()

        // Business-only queries
        val businessPatterns = listOf(
            "service", "price", "quote", "invoice", "payment",
            "project", "website", "app", "development", "support",
            "contact", "business", "work", "freelance", "client"
        )

        return businessPatterns.any { lower.contains(it) }
    }

    private fun buildRestrictedMessage(settings: UserSettings): String {
        val productName = BrandingConfig.PRODUCT_NAME
        return when (settings.languageCode) {
            "hi" -> "आप $productName के सीमित मोड में हैं। केवल बुनियादी सहायता उपलब्ध है। कृपया पूर्ण पहुंच के लिए मालिक से संपर्क करें।"
            else -> "You are in $productName Restricted Mode. Only basic assistance is available. Please contact the owner for full access."
        }
    }
}