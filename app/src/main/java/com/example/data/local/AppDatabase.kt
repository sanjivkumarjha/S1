package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AppAliasDao
import com.example.data.local.dao.AppPermissionDao
import com.example.data.local.dao.AutomationLogDao
import com.example.data.local.dao.AutomationRuleDao
import com.example.data.local.dao.AutomationTaskQueueDao
import com.example.data.local.dao.BirthdayProfileDao
import com.example.data.local.dao.BusinessServiceDao
import com.example.data.local.dao.CallSummaryDao
import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.CustomerDao
import com.example.data.local.dao.ExpenseDao
import com.example.data.local.dao.FamilyProfileDao
import com.example.data.local.dao.FavoriteSongDao
import com.example.data.local.dao.GuestPermissionDao
import com.example.data.local.dao.IRCommandDao
import com.example.data.local.dao.InvoiceDao
import com.example.data.local.dao.LeadDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.RevenueTargetDao
import com.example.data.local.dao.SecurityLogDao
import com.example.data.local.dao.SmartDeviceDao
import com.example.data.local.dao.SocialMediaPostDao
import com.example.data.local.dao.UserPreferenceDao
import com.example.data.local.entities.AppAliasEntity
import com.example.data.local.entities.AppPermissionEntity
import com.example.data.local.entities.AutomationLogEntity
import com.example.data.local.entities.AutomationRuleEntity
import com.example.data.local.entities.AutomationTaskQueueEntity
import com.example.data.local.entities.BirthdayProfileEntity
import com.example.data.local.entities.BusinessServiceEntity
import com.example.data.local.entities.CallSummaryEntity
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.CustomerEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.FamilyProfileEntity
import com.example.data.local.entities.FavoriteSongEntity
import com.example.data.local.entities.GuestPermissionTokenEntity
import com.example.data.local.entities.IRCommandEntity
import com.example.data.local.entities.InvoiceEntity
import com.example.data.local.entities.LeadEntity
import com.example.data.local.entities.MemoryEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.RevenueTargetEntity
import com.example.data.local.entities.SecurityEventLogEntity
import com.example.data.local.entities.SmartDeviceEntity
import com.example.data.local.entities.SocialMediaPostEntity
import com.example.data.local.entities.UserPreferenceEntity

@Database(
    entities = [
        ChatMessageEntity::class,
        MemoryEntity::class,
        NoteEntity::class,
        ReminderEntity::class,
        UserPreferenceEntity::class,
        AppAliasEntity::class,
        FamilyProfileEntity::class,
        AppPermissionEntity::class,
        GuestPermissionTokenEntity::class,
        CustomerEntity::class,
        BirthdayProfileEntity::class,
        SecurityEventLogEntity::class,
        SocialMediaPostEntity::class,
        FavoriteSongEntity::class,
        SmartDeviceEntity::class,
        IRCommandEntity::class,
        AutomationRuleEntity::class,
        AutomationLogEntity::class,
        CallSummaryEntity::class,
        LeadEntity::class,
        ProjectEntity::class,
        InvoiceEntity::class,
        ExpenseEntity::class,
        BusinessServiceEntity::class,
        RevenueTargetEntity::class,
        AutomationTaskQueueEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun memoryDao(): MemoryDao
    abstract fun noteDao(): NoteDao
    abstract fun reminderDao(): ReminderDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun appAliasDao(): AppAliasDao
    abstract fun familyProfileDao(): FamilyProfileDao
    abstract fun appPermissionDao(): AppPermissionDao
    abstract fun guestPermissionDao(): GuestPermissionDao
    abstract fun customerDao(): CustomerDao
    abstract fun birthdayProfileDao(): BirthdayProfileDao
    abstract fun securityLogDao(): SecurityLogDao
    abstract fun socialMediaPostDao(): SocialMediaPostDao
    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun smartDeviceDao(): SmartDeviceDao
    abstract fun irCommandDao(): IRCommandDao
    abstract fun automationRuleDao(): AutomationRuleDao
    abstract fun automationLogDao(): AutomationLogDao
    abstract fun callSummaryDao(): CallSummaryDao
    abstract fun leadDao(): LeadDao
    abstract fun projectDao(): ProjectDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun businessServiceDao(): BusinessServiceDao
    abstract fun revenueTargetDao(): RevenueTargetDao
    abstract fun automationTaskQueueDao(): AutomationTaskQueueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "snaper_technology_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
