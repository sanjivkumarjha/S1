package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.data.local.AppDatabase
import com.example.data.local.entities.BirthdayProfileEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

class BirthdayManager(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val birthdayDao = db.birthdayProfileDao()

    val birthdays: Flow<List<BirthdayProfileEntity>> = birthdayDao.getAllBirthdays()

    suspend fun saveBirthday(
        personName: String,
        relationship: String,
        dateFormatted: String,
        dayOfMonth: Int,
        month: Int,
        photoUri: String? = null,
        greeting: String = ""
    ): Long {
        return birthdayDao.insertBirthday(
            BirthdayProfileEntity(
                personName = personName.trim(),
                relationship = relationship.trim(),
                dateFormatted = dateFormatted.trim(),
                dayOfMonth = dayOfMonth,
                month = month,
                photoUri = photoUri,
                customGreeting = greeting
            )
        )
    }

    suspend fun deleteBirthday(id: Long) {
        birthdayDao.deleteBirthdayById(id)
    }

    fun generateBirthdayCardImage(
        personName: String,
        relationship: String,
        greetingMessage: String
    ): File? {
        return try {
            val width = 1080
            val height = 1080
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Background Gradient simulation
            val bgPaint = Paint().apply {
                color = Color.parseColor("#1E1035")
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Decorative Frame
            val framePaint = Paint().apply {
                color = Color.parseColor("#FFD700")
                style = Paint.Style.STROKE
                strokeWidth = 24f
            }
            val rect = RectF(40f, 40f, width - 40f, height - 40f)
            canvas.drawRoundRect(rect, 40f, 40f, framePaint)

            // Header Text
            val headerPaint = Paint().apply {
                color = Color.parseColor("#FF4081")
                textSize = 72f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("🎉 Happy Birthday! 🎉", width / 2f, 220f, headerPaint)

            // Name Text
            val namePaint = Paint().apply {
                color = Color.WHITE
                textSize = 90f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(personName, width / 2f, 380f, namePaint)

            // Relationship
            val relPaint = Paint().apply {
                color = Color.parseColor("#BB86FC")
                textSize = 48f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("($relationship)", width / 2f, 460f, relPaint)

            // Message Body
            val msgPaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                textSize = 44f
                textAlign = Paint.Align.CENTER
            }
            val msgLines = if (greetingMessage.isNotBlank()) greetingMessage else "Wishing you a joyful day filled with love, laughter, and health! ❤️"
            canvas.drawText(msgLines, width / 2f, 650f, msgPaint)

            // Footer
            val footerPaint = Paint().apply {
                color = Color.parseColor("#FFD700")
                textSize = 36f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Designed with ❤️ by Snaper Assistant", width / 2f, 950f, footerPaint)

            val outputDir = context.getExternalFilesDir("birthday_cards") ?: context.filesDir
            if (!outputDir.exists()) outputDir.mkdirs()
            val file = File(outputDir, "Birthday_${System.currentTimeMillis()}.png")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
