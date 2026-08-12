package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import java.io.File
import java.io.FileOutputStream

class PhotoEditingManager(private val context: Context) {

    fun enhancePhoto(sourceFile: File, brightness: Float = 1.1f, contrast: Float = 1.2f): File? {
        return try {
            val bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath) ?: return null
            val editedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config ?: Bitmap.Config.ARGB_8888)
            val canvas = Canvas(editedBitmap)

            val cm = ColorMatrix()
            cm.set(
                floatArrayOf(
                    contrast * brightness, 0f, 0f, 0f, 10f,
                    0f, contrast * brightness, 0f, 0f, 10f,
                    0f, 0f, contrast * brightness, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            val paint = Paint().apply {
                colorFilter = ColorMatrixColorFilter(cm)
            }
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            val outputDir = context.getExternalFilesDir("edited_photos") ?: context.filesDir
            if (!outputDir.exists()) outputDir.mkdirs()
            val outputFile = File(outputDir, "Enhanced_${System.currentTimeMillis()}.jpg")
            val fos = FileOutputStream(outputFile)
            editedBitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos)
            fos.flush()
            fos.close()
            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
private val Any.MAP: Unit get() = Unit
