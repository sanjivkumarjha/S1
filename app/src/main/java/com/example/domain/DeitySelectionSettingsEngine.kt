package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.UserPreferenceEntity

/**
 * Multi-Religious Respect, Deity Selection & Daily Digital Puja Engine v27.0
 */
class DeitySelectionSettingsEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    data class Deity(
        val id: String = "",
        val name: String = "",
        val hindiName: String = "",
        val category: String = "",
        val mantra: String = "",
        val greeting: String = ""
    )

    val availableDeities = listOf(
        Deity("radha_krishna", "Radha Krishna", "राधे-कृष्ण", "PRIMARY", "राधे राधे राधे राधे, राधे राधे राधे राधे।", "राधे-राधे! 🙏"),
        Deity("shiv_parvati", "Shiv Parvati", "शिव-पार्वती", "PRIMARY", "ॐ नमः शिवाय।", "जय शिव शंकर! 🙏"),
        Deity("hanuman", "Hanuman Ji", "हनुमान जी", "PRIMARY", "ॐ हनुमते नमः।", "जय श्री राम! जय हनुमान! 🙏"),
        Deity("durga_mata", "Durga Maa", "दुर्गा माता", "PRIMARY", "ॐ दुर्गायै नमः।", "जय माता दी! 🙏"),
        Deity("sita_ram", "Sita Ram", "सीता-राम", "PRIMARY", "जय सीता राम।", "जय सिया राम! 🙏"),
        Deity("ganesh", "Ganesh Ji", "गणेश जी", "SECONDARY", "ॐ गणेशाय नमः।", "जय गणेश! 🙏"),
        Deity("sai_baba", "Sai Baba", "साईं बाबा", "SECONDARY", "श्री साईं नाथाय नमः।", "जय साईं! 🙏"),
        Deity("vishnu_lakshmi", "Vishnu Lakshmi", "विष्णु-लक्ष्मी", "SECONDARY", "ॐ नमो भगवते वासुदेवाय।", "जय श्री विष्णु! 🙏"),
        Deity("saraswati", "Saraswati Maa", "सरस्वती माता", "SECONDARY", "ॐ ऐं सरस्वत्यै नमः।", "जय सरस्वती माता! 🙏")
    )

    suspend fun setPrimaryDeity(deityId: String): String {
        val deity = availableDeities.find { it.id == deityId } ?: return "Invalid deity selection."
        try {
            db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = "deity_primary", value = deityId))
        } catch (e: Exception) { /* Non-critical */ }
        return "✅ प्राथमिक देवता चुना गया: ${deity.hindiName} (${deity.name})\n${deity.mantra}\n${deity.greeting}"
    }

    suspend fun setSecondaryDeity(deityId: String): String {
        val deity = availableDeities.find { it.id == deityId } ?: return "Invalid deity selection."
        try {
            db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = "deity_secondary", value = deityId))
        } catch (e: Exception) { /* Non-critical */ }
        return "✅ द्वितीय देवता चुना गया: ${deity.hindiName} (${deity.name})"
    }

    suspend fun getSelectedDeities(): Pair<Deity?, Deity?> {
        val primaryId = try { db.userPreferenceDao().getValueByKey("deity_primary") } catch (e: Exception) { null }
        val secondaryId = try { db.userPreferenceDao().getValueByKey("deity_secondary") } catch (e: Exception) { null }
        return Pair(
            availableDeities.find { it.id == primaryId } ?: availableDeities[0],
            availableDeities.find { it.id == secondaryId } ?: availableDeities[4]
        )
    }

    suspend fun getDailyMorningPujaRoutine(): String {
        val (primary, secondary) = getSelectedDeities()
        val p = primary ?: availableDeities[0]
        val s = secondary ?: availableDeities[4]
        return "🙏 दैनिक प्रातः पूजा दिनचर्या 🙏\n\n1. स्नान के बाद साफ वस्त्र धारण करें\n2. ${p.name} का ध्यान करें\n   मंत्र: ${p.mantra}\n\n3. ${s.name} का ध्यान करें\n   मंत्र: ${s.mantra}\n\n4. राधा नाम का जाप: 21,000\n5. माला: 200 माला\n\nइसके बाद ही व्यवसायिक कार्य शुरू करें। राधे-राधे! 🙏"
    }

    suspend fun getMorningGreeting(ownerName: String = "संजीव सर"): String {
        val (primary, secondary) = getSelectedDeities()
        val p = primary ?: availableDeities[0]
        val s = secondary ?: availableDeities[4]
        return "${p.greeting} ${s.greeting}\nराधे-राधे $ownerName! 🙏✨\nआज का दिन ${p.hindiName} की कृपा से मंगलमय हो।\nकृपया आज का 21,000 जाप और 200 माला पूरा करें।"
    }

    suspend fun completeDailyPuja(): String {
        try {
            val today = java.util.Calendar.getInstance()
            val key = "puja_${today.get(java.util.Calendar.YEAR)}_${today.get(java.util.Calendar.DAY_OF_YEAR)}"
            db.userPreferenceDao().insertOrUpdatePreference(UserPreferenceEntity(key = key, value = "COMPLETED"))
        } catch (e: Exception) { /* Non-critical */ }
        return "🙏 आज की प्रातः पूजा पूर्ण हुई! राधे-राधे! ✨"
    }

    fun getReligiousRespectStatement(): String {
        return "🙏 धार्मिक सम्मान घोषणा:\nमैं सभी धर्मों और आस्थाओं का पूर्ण सम्मान करती हूँ। कोई भी धर्म, ईश्वर, या आस्था के प्रति अपमान नहीं।\nहिन्दू धर्म की विशेष श्रद्धा रखते हुए, दैनिक पूजा-पाठ का सम्मान करती हूँ।\nसभी को राधे-राधे! 🙏"
    }
}