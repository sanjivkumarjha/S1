package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BusinessServiceEntity
import com.example.data.local.entities.InvoiceEntity
import com.example.data.local.entities.LeadEntity
import com.example.data.local.entities.ProjectEntity
import com.example.data.local.entities.RevenueTargetEntity
import com.example.domain.BusinessAutomationManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessAutomationScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bizManager = remember { BusinessAutomationManager(context) }

    LaunchedEffect(Unit) {
        bizManager.seedDefaultServicesIfEmpty()
    }

    val leads by bizManager.allLeads.collectAsState(initial = emptyList())
    val projects by bizManager.allProjects.collectAsState(initial = emptyList())
    val invoices by bizManager.allInvoices.collectAsState(initial = emptyList())
    val services by bizManager.allServices.collectAsState(initial = emptyList())
    val targets by bizManager.allTargets.collectAsState(initial = emptyList())
    val tasks by bizManager.allTasks.collectAsState(initial = emptyList())

    var activeTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Dashboard", "Leads", "Services", "Projects", "Invoices", "Payments", "Queue")

    // Dialog state for adding a Lead
    var showAddLeadDialog by remember { mutableStateOf(false) }
    var leadName by remember { mutableStateOf("") }
    var leadCompany by remember { mutableStateOf("") }
    var leadPhone by remember { mutableStateOf("") }
    var leadEmail by remember { mutableStateOf("") }
    var leadService by remember { mutableStateOf("Website Development") }
    var leadValue by remember { mutableStateOf("15000") }

    // Dialog state for adding a Project
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var projName by remember { mutableStateOf("") }
    var projClient by remember { mutableStateOf("") }
    var projService by remember { mutableStateOf("Android App Development") }
    var projBudget by remember { mutableStateOf("25000") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snaper Tech – Business Automation", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        text = { Text(title, fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            when (activeTab) {
                0 -> BusinessDashboardTab(leads = leads, projects = projects, invoices = invoices, targets = targets)
                1 -> LeadsManagementTab(
                    leads = leads,
                    onAddLeadClick = { showAddLeadDialog = true },
                    onStatusChange = { lead, newStatus ->
                        scope.launch { bizManager.updateLeadStatus(lead, newStatus) }
                    },
                    onDeleteLead = { lead ->
                        scope.launch { bizManager.deleteLead(lead) }
                    }
                )
                2 -> ServicesCatalogTab(services = services)
                3 -> ProjectsTab(
                    projects = projects,
                    onAddProjectClick = { showAddProjectDialog = true },
                    onProgressUpdate = { proj, newProgress, status ->
                        scope.launch { bizManager.updateProjectProgress(proj, newProgress, status) }
                    }
                )
                4 -> InvoicesTab(invoices = invoices)
                5 -> PaymentInfoTab()
                6 -> AutomationTaskQueueTab(tasks = tasks)
            }
        }
    }

    // Add Lead Dialog
    if (showAddLeadDialog) {
        AlertDialog(
            onDismissRequest = { showAddLeadDialog = false },
            title = { Text("Add New Business Lead", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = leadName, onValueChange = { leadName = it }, label = { Text("Client Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = leadCompany, onValueChange = { leadCompany = it }, label = { Text("Company / Organization") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = leadPhone, onValueChange = { leadPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = leadEmail, onValueChange = { leadEmail = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = leadService, onValueChange = { leadService = it }, label = { Text("Service Interested In") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = leadValue, onValueChange = { leadValue = it }, label = { Text("Estimated Deal Value (₹)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (leadName.isNotBlank()) {
                            scope.launch {
                                bizManager.addLead(
                                    name = leadName,
                                    company = leadCompany,
                                    phone = leadPhone,
                                    email = leadEmail,
                                    serviceInterested = leadService,
                                    estimatedValue = leadValue.toDoubleOrNull() ?: 15000.0
                                )
                                showAddLeadDialog = false
                                leadName = ""
                                leadCompany = ""
                                leadPhone = ""
                                leadEmail = ""
                                Toast.makeText(context, "Lead saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Save Lead")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddLeadDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Project Dialog
    if (showAddProjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddProjectDialog = false },
            title = { Text("Create New IT/Marketing Project", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = projName, onValueChange = { projName = it }, label = { Text("Project Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = projClient, onValueChange = { projClient = it }, label = { Text("Client Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = projService, onValueChange = { projService = it }, label = { Text("Service Type") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = projBudget, onValueChange = { projBudget = it }, label = { Text("Budget (₹)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projName.isNotBlank()) {
                            scope.launch {
                                bizManager.addProject(
                                    name = projName,
                                    customerName = projClient,
                                    serviceType = projService,
                                    totalBudget = projBudget.toDoubleOrNull() ?: 25000.0
                                )
                                showAddProjectDialog = false
                                projName = ""
                                projClient = ""
                                Toast.makeText(context, "Project created!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Create Project")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddProjectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun BusinessDashboardTab(
    leads: List<LeadEntity>,
    projects: List<ProjectEntity>,
    invoices: List<InvoiceEntity>,
    targets: List<RevenueTargetEntity>
) {
    val currentTarget = targets.firstOrNull()?.monthlyTarget ?: 50000.0
    val totalWonValue = leads.filter { it.status == "Won" }.sumOf { it.estimatedValue }
    val achieved = totalWonValue + projects.sumOf { it.paidAmount }
    val remaining = (currentTarget - achieved).coerceAtLeast(0.0)
    val progressPct = ((achieved / currentTarget) * 100).toInt().coerceIn(0, 100)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Revenue Target Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Revenue Target Dashboard • August 2026", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Monthly Goal", style = MaterialTheme.typography.bodySmall)
                        Text("₹${currentTarget.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text("Achieved", style = MaterialTheme.typography.bodySmall)
                        Text("₹${achieved.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF10B981))
                    }
                    Column {
                        Text("Remaining", style = MaterialTheme.typography.bodySmall)
                        Text("₹${remaining.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFFEF4444))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progressPct / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF10B981),
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$progressPct% Target Achieved • Keep pushing for high-value leads!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Total Leads", style = MaterialTheme.typography.bodySmall)
                    Text("${leads.size}", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("${leads.count { it.status == "New" }} New today", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Active Projects", style = MaterialTheme.typography.bodySmall)
                    Text("${projects.size}", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("${projects.count { it.status == "In Progress" }} In Progress", fontSize = 11.sp, color = Color(0xFF10B981))
                }
            }
        }

        // Daily Business Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Daily Business Overview", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("• ${leads.count { it.status == "Follow-up" }} Leads pending follow-up", style = MaterialTheme.typography.bodyMedium)
                Text("• ${projects.count { it.status == "Testing" || it.status == "Review" }} Projects pending customer review", style = MaterialTheme.typography.bodyMedium)
                Text("• ${invoices.count { it.status == "Sent" }} Unpaid client invoices", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun LeadsManagementTab(
    leads: List<LeadEntity>,
    onAddLeadClick: () -> Unit,
    onStatusChange: (LeadEntity, String) -> Unit,
    onDeleteLead: (LeadEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filterOptions = listOf("All", "New", "Contacted", "Interested", "Proposal Sent", "Negotiation", "Won", "Lost", "Follow-up")

    val filteredLeads = if (selectedFilter == "All") leads else leads.filter { it.status == selectedFilter }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Business Leads (${filteredLeads.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(
                onClick = onAddLeadClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Lead")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filterOptions) { status ->
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = status },
                    label = { Text(status, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLeads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No leads found for this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredLeads) { lead ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(lead.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("₹${lead.estimatedValue.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }

                            if (lead.company.isNotEmpty()) {
                                Text(lead.company, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Text("Service: ${lead.serviceInterested}", style = MaterialTheme.typography.bodySmall)

                            if (lead.phone.isNotEmpty()) {
                                Text("Phone: ${lead.phone}", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = true,
                                    onClick = {
                                        val nextStatus = when (lead.status) {
                                            "New" -> "Contacted"
                                            "Contacted" -> "Interested"
                                            "Interested" -> "Proposal Sent"
                                            "Proposal Sent" -> "Negotiation"
                                            "Negotiation" -> "Won"
                                            else -> "Follow-up"
                                        }
                                        onStatusChange(lead, nextStatus)
                                    },
                                    label = { Text("Status: ${lead.status}", fontSize = 11.sp) }
                                )

                                IconButton(onClick = { onDeleteLead(lead) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Lead", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServicesCatalogTab(services: List<BusinessServiceEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("IT & Digital Marketing Services Catalog", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Configurable services offered by Snaper Technology.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(services) { service ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(service.serviceName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Category: ${service.category}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Starts ₹${service.startingPrice.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF10B981))
                }
            }
        }
    }
}

@Composable
private fun ProjectsTab(
    projects: List<ProjectEntity>,
    onAddProjectClick: () -> Unit,
    onProgressUpdate: (ProjectEntity, Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Active Client Projects (${projects.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(
                onClick = onAddProjectClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Project")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No active client projects currently.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(projects) { project ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Client: ${project.customerName} • ${project.serviceType}", style = MaterialTheme.typography.bodySmall)
                            Text("Budget: ₹${project.totalBudget.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { project.progressPct / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Progress: ${project.progressPct}% (${project.status})", fontSize = 12.sp)

                                OutlinedButton(
                                    onClick = {
                                        val newProgress = (project.progressPct + 25).coerceAtMost(100)
                                        val newStatus = if (newProgress == 100) "Completed" else "In Progress"
                                        onProgressUpdate(project, newProgress, newStatus)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+25% Progress", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoicesTab(invoices: List<InvoiceEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Client Invoices (${invoices.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (invoices.isEmpty()) {
            item {
                Text("No invoices generated yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(invoices) { inv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${inv.invoiceNumber} • ${inv.clientName}", fontWeight = FontWeight.Bold)
                            Text(inv.projectTitle, style = MaterialTheme.typography.bodySmall)
                            Text("Status: ${inv.status}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("₹${inv.totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentInfoTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Payment Information Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Configure payment details shown to Indian and global clients.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("UPI Payment Details (India)", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = "snapertech@upi", onValueChange = {}, label = { Text("UPI ID") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                OutlinedTextField(value = "+91 9876543210", onValueChange = {}, label = { Text("UPI Registered Phone") }, modifier = Modifier.fillMaxWidth(), enabled = false)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Bank Transfer (India / RTGS / NEFT)", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = "Snaper Technology Pvt Ltd", onValueChange = {}, label = { Text("Account Holder Name") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                OutlinedTextField(value = "123456789012", onValueChange = {}, label = { Text("Account Number") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                OutlinedTextField(value = "SBIN0001234", onValueChange = {}, label = { Text("IFSC Code") }, modifier = Modifier.fillMaxWidth(), enabled = false)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("International Payments (Global Clients)", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = "payments@snapertech.com", onValueChange = {}, label = { Text("PayPal Email") }, modifier = Modifier.fillMaxWidth(), enabled = false)
                OutlinedTextField(value = "rzp_live_snapertech", onValueChange = {}, label = { Text("Razorpay Key ID") }, modifier = Modifier.fillMaxWidth(), enabled = false)
            }
        }
    }
}

@Composable
private fun AutomationTaskQueueTab(tasks: List<com.example.data.local.entities.AutomationTaskQueueEntity>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Automation Task Queue (${tasks.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Background automation queue for autonomous execution.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (tasks.isEmpty()) {
            item {
                Text("No tasks currently queued.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(task.taskType, fontWeight = FontWeight.Bold)
                            Text("Status: ${task.status}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        if (task.payload.isNotEmpty()) {
                            Text("Payload: ${task.payload}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
