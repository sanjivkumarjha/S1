package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_leads")
data class LeadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val company: String = "",
    val phone: String = "",
    val email: String = "",
    val country: String = "India",
    val currency: String = "INR",
    val source: String = "Website", // "Website", "Social Media", "Referral", "Google Ads", "Direct"
    val serviceInterested: String = "Website Development",
    val status: String = "New", // "New", "Contacted", "Interested", "Proposal Sent", "Negotiation", "Won", "Lost", "Follow-up"
    val priority: String = "Medium", // "Low", "Medium", "High"
    val notes: String = "",
    val followUpDate: String = "",
    val assignedTask: String = "",
    val conversionStatus: String = "Pending",
    val estimatedValue: Double = 15000.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val customerName: String,
    val clientPhone: String = "",
    val clientEmail: String = "",
    val serviceType: String = "Android App Development",
    val status: String = "In Progress", // "Planning", "In Progress", "Testing", "Review", "Revision", "Completed"
    val progressPct: Int = 0,
    val totalBudget: Double = 25000.0,
    val paidAmount: Double = 0.0,
    val startDate: String = "",
    val deadline: String = "",
    val notes: String = "",
    val country: String = "India",
    val currency: String = "INR",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val clientName: String,
    val clientEmail: String = "",
    val projectTitle: String = "",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: String = "Draft", // "Draft", "Sent", "Paid", "Overdue"
    val dueDate: String = "",
    val createdDate: String = "",
    val currency: String = "INR"
)

@Entity(tableName = "business_expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Software", // "Salary", "Software", "Marketing", "Server", "Office", "Other"
    val amount: Double = 0.0,
    val date: String = "",
    val notes: String = "",
    val currency: String = "INR"
)

@Entity(tableName = "business_services")
data class BusinessServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String = "Development",
    val serviceName: String,
    val description: String = "",
    val startingPrice: Double = 10000.0,
    val priceUnit: String = "Per Project", // "Per Project", "Per Month", "Per Hour"
    val isEnabled: Boolean = true
)

@Entity(tableName = "revenue_targets")
data class RevenueTargetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monthYear: String, // e.g. "August 2026"
    val monthlyTarget: Double = 50000.0,
    val achievedRevenue: Double = 0.0,
    val pendingRevenue: Double = 0.0,
    val confirmedRevenue: Double = 0.0,
    val targetStatus: String = "Active" // "Active", "Achieved", "Rolled Over"
)

@Entity(tableName = "automation_task_queue")
data class AutomationTaskQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskType: String, // "LEAD_FOLLOWUP", "PROJECT_UPDATE", "GENERATE_INVOICE", "SOCIAL_POST", "BACKUP_MEMORIES", "CLEANUP_CACHE"
    val priority: String = "Normal", // "Low", "Normal", "High"
    val scheduledTime: Long = System.currentTimeMillis(),
    val status: String = "Pending", // "Pending", "Running", "Completed", "Failed", "Waiting for Permission", "Waiting for User", "Retrying"
    val progress: Int = 0,
    val payload: String = "",
    val result: String = "",
    val error: String = "",
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
