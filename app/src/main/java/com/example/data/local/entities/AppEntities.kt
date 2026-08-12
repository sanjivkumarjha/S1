package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaUri: String? = null,
    val mediaType: String? = null, // "image", "document", "audio"
    val codeSnippet: String? = null,
    val isThought: Boolean = false
)

@Entity(tableName = "assistant_memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "preference", "fact", "goal", "note", "interaction"
    val key: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val importance: Int = 1, // 1: Low, 2: Medium, 3: High
    val value: String = content,
    val description: String = "",
    val source: String = "user",
    val createdAt: Long = timestamp,
    val updatedAt: Long = timestamp,
    val isPinned: Boolean = false,
    val isPermanent: Boolean = true,
    val tags: String = "",
    val version: Int = 1
)

@Entity(tableName = "app_aliases")
data class AppAliasEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val aliasName: String
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val tags: String = "",
    val colorHex: String = "#7F56D9",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val timeMillis: Long,
    val isCompleted: Boolean = false,
    val priority: String = "Normal" // "Low", "Normal", "High"
)

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "family_profiles")
data class FamilyProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relationship: String, // "Mother", "Father", "Sister", "Brother", "Spouse", "Child"
    val photoUri: String = "",
    val faceVoiceTag: String = "",
    val allowedApps: String = "com.google.android.youtube",
    val allowedFeatures: String = "youtube,camera_basic",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_permissions")
data class AppPermissionEntity(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val permissionLevel: String = "Allowed", // "Allowed", "Restricted", "Owner Only", "Temporary Access", "Read Only", "Disabled"
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "guest_permissions")
data class GuestPermissionTokenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetAppOrFeature: String,
    val permissionType: String = "READ_ONLY", // "READ_ONLY", "CALL_ONLY", "LIMITED_TIME"
    val durationMinutes: Int = 30,
    val grantedAtMillis: Long = System.currentTimeMillis(),
    val expiresAtMillis: Long = System.currentTimeMillis() + (30 * 60 * 1000L),
    val isRevoked: Boolean = false
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val company: String = "",
    val status: String = "Active", // "Active", "FollowUp", "Closed"
    val notes: String = "",
    val lastCallTimestamp: Long = System.currentTimeMillis(),
    val nextFollowUpTimestamp: Long = 0L
)

@Entity(tableName = "birthday_profiles")
data class BirthdayProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val relationship: String = "Friend", // "Mother", "Father", "Friend", "Colleague"
    val dateFormatted: String, // e.g. "15 September"
    val dayOfMonth: Int,
    val month: Int,
    val photoUri: String? = null,
    val customGreeting: String = "",
    val customTheme: String = "Gold Ribbon"
)

@Entity(tableName = "security_logs")
data class SecurityEventLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val securityLevel: String = "MEDIUM"
)

@Entity(tableName = "social_media_posts")
data class SocialMediaPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platform: String,
    val caption: String,
    val hashtags: String,
    val imageUri: String? = null,
    val scheduledTimeMillis: Long = 0L,
    val isPosted: Boolean = false
)

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songName: String,
    val artist: String = "",
    val source: String = "youtube", // "youtube", "local", "spotify", "music_player"
    val sourceId: String = "",
    val localUri: String? = null,
    val preferredPlatform: String = "youtube",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "smart_devices")
data class SmartDeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceName: String,
    val manufacturer: String = "Generic",
    val model: String = "Standard IoT",
    val protocol: String = "wifi", // "wifi", "bluetooth", "matter", "home_assistant", "ir", "mqtt"
    val ipAddress: String = "192.168.1.100",
    val capabilities: String = "power,volume,temperature,brightness,fan_speed",
    val room: String = "Living Room", // "Bedroom", "Living Room", "Kitchen", "Office"
    val isOnline: Boolean = true,
    val powerState: Boolean = false,
    val currentValue: String = "Off",
    val deviceType: String = "Light" // "TV", "AC", "Fan", "Light", "Plug", "Curtain", "Camera", "Speaker"
)

@Entity(tableName = "ir_commands")
data class IRCommandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: Long = 0,
    val manufacturer: String = "Universal",
    val model: String = "IR Device",
    val commandName: String, // "Power", "Volume Up", "Temp Down"
    val protocol: String = "NEC",
    val frequency: Int = 38000,
    val rawCommand: String = "",
    val room: String = "Living Room"
)

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleName: String,
    val triggerType: String = "TEMPERATURE", // "TIME", "WEATHER", "TEMPERATURE", "HUMIDITY", "SUNRISE", "SUNSET", "PRESENCE"
    val triggerCondition: String = "temp > 30",
    val targetDeviceIds: String = "",
    val actionPayload: String = "AC ON, Fan Speed 2",
    val isEnabled: Boolean = true,
    val cooldownMinutes: Int = 15,
    val lastExecutedTimestamp: Long = 0L
)

@Entity(tableName = "automation_logs")
data class AutomationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val automationName: String,
    val trigger: String,
    val executedAt: Long = System.currentTimeMillis(),
    val devicesChanged: String = "",
    val result: String = "Success",
    val failureReason: String = ""
)

@Entity(tableName = "call_summaries")
data class CallSummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val callerName: String,
    val callerPhone: String = "",
    val purpose: String = "",
    val importantPoints: String = "",
    val requestedAction: String = "",
    val followUpDate: String = "",
    val summaryHindi: String = "",
    val summaryEnglish: String = "",
    val transcript: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

