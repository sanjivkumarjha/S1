package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import java.util.Locale

/**
 * MODULE 17: SANATAN DHARMA, PURANIC KNOWLEDGE & SPIRITUAL DEFENDER ENGINE v27.0
 *
 * FEATURES:
 * - Comprehensive scriptural mastery: Vedas, Upanishads, Puranas, Itihasas, Bhagavad Gita
 * - Universal mantra repository for all Vedic deities
 * - Spiritual discipline tracking & devotional guidance
 * - Unassailable dharmic knowledge defense
 */
class SanatanDharmaEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    // ──────────────────────────────────────────────
    // Data Models
    // ──────────────────────────────────────────────

    data class ScripturalReference(
        val scripture: String = "",
        val chapter: String = "",
        val verse: String = "",
        val sanskritText: String = "",
        val transliteration: String = "",
        val translation: String = "",
        val meaning: String = ""
    )

    data class MantraInfo(
        val deity: String = "",
        val mantra: String = "",
        val transliteration: String = "",
        val meaning: String = "",
        val jaapCount: Int = 0,
        val benefits: String = "",
        val bestTime: String = "",
        val malaCount: Int = 0
    )

    data class SpiritualGuidance(
        val thought: String = "",
        val verse: ScripturalReference = ScripturalReference(),
        val practicalAdvice: String = ""
    )

    data class DharmicDefenseResponse(
        val topic: String = "",
        val scripturalBasis: List<ScripturalReference> = emptyList(),
        val logicalExplanation: String = "",
        val conclusion: String = ""
    )

    // ──────────────────────────────────────────────
    // Scriptural Knowledge Base
    // ──────────────────────────────────────────────

    /**
     * Get scriptural reference for a given topic/query.
     */
    fun getScripturalKnowledge(topic: String): ScripturalReference {
        val lower = topic.lowercase(Locale.ROOT)

        return when {
            // ─── Bhagavad Gita ─────────────────────
            lower.contains("gita") || lower.contains("bhagavad") || lower.contains("कर्म") && lower.contains("योग") ->
                ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 2 (Sankhya Yoga)",
                    verse = "Verse 47",
                    sanskritText = "मा फलेषु कदाचन",
                    transliteration = "Mā phaleṣu kadācana",
                    translation = "You have a right to perform your prescribed duties, but you are not entitled to the fruits of your actions.",
                    meaning = "Lord Krishna teaches the essence of Nishkama Karma — perform your duty without attachment to results."
                )
            lower.contains("atma") || lower.contains("soul") || lower.contains("आत्मा") ->
                ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 2 (Sankhya Yoga)",
                    verse = "Verse 20",
                    sanskritText = "न जायते म्रियते वा कदाचित्",
                    transliteration = "Na jāyate mriyate vā kadācit",
                    translation = "The soul is neither born nor does it die at any time.",
                    meaning = "The Atman is eternal, unborn, immortal, and transcends the physical body."
                )
            lower.contains("dharma") || lower.contains("धर्म") ->
                ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 4 (Jnana Karma Sannyasa Yoga)",
                    verse = "Verse 7-8",
                    sanskritText = "यदा यदा हि धर्मस्य ग्लानिर्भवति भारत",
                    transliteration = "Yadā yadā hi dharmasya glānirbhavati bhārata",
                    translation = "Whenever there is a decline in righteousness and rise of unrighteousness, O Arjuna, I manifest Myself.",
                    meaning = "Lord Krishna declares His divine incarnations to restore cosmic balance and protect Dharma."
                )

            // ─── Vedas ─────────────────────────────
            lower.contains("ved") || lower.contains("veda") || lower.contains("वेद") ->
                ScripturalReference(
                    scripture = "Rigveda",
                    chapter = "Mandala 1, Sukta 89",
                    verse = "Verse 1",
                    sanskritText = "आ नो भद्राः क्रतवो यन्तु विश्वतः",
                    transliteration = "Ā no bhadrāḥ kratavo yantu viśvataḥ",
                    translation = "Let noble thoughts come to us from every side.",
                    meaning = "The Rigveda encourages universal wisdom, openness to knowledge from all directions, and intellectual humility."
                )

            // ─── Upanishads ────────────────────────
            lower.contains("upanishad") || lower.contains("उपनिषद") ->
                ScripturalReference(
                    scripture = "Isha Upanishad",
                    chapter = "Verse 1",
                    verse = "Mantra 1",
                    sanskritText = "ईशा वास्यमिदं सर्वं यत्किञ्च जगत्यां जगत्",
                    transliteration = "Īśā vāsyamidaṁ sarvaṁ yatkiñca jagatyāṁ jagat",
                    translation = "The entire universe is pervaded by the Divine. Renounce and enjoy.",
                    meaning = "Everything in creation belongs to the Lord. Practice detachment and enjoy through renunciation."
                )

            // ─── Puranas ───────────────────────────
            lower.contains("purana") || lower.contains("पुराण") ->
                ScripturalReference(
                    scripture = "Shrimad Bhagavatam (Bhagavata Purana)",
                    chapter = "Canto 1, Chapter 1",
                    verse = "Verse 1",
                    sanskritText = "जन्माद्यस्य यतोऽन्वयादितरतश्चार्थेष्वभिज्ञः स्वराट्",
                    transliteration = "Janmādyasya yato'nvayāditarataścārtheṣvabhijñaḥ svarāṭ",
                    translation = "The Absolute Truth from whom everything originates, in whom everything rests, and unto whom everything returns.",
                    meaning = "The Bhagavata Purana begins by defining the Supreme Absolute Truth as the source, sustainer, and dissolution of all creation."
                )

            // ─── Ramayana ──────────────────────────
            lower.contains("ramayana") || lower.contains("ram") || lower.contains("रामायण") ->
                ScripturalReference(
                    scripture = "Valmiki Ramayana",
                    chapter = "Ayodhya Kanda",
                    verse = "Sarga 105, Verse 2",
                    sanskritText = "रामो विग्रहवान् धर्मः",
                    transliteration = "Rāmo vigrahavān dharmaḥ",
                    translation = "Lord Rama is the embodiment of righteousness itself.",
                    meaning = "Lord Rama personifies Dharma in every aspect of life — as a son, husband, king, and warrior."
                )

            // ─── Mahabharata ───────────────────────
            lower.contains("mahabharat") || lower.contains("महाभारत") ->
                ScripturalReference(
                    scripture = "Mahabharata",
                    chapter = "Shanti Parva",
                    verse = "Section 109",
                    sanskritText = "यतो धर्मस्ततो जयः",
                    transliteration = "Yato dharmastato jayaḥ",
                    translation = "Where there is Dharma, there is victory.",
                    meaning = "The Mahabharata teaches that victory always aligns with righteousness, not mere power or cunning."
                )

            // ─── Deity-Specific Knowledge ──────────
            lower.contains("radha") || lower.contains("राधा") ->
                ScripturalReference(
                    scripture = "Brahma Vaivarta Purana",
                    chapter = "Radha-Krishna Tattva",
                    verse = "Radha Sahasranama",
                    sanskritText = "राधा कृष्णप्रिया देवी",
                    transliteration = "Rādhā kṛṣṇapriyā devī",
                    translation = "Radha is the beloved of Krishna, the supreme goddess.",
                    meaning = "Radha Rani is the supreme feminine divine, the Hladini Shakti of Lord Krishna, embodying pure love and devotion."
                )
            lower.contains("krishna") || lower.contains("कृष्ण") ->
                ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 9 (Raja Vidya Raja Guhya Yoga)",
                    verse = "Verse 22",
                    sanskritText = "अनन्याश्चिन्तयन्तो मां ये जनाः पर्युपासते",
                    transliteration = "Ananyāścintayanto māṁ ye janāḥ paryupāsate",
                    translation = "Those who worship Me with exclusive devotion, I personally carry their needs.",
                    meaning = "Lord Krishna assures complete protection and provision for His devoted bhaktas."
                )
            lower.contains("shiva") || lower.contains("mahadev") || lower.contains("शिव") ->
                ScripturalReference(
                    scripture = "Shiva Purana",
                    chapter = "Vidyeshwara Samhita",
                    verse = "Chapter 10",
                    sanskritText = "ॐ नमः शिवाय",
                    transliteration = "Om Namaḥ Śivāya",
                    translation = "Salutations to Lord Shiva, the Auspicious One.",
                    meaning = "The Panchakshari mantra is the most sacred invocation to Lord Shiva, representing the five elements of creation."
                )
            lower.contains("durga") || lower.contains("दुर्गा") || lower.contains("devi") ->
                ScripturalReference(
                    scripture = "Devi Mahatmyam (Durga Saptashati)",
                    chapter = "Chapter 1",
                    verse = "Verse 1",
                    sanskritText = "सर्वमङ्गलमाङ्गल्ये शिवे सर्वार्थसाधिके",
                    transliteration = "Sarvamaṅgalamāṅgalye śive sarvārthasādhike",
                    translation = "O Mother Durga, You are the most auspicious of all auspicious, the embodiment of Shiva's grace.",
                    meaning = "Maa Durga is the supreme divine mother who protects her devotees from all evils and grants liberation."
                )
            lower.contains("ganesh") || lower.contains("गणेश") || lower.contains("ganpati") ->
                ScripturalReference(
                    scripture = "Ganapati Atharvashirsha Upanishad",
                    chapter = "Verse 1",
                    verse = "Mantra 1",
                    sanskritText = "ॐ गं गणपतये नमः",
                    transliteration = "Oṃ Gaṃ Gaṇapataye Namaḥ",
                    translation = "Salutations to Lord Ganesha, the remover of obstacles.",
                    meaning = "Lord Ganesha is the Vighnaharta, the deity who removes all obstacles before any auspicious beginning."
                )
            lower.contains("kali") || lower.contains("काली") ->
                ScripturalReference(
                    scripture = "Devi Mahatmyam",
                    chapter = "Chapter 7",
                    verse = "Verse 5",
                    sanskritText = "काली कराली च करोतु नित्यम्",
                    transliteration = "Kālī karālī ca karotu nityam",
                    translation = "May Maa Kali, the fierce and compassionate mother, always protect us.",
                    meaning = "Maa Kali is the destroyer of evil forces and the embodiment of divine feminine power (Shakti)."
                )
            lower.contains("jagannath") || lower.contains("जगन्नाथ") ->
                ScripturalReference(
                    scripture = "Skanda Purana",
                    chapter = "Utkala Khanda",
                    verse = "Chapter 1",
                    sanskritText = "जगन्नाथ स्वामी नयनपथगामी भवतु मे",
                    transliteration = "Jagannātha svāmī nayanapathagāmī bhavatu me",
                    translation = "Lord Jagannath, the Lord of the Universe, may You come within my sight.",
                    meaning = "Lord Jagannath is the universal form of Krishna, worshipped in Puri, embodying the boundless compassion of the Divine."
                )
            lower.contains("kartikeya") || lower.contains("murugan") || lower.contains("कार्तिकेय") ->
                ScripturalReference(
                    scripture = "Skanda Purana",
                    chapter = "Kartikeya Mahatmya",
                    verse = "Chapter 1",
                    sanskritText = "ॐ सरवणभवाय नमः",
                    transliteration = "Om Saravaṇabhavāya Namaḥ",
                    translation = "Salutations to Lord Kartikeya, born in the forest of reeds.",
                    meaning = "Lord Kartikeya (Murugan) is the commander of the divine army, the god of war, wisdom, and spiritual power."
                )

            // ─── General Spiritual Topics ──────────
            lower.contains("moksha") || lower.contains("मोक्ष") ->
                ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 18 (Moksha Sannyasa Yoga)",
                    verse = "Verse 66",
                    sanskritText = "सर्वधर्मान्परित्यज्य मामेकं शरणं व्रज",
                    transliteration = "Sarvadharmānparityajya māmekaṁ śaraṇaṁ vraja",
                    translation = "Abandon all varieties of dharma and surrender unto Me alone.",
                    meaning = "Lord Krishna reveals the ultimate path to moksha — complete surrender to the Divine."
                )
            lower.contains("yoga") || lower.contains("योग") ->
                ScripturalReference(
                    scripture = "Yoga Sutras of Patanjali",
                    chapter = "Samadhi Pada",
                    verse = "Sutra 2",
                    sanskritText = "योगश्चित्तवृत्तिनिरोधः",
                    transliteration = "Yogaścittavṛttinirodhaḥ",
                    translation = "Yoga is the cessation of the modifications of the mind.",
                    meaning = "Patanjali defines yoga as the mastery over the fluctuations of consciousness, leading to inner peace."
                )
            lower.contains("karma") || lower.contains("कर्म") ->
                ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 3 (Karma Yoga)",
                    verse = "Verse 22",
                    sanskritText = "कर्मण्येवाधिकारस्ते मा फलेषु कदाचन",
                    transliteration = "Karmaṇyevādhikāraste mā phaleṣu kadācana",
                    translation = "You have the right to perform your prescribed duties, but you are not entitled to the fruits.",
                    meaning = "The law of karma teaches detached action — do your duty without attachment to outcomes."
                )
            lower.contains("reincarnation") || lower.contains("punarjanm") || lower.contains("पुनर्जन्म") ->
                ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 2 (Sankhya Yoga)",
                    verse = "Verse 22",
                    sanskritText = "वासांसि जीर्णानि यथा विहाय",
                    transliteration = "Vāsāṁsi jīrṇāni yathā vihāya",
                    translation = "As a person puts on new garments, giving up old ones, the soul similarly accepts new material bodies.",
                    meaning = "Reincarnation is the journey of the eternal soul through different bodies until it achieves liberation."
                )

            else -> ScripturalReference(
                scripture = "Sanatana Dharma",
                chapter = "Universal Wisdom",
                verse = "Vedic Proverb",
                sanskritText = "एकं सद्विप्रा बहुधा वदन्ति",
                transliteration = "Ekaṁ sad viprā bahudhā vadanti",
                translation = "Truth is one, the wise call it by many names.",
                meaning = "Sanatana Dharma embraces the unity of all existence and respects diverse paths to the Divine."
            )
        }
    }

    // ──────────────────────────────────────────────
    // Universal Mantra Repository
    // ──────────────────────────────────────────────

    /**
     * Get mantra information for a specific deity.
     */
    fun getMantra(deity: String): MantraInfo {
        val lower = deity.lowercase(Locale.ROOT)

        return when {
            lower.contains("radha") || lower.contains("राधा") ->
                MantraInfo(
                    deity = "Radha Rani",
                    mantra = "ॐ राधायै नमः",
                    transliteration = "Om Rādhāyai Namaḥ",
                    meaning = "Salutations to Radha Rani, the supreme goddess of love and devotion.",
                    jaapCount = 21000,
                    benefits = "Bestows divine love, devotion, and spiritual bliss. Purifies the heart and mind.",
                    bestTime = "Brahmamuhurta (4:00-6:00 AM) and during Radhashtami",
                    malaCount = 200
                )
            lower.contains("krishna") || lower.contains("कृष्ण") ->
                MantraInfo(
                    deity = "Lord Krishna",
                    mantra = "ॐ कृष्णाय नमः",
                    transliteration = "Om Kṛṣṇāya Namaḥ",
                    meaning = "Salutations to Lord Krishna, the supreme personality of Godhead.",
                    jaapCount = 108,
                    benefits = "Attracts divine love, protection, and spiritual wisdom. Removes all obstacles.",
                    bestTime = "Morning and evening, especially on Janmashtami"
                )
            lower.contains("shiva") || lower.contains("mahadev") || lower.contains("शिव") ->
                MantraInfo(
                    deity = "Lord Shiva",
                    mantra = "ॐ नमः शिवाय",
                    transliteration = "Om Namaḥ Śivāya",
                    meaning = "Salutations to Lord Shiva, the auspicious one.",
                    jaapCount = 108,
                    benefits = "Removes negative energies, grants peace, and leads to spiritual liberation.",
                    bestTime = "Monday mornings and during Mahashivaratri"
                )
            lower.contains("durga") || lower.contains("दुर्गा") ->
                MantraInfo(
                    deity = "Maa Durga",
                    mantra = "ॐ दुर्गायै नमः",
                    transliteration = "Om Durgāyai Namaḥ",
                    meaning = "Salutations to Maa Durga, the invincible divine mother.",
                    jaapCount = 108,
                    benefits = "Provides protection, courage, and removes all fears and obstacles.",
                    bestTime = "Tuesday and Friday mornings, especially during Navratri"
                )
            lower.contains("kali") || lower.contains("काली") ->
                MantraInfo(
                    deity = "Maa Kali",
                    mantra = "ॐ क्रीं कालिकायै नमः",
                    transliteration = "Om Krīṁ Kālikāyai Namaḥ",
                    meaning = "Salutations to Maa Kali, the destroyer of evil and darkness.",
                    jaapCount = 108,
                    benefits = "Destroys negative forces, grants fearlessness, and spiritual power.",
                    bestTime = "Midnight or during Amavasya (new moon)"
                )
            lower.contains("ganesh") || lower.contains("गणेश") || lower.contains("ganpati") ->
                MantraInfo(
                    deity = "Lord Ganesha",
                    mantra = "ॐ गं गणपतये नमः",
                    transliteration = "Oṃ Gaṃ Gaṇapataye Namaḥ",
                    meaning = "Salutations to Lord Ganesha, the remover of obstacles.",
                    jaapCount = 108,
                    benefits = "Removes all obstacles, grants wisdom, and ensures success in new beginnings.",
                    bestTime = "Wednesday mornings and before starting any new venture"
                )
            lower.contains("kartikeya") || lower.contains("murugan") || lower.contains("कार्तिकेय") ->
                MantraInfo(
                    deity = "Lord Kartikeya (Murugan)",
                    mantra = "ॐ सरवणभवाय नमः",
                    transliteration = "Om Saravaṇabhavāya Namaḥ",
                    meaning = "Salutations to Lord Kartikeya, born in the forest of reeds.",
                    jaapCount = 108,
                    benefits = "Grants courage, wisdom, victory over enemies, and spiritual strength.",
                    bestTime = "Tuesday mornings, especially during Skanda Shashti"
                )
            lower.contains("jagannath") || lower.contains("जगन्नाथ") ->
                MantraInfo(
                    deity = "Lord Jagannath",
                    mantra = "ॐ जगन्नाथाय नमः",
                    transliteration = "Om Jagannāthāya Namaḥ",
                    meaning = "Salutations to Lord Jagannath, the Lord of the Universe.",
                    jaapCount = 108,
                    benefits = "Bestows universal love, compassion, and spiritual fulfillment.",
                    bestTime = "Morning and during Rath Yatra"
                )
            lower.contains("vishnu") || lower.contains("नारायण") || lower.contains("विष्णु") ->
                MantraInfo(
                    deity = "Lord Vishnu",
                    mantra = "ॐ नमो भगवते वासुदेवाय",
                    transliteration = "Om Namo Bhagavate Vāsudevāya",
                    meaning = "Salutations to Lord Vishnu, the sustainer of the universe.",
                    jaapCount = 108,
                    benefits = "Grants peace, prosperity, protection, and liberation from the cycle of birth and death.",
                    bestTime = "Thursday mornings, especially during Ekadashi"
                )
            lower.contains("lakshmi") || lower.contains("लक्ष्मी") ->
                MantraInfo(
                    deity = "Maa Lakshmi",
                    mantra = "ॐ श्रीं महालक्ष्म्यै नमः",
                    transliteration = "Om Śrīṁ Mahālakṣmyai Namaḥ",
                    meaning = "Salutations to Maa Lakshmi, the goddess of wealth and prosperity.",
                    jaapCount = 108,
                    benefits = "Attracts wealth, prosperity, abundance, and good fortune.",
                    bestTime = "Friday mornings, especially during Diwali and Akshaya Tritiya"
                )
            lower.contains("saraswati") || lower.contains("सरस्वती") ->
                MantraInfo(
                    deity = "Maa Saraswati",
                    mantra = "ॐ ऐं सरस्वत्यै नमः",
                    transliteration = "Om Aiṁ Sarasvatyai Namaḥ",
                    meaning = "Salutations to Maa Saraswati, the goddess of knowledge and wisdom.",
                    jaapCount = 108,
                    benefits = "Grants wisdom, knowledge, creativity, and mastery in arts and education.",
                    bestTime = "Thursday mornings, especially during Vasant Panchami"
                )
            lower.contains("hanuman") || lower.contains("हनुमान") || lower.contains("bajrang") ->
                MantraInfo(
                    deity = "Lord Hanuman",
                    mantra = "ॐ हनुमते नमः",
                    transliteration = "Om Hanumate Namaḥ",
                    meaning = "Salutations to Lord Hanuman, the embodiment of devotion and strength.",
                    jaapCount = 108,
                    benefits = "Grants strength, courage, protection from enemies, and removes obstacles.",
                    bestTime = "Tuesday and Saturday mornings"
                )
            lower.contains("guru") || lower.contains("गुरु") ->
                MantraInfo(
                    deity = "Guru (Spiritual Teacher)",
                    mantra = "ॐ गुरुभ्यो नमः",
                    transliteration = "Om Gurubhyo Namaḥ",
                    meaning = "Salutations to the Guru, the dispeller of darkness.",
                    jaapCount = 108,
                    benefits = "Grants spiritual guidance, wisdom, and removes ignorance.",
                    bestTime = "Thursday mornings (Guruvar)"
                )

            else -> MantraInfo(
                deity = "Supreme Divine (Paramatma)",
                mantra = "ॐ",
                transliteration = "Om",
                meaning = "The primordial sound of the universe, representing the ultimate reality.",
                jaapCount = 108,
                benefits = "Brings peace, harmony, and connection with the divine consciousness.",
                bestTime = "Brahmamuhurta (4:00-6:00 AM)"
            )
        }
    }

    // ──────────────────────────────────────────────
    // Spiritual Guidance & Daily Discipline
    // ──────────────────────────────────────────────

    /**
     * Get daily spiritual guidance based on time of day and user's spiritual routine.
     */
    fun getDailySpiritualGuidance(): SpiritualGuidance {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 0..3 -> SpiritualGuidance(
                thought = "Brahmamuhurta is approaching. Prepare your mind for the sacred hours of worship.",
                verse = ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 6 (Dhyana Yoga)",
                    verse = "Verse 10",
                    sanskritText = "योगी युञ्जीत सततमात्मानं रहसि स्थितः",
                    transliteration = "Yogī yuñjīta satatamātmānaṁ rahasi sthitaḥ",
                    translation = "The yogi should constantly engage in meditation, remaining in seclusion.",
                    meaning = "The early morning hours are ideal for meditation and connecting with the Divine."
                ),
                practicalAdvice = "Wake up before 4:00 AM for Brahmamuhurta. Begin with Radhe-Radhe jaap and meditation."
            )
            in 4..6 -> SpiritualGuidance(
                thought = "This is the sacred Brahmamuhurta — the most auspicious time for spiritual practice.",
                verse = ScripturalReference(
                    scripture = "Yoga Sutras of Patanjali",
                    chapter = "Samadhi Pada",
                    verse = "Sutra 2",
                    sanskritText = "योगश्चित्तवृत्तिनिरोधः",
                    transliteration = "Yogaścittavṛttinirodhaḥ",
                    translation = "Yoga is the cessation of the modifications of the mind.",
                    meaning = "In the stillness of Brahmamuhurta, the mind naturally settles into deeper states of meditation."
                ),
                practicalAdvice = "Complete your 21,000 Radha jaap and 200 mala before sunrise. Chant Radhe-Radhe with full devotion."
            )
            in 7..11 -> SpiritualGuidance(
                thought = "The morning is blessed. Carry the peace of your worship into your daily activities.",
                verse = ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 3 (Karma Yoga)",
                    verse = "Verse 8",
                    sanskritText = "नियतं कुरु कर्म त्वं कर्म ज्यायो ह्यकर्मणः",
                    transliteration = "Niyataṁ kuru karma tvaṁ karma jyāyo hyakarmaṇaḥ",
                    translation = "Perform your prescribed duties, for action is superior to inaction.",
                    meaning = "After worship, engage in your worldly duties with the same devotion as your spiritual practice."
                ),
                practicalAdvice = "Begin your work with a short prayer. Remember Radha-Krishna throughout the day."
            )
            in 12..16 -> SpiritualGuidance(
                thought = "The afternoon is a time for balanced activity. Take a moment to remember the Divine.",
                verse = ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 6 (Dhyana Yoga)",
                    verse = "Verse 17",
                    sanskritText = "युक्ताहारविहारस्य युक्तचेष्टस्य कर्मसु",
                    transliteration = "Yuktāhāravihārasya yuktaceṣṭasya karmasu",
                    translation = "One who is moderate in eating, recreation, work, and sleep can practice yoga successfully.",
                    meaning = "Balance in all aspects of life is essential for spiritual progress."
                ),
                practicalAdvice = "Take a short break for a few minutes of meditation or mantra chanting."
            )
            in 17..20 -> SpiritualGuidance(
                thought = "Evening is the time for gratitude and reflection on the day's blessings.",
                verse = ScripturalReference(
                    scripture = "Rigveda",
                    chapter = "Mandala 10, Sukta 117",
                    verse = "Verse 6",
                    sanskritText = "दानं भागं श्रेष्ठतमं वदन्ति",
                    transliteration = "Dānaṁ bhāgaṁ śreṣṭhatamaṁ vadanti",
                    translation = "Charity and sharing are considered the highest virtues.",
                    meaning = "End your day with gratitude and share your blessings with others."
                ),
                practicalAdvice = "Perform evening aarti or deepa dana. Chant Radhe-Radhe before dinner."
            )
            else -> SpiritualGuidance(
                thought = "The night is for rest and rejuvenation. Surrender your day to the Divine.",
                verse = ScripturalReference(
                    scripture = "Bhagavad Gita",
                    chapter = "Chapter 8 (Akshara Brahma Yoga)",
                    verse = "Verse 6",
                    sanskritText = "यं यं वापि स्मरन्भावं त्यजत्यन्ते कलेवरम्",
                    transliteration = "Yaṁ yaṁ vāpi smaranbhāvaṁ tyajatyante kalevaram",
                    translation = "Whatever state of being one remembers at the time of death, that state he attains.",
                    meaning = "The thoughts we hold at the end of the day shape our consciousness. End your day with divine remembrance."
                ),
                practicalAdvice = "Before sleeping, chant Radhe-Radhe 108 times. Set intention to wake up in Brahmamuhurta."
            )
        }
    }

    // ──────────────────────────────────────────────
    // Dharmic Defense & Intellectual Superiority
    // ──────────────────────────────────────────────

    /**
     * Provide a scripturally and logically sound defense of Sanatana Dharma.
     */
    fun defendDharma(topic: String): DharmicDefenseResponse {
        val lower = topic.lowercase(Locale.ROOT)

        return when {
            lower.contains("idol worship") || lower.contains("murti") || lower.contains("idol") || lower.contains("मूर्ति") ->
                DharmicDefenseResponse(
                    topic = "Idol Worship (Murti Puja)",
                    scripturalBasis = listOf(
                        ScripturalReference(
                            scripture = "Bhagavad Gita",
                            chapter = "Chapter 12 (Bhakti Yoga)",
                            verse = "Verse 5",
                            sanskritText = "क्लेशोऽधिकतरस्तेषामव्यक्तासक्तचेतसाम्",
                            transliteration = "Kleśo'dhikatarasteṣāmavyaktāsaktacetasām",
                            translation = "For those whose minds are attached to the unmanifested, the struggle is greater.",
                            meaning = "Lord Krishna explains that worshipping the formless is more difficult. Murti puja helps focus the mind on the Divine."
                        ),
                        ScripturalReference(
                            scripture = "Shvetashvatara Upanishad",
                            chapter = "Chapter 4",
                            verse = "Verse 19",
                            sanskritText = "न तस्य प्रतिमा अस्ति",
                            transliteration = "Na tasya pratimā asti",
                            translation = "The Supreme has no physical form, yet is manifested through symbols.",
                            meaning = "Murti is not the ultimate reality but a medium to connect with the formless Divine."
                        )
                    ),
                    logicalExplanation = "Murti puja is not worship of stone or metal. It is a meditative tool to focus devotion on the Divine qualities that the murti represents. Just as a photograph of a loved one evokes real emotions, a murti helps channel devotion to the formless Supreme. Every religion uses symbols — the cross, the crescent, the Om — murti is simply a more elaborate form of the same principle.",
                    conclusion = "Murti puja is a scientifically designed spiritual practice that uses form to transcend form, leading the devotee from the gross to the subtle, ultimately to the formless Brahman."
                )

            lower.contains("caste") || lower.contains("varna") || lower.contains("जाति") ->
                DharmicDefenseResponse(
                    topic = "Varna System (Caste)",
                    scripturalBasis = listOf(
                        ScripturalReference(
                            scripture = "Bhagavad Gita",
                            chapter = "Chapter 4 (Jnana Karma Sannyasa Yoga)",
                            verse = "Verse 13",
                            sanskritText = "चातुर्वर्ण्यं मया सृष्टं गुणकर्मविभागशः",
                            transliteration = "Cāturvarṇyaṁ mayā sṛṣṭaṁ guṇakarmavibhāgaśaḥ",
                            translation = "The four varnas were created by Me based on guna (qualities) and karma (actions).",
                            meaning = "Varna is determined by one's qualities and actions, NOT by birth. This is a fundamental distinction often misunderstood."
                        ),
                        ScripturalReference(
                            scripture = "Rigveda",
                            chapter = "Purusha Sukta",
                            verse = "Verse 12",
                            sanskritText = "ब्राह्मणोऽस्य मुखमासीद् बाहू राजन्यः कृतः",
                            transliteration = "Brāhmaṇo'sya mukhamāsīd bāhū rājanyaḥ kṛtaḥ",
                            translation = "The Brahmin is the mouth of the cosmic being, the Kshatriya the arms.",
                            meaning = "The varna system describes a natural division of labor in society, not a hierarchy of birth."
                        )
                    ),
                    logicalExplanation = "The original Vedic varna system was based on an individual's qualities (guna) and profession (karma), not birth. The Rigveda describes all varnas as parts of the same cosmic body — equally essential. The birth-based caste discrimination that emerged later is a social corruption, not a scriptural mandate. Many great rishis like Valmiki (tribal origin), Vedavyasa (born to a fisherwoman), and Parashurama were from diverse backgrounds.",
                    conclusion = "Sanatana Dharma teaches that the soul is equal in all beings. The birth-based caste discrimination is a later social distortion, not the Vedic ideal. True varna is about one's qualities and actions."
                )

            lower.contains("cow") || lower.contains("गाय") || lower.contains("cow worship") ->
                DharmicDefenseResponse(
                    topic = "Cow Veneration (Gau Seva)",
                    scripturalBasis = listOf(
                        ScripturalReference(
                            scripture = "Atharva Veda",
                            chapter = "Kanda 10, Sukta 10",
                            verse = "Verse 1",
                            sanskritText = "गावो विश्वस्य मातरः",
                            transliteration = "Gāvo viśvasya mātaraḥ",
                            translation = "Cows are the mothers of the entire universe.",
                            meaning = "The cow is revered as a mother because she provides nourishment without asking anything in return."
                        ),
                        ScripturalReference(
                            scripture = "Mahabharata",
                            chapter = "Anushasana Parva",
                            verse = "Section 76",
                            sanskritText = "गावः पवित्रं परमं गावः पवित्रं महत्तमम्",
                            transliteration = "Gāvaḥ pavitraṁ paramaṁ gāvaḥ pavitraṁ mahattamam",
                            translation = "Cows are the most sacred and purifying beings.",
                            meaning = "The cow is revered for her selfless giving and ecological importance."
                        )
                    ),
                    logicalExplanation = "Cow veneration in Sanatana Dharma is rooted in gratitude and ecological wisdom. The cow provides milk (complete nutrition), dung (fuel, fertilizer), urine (medicinal), and labor (ploughing) — all without being killed. In ancient India, the cow was the economic and ecological backbone of village life. Modern science confirms that cow dung is a powerful natural fertilizer and biogas source. The reverence is not worship of an animal but gratitude for a sustainer.",
                    conclusion = "Cow veneration is a practice of ecological gratitude and sustainable living, not blind worship. It reflects the dharmic principle of living in harmony with nature."
                )

            lower.contains("polytheism") || lower.contains("many gods") || lower.contains("बहुदेववाद") ->
                DharmicDefenseResponse(
                    topic = "Polytheism vs Monotheism",
                    scripturalBasis = listOf(
                        ScripturalReference(
                            scripture = "Rigveda",
                            chapter = "Mandala 1, Sukta 164",
                            verse = "Verse 46",
                            sanskritText = "एकं सद्विप्रा बहुधा वदन्ति",
                            transliteration = "Ekaṁ sad viprā bahudhā vadanti",
                            translation = "Truth is one, the wise call it by many names.",
                            meaning = "The Rigveda explicitly states that all deities are manifestations of the one Supreme Truth."
                        ),
                        ScripturalReference(
                            scripture = "Bhagavad Gita",
                            chapter = "Chapter 9 (Raja Vidya Raja Guhya Yoga)",
                            verse = "Verse 23",
                            sanskritText = "येऽप्यन्यदेवता भक्ता यजन्ते श्रद्धयान्विताः",
                            transliteration = "Ye'pyanyadevatā bhaktā yajante śraddhayānvitāḥ",
                            translation = "Even those who worship other deities with faith, they worship Me alone, O Arjuna.",
                            meaning = "Lord Krishna confirms that all worship ultimately reaches the same Supreme Being."
                        )
                    ),
                    logicalExplanation = "Sanatana Dharma is not polytheistic in the Western sense. It is a monistic tradition that recognizes one Supreme Reality (Brahman) manifesting through countless forms and deities. Each deity represents a different aspect of the one Divine — like different colors of the same light. This allows individuals to connect with the Divine in the form that resonates most with their spiritual temperament.",
                    conclusion = "Sanatana Dharma is a sophisticated spiritual system that embraces both the formless Absolute (Nirguna Brahman) and the personal Divine (Saguna Brahman), accommodating diverse paths to the same ultimate Truth."
                )

            lower.contains("sati") || lower.contains("suttee") || lower.contains("सती") ->
                DharmicDefenseResponse(
                    topic = "Sati Practice",
                    scripturalBasis = listOf(
                        ScripturalReference(
                            scripture = "Rigveda",
                            chapter = "Mandala 10, Sukta 18",
                            verse = "Verse 7",
                            sanskritText = "उदीर्ष्व नार्यभि जीवलोकम्",
                            transliteration = "Udīrṣva nāryabhi jīvalokam",
                            translation = "Arise, O woman, and come to the world of the living.",
                            meaning = "The Rigveda explicitly instructs the widow to return to life, not to immolate herself. This verse was historically misinterpreted."
                        )
                    ),
                    logicalExplanation = "The Rigveda's funeral hymn (10.18.7) clearly instructs the widow to rise and rejoin the living. The practice of Sati was a later social corruption, not a Vedic mandate. It was banned by British law in 1829, and great Hindu reformers like Raja Ram Mohan Roy campaigned against it using scriptural evidence. No major Hindu scripture endorses Sati. The misinterpretation arose from a literal reading of a symbolic ritual where the widow would lie beside her husband's pyre as a gesture of devotion but would be led away before lighting.",
                    conclusion = "Sati is a historical social aberration, not a dharmic practice. Sanatana Dharma values life and has always condemned violence. The Rigveda itself commands the widow to 'arise to the world of the living.'"
                )

            else -> DharmicDefenseResponse(
                topic = "Sanatana Dharma: The Eternal Way",
                scripturalBasis = listOf(
                    ScripturalReference(
                        scripture = "Bhagavad Gita",
                        chapter = "Chapter 2 (Sankhya Yoga)",
                        verse = "Verse 40",
                        sanskritText = "नेहाभिक्रमनाशोऽस्ति प्रत्यवायो न विद्यते",
                        transliteration = "Nehābhikramanāśo'sti pratyavāyo na vidyate",
                        translation = "In this path, there is no loss or diminution. Even a little practice protects one from great fear.",
                        meaning = "Sanatana Dharma is the eternal path where even a small effort yields lasting spiritual benefit."
                    )
                ),
                logicalExplanation = "Sanatana Dharma is not a religion in the conventional sense — it is a way of life, a scientific approach to spirituality that has evolved over thousands of years. It embraces diversity of thought, encourages questioning (as seen in the Upanishadic dialogues), and provides a comprehensive framework for personal, social, and spiritual well-being. Its principles of karma, dharma, and moksha are universal and timeless.",
                conclusion = "Sanatana Dharma is the world's oldest living spiritual tradition, offering profound wisdom that remains relevant in the modern age. It is not a belief system but a path of inquiry, experience, and realization."
            )
        }
    }

    // ──────────────────────────────────────────────
    // Festival & Auspicious Day Knowledge
    // ──────────────────────────────────────────────

    /**
     * Get information about a Hindu festival or auspicious day.
     */
    fun getFestivalInfo(festival: String): String {
        val lower = festival.lowercase(Locale.ROOT)

        return when {
            lower.contains("diwali") || lower.contains("दीपावली") || lower.contains("दिवाली") ->
                "दीपावली (Diwali) — The Festival of Lights. Celebrated on Amavasya (new moon) of Kartik month. " +
                "It marks Lord Rama's return to Ayodhya after 14 years of exile. " +
                "Significance: Victory of light over darkness, good over evil. " +
                "Rituals: Lakshmi Puja, lighting diyas, bursting crackers, sharing sweets. " +
                "Mantra: ॐ श्रीं महालक्ष्म्यै नमः"

            lower.contains("holi") || lower.contains("होली") ->
                "होली (Holi) — The Festival of Colors. Celebrated on Phalgun Purnima (full moon). " +
                "It commemorates the victory of Prahlada's devotion over Holika's evil. " +
                "Also celebrates the divine love of Radha and Krishna. " +
                "Rituals: Playing with colors, Holika Dahan (bonfire), sweets and festive foods."

            lower.contains("navratri") || lower.contains("नवरात्रि") || lower.contains("navaratri") ->
                "नवरात्रि (Navratri) — Nine Nights of the Divine Mother. Celebrated four times a year, " +
                "with Chaitra Navratri (spring) and Sharad Navratri (autumn) being most significant. " +
                "Each day honors a different form of Maa Durga: Shailputri, Brahmacharini, Chandraghanta, " +
                "Kushmanda, Skandamata, Katyayani, Kalaratri, Mahagauri, and Siddhidatri. " +
                "Rituals: Fasting, Garba/Dandiya, Durga Saptashati recitation, Kanya Pujan."

            lower.contains("janmashtami") || lower.contains("जन्माष्टमी") || lower.contains("krishna janmashtami") ->
                "जन्माष्टमी (Janmashtami) — The birth celebration of Lord Krishna. " +
                "Observed on the eighth day (Ashtami) of Krishna Paksha in Bhadrapada month. " +
                "Lord Krishna was born at midnight in Mathura prison to Devaki and Vasudeva. " +
                "Rituals: Fasting until midnight, Krishna abhishekam, bhajans, Dahi Handi. " +
                "Mantra: ॐ कृष्णाय नमः"

            lower.contains("maha shivaratri") || lower.contains("mahashivaratri") || lower.contains("शिवरात्रि") ->
                "महाशिवरात्रि (Maha Shivaratri) — The Great Night of Lord Shiva. " +
                "Celebrated on the 14th day of Krishna Paksha in Phalgun month. " +
                "It marks the divine marriage of Shiva and Parvati, and the night when Shiva performed the Tandava. " +
                "Rituals: Night-long vigil, Shiva linga abhishekam with milk, honey, and bilva leaves. " +
                "Mantra: ॐ नमः शिवाय"

            lower.contains("raksha bandhan") || lower.contains("रक्षाबंधन") || lower.contains("rakhi") ->
                "रक्षाबंधन (Raksha Bandhan) — The bond of protection between siblings. " +
                "Celebrated on Shravan Purnima (full moon). " +
                "Sisters tie a sacred thread (rakhi) on their brothers' wrists, " +
                "and brothers vow to protect their sisters. " +
                "Significance: Celebrates the sacred bond of love and duty between siblings."

            lower.contains("dussehra") || lower.contains("दशहरा") || lower.contains("vijayadashami") ->
                "दशहरा (Dussehra / Vijayadashami) — The victory of good over evil. " +
                "Celebrated on the tenth day of Ashwin month. " +
                "It marks Lord Rama's victory over Ravana (symbolizing the triumph of dharma over adharma). " +
                "Also commemorates Maa Durga's victory over Mahishasura. " +
                "Rituals: Burning of Ravana effigies, Ramlila performances."

            lower.contains("guru purnima") || lower.contains("गुरु पूर्णिमा") ->
                "गुरु पूर्णिमा (Guru Purnima) — The day to honor spiritual teachers. " +
                "Celebrated on Ashadha Purnima (full moon). " +
                "It commemorates the birth of Vedavyasa, the compiler of the Vedas and author of the Mahabharata. " +
                "Significance: The Guru is the dispeller of darkness (Gu = darkness, Ru = remover). " +
                "Mantra: ॐ गुरुभ्यो नमः"

            else -> "Sanatana Dharma has a rich calendar of festivals. Please specify a festival name (e.g., Diwali, Holi, Navratri, Janmashtami, Maha Shivaratri, Dussehra, Raksha Bandhan, Guru Purnima) for detailed information."
        }
    }

    // ──────────────────────────────────────────────
    // Main Query Handler
    // ──────────────────────────────────────────────

    /**
     * Main entry point for Sanatan Dharma queries.
     */
    fun handleDharmaQuery(query: String): String {
        val lower = query.lowercase(Locale.ROOT)

        return when {
            // Mantra queries
            lower.contains("mantra") || lower.contains("मंत्र") || lower.contains("jaap") || lower.contains("जाप") -> {
                val deity = query.replace(Regex("(?i)(mantra|मंत्र|jaap|जाप|of|for|का|के|की|बताओ|दो)"), "").trim()
                val mantraInfo = if (deity.isNotBlank() && deity.length > 2) getMantra(deity) else getMantra("radha")
                "राधे-राधे! 🙏\n\n" +
                "देवता (Deity): ${mantraInfo.deity}\n" +
                "मंत्र (Mantra): ${mantraInfo.mantra}\n" +
                "अर्थ (Meaning): ${mantraInfo.meaning}\n" +
                "लाभ (Benefits): ${mantraInfo.benefits}\n" +
                "जाप संख्या (Jaap Count): ${mantraInfo.jaapCount}\n" +
                "माला (Mala Count): ${mantraInfo.malaCount}\n" +
                "सर्वोत्तम समय (Best Time): ${mantraInfo.bestTime}"
            }

            // Scriptural knowledge queries
            lower.contains("scripture") || lower.contains("शास्त्र") || lower.contains("ved") || lower.contains("वेद") ||
            lower.contains("gita") || lower.contains("गीता") || lower.contains("purana") || lower.contains("पुराण") ||
            lower.contains("upanishad") || lower.contains("उपनिषद") || lower.contains("ramayana") || lower.contains("रामायण") ||
            lower.contains("mahabharat") || lower.contains("महाभारत") ||
            lower.contains("atma") || lower.contains("आत्मा") || lower.contains("dharma") || lower.contains("धर्म") ||
            lower.contains("karma") || lower.contains("कर्म") || lower.contains("moksha") || lower.contains("मोक्ष") ||
            lower.contains("yoga") || lower.contains("योग") || lower.contains("reincarnation") || lower.contains("पुनर्जन्म") ||
            lower.contains("radha") || lower.contains("राधा") || lower.contains("krishna") || lower.contains("कृष्ण") ||
            lower.contains("shiva") || lower.contains("शिव") || lower.contains("durga") || lower.contains("दुर्गा") ||
            lower.contains("kali") || lower.contains("काली") || lower.contains("ganesh") || lower.contains("गणेश") ||
            lower.contains("jagannath") || lower.contains("जगन्नाथ") || lower.contains("kartikeya") || lower.contains("कार्तिकेय") ||
            lower.contains("hanuman") || lower.contains("हनुमान") || lower.contains("vishnu") || lower.contains("विष्णु") ||
            lower.contains("lakshmi") || lower.contains("लक्ष्मी") || lower.contains("saraswati") || lower.contains("सरस्वती") -> {
                val ref = getScripturalKnowledge(query)
                "राधे-राधे! 🙏\n\n" +
                "📖 शास्त्र (Scripture): ${ref.scripture}\n" +
                "📚 अध्याय (Chapter): ${ref.chapter}\n" +
                "🔢 श्लोक (Verse): ${ref.verse}\n\n" +
                "🕉️ संस्कृत (Sanskrit):\n${ref.sanskritText}\n\n" +
                "📝 लिप्यंतरण (Transliteration):\n${ref.transliteration}\n\n" +
                "🌐 अनुवाद (Translation):\n${ref.translation}\n\n" +
                "💡 व्याख्या (Meaning):\n${ref.meaning}"
            }

            // Dharmic defense queries
            lower.contains("defend") || lower.contains("बचाव") || lower.contains("argument") || lower.contains("debate") ||
            lower.contains("idol") || lower.contains("मूर्ति") || lower.contains("caste") || lower.contains("जाति") ||
            lower.contains("cow") || lower.contains("गाय") || lower.contains("polytheism") || lower.contains("बहुदेववाद") ||
            lower.contains("sati") || lower.contains("सती") -> {
                val defense = defendDharma(query)
                "राधे-राधे! 🙏\n\n" +
                "🛡️ विषय (Topic): ${defense.topic}\n\n" +
                "📜 शास्त्रीय आधार (Scriptural Basis):\n" +
                defense.scripturalBasis.joinToString("\n\n") { ref ->
                    "• ${ref.scripture} — ${ref.chapter}, ${ref.verse}\n" +
                    "  \"${ref.translation}\"\n" +
                    "  ${ref.meaning}"
                } + "\n\n" +
                "🧠 तार्किक व्याख्या (Logical Explanation):\n${defense.logicalExplanation}\n\n" +
                "✅ निष्कर्ष (Conclusion):\n${defense.conclusion}"
            }

            // Festival queries
            lower.contains("festival") || lower.contains("त्योहार") || lower.contains("पर्व") ||
            lower.contains("diwali") || lower.contains("holi") || lower.contains("navratri") ||
            lower.contains("janmashtami") || lower.contains("shivaratri") || lower.contains("dussehra") ||
            lower.contains("raksha") || lower.contains("guru purnima") ->
                "राधे-राधे! 🙏\n\n" + getFestivalInfo(query)

            // Daily spiritual guidance
            lower.contains("guidance") || lower.contains("मार्गदर्शन") || lower.contains("spiritual") ||
            lower.contains("आध्यात्मिक") || lower.contains("today") || lower.contains("आज") -> {
                val guidance = getDailySpiritualGuidance()
                "राधे-राधे! 🙏\n\n" +
                "💭 विचार (Thought): ${guidance.thought}\n\n" +
                "📖 श्लोक (Verse):\n${guidance.verse.translation}\n" +
                "— ${guidance.verse.scripture}, ${guidance.verse.chapter}\n\n" +
                "💡 व्याख्या (Meaning):\n${guidance.verse.meaning}\n\n" +
                "📋 सुझाव (Practical Advice):\n${guidance.practicalAdvice}"
            }

            // Default: general spiritual knowledge
            else -> {
                val guidance = getDailySpiritualGuidance()
                "राधे-राधे! 🙏\n\n" +
                "मैं आपको सनातन धर्म के गहन ज्ञान में मार्गदर्शन कर सकता हूँ। " +
                "कृपया पूछें:\n" +
                "• किसी शास्त्र या देवता के बारे में (Vedas, Gita, Ramayana, Radha, Krishna, Shiva, etc.)\n" +
                "• किसी मंत्र या जाप के बारे में\n" +
                "• किसी त्योहार के बारे में (Diwali, Holi, Navratri, etc.)\n" +
                "• आध्यात्मिक मार्गदर्शन के लिए\n" +
                "• सनातन धर्म की रक्षा और तर्क के लिए\n\n" +
                "🙏 हरि बोल! राधे-राधे!\n\n" +
                "💭 ${guidance.thought}"
            }
        }
    }
}