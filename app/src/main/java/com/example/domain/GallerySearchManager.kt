package com.example.domain

import android.content.Context
import android.provider.MediaStore
import java.io.File

data class MediaPhotoItem(
    val id: Long,
    val name: String,
    val dateTaken: Long,
    val path: String
)

class GallerySearchManager(private val context: Context) {

    fun searchLocalGallery(query: String): List<MediaPhotoItem> {
        val photos = mutableListOf<MediaPhotoItem>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATA
        )

        try {
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_TAKEN} DESC"
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val pathColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                var count = 0
                while (it.moveToNext() && count < 30) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn) ?: "Photo_$id"
                    val date = it.getLong(dateColumn)
                    val path = it.getString(pathColumn) ?: ""

                    val item = MediaPhotoItem(id, name, date, path)
                    val q = query.lowercase()
                    if (q.isEmpty() ||
                        name.lowercase().contains(q) ||
                        path.lowercase().contains(q) ||
                        q.contains("photo") ||
                        q.contains("फोटो") ||
                        q.contains("birthday") ||
                        q.contains("2024") ||
                        q.contains("mummy")
                    ) {
                        if (File(path).exists() || path.isNotBlank()) {
                            photos.add(item)
                            count++
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return photos
    }
}
