package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcontrol.AppInfo
import com.example.appcontrol.AppLaunchResult
import com.example.appcontrol.AppRegistry
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppAliasEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppControlScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val appRegistry = remember { AppRegistry(context) }
    val appAliasDao = remember { AppDatabase.getDatabase(context).appAliasDao() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }

    val aliasesFlow = appAliasDao.getAllAliases()
    val aliasesList by aliasesFlow.collectAsState(initial = emptyList())

    var showAddAliasDialog by remember { mutableStateOf(false) }
    var targetPackageForAlias by remember { mutableStateOf("") }
    var newAliasName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        installedApps = appRegistry.getInstalledApps()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Control & Aliases", fontWeight = FontWeight.Bold) },
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
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Installed Apps (${installedApps.size})", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("App Aliases (${aliasesList.size})", fontWeight = FontWeight.SemiBold) }
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps or package names...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (selectedTabIndex == 0) {
                // Installed Apps List
                val filteredApps = installedApps.filter {
                    it.applicationLabel.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps) { app ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(app.applicationLabel, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(app.packageName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    targetPackageForAlias = app.packageName
                                    showAddAliasDialog = true
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Alias", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        val res = appRegistry.launchAppByName(app.applicationLabel)
                                        if (res is AppLaunchResult.Success) {
                                            Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                                        } else if (res is AppLaunchResult.NotInstalled) {
                                            Toast.makeText(context, res.message, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Launch, contentDescription = "Launch App")
                                }
                            }
                        }
                    }
                }
            } else {
                // Aliases List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(aliasesList) { alias ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Alias: ${alias.aliasName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Package: ${alias.packageName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        appAliasDao.deleteAliasById(alias.id)
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Alias", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddAliasDialog) {
        AlertDialog(
            onDismissRequest = { showAddAliasDialog = false },
            title = { Text("Add Custom App Alias") },
            text = {
                Column {
                    Text("Package: $targetPackageForAlias", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newAliasName,
                        onValueChange = { newAliasName = it },
                        label = { Text("Alias Name (e.g. Business WhatsApp)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newAliasName.isNotBlank()) {
                        coroutineScope.launch {
                            appAliasDao.insertAlias(
                                AppAliasEntity(packageName = targetPackageForAlias, aliasName = newAliasName.trim())
                            )
                            showAddAliasDialog = false
                            newAliasName = ""
                            Toast.makeText(context, "Alias created successfully ✨", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Save Alias")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAliasDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
