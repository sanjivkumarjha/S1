package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.SecurityEventLogEntity

/**
 * Temple Priest Recognition & Compassionate Animal Love Engine v27.0
 *
 * Camera computer vision to detect temple priests/sadhus (saffron robes)
 * and animals/pets, triggering respective reverent greetings or affectionate care.
 */
class PriestAnimalVisionEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val TAG = "PriestAnimalVision"

    data class VisionDetectionResult(
        val isPriestDetected: Boolean = false,
        val isAnimalDetected: Boolean = false,
        val confidenceScore: Float = 0f,
        val message: String = "",
        val greetingToSpeak: String? = null
    )

    suspend fun analyzeCameraFrame(bitmap: Bitmap?, frameLabel: String = "rear_camera"): VisionDetectionResult {
        if (bitmap == null) return VisionDetectionResult(confidenceScore = 0f, message = "Camera frame unavailable.")

        try {
            val buckets = extractDominantColorBuckets(bitmap)
            val priestScore = detectPriestByColorBuckets(buckets)
            val animalScore = detectAnimalByTextureAndHue(bitmap, buckets)

            return when {
                priestScore >= 0.65f && animalScore >= 0.6f -> {
                    val msg = "🙏 राधे-राधे महाराज जी! प्रणाम! Snaper AI Assistant आपकी सेवा में हूँ।"
                    logEvent("PRIEST_ANIMAL", "$frameLabel priest=${(priestScore*100).toInt()}%, animal=${(animalScore*100).toInt()}%")
                    VisionDetectionResult(isPriestDetected = true, isAnimalDetected = true, confidenceScore = maxOf(priestScore, animalScore), message = "महाराज जी और पशु! राधे-राधे! 🙏", greetingToSpeak = msg)
                }
                priestScore >= 0.65f -> {
                    val msg = "राधे-राधे महाराज जी! 🙏 प्रणाम! आपके दर्शन मात्र से जीवन धन्य। मैं Snaper AI Assistant, आपका आदर करती हूँ।"
                    logEvent("PRIEST", "$frameLabel priest=${(priestScore*100).toInt()}%")
                    VisionDetectionResult(isPriestDetected = true, confidenceScore = priestScore, message = "महाराज जी को देखा! राधे-राधे! 🙏", greetingToSpeak = msg)
                }
                animalScore >= 0.6f -> {
                    val msg = "ओह! प्यारा जानवर! 🐾 मैं तुमसे प्यार करती हूँ! क्या तुम्हें कुछ चाहिए?"
                    logEvent("ANIMAL", "$frameLabel animal=${(animalScore*100).toInt()}%")
                    VisionDetectionResult(isAnimalDetected = true, confidenceScore = animalScore, message = "प्यारा जानवर दिखा! 🐾 कितना प्यारा है!", greetingToSpeak = msg)
                }
                else -> VisionDetectionResult(confidenceScore = 0f, message = "No priest or animal detected.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            return VisionDetectionResult(confidenceScore = 0f, message = "Analysis failed: ${e.message}")
        }
    }

    fun getPriestReverenceGreeting(): String = "राधे-राधे महाराज जी! 🙏 प्रणाम! आपके दर्शन से जीवन धन्य। जय श्री राधे-कृष्ण! 🙏✨"

    fun getAnimalAffectionMessage(animalType: String = "प्यारा जानवर"): String {
        return listOf(
            "ओह! कितना प्यारा! 🐾 मैं तुम्हे बहुत प्यार करती हूँ। क्या तुम्हें भूख लगी है?",
            "$animalType बहुत सुंदर है! 🥰 सभी जीवों से प्रेम करना ही सच्चा धर्म है।",
            "देखो! एक प्यारा जानवर! 🐶✨ मैं इसका ध्यान रखूंगी।",
            "जानवर भगवान की अनमोल रचना हैं। 🐾 इनके प्रति दया रखना ही सच्चा धर्म। राधे-राधे!"
        ).random()
    }

    fun getPrivacyScanGuidance(): String = "प्राइवेसी स्कैन:\n• कैमरा कोण: केवल आपका चेहरा दिखे\n• आस-पास गोपनीयता जांचें\n• माइक शोर कम हो\n• खिड़की/दरवाजे बंद करें\n• संवेदनशील जानकारी से पहले एन्क्रिप्शन सक्रिय"

    private fun extractDominantColorBuckets(bitmap: Bitmap): IntArray {
        val buckets = IntArray(64)
        val step = maxOf(1, (bitmap.width * bitmap.height) / 2000)
        try {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            var idx = 0
            while (idx < pixels.size) {
                val p = pixels[idx]
                val bi = ((p shr 22) and 0x30) or ((p shr 14) and 0x0C) or ((p shr 6) and 0x03)
                buckets[bi]++
                idx += step
            }
        } catch (e: Exception) { Log.e(TAG, "Bucket extraction error: ${e.message}") }
        return buckets
    }

    private fun detectPriestByColorBuckets(buckets: IntArray): Float {
        var saffronCount = 0
        var totalSamples = 0
        for (i in 0 until 64) {
            val s = buckets[i]; if (s == 0) continue
            totalSamples += s
            val r = ((i shr 4) and 0x3) * 64 + 32
            val g = ((i shr 2) and 0x3) * 64 + 32
            val b = (i and 0x3) * 64 + 32
            if (r > 180 && g in 80..160 && b < 100) saffronCount += s
            if (r > 200 && g in 100..170 && b < 60) saffronCount += s
        }
        if (totalSamples == 0) return 0f
        return (saffronCount.toFloat() / totalSamples * 3f).coerceIn(0f, 1f)
    }

    private fun detectAnimalByTextureAndHue(bitmap: Bitmap, buckets: IntArray): Float {
        val scaled = try { Bitmap.createScaledBitmap(bitmap, 32, 32, true) } catch (e: Exception) { return 0f }
        var warmHue = 0; var variance = 0
        try {
            val px = IntArray(1024); scaled.getPixels(px, 0, 32, 0, 0, 32, 32)
            for (i in 0 until 1023) {
                val r1 = (px[i] shr 16) and 0xFF; val g1 = (px[i] shr 8) and 0xFF; val b1 = px[i] and 0xFF
                val r2 = (px[i+1] shr 16) and 0xFF; val g2 = (px[i+1] shr 8) and 0xFF; val b2 = px[i+1] and 0xFF
                if (r1 > g1 && g1 > b1 && r1 - b1 > 20) warmHue++
                if (kotlin.math.abs(r1-r2) + kotlin.math.abs(g1-g2) + kotlin.math.abs(b1-b2) > 90) variance++
            }
            scaled.recycle()
        } catch (e: Exception) { Log.e(TAG, "Texture error: ${e.message}") }
        return ((warmHue/1024f)*0.6f + (variance/1024f)*1.2f).coerceIn(0f, 1f)
    }

    private suspend fun logEvent(type: String, desc: String) {
        try { db.securityLogDao().insertLog(SecurityEventLogEntity(eventType = type, description = desc, timestamp = System.currentTimeMillis(), securityLevel = "INFO")) }
        catch (e: Exception) {}
    }
}