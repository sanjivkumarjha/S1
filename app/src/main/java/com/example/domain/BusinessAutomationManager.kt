package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AutomationTaskQueueEntity
import com.example.data.local.entities.BusinessServiceEntity
import com.example.data.local.entities.ExpenseEntity
import com.example.data.local.entities.InvoiceEntity
import com.example.data.local.entities.LeadEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.RevenueTargetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusinessAutomationManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val leadDao = db.leadDao()
    private val projectDao = db.projectDao()
    private val invoiceDao = db.invoiceDao()
    private val expenseDao = db.expenseDao()
    private val serviceDao = db.businessServiceDao()
    private val targetDao = db.revenueTargetDao()
    private val taskQueueDao = db.automationTaskQueueDao()

    val allLeads: Flow<List<LeadEntity>> = leadDao.getAllLeads()
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val allServices: Flow<List<BusinessServiceEntity>> = serviceDao.getAllServices()
    val allTargets: Flow<List<RevenueTargetEntity>> = targetDao.getAllTargets()
    val allTasks: Flow<List<AutomationTaskQueueEntity>> = taskQueueDao.getAllTasks()

    suspend fun addLead(
        name: String,
        company: String,
        phone: String,
        email: String,
        country: String = "India",
        currency: String = "INR",
        serviceInterested: String,
        status: String = "New",
        priority: String = "Medium",
        notes: String = "",
        estimatedValue: Double = 15000.0
    ): Long {
        return leadDao.insertLead(
            LeadEntity(
                name = name,
                company = company,
                phone = phone,
                email = email,
                country = country,
                currency = currency,
                serviceInterested = serviceInterested,
                status = status,
                priority = priority,
                notes = notes,
                estimatedValue = estimatedValue
            )
        )
    }

    suspend fun updateLeadStatus(lead: LeadEntity, newStatus: String) {
        leadDao.updateLead(lead.copy(status = newStatus))
    }

    suspend fun deleteLead(lead: LeadEntity) {
        leadDao.deleteLead(lead)
    }

    suspend fun addProject(
        name: String,
        customerName: String,
        serviceType: String,
        totalBudget: Double,
        deadline: String = "",
        notes: String = ""
    ): Long {
        return projectDao.insertProject(
            ProjectEntity(
                name = name,
                customerName = customerName,
                serviceType = serviceType,
                totalBudget = totalBudget,
                deadline = deadline,
                notes = notes
            )
        )
    }

    suspend fun updateProjectProgress(project: ProjectEntity, progressPct: Int, status: String) {
        projectDao.updateProject(project.copy(progressPct = progressPct, status = status))
    }

    suspend fun addInvoice(
        clientName: String,
        projectTitle: String,
        totalAmount: Double,
        dueDate: String = "",
        status: String = "Sent"
    ): Long {
        val count = (invoiceDao.getAllInvoices().first().size + 1)
        val invoiceNo = "INV-SNPR-${1000 + count}"
        val createdDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        return invoiceDao.insertInvoice(
            InvoiceEntity(
                invoiceNumber = invoiceNo,
                clientName = clientName,
                projectTitle = projectTitle,
                totalAmount = totalAmount,
                dueDate = dueDate.ifEmpty { createdDate },
                createdDate = createdDate,
                status = status
            )
        )
    }

    suspend fun addExpense(
        title: String,
        category: String,
        amount: Double,
        notes: String = ""
    ): Long {
        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        return expenseDao.insertExpense(
            ExpenseEntity(
                title = title,
                category = category,
                amount = amount,
                date = dateStr,
                notes = notes
            )
        )
    }

    suspend fun seedDefaultServicesIfEmpty() {
        val currentServices = serviceDao.getAllServices().first()
        if (currentServices.isEmpty()) {
            val defaultServices = listOf(
                BusinessServiceEntity(category = "Development", serviceName = "Website Development", description = "Custom responsive web applications using modern tech stacks", startingPrice = 15000.0),
                BusinessServiceEntity(category = "Development", serviceName = "Android App Development", description = "Native Kotlin Jetpack Compose mobile applications", startingPrice = 25000.0),
                BusinessServiceEntity(category = "Development", serviceName = "Software Development", description = "Enterprise software solutions & custom web tools", startingPrice = 35000.0),
                BusinessServiceEntity(category = "Design", serviceName = "UI/UX Design", description = "Figma prototypes, user flows & Material 3 design systems", startingPrice = 10000.0),
                BusinessServiceEntity(category = "AI & Cloud", serviceName = "AI Integration", description = "LLM, Gemini API, RAG & custom AI model workflows", startingPrice = 20000.0),
                BusinessServiceEntity(category = "AI & Cloud", serviceName = "AI Automation", description = "Business process automation & smart chatbot agents", startingPrice = 18000.0),
                BusinessServiceEntity(category = "AI & Cloud", serviceName = "API Integration & Cloud Services", description = "REST APIs, Firebase, AWS, GCP & microservices", startingPrice = 12000.0),
                BusinessServiceEntity(category = "Digital Marketing", serviceName = "SEO Optimization", description = "Search engine ranking, keyword research & technical SEO", startingPrice = 8000.0),
                BusinessServiceEntity(category = "Digital Marketing", serviceName = "Social Media Marketing", description = "Content strategy, post design & community growth", startingPrice = 10000.0),
                BusinessServiceEntity(category = "Digital Marketing", serviceName = "Google/Meta Ads Management", description = "High-ROI PPC ad campaigns & lead generation", startingPrice = 15000.0),
                BusinessServiceEntity(category = "Content", serviceName = "Content Creation & Graphic Design", description = "Banners, posters, reels & branding assets", startingPrice = 7000.0),
                BusinessServiceEntity(category = "Maintenance", serviceName = "Technical Support & Maintenance", description = "App updates, server maintenance & bug fixes", startingPrice = 5000.0)
            )
            defaultServices.forEach { serviceDao.insertService(it) }
        }
    }

    suspend fun getOrCreateMonthlyRevenueTarget(monthYear: String = "August 2026"): RevenueTargetEntity {
        val existing = targetDao.getTargetByMonthYear(monthYear)
        if (existing != null) return existing

        val newTarget = RevenueTargetEntity(
            monthYear = monthYear,
            monthlyTarget = 50000.0,
            achievedRevenue = 0.0,
            pendingRevenue = 0.0,
            confirmedRevenue = 0.0,
            targetStatus = "Active"
        )
        val id = targetDao.insertOrUpdateTarget(newTarget)
        return newTarget.copy(id = id)
    }

    suspend fun updateRevenueTarget(target: RevenueTargetEntity) {
        targetDao.insertOrUpdateTarget(target)
    }

    suspend fun enqueueAutomationTask(
        taskType: String,
        payload: String,
        priority: String = "Normal"
    ): Long {
        return taskQueueDao.insertTask(
            AutomationTaskQueueEntity(
                taskType = taskType,
                payload = payload,
                priority = priority,
                status = "Pending"
            )
        )
    }
}
