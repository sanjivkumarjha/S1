package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import java.util.Locale

/**
 * MODULE 20: SACRED SWAPNA SHASTRA & DIVINE SIGN INTERPRETATION ENGINE v27.0
 *
 * FEATURES:
 * - Scriptural dream analysis based on ancient Hindu Swapna Shastra
 * - Puranic portent interpretation for visions, dreams, and nocturnal experiences
 * - Divine sign & omen decoder — reveals messages, blessings, warnings from Bhagwan
 * - Scripturally backed insights with suggested remedial actions (mantra jaap, prayers)
 */
class SwapnaShastraEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    // ──────────────────────────────────────────────
    // Data Models
    // ──────────────────────────────────────────────

    data class DreamInterpretation(
        val dreamType: String = "",
        val category: String = "",           // Auspicious, Inauspicious, Mixed, Neutral
        val scripturalReference: String = "",
        val spiritualMeaning: String = "",
        val divineMessage: String = "",
        val suggestedRemedy: String = "",
        val astrologicalSignificance: String = "",
        val urgencyLevel: String = "Normal"   // Normal, Important, Urgent
    )

    data class DivineSign(
        val signType: String = "",
        val observedIn: String = "",         // Dream, Nature, Daily Life, Ritual, Meditation
        val puranicReference: String = "",
        val interpretation: String = "",
        val recommendedAction: String = ""
    )

    // ──────────────────────────────────────────────
    // Main Dream Interpretation Engine
    // ──────────────────────────────────────────────

    /**
     * Interpret a dream based on Swapna Shastra and Puranic knowledge.
     */
    fun interpretDream(dreamDescription: String): DreamInterpretation {
        val lower = dreamDescription.lowercase(Locale.ROOT)

        return when {
            // ─── Divine / Deity Dreams ─────────────
            lower.contains("radha") || lower.contains("राधा") ->
                DreamInterpretation(
                    dreamType = "Radha Rani Darshan",
                    category = "Highly Auspicious",
                    scripturalReference = "Brahma Vaivarta Purana — Radha is the Hladini Shakti of Krishna. Seeing Radha in a dream is a sign of divine grace.",
                    spiritualMeaning = "Radha Rani's appearance in your dream is a profound blessing. It signifies that your devotion has reached her divine presence. She is showering her love and protection upon you and your family. This dream indicates spiritual purification and elevation.",
                    divineMessage = "🙏 श्री राधा रानी आप पर कृपा कर रही हैं। आपकी भक्ति स्वीकार हुई है। निरंतर राधे-राधे का जाप करते रहें। राधा रानी आपके जीवन में प्रेम, समृद्धि और आध्यात्मिक उन्नति लाएंगी।",
                    suggestedRemedy = "आज विशेष रूप से 21,000 राधा जाप करें। राधा रानी को मिठाई का भोग लगाएं। राधा-कृष्ण मंदिर में दीप दान करें।",
                    astrologicalSignificance = "This dream is most powerful during Shravan Nakshatra or on Radhashtami. It neutralizes negative planetary influences and strengthens the Moon (mind) and Venus (devotion).",
                    urgencyLevel = "Normal"
                )

            lower.contains("krishna") || lower.contains("कृष्ण") || lower.contains("kanha") || lower.contains("mohan") ->
                DreamInterpretation(
                    dreamType = "Lord Krishna Darshan",
                    category = "Highly Auspicious",
                    scripturalReference = "Bhagavad Gita 4.7-8 — Whenever dharma declines, the Lord manifests. Seeing Krishna in a dream is His divine assurance.",
                    spiritualMeaning = "Lord Krishna's appearance in your dream is a direct sign of His protection and guidance. He is reassuring you that He is with you in your journey. This dream indicates that you are on the right path and He will remove all obstacles.",
                    divineMessage = "🕉️ श्री कृष्ण आपको आशीर्वाद दे रहे हैं। 'मैं तुम्हारी रक्षा करूंगा, निर्भय होकर अपना कर्म करो।' आपके सभी संकट दूर होंगे।",
                    suggestedRemedy = "भगवद गीता का एक अध्याय पढ़ें। कृष्ण मंदिर जाएं या घर पर ही दीप जलाएं। 'ॐ कृष्णाय नमः' का 108 बार जाप करें।",
                    astrologicalSignificance = "Strengthens Jupiter (guru) and brings wisdom. Removes obstacles caused by Shani (Saturn).",
                    urgencyLevel = "Normal"
                )

            lower.contains("shiva") || lower.contains("mahadev") || lower.contains("शिव") || lower.contains("भोलेनाथ") ->
                DreamInterpretation(
                    dreamType = "Lord Shiva Darshan",
                    category = "Highly Auspicious",
                    scripturalReference = "Shiva Purana — Lord Shiva's vision in a dream grants liberation from fears and negative energies.",
                    spiritualMeaning = "Lord Shiva's appearance signifies the destruction of negative forces in your life. He is removing your fears, attachments, and karmic burdens. This dream indicates spiritual transformation and renewal.",
                    divineMessage = "🔱 भगवान शिव आपको निर्भयता का वरदान दे रहे हैं। 'ॐ नमः शिवाय' का जाप आपकी सभी बाधाओं को दूर करेगा। महादेव आपकी रक्षा कर रहे हैं।",
                    suggestedRemedy = "सोमवार के दिन शिव मंदिर जाएं। शिवलिंग पर जल, दूध और बिल्व पत्र चढ़ाएं। 'ॐ नमः शिवाय' का 108 बार जाप करें। रुद्राभिषेक करवाएं।",
                    astrologicalSignificance = "Neutralizes malefic effects of Mars (Mangal) and Saturn (Shani). Strengthens the Moon for mental peace.",
                    urgencyLevel = "Normal"
                )

            lower.contains("durga") || lower.contains("दुर्गा") || lower.contains("devi") || lower.contains("माँ") ->
                DreamInterpretation(
                    dreamType = "Maa Durga / Divine Mother Darshan",
                    category = "Highly Auspicious",
                    scripturalReference = "Devi Mahatmyam (Durga Saptashati) — The Divine Mother protects her devotees from all evils.",
                    spiritualMeaning = "Maa Durga's appearance signifies that she is removing all obstacles and protecting you from negative forces. This dream indicates victory over challenges and the arrival of strength and courage.",
                    divineMessage = "🙏 माँ दुर्गा आपकी रक्षा कर रही हैं। 'सर्वमङ्गलमाङ्गल्ये शिवे सर्वार्थसाधिके' — वह आपके सभी कष्टों को दूर करेंगी। निर्भय रहें।",
                    suggestedRemedy = "दुर्गा सप्तशती का पाठ करें। माँ को लाल फूल और सिंदूर अर्पित करें। मंगलवार या शुक्रवार को देवी मंदिर जाएं।",
                    astrologicalSignificance = "Removes the malefic effects of Rahu and Ketu. Brings courage and protection.",
                    urgencyLevel = "Normal"
                )

            lower.contains("kali") || lower.contains("काली") || lower.contains("maa kali") ->
                DreamInterpretation(
                    dreamType = "Maa Kali Darshan",
                    category = "Highly Auspicious (but intense)",
                    scripturalReference = "Devi Mahatmyam, Chapter 7 — Maa Kali is the destroyer of demons and negative forces.",
                    spiritualMeaning = "Maa Kali's appearance indicates that she is destroying deep-rooted negative patterns, enemies, and karmic impurities. This dream may feel intense but is extremely purifying. She is removing what no longer serves your spiritual growth.",
                    divineMessage = "🖤 माँ काली आपके शत्रुओं और नकारात्मक शक्तियों का नाश कर रही हैं। यह आध्यात्मिक शुद्धि का समय है। डरें नहीं, माँ आपकी रक्षा कर रही हैं।",
                    suggestedRemedy = "माँ काली को लाल फूल और नारियल चढ़ाएं। 'ॐ क्रीं कालिकायै नमः' का 108 बार जाप करें। मंगलवार या शनिवार को काली मंदिर जाएं।",
                    astrologicalSignificance = "Powerful remedy for Ketu-related issues. Destroys black magic and negative energies.",
                    urgencyLevel = "Important"
                )

            lower.contains("ganesh") || lower.contains("गणेश") || lower.contains("ganpati") || lower.contains("गणपति") ->
                DreamInterpretation(
                    dreamType = "Lord Ganesha Darshan",
                    category = "Highly Auspicious",
                    scripturalReference = "Ganapati Atharvashirsha Upanishad — Ganesha is the remover of all obstacles.",
                    spiritualMeaning = "Lord Ganesha's appearance signifies that all obstacles in your path will be removed. New beginnings will be successful. This dream indicates wisdom, prosperity, and auspiciousness coming your way.",
                    divineMessage = "🐘 श्री गणेश आपको सफलता का आशीर्वाद दे रहे हैं। 'ॐ गं गणपतये नमः' — आपके सभी कार्य बिना विघ्न के पूर्ण होंगे। नए कार्य शुरू करने का यह शुभ समय है।",
                    suggestedRemedy = "गणेश मंदिर जाएं। दूर्वा (grass) और मोदक चढ़ाएं। बुधवार को गणेश चतुर्थी का व्रत रखें। 'ॐ गं गणपतये नमः' का 108 बार जाप करें।",
                    astrologicalSignificance = "Removes obstacles caused by Mercury (Budh). Brings success in new ventures.",
                    urgencyLevel = "Normal"
                )

            // ─── Elemental / Nature Dreams ─────────
            lower.contains("water") || lower.contains("पानी") || lower.contains("समुद्र") || lower.contains("नदी") || lower.contains("river") || lower.contains("ocean") || lower.contains("sea") ->
                DreamInterpretation(
                    dreamType = "Water Element Dream",
                    category = "Mixed (depends on water condition)",
                    scripturalReference = "Swapna Shastra — Water represents emotions, purification, and the subconscious mind.",
                    spiritualMeaning = when {
                        lower.contains("clean") || lower.contains("clear") || lower.contains("स्वच्छ") || lower.contains("शांत") ->
                            "Clean, clear water indicates emotional purity, peace, and spiritual clarity. Your mind is in a balanced state. This is a very positive sign indicating prosperity and happiness."
                        lower.contains("dirty") || lower.contains("muddy") || lower.contains("गंदा") || lower.contains("कीचड़") ->
                            "Dirty or muddy water indicates emotional turmoil, confusion, or unresolved issues. The subconscious is asking you to address these emotions."
                        lower.contains("flood") || lower.contains("बाढ़") || lower.contains("overflow") ->
                            "Flooding water indicates overwhelming emotions or situations that feel out of control. You may be feeling overwhelmed by responsibilities."
                        lower.contains("drowning") || lower.contains("डूबना") ->
                            "Drowning indicates feeling overwhelmed by emotions or circumstances. Seek support and practice surrender to the Divine."
                        else ->
                            "Water in dreams represents the flow of emotions and life. Pay attention to the state of the water — clear water is auspicious, turbulent water suggests emotional challenges."
                    },
                    divineMessage = "जल आपके मन की स्थिति का दर्पण है। शांत जल शांत मन का संकेत है। गंदा जल अशुद्ध विचारों को शुद्ध करने का आह्वान है। गंगा जल का ध्यान करें।",
                    suggestedRemedy = "गंगा जल का छिड़काव करें। पानी में काला तिल डालकर स्नान करें। सूर्य को जल अर्पित करें। 'ॐ नमः शिवाय' का जाप करें।",
                    astrologicalSignificance = "Water dreams relate to the Moon (Chandra) and emotions. Clean water strengthens the Moon; dirty water indicates lunar affliction.",
                    urgencyLevel = if (lower.contains("flood") || lower.contains("drowning")) "Important" else "Normal"
                )

            lower.contains("fire") || lower.contains("आग") || lower.contains("अग्नि") ->
                DreamInterpretation(
                    dreamType = "Fire Element Dream",
                    category = "Mixed",
                    scripturalReference = "Swapna Shastra — Fire represents transformation, purification, and divine energy (Agni).",
                    spiritualMeaning = when {
                        lower.contains("burning") && !lower.contains("house") && !lower.contains("घर") ->
                            "Fire that does not destroy represents spiritual transformation and purification. Old patterns are being burned away for renewal."
                        lower.contains("house") && lower.contains("burning") || lower.contains("घर") && lower.contains("जलना") ->
                            "A house burning in a dream can indicate major transformation in family or domestic life. It may feel frightening but often represents the removal of old structures to make way for new."
                        lower.contains("lamp") || lower.contains("दीपक") || lower.contains("ज्योति") ->
                            "A flame or lamp in a dream is highly auspicious — it represents divine light, knowledge, and the presence of the Divine in your life."
                        lower.contains("bonfire") || lower.contains("हवन") || lower.contains("yagna") ->
                            "Sacred fire (yagna/havan) in a dream indicates spiritual purification and the fulfillment of prayers."
                        else ->
                            "Fire represents the transformative power of the Divine. It burns away impurities and prepares you for spiritual growth."
                    },
                    divineMessage = "अग्नि देवता आपको शुद्ध कर रहे हैं। जो पुराना है वह जल रहा है, नए की शुरुआत होने वाली है। हवन और यज्ञ से आपकी मनोकामनाएं पूरी होंगी।",
                    suggestedRemedy = "हवन या अग्निहोत्र करें। घी का दीपक जलाएं। सूर्य को अर्घ्य दें। 'ॐ अग्नये नमः' का 108 बार जाप करें।",
                    astrologicalSignificance = "Fire relates to the Sun (Surya) and Mars (Mangala). Purifies karmic debts and strengthens willpower.",
                    urgencyLevel = if (lower.contains("house") && lower.contains("burning")) "Important" else "Normal"
                )

            lower.contains("snake") || lower.contains("साँप") || lower.contains("नाग") || lower.contains("sarpa") ->
                DreamInterpretation(
                    dreamType = "Snake / Naga Dream",
                    category = "Mixed (highly significant)",
                    scripturalReference = "Swapna Shastra — Snakes represent Kundalini energy, transformation, and divine protection (Nagas).",
                    spiritualMeaning = when {
                        lower.contains("bite") || lower.contains("काटना") ->
                            "A snake bite in a dream indicates a spiritual awakening or initiation. The Kundalini energy is being activated. It can also indicate healing from deep-seated fears."
                        lower.contains("cobra") || lower.contains("नाग") || lower.contains("hood") ->
                            "A cobra with its hood expanded represents divine protection and the awakening of spiritual power. Lord Shiva's serpent is a sign of mastery over death and time."
                        lower.contains("dead") || lower.contains("मरा") || lower.contains("मृत") ->
                            "A dead snake indicates the end of an enemy or obstacle. Fears that have been holding you back are being resolved."
                        lower.contains("multiple") || lower.contains("अनेक") || lower.contains("many") ->
                            "Multiple snakes indicate multiple sources of wisdom or multiple challenges. It can also represent the many layers of the subconscious being revealed."
                        else ->
                            "Snakes in dreams are powerful spiritual symbols. They represent transformation, healing, and the awakening of divine energy within you."
                    },
                    divineMessage = "नाग देवता आपको जागृति का संदेश दे रहे हैं। कुंडलिनी शक्ति जागृत हो रही है। शिवजी के नाग से सुरक्षा का भाव रखें। सर्प दर्शन शुभ है।",
                    suggestedRemedy = "नाग पंचमी पर नाग देवता की पूजा करें। शिव मंदिर में दूध चढ़ाएं। 'ॐ नागदेवाय नमः' का 108 बार जाप करें। सर्प सूक्त का पाठ करें।",
                    astrologicalSignificance = "Snakes relate to Rahu and Ketu (the lunar nodes). This dream indicates karmic clearing and spiritual evolution.",
                    urgencyLevel = if (lower.contains("bite")) "Important" else "Normal"
                )

            lower.contains("temple") || lower.contains("मंदिर") || lower.contains("मन्दिर") ->
                DreamInterpretation(
                    dreamType = "Temple / Sacred Space Dream",
                    category = "Highly Auspicious",
                    scripturalReference = "Swapna Shastra — Seeing a temple in a dream indicates divine presence and spiritual progress.",
                    spiritualMeaning = when {
                        lower.contains("enter") || lower.contains("प्रवेश") || lower.contains("inside") ->
                            "Entering a temple in a dream indicates that you are being welcomed into a higher state of consciousness. Your prayers are being heard."
                        lower.contains("broken") || lower.contains("टूटा") || lower.contains("ruins") ->
                            "A broken temple may indicate neglected spiritual practices. The Divine is calling you to renew your devotion."
                        lower.contains("worship") || lower.contains("पूजा") || lower.contains("aarti") ->
                            "Performing worship in a temple dream is highly auspicious — it indicates that your devotion is accepted and you are being blessed."
                        else ->
                            "Seeing a temple indicates spiritual progress and divine blessings. The Divine is drawing you closer."
                    },
                    divineMessage = "मंदिर का स्वप्न अत्यंत शुभ है। भगवान आपको अपने समीप बुला रहे हैं। नियमित मंदिर जाने का संकल्प लें। आपकी आस्था गहरी हो रही है।",
                    suggestedRemedy = "आज मंदिर अवश्य जाएं। घर में दीप जलाएं। किसी मंदिर में दान करें। भगवान का ध्यान करें।",
                    astrologicalSignificance = "Strengthens Jupiter (Guru) and brings spiritual growth. Removes obstacles in spiritual path.",
                    urgencyLevel = "Normal"
                )

            lower.contains("death") || lower.contains("मृत्यु") || lower.contains("मरना") || lower.contains("dead") ->
                DreamInterpretation(
                    dreamType = "Death / Endings Dream",
                    category = "Transformation (not inauspicious)",
                    scripturalReference = "Bhagavad Gita 2.22 — The soul is eternal; death is merely changing bodies. Swapna Shastra: Death in dreams symbolizes transformation, not literal death.",
                    spiritualMeaning = "Death in a dream is NOT a bad omen. It represents the end of one phase and the beginning of another. Old habits, relationships, or situations are ending to make way for new growth. It is a symbol of profound transformation and rebirth.",
                    divineMessage = "मृत्यु का स्वप्न शुभ है — यह पुराने के अंत और नए की शुरुआत का संकेत है। आत्मा अमर है, शरीर ही बदलता है। भगवद गीता का स्मरण करें।",
                    suggestedRemedy = "भगवद गीता का दूसरा अध्याय पढ़ें। पितरों का तर्पण करें। गरीबों को भोजन दान करें। 'ॐ नमः शिवाय' का जाप करें।",
                    astrologicalSignificance = "Death dreams often occur during major planetary transits (Sade Sati, Dasha changes). Indicates karmic completion.",
                    urgencyLevel = "Normal"
                )

            lower.contains("marriage") || lower.contains("शादी") || lower.contains("विवाह") || lower.contains("wedding") ->
                DreamInterpretation(
                    dreamType = "Marriage / Union Dream",
                    category = "Highly Auspicious",
                    scripturalReference = "Swapna Shastra — Marriage in a dream represents union, commitment, and spiritual integration.",
                    spiritualMeaning = when {
                        lower.contains("own") || lower.contains("अपनी") || lower.contains("self") ->
                            "Dreaming of your own marriage indicates a new beginning, a significant commitment, or spiritual union with the Divine."
                        lower.contains("someone") || lower.contains("किसी") || lower.contains("other") ->
                            "Seeing someone else's marriage indicates happiness and celebrations coming your way."
                        else ->
                            "Marriage in a dream represents the union of opposites, integration of the self, and divine union (Shiva-Shakti)."
                    },
                    divineMessage = "विवाह का स्वप्न शुभ है। यह नए संबंधों, प्रतिबद्धता और आध्यात्मिक एकता का संकेत है। राधा-कृष्ण के दिव्य प्रेम का स्मरण करें।",
                    suggestedRemedy = "राधा-कृष्ण की पूजा करें। सुहाग की वस्तुओं का दान करें। 'ॐ राधायै नमः' का जाप करें।",
                    astrologicalSignificance = "Strengthens Venus (Shukra) and brings harmony in relationships.",
                    urgencyLevel = "Normal"
                )

            lower.contains("child") || lower.contains("बच्चा") || lower.contains("शिशु") || lower.contains("baby") ->
                DreamInterpretation(
                    dreamType = "Child / Baby Dream",
                    category = "Highly Auspicious",
                    scripturalReference = "Swapna Shastra — A child in a dream represents new beginnings, creativity, and divine blessing.",
                    spiritualMeaning = when {
                        lower.contains("own") || lower.contains("अपना") || lower.contains("your") ->
                            "Dreaming of your own child indicates blessings for your family. For those trying to conceive, this is a positive sign."
                        lower.contains("crying") || lower.contains("रोना") ->
                            "A crying child may indicate unexpressed emotions or a creative project that needs attention."
                        else ->
                            "A child represents purity, new beginnings, and the innocent joy of the soul. This is a very auspicious dream."
                    },
                    divineMessage = "बच्चे का स्वप्न अत्यंत शुभ है। यह नई शुरुआत, रचनात्मकता और ईश्वरीय आशीर्वाद का प्रतीक है। बाल कृष्ण का स्मरण करें।",
                    suggestedRemedy = "बाल कृष्ण की पूजा करें। बच्चों को मिठाई बांटें। कन्या पूजन करें। 'ॐ कृष्णाय नमः' का जाप करें।",
                    astrologicalSignificance = "Strengthens Jupiter (Guru) and the 5th house (children, creativity).",
                    urgencyLevel = "Normal"
                )

            lower.contains("food") || lower.contains("खाना") || lower.contains("भोजन") || lower.contains("prasad") || lower.contains("प्रसाद") ->
                DreamInterpretation(
                    dreamType = "Food / Prasad Dream",
                    category = "Auspicious",
                    scripturalReference = "Swapna Shastra — Food in dreams represents nourishment, abundance, and divine blessings.",
                    spiritualMeaning = when {
                        lower.contains("prasad") || lower.contains("प्रसाद") || lower.contains("temple") ->
                            "Receiving prasad in a dream is highly auspicious — it indicates that the Divine is directly blessing you with grace."
                        lower.contains("sweet") || lower.contains("मीठा") || lower.contains("मिठाई") ->
                            "Sweet food indicates happiness, prosperity, and joyful events coming your way."
                        lower.contains("rotten") || lower.contains("सड़ा") || lower.contains("spoiled") ->
                            "Rotten or spoiled food may indicate spiritual malnutrition or the need to purify your thoughts and actions."
                        else ->
                            "Food represents spiritual and material nourishment. Abundant food indicates prosperity; scarcity may indicate the need for spiritual sustenance."
                    },
                    divineMessage = "भोजन का स्वप्न ईश्वर की कृपा का संकेत है। प्रसाद प्राप्त करना विशेष रूप से शुभ है। अन्न दान का महत्व समझें।",
                    suggestedRemedy = "अन्न दान करें। किसी भूखे को भोजन कराएं। मंदिर में प्रसाद चढ़ाएं। 'ॐ अन्नपूर्णायै नमः' का जाप करें।",
                    astrologicalSignificance = "Strengthens Jupiter (Guru) and brings abundance. Removes poverty-related planetary afflictions.",
                    urgencyLevel = "Normal"
                )

            lower.contains("flight") || lower.contains("उड़ना") || lower.contains("flying") || lower.contains("आकाश") ->
                DreamInterpretation(
                    dreamType = "Flying / Sky Dream",
                    category = "Highly Auspicious",
                    scripturalReference = "Swapna Shastra — Flying represents spiritual elevation, freedom, and transcendence of limitations.",
                    spiritualMeaning = "Flying in a dream indicates that you are rising above your problems and limitations. It represents spiritual growth, freedom, and the expansion of consciousness. This is a sign of progress on your spiritual path.",
                    divineMessage = "उड़ने का स्वप्न आध्यात्मिक उन्नति का संकेत है। आप सीमाओं से ऊपर उठ रहे हैं। आपकी चेतना का विस्तार हो रहा है। गरुड़ पुराण में इसे शुभ माना गया है।",
                    suggestedRemedy = "ध्यान (meditation) का अभ्यास बढ़ाएं। प्राणायाम करें। गरुड़ मंत्र का जाप करें। ऊंचाई पर स्थित मंदिर में दर्शन करें।",
                    astrologicalSignificance = "Strengthens the Sun (Surya) and Jupiter (Guru). Indicates spiritual progress and liberation from earthly bonds.",
                    urgencyLevel = "Normal"
                )

            lower.contains("fall") || lower.contains("गिरना") || lower.contains("falling") ->
                DreamInterpretation(
                    dreamType = "Falling Dream",
                    category = "Cautionary",
                    scripturalReference = "Swapna Shastra — Falling represents fear of losing control, ego deflation, or a need for grounding.",
                    spiritualMeaning = "Falling in a dream indicates that you may be feeling insecure or out of control in some area of life. It can also represent the ego being humbled. The Divine is asking you to surrender and trust the process.",
                    divineMessage = "गिरने का स्वप्न चेतावनी है — अपने कदम संभाल कर रखें। अहंकार को त्यागें और ईश्वर पर भरोसा करें। विनम्रता ही सच्ची शक्ति है।",
                    suggestedRemedy = "भूमि पर ष्टंड करें (grounding meditation)। पृथ्वी मंत्र का जाप करें। किसी बुजुर्ग का आशीर्वाद लें। 'ॐ भूर्भुवः स्वः' का जाप करें।",
                    astrologicalSignificance = "May indicate a challenging planetary period. Strengthen your foundation through spiritual practice.",
                    urgencyLevel = "Important"
                )

            lower.contains("teeth") || lower.contains("दांत") || lower.contains("दाँत") || lower.contains("tooth") ->
                DreamInterpretation(
                    dreamType = "Teeth Dream",
                    category = "Cautionary",
                    scripturalReference = "Swapna Shastra — Teeth represent strength, communication, and family roots.",
                    spiritualMeaning = when {
                        lower.contains("fall") || lower.contains("break") || lower.contains("टूटना") || lower.contains("गिरना") ->
                            "Teeth falling or breaking can indicate concerns about appearance, communication, or family matters. It may also indicate anxiety about aging or loss of power."
                        lower.contains("rot") || lower.contains("सड़ना") || lower.contains("decay") ->
                            "Decaying teeth may indicate neglected aspects of life that need attention — health, relationships, or spiritual practice."
                        else ->
                            "Teeth dreams often relate to how you communicate or present yourself to the world. Pay attention to what you are saying or not saying."
                    },
                    divineMessage = "दांतों का स्वप्न आपके संवाद और आत्मविश्वास से जुड़ा है। मन की बात स्पष्ट रूप से कहें। किसी चिंता को व्यक्त करें।",
                    suggestedRemedy = "गायत्री मंत्र का जाप करें। पितरों का तर्पण करें। दंत चिकित्सक से जांच कराएं (यदि शारीरिक चिंता हो)। 'ॐ दंष्ट्रायै नमः' का जाप करें।",
                    astrologicalSignificance = "May relate to family lineage (pitri dosha) or communication issues (Mercury).",
                    urgencyLevel = "Normal"
                )

            lower.contains("money") || lower.contains("पैसा") || lower.contains("धन") || lower.contains("wealth") || lower.contains("gold") || lower.contains("सोना") ->
                DreamInterpretation(
                    dreamType = "Wealth / Money Dream",
                    category = "Mixed",
                    scripturalReference = "Swapna Shastra — Wealth in dreams represents spiritual richness, not just material wealth.",
                    spiritualMeaning = when {
                        lower.contains("find") || lower.contains("मिलना") || lower.contains("receive") ->
                            "Finding or receiving money indicates that you are about to receive blessings — both material and spiritual. Be grateful."
                        lower.contains("lose") || lower.contains("खोना") || lower.contains("lost") ->
                            "Losing money may indicate attachment to material things. The Divine is teaching detachment."
                        lower.contains("gold") || lower.contains("सोना") ->
                            "Gold in a dream is highly auspicious — it represents purity, prosperity, and divine light."
                        else ->
                            "Money represents energy and value. Consider what you truly value in life."
                    },
                    divineMessage = "धन का स्वप्न लक्ष्मी जी की कृपा का संकेत है। लेकिन सच्चा धन आध्यात्मिक है। माँ लक्ष्मी को केवल धन के लिए नहीं, बल्कि कृतज्ञता के लिए याद करें।",
                    suggestedRemedy = "माँ लक्ष्मी की पूजा करें। दान करें। 'ॐ श्रीं महालक्ष्म्यै नमः' का 108 बार जाप करें। शुक्रवार को व्रत रखें।",
                    astrologicalSignificance = "Strengthens Venus (Shukra) and Jupiter (Guru). Brings prosperity and abundance.",
                    urgencyLevel = "Normal"
                )

            lower.contains("storm") || lower.contains("तूफान") || lower.contains("तूफ़ान") || lower.contains("hurricane") || lower.contains("tornado") || lower.contains("बवंडर") ->
                DreamInterpretation(
                    dreamType = "Storm / Turbulence Dream",
                    category = "Cautionary",
                    scripturalReference = "Swapna Shastra — Storms represent upheaval, change, and the cleansing power of nature.",
                    spiritualMeaning = "A storm in a dream indicates that significant changes are coming. While storms can be frightening, they also clear the air and bring renewal. The Divine is preparing you for transformation. After every storm, there is peace.",
                    divineMessage = "तूफान परिवर्तन का संकेत है। यह कठिन लग सकता है, लेकिन हर तूफान के बाद शांति आती है। भगवान पर विश्वास रखें। यह समय भी गुजर जाएगा।",
                    suggestedRemedy = "हनुमान चालीसा का पाठ करें। शनि मंदिर जाएं। 'ॐ नमः शिवाय' का जाप करें। घर में धूप और दीप जलाएं।",
                    astrologicalSignificance = "Often occurs during major planetary transits (Sade Sati, Dasha changes). Indicates karmic cleansing.",
                    urgencyLevel = "Important"
                )

            lower.contains("light") || lower.contains("रोशनी") || lower.contains("प्रकाश") || lower.contains("divine light") || lower.contains("दिव्य प्रकाश") ->
                DreamInterpretation(
                    dreamType = "Divine Light / Vision",
                    category = "Highly Auspicious",
                    scripturalReference = "Bhagavad Gita 11.12 — The divine light is beyond comparison. Upanishads describe Brahman as pure consciousness and light.",
                    spiritualMeaning = "Seeing divine light in a dream is one of the most auspicious spiritual experiences. It indicates that you are receiving direct grace from the Divine. This is a sign of spiritual awakening, purification, and the removal of ignorance.",
                    divineMessage = "🪔 दिव्य प्रकाश का दर्शन अत्यंत दुर्लभ और शुभ है। यह ब्रह्मज्ञान का प्रकाश है। आप आध्यात्मिक रूप से जागृत हो रहे हैं। गुरु की कृपा आप पर है।",
                    suggestedRemedy = "गायत्री मंत्र का 108 बार जाप करें। ध्यान (meditation) का अभ्यास गहरा करें। गुरु को धन्यवाद दें। दीप दान करें।",
                    astrologicalSignificance = "Strengthens the Sun (Surya) and Jupiter (Guru). Indicates spiritual enlightenment and karmic purification.",
                    urgencyLevel = "Normal"
                )

            // ─── Default / General Interpretation ──
            else -> getGeneralInterpretation(dreamDescription)
        }
    }

    /**
     * General dream interpretation for unclassified dreams.
     */
    private fun getGeneralInterpretation(dreamDescription: String): DreamInterpretation {
        val lower = dreamDescription.lowercase(Locale.ROOT)

        // Check for emotional tone
        val isPositive = listOf("happy", "joy", "beautiful", "peaceful", "love", "blessing", "सुख", "शांति", "प्रेम", "खुशी")
            .any { lower.contains(it) }
        val isNegative = listOf("scared", "fear", "sad", "crying", "angry", "pain", "horror", "nightmare", "डर", "भय", "दुख", "रोना")
            .any { lower.contains(it) }

        return when {
            isPositive -> DreamInterpretation(
                dreamType = "Positive / Blissful Dream",
                category = "Auspicious",
                scripturalReference = "Swapna Shastra — Pleasant dreams indicate a pure mind and divine blessings.",
                spiritualMeaning = "Your dream carries positive energy and divine blessings. It reflects the purity of your heart and the grace of the Divine upon your life. Such dreams indicate that your spiritual practice is bearing fruit.",
                divineMessage = "आपका स्वप्न शुभ है। यह आपके शुद्ध मन और ईश्वर की कृपा का प्रतिबिंब है। अपनी साधना जारी रखें। राधे-राधे! 🙏",
                suggestedRemedy = "इस शुभ दिन की शुरुआत राधे-राधे से करें। मंदिर जाएं या घर में ही पूजा करें। दान करें।",
                astrologicalSignificance = "Positive dreams indicate strong beneficial planetary influences. Continue your spiritual practice.",
                urgencyLevel = "Normal"
            )
            isNegative -> DreamInterpretation(
                dreamType = "Disturbing / Fearful Dream",
                category = "Cautionary / Purification",
                scripturalReference = "Swapna Shastra — Disturbing dreams often indicate the purification of subconscious fears and karmic impressions.",
                spiritualMeaning = "Disturbing dreams are not necessarily bad omens. They often indicate that deep-seated fears, anxieties, or karmic impressions are rising to the surface to be released. The Divine is purifying your subconscious mind. Do not be afraid — this is a healing process.",
                divineMessage = "भयावह स्वप्न देखकर चिंता न करें। यह आपके अवचेतन मन की शुद्धि का संकेत है। पुराने भय और कर्म संस्कार बाहर निकल रहे हैं। हनुमान चालीसा का पाठ करें और निर्भय रहें।",
                suggestedRemedy = "हनुमान चालीसा का पाठ करें। 'ॐ नमः शिवाय' का 108 बार जाप करें। सरसों के तेल का दीपक जलाएं। नींबू-मिर्च का प्रयोग करें। शनि मंदिर जाएं।",
                astrologicalSignificance = "May indicate challenging planetary periods (Sade Sati, Dasha). Perform remedial measures as suggested.",
                urgencyLevel = "Important"
            )
            else -> DreamInterpretation(
                dreamType = "General / Mixed Dream",
                category = "Neutral",
                scripturalReference = "Swapna Shastra — All dreams are messages from the subconscious and the Divine. Pay attention to recurring themes.",
                spiritualMeaning = "Your dream contains elements that require deeper reflection. Every dream carries a message from your subconscious mind and, ultimately, from the Divine. Consider the emotions you felt in the dream and any recurring symbols. These are clues to the message being conveyed.",
                divineMessage = "हर स्वप्न ईश्वर का संदेश है। अपने स्वप्न के भाव और प्रतीकों पर ध्यान दें। नियमित ध्यान और जाप से आप स्वप्नों के गहरे अर्थ समझ पाएंगे। राधे-राधे! 🙏",
                suggestedRemedy = "सोने से पहले राधे-राधे का जाप करें। स्वप्न डायरी रखें। गायत्री मंत्र का जाप करें। ध्यान का अभ्यास बढ़ाएं।",
                astrologicalSignificance = "Mixed dreams are normal and reflect the natural flow of subconscious processing. Regular spiritual practice brings clarity.",
                urgencyLevel = "Normal"
            )
        }
    }

    // ──────────────────────────────────────────────
    // Divine Sign & Omen Interpretation
    // ──────────────────────────────────────────────

    /**
     * Interpret a divine sign or omen observed in daily life.
     */
    fun interpretDivineSign(description: String): DivineSign {
        val lower = description.lowercase(Locale.ROOT)

        return when {
            // ─── Animal Signs ──────────────────────
            lower.contains("peacock") || lower.contains("मोर") || lower.contains("mayur") ->
                DivineSign(
                    signType = "Peacock Sighting",
                    observedIn = "Daily Life",
                    puranicReference = "Lord Krishna adorns a peacock feather. The peacock is the vahana of Lord Kartikeya and Maa Saraswati.",
                    interpretation = "Seeing a peacock or its feather is highly auspicious. It indicates beauty, grace, and divine protection. Lord Krishna's blessings are with you. It also represents wisdom and knowledge (Saraswati's association).",
                    recommendedAction = "Keep a peacock feather in your home or puja room. Chant 'ॐ कृष्णाय नमः'. It attracts positive energy and removes negative influences."
                )

            lower.contains("cow") || lower.contains("गाय") || lower.contains("gau") ->
                DivineSign(
                    signType = "Cow Sighting / Blessing",
                    observedIn = "Daily Life",
                    puranicReference = "Atharva Veda — 'Gavo Vishvasya Matarah' (Cows are the mothers of the universe). Kamadhenu is the divine wish-fulfilling cow.",
                    interpretation = "Seeing a cow, especially a white cow, is extremely auspicious. It indicates that the Divine Mother is blessing you with abundance, nourishment, and protection. The cow represents selfless giving and motherhood.",
                    recommendedAction = "Feed a cow with roti or green grass. This act brings immense punya (merit). Chant 'ॐ गोमात्रे नमः'. Gau Seva (cow service) is highly recommended."
                )

            lower.contains("eagle") || lower.contains("गरुड़") || lower.contains("garuda") || lower.contains("vulture") || lower.contains("गिद्ध") ->
                DivineSign(
                    signType = "Eagle / Garuda Sighting",
                    observedIn = "Sky / Nature",
                    puranicReference = "Garuda is the divine vahana of Lord Vishnu. He represents speed, power, and the ability to rise above worldly attachments.",
                    interpretation = "Seeing an eagle or Garuda is a powerful sign. It indicates that Lord Vishnu is watching over you. You are being given the strength to rise above your problems and see the bigger picture. This is a sign of victory and protection.",
                    recommendedAction = "Chant Vishnu Sahasranama or 'ॐ नमो भगवते वासुदेवाय'. Offer water to the Sun. Donate white items (rice, milk) on Thursday."
                )

            lower.contains("butterfly") || lower.contains("तितली") || lower.contains("titli") ->
                DivineSign(
                    signType = "Butterfly Sighting",
                    observedIn = "Nature",
                    puranicReference = "In Hindu symbolism, the butterfly represents the soul's journey through cycles of birth, death, and rebirth (samsara).",
                    interpretation = "A butterfly sighting indicates transformation and spiritual growth. Just as a caterpillar transforms into a butterfly, you are undergoing a beautiful transformation. This is a sign of hope, renewal, and the beauty of change.",
                    recommendedAction = "Embrace the changes in your life. Start a new spiritual practice. Chant 'ॐ नमः शिवाय' for smooth transformation."
                )

            lower.contains("crow") || lower.contains("कौवा") || lower.contains("kag") || lower.contains("kak") ->
                DivineSign(
                    signType = "Crow Sighting / Cawing",
                    observedIn = "Daily Life",
                    puranicReference = "Crows are associated with ancestors (pitris). In Hindu tradition, feeding crows is a way to honor departed ancestors.",
                    interpretation = "A crow's presence, especially if it is cawing persistently or at an unusual time, may be a message from your ancestors. They are reminding you to perform their rituals (shraddha/tarpan) or they are blessing you. In some contexts, a crow at the doorstep indicates an upcoming visitor.",
                    recommendedAction = "Feed crows with rice or roti — this is considered feeding your ancestors. Perform tarpan for your pitris. Chant 'ॐ पितृभ्यो नमः'."
                )

            lower.contains("dog") || lower.contains("कुत्ता") || lower.contains("kutta") || lower.contains("shwan") ->
                DivineSign(
                    signType = "Dog Sighting / Interaction",
                    observedIn = "Daily Life",
                    puranicReference = "Dogs are associated with Bhairava (a fierce form of Lord Shiva) and Dharmaraj Yudhishthira's loyal companion in the Mahabharata.",
                    interpretation = "A dog's presence, especially if it follows you or is friendly, indicates loyalty, protection, and the presence of Bhairava's energy. In the Mahabharata, the dog that followed Yudhishthira to heaven was Dharma himself. This is a sign of righteousness and protection.",
                    recommendedAction = "Feed a stray dog. This pleases Bhairava and brings protection. Chant 'ॐ भैरवाय नमः'. Be kind to animals."
                )

            // ─── Natural Phenomena ─────────────────
            lower.contains("rainbow") || lower.contains("इंद्रधनुष") || lower.contains("indradhanush") ->
                DivineSign(
                    signType = "Rainbow Sighting",
                    observedIn = "Sky / Nature",
                    puranicReference = "The rainbow is considered Indra's bow (Indradhanush). It represents the bridge between heaven and earth.",
                    interpretation = "Seeing a rainbow is a sign of hope, promise, and divine assurance. Just as the rainbow appears after rain, happiness will follow your struggles. It is a sign that the Divine is keeping His promise to protect and guide you.",
                    recommendedAction = "Make a wish when you see a rainbow. Chant 'ॐ इन्द्राय नमः'. Thank the Divine for His blessings."
                )

            lower.contains("eclipse") || lower.contains("ग्रहण") || lower.contains("surya grahan") || lower.contains("chandra grahan") ->
                DivineSign(
                    signType = "Eclipse (Solar / Lunar)",
                    observedIn = "Sky / Celestial Event",
                    puranicReference = "The Puranas describe eclipses as times when Rahu and Ketu temporarily overcome the Sun and Moon. Special rituals are prescribed.",
                    interpretation = "An eclipse is a powerful spiritual time. It is ideal for meditation, mantra chanting, and spiritual practices. Avoid eating during the eclipse. The period after the eclipse is considered highly purified and auspicious for new beginnings.",
                    recommendedAction = "Take a bath after the eclipse. Chant the Mahamrityunjaya Mantra 108 times. Donate food and clothes. Clean the home temple."
                )

            lower.contains("shooting star") || lower.contains("टूटता तारा") || lower.contains("meteor") || lower.contains("उल्का") ->
                DivineSign(
                    signType = "Shooting Star / Meteor",
                    observedIn = "Night Sky",
                    puranicReference = "Shooting stars are considered divine signs. They represent the swift movement of celestial beings (devas).",
                    interpretation = "Seeing a shooting star is a powerful sign. Make a wish — it is believed that wishes made on shooting stars are more likely to manifest. It also indicates that a significant change or opportunity is coming your way swiftly.",
                    recommendedAction = "Make a wish with pure intention. Chant 'ॐ देवाय नमः'. Start a new positive habit or project."
                )

            // ─── Daily Life Signs ──────────────────
            lower.contains("coin") || lower.contains("सिक्का") || lower.contains("sikka") || lower.contains("money") && lower.contains("find") ->
                DivineSign(
                    signType = "Finding a Coin / Money",
                    observedIn = "Daily Life",
                    puranicReference = "Finding money is considered a blessing from Maa Lakshmi. It indicates that prosperity is coming your way.",
                    interpretation = "Finding a coin or money, especially if it is an old coin or an unexpected amount, is a sign of Maa Lakshmi's blessings. It indicates that your financial situation will improve. However, do not become attached — offer a portion in charity.",
                    recommendedAction = "Keep the coin in your wallet or puja room as a blessing. Donate an equal amount to charity. Chant 'ॐ श्रीं महालक्ष्म्यै नमः'."
                )

            lower.contains("lamp") || lower.contains("दीपक") || lower.contains("diya") || lower.contains("flame") && lower.contains("steady") ->
                DivineSign(
                    signType = "Steady Lamp Flame",
                    observedIn = "Ritual / Puja",
                    puranicReference = "A steady, bright flame in the lamp is considered a sign of divine presence and acceptance of prayers.",
                    interpretation = "If your puja lamp burns with a steady, bright flame, it indicates that your prayers are being accepted. The Divine is pleased with your devotion. A flickering flame may indicate the presence of divine energy or, in some contexts, the need for more focused concentration.",
                    recommendedAction = "Continue your daily puja with devotion. Offer ghee or oil to the lamp. Chant your ishta mantra with focus."
                )

            lower.contains("flower") || lower.contains("फूल") || lower.contains("pushpa") || lower.contains("petal") && lower.contains("fall") ->
                DivineSign(
                    signType = "Flower Petal Falling on You",
                    observedIn = "Temple / Puja",
                    puranicReference = "Flowers are offerings to the Divine. A flower or petal falling on you is considered a direct blessing.",
                    interpretation = "If a flower or petal falls on you during puja or in a temple, it is a direct blessing from the deity. The Divine is showering grace upon you. This is a sign that your devotion is recognized and accepted.",
                    recommendedAction = "Receive the flower with gratitude. Place it on your head as a blessing. Keep it in your puja room. Chant the deity's mantra."
                )

            else -> DivineSign(
                signType = "General Divine Sign",
                observedIn = "Daily Life",
                puranicReference = "Sanatana Dharma teaches that the Divine communicates through all of creation. Every moment carries a message.",
                interpretation = "The sign you observed is a reminder that the Divine is always present and communicating with you. Pay attention to your intuition and the synchronicities in your life. Nothing happens by coincidence — everything is a message from Bhagwan.",
                recommendedAction = "Spend time in silence and meditation. Observe nature and your surroundings with awareness. Chant 'ॐ' and listen to the silence within. Keep a journal of signs and synchronicities."
            )
        }
    }

    // ──────────────────────────────────────────────
    // Main Query Handler
    // ──────────────────────────────────────────────

    /**
     * Handle a dream interpretation or divine sign query.
     */
    fun handleDreamQuery(query: String): String {
        val lower = query.lowercase(Locale.ROOT)

        return when {
            // Divine sign interpretation
            lower.contains("sign") || lower.contains("शकुन") || lower.contains("omen") || lower.contains("अपशकुन") ||
            lower.contains("peacock") || lower.contains("मोर") || lower.contains("cow") || lower.contains("गाय") ||
            lower.contains("eagle") || lower.contains("गरुड़") || lower.contains("butterfly") || lower.contains("तितली") ||
            lower.contains("crow") || lower.contains("कौवा") || lower.contains("dog") || lower.contains("कुत्ता") ||
            lower.contains("rainbow") || lower.contains("इंद्रधनुष") || lower.contains("eclipse") || lower.contains("ग्रहण") ||
            lower.contains("shooting star") || lower.contains("टूटता तारा") || lower.contains("coin") || lower.contains("सिक्का") ||
            lower.contains("lamp") || lower.contains("दीपक") || lower.contains("flower") || lower.contains("फूल") -> {
                val sign = interpretDivineSign(query)
                "राधे-राधे! 🙏\n\n" +
                "🔮 **दिव्य संकेत (Divine Sign):** ${sign.signType}\n" +
                "📍 **कहाँ देखा (Observed):** ${sign.observedIn}\n\n" +
                "📜 **पौराणिक संदर्भ (Puranic Reference):**\n${sign.puranicReference}\n\n" +
                "💡 **व्याख्या (Interpretation):**\n${sign.interpretation}\n\n" +
                "📋 **सुझाव (Recommended Action):**\n${sign.recommendedAction}\n\n" +
                "🙏 राधे-राधे! हरि बोल!"
            }

            // Dream interpretation
            lower.contains("dream") || lower.contains("स्वप्न") || lower.contains("सपना") || lower.contains("dreamt") ||
            lower.contains("saw") || lower.contains("देखा") || lower.contains("vision") || lower.contains("दर्शन") ||
            lower.contains("night") || lower.contains("रात") || lower.contains("sleep") || lower.contains("नींद") -> {
                val interpretation = interpretDream(query)
                "राधे-राधे! 🙏\n\n" +
                "🌙 **स्वप्न शास्त्र विश्लेषण (Dream Analysis)**\n\n" +
                "📋 **स्वप्न प्रकार (Dream Type):** ${interpretation.dreamType}\n" +
                "🏷️ **श्रेणी (Category):** ${interpretation.category}\n" +
                "⚠️ **महत्व स्तर (Urgency):** ${interpretation.urgencyLevel}\n\n" +
                "📜 **शास्त्रीय संदर्भ (Scriptural Reference):**\n${interpretation.scripturalReference}\n\n" +
                "💡 **आध्यात्मिक अर्थ (Spiritual Meaning):**\n${interpretation.spiritualMeaning}\n\n" +
                "🕉️ **दिव्य संदेश (Divine Message):**\n${interpretation.divineMessage}\n\n" +
                "📋 **सुझाव (Suggested Remedy):**\n${interpretation.suggestedRemedy}\n\n" +
                "🌟 **ज्योतिषीय महत्व (Astrological Significance):**\n${interpretation.astrologicalSignificance}\n\n" +
                "🙏 राधे-राधे! हरि बोल!"
            }

            // Default
            else -> {
                "राधे-राधे! 🙏\n\n" +
                "🌙 **स्वप्न शास्त्र एवं दिव्य संकेत इंजिन**\n\n" +
                "मैं आपके स्वप्नों और दिव्य संकेतों का शास्त्रीय विश्लेषण कर सकता हूँ।\n\n" +
                "**स्वप्न विश्लेषण के लिए:**\n" +
                "अपना स्वप्न विस्तार से बताएं। उदाहरण:\n" +
                "• 'मैंने सपने में राधा रानी को देखा'\n" +
                "• 'सपने में मंदिर देखा और पूजा कर रहा था'\n" +
                "• 'सपने में साँप देखा, बहुत डर लगा'\n" +
                "• 'पानी में तैरने का सपना देखा'\n\n" +
                "**दिव्य संकेत विश्लेषण के लिए:**\n" +
                "कोई भी शकुन या संकेत बताएं। उदाहरण:\n" +
                "• 'आज सुबह मोर देखा'\n" +
                "• 'गाय मेरे घर के सामने आई'\n" +
                "• 'आसमान में इंद्रधनुष देखा'\n" +
                "• 'पूजा में दीपक की लौ स्थिर थी'\n\n" +
                "🙏 राधे-राधे! अपना अनुभव साझा करें।"
            }
        }
    }
}