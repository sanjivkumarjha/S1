package com.example.domain.memory

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.entities.MemoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

enum class MemoryCategory {
    FAMILY, WORK, BUSINESS, CUSTOMERS, PERSONAL, PROJECTS, PHOTOS, VIDEOS, DOCUMENTS, IMPORTANT, FAVORITES, CUSTOM
}

data class MemoryImportProgress(
    val isImporting: Boolean = false,
    val totalFiles: Int = 0,
    val processedFiles: Int = 0,
    val currentFileName: String = "",
    val errorCount: Int = 0,
    val isPaused: Boolean = false
)

data class ScannedFileInfo(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val mimeType: String,
    val category: MemoryCategory,
    val isDuplicate: Boolean = false
)

class MemoryImportManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val memoryDao = db.memoryDao()

    private val _importProgress = MutableStateFlow(MemoryImportProgress())
    val importProgress: StateFlow<MemoryImportProgress> = _importProgress.asStateFlow()

    suspend fun processFolderSelection(files: List<ScannedFileInfo>): List<ScannedFileInfo> = withContext(Dispatchers.IO) {
        val existingMemories = memoryDao.getAllMemoriesOnce()
        val existingNames = existingMemories.map { it.key.lowercase() }.toSet()

        files.map { file ->
            val isDup = existingNames.contains(file.name.lowercase())
            file.copy(isDuplicate = isDup)
        }
    }

    suspend fun importFiles(
        scannedFiles: List<ScannedFileInfo>,
        skipDuplicates: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val targets = if (skipDuplicates) scannedFiles.filter { !it.isDuplicate } else scannedFiles
        if (targets.isEmpty()) return@withContext

        _importProgress.value = MemoryImportProgress(
            isImporting = true,
            totalFiles = targets.size,
            processedFiles = 0,
            currentFileName = ""
        )

        targets.forEachIndexed { index, file ->
            if (_importProgress.value.isPaused) {
                // Wait while paused
            }

            _importProgress.value = _importProgress.value.copy(
                processedFiles = index + 1,
                currentFileName = file.name
            )

            // Save to Room DB memory entities
            val memory = MemoryEntity(
                category = file.category.name,
                key = file.name,
                content = "Imported File: ${file.name} (${file.sizeBytes / 1024} KB) - ${file.uri}",
                tags = "${file.category.name.lowercase()}, file, imported",
                timestamp = System.currentTimeMillis()
            )
            memoryDao.insertMemory(memory)
        }

        _importProgress.value = MemoryImportProgress(isImporting = false)
    }

    suspend fun saveTextMemory(
        key: String,
        content: String,
        category: String = "PERSONAL",
        ownerName: String,
        ownerTitle: String = "Sir"
    ): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val memory = MemoryEntity(
                category = category,
                key = key,
                content = content,
                tags = "text, memory, custom",
                timestamp = System.currentTimeMillis()
            )
            memoryDao.insertMemory(memory)
            val titleStr = if (ownerTitle.isNotBlank() && ownerTitle != "None") " $ownerTitle" else ""
            val nameStr = if (ownerName.isNotBlank() && ownerName != "User") ownerName else "Customer"
            "हाँ $nameStr$titleStr, मैंने यह सीख लिया है।"
        } catch (e: Exception) {
            val titleStr = if (ownerTitle.isNotBlank() && ownerTitle != "None") " $ownerTitle" else ""
            val nameStr = if (ownerName.isNotBlank() && ownerName != "User") ownerName else "Customer"
            "हाँ $nameStr$titleStr, यह जानकारी अभी मेरी मेमोरी में पूरी तरह सेव नहीं हो पाई।"
        }
    }

    fun pauseImport() {
        _importProgress.value = _importProgress.value.copy(isPaused = true)
    }

    fun resumeImport() {
        _importProgress.value = _importProgress.value.copy(isPaused = false)
    }

    fun cancelImport() {
        _importProgress.value = MemoryImportProgress(isImporting = false)
    }
}
