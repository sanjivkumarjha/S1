package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AppAliasEntity
import com.example.data.local.entities.AppPermissionEntity
import com.example.data.local.entities.BirthdayProfileEntity
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.FamilyProfileEntity
import com.example.data.local.entities.GuestPermissionTokenEntity
import com.example.data.local.entities.MemoryEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.SecurityEventLogEntity
import com.example.data.local.entities.SocialMediaPostEntity
import com.example.data.local.entities.UserPreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM assistant_memories ORDER BY isPinned DESC, importance DESC, timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM assistant_memories")
    suspend fun getAllMemoriesOnce(): List<MemoryEntity>

    @Query("SELECT * FROM assistant_memories WHERE content LIKE '%' || :query || '%' OR key LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchMemories(query: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM assistant_memories WHERE key = :key LIMIT 1")
    suspend fun getMemoryByKey(key: String): MemoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM assistant_memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("DELETE FROM assistant_memories WHERE key = :key")
    suspend fun deleteMemoryByKey(key: String)

    @Query("DELETE FROM assistant_memories")
    suspend fun deleteAllMemories()
}

@Dao
interface AppAliasDao {
    @Query("SELECT * FROM app_aliases")
    fun getAllAliases(): Flow<List<AppAliasEntity>>

    @Query("SELECT * FROM app_aliases")
    suspend fun getAllAliasesList(): List<AppAliasEntity>

    @Query("SELECT packageName FROM app_aliases WHERE aliasName LIKE :alias LIMIT 1")
    suspend fun getPackageByAlias(alias: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: AppAliasEntity): Long

    @Query("DELETE FROM app_aliases WHERE id = :id")
    suspend fun deleteAliasById(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timeMillis ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): Flow<List<UserPreferenceEntity>>

    @Query("SELECT * FROM user_preferences WHERE key = :key LIMIT 1")
    fun getPreferenceByKey(key: String): Flow<UserPreferenceEntity?>

    @Query("SELECT value FROM user_preferences WHERE key = :key LIMIT 1")
    suspend fun getValueByKey(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreference(preference: UserPreferenceEntity)

    @Query("DELETE FROM user_preferences WHERE key = :key")
    suspend fun deletePreference(key: String)
}

@Dao
interface FamilyProfileDao {
    @Query("SELECT * FROM family_profiles ORDER BY name ASC")
    fun getAllFamilyProfiles(): Flow<List<FamilyProfileEntity>>

    @Query("SELECT * FROM family_profiles WHERE id = :id LIMIT 1")
    suspend fun getFamilyProfileById(id: Long): FamilyProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyProfile(family: FamilyProfileEntity): Long

    @Query("DELETE FROM family_profiles WHERE id = :id")
    suspend fun deleteFamilyProfileById(id: Long)
}

@Dao
interface AppPermissionDao {
    @Query("SELECT * FROM app_permissions")
    fun getAllAppPermissions(): Flow<List<AppPermissionEntity>>

    @Query("SELECT * FROM app_permissions WHERE packageName = :packageName LIMIT 1")
    suspend fun getAppPermission(packageName: String): AppPermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAppPermission(permission: AppPermissionEntity)
}

@Dao
interface GuestPermissionDao {
    @Query("SELECT * FROM guest_permissions WHERE isRevoked = 0 ORDER BY expiresAtMillis DESC")
    fun getActiveGuestPermissions(): Flow<List<GuestPermissionTokenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: GuestPermissionTokenEntity): Long

    @Query("UPDATE guest_permissions SET isRevoked = 1 WHERE id = :id")
    suspend fun revokeTokenById(id: Long)

    @Query("UPDATE guest_permissions SET isRevoked = 1")
    suspend fun revokeAllTokens()
}

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Long)
}

@Dao
interface BirthdayProfileDao {
    @Query("SELECT * FROM birthday_profiles ORDER BY month ASC, dayOfMonth ASC")
    fun getAllBirthdays(): Flow<List<BirthdayProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBirthday(birthday: BirthdayProfileEntity): Long

    @Query("DELETE FROM birthday_profiles WHERE id = :id")
    suspend fun deleteBirthdayById(id: Long)
}

@Dao
interface SecurityLogDao {
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentSecurityLogs(): Flow<List<SecurityEventLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SecurityEventLogEntity): Long

    @Query("DELETE FROM security_logs")
    suspend fun clearLogs()
}

@Dao
interface SocialMediaPostDao {
    @Query("SELECT * FROM social_media_posts ORDER BY scheduledTimeMillis DESC")
    fun getAllPosts(): Flow<List<SocialMediaPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: SocialMediaPostEntity): Long

    @Query("DELETE FROM social_media_posts WHERE id = :id")
    suspend fun deletePostById(id: Long)
}

@Dao
interface FavoriteSongDao {
    @Query("SELECT * FROM favorite_songs ORDER BY createdAt DESC")
    fun getAllFavoriteSongs(): Flow<List<com.example.data.local.entities.FavoriteSongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteSong(song: com.example.data.local.entities.FavoriteSongEntity): Long

    @Query("DELETE FROM favorite_songs WHERE id = :id")
    suspend fun deleteFavoriteSongById(id: Long)

    @Query("DELETE FROM favorite_songs WHERE songName LIKE '%' || :name || '%'")
    suspend fun deleteFavoriteSongByName(name: String)
}

@Dao
interface SmartDeviceDao {
    @Query("SELECT * FROM smart_devices ORDER BY room ASC, deviceName ASC")
    fun getAllSmartDevices(): Flow<List<com.example.data.local.entities.SmartDeviceEntity>>

    @Query("SELECT * FROM smart_devices WHERE room = :room")
    fun getDevicesByRoom(room: String): Flow<List<com.example.data.local.entities.SmartDeviceEntity>>

    @Query("SELECT * FROM smart_devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: Long): com.example.data.local.entities.SmartDeviceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmartDevice(device: com.example.data.local.entities.SmartDeviceEntity): Long

    @Update
    suspend fun updateSmartDevice(device: com.example.data.local.entities.SmartDeviceEntity)

    @Query("DELETE FROM smart_devices WHERE id = :id")
    suspend fun deleteSmartDeviceById(id: Long)
}

@Dao
interface IRCommandDao {
    @Query("SELECT * FROM ir_commands ORDER BY deviceId ASC, commandName ASC")
    fun getAllIRCommands(): Flow<List<com.example.data.local.entities.IRCommandEntity>>

    @Query("SELECT * FROM ir_commands WHERE deviceId = :deviceId")
    suspend fun getCommandsForDevice(deviceId: Long): List<com.example.data.local.entities.IRCommandEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIRCommand(command: com.example.data.local.entities.IRCommandEntity): Long

    @Query("DELETE FROM ir_commands WHERE id = :id")
    suspend fun deleteIRCommandById(id: Long)
}

@Dao
interface AutomationRuleDao {
    @Query("SELECT * FROM automation_rules ORDER BY isEnabled DESC, id DESC")
    fun getAllAutomationRules(): Flow<List<com.example.data.local.entities.AutomationRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomationRule(rule: com.example.data.local.entities.AutomationRuleEntity): Long

    @Update
    suspend fun updateAutomationRule(rule: com.example.data.local.entities.AutomationRuleEntity)

    @Query("DELETE FROM automation_rules WHERE id = :id")
    suspend fun deleteAutomationRuleById(id: Long)
}

@Dao
interface AutomationLogDao {
    @Query("SELECT * FROM automation_logs ORDER BY executedAt DESC LIMIT 100")
    fun getAutomationLogs(): Flow<List<com.example.data.local.entities.AutomationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutomationLog(log: com.example.data.local.entities.AutomationLogEntity): Long

    @Query("DELETE FROM automation_logs")
    suspend fun clearLogs()
}

@Dao
interface CallSummaryDao {
    @Query("SELECT * FROM call_summaries ORDER BY timestamp DESC")
    fun getAllCallSummaries(): Flow<List<com.example.data.local.entities.CallSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallSummary(summary: com.example.data.local.entities.CallSummaryEntity): Long

    @Query("DELETE FROM call_summaries WHERE id = :id")
    suspend fun deleteCallSummaryById(id: Long)

    @Query("DELETE FROM call_summaries")
    suspend fun clearAllCallSummaries()
}

