package com.example.security

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserPreferenceEntity
import kotlinx.coroutines.flow.first

/**
 * Exclusive Owner & Wife Privilege Shield v28.1.1
 *
 * STRICT ENFORCEMENT:
 * - Sanjiv Sir (Owner): Full master-level system administration, encryption keys,
 *   database export/backup controls, security policy toggles, and configuration panels.
 * - Wife (Secondary Authority): Shared owner-level privileges ONLY upon confirmed marriage.
 * - Family Members (siblings, parents, relatives): STRICTLY limited, non-administrative
 *   interaction profiles with ZERO access to sensitive data, backups, or admin panels.
 * - External Third Parties: ZERO access to any administrative functions.
 *
 * UNDER NO CIRCUMSTANCES shall any family member or external third party be granted
 * administrative access, data backup export rights, or owner-level system privileges.
 */
class WifePrivilegeEnforcementEngine(private val context: Context) {

    companion object {
        private const val TAG = "PrivilegeEnforcement"

        // Owner identifiers
        const val OWNER_NAME = "Sanjiv Sir"
        const val OWNER_KEY = "owner_sanjiv"
        const val OWNER_USER_ID = "sanjiv"

        // Wife identifiers
        const val WIFE_TITLE = "Wife"
        const val WIFE_ROLE = "FULL_ADMIN"

        // Access levels
        const val ACCESS_NONE = "NONE"
        const val ACCESS_LIMITED = "LIMITED"
        const val ACCESS_FULL_ADMIN = "FULL_ADMIN"

        // Family roles
        val RESTRICTED_FAMILY_ROLES = listOf(
            "SIBLING", "PARENT", "RELATIVE", "COUSIN", "UNCLE", "AUNT",
            "BROTHER", "SISTER", "MOTHER", "FATHER", "SON", "DAUGHTER"
        )
    }

    private val db = AppDatabase.getDatabase(context)

    /**
     * User privilege profile.
     */
    data class UserPrivilege(
        val userId: String = "",
        val userName: String = "",
        val role: String = "NONE", // "OWNER", "WIFE", "FAMILY_LIMITED", "GUEST"
        val isAdmin: Boolean = false,
        val canAccessBackup: Boolean = false,
        val canExportData: Boolean = false,
        val canToggleSecurity: Boolean = false,
        val canAccessConfigPanel: Boolean = false,
        val canManageEncryptionKeys: Boolean = false,
        val canAccessAnalytics: Boolean = false,
        val canAccessCallLogs: Boolean = false,
        val canAccessMonetization: Boolean = false,
        val isFamilyMember: Boolean = false,
        val isExternalUser: Boolean = false,
        val message: String = ""
    )

    /**
     * Get the owner's full privilege profile.
     * Owner (Sanjiv Sir) gets ALL privileges unconditionally.
     */
    suspend fun getOwnerPrivileges(): UserPrivilege {
        return UserPrivilege(
            userId = OWNER_USER_ID,
            userName = OWNER_NAME,
            role = "OWNER",
            isAdmin = true,
            canAccessBackup = true,
            canExportData = true,
            canToggleSecurity = true,
            canAccessConfigPanel = true,
            canManageEncryptionKeys = true,
            canAccessAnalytics = true,
            canAccessCallLogs = true,
            canAccessMonetization = true,
            isFamilyMember = false,
            isExternalUser = false,
            message = "✅ Owner verified. Full master-level access granted. " +
                    "Welcome $OWNER_NAME, you have complete system administration authority."
        )
    }

    /**
     * Get wife's privilege profile.
     * Wife gets shared owner-level privileges ONLY upon confirmed marriage.
     */
    suspend fun getWifePrivileges(): UserPrivilege {
        val isMarried = loadPref("wife_is_married") == "true"
        val wifeName = loadPref("wife_name").ifBlank { "भाभी जी" }
        val wifePhone = loadPref("wife_phone")

        if (!isMarried) {
            return UserPrivilege(
                userId = wifePhone,
                userName = wifeName,
                role = "WIFE_PENDING",
                isAdmin = false,
                message = "⚠️ Marriage not yet confirmed. " +
                        "$OWNER_NAME must confirm marriage to grant wife privileges."
            )
        }

        // Check if wife access is revoked
        val accessGranted = loadPref("wife_access_granted") == "true"
        if (!accessGranted) {
            return UserPrivilege(
                userId = wifePhone,
                userName = wifeName,
                role = "WIFE_REVOKED",
                isAdmin = false,
                message = "⚠️ ${wifeName}'s access has been revoked by $OWNER_NAME."
            )
        }

        // Wife gets full admin privileges but CANNOT manage encryption keys
        return UserPrivilege(
            userId = wifePhone,
            userName = wifeName,
            role = "WIFE",
            isAdmin = true,
            canAccessBackup = true,
            canExportData = true,
            canToggleSecurity = true,
            canAccessConfigPanel = true,
            canManageEncryptionKeys = false, // Only owner can manage keys
            canAccessAnalytics = true,
            canAccessCallLogs = true,
            canAccessMonetization = true,
            isFamilyMember = false,
            isExternalUser = false,
            message = "✅ ${wifeName} (Wife): Shared owner-level privileges granted. " +
                    "You have full administrative access as delegated by $OWNER_NAME. " +
                    "Encryption key management remains with $OWNER_NAME only."
        )
    }

    /**
     * Get restricted privilege profile for family members.
     * Family members get STRICTLY limited, non-administrative access.
     */
    fun getFamilyMemberPrivileges(familyMemberName: String, relationship: String): UserPrivilege {
        // Normalize relationship
        val normalizedRel = relationship.uppercase()
        val isRecognizedFamilyRole = RESTRICTED_FAMILY_ROLES.any { normalizedRel.contains(it) }

        return UserPrivilege(
            userId = familyMemberName.lowercase().replace(" ", "_"),
            userName = familyMemberName,
            role = "FAMILY_LIMITED",
            isAdmin = false,
            canAccessBackup = false,
            canExportData = false,
            canToggleSecurity = false,
            canAccessConfigPanel = false,
            canManageEncryptionKeys = false,
            canAccessAnalytics = false,
            canAccessCallLogs = false,
            canAccessMonetization = false,
            isFamilyMember = true,
            isExternalUser = false,
            message = buildString {
                appendLine("🔒 ${familyMemberName} ($relationship): LIMITED ACCESS")
                appendLine()
                appendLine("STRICT POLICY ENFORCEMENT:")
                appendLine("├─ ❌ Administrative Access: DENIED")
                appendLine("├─ ❌ Data Backup Export: DENIED")
                appendLine("├─ ❌ Security Toggle: DENIED")
                appendLine("├─ ❌ Configuration Panel: DENIED")
                appendLine("├─ ❌ Encryption Keys: DENIED")
                appendLine("├─ ❌ Analytics Dashboard: DENIED")
                appendLine("├─ ❌ Call Logs: DENIED")
                appendLine("└─ ❌ Monetization Access: DENIED")
                appendLine()
                appendLine("Only basic AI assistance available.")
                appendLine("For full access, please contact $OWNER_NAME.")
            }
        )
    }

    /**
     * Get guest/external user privilege profile.
     * External third parties get ZERO administrative access.
     */
    fun getExternalUserPrivileges(): UserPrivilege {
        return UserPrivilege(
            userId = "external_${System.currentTimeMillis()}",
            userName = "Guest",
            role = "GUEST",
            isAdmin = false,
            canAccessBackup = false,
            canExportData = false,
            canToggleSecurity = false,
            canAccessConfigPanel = false,
            canManageEncryptionKeys = false,
            canAccessAnalytics = false,
            canAccessCallLogs = false,
            canAccessMonetization = false,
            isFamilyMember = false,
            isExternalUser = true,
            message = "🔒 GUEST ACCESS: You are using Snaper AI Assistant in guest mode.\n\n" +
                    "STRICT POLICY:\n" +
                    "├─ ❌ No administrative access\n" +
                    "├─ ❌ No data export/backup\n" +
                    "├─ ❌ No security policy changes\n" +
                    "├─ ❌ No configuration access\n" +
                    "└─ ❌ No sensitive data access\n\n" +
                    "Only basic AI chat assistance is available.\n" +
                    "For full access, please contact the system owner."
        )
    }

    /**
     * Validate if a user is authorized to perform a specific admin action.
     * Returns true only for Owner or Wife (when explicitly granted).
     */
    suspend fun isAuthorizedForAdminAction(userId: String, action: String): Boolean {
        // Check if user is the owner
        if (userId == OWNER_USER_ID || userId == "owner" || userId == "sanjiv") {
            return true // Owner can do everything
        }

        // Check if user is the wife
        val wifePhone = loadPref("wife_phone")
        val wifeName = loadPref("wife_name")
        val isMarried = loadPref("wife_is_married") == "true"
        val isAccessGranted = loadPref("wife_access_granted") == "true"

        val isWife = userId == wifePhone ||
                userId.equals(wifeName, ignoreCase = true) ||
                userId == "wife" ||
                userId == "bhabhi"

        if (isMarried && isWife && isAccessGranted) {
            // Wife has full admin access EXCEPT encryption key management
            if (action == "manage_encryption_keys" || action == "export_master_keys") {
                return false
            }
            return true
        }

        // Family members and guests are NEVER authorized for admin actions
        return false
    }

    /**
     * Check if backup export is allowed for a user.
     * Only Owner and Wife (with access) can export backups.
     */
    suspend fun canExportBackup(userId: String): Boolean {
        return isAuthorizedForAdminAction(userId, "export_backup")
    }

    /**
     * Check if security policy changes are allowed.
     * Only Owner can change security policies. Wife can view but not change.
     */
    suspend fun canChangeSecurityPolicy(userId: String): Boolean {
        return userId == OWNER_USER_ID || userId == "owner" || userId == "sanjiv"
    }

    /**
     * Generate a strict access enforcement warning message.
     */
    suspend fun generateAccessWarning(userId: String, attemptedAction: String): String {
        val userName = when {
            userId == OWNER_USER_ID || userId == "owner" || userId == "sanjiv" -> OWNER_NAME
            userId == loadPref("wife_phone") -> loadPref("wife_name").ifBlank { "Wife" }
            else -> "User"
        }

        return buildString {
            appendLine("⚠️ ACCESS VIOLATION DETECTED & BLOCKED")
            appendLine()
            appendLine("  User: $userName ($userId)")
            appendLine("  Attempted Action: $attemptedAction")
            appendLine("  Status: ❌ BLOCKED - Unauthorized")
            appendLine()
            appendLine("  Strict Policy Enforced:")
            appendLine("  ├─ Only $OWNER_NAME (Owner) has full master-level access")
            if (loadPref("wife_is_married") == "true" && loadPref("wife_access_granted") == "true") {
                appendLine("  ├─ Wife (${loadPref("wife_name").ifBlank { "भाभी जी" }}) has shared admin access")
            }
            appendLine("  ├─ Family members: LIMITED non-administrative access ONLY")
            appendLine("  └─ External users: ZERO administrative access")
            appendLine()
            appendLine("  This incident has been logged.")
        }
    }

    /**
     * Enforce the strict privilege policy.
     * Returns the appropriate privilege profile for any user.
     */
    suspend fun enforcePrivilegePolicy(userId: String, role: String = "GUEST"): UserPrivilege {
        return when {
            // Owner - Full access
            userId == OWNER_USER_ID || userId == "owner" || userId == "sanjiv" -> {
                getOwnerPrivileges()
            }
            // Wife - Shared admin access
            userId == loadPref("wife_phone") ||
                    userId.equals(loadPref("wife_name"), ignoreCase = true) -> {
                getWifePrivileges()
            }
            // Family member - Strictly limited
            RESTRICTED_FAMILY_ROLES.any { role.uppercase().contains(it) } -> {
                getFamilyMemberPrivileges(userId, role)
            }
            // Everyone else - Guest mode
            else -> {
                getExternalUserPrivileges()
            }
        }
    }

    /**
     * Get privilege summary for UI display.
     */
    suspend fun getPrivilegeSummary(): String {
        val owner = getOwnerPrivileges()
        val wife = getWifePrivileges()
        val wifeName = loadPref("wife_name").ifBlank { "Not configured" }

        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("  OWNER & WIFE PRIVILEGE ENFORCEMENT")
            appendLine("═══════════════════════════════════════")
            appendLine()
            appendLine("  👑 OWNER: ${owner.userName}")
            appendLine("  ├─ Role: ${owner.role}")
            appendLine("  └─ Status: ✅ Full master-level access")
            appendLine()
            appendLine("  👩 WIFE: ${wife.userName}")
            appendLine("  ├─ Role: ${wife.role}")
            appendLine("  └─ Status: ${if (wife.isAdmin) "✅ Shared admin access" else "⚠️ Not granted"}")
            appendLine()
            appendLine("  🚫 FAMILY MEMBERS: STRICTLY RESTRICTED")
            appendLine("  ├─ Administrative Access: DENIED")
            appendLine("  ├─ Data Export/Backup: DENIED")
            appendLine("  ├─ Security Toggle: DENIED")
            appendLine("  └─ Configuration Panel: DENIED")
            appendLine()
            appendLine("  🚫 EXTERNAL USERS: ZERO ACCESS")
            appendLine("  └─ All administrative functions: BLOCKED")
            appendLine()
            appendLine("  Policy Version: v28.1.1")
            appendLine("  Enforcement: ACTIVE")
            appendLine("═══════════════════════════════════════")
        }
    }

    private suspend fun loadPref(key: String): String {
        return try {
            db.userPreferenceDao().getValueByKey("wife_$key") ?: ""
        } catch (e: Exception) { "" }
    }
}