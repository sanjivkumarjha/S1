package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AutomationTaskQueueEntity
import com.example.data.local.entities.BusinessServiceEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.InvoiceEntity
import com.example.data.local.entities.LeadEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.RevenueTargetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM business_leads ORDER BY id DESC")
    fun getAllLeads(): Flow<List<LeadEntity>>

    @Query("SELECT * FROM business_leads WHERE status = :status ORDER BY id DESC")
    fun getLeadsByStatus(status: String): Flow<List<LeadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: LeadEntity): Long

    @Update
    suspend fun updateLead(lead: LeadEntity)

    @Delete
    suspend fun deleteLead(lead: LeadEntity)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM business_projects ORDER BY id DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM business_invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM business_invoices ORDER BY id DESC")
    suspend fun getInvoicesListOnce(): List<InvoiceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM business_expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}

@Dao
interface BusinessServiceDao {
    @Query("SELECT * FROM business_services ORDER BY category ASC, serviceName ASC")
    fun getAllServices(): Flow<List<BusinessServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: BusinessServiceEntity): Long

    @Update
    suspend fun updateService(service: BusinessServiceEntity)

    @Delete
    suspend fun deleteService(service: BusinessServiceEntity)
}

@Dao
interface RevenueTargetDao {
    @Query("SELECT * FROM revenue_targets ORDER BY id DESC")
    fun getAllTargets(): Flow<List<RevenueTargetEntity>>

    @Query("SELECT * FROM revenue_targets WHERE monthYear = :monthYear LIMIT 1")
    suspend fun getTargetByMonthYear(monthYear: String): RevenueTargetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTarget(target: RevenueTargetEntity): Long
}

@Dao
interface AutomationTaskQueueDao {
    @Query("SELECT * FROM automation_task_queue ORDER BY scheduledTime ASC")
    fun getAllTasks(): Flow<List<AutomationTaskQueueEntity>>

    @Query("SELECT * FROM automation_task_queue WHERE status = 'Pending' ORDER BY scheduledTime ASC")
    suspend fun getPendingTasks(): List<AutomationTaskQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AutomationTaskQueueEntity): Long

    @Update
    suspend fun updateTask(task: AutomationTaskQueueEntity)

    @Delete
    suspend fun deleteTask(task: AutomationTaskQueueEntity)

    @Query("DELETE FROM automation_task_queue WHERE status = 'Completed'")
    suspend fun clearCompletedTasks()
}
