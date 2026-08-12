package com.example.domain

import android.content.Context
import com.example.data.preferences.UserPreferencesRepository

class DoctorModeManager(private val context: Context) {

    private val prefsRepo = UserPreferencesRepository(context)

    suspend fun enableDoctorMode(): String {
        prefsRepo.setDoctorModeEnabled(true)
        return "🩺 Doctor Mode Activated! Ask me about medicines, symptoms, precautions, or general health tips. (Note: Always consult a licensed physician for diagnosis and prescriptions)."
    }

    suspend fun disableDoctorMode(): String {
        prefsRepo.setDoctorModeEnabled(false)
        return "🩺 Doctor Mode Deactivated. Returned to standard AI Assistant mode."
    }

    fun isMedicalQuery(query: String): Boolean {
        val q = query.lowercase()
        return q.contains("medicine") || q.contains("tablet") || q.contains("capsule") ||
               q.contains("syrup") || q.contains("fever") || q.contains("headache") ||
               q.contains("pain") || q.contains("doctor") || q.contains("symptom") ||
               q.contains("side effect") || q.contains("dosage") || q.contains("precaution") ||
               q.contains("dawa") || q.contains("dawai") || q.contains("bukhar") ||
               q.contains("sir dard") || q.contains("pet dard") || q.contains("paracetamol") ||
               q.contains("crocin") || q.contains("disprin") || q.contains("combiflam")
    }

    fun generateDoctorResponse(query: String, customerName: String, customerTitle: String): String {
        val titleStr = if (customerTitle.isNotBlank() && customerTitle != "None") " $customerTitle" else ""
        val greeting = "हाँ $customerName$titleStr,"
        val q = query.lowercase()

        // Emergency Check
        if (q.contains("chest pain") || q.contains("heart attack") || q.contains("breathing problem") ||
            q.contains("unconscious") || q.contains("severe bleeding") || q.contains("stroke")) {
            return "⚠️ EMERGENCY WARNING $customerName$titleStr!\n" +
                   "The symptoms described could indicate a critical emergency. Please call an ambulance or visit the nearest hospital emergency room immediately! Do not delay."
        }

        // Medication Specific Queries
        if (q.contains("paracetamol") || q.contains("crocin") || q.contains("fever tablet")) {
            return "$greeting Paracetamol (Crocin/Calpol) standard guidance:\n" +
                   "• Uses: Mild-to-moderate fever reduction and pain relief.\n" +
                   "• Precautions: Avoid exceeding recommended daily dosage (max 4,000 mg/day for adults). Keep hydrated.\n" +
                   "• Warnings: Do not combine with other acetaminophen-containing drugs or excessive alcohol to prevent liver overload.\n" +
                   "• Side Effects: Rare skin rash or nausea.\n" +
                   "⚠️ Please consult a healthcare professional or pharmacist for your personal prescribed dose."
        }

        if (q.contains("disprin") || q.contains("aspirin")) {
            return "$greeting Disprin/Aspirin standard guidance:\n" +
                   "• Uses: Mild pain relief, anti-inflammatory, and doctor-monitored blood thinning.\n" +
                   "• Precautions: Take after meals with plenty of water. Do not give to children/teenagers with viral fever (Reye's syndrome risk).\n" +
                   "• Warnings: Avoid if you have active stomach ulcers, bleeding disorders, or asthma sensitivity.\n" +
                   "• Side Effects: Heartburn, stomach distress, or mild nausea.\n" +
                   "⚠️ Always seek a doctor's prescription before taking blood thinners."
        }

        // Home remedies for mild symptoms
        if (q.contains("cough") || q.contains("khasi") || q.contains("cold") || q.contains("sardi")) {
            return "$greeting General evidence-based care for mild cough/cold:\n" +
                   "• Warm water gargles with a pinch of salt 2-3 times daily.\n" +
                   "• Steam inhalation for nasal congestion relief.\n" +
                   "• Honey and ginger tea to soothe throat irritation.\n" +
                   "• Adequate rest and warm fluid intake.\n" +
                   "⚠️ If fever exceeds 102°F or lasts over 3 days, consult a physician promptly."
        }

        // General Health guidance
        return "$greeting Here is trustworthy medical reference guidance for your query:\n" +
               "• General Info: Medical queries require reviewing verified usage guidelines, contraindications, and precautions.\n" +
               "• Self-Care: Ensure rest, proper hydration, and nutritious diet.\n" +
               "• Safety Notice: I do not prescribe medicines or provide medical diagnosis. Please confirm all treatments with a certified doctor or registered medical practitioner."
    }
}
