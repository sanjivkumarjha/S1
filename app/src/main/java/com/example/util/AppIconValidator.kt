package com.example.util

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import java.io.InputStream
import java.util.Locale

/**
 * Helper class that validates user-selected image files (file size, MIME type, dimensions)
 * before updating the application's launcher icon state.
 */
object AppIconValidator {

    // Maximum file size: 10 MB
    const val MAX_FILE_SIZE_BYTES: Long = 10 * 1024 * 1024L // 10 MB

    // Minimum required dimensions (in pixels)
    const val MIN_DIMENSION_PX: Int = 64

    // Maximum allowed dimensions (in pixels) to prevent OOM
    const val MAX_DIMENSION_PX: Int = 8192

    // Supported image MIME types
    private val SUPPORTED_MIME_TYPES = setOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/webp",
        "image/bmp",
        "image/gif",
        "image/heic",
        "image/heif"
    )

    sealed class ValidationResult {
        data class Success(
            val width: Int,
            val height: Int,
            val sizeBytes: Long,
            val mimeType: String,
            val userFriendlyMessage: String
        ) : ValidationResult()

        data class Error(
            val userFriendlyMessage: String,
            val errorType: ValidationErrorType
        ) : ValidationResult()
    }

    enum class ValidationErrorType {
        FILE_NOT_FOUND,
        FILE_EMPTY,
        FILE_TOO_LARGE,
        UNSUPPORTED_MIME_TYPE,
        INVALID_DIMENSIONS,
        DIMENSIONS_TOO_SMALL,
        DIMENSIONS_TOO_LARGE,
        UNREADABLE_IMAGE
    }

    /**
     * Validates an image URI String.
     */
    fun validate(context: Context, uriString: String?): ValidationResult {
        if (uriString.isNull_orEmpty()) {
            return ValidationResult.Error(
                userFriendlyMessage = "⚠️ No image selected.",
                errorType = ValidationErrorType.FILE_NOT_FOUND
            )
        }
        return try {
            val uri = Uri.parse(uriString)
            validate(context, uri)
        } catch (e: Exception) {
            ValidationResult.Error(
                userFriendlyMessage = "⚠️ Invalid image path or URI format.",
                errorType = ValidationErrorType.FILE_NOT_FOUND
            )
        }
    }

    /**
     * Validates a selected image Uri against file size, MIME type, and dimension rules.
     */
    fun validate(context: Context, uri: Uri): ValidationResult {
        val contentResolver = context.contentResolver

        // 1. Check MIME type
        val mimeType = contentResolver.getType(uri)?.lowercase(Locale.ROOT)
            ?: getMimeTypeFromExtension(uri.toString())

        if (mimeType != null && !isSupportedMimeType(mimeType)) {
            return ValidationResult.Error(
                userFriendlyMessage = "❌ Unsupported file type ($mimeType). Please select a PNG, JPEG, WEBP, or BMP image.",
                errorType = ValidationErrorType.UNSUPPORTED_MIME_TYPE
            )
        }

        // 2. Query file size
        val fileSize = getFileSize(context, uri)
        if (fileSize == 0L) {
            return ValidationResult.Error(
                userFriendlyMessage = "❌ The selected image file is empty (0 bytes). Please choose a valid image file.",
                errorType = ValidationErrorType.FILE_EMPTY
            )
        }
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            val formattedSize = formatFileSize(fileSize)
            val maxFormatted = formatFileSize(MAX_FILE_SIZE_BYTES)
            return ValidationResult.Error(
                userFriendlyMessage = "❌ Image size ($formattedSize) exceeds the maximum limit ($maxFormatted). Please select a smaller image.",
                errorType = ValidationErrorType.FILE_TOO_LARGE
            )
        }

        // 3. Inspect image dimensions using BitmapFactory.Options (without decoding full bitmap)
        var inputStream: InputStream? = null
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        try {
            inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: Exception) {
            return ValidationResult.Error(
                userFriendlyMessage = "⚠️ Could not read image stream: ${e.localizedMessage ?: "Unknown error"}",
                errorType = ValidationErrorType.UNREADABLE_IMAGE
            )
        } finally {
            try {
                inputStream?.close()
            } catch (_: Exception) {}
        }

        val width = options.outWidth
        val height = options.outHeight

        if (width <= 0 || height <= 0) {
            return ValidationResult.Error(
                userFriendlyMessage = "❌ Unable to decode image dimensions. The file may be corrupt or not a valid image format.",
                errorType = ValidationErrorType.INVALID_DIMENSIONS
            )
        }

        if (width < MIN_DIMENSION_PX || height < MIN_DIMENSION_PX) {
            return ValidationResult.Error(
                userFriendlyMessage = "❌ Image dimensions (${width}x${height} px) are too small. Minimum required dimensions are ${MIN_DIMENSION_PX}x${MIN_DIMENSION_PX} px.",
                errorType = ValidationErrorType.DIMENSIONS_TOO_SMALL
            )
        }

        if (width > MAX_DIMENSION_PX || height > MAX_DIMENSION_PX) {
            return ValidationResult.Error(
                userFriendlyMessage = "❌ Image dimensions (${width}x${height} px) are too large. Maximum supported dimensions are ${MAX_DIMENSION_PX}x${MAX_DIMENSION_PX} px.",
                errorType = ValidationErrorType.DIMENSIONS_TOO_LARGE
            )
        }

        // Craft user-friendly success message with dimension and file size summary
        val finalMime = mimeType ?: "image/*"
        val formattedSize = formatFileSize(fileSize)
        val isSquare = Math.abs(width - height) < (Math.min(width, height) * 0.1)
        val squareNote = if (isSquare) " (Optimal 1:1 Aspect Ratio)" else " (Auto-cropped to fit icon)"

        val successMessage = "✨ Image validated successfully! (${width}x${height} px, $formattedSize)$squareNote"

        return ValidationResult.Success(
            width = width,
            height = height,
            sizeBytes = fileSize,
            mimeType = finalMime,
            userFriendlyMessage = successMessage
        )
    }

    private fun isSupportedMimeType(mimeType: String): Boolean {
        if (SUPPORTED_MIME_TYPES.contains(mimeType)) return true
        return mimeType.startsWith("image/")
    }

    private fun getMimeTypeFromExtension(path: String): String? {
        val lower = path.lowercase(Locale.ROOT)
        return when {
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".bmp") -> "image/bmp"
            lower.endsWith(".gif") -> "image/gif"
            else -> null
        }
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        return cursor.getLong(sizeIndex)
                    }
                }
                0L
            } ?: 0L
        } catch (_: Exception) {
            try {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                    afd.length
                } ?: 0L
            } catch (_: Exception) {
                0L
            }
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format(Locale.ROOT, "%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format(Locale.ROOT, "%.1f MB", mb)
    }

    /**
     * Dynamically switches the active home screen launcher icon component alias using PackageManager.
     */
    fun applyLauncherAlias(context: Context, activeAliasSuffix: String): Boolean {
        return try {
            val pm = context.packageManager
            val pkgName = context.packageName
            val aliases = listOf(
                "MainActivityDefault",
                "MainActivityGold",
                "MainActivityDark",
                "MainActivityNeon"
            )

            val targetSuffix = if (activeAliasSuffix.isBlank()) "MainActivityDefault" else activeAliasSuffix

            aliases.forEach { alias ->
                val compName = android.content.ComponentName(pkgName, "$pkgName.$alias")
                val isTarget = alias.equals(targetSuffix, ignoreCase = true) || (targetSuffix == "default" && alias == "MainActivityDefault")
                val newState = if (isTarget) {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                pm.setComponentEnabledSetting(
                    compName,
                    newState,
                    android.content.pm.PackageManager.DONT_KILL_APP
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun String?.isNull_orEmpty(): Boolean = this == null || this.trim().isEmpty()
}
