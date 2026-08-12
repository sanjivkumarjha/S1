package com.example.data.cache

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

object AppCacheManager {

    /**
     * Calculates the actual current cache size in bytes across all temporary cache locations.
     */
    suspend fun getCacheSizeBytes(context: Context): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L
        try {
            context.cacheDir?.let { totalSize += getFolderSize(it) }
            context.codeCacheDir?.let { totalSize += getFolderSize(it) }
            context.externalCacheDir?.let { totalSize += getFolderSize(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext totalSize
    }

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0L
        var size = 0L
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                size += getFolderSize(child)
            }
        } else {
            size += file.length()
        }
        return size
    }

    /**
     * Formats bytes into human-readable string (e.g. 245.5 MB, 0.0 B).
     */
    fun formatCacheSize(bytes: Long): String {
        if (bytes <= 0) return "0.0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
            else -> "$bytes B"
        }
    }

    /**
     * Clears ONLY temporary/removable cache directories.
     * GUARANTEED TO NOT DELETE:
     * - Databases (Room DB, user preferences, memories)
     * - Shared Preferences
     * - Long term memories
     * - User Profile & Avatars
     * - Settings
     */
    suspend fun clearRemovableCache(context: Context): Long = withContext(Dispatchers.IO) {
        try {
            // 1. Clear internal cacheDir
            context.cacheDir?.let { deleteContents(it) }

            // 2. Clear codeCacheDir
            context.codeCacheDir?.let { deleteContents(it) }

            // 3. Clear externalCacheDir
            context.externalCacheDir?.let { deleteContents(it) }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext getCacheSizeBytes(context)
    }

    private fun deleteContents(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) {
            file.listFiles()?.forEach { child ->
                if (child.isDirectory) {
                    deleteContents(child)
                }
                child.delete()
            }
        }
    }

    /**
     * Safe automatic cleanup when cache exceeds configured limit (e.g., 250 MB).
     * Only deletes files older than 24 hours.
     */
    suspend fun autoCleanupIfNeeded(context: Context, limitMb: Int = 250) = withContext(Dispatchers.IO) {
        val currentBytes = getCacheSizeBytes(context)
        val limitBytes = limitMb * 1024L * 1024L

        if (currentBytes > limitBytes) {
            val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            context.cacheDir?.let { cleanOldFiles(it, oneDayAgo) }
            context.externalCacheDir?.let { cleanOldFiles(it, oneDayAgo) }
        }
    }

    private fun cleanOldFiles(dir: File, thresholdTime: Long) {
        if (!dir.exists()) return
        if (dir.isDirectory) {
            dir.listFiles()?.forEach { child ->
                if (child.lastModified() < thresholdTime) {
                    child.deleteRecursively()
                }
            }
        }
    }
}
