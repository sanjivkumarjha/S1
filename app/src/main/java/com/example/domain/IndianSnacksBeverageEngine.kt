package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import java.util.Locale

/**
 * MODULE 21: INDIAN SNACKS, BEVERAGE, AUTHENTIC STREET FOOD CULINARY & HEALTH-FIRST PROACTIVE REMINDER ENGINE v27.0
 *
 * FEATURES:
 * - Vast repository of authentic Indian snacks, tea/coffee variants, and popular Indian Street Foods
 * - Expert-level mastery in traditional preparation and smart-hardware execution
 * - Hybrid assistance: textual/voice guidance OR autonomous smart hardware execution
 * - Health-first proactive reminders for hydration, breakfast, and tea/coffee breaks
 * - Worship-priority logic: no food/snack/beverage reminders until Brahmamuhurta worship cycle is complete
 */
class IndianSnacksBeverageEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    // ──────────────────────────────────────────────
    // Data Models
    // ──────────────────────────────────────────────

    data class SnackBeverageRecipe(
        val name: String = "",
        val category: String = "Snack", // Snack, Beverage, StreetFood, Dessert
        val subCategory: String = "",    // e.g., Chaat, Tea, Coffee, Roll, Momo, etc.
        val cuisine: String = "Indian",
        val preparationTime: Int = 0,
        val cookingTime: Int = 0,
        val totalTime: Int = 0,
        val difficulty: String = "Medium",
        val spiceLevel: String = "Medium",
        val servings: Int = 2,
        val isBeverage: Boolean = false,
        val isStreetFood: Boolean = false,
        val isSnack: Boolean = true,
        val temperature: String = "Hot", // Hot, Cold, Room Temperature
        val ingredients: List<String> = emptyList(),
        val steps: List<String> = emptyList(),
        val proTips: String = "",
        val nutritionalInfo: String = "",
        val healthTags: List<String> = emptyList(), // e.g., Hydrating, Energizing, Digestive, Comfort
        val smartApplianceMode: String = "", // "KETTLE", "COFFEE_MAKER", "INDUCTION", "AIR_FRYER", "MICROWAVE", "MANUAL"
        val smartApplianceParams: Map<String, Any> = emptyMap()
    )

    data class SmartKitchenHardware(
        val isConnected: Boolean = false,
        val applianceName: String = "",
        val applianceType: String = "",
        val protocol: String = "wifi",
        val ipAddress: String = ""
    )

    data class HealthReminder(
        val type: String = "", // HYDRATION, BREAKFAST, TEA_BREAK, COFFEE_BREAK, SNACK_TIME
        val title: String = "",
        val message: String = "",
        val suggestedTime: String = "",
        val priority: Int = 5, // 1-10
        val isWorshipGated: Boolean = true // Cannot fire until worship complete
    )

    // ──────────────────────────────────────────────
    // Main Recipe Router
    // ──────────────────────────────────────────────

    /**
     * Get recipe for a given Indian snack, beverage, or street food item.
     */
    fun getSnackBeverageRecipe(dishName: String): SnackBeverageRecipe {
        val lower = dishName.lowercase(Locale.ROOT)

        return when {
            // ─── TEA VARIANTS ────────────────────────
            lower.contains("masala chai") || (lower.contains("chai") && lower.contains("masala")) || lower.contains("spiced tea") ->
                getMasalaChaiRecipe()
            lower.contains("ginger tea") || lower.contains("adrak chai") || lower.contains("adrak wali chai") || (lower.contains("chai") && lower.contains("adrak")) ->
                getGingerTeaRecipe()
            lower.contains("tulsi tea") || lower.contains("tulsi chai") || lower.contains("holy basil tea") || (lower.contains("chai") && lower.contains("tulsi")) ->
                getTulsiTeaRecipe()
            lower.contains("green tea") || lower.contains("hari chai") ->
                getGreenTeaRecipe()
            lower.contains("kashmiri chai") || lower.contains("noon chai") || lower.contains("kashmiri tea") ->
                getKashmiriChaiRecipe()
            lower.contains("chai") || lower.contains("tea") || lower.contains("चाय") ->
                getMasalaChaiRecipe().copy(name = "Indian Chai (Tea)")

            // ─── COFFEE VARIANTS ─────────────────────
            lower.contains("filter coffee") || lower.contains("south indian coffee") || lower.contains("degree coffee") || lower.contains("kaapi") ->
                getFilterCoffeeRecipe()
            lower.contains("cold brew") && lower.contains("coffee") ->
                getColdBrewCoffeeRecipe()
            lower.contains("cold coffee") || lower.contains("iced coffee") || lower.contains("frappe") ->
                getColdCoffeeRecipe()
            lower.contains("instant coffee") || lower.contains("nescafe") ->
                getInstantCoffeeRecipe()
            lower.contains("coffee") || lower.contains("कॉफी") ->
                getFilterCoffeeRecipe().copy(name = "Indian Coffee")

            // ─── OTHER BEVERAGES ─────────────────────
            lower.contains("lassi") && lower.contains("sweet") ->
                getSweetLassiRecipe()
            lower.contains("lassi") && lower.contains("mango") ->
                getMangoLassiRecipe()
            lower.contains("lassi") && lower.contains("salt") || lower.contains("salted lassi") || lower.contains("namkin lassi") ->
                getSaltedLassiRecipe()
            lower.contains("lassi") ->
                getSweetLassiRecipe()
            lower.contains("buttermilk") || lower.contains("chaas") || lower.contains("chhach") || lower.contains("majjiga") ->
                getButtermilkRecipe()
            lower.contains("jaljeera") || lower.contains("jal jira") ->
                getJaljeeraRecipe()
            lower.contains("sharbat") || lower.contains("rooh afza") || lower.contains("roohafza") ->
                getSharbatRecipe()
            lower.contains("nimbu pani") || lower.contains("nimbu paani") || lower.contains("lemon water") || lower.contains("shikanji") || lower.contains("limbu pani") ->
                getNimbuPaniRecipe()
            lower.contains("aam panna") || lower.contains("aam ka panna") || lower.contains("raw mango drink") ->
                getAamPannaRecipe()
            lower.contains("bel sharbat") || lower.contains("bel ka sharbat") || lower.contains("wood apple") ->
                getBelSharbatRecipe()
            lower.contains("coconut water") || lower.contains("nariyal pani") || lower.contains("tender coconut") ->
                getCoconutWaterRecipe()
            lower.contains("smoothie") || lower.contains("milkshake") ->
                getMangoMilkshakeRecipe()

            // ─── STREET FOOD: Pani Puri / Golgappa ───
            lower.contains("pani puri") || lower.contains("golgappa") || lower.contains("gup chup") || lower.contains("phuchka") || lower.contains("puchka") || lower.contains("pani ke batashe") ->
                getPaniPuriRecipe()
            lower.contains("dahi puri") || lower.contains("dahi batata puri") ->
                getDahiPuriRecipe()
            lower.contains("sev puri") || lower.contains("sev batata puri") ->
                getSevPuriRecipe()

            // ─── STREET FOOD: Chaat ──────────────────
            lower.contains("bhel puri") || lower.contains("bhel") ->
                getBhelPuriRecipe()
            lower.contains("papdi chaat") || lower.contains("papri chaat") || lower.contains("dahi papdi") ->
                getPapdiChaatRecipe()
            lower.contains("aloo chaat") || lower.contains("aloo tikki chaat") ->
                getAlooChaatRecipe()
            lower.contains("samosa chaat") ->
                getSamosaChaatRecipe()
            lower.contains("fruit chaat") || lower.contains("fruit salad") ->
                getFruitChaatRecipe()
            lower.contains("chaat") ->
                getPapdiChaatRecipe().copy(name = "Indian Chaat Platter")

            // ─── STREET FOOD: Pav Bhaji ──────────────
            lower.contains("pav bhaji") || lower.contains("pav bhaji") ->
                getPavBhajiRecipe()
            lower.contains("bhaji") && !lower.contains("bhajiya") && !lower.contains("pakora") ->
                getPavBhajiRecipe().copy(name = "Bhaji (for Pav Bhaji)")

            // ─── STREET FOOD: Tikki / Cutlet ─────────
            lower.contains("aloo tikki") || lower.contains("aloo ki tikki") || lower.contains("tikki") ->
                getAlooTikkiRecipe()
            lower.contains("ragda pattice") || lower.contains("ragda pattice") ->
                getRagdaPatticeRecipe()
            lower.contains("cutlet") || lower.contains("vegetable cutlet") ->
                getVegetableCutletRecipe()

            // ─── STREET FOOD: Momos ──────────────────
            lower.contains("momo") && lower.contains("veg") || lower.contains("vegetable momo") || lower.contains("steamed momo") ->
                getVegMomosRecipe()
            lower.contains("momo") && lower.contains("paneer") ->
                getVegMomosRecipe().copy(name = "Paneer Momos", ingredients = listOf("Same as Veg Momos but stuffing uses crumbled paneer + vegetables"))
            lower.contains("momo") && lower.contains("fried") || lower.contains("kurkure momo") ->
                getFriedMomosRecipe()
            lower.contains("momo") && lower.contains("chilli") || lower.contains("chilli momo") ->
                getChilliMomosRecipe()
            lower.contains("momo") || lower.contains("मोमो") ->
                getVegMomosRecipe()

            // ─── STREET FOOD: Rolls / Wraps ──────────
            lower.contains("veg roll") || lower.contains("vegetable roll") || lower.contains("kathi roll") && lower.contains("veg") ->
                getVegKathiRollRecipe()
            lower.contains("paneer roll") || lower.contains("paneer kathi roll") ->
                getPaneerKathiRollRecipe()
            lower.contains("egg roll") || lower.contains("kathi roll") && lower.contains("egg") ->
                getEggKathiRollRecipe()
            lower.contains("kathi roll") || lower.contains("roll") && (lower.contains("wrap") || lower.contains("kathi")) ->
                getVegKathiRollRecipe().copy(name = "Kathi Roll (Vegetable Wrap)")

            // ─── STREET FOOD: Other Snacks ───────────
            lower.contains("samosa") ->
                getSamosaRecipe()
            lower.contains("kachori") || lower.contains("khasta kachori") ->
                getKachoriRecipe()
            lower.contains("pakora") || lower.contains("pakoda") || lower.contains("bhajiya") || lower.contains("onion bhaji") ->
                getPakoraRecipe()
            lower.contains("vada pav") || lower.contains("vadapav") || lower.contains("wada pav") ->
                getVadaPavRecipe()
            lower.contains("dabeli") || lower.contains("dabeli") ->
                getDabeliRecipe()
            lower.contains("misal pav") || lower.contains("misal") ->
                getMisalPavRecipe()
            lower.contains("idli") && lower.contains("fry") || lower.contains("fried idli") ->
                getFriedIdliRecipe()
            lower.contains("idli") && lower.contains("chilli") || lower.contains("chilli idli") ->
                getChilliIdliRecipe()
            lower.contains("medu vada") || lower.contains("vada") && !lower.contains("pav") ->
                getMeduVadaRecipe()

            // ─── STREET FOOD: South Indian Snacks ────
            lower.contains("masala dosa") || lower.contains("masala dose") ->
                getMasalaDosaRecipe()
            lower.contains("dosa") && !lower.contains("masala") ->
                getPlainDosaRecipe()
            lower.contains("uttapam") || lower.contains("ootapam") ->
                getUttapamRecipe()
            lower.contains("appam") || lower.contains("hopper") ->
                getAppamRecipe()

            // ─── SNACKS: Farsan / Gujarati ───────────
            lower.contains("dhokla") || lower.contains("khaman") ->
                getDhoklaRecipe()
            lower.contains("khandvi") ->
                getKhandviRecipe()
            lower.contains("muthia") || lower.contains("methi muthia") ->
                getMuthiaRecipe()
            lower.contains("fafda") || lower.contains("jalebi fafda") ->
                getFafdaRecipe()

            // ─── SNACKS: North Indian ────────────────
            lower.contains("mathri") || lower.contains("mathi") || lower.contains("namak para") ->
                getMathriRecipe()
            lower.contains("bhujia") || lower.contains("aloo bhujia") ->
                getBhujiaRecipe()

            // ─── DESSERT SNACKS ──────────────────────
            lower.contains("jalebi") || lower.contains("jilebi") ->
                getJalebiRecipe()
            lower.contains("gulab jamun") || lower.contains("gulab jamoon") ->
                getGulabJamunRecipe()
            lower.contains("rasgulla") || lower.contains("rosogolla") ->
                getRasgullaRecipe()
            lower.contains("kulfi") || lower.contains("matka kulfi") ->
                getKulfiRecipe()
            lower.contains("ice cream") && lower.contains("kulfi") ->
                getKulfiRecipe()
            lower.contains("rabri") || lower.contains("rabdi") ->
                getRabriRecipe()
            lower.contains("malpua") ->
                getMalpuaRecipe()

            // ─── DEFAULT ─────────────────────────────
            else -> getGenericSnackGuidance(dishName)
        }
    }

    // ──────────────────────────────────────────────
    // TEA VARIANTS RECIPES
    // ──────────────────────────────────────────────

    private fun getMasalaChaiRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Masala Chai (Indian Spiced Tea)",
        category = "Beverage",
        subCategory = "Tea",
        cuisine = "Indian (Pan-India)",
        preparationTime = 2,
        cookingTime = 10,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Hot",
        ingredients = listOf(
            "2 cups Water",
            "1 cup Full-fat Milk",
            "2 tsp Tea Leaves (Assam CTC or Darjeeling)",
            "2-3 Green Cardamoms (Elaichi), crushed",
            "1-inch Fresh Ginger, grated or sliced",
            "1 small Cinnamon stick (Dalchini)",
            "2-3 Cloves (Laung)",
            "2-4 Black Peppercorns (Kalimirch)",
            "1-2 tsp Sugar (adjust to taste)",
            "Optional: 1 star anise, 1 fennel seed pinch"
        ),
        steps = listOf(
            "STEP 1 — BOIL SPICES: In a saucepan, add 2 cups water. Add crushed cardamom, cinnamon, cloves, peppercorns, and grated ginger. Bring to a rolling boil.",
            "STEP 2 — SIMMER SPICES: Reduce heat and let the spices simmer for 2-3 minutes until the water is fragrant and slightly reduced.",
            "STEP 3 — ADD TEA: Add tea leaves. Boil for 1-2 minutes on medium heat. Longer boiling = stronger, more astringent tea.",
            "STEP 4 — ADD MILK: Pour in milk. Bring to a rolling boil again. Watch carefully — it can overflow quickly!",
            "STEP 5 — SIMMER: Reduce heat and simmer for 2-3 minutes until the chai reaches a rich, deep brown colour.",
            "STEP 6 — SWEETEN: Add sugar to taste. Stir until dissolved.",
            "STEP 7 — STRAIN & SERVE: Strain through a tea strainer into cups. Pour from height to create the signature froth (this aerates the chai).",
            "SERVE: Hot, in small cups (cutting chai style). Perfect with biscuits, namkeen, or samosa."
        ),
        proTips = "PRO TIPS: 🔸 The secret to perfect chai is the 'boil and simmer' technique — let the spices release their oils in water first, then add milk. 🔸 Assam CTC tea gives the strongest, most robust chai. Darjeeling is more delicate. 🔸 For 'cutting chai' (half-cup), use equal parts water and milk. 🔸 Crush spices just before using for maximum flavour. 🔸 Never boil milk alone with tea for too long — it can turn bitter. 🔸 A pinch of salt enhances the sweetness without adding sugar.",
        nutritionalInfo = "Approx: 60 cal/cup (with milk & 1 tsp sugar), 2g protein, 3g fat, 6g carbs",
        healthTags = listOf("Energizing", "Digestive", "Comfort", "Antioxidant"),
        smartApplianceMode = "KETTLE",
        smartApplianceParams = mapOf("mode" to "BOIL", "temperatureC" to 100, "durationMin" to 10)
    )

    private fun getGingerTeaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Ginger Tea (Adrak Chai)",
        category = "Beverage",
        subCategory = "Tea",
        cuisine = "Indian (Pan-India)",
        preparationTime = 2,
        cookingTime = 8,
        difficulty = "Easy",
        spiceLevel = "Mild-Medium",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Hot",
        ingredients = listOf(
            "2 cups Water",
            "1 cup Milk",
            "2 tsp Tea Leaves",
            "1.5-inch Fresh Ginger, crushed or thinly sliced",
            "Sugar to taste"
        ),
        steps = listOf(
            "STEP 1: Crush ginger with a mortar and pestle or grate finely. The more surface area, the stronger the ginger kick.",
            "STEP 2: Boil water with crushed ginger for 3-4 minutes until the water is aromatic and slightly yellowish.",
            "STEP 3: Add tea leaves. Boil for 1 minute.",
            "STEP 4: Add milk. Bring to a boil. Reduce heat and simmer for 2 minutes.",
            "STEP 5: Add sugar, stir, strain, and serve hot.",
            "SERVE: Hot, preferably in a clay cup (kulhad) for an earthy aroma."
        ),
        proTips = "PRO TIPS: 🔸 The ginger should be fresh and juicy — dry ginger won't give the same kick. 🔸 For extra immunity boost, add a pinch of turmeric and black pepper. 🔸 Ginger tea is excellent for cold, cough, and sore throat. 🔸 For a stronger ginger flavour, add ginger at the very end and let it steep without boiling.",
        nutritionalInfo = "Approx: 50 cal/cup, 2g protein, 2g fat, 5g carbs",
        healthTags = listOf("Immunity", "Digestive", "Energizing", "Therapeutic"),
        smartApplianceMode = "KETTLE",
        smartApplianceParams = mapOf("mode" to "BOIL", "temperatureC" to 100, "durationMin" to 8)
    )

    private fun getTulsiTeaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Tulsi Tea (Holy Basil Tea)",
        category = "Beverage",
        subCategory = "Tea",
        cuisine = "Indian (Ayurvedic)",
        preparationTime = 2,
        cookingTime = 8,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Hot",
        ingredients = listOf(
            "2 cups Water",
            "10-12 Fresh Tulsi (Holy Basil) leaves",
            "1 tsp Tea Leaves (optional)",
            "1-inch Ginger, sliced",
            "1 tsp Honey or Sugar (optional)",
            "1 tsp Lemon juice (optional)"
        ),
        steps = listOf(
            "STEP 1: Wash tulsi leaves thoroughly. Gently crush them between your palms to release oils.",
            "STEP 2: Boil water with crushed tulsi leaves and ginger slices for 3-4 minutes.",
            "STEP 3: (Optional) Add tea leaves and boil for 1 more minute.",
            "STEP 4: Strain into cups. Add honey and lemon if desired.",
            "SERVE: Hot, preferably in a clear glass to appreciate the golden colour."
        ),
        proTips = "PRO TIPS: 🔸 Tulsi is considered sacred in Hinduism — it has immense medicinal value. 🔸 Fresh tulsi leaves are far superior to dried. 🔸 Tulsi tea is excellent for respiratory health, stress relief, and immunity. 🔸 Do not boil tulsi for too long — it loses its delicate flavour. 🔸 Honey should be added after the tea cools slightly (hot honey can be toxic).",
        nutritionalInfo = "Approx: 15 cal/cup (without sugar), 0g protein, 0g fat, 3g carbs",
        healthTags = listOf("Immunity", "Stress Relief", "Respiratory", "Ayurvedic", "Therapeutic"),
        smartApplianceMode = "KETTLE",
        smartApplianceParams = mapOf("mode" to "BOIL", "temperatureC" to 95, "durationMin" to 8)
    )

    private fun getGreenTeaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Green Tea (Hari Chai)",
        category = "Beverage",
        subCategory = "Tea",
        cuisine = "Indian / Asian",
        preparationTime = 1,
        cookingTime = 5,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 1,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Hot",
        ingredients = listOf(
            "1 cup Water",
            "1 tsp Green Tea Leaves or 1 Green Tea Bag",
            "1 tsp Honey (optional)",
            "1 tsp Lemon juice (optional)",
            "Optional: Mint leaves, ginger slice"
        ),
        steps = listOf(
            "CRITICAL: Green tea should NEVER be boiled. Boiling destroys the delicate flavour and makes it bitter.",
            "STEP 1: Heat water to 80°C (just before boiling — small bubbles at the bottom).",
            "STEP 2: Pour hot water over green tea leaves/bag in a cup.",
            "STEP 3: Let steep for exactly 2-3 minutes. Longer steeping = bitter tea.",
            "STEP 4: Remove tea bag or strain leaves.",
            "STEP 5: Add honey and lemon if desired.",
            "SERVE: Hot, in a ceramic cup. Sip slowly and mindfully."
        ),
        proTips = "PRO TIPS: 🔸 Water temperature is CRITICAL — 80°C is ideal. If you don't have a thermometer, let boiling water sit for 1 minute. 🔸 Never use boiling water directly on green tea. 🔸 Steep for exactly 2-3 minutes — oversteeping causes bitterness. 🔸 Add a slice of ginger or a sprig of mint for variety. 🔸 Green tea is rich in antioxidants (catechins) and supports metabolism.",
        nutritionalInfo = "Approx: 2 cal/cup (without honey), 0g protein, 0g fat, 0g carbs",
        healthTags = listOf("Antioxidant", "Metabolism", "Weight Management", "Energizing"),
        smartApplianceMode = "KETTLE",
        smartApplianceParams = mapOf("mode" to "WARM", "temperatureC" to 80, "durationMin" to 5)
    )

    private fun getKashmiriChaiRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Kashmiri Chai (Noon Chai / Pink Tea)",
        category = "Beverage",
        subCategory = "Tea",
        cuisine = "Kashmiri",
        preparationTime = 5,
        cookingTime = 30,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 4,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Hot",
        ingredients = listOf(
            "4 cups Water",
            "2 tsp Kashmiri Green Tea Leaves (Gunpowder Tea)",
            "½ tsp Baking Soda (critical for pink colour)",
            "½ cup Cold Milk",
            "2 tbsp Sugar (or to taste)",
            "2-3 Green Cardamoms, crushed",
            "1-inch Cinnamon stick",
            "10-12 Almonds, sliced (for garnish)",
            "2 tbsp Chopped Pistachios (for garnish)",
            "1 tbsp Dried Rose Petals (optional)"
        ),
        steps = listOf(
            "STEP 1 — BOIL: In a saucepan, add 4 cups water and bring to a boil.",
            "STEP 2 — ADD TEA: Add Kashmiri green tea leaves. Boil for 5 minutes on medium heat.",
            "STEP 3 — BAKING SODA: Add baking soda. The tea will immediately foam and turn a deep red/brown colour. This is normal.",
            "STEP 4 — SIMMER: Reduce heat and simmer for 15-20 minutes. The tea will reduce and darken.",
            "STEP 5 — BEAT: Using a ladle, beat the tea vigorously for 2-3 minutes. This aerates it and helps develop the colour.",
            "STEP 6 — ADD MILK: In a separate pan, warm the milk. Add the warm milk to the tea. Stir gently.",
            "STEP 7 — PINK MAGIC: The tea will slowly turn a beautiful salmon-pink colour. Add crushed cardamom and cinnamon.",
            "STEP 8 — SWEETEN: Add sugar and stir until dissolved.",
            "STEP 9 — GARNISH: Pour into cups. Top with sliced almonds, pistachios, and rose petals.",
            "SERVE: Hot, in traditional Kashmiri samovar or small cups. Best enjoyed with Kashmiri bread (Kulcha) or bakarkhani."
        ),
        proTips = "PRO TIPS: 🔸 The baking soda is the secret to the pink colour — it reacts with the tea to create the signature hue. 🔸 Beating the tea vigorously is essential for the colour development. 🔸 Kashmiri chai is traditionally made in a samovar (traditional kettle). 🔸 The longer you simmer, the richer the colour. 🔸 For authentic taste, use Kashmiri gunpowder green tea — no substitute works as well.",
        nutritionalInfo = "Approx: 80 cal/cup, 2g protein, 3g fat, 12g carbs",
        healthTags = listOf("Comfort", "Energizing", "Traditional"),
        smartApplianceMode = "KETTLE",
        smartApplianceParams = mapOf("mode" to "BOIL", "temperatureC" to 100, "durationMin" to 30)
    )

    // ─── COFFEE VARIANTS ──────────────────────────

    private fun getFilterCoffeeRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "South Indian Filter Coffee (Kaapi)",
        category = "Beverage",
        subCategory = "Coffee",
        cuisine = "South Indian",
        preparationTime = 5,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Hot",
        ingredients = listOf(
            "2 tbsp South Indian Filter Coffee Powder (70% coffee + 30% chicory)",
            "1½ cups Boiling Water",
            "1½ cups Full-fat Milk, boiled",
            "2-3 tsp Sugar (adjust to taste)",
            "Traditional stainless steel filter (dabara) and tumbler"
        ),
        steps = listOf(
            "PREP: You need a traditional South Indian coffee filter (stainless steel, 2-tier).",
            "STEP 1 — LOAD FILTER: Place the filter on a flat surface. Add coffee powder to the upper chamber. Level it gently — do not press down.",
            "STEP 2 — POUR WATER: Pour boiling water over the coffee powder until the upper chamber is full. Place the plunger on top and press gently to seal.",
            "STEP 3 — BREW: Let the coffee drip into the lower chamber. This takes 10-15 minutes. The decoction will be thick and dark.",
            "STEP 4 — BOIL MILK: While the coffee brews, boil milk in a separate pan. The milk should be hot and frothy.",
            "STEP 5 — ASSEMBLE: In a warm tumbler (steel cup), add 2-3 tbsp of coffee decoction (adjust to taste).",
            "STEP 6 — ADD MILK: Pour hot milk into the tumbler, holding it high to create froth. Add sugar.",
            "STEP 7 — THE POUR: The signature move — pour the coffee back and forth between the tumbler and dabara (small bowl) to aerate and create the perfect froth.",
            "SERVE: Hot, in the traditional tumbler-dabara set. The froth on top should be thick and creamy."
        ),
        proTips = "PRO TIPS: 🔸 The coffee-chicory blend is essential — pure coffee doesn't give the same flavour. 🔸 The decoction should be thick, almost syrupy. 🔸 The 'pour' between tumbler and dabara is not just for show — it aerates the coffee and creates the perfect temperature. 🔸 Use full-fat milk for the creamiest kaapi. 🔸 The traditional stainless steel vessels enhance the taste — avoid ceramic cups for this.",
        nutritionalInfo = "Approx: 80 cal/cup, 3g protein, 4g fat, 8g carbs",
        healthTags = listOf("Energizing", "Traditional", "Comfort"),
        smartApplianceMode = "COFFEE_MAKER",
        smartApplianceParams = mapOf("mode" to "BREW", "strength" to "STRONG", "durationMin" to 15)
    )

    private fun getColdBrewCoffeeRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Cold Brew Coffee",
        category = "Beverage",
        subCategory = "Coffee",
        cuisine = "International / Indian",
        preparationTime = 5,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 4,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "½ cup Coarsely Ground Coffee Beans",
            "2 cups Cold / Room Temperature Water",
            "Ice cubes",
            "Milk or Cream (optional)",
            "Sugar or Honey (optional)",
            "A large jar or French press",
            "Cheesecloth or fine mesh strainer"
        ),
        steps = listOf(
            "STEP 1 — COMBINE: In a large jar, add coarsely ground coffee and cold water. Stir gently to ensure all grounds are wet.",
            "STEP 2 — STEEP: Cover the jar. Let it steep at room temperature for 12-24 hours. Longer steeping = stronger brew.",
            "STEP 3 — FILTER: Line a strainer with cheesecloth or use a fine mesh strainer. Pour the steeped coffee through to filter out grounds.",
            "STEP 4 — STORE: Transfer the cold brew concentrate to a clean bottle. Refrigerate for up to 2 weeks.",
            "STEP 5 — SERVE: Fill a glass with ice. Add 1 part cold brew concentrate + 1 part water or milk. Sweeten to taste.",
            "SERVE: Over ice, with or without milk. Smooth, low-acid, and naturally sweet."
        ),
        proTips = "PRO TIPS: 🔸 Coarse grind is essential — fine grounds will make the brew cloudy and bitter. 🔸 Cold brew is naturally less acidic and smoother than hot-brewed coffee. 🔸 The concentrate is strong — dilute with equal parts water or milk. 🔸 For flavoured cold brew, add cinnamon stick or vanilla bean during steeping. 🔸 Cold brew can be stored in the refrigerator for up to 2 weeks.",
        nutritionalInfo = "Approx: 5 cal/serving (black), 0g protein, 0g fat, 0g carbs",
        healthTags = listOf("Energizing", "Low Acid", "Refreshing"),
        smartApplianceMode = "COFFEE_MAKER",
        smartApplianceParams = mapOf("mode" to "COLD_BREW", "durationMin" to 720)
    )

    private fun getColdCoffeeRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Cold Coffee (Iced Coffee / Frappe)",
        category = "Beverage",
        subCategory = "Coffee",
        cuisine = "Indian (Café Style)",
        preparationTime = 5,
        cookingTime = 5,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "2 tsp Instant Coffee (Nescafe)",
            "2 tbsp Sugar (adjust to taste)",
            "2 tbsp Hot Water (for dissolving)",
            "1½ cups Cold Milk",
            "2-3 Ice cubes",
            "2 tbsp Fresh Cream (optional, for richness)",
            "1 tsp Vanilla Extract (optional)",
            "Chocolate syrup for garnish (optional)",
            "Whipped cream for topping (optional)"
        ),
        steps = listOf(
            "STEP 1 — COFFEE FOAM: In a small bowl, mix instant coffee + sugar + 2 tbsp hot water. Whisk vigorously until the mixture becomes thick, pale, and frothy (3-4 minutes).",
            "STEP 2 — BLEND: In a blender, add cold milk, ice cubes, coffee foam, and vanilla extract (if using). Blend on high speed for 1-2 minutes until frothy.",
            "STEP 3 — SERVE: Pour into tall glasses. Top with whipped cream and drizzle chocolate syrup.",
            "SERVE: Chilled, with a straw. Perfect summer refreshment."
        ),
        proTips = "PRO TIPS: 🔸 The coffee-sugar foam (whipped coffee) is the key to a creamy cold coffee. 🔸 Use a hand whisk or electric mixer for the foam — the thicker the foam, the better the drink. 🔸 For a café-style frappe, add 2 tbsp vanilla ice cream while blending. 🔸 Drizzle chocolate syrup inside the glass before pouring for a professional look. 🔸 For a healthier version, use cold almond milk and skip the cream.",
        nutritionalInfo = "Approx: 150 cal/glass, 5g protein, 6g fat, 20g carbs",
        healthTags = listOf("Energizing", "Refreshing", "Indulgent"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "BLEND", "durationMin" to 5)
    )

    private fun getInstantCoffeeRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Instant Coffee (Nescafe Style)",
        category = "Beverage",
        subCategory = "Coffee",
        cuisine = "Indian (Quick)",
        preparationTime = 1,
        cookingTime = 3,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 1,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Hot",
        ingredients = listOf(
            "1 tsp Instant Coffee (Nescafe / Bru)",
            "1 tsp Sugar (adjust to taste)",
            "1 tbsp Hot Water (for frothing)",
            "1 cup Hot Milk",
            "Optional: a pinch of cinnamon or cardamom powder"
        ),
        steps = listOf(
            "STEP 1 — FROTH: In a cup, add instant coffee + sugar + 1 tbsp hot water. Stir vigorously until the mixture becomes a thick, frothy paste.",
            "STEP 2 — ADD MILK: Pour hot milk into the cup. Stir well.",
            "STEP 3 — GARNISH: Sprinkle a pinch of cinnamon or cardamom on top.",
            "SERVE: Hot, in a ceramic mug. Perfect for a quick morning boost."
        ),
        proTips = "PRO TIPS: 🔸 The 'froth' step is crucial — it dissolves the coffee properly and creates a creamy texture. 🔸 For a stronger coffee, use 1.5 tsp instead of 1 tsp. 🔸 Add a pinch of salt to reduce bitterness. 🔸 For a café-style touch, microwave the milk and froth it before adding.",
        nutritionalInfo = "Approx: 60 cal/cup, 3g protein, 3g fat, 6g carbs",
        healthTags = listOf("Energizing", "Quick"),
        smartApplianceMode = "KETTLE",
        smartApplianceParams = mapOf("mode" to "BOIL", "temperatureC" to 100, "durationMin" to 3)
    )

    // ─── OTHER BEVERAGES ──────────────────────────

    private fun getSweetLassiRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Sweet Lassi (Punjabi Sweet Yogurt Drink)",
        category = "Beverage",
        subCategory = "Lassi",
        cuisine = "North Indian (Punjabi)",
        preparationTime = 5,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "2 cups Thick Fresh Curd (Yogurt), chilled",
            "4 tbsp Sugar (adjust to taste)",
            "½ cup Cold Milk",
            "½ tsp Cardamom powder",
            "2-3 Ice cubes",
            "2 tbsp Chopped Pistachios + Almonds for garnish",
            "Few saffron strands (optional)",
            "1 tbsp Rose water (optional)"
        ),
        steps = listOf(
            "STEP 1: Add chilled curd, sugar, cold milk, cardamom powder, and ice cubes to a blender.",
            "STEP 2: Blend on high speed for 1-2 minutes until smooth, creamy, and frothy.",
            "STEP 3: Pour into tall glasses.",
            "STEP 4: Garnish with chopped nuts, saffron strands, and a pinch of cardamom.",
            "SERVE: Chilled. For a thicker lassi, add 2 tbsp fresh cream before blending."
        ),
        proTips = "PRO TIPS: 🔸 Use thick, fresh curd — sour curd will ruin the lassi. 🔸 Blend until the lassi is creamy and has a thick froth on top. 🔸 For mango lassi, add ½ cup mango pulp. 🔸 For strawberry lassi, add ½ cup fresh strawberries. 🔸 Serve in tall glasses with a dollop of malai (cream) on top for the authentic dhaba experience.",
        nutritionalInfo = "Approx: 180 cal/glass, 6g protein, 6g fat, 26g carbs",
        healthTags = listOf("Refreshing", "Digestive", "Cooling", "Probiotic"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "BLEND", "durationMin" to 2)
    )

    private fun getMangoLassiRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Mango Lassi",
        category = "Beverage",
        subCategory = "Lassi",
        cuisine = "North Indian",
        preparationTime = 5,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Sweet",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "1 cup Thick Curd (Yogurt), chilled",
            "1 cup Mango Pulp (fresh or canned Alphonso)",
            "3 tbsp Sugar (adjust to taste)",
            "½ cup Cold Milk",
            "2-3 Ice cubes",
            "¼ tsp Cardamom powder",
            "Chopped pistachios for garnish"
        ),
        steps = listOf(
            "STEP 1: Add curd, mango pulp, sugar, cold milk, cardamom, and ice cubes to a blender.",
            "STEP 2: Blend on high speed for 1-2 minutes until smooth and creamy.",
            "STEP 3: Pour into glasses. Garnish with chopped pistachios.",
            "SERVE: Chilled. Best made with Alphonso mangoes for the richest flavour."
        ),
        proTips = "PRO TIPS: 🔸 Alphonso mango pulp gives the best flavour and colour. 🔸 For a vegan version, use coconut yogurt and almond milk. 🔸 Add a pinch of saffron for a royal touch. 🔸 The lassi should be thick enough to coat the back of a spoon.",
        nutritionalInfo = "Approx: 200 cal/glass, 5g protein, 5g fat, 36g carbs",
        healthTags = listOf("Refreshing", "Energizing", "Indulgent"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "BLEND", "durationMin" to 2)
    )

    private fun getSaltedLassiRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Salted Lassi (Namkin Lassi)",
        category = "Beverage",
        subCategory = "Lassi",
        cuisine = "North Indian (Punjabi / Haryanvi)",
        preparationTime = 5,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "2 cups Thick Curd (Yogurt), chilled",
            "½ cup Cold Water",
            "½ tsp Roasted Cumin Powder (Bhuna Jeera)",
            "¼ tsp Black Salt (Kala Namak)",
            "¼ tsp Regular Salt (or to taste)",
            "2-3 Ice cubes",
            "Fresh Coriander or Mint leaves for garnish",
            "Optional: a pinch of red chilli powder"
        ),
        steps = listOf(
            "STEP 1: Add chilled curd, cold water, roasted cumin powder, black salt, regular salt, and ice cubes to a blender.",
            "STEP 2: Blend on high speed for 1 minute until smooth and frothy.",
            "STEP 3: Pour into glasses. Garnish with a sprinkle of cumin powder and fresh coriander/mint.",
            "SERVE: Chilled. This is the perfect digestive drink after a heavy meal."
        ),
        proTips = "PRO TIPS: 🔸 Roast cumin seeds on a dry pan until fragrant, then grind — this makes a huge difference. 🔸 Black salt (kala namak) adds a unique tangy flavour. 🔸 For a Haryanvi twist, add finely chopped mint and green chilli. 🔸 Salted lassi is traditionally served in a matka (earthen pot) for an earthy flavour.",
        nutritionalInfo = "Approx: 100 cal/glass, 5g protein, 4g fat, 8g carbs",
        healthTags = listOf("Digestive", "Cooling", "Probiotic", "Refreshing"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "BLEND", "durationMin" to 1)
    )

    private fun getButtermilkRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Chaas / Buttermilk (Spiced Yogurt Drink)",
        category = "Beverage",
        subCategory = "Chaas",
        cuisine = "Indian (Pan-India)",
        preparationTime = 5,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "1 cup Thick Curd (Yogurt)",
            "1½ cups Cold Water",
            "½ tsp Roasted Cumin Powder",
            "¼ tsp Black Salt (Kala Namak)",
            "¼ tsp Regular Salt",
            "2-3 Fresh Curry Leaves, crushed",
            "1 Green Chilli, finely chopped (optional)",
            "1 tbsp Fresh Coriander, finely chopped",
            "A pinch of Asafoetida (Hing)"
        ),
        steps = listOf(
            "STEP 1: In a bowl, whisk curd until smooth. Add cold water and whisk again until well combined.",
            "STEP 2: Add roasted cumin powder, black salt, regular salt, crushed curry leaves, hing, and green chilli. Mix well.",
            "STEP 3: Pour into glasses. Garnish with fresh coriander.",
            "SERVE: Chilled. Chaas is the ultimate digestive drink, especially after a spicy meal."
        ),
        proTips = "PRO TIPS: 🔸 Chaas should be thinner than lassi — it's meant to be a refreshing, light drink. 🔸 Whisking by hand with a traditional madhani (wooden churner) gives the best texture. 🔸 For Gujarati chaas, add a pinch of sugar and grated ginger. 🔸 Always use fresh curd — sour curd makes bitter chaas. 🔸 A tempering of mustard seeds + curry leaves in ghee poured on top takes it to another level.",
        nutritionalInfo = "Approx: 60 cal/glass, 3g protein, 2g fat, 6g carbs",
        healthTags = listOf("Digestive", "Cooling", "Probiotic", "Hydrating"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "BLEND", "durationMin" to 1)
    )

    private fun getJaljeeraRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Jaljeera (Spiced Cumin Cooler)",
        category = "Beverage",
        subCategory = "Summer Drink",
        cuisine = "North Indian",
        preparationTime = 5,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "2 cups Cold Water",
            "2 tbsp Jaljeera Powder (readymade or homemade)",
            "1 tbsp Lemon Juice",
            "1 tsp Roasted Cumin Powder",
            "½ tsp Black Salt (Kala Namak)",
            "½ tsp Chaat Masala",
            "1 tsp Mint Leaves, crushed",
            "2-3 Ice cubes",
            "Fresh Mint leaves for garnish"
        ),
        steps = listOf(
            "HOMEMADE JALJEERA POWDER: Dry roast 2 tbsp cumin seeds + 1 tbsp coriander seeds + 1 tsp black peppercorns + 2 dry red chillies + 1 tsp amchur + 1 tsp black salt. Grind to powder.",
            "STEP 1: In a glass, add jaljeera powder, lemon juice, roasted cumin powder, black salt, chaat masala, and crushed mint leaves.",
            "STEP 2: Add cold water and stir well until all powders dissolve.",
            "STEP 3: Add ice cubes. Garnish with fresh mint leaves.",
            "SERVE: Chilled. The perfect summer drink that beats the heat and aids digestion."
        ),
        proTips = "PRO TIPS: 🔸 Jaljeera is a natural digestive aid and appetite stimulant. 🔸 The combination of cumin, mint, and black salt creates a unique tangy-spicy flavour. 🔸 For extra flavour, add a pinch of ginger powder and a splash of raw mango pulp. 🔸 Serve immediately — the flavours are best when fresh. 🔸 Jaljeera is also served as a welcome drink at Indian weddings.",
        nutritionalInfo = "Approx: 20 cal/glass, 0g protein, 0g fat, 4g carbs",
        healthTags = listOf("Digestive", "Cooling", "Hydrating", "Appetizer"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "MIX", "durationMin" to 2)
    )

    private fun getSharbatRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Rooh Afza Sharbat (Herbal Summer Drink)",
        category = "Beverage",
        subCategory = "Sharbat",
        cuisine = "Indian (Mughlai / Hyderabadi)",
        preparationTime = 2,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Sweet",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "2 cups Cold Milk or Water",
            "3-4 tbsp Rooh Afza Syrup",
            "2-3 Ice cubes",
            "1 tbsp Basil Seeds (Sabja / Tukmaria), soaked",
            "Few rose petals for garnish",
            "Chopped nuts (optional)"
        ),
        steps = listOf(
            "PREP: Soak basil seeds in water for 15 minutes — they will swell up.",
            "STEP 1: In a glass, add Rooh Afza syrup.",
            "STEP 2: Add cold milk (or water for a lighter version). Stir well.",
            "STEP 3: Add soaked basil seeds and ice cubes. Stir gently.",
            "STEP 4: Garnish with rose petals and chopped nuts.",
            "SERVE: Chilled. The iconic pink summer drink of India."
        ),
        proTips = "PRO TIPS: 🔸 Rooh Afza is a concentrated herbal syrup made from fruits, flowers, and herbs. 🔸 For a lighter version, use water instead of milk. 🔸 Basil seeds (sabja) are extremely healthy — they aid digestion and cool the body. 🔸 The drink should be a beautiful rose-pink colour. 🔸 For a special touch, add a scoop of vanilla ice cream.",
        nutritionalInfo = "Approx: 120 cal/glass (with milk), 3g protein, 3g fat, 22g carbs",
        healthTags = listOf("Cooling", "Hydrating", "Refreshing", "Herbal"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "MIX", "durationMin" to 2)
    )

    private fun getNimbuPaniRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Nimbu Pani (Indian Lemonade / Shikanji)",
        category = "Beverage",
        subCategory = "Summer Drink",
        cuisine = "Indian (Pan-India)",
        preparationTime = 3,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "2 cups Cold Water",
            "2-3 Lemons, juiced",
            "3-4 tsp Sugar (adjust to taste)",
            "½ tsp Black Salt (Kala Namak)",
            "½ tsp Roasted Cumin Powder",
            "¼ tsp Regular Salt",
            "2-3 Ice cubes",
            "Fresh Mint leaves",
            "Optional: a pinch of chaat masala, grated ginger"
        ),
        steps = listOf(
            "STEP 1: In a glass, add lemon juice, sugar, black salt, roasted cumin powder, and regular salt.",
            "STEP 2: Add a little water and stir until sugar dissolves completely.",
            "STEP 3: Add remaining water and ice cubes. Stir well.",
            "STEP 4: Garnish with fresh mint leaves and a lemon slice.",
            "SERVE: Chilled. The quintessential Indian summer drink."
        ),
        proTips = "PRO TIPS: 🔸 Always dissolve sugar in a small amount of water first — it mixes better. 🔸 Black salt and roasted cumin are what make this 'Indian' lemonade. 🔸 For a spicy version, add a pinch of red chilli powder and grated ginger. 🔸 For nimbu paani with soda, replace water with chilled soda water. 🔸 Serve in a glass bottle for the authentic roadside feel.",
        nutritionalInfo = "Approx: 40 cal/glass, 0g protein, 0g fat, 10g carbs",
        healthTags = listOf("Hydrating", "Refreshing", "Digestive", "Vitamin C"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "MIX", "durationMin" to 2)
    )

    private fun getAamPannaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Aam Panna (Raw Mango Drink)",
        category = "Beverage",
        subCategory = "Summer Drink",
        cuisine = "North Indian",
        preparationTime = 10,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Sweet-Sour",
        servings = 4,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "2 large Raw Green Mangoes (Kacchi Kairi)",
            "4 cups Water",
            "½ cup Sugar (adjust to taste)",
            "1 tsp Roasted Cumin Powder",
            "½ tsp Black Salt (Kala Namak)",
            "½ tsp Regular Salt",
            "¼ tsp Red Chilli Powder (optional)",
            "Fresh Mint leaves for garnish",
            "Ice cubes"
        ),
        steps = listOf(
            "STEP 1 — BOIL MANGOES: Wash raw mangoes. Pressure cook or boil in water until soft (15-20 minutes). The skin will crack.",
            "STEP 2 — EXTRACT PULP: Let mangoes cool. Peel off skin. Extract the pulp, discarding the seed. Mash the pulp well.",
            "STEP 3 — BLEND: In a blender, add mango pulp, sugar, roasted cumin, black salt, regular salt, red chilli powder, and 2 cups water. Blend until smooth.",
            "STEP 4 — DILUTE: Add remaining water and blend again. Adjust sweetness and salt to taste.",
            "STEP 5 — CHILL: Refrigerate for at least 1 hour before serving.",
            "SERVE: Over ice, garnished with fresh mint leaves. The perfect summer cooler."
        ),
        proTips = "PRO TIPS: 🔸 Use raw, green mangoes — they should be sour and firm. 🔸 Boiling the mangoes whole retains more flavour than peeling and cutting. 🔸 Aam panna is a natural heatstroke preventer — it's a traditional summer drink for a reason. 🔸 The drink should be sweet, sour, and spicy all at once. 🔸 For a quick version, use raw mango pulp from a jar.",
        nutritionalInfo = "Approx: 80 cal/glass, 0g protein, 0g fat, 20g carbs",
        healthTags = listOf("Cooling", "Hydrating", "Vitamin C", "Heatstroke Prevention"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "BOIL", "temperatureC" to 100, "durationMin" to 15)
    )

    private fun getBelSharbatRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Bel Sharbat (Wood Apple Drink)",
        category = "Beverage",
        subCategory = "Summer Drink",
        cuisine = "Indian (Pan-India)",
        preparationTime = 10,
        cookingTime = 0,
        difficulty = "Medium",
        spiceLevel = "Sweet-Sour",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "2 ripe Wood Apples (Bael / Bel)",
            "2 cups Cold Milk or Water",
            "3-4 tbsp Sugar (adjust to taste)",
            "½ tsp Cardamom powder",
            "Ice cubes",
            "Optional: a pinch of saffron"
        ),
        steps = listOf(
            "STEP 1 — CRACK: Wood apples have a hard shell. Crack open with a heavy knife or hammer.",
            "STEP 2 — EXTRACT: Scoop out the sticky, fibrous pulp. Remove seeds and any hard fibres.",
            "STEP 3 — BLEND: Add pulp, milk/water, sugar, cardamom, and ice to a blender. Blend until smooth.",
            "STEP 4 — STRAIN: Strain through a sieve to remove any remaining fibres.",
            "SERVE: Chilled. Bel sharbat is a traditional digestive and cooling drink."
        ),
        proTips = "PRO TIPS: 🔸 Wood apple is known as 'bel' in Hindi — it's a sacred fruit offered to Lord Shiva. 🔸 The fruit has immense digestive and medicinal properties. 🔸 The pulp is naturally sticky and fibrous — straining is essential. 🔸 For a richer version, use milk instead of water. 🔸 Bel sharbat is traditionally consumed during the summer months for its cooling effect.",
        nutritionalInfo = "Approx: 100 cal/glass, 2g protein, 2g fat, 22g carbs",
        healthTags = listOf("Digestive", "Cooling", "Therapeutic", "Traditional"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "BLEND", "durationMin" to 3)
    )

    private fun getCoconutWaterRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Tender Coconut Water (Nariyal Pani)",
        category = "Beverage",
        subCategory = "Natural Drink",
        cuisine = "Indian (Pan-India)",
        preparationTime = 2,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 1,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "1 Tender Green Coconut",
            "Ice cubes (optional)",
            "Lemon wedge (optional)"
        ),
        steps = listOf(
            "STEP 1 — CHILL: Refrigerate the tender coconut for 2 hours before serving.",
            "STEP 2 — OPEN: Using a large knife, slice off the top of the coconut to create an opening.",
            "STEP 3 — SERVE: Serve in the coconut itself with a straw. Add ice if desired.",
            "STEP 4 — SCOOP: After drinking the water, scoop out the soft coconut malai (flesh) with a spoon.",
            "SERVE: Chilled, in the coconut. The ultimate natural electrolyte drink."
        ),
        proTips = "PRO TIPS: 🔸 Tender green coconut water is nature's perfect electrolyte drink — rich in potassium, magnesium, and calcium. 🔸 Always choose heavy coconuts with more water. 🔸 The water should be clear and sweet — if it's pink or sour, the coconut is spoiled. 🔸 Drink immediately after opening — exposure to air reduces nutritional value. 🔸 The soft malai (flesh) is equally nutritious — don't waste it!",
        nutritionalInfo = "Approx: 45 cal/coconut, 0g protein, 0g fat, 9g carbs",
        healthTags = listOf("Hydrating", "Electrolyte", "Natural", "Cooling", "Detox"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "NONE", "durationMin" to 0)
    )

    private fun getMangoMilkshakeRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Mango Milkshake (Aam Milkshake)",
        category = "Beverage",
        subCategory = "Milkshake",
        cuisine = "Indian",
        preparationTime = 5,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Sweet",
        servings = 2,
        isBeverage = true,
        isStreetFood = false,
        isSnack = false,
        temperature = "Cold",
        ingredients = listOf(
            "2 cups Mango Pulp (Alphonso preferred)",
            "2 cups Cold Milk",
            "3 tbsp Sugar (adjust to taste)",
            "2 scoops Vanilla Ice Cream (optional)",
            "Ice cubes",
            "Chopped nuts for garnish"
        ),
        steps = listOf(
            "STEP 1: Add mango pulp, cold milk, sugar, and ice cubes to a blender.",
            "STEP 2: Blend on high speed for 1-2 minutes until smooth and creamy.",
            "STEP 3: Add vanilla ice cream (if using) and blend for 10 seconds.",
            "STEP 4: Pour into tall glasses. Garnish with chopped nuts.",
            "SERVE: Chilled, with a straw. The ultimate summer indulgence."
        ),
        proTips = "PRO TIPS: 🔸 Alphonso mangoes make the best milkshake — their natural sweetness and creaminess are unmatched. 🔸 For a thicker shake, use frozen mango chunks instead of ice. 🔸 Add a scoop of mango ice cream for extra richness. 🔸 For a dairy-free version, use coconut milk or almond milk.",
        nutritionalInfo = "Approx: 220 cal/glass, 6g protein, 6g fat, 38g carbs",
        healthTags = listOf("Energizing", "Indulgent", "Refreshing"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "BLEND", "durationMin" to 2)
    )

    // ──────────────────────────────────────────────
    // STREET FOOD: Pani Puri / Golgappa
    // ──────────────────────────────────────────────

    private fun getPaniPuriRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Pani Puri / Golgappa / Phuchka",
        category = "StreetFood",
        subCategory = "Chaat",
        cuisine = "Indian (Pan-India)",
        preparationTime = 30,
        cookingTime = 15,
        difficulty = "Hard",
        spiceLevel = "Medium-Hot",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Cold",
        ingredients = listOf(
            "PURI (Puffed Shells):",
            "1 cup Semolina (Suji / Rava)",
            "¼ cup All-Purpose Flour (Maida)",
            "Water for kneading",
            "Oil for deep frying",
            "PANIPURI PANI (Spiced Water):",
            "2 cups Cold Water",
            "½ cup Fresh Mint Leaves, tightly packed",
            "½ cup Fresh Coriander Leaves",
            "2-3 Green Chillies",
            "1-inch Ginger",
            "2 tbsp Tamarind Pulp",
            "1 tsp Roasted Cumin Powder",
            "1 tsp Chaat Masala",
            "½ tsp Black Salt (Kala Namak)",
            "1 tsp Regular Salt",
            "1 tbsp Sugar",
            "STUFFING (Masala):",
            "2 medium Potatoes, boiled and mashed",
            "½ cup Boiled Chickpeas (Kabuli Chana)",
            "1 small Onion, finely chopped",
            "1 tsp Chaat Masala",
            "½ tsp Red Chilli Powder",
            "Salt to taste",
            "2 tbsp Fresh Coriander, chopped",
            "MEETHI CHUTNEY (Sweet Tamarind Chutney):",
            "½ cup Tamarind Pulp",
            "¼ cup Dates (Khajoor), deseeded",
            "¼ cup Jaggery (Gur) or Sugar",
            "½ tsp Roasted Cumin Powder",
            "½ tsp Fennel Powder",
            "Pinch of Black Salt"
        ),
        steps = listOf(
            "PURI PREPARATION:",
            "STEP 1: Mix semolina + flour + salt. Add water gradually and knead into a stiff dough. Cover and rest for 20 minutes.",
            "STEP 2: Roll the dough very thin (almost paper-thin). Cut into small circles (2-inch diameter).",
            "STEP 3: Heat oil for deep frying. Fry the puris on medium heat — press gently with a slotted spoon to help them puff.",
            "STEP 4: Fry until golden and crisp. Drain on paper towel. Store in an airtight container.",
            "",
            "PANIPURI PANI (The Soul):",
            "STEP 5: In a blender, add mint, coriander, green chillies, ginger, and ½ cup water. Blend to a smooth paste.",
            "STEP 6: Transfer to a large bowl. Add remaining water, tamarind pulp, roasted cumin, chaat masala, black salt, regular salt, and sugar. Stir well.",
            "STEP 7: Refrigerate for at least 1 hour. The pani should be ice-cold when served.",
            "",
            "MEETHI CHUTNEY:",
            "STEP 8: Soak tamarind and dates in warm water for 30 minutes. Blend to a smooth paste.",
            "STEP 9: Cook the paste with jaggery, cumin, fennel, and black salt until it thickens. Cool.",
            "",
            "STUFFING:",
            "STEP 10: Mix mashed potatoes + chickpeas + onion + chaat masala + red chilli + salt + coriander.",
            "",
            "ASSEMBLY:",
            "STEP 11: Make a small hole in each puri. Fill with a little stuffing. Add a teaspoon of meethi chutney.",
            "STEP 12: Dip the filled puri into the spicy pani and eat immediately — whole, in one bite!",
            "SERVE: Immediately — the puri should be crispy and the pani cold. The perfect balance of spicy, tangy, sweet, and crunchy."
        ),
        proTips = "PRO TIPS: 🔸 The pani is the soul of this dish — the balance of mint, coriander, tamarind, and spices is critical. 🔸 The puris must be paper-thin and fried on medium heat to puff properly. 🔸 Always serve the pani ice-cold — it enhances the experience. 🔸 For a spicier version, add more green chillies and a pinch of red chilli powder to the pani. 🔸 The meethi chutney should be sweet-tangy — it balances the spicy pani. 🔸 Eat immediately after filling — soggy puris ruin the experience!",
        nutritionalInfo = "Approx: 250 cal/serving (6-8 pieces), 5g protein, 8g fat, 40g carbs",
        healthTags = listOf("Street Food", "Indulgent", "Social Food"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 180, "durationMin" to 15)
    )

    private fun getDahiPuriRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Dahi Puri (Dahi Batata Puri)",
        category = "StreetFood",
        subCategory = "Chaat",
        cuisine = "Indian (Mumbai / Pan-India)",
        preparationTime = 20,
        cookingTime = 5,
        difficulty = "Easy",
        spiceLevel = "Mild-Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Cold",
        ingredients = listOf(
            "20-24 Small Puris (readymade or homemade)",
            "2 cups Thick Curd (Yogurt), whisked and chilled",
            "2 medium Potatoes, boiled and mashed",
            "½ cup Boiled Chickpeas",
            "½ cup Meethi Chutney (Sweet Tamarind Chutney)",
            "½ cup Green Chutney (Mint-Coriander)",
            "1 tsp Chaat Masala",
            "1 tsp Roasted Cumin Powder",
            "½ tsp Red Chilli Powder",
            "½ tsp Black Salt",
            "¼ cup Sev (thin gram flour noodles)",
            "2 tbsp Fresh Coriander, chopped",
            "2 tbsp Pomegranate seeds (optional)"
        ),
        steps = listOf(
            "STEP 1 — PREP: Arrange puris on a serving plate. Gently crack the top of each puri to create an opening.",
            "STEP 2 — POTATO LAYER: Place a small amount of mashed potato and chickpeas in each puri.",
            "STEP 3 — CHUTNEYS: Add a few drops of meethi chutney and green chutney into each puri.",
            "STEP 4 — DAHI: Generously pour whisked chilled curd over all the puris, covering them completely.",
            "STEP 5 — SPICES: Sprinkle chaat masala, roasted cumin powder, red chilli powder, and black salt on top.",
            "STEP 6 — GARNISH: Top with sev, fresh coriander, and pomegranate seeds.",
            "SERVE: Immediately. The puris should be slightly soft from the curd but still retain some crunch."
        ),
        proTips = "PRO TIPS: 🔸 The curd should be thick, fresh, and well-whisked — thin or sour curd will ruin the dish. 🔸 Serve immediately after adding curd — the puris soften quickly. 🔸 For a richer version, add a dollop of sweetened curd or cream. 🔸 The combination of sweet, tangy, and spicy chutneys is what makes this dish special.",
        nutritionalInfo = "Approx: 200 cal/serving, 6g protein, 6g fat, 32g carbs",
        healthTags = listOf("Street Food", "Indulgent", "Probiotic"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "ASSEMBLE", "durationMin" to 5)
    )

    private fun getSevPuriRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Sev Puri (Crispy Snack with Chutneys)",
        category = "StreetFood",
        subCategory = "Chaat",
        cuisine = "Indian (Mumbai)",
        preparationTime = 15,
        cookingTime = 5,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Room Temperature",
        ingredients = listOf(
            "20-24 Small Puris (flat, round)",
            "2 medium Potatoes, boiled and cubed",
            "1 small Onion, finely chopped",
            "½ cup Meethi Chutney (Sweet Tamarind Chutney)",
            "½ cup Green Chutney (Mint-Coriander)",
            "1 tsp Chaat Masala",
            "½ tsp Red Chilli Powder",
            "½ tsp Roasted Cumin Powder",
            "¼ cup Sev (thin gram flour noodles)",
            "2 tbsp Fresh Coriander, chopped",
            "Lemon wedges for serving"
        ),
        steps = listOf(
            "STEP 1 — ARRANGE: Place puris on a serving plate, flat side up.",
            "STEP 2 — POTATO: Place a few cubes of boiled potato on each puri.",
            "STEP 3 — ONION: Sprinkle finely chopped onion over the potatoes.",
            "STEP 4 — CHUTNEYS: Drizzle green chutney and meethi chutney over each puri.",
            "STEP 5 — SPICES: Sprinkle chaat masala, red chilli powder, and roasted cumin powder.",
            "STEP 6 — SEV: Generously top with sev. Garnish with fresh coriander.",
            "SERVE: Immediately, with lemon wedges. Each puri should be eaten in one bite for the full experience."
        ),
        proTips = "PRO TIPS: 🔸 Unlike dahi puri, sev puri is dry (no curd) — the crunch is the star. 🔸 Use flat puris specifically made for sev puri (not the round puffed ones). 🔸 The chutneys should be drizzled, not poured — too much liquid makes the puris soggy. 🔸 Sev should be added just before serving to maintain its crunch.",
        nutritionalInfo = "Approx: 180 cal/serving, 4g protein, 6g fat, 28g carbs",
        healthTags = listOf("Street Food", "Crunchy", "Indulgent"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "ASSEMBLE", "durationMin" to 5)
    )

    // ─── STREET FOOD: Chaat ───────────────────────

    private fun getBhelPuriRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Bhel Puri (Mumbai's Iconic Street Snack)",
        category = "StreetFood",
        subCategory = "Chaat",
        cuisine = "Indian (Mumbai / Pan-India)",
        preparationTime = 15,
        cookingTime = 5,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Room Temperature",
        ingredients = listOf(
            "2 cups Puffed Rice (Murmura / Kurmura)",
            "1 cup Sev (thin gram flour noodles)",
            "1 medium Onion, finely chopped",
            "1 medium Tomato, finely chopped",
            "2 medium Potatoes, boiled and cubed",
            "½ cup Meethi Chutney (Sweet Tamarind Chutney)",
            "¼ cup Green Chutney (Mint-Coriander)",
            "1 tsp Chaat Masala",
            "1 tsp Roasted Cumin Powder",
            "½ tsp Red Chilli Powder",
            "½ tsp Black Salt",
            "2 tbsp Fresh Coriander, chopped",
            "2 tbsp Lemon Juice",
            "Handful of Papdi (crushed)",
            "Optional: ¼ cup chopped raw mango"
        ),
        steps = listOf(
            "STEP 1 — PREP: In a large mixing bowl, add puffed rice, sev, crushed papdi, chopped onion, tomato, and boiled potato.",
            "STEP 2 — CHUTNEYS: Add meethi chutney and green chutney. Toss gently to combine.",
            "STEP 3 — SPICES: Sprinkle chaat masala, roasted cumin, red chilli powder, black salt, and lemon juice. Toss again.",
            "STEP 4 — GARNISH: Top with extra sev and fresh coriander.",
            "SERVE: Immediately in paper cones (traditional) or bowls. The bhel should be crunchy, tangy, and spicy."
        ),
        proTips = "PRO TIPS: 🔸 The key to perfect bhel is the balance of sweet, tangy, and spicy — adjust chutneys to taste. 🔸 Puffed rice should be fresh and crispy — stale puffed rice ruins the texture. 🔸 Serve immediately — bhel becomes soggy within minutes. 🔸 For a healthier version, add more vegetables like cucumber and bell pepper. 🔸 Mumbai's Juhu Beach bhel is legendary — the secret is the combination of fresh chutneys and the seaside air!",
        nutritionalInfo = "Approx: 180 cal/serving, 4g protein, 6g fat, 30g carbs",
        healthTags = listOf("Street Food", "Crunchy", "Indulgent"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "MIX", "durationMin" to 5)
    )

    private fun getPapdiChaatRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Papdi Chaat (Crispy Discs with Yogurt & Chutneys)",
        category = "StreetFood",
        subCategory = "Chaat",
        cuisine = "Indian (North Indian / Pan-India)",
        preparationTime = 20,
        cookingTime = 10,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Cold",
        ingredients = listOf(
            "12-15 Papdi (fried crispy discs, readymade or homemade)",
            "2 cups Thick Curd (Yogurt), whisked and chilled",
            "2 medium Potatoes, boiled and mashed",
            "½ cup Boiled Chickpeas",
            "½ cup Meethi Chutney (Sweet Tamarind Chutney)",
            "½ cup Green Chutney (Mint-Coriander)",
            "1 tsp Chaat Masala",
            "1 tsp Roasted Cumin Powder",
            "½ tsp Red Chilli Powder",
            "½ tsp Black Salt",
            "¼ cup Sev",
            "2 tbsp Fresh Coriander, chopped",
            "2 tbsp Pomegranate seeds"
        ),
        steps = listOf(
            "HOMEMADE PAPDI: Mix 1 cup maida + 2 tbsp ghee + water + salt. Knead stiff dough. Roll thin, cut into circles, prick with fork, and deep fry until golden.",
            "STEP 1 — ARRANGE: Place papdi on a serving plate, 3-4 per person.",
            "STEP 2 — BASE: Spread a layer of mashed potato and chickpeas on each papdi.",
            "STEP 3 — DAHI: Pour whisked chilled curd generously over the papdi.",
            "STEP 4 — CHUTNEYS: Drizzle meethi chutney and green chutney.",
            "STEP 5 — SPICES: Sprinkle chaat masala, roasted cumin, red chilli, and black salt.",
            "STEP 6 — GARNISH: Top with sev, fresh coriander, and pomegranate seeds.",
            "SERVE: Immediately. The papdi should be crunchy on the bottom, soft on top."
        ),
        proTips = "PRO TIPS: 🔸 The papdi must be crispy — if they absorb too much moisture, the chaat becomes a mess. 🔸 Serve on individual plates, not a shared platter, to maintain crunch. 🔸 The curd should be sweetened slightly for the perfect balance. 🔸 For a restaurant-style presentation, drizzle green chutney in a zigzag pattern.",
        nutritionalInfo = "Approx: 220 cal/serving, 6g protein, 8g fat, 34g carbs",
        healthTags = listOf("Street Food", "Indulgent", "Crunchy"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 180, "durationMin" to 10)
    )

    private fun getAlooChaatRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Aloo Chaat (Spicy Potato Snack)",
        category = "StreetFood",
        subCategory = "Chaat",
        cuisine = "Indian (North Indian)",
        preparationTime = 10,
        cookingTime = 15,
        difficulty = "Easy",
        spiceLevel = "Medium-Hot",
        servings = 3,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "4 medium Potatoes, boiled and cubed",
            "2 tbsp Oil",
            "1 tsp Cumin seeds",
            "1 tsp Ginger, grated",
            "2 Green Chillies, chopped",
            "½ tsp Turmeric powder",
            "1 tsp Red Chilli powder",
            "1 tsp Chaat Masala",
            "½ tsp Black Salt",
            "1 tbsp Lemon Juice",
            "2 tbsp Fresh Coriander, chopped",
            "2 tbsp Meethi Chutney (optional)",
            "2 tbsp Curd (optional)"
        ),
        steps = listOf(
            "STEP 1 — SHALLOW FRY: Heat oil in a pan. Add cumin seeds. Let them crackle.",
            "STEP 2: Add grated ginger and green chillies. Sauté for 30 seconds.",
            "STEP 3: Add boiled potato cubes. Sauté on medium-high heat for 5-6 minutes until the potatoes are golden and crispy on the edges.",
            "STEP 4 — SPICES: Add turmeric, red chilli powder, chaat masala, black salt. Toss well to coat.",
            "STEP 5 — FINISH: Add lemon juice and fresh coriander. Toss and remove from heat.",
            "STEP 6 — SERVE: Transfer to a plate. Drizzle with meethi chutney and curd if desired.",
            "SERVE: Hot, as a snack or side dish. Perfect with evening chai."
        ),
        proTips = "PRO TIPS: 🔸 The potatoes should be crispy on the outside and soft inside — high heat is key. 🔸 Don't overcrowd the pan — the potatoes need space to crisp up. 🔸 For extra crunch, add a handful of sev on top before serving. 🔸 Aloo chaat is also delicious with a squeeze of lemon and a sprinkle of fresh pomegranate.",
        nutritionalInfo = "Approx: 180 cal/serving, 3g protein, 6g fat, 30g carbs",
        healthTags = listOf("Street Food", "Comfort Food", "Spicy"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SHALLOW_FRY", "temperatureC" to 200, "durationMin" to 15)
    )

    private fun getSamosaChaatRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Samosa Chaat (Crushed Samosa with Chutneys)",
        category = "StreetFood",
        subCategory = "Chaat",
        cuisine = "Indian (North Indian)",
        preparationTime = 5,
        cookingTime = 5,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 2,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "4 readymade Samosas, crushed",
            "1 cup Curd (Yogurt), whisked",
            "½ cup Meethi Chutney",
            "¼ cup Green Chutney",
            "1 tsp Chaat Masala",
            "½ tsp Red Chilli Powder",
            "½ tsp Roasted Cumin Powder",
            "¼ cup Sev",
            "2 tbsp Fresh Coriander, chopped",
            "2 tbsp Onion, finely chopped"
        ),
        steps = listOf(
            "STEP 1 — CRUSH: Crush the samosas into bite-sized pieces on a plate.",
            "STEP 2 — TOP: Pour whisked curd over the crushed samosas.",
            "STEP 3 — CHUTNEYS: Drizzle meethi chutney and green chutney.",
            "STEP 4 — SPICES: Sprinkle chaat masala, red chilli powder, and roasted cumin.",
            "STEP 5 — GARNISH: Top with sev, chopped onion, and fresh coriander.",
            "SERVE: Immediately. The combination of crispy samosa, cool curd, and tangy chutneys is irresistible."
        ),
        proTips = "PRO TIPS: 🔸 Use fresh, crispy samosas — stale samosas will be soggy. 🔸 The samosas should be crushed just before serving to maintain crunch. 🔸 For extra flavour, add a spoonful of chole (chickpea curry) under the samosa. 🔸 This is the ultimate 'leftover samosa' makeover!",
        nutritionalInfo = "Approx: 280 cal/serving, 6g protein, 12g fat, 36g carbs",
        healthTags = listOf("Street Food", "Indulgent", "Comfort Food"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "ASSEMBLE", "durationMin" to 5)
    )

    private fun getFruitChaatRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Fruit Chaat (Spiced Fruit Salad)",
        category = "StreetFood",
        subCategory = "Chaat",
        cuisine = "Indian (Pan-India)",
        preparationTime = 15,
        cookingTime = 0,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Cold",
        ingredients = listOf(
            "1 cup Apple, cubed",
            "1 cup Banana, sliced",
            "1 cup Papaya, cubed",
            "1 cup Pomegranate seeds",
            "1 cup Orange segments, deseeded",
            "1 cup Grapes, halved",
            "1 tsp Chaat Masala",
            "½ tsp Black Salt",
            "½ tsp Roasted Cumin Powder",
            "½ tsp Red Chilli Powder (optional)",
            "1 tbsp Lemon Juice",
            "1 tbsp Fresh Mint, chopped",
            "Optional: 1 tbsp honey, ¼ cup chopped nuts"
        ),
        steps = listOf(
            "STEP 1 — PREP: Wash and cut all fruits into bite-sized pieces.",
            "STEP 2 — MIX: In a large bowl, combine all fruits.",
            "STEP 3 — SEASON: Add chaat masala, black salt, roasted cumin, red chilli powder, and lemon juice. Toss gently.",
            "STEP 4 — CHILL: Refrigerate for 15 minutes to let the flavours meld.",
            "STEP 5 — GARNISH: Top with fresh mint and chopped nuts before serving.",
            "SERVE: Chilled, as a refreshing snack or dessert."
        ),
        proTips = "PRO TIPS: 🔸 Use a mix of sweet and tangy fruits for the best flavour. 🔸 Add fruits just before serving to prevent them from releasing water. 🔸 For a savoury version, add a pinch of salt and skip the honey. 🔸 Fruit chaat is a healthy alternative to fried snacks — perfect for evening hunger pangs.",
        nutritionalInfo = "Approx: 120 cal/serving, 2g protein, 1g fat, 28g carbs",
        healthTags = listOf("Healthy", "Refreshing", "Vitamin Rich", "Light"),
        smartApplianceMode = "MANUAL",
        smartApplianceParams = mapOf("mode" to "MIX", "durationMin" to 2)
    )

    // ─── STREET FOOD: Pav Bhaji ───────────────────

    private fun getPavBhajiRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Pav Bhaji (Mumbai's Iconic Street Food)",
        category = "StreetFood",
        subCategory = "Pav Bhaji",
        cuisine = "Indian (Mumbai / Maharashtrian)",
        preparationTime = 20,
        cookingTime = 30,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "BHAJI (Vegetable Mash):",
            "3 medium Potatoes, boiled and mashed",
            "1 cup Cauliflower florets, finely chopped",
            "1 cup Green Peas",
            "1 cup Capsicum (Bell Pepper), finely chopped",
            "2 large Onions, finely chopped",
            "3 large Tomatoes, pureed",
            "2 tbsp Pav Bhaji Masala (readymade or homemade)",
            "1 tsp Red Chilli Powder",
            "½ tsp Turmeric Powder",
            "1 tsp Ginger-Garlic paste",
            "2 tbsp Butter",
            "1 tbsp Oil",
            "Salt to taste",
            "1 tbsp Lemon Juice",
            "2 tbsp Fresh Coriander, chopped",
            "PAV (Bread Buns):",
            "8 Pav Buns (soft dinner rolls)",
            "2 tbsp Butter for toasting",
            "GARNISH:",
            "1 large Onion, finely chopped",
            "1 Lemon, cut into wedges",
            "Fresh Coriander sprigs"
        ),
        steps = listOf(
            "BHAJI PREPARATION:",
            "STEP 1 — BOIL VEGETABLES: Pressure cook potatoes, cauliflower, and peas until soft. Mash coarsely.",
            "STEP 2 — SAUTÉ: Heat 1 tbsp oil + 1 tbsp butter in a large pan. Add half the chopped onion. Sauté until golden brown.",
            "STEP 3 — AROMATICS: Add ginger-garlic paste. Sauté for 1 minute.",
            "STEP 4 — TOMATO: Add tomato puree. Cook until oil separates (5-6 minutes).",
            "STEP 5 — SPICES: Add pav bhaji masala, red chilli powder, turmeric. Cook for 1 minute.",
            "STEP 6 — VEGETABLES: Add the mashed vegetables + capsicum + salt + ½ cup water. Mix well.",
            "STEP 7 — SIMMER: Let the bhaji simmer on low heat for 15-20 minutes, stirring occasionally. Mash the vegetables with a potato masher while cooking.",
            "STEP 8 — FINISH: Add remaining butter + lemon juice + fresh coriander. Stir well. The bhaji should be thick and creamy.",
            "",
            "PAV (BUNS):",
            "STEP 9 — TOAST: Slice pav buns horizontally. Heat a tawa with butter. Toast the buns until golden and crispy on both sides.",
            "",
            "SERVE: Serve hot bhaji in a bowl, topped with a pat of butter. Place toasted pav on the side. Garnish with chopped onion, lemon wedges, and coriander. The traditional way: dip the pav in bhaji and eat!"
        ),
        proTips = "PRO TIPS: 🔸 The secret to Mumbai-style pav bhaji is mashing the vegetables well — the bhaji should be almost smooth. 🔸 Generous butter is non-negotiable — it's what makes pav bhaji indulgent. 🔸 Pav bhaji masala is the key spice blend — use a good quality brand or make your own. 🔸 The bhaji tastes better the next day — the flavours deepen overnight. 🔸 For the iconic 'butter pat' on top, add a cube of butter just before serving so it melts slowly.",
        nutritionalInfo = "Approx: 350 cal/serving (2 pav + bhaji), 8g protein, 14g fat, 48g carbs",
        healthTags = listOf("Street Food", "Comfort Food", "Indulgent"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SIMMER", "temperatureC" to 160, "durationMin" to 30)
    )

    // ─── STREET FOOD: Tikki / Cutlet ──────────────

    private fun getAlooTikkiRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Aloo Tikki (Crispy Potato Cutlet)",
        category = "StreetFood",
        subCategory = "Tikki",
        cuisine = "Indian (North Indian)",
        preparationTime = 20,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "4 medium Potatoes, boiled and mashed",
            "2 tbsp Cornflour (or breadcrumbs)",
            "1 tsp Ginger, grated",
            "2 Green Chillies, finely chopped",
            "1 tsp Cumin Powder",
            "½ tsp Garam Masala",
            "½ tsp Red Chilli Powder",
            "1 tsp Chaat Masala",
            "1 tbsp Fresh Coriander, chopped",
            "Salt to taste",
            "Oil for shallow frying",
            "SERVING:",
            "Green Chutney",
            "Meethi Chutney",
            "Curd (Yogurt)",
            "Chaat Masala for sprinkling"
        ),
        steps = listOf(
            "STEP 1 — MIX: In a bowl, combine mashed potatoes, cornflour, ginger, green chillies, cumin powder, garam masala, red chilli powder, chaat masala, coriander, and salt. Mix well.",
            "STEP 2 — SHAPE: Divide the mixture into equal portions. Shape into round, flat patties (tikkis).",
            "STEP 3 — CHILL: Refrigerate the tikkis for 15 minutes — this helps them hold shape while frying.",
            "STEP 4 — FRY: Heat oil on a tawa (griddle). Shallow fry tikkis on medium heat until golden brown and crispy on both sides (4-5 minutes per side).",
            "STEP 5 — DRAIN: Remove and drain on paper towel.",
            "SERVE: Hot, topped with curd, green chutney, meethi chutney, and a sprinkle of chaat masala. Serve with chopped onion and coriander."
        ),
        proTips = "PRO TIPS: 🔸 The potatoes should be completely dry — excess moisture makes the tikkis fall apart. 🔸 Cornflour acts as a binder — adjust quantity if the mixture is too wet. 🔸 Chilling the tikkis before frying is essential for shape retention. 🔸 For extra crispy tikkis, roll in breadcrumbs before frying. 🔸 Aloo tikki is the base for many chaat dishes — aloo tikki chaat, ragda pattice, etc.",
        nutritionalInfo = "Approx: 180 cal/serving (2 tikkis), 3g protein, 6g fat, 30g carbs",
        healthTags = listOf("Street Food", "Crunchy", "Comfort Food"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SHALLOW_FRY", "temperatureC" to 180, "durationMin" to 15)
    )

    private fun getRagdaPatticeRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Ragda Pattice (Aloo Tikki with White Pea Curry)",
        category = "StreetFood",
        subCategory = "Tikki",
        cuisine = "Indian (Mumbai / Gujarati)",
        preparationTime = 30,
        cookingTime = 30,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "PATTICE (TIKKI): Same as Aloo Tikki recipe above",
            "RAGDA (WHITE PEA CURRY):",
            "1 cup Dried White Peas (Safed Vatana), soaked overnight",
            "1 tbsp Oil",
            "1 tsp Cumin seeds",
            "1 tsp Ginger-Garlic paste",
            "1 large Onion, finely chopped",
            "2 Tomatoes, pureed",
            "1 tsp Red Chilli Powder",
            "½ tsp Turmeric Powder",
            "1 tsp Coriander Powder",
            "1 tsp Chaat Masala",
            "Salt to taste",
            "Fresh Coriander for garnish",
            "TOPPINGS:",
            "Curd, Green Chutney, Meethi Chutney, Sev, Chopped Onion"
        ),
        steps = listOf(
            "RAGDA:",
            "STEP 1: Pressure cook soaked white peas with salt and turmeric until soft (5-6 whistles).",
            "STEP 2: Heat oil. Add cumin seeds. Let them crackle.",
            "STEP 3: Add onion. Sauté until golden. Add ginger-garlic paste. Sauté for 1 minute.",
            "STEP 4: Add tomato puree. Cook until oil separates.",
            "STEP 5: Add red chilli powder, coriander powder, chaat masala. Cook for 30 seconds.",
            "STEP 6: Add cooked white peas + ½ cup water. Simmer for 10 minutes until thick. Mash some peas for thickness.",
            "",
            "ASSEMBLY:",
            "STEP 7: Prepare aloo tikkis as per Aloo Tikki recipe. Fry until golden.",
            "STEP 8: Place 2 tikkis on a plate. Pour hot ragda over them.",
            "STEP 9: Top with curd, green chutney, meethi chutney, sev, chopped onion, and coriander.",
            "SERVE: Hot. The combination of crispy tikki and spicy-sweet ragda is legendary."
        ),
        proTips = "PRO TIPS: 🔸 White peas must be soaked overnight — they won't cook properly otherwise. 🔸 The ragda should be thick, not watery — it should coat the tikkis. 🔸 For the authentic Mumbai street food experience, serve with extra sev and a sprinkle of chaat masala. 🔸 This dish is also known as 'Ragda Pattice' and is a staple of Gujarati street food.",
        nutritionalInfo = "Approx: 320 cal/serving, 10g protein, 10g fat, 48g carbs",
        healthTags = listOf("Street Food", "Indulgent", "Protein Rich"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SIMMER", "temperatureC" to 160, "durationMin" to 30)
    )

    private fun getVegetableCutletRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Vegetable Cutlet (Crispy Veggie Patties)",
        category = "Snack",
        subCategory = "Cutlet",
        cuisine = "Indian (Pan-India)",
        preparationTime = 20,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 4,
        isBeverage = false,
        isStreetFood = false,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "3 medium Potatoes, boiled and mashed",
            "1 cup Mixed Vegetables (carrot, beans, peas), finely chopped and boiled",
            "½ cup Breadcrumbs",
            "1 tsp Ginger, grated",
            "2 Green Chillies, finely chopped",
            "1 tsp Garam Masala",
            "½ tsp Red Chilli Powder",
            "1 tsp Chaat Masala",
            "2 tbsp Fresh Coriander, chopped",
            "Salt to taste",
            "Oil for shallow frying",
            "COATING:",
            "½ cup Cornflour slurry (cornflour + water)",
            "1 cup Breadcrumbs for coating"
        ),
        steps = listOf(
            "STEP 1 — MIX: In a bowl, combine mashed potatoes, boiled vegetables, breadcrumbs, ginger, green chillies, garam masala, red chilli powder, chaat masala, coriander, and salt. Mix well.",
            "STEP 2 — SHAPE: Divide into equal portions. Shape into round or oval patties (cutlet shape).",
            "STEP 3 — COAT: Dip each cutlet in cornflour slurry, then roll in breadcrumbs. Press gently to adhere.",
            "STEP 4 — CHILL: Refrigerate for 15 minutes.",
            "STEP 5 — FRY: Shallow fry on medium heat until golden brown and crispy on both sides (4-5 minutes per side).",
            "SERVE: Hot with tomato ketchup and green chutney. Perfect tea-time snack."
        ),
        proTips = "PRO TIPS: 🔸 The double coating (slurry + breadcrumbs) gives the cutlets their signature crunch. 🔸 Vegetables should be well-drained — excess moisture makes cutlets soggy. 🔸 For a healthier version, air fry at 180°C for 12 minutes. 🔸 Add grated cheese to the centre for a 'cheese burst' cutlet.",
        nutritionalInfo = "Approx: 200 cal/serving (2 cutlets), 5g protein, 6g fat, 32g carbs",
        healthTags = listOf("Snack", "Crunchy", "Tea-Time"),
        smartApplianceMode = "AIR_FRYER",
        smartApplianceParams = mapOf("mode" to "AIR_FRY", "temperatureC" to 180, "durationMin" to 12)
    )

    // ─── STREET FOOD: Momos ───────────────────────

    private fun getVegMomosRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Veg Momos (Steamed Dumplings)",
        category = "StreetFood",
        subCategory = "Momo",
        cuisine = "Indian (Tibetan / Nepali / North-East Indian)",
        preparationTime = 30,
        cookingTime = 15,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "DOUGH (WRAPPER):",
            "2 cups All-Purpose Flour (Maida)",
            "½ tsp Salt",
            "1 tsp Oil",
            "Water for kneading",
            "STUFFING:",
            "2 cups Cabbage, finely shredded",
            "1 cup Carrot, grated",
            "½ cup Spring Onions, chopped (white + green parts)",
            "½ cup Capsicum, finely chopped",
            "1 tbsp Ginger-Garlic paste",
            "2 Green Chillies, finely chopped",
            "1 tbsp Soya Sauce",
            "1 tsp Vinegar",
            "½ tsp Black Pepper",
            "1 tsp Oil",
            "Salt to taste",
            "DIPPING:",
            "Soya Sauce + Vinegar + Chilli Oil"
        ),
        steps = listOf(
            "DOUGH:",
            "STEP 1: Mix flour + salt + oil. Add water gradually and knead into a smooth, soft dough. Cover and rest for 20 minutes.",
            "",
            "STUFFING:",
            "STEP 2: Heat 1 tsp oil in a pan. Add ginger-garlic paste and green chillies. Sauté for 30 seconds.",
            "STEP 3: Add cabbage, carrot, capsicum. Sauté on high heat for 2-3 minutes. The vegetables should remain crunchy, not mushy.",
            "STEP 4: Add spring onions, soya sauce, vinegar, black pepper, salt. Mix well. Cook for 1 minute. Let the stuffing cool completely.",
            "",
            "SHAPING MOMOS:",
            "STEP 5: Divide dough into small balls. Roll each into a thin circle (3-inch diameter). The edges should be thinner than the centre.",
            "STEP 6: Place a spoonful of stuffing in the centre. Fold the edges to create pleats, bringing them together at the top. Twist to seal.",
            "STEP 7: Place each momo on a greased surface or parchment paper.",
            "",
            "STEAMING:",
            "STEP 8: Grease a steamer basket or idli plate. Arrange momos without touching each other.",
            "STEP 9: Steam on medium heat for 10-12 minutes. The momo wrapper should become translucent.",
            "",
            "SERVE: Hot with spicy chilli-garlic chutney or soya sauce-vinegar dip. Garnish with spring onion greens."
        ),
        proTips = "PRO TIPS: 🔸 The dough should be soft and pliable — if it's too stiff, the momos will be hard. 🔸 The stuffing must be completely cool before filling — hot stuffing makes the dough sticky. 🔸 The pleating technique takes practice — start with simple 'half-moon' shapes if pleating is difficult. 🔸 Do not overstuff — the momos will burst while steaming. 🔸 For extra flavour, add 1 tbsp finely chopped mushrooms or crumbled paneer to the stuffing. 🔸 Leftover momos can be pan-fried the next day for 'pan-fried momos'.",
        nutritionalInfo = "Approx: 220 cal/serving (6-8 momos), 6g protein, 4g fat, 40g carbs",
        healthTags = listOf("Street Food", "Steamed", "Healthy-ish"),
        smartApplianceMode = "STEAMER",
        smartApplianceParams = mapOf("mode" to "STEAM", "durationMin" to 12)
    )

    private fun getFriedMomosRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Fried Momos (Kurkure Momos)",
        category = "StreetFood",
        subCategory = "Momo",
        cuisine = "Indian (Fusion)",
        preparationTime = 30,
        cookingTime = 15,
        difficulty = "Hard",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "Same as Veg Momos (dough + stuffing)",
            "Oil for deep frying",
            "1 cup Cornflakes, crushed (for kurkure coating, optional)"
        ),
        steps = listOf(
            "STEP 1: Prepare momos as per Veg Momos recipe (steps 1-7).",
            "STEP 2 — STEAM FIRST: Steam the momos for 8 minutes (slightly undercooked). Let them cool completely.",
            "STEP 3 — COAT (Optional): Roll the steamed momos in crushed cornflakes for extra crunch.",
            "STEP 4 — DEEP FRY: Heat oil on medium heat. Deep fry the momos until golden and crispy (3-4 minutes).",
            "STEP 5 — DRAIN: Drain on paper towel.",
            "SERVE: Hot with spicy mayo or schezwan chutney. The outside should be crispy, inside soft and juicy."
        ),
        proTips = "PRO TIPS: 🔸 Steaming before frying ensures the inside is cooked while the outside gets crispy. 🔸 The cornflake coating is optional but adds an amazing crunch. 🔸 For 'Kurkure Momos', roll in crushed Kurkure (Indian snack) instead of cornflakes. 🔸 Serve immediately — fried momos lose their crunch quickly.",
        nutritionalInfo = "Approx: 280 cal/serving, 5g protein, 12g fat, 38g carbs",
        healthTags = listOf("Street Food", "Indulgent", "Crunchy"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 180, "durationMin" to 15)
    )

    private fun getChilliMomosRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Chilli Momos (Indo-Chinese Style)",
        category = "StreetFood",
        subCategory = "Momo",
        cuisine = "Indian (Indo-Chinese Fusion)",
        preparationTime = 30,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Medium-Hot",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "12-16 Steamed or Fried Momos (from Veg Momos recipe)",
            "SAUCE:",
            "1 tbsp Oil",
            "1 tsp Ginger, finely chopped",
            "1 tsp Garlic, finely chopped",
            "2-3 Green Chillies, slit",
            "1 medium Capsicum, cubed",
            "1 medium Onion, cubed",
            "2 tbsp Soya Sauce",
            "1 tbsp Red Chilli Sauce",
            "1 tbsp Tomato Ketchup",
            "1 tsp Vinegar",
            "½ tsp Sugar",
            "1 tsp Cornflour mixed with 2 tbsp water (slurry)",
            "Spring onions for garnish"
        ),
        steps = listOf(
            "STEP 1: Prepare momos (steamed or fried) as per Veg Momos recipe.",
            "STEP 2 — STIR FRY: Heat oil in a wok on high heat. Add ginger + garlic + green chillies. Sauté for 15 seconds.",
            "STEP 3 — VEGETABLES: Add cubed capsicum and onion. Stir fry on high heat for 1 minute — they should remain crunchy.",
            "STEP 4 — SAUCES: Add soya sauce, red chilli sauce, tomato ketchup, vinegar, sugar. Stir quickly to combine.",
            "STEP 5 — SLURRY: Add cornflour slurry + 2 tbsp water. Stir and let the sauce thicken for 30 seconds.",
            "STEP 6 — MOMOS: Add the momos. Toss on high heat to coat evenly with the sauce. Cook for 1 minute.",
            "SERVE: Garnish with spring onion greens. Serve hot as a starter or snack."
        ),
        proTips = "PRO TIPS: 🔸 High heat is essential for the Indo-Chinese wok-style flavour. 🔸 The sauce should coat the momos, not drown them. 🔸 For a drier version, skip the cornflour slurry. 🔸 For extra heat, add 1 tsp schezwan sauce. 🔸 This dish is hugely popular in Indian street food culture.",
        nutritionalInfo = "Approx: 260 cal/serving, 5g protein, 8g fat, 40g carbs",
        healthTags = listOf("Street Food", "Spicy", "Indo-Chinese"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "STIR_FRY", "temperatureC" to 220, "durationMin" to 10)
    )

    // ─── STREET FOOD: Rolls / Wraps ───────────────

    private fun getVegKathiRollRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Veg Kathi Roll (Vegetable Wrap)",
        category = "StreetFood",
        subCategory = "Roll",
        cuisine = "Indian (Kolkata / North Indian)",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "PARATHA (WRAP):",
            "2 cups Whole Wheat Flour",
            "Water for kneading",
            "Salt to taste",
            "1 tsp Oil",
            "Butter/Ghee for cooking",
            "FILLING:",
            "2 cups Mixed Vegetables (cabbage, carrot, capsicum, beans), julienned",
            "1 large Onion, sliced",
            "1 tbsp Oil",
            "1 tsp Ginger-Garlic paste",
            "1 tsp Soya Sauce",
            "1 tsp Red Chilli Sauce",
            "½ tsp Black Pepper",
            "½ tsp Chaat Masala",
            "Salt to taste",
            "1 tbsp Lemon Juice",
            "SPREADS:",
            "Green Chutney",
            "Mayonnaise (optional)",
            "GARNISH:",
            "1 Onion, sliced into rings",
            "Lemon wedges",
            "Chaat Masala"
        ),
        steps = listOf(
            "PARATHA:",
            "STEP 1: Knead flour + salt + oil + water into a soft dough. Rest for 15 minutes.",
            "STEP 2: Divide into balls. Roll each into a thin circle.",
            "STEP 3: Cook on a hot tawa with butter/ghee until golden spots appear. Keep warm.",
            "",
            "FILLING:",
            "STEP 4: Heat oil in a pan. Add ginger-garlic paste. Sauté for 30 seconds.",
            "STEP 5: Add sliced onion. Sauté until translucent.",
            "STEP 6: Add julienned vegetables. Stir fry on high heat for 2-3 minutes — they should remain crunchy.",
            "STEP 7: Add soya sauce, red chilli sauce, black pepper, chaat masala, salt. Toss well.",
            "STEP 8: Add lemon juice. Remove from heat.",
            "",
            "ASSEMBLY:",
            "STEP 9: Lay a paratha flat. Spread green chutney (and mayonnaise if using).",
            "STEP 10: Place the vegetable filling in the centre. Top with onion rings.",
            "STEP 11: Roll tightly, tucking in one end. Wrap the bottom in parchment paper for easy handling.",
            "SERVE: Hot, with extra chutney on the side. The perfect on-the-go meal."
        ),
        proTips = "PRO TIPS: 🔸 The paratha should be soft and pliable — if it's too crispy, it will crack while rolling. 🔸 The filling should be dry (not watery) to prevent the paratha from getting soggy. 🔸 For a paneer version, add ½ cup crumbled paneer to the filling. 🔸 The traditional Kolkata kathi roll uses a paratha made with maida (all-purpose flour) and is egg-coated.",
        nutritionalInfo = "Approx: 280 cal/roll, 6g protein, 8g fat, 46g carbs",
        healthTags = listOf("Street Food", "On-the-Go", "Filling"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SHALLOW_FRY", "temperatureC" to 180, "durationMin" to 20)
    )

    private fun getPaneerKathiRollRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Paneer Kathi Roll",
        category = "StreetFood",
        subCategory = "Roll",
        cuisine = "Indian (North Indian)",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "Same as Veg Kathi Roll, with:",
            "200g Paneer, cubed or crumbled",
            "1 tsp Tandoori Masala",
            "½ tsp Red Chilli Powder",
            "1 tbsp Curd (for marination)"
        ),
        steps = listOf(
            "STEP 1: Marinate paneer cubes with curd, tandoori masala, red chilli powder, and salt for 15 minutes.",
            "STEP 2: Heat 1 tbsp oil in a pan. Sauté the marinated paneer until golden on all sides (3-4 minutes).",
            "STEP 3: Follow the Veg Kathi Roll recipe, replacing the vegetable filling with paneer filling.",
            "SERVE: Hot, with green chutney and onion rings."
        ),
        proTips = "PRO TIPS: 🔸 Paneer should be marinated for at least 15 minutes for maximum flavour. 🔸 Don't overcook the paneer — it should remain soft. 🔸 Add a squeeze of lemon and chaat masala just before rolling.",
        nutritionalInfo = "Approx: 320 cal/roll, 12g protein, 14g fat, 38g carbs",
        healthTags = listOf("Street Food", "Protein Rich", "Filling"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SHALLOW_FRY", "temperatureC" to 180, "durationMin" to 20)
    )

    private fun getEggKathiRollRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Egg Kathi Roll",
        category = "StreetFood",
        subCategory = "Roll",
        cuisine = "Indian (Kolkata)",
        preparationTime = 15,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "4 Parathas (as per Veg Kathi Roll)",
            "4 Eggs",
            "1 small Onion, finely chopped",
            "2 Green Chillies, chopped",
            "½ tsp Black Pepper",
            "½ tsp Chaat Masala",
            "Salt to taste",
            "Oil/Butter for cooking",
            "Green Chutney",
            "Onion rings for garnish"
        ),
        steps = listOf(
            "STEP 1: Beat eggs with chopped onion, green chillies, black pepper, chaat masala, and salt.",
            "STEP 2: Heat a tawa with a little oil. Pour the egg mixture and spread like a thin omelette.",
            "STEP 3: Place a paratha on top of the semi-cooked egg. Press gently. Cook until the egg is set.",
            "STEP 4: Flip carefully. Cook the other side until golden.",
            "STEP 5: Spread green chutney on the egg-coated paratha. Add onion rings.",
            "STEP 6: Roll tightly and serve hot.",
            "SERVE: Hot, wrapped in parchment paper. The classic Kolkata street food."
        ),
        proTips = "PRO TIPS: 🔸 The egg should be spread thin — it should coat the paratha, not overwhelm it. 🔸 For a 'double egg' roll, use 2 eggs per paratha. 🔸 Add a sprinkle of chaat masala and lemon juice before rolling for extra flavour. 🔸 This is the most popular kathi roll variant in Kolkata.",
        nutritionalInfo = "Approx: 300 cal/roll, 10g protein, 12g fat, 38g carbs",
        healthTags = listOf("Street Food", "Protein Rich", "On-the-Go"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SHALLOW_FRY", "temperatureC" to 180, "durationMin" to 15)
    )

    // ─── STREET FOOD: Other Snacks ────────────────

    private fun getSamosaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Samosa (Crispy Potato-Stuffed Pastry)",
        category = "Snack",
        subCategory = "Fried Snack",
        cuisine = "Indian (North Indian)",
        preparationTime = 30,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Medium",
        servings = 8,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "DOUGH:",
            "2 cups All-Purpose Flour (Maida)",
            "¼ cup Oil/Ghee",
            "½ tsp Ajwain (Carom seeds)",
            "Salt to taste",
            "Water for kneading",
            "STUFFING:",
            "4 medium Potatoes, boiled and mashed",
            "1 tbsp Oil",
            "1 tsp Cumin seeds",
            "1 tsp Ginger, grated",
            "2 Green Chillies, finely chopped",
            "½ cup Green Peas (fresh or frozen)",
            "1 tsp Coriander powder",
            "½ tsp Cumin powder",
            "½ tsp Garam Masala",
            "½ tsp Red Chilli powder",
            "1 tsp Amchur (Dry Mango powder)",
            "2 tbsp Fresh Coriander, chopped",
            "Salt to taste",
            "Oil for deep frying"
        ),
        steps = listOf(
            "DOUGH:",
            "STEP 1: Mix flour + oil/ghee + ajwain + salt. Rub with fingertips until crumbly.",
            "STEP 2: Add water gradually and knead into a stiff dough. Cover and rest for 30 minutes.",
            "STUFFING:",
            "STEP 3: Heat oil. Add cumin seeds. Let them crackle.",
            "STEP 4: Add ginger + green chillies. Sauté for 30 seconds.",
            "STEP 5: Add green peas. Sauté for 1 minute.",
            "STEP 6: Add mashed potatoes + coriander powder + cumin powder + garam masala + red chilli powder + amchur + salt + fresh coriander. Mix well. Let cool.",
            "SHAPING:",
            "STEP 7: Divide dough into small balls. Roll each into a thin oval/circle.",
            "STEP 8: Cut in half. Form each half into a cone shape, sealing the straight edge with water.",
            "STEP 9: Fill the cone with stuffing. Seal the top edge with water. Press gently.",
            "FRYING:",
            "STEP 10: Heat oil on medium heat. Gently slide in samosas.",
            "STEP 11: Fry on low-medium heat for 8-10 minutes, turning occasionally, until golden and crispy.",
            "SERVE: Hot with green chutney (mint-coriander) and tamarind chutney."
        ),
        proTips = "PRO TIPS: 🔸 The dough should be stiff, not soft — soft dough absorbs oil. 🔸 Resting the dough is essential for gluten development. 🔸 Fry on LOW heat — high heat burns the outside while the inside remains raw. 🔸 For extra crispy samosas, add 1 tbsp rice flour to the dough. 🔸 Freeze uncooked samosas for up to 1 month — fry directly from frozen.",
        nutritionalInfo = "Approx: 180 cal/samosa, 4g protein, 8g fat, 24g carbs",
        healthTags = listOf("Snack", "Street Food", "Indulgent"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 170, "durationMin" to 20)
    )

    private fun getKachoriRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Kachori (Crispy Stuffed Pastry)",
        category = "Snack",
        subCategory = "Fried Snack",
        cuisine = "Indian (North Indian / Rajasthani)",
        preparationTime = 30,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Medium",
        servings = 6,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "DOUGH:",
            "2 cups All-Purpose Flour (Maida)",
            "¼ cup Ghee/Oil",
            "Salt to taste",
            "Water for kneading",
            "STUFFING:",
            "1 cup Moong Dal (split yellow gram), soaked 4 hours",
            "1 tbsp Oil",
            "1 tsp Cumin seeds",
            "1 tsp Fennel seeds (Saunf)",
            "1 tsp Ginger, grated",
            "2 Green Chillies, chopped",
            "1 tsp Coriander powder",
            "½ tsp Red Chilli powder",
            "½ tsp Garam Masala",
            "1 tsp Amchur (Dry Mango powder)",
            "½ tsp Asafoetida (Hing)",
            "Salt to taste",
            "Oil for deep frying"
        ),
        steps = listOf(
            "DOUGH:",
            "STEP 1: Mix flour + ghee + salt. Rub until crumbly. Add water and knead into a stiff dough. Rest for 20 minutes.",
            "STUFFING:",
            "STEP 2: Drain soaked moong dal completely. Coarsely grind (do not make a paste).",
            "STEP 3: Heat oil. Add cumin + fennel seeds. Let them crackle.",
            "STEP 4: Add ginger + green chillies. Sauté for 30 seconds.",
            "STEP 5: Add ground dal + coriander powder + red chilli + garam masala + amchur + hing + salt. Cook on low heat for 5-6 minutes until dry. Let cool.",
            "SHAPING & FRYING:",
            "STEP 6: Divide dough into balls. Roll each into a 3-inch circle.",
            "STEP 7: Place stuffing in centre. Bring edges together to seal. Flatten gently.",
            "STEP 8: Heat oil on low-medium heat. Fry kachoris until golden and crispy (8-10 minutes).",
            "SERVE: Hot with green chutney and meethi chutney. Rajasthani version is served with spicy potato curry (aloo ki sabzi)."
        ),
        proTips = "PRO TIPS: 🔸 The dough should be stiffer than samosa dough. 🔸 Moong dal should be coarsely ground, not smooth — the texture is important. 🔸 Fry on low heat — kachoris need slow frying to cook the inside. 🔸 For Khasta Kachori (Rajasthani style), use urad dal instead of moong dal.",
        nutritionalInfo = "Approx: 200 cal/kachori, 5g protein, 8g fat, 28g carbs",
        healthTags = listOf("Snack", "Street Food", "Crunchy"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 170, "durationMin" to 20)
    )

    private fun getPakoraRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Pakora / Bhajiya (Vegetable Fritters)",
        category = "Snack",
        subCategory = "Fried Snack",
        cuisine = "Indian (Pan-India)",
        preparationTime = 10,
        cookingTime = 15,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "1 cup Besan (Gram Flour / Chickpea Flour)",
            "2 tbsp Rice Flour (for crispiness)",
            "½ tsp Red Chilli Powder",
            "½ tsp Turmeric Powder",
            "¼ tsp Asafoetida (Hing)",
            "½ tsp Carom Seeds (Ajwain)",
            "Salt to taste",
            "Water for batter",
            "2 cups Mixed Vegetables (onion slices, spinach leaves, potato slices, cauliflower florets, paneer cubes)",
            "Oil for deep frying",
            "GARNISH:",
            "Chaat Masala",
            "Fresh Coriander, chopped"
        ),
        steps = listOf(
            "BATTER:",
            "STEP 1: In a bowl, mix besan + rice flour + red chilli + turmeric + hing + ajwain + salt.",
            "STEP 2: Add water gradually and whisk into a thick, smooth batter (no lumps). The batter should coat the back of a spoon.",
            "STEP 3: Let the batter rest for 10 minutes — this makes the pakoras crispier.",
            "",
            "FRYING:",
            "STEP 4: Heat oil on medium heat.",
            "STEP 5: Dip vegetable pieces in batter, ensuring they are well coated.",
            "STEP 6: Gently slide into hot oil. Fry in batches — do not overcrowd.",
            "STEP 7: Fry until golden brown and crispy (4-5 minutes), turning occasionally.",
            "STEP 8: Drain on paper towel.",
            "",
            "SERVE: Hot, sprinkled with chaat masala. Serve with green chutney and tomato ketchup. The perfect rainy day snack with chai!"
        ),
        proTips = "PRO TIPS: 🔸 The batter should be thick enough to coat the vegetables — if it's too thin, it won't stick. 🔸 Rice flour is the secret to crispy pakoras — don't skip it. 🔸 Add a pinch of baking soda for extra fluffy pakoras. 🔸 Onion pakoras should be fried on slightly lower heat to cook the onions through. 🔸 For paneer pakoras, use thin slices of paneer — they cook faster. 🔸 Serve immediately — pakoras lose crispiness quickly.",
        nutritionalInfo = "Approx: 200 cal/serving, 6g protein, 10g fat, 22g carbs",
        healthTags = listOf("Snack", "Rainy Day", "Tea-Time", "Indulgent"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 180, "durationMin" to 15)
    )

    private fun getVadaPavRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Vada Pav (Mumbai's Iconic Burger)",
        category = "StreetFood",
        subCategory = "Vada Pav",
        cuisine = "Indian (Mumbai / Maharashtrian)",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "VADA (BATATA BHAJI):",
            "4 medium Potatoes, boiled and mashed",
            "1 tbsp Oil",
            "1 tsp Mustard Seeds",
            "1 tsp Cumin Seeds",
            "8-10 Curry Leaves",
            "1 tsp Ginger, grated",
            "2 Green Chillies, finely chopped",
            "½ tsp Turmeric Powder",
            "½ tsp Red Chilli Powder",
            "1 tbsp Lemon Juice",
            "2 tbsp Fresh Coriander, chopped",
            "Salt to taste",
            "BATTER:",
            "1 cup Besan (Gram Flour)",
            "½ tsp Red Chilli Powder",
            "¼ tsp Asafoetida (Hing)",
            "½ tsp Turmeric",
            "Salt to taste",
            "Water for batter",
            "ASSEMBLY:",
            "4 Pav Buns (soft dinner rolls)",
            "2 tbsp Green Chutney (Mint-Coriander)",
            "2 tbsp Garlic Chutney (dry red chilli + garlic + coconut)",
            "Oil for deep frying"
        ),
        steps = listOf(
            "VADA (POTATO FILLING):",
            "STEP 1: Heat oil. Add mustard seeds + cumin seeds. Let them crackle.",
            "STEP 2: Add curry leaves + ginger + green chillies. Sauté for 30 seconds.",
            "STEP 3: Add turmeric + red chilli powder. Stir.",
            "STEP 4: Add mashed potatoes + salt + lemon juice + coriander. Mix well. Cook for 2 minutes. Let cool.",
            "STEP 5: Shape the mixture into round balls (vadas).",
            "",
            "BATTER & FRY:",
            "STEP 6: Mix besan + red chilli + hing + turmeric + salt + water into a thick batter.",
            "STEP 7: Heat oil for deep frying. Dip each vada in batter, coating completely.",
            "STEP 8: Deep fry on medium heat until golden brown (4-5 minutes). Drain.",
            "",
            "ASSEMBLY:",
            "STEP 9: Slice pav buns horizontally. Toast lightly on a tawa with butter.",
            "STEP 10: Spread green chutney on one half, garlic chutney on the other.",
            "STEP 11: Place the fried vada in the centre. Press gently.",
            "SERVE: Hot, with fried green chilli (mirchi) on the side. The ultimate Mumbai street food experience!"
        ),
        proTips = "PRO TIPS: 🔸 The vada should be crispy outside and soft inside — the batter consistency is key. 🔸 Garlic chutney (sukhi chutney) is what gives vada pav its signature kick — don't skip it. 🔸 The pav should be soft and fresh — stale pav ruins the experience. 🔸 For the authentic Mumbai taste, serve with a side of fried green chillies sprinkled with salt. 🔸 Vada pav is also known as the 'Indian burger' and is a beloved street food across Maharashtra.",
        nutritionalInfo = "Approx: 300 cal/vada pav, 6g protein, 12g fat, 42g carbs",
        healthTags = listOf("Street Food", "Iconic", "Indulgent"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 180, "durationMin" to 20)
    )

    private fun getDabeliRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Dabeli (Kutchi Dabeli / Gujarati Burger)",
        category = "StreetFood",
        subCategory = "Dabeli",
        cuisine = "Indian (Gujarati / Kutch)",
        preparationTime = 20,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Sweet-Spicy",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "DABELI MASALA:",
            "4 medium Potatoes, boiled and mashed",
            "2 tbsp Oil",
            "1 tsp Mustard Seeds",
            "1 tsp Cumin Seeds",
            "1 tsp Ginger-Garlic paste",
            "2 Green Chillies, chopped",
            "1 tsp Red Chilli Powder",
            "½ tsp Turmeric",
            "1 tsp Dabeli Masala (readymade)",
            "2 tbsp Tamarind Pulp",
            "2 tbsp Sugar",
            "Salt to taste",
            "ASSEMBLY:",
            "4 Pav Buns (soft dinner rolls)",
            "Butter for toasting",
            "2 tbsp Green Chutney",
            "2 tbsp Meethi Chutney (Sweet Tamarind Chutney)",
            "½ cup Sev (thin gram flour noodles)",
            "¼ cup Pomegranate seeds",
            "¼ cup Roasted Peanuts",
            "1 small Onion, finely chopped",
            "Fresh Coriander, chopped"
        ),
        steps = listOf(
            "DABELI FILLING:",
            "STEP 1: Heat oil. Add mustard seeds + cumin seeds. Let them crackle.",
            "STEP 2: Add ginger-garlic paste + green chillies. Sauté for 30 seconds.",
            "STEP 3: Add red chilli powder + turmeric + dabeli masala. Stir.",
            "STEP 4: Add mashed potatoes + tamarind pulp + sugar + salt. Mix well. Cook for 3-4 minutes.",
            "",
            "ASSEMBLY:",
            "STEP 5: Slice pav buns. Toast with butter on a hot tawa until golden.",
            "STEP 6: Spread green chutney on one half, meethi chutney on the other.",
            "STEP 7: Place a generous portion of the potato mixture on the bottom half.",
            "STEP 8: Top with sev, pomegranate seeds, roasted peanuts, chopped onion, and coriander.",
            "STEP 9: Close with the top half. Press gently.",
            "SERVE: Hot, with extra sev and chutney on the side. The sweet-spicy combination is addictive!"
        ),
        proTips = "PRO TIPS: 🔸 Dabeli masala is a specific spice blend — use a good quality readymade brand. 🔸 The filling should be sweet, spicy, and tangy all at once. 🔸 The combination of sev, pomegranate, and peanuts creates a unique texture. 🔸 Dabeli is also known as 'Kutchi Dabeli' and is a specialty of the Kutch region of Gujarat. 🔸 For the authentic street food experience, serve in a paper plate with extra sev on top.",
        nutritionalInfo = "Approx: 280 cal/dabeli, 6g protein, 10g fat, 42g carbs",
        healthTags = listOf("Street Food", "Sweet-Spicy", "Iconic"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SHALLOW_FRY", "temperatureC" to 180, "durationMin" to 15)
    )

    private fun getMisalPavRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Misal Pav (Spicy Sprouted Bean Curry with Bread)",
        category = "StreetFood",
        subCategory = "Misal",
        cuisine = "Indian (Maharashtrian)",
        preparationTime = 30,
        cookingTime = 30,
        difficulty = "Hard",
        spiceLevel = "Hot",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "USAL (SPROUTED BEAN CURRY):",
            "2 cups Mixed Sprouts (Moth beans / Matki preferred)",
            "1 large Onion, finely chopped",
            "2 Tomatoes, pureed",
            "1 tbsp Ginger-Garlic paste",
            "2 tsp Misal Masala (or Goda Masala)",
            "1 tsp Red Chilli Powder",
            "½ tsp Turmeric",
            "1 tbsp Oil",
            "Salt to taste",
            "TADKA:",
            "1 tbsp Oil",
            "1 tsp Mustard Seeds",
            "1 tsp Cumin Seeds",
            "8-10 Curry Leaves",
            "2 Dry Red Chillies",
            "A pinch of Asafoetida",
            "TOPPINGS:",
            "1 cup Farsan / Sev (mixed fried snacks)",
            "1 large Onion, finely chopped",
            "Fresh Coriander, chopped",
            "Lemon wedges",
            "4 Pav Buns"
        ),
        steps = listOf(
            "USAL:",
            "STEP 1: Pressure cook sprouts with salt and turmeric until soft but not mushy (2-3 whistles).",
            "STEP 2: Heat oil. Add mustard seeds + cumin seeds. Let them crackle.",
            "STEP 3: Add curry leaves + dry red chillies + asafoetida.",
            "STEP 4: Add onion. Sauté until golden. Add ginger-garlic paste. Sauté for 1 minute.",
            "STEP 5: Add tomato puree. Cook until oil separates.",
            "STEP 6: Add misal masala + red chilli powder. Cook for 1 minute.",
            "STEP 7: Add cooked sprouts + 1 cup water. Simmer for 10-15 minutes. The usal should be thick and spicy.",
            "",
            "ASSEMBLY:",
            "STEP 8: Toast pav buns with butter on a hot tawa.",
            "STEP 9: Pour usal into a bowl. Top generously with farsan/sew, chopped onion, and coriander.",
            "STEP 10: Squeeze lemon juice on top.",
            "SERVE: Hot, with toasted pav on the side. The traditional way: crush some farsan into the usal and eat with pav."
        ),
        proTips = "PRO TIPS: 🔸 The sprouts should be fresh — day-old sprouts work best. 🔸 Misal is meant to be SPICY — don't hold back on the masala. 🔸 The farsan/sew topping is essential — it adds crunch to the spicy usal. 🔸 Misal pav is a traditional Maharashtrian breakfast/brunch dish. 🔸 For the authentic Kolhapuri experience, add a spoonful of 'tambda rassa' (red spicy curry) on top.",
        nutritionalInfo = "Approx: 350 cal/serving, 12g protein, 12g fat, 50g carbs",
        healthTags = listOf("Street Food", "Spicy", "Protein Rich", "Traditional"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SIMMER", "temperatureC" to 160, "durationMin" to 30)
    )

    private fun getFriedIdliRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Fried Idli (Crispy Leftover Idli Snack)",
        category = "Snack",
        subCategory = "South Indian Snack",
        cuisine = "Indian (South Indian / Fusion)",
        preparationTime = 5,
        cookingTime = 10,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 2,
        isBeverage = false,
        isStreetFood = false,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "6-8 Leftover Idlis, cubed",
            "2 tbsp Oil",
            "1 tsp Mustard Seeds",
            "1 tsp Cumin Seeds",
            "8-10 Curry Leaves",
            "2 Green Chillies, slit",
            "1 small Onion, sliced",
            "1 tsp Ginger, grated",
            "½ tsp Turmeric",
            "½ tsp Red Chilli Powder",
            "1 tsp Chaat Masala",
            "Salt to taste",
            "1 tbsp Lemon Juice",
            "2 tbsp Fresh Coriander, chopped"
        ),
        steps = listOf(
            "STEP 1: Cut leftover idlis into bite-sized cubes.",
            "STEP 2: Heat oil in a pan. Add mustard seeds + cumin seeds. Let them crackle.",
            "STEP 3: Add curry leaves + green chillies + ginger. Sauté for 30 seconds.",
            "STEP 4: Add sliced onion. Sauté until golden.",
            "STEP 5: Add turmeric + red chilli powder. Stir.",
            "STEP 6: Add idli cubes. Toss gently to coat with spices. Cook on medium heat for 4-5 minutes until the idlis are crispy on the edges.",
            "STEP 7: Add chaat masala + salt + lemon juice. Toss well.",
            "STEP 8: Garnish with fresh coriander.",
            "SERVE: Hot, as a snack or light meal. Perfect way to use leftover idlis!"
        ),
        proTips = "PRO TIPS: 🔸 Day-old idlis work best — fresh idlis are too soft and will crumble. 🔸 Cut the idlis into uniform cubes for even cooking. 🔸 For extra crunch, shallow fry the idli cubes in a little oil before adding spices. 🔸 Add chopped vegetables (capsicum, carrot) for a more nutritious version.",
        nutritionalInfo = "Approx: 200 cal/serving, 4g protein, 8g fat, 28g carbs",
        healthTags = listOf("Snack", "Leftover Makeover", "Crunchy"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SHALLOW_FRY", "temperatureC" to 180, "durationMin" to 10)
    )

    private fun getChilliIdliRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Chilli Idli (Indo-Chinese Style Idli)",
        category = "Snack",
        subCategory = "South Indian Snack",
        cuisine = "Indian (Indo-Chinese Fusion)",
        preparationTime = 5,
        cookingTime = 10,
        difficulty = "Easy",
        spiceLevel = "Medium-Hot",
        servings = 2,
        isBeverage = false,
        isStreetFood = false,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "6-8 Leftover Idlis, cubed or quartered",
            "2 tbsp Oil",
            "1 tsp Ginger, finely chopped",
            "1 tsp Garlic, finely chopped",
            "2 Green Chillies, slit",
            "1 medium Capsicum, cubed",
            "1 medium Onion, cubed",
            "2 tbsp Soya Sauce",
            "1 tbsp Red Chilli Sauce",
            "1 tbsp Tomato Ketchup",
            "1 tsp Vinegar",
            "½ tsp Sugar",
            "1 tsp Cornflour mixed with 2 tbsp water (slurry)",
            "Spring onions for garnish"
        ),
        steps = listOf(
            "STEP 1: Cut leftover idlis into quarters or cubes.",
            "STEP 2: Heat oil in a wok on high heat. Add ginger + garlic + green chillies. Sauté for 15 seconds.",
            "STEP 3: Add cubed capsicum and onion. Stir fry on high heat for 1 minute — they should remain crunchy.",
            "STEP 4: Add soya sauce, red chilli sauce, tomato ketchup, vinegar, sugar. Stir quickly to combine.",
            "STEP 5: Add cornflour slurry + 2 tbsp water. Stir and let the sauce thicken for 30 seconds.",
            "STEP 6: Add idli pieces. Toss on high heat to coat evenly. Cook for 1-2 minutes until the idlis absorb the sauce.",
            "SERVE: Garnish with spring onion greens. Serve hot as a snack or light meal."
        ),
        proTips = "PRO TIPS: 🔸 Day-old idlis work best — they absorb the sauce without becoming mushy. 🔸 High heat is essential for the Indo-Chinese wok-style flavour. 🔸 For extra crunch, shallow fry the idli pieces in oil before adding to the sauce.",
        nutritionalInfo = "Approx: 220 cal/serving, 4g protein, 8g fat, 32g carbs",
        healthTags = listOf("Snack", "Spicy", "Indo-Chinese"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "STIR_FRY", "temperatureC" to 220, "durationMin" to 10)
    )

    private fun getMeduVadaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Medu Vada (Savory Lentil Donuts)",
        category = "Snack",
        subCategory = "South Indian Snack",
        cuisine = "South Indian",
        preparationTime = 120,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "1 cup Urad Dal (split black gram)",
            "2-3 Green Chillies, finely chopped",
            "1 inch Ginger, grated",
            "1 small Onion, finely chopped (optional)",
            "8-10 Curry leaves, chopped",
            "1 tbsp Fresh Coriander, chopped",
            "1 tbsp Rice flour (for crispiness)",
            "¼ tsp Asafoetida (Hing)",
            "½ tsp Cumin seeds",
            "½ tsp Black peppercorns, crushed",
            "Salt to taste",
            "Oil for deep frying"
        ),
        steps = listOf(
            "PREP: Wash and soak urad dal in water for 4-6 hours. Drain completely.",
            "STEP 1 — GRIND: Grind the soaked urad dal WITHOUT water or with very minimal water. The batter should be thick, smooth, and fluffy.",
            "STEP 2 — WHIP: Transfer to a bowl. Whisk the batter with a spoon for 3-4 minutes until it becomes light and airy. The batter should hold its shape when dropped.",
            "STEP 3 — SEASON: Add green chillies, ginger, onion, curry leaves, coriander, rice flour, hing, cumin, crushed pepper, salt. Mix gently.",
            "STEP 4 — SHAPE: Wet your palms. Take a lemon-sized portion. Shape into a ball, then flatten slightly. Poke a hole in the centre using your thumb.",
            "STEP 5 — FRY: Heat oil on medium heat. Gently slide the vadas into the oil. Fry until golden brown on both sides (3-4 minutes per side).",
            "STEP 6 — DRAIN: Remove and drain on paper towel.",
            "SERVE: Hot with coconut chutney and sambhar."
        ),
        proTips = "PRO TIPS: 🔸 The batter should be thick enough to hold shape — excess water makes vadas flat and oily. 🔸 Whisking the batter well adds air and makes vadas fluffy. 🔸 Fry on medium heat — high heat will burn the outside while inside remains raw. 🔸 For extra crispiness, add 1 tbsp rice flour.",
        nutritionalInfo = "Approx: 200 cal/serving (2 vadas), 8g protein, 10g fat, 20g carbs",
        healthTags = listOf("Snack", "Traditional", "Crunchy"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 180, "durationMin" to 20)
    )

    private fun getMasalaDosaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Masala Dosa (South Indian Crispy Crepe)",
        category = "StreetFood",
        subCategory = "South Indian",
        cuisine = "South Indian",
        preparationTime = 240,
        cookingTime = 30,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 6,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "DOSA BATTER:",
            "2 cups Idli Rice (parboiled rice)",
            "½ cup Urad Dal (white split black gram)",
            "1 tsp Fenugreek seeds (Methi)",
            "1 tbsp Poha (flattened rice), optional",
            "Salt to taste",
            "Water for grinding",
            "ALOO MASALA FILLING:",
            "4 medium Potatoes, boiled and mashed",
            "1 tbsp Oil",
            "1 tsp Mustard seeds",
            "1 tsp Cumin seeds",
            "2-3 Green Chillies, chopped",
            "1 inch Ginger, grated",
            "1 medium Onion, sliced",
            "10-12 Curry leaves",
            "½ tsp Turmeric powder",
            "Salt to taste",
            "2 tbsp Fresh Coriander",
            "1 tbsp Lemon juice"
        ),
        steps = listOf(
            "DOSA BATTER (Do this the previous evening):",
            "STEP 1: Wash rice and urad dal separately. Soak rice + fenugreek seeds in water for 6-8 hours. Soak urad dal separately.",
            "STEP 2: First grind urad dal with minimal water until smooth and fluffy. Then grind rice + poha to a slightly grainy consistency.",
            "STEP 3: Combine both batters. Add salt. Mix well with your hand (this helps fermentation).",
            "STEP 4: Cover and keep in a warm place for 8-12 hours or overnight. The batter should rise and become bubbly.",
            "ALOO MASALA:",
            "STEP 5: Heat oil. Add mustard seeds + cumin seeds. Let them pop.",
            "STEP 6: Add curry leaves + green chillies + ginger. Sauté for 30 seconds.",
            "STEP 7: Add sliced onion. Sauté until translucent.",
            "STEP 8: Add turmeric + salt + mashed potatoes + lemon juice + coriander. Mix well.",
            "MAKING DOSA:",
            "STEP 9: Heat a non-stick tawa. Sprinkle water — if it sizzles, it's ready. Wipe with an oiled cloth.",
            "STEP 10: Pour a ladleful of batter in the centre. Spread in a circular motion from centre outwards into a thin circle.",
            "STEP 11: Drizzle a few drops of oil/ghee around the edges. Cook on medium-high heat until golden and crispy.",
            "STEP 12: Place a portion of aloo masala on one half. Fold the other half over.",
            "SERVE: With coconut chutney + sambhar."
        ),
        proTips = "PRO TIPS: 🔸 Urad dal must be ground to a very smooth, fluffy consistency. 🔸 Fermentation time depends on climate. 🔸 The tawa must be hot enough. 🔸 For crispy dosa, spread the batter thin.",
        nutritionalInfo = "Approx: 180 cal/dosa, 6g protein, 4g fat, 32g carbs",
        healthTags = listOf("Street Food", "Traditional", "Fermented"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "GRIDDLE", "temperatureC" to 200, "durationMin" to 30)
    )

    private fun getPlainDosaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Plain Dosa / Sada Dosa",
        category = "StreetFood",
        subCategory = "South Indian",
        cuisine = "South Indian",
        preparationTime = 240,
        cookingTime = 20,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 6,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "Same batter as Masala Dosa (above)",
            "Oil or Ghee for cooking"
        ),
        steps = listOf(
            "FOLLOW Masala Dosa batter preparation steps 1-4.",
            "STEP 1: Heat tawa. Pour batter, spread thin in circular motion.",
            "STEP 2: Drizzle ghee around edges. Cook until golden and crispy.",
            "STEP 3: Fold in half or roll. Serve immediately.",
            "SERVE: With coconut chutney + tomato chutney + sambhar."
        ),
        proTips = "PRO TIPS: 🔸 Sada dosa is thinner and crispier than masala dosa. 🔸 For paper dosa, spread the thinnest possible layer. 🔸 Ghee gives the best flavour.",
        nutritionalInfo = "Approx: 120 cal/dosa, 4g protein, 2g fat, 22g carbs",
        healthTags = listOf("Street Food", "Traditional", "Light"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "GRIDDLE", "temperatureC" to 200, "durationMin" to 20)
    )

    private fun getUttapamRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Uttapam (Thick Vegetable Pancake)",
        category = "StreetFood",
        subCategory = "South Indian",
        cuisine = "South Indian",
        preparationTime = 240,
        cookingTime = 20,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "Same dosa batter as Masala Dosa",
            "1 small Onion, finely chopped",
            "1 small Tomato, finely chopped",
            "1 Green Chilli, finely chopped",
            "2 tbsp Fresh Coriander, chopped",
            "1 tbsp Grated Carrot (optional)",
            "Oil/Ghee for cooking"
        ),
        steps = listOf(
            "STEP 1: Prepare dosa batter as per Masala Dosa recipe.",
            "STEP 2: Heat a tawa on medium heat. Pour a thick ladleful of batter — do not spread thin like dosa.",
            "STEP 3: Immediately sprinkle chopped onion, tomato, green chilli, coriander, and carrot on top. Press gently.",
            "STEP 4: Drizzle oil/ghee around the edges. Cook on medium heat until the bottom is golden and crispy (3-4 minutes).",
            "STEP 5: Flip carefully. Cook the other side for 1 minute.",
            "SERVE: Hot with coconut chutney and sambhar."
        ),
        proTips = "PRO TIPS: 🔸 Uttapam is thicker than dosa — do not spread the batter thin. 🔸 Add the toppings immediately after pouring the batter so they stick. 🔸 Cook on medium heat to ensure the inside cooks through.",
        nutritionalInfo = "Approx: 150 cal/uttapam, 4g protein, 3g fat, 28g carbs",
        healthTags = listOf("Street Food", "Traditional", "Filling"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "GRIDDLE", "temperatureC" to 180, "durationMin" to 20)
    )

    private fun getAppamRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Appam (Lacy Rice Pancake / Hopper)",
        category = "StreetFood",
        subCategory = "South Indian",
        cuisine = "South Indian (Kerala / Tamil Nadu)",
        preparationTime = 240,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 6,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "2 cups Raw Rice, soaked 4-6 hours",
            "½ cup Fresh Grated Coconut",
            "½ cup Cooked Rice (for fermentation)",
            "1 tsp Active Dry Yeast or 1 tbsp Sugar (for fermentation)",
            "Salt to taste",
            "Oil/Ghee for cooking"
        ),
        steps = listOf(
            "BATTER:",
            "STEP 1: Drain soaked rice. Grind rice + coconut + cooked rice into a smooth batter using minimal water.",
            "STEP 2: Add yeast/sugar + salt. Mix well. Ferment for 8-12 hours or overnight.",
            "STEP 3: The batter should be thin and flowing, like dosa batter but slightly thinner.",
            "",
            "MAKING APPAM:",
            "STEP 4: Heat an appam chatti (small round-bottomed pan). Add a ladleful of batter and swirl to coat the sides thinly.",
            "STEP 5: Cover and cook on medium heat for 2-3 minutes. The centre should be soft and spongy, edges crispy and lacy.",
            "STEP 6: Do not flip. Remove gently with a spatula.",
            "SERVE: Hot with coconut chutney, vegetable stew, or egg curry."
        ),
        proTips = "PRO TIPS: 🔸 An appam chatti (special pan) is essential for the authentic shape. 🔸 The batter should be thinner than dosa batter. 🔸 Swirl the batter immediately after pouring to coat the sides. 🔸 The edges should be lacy and crispy, the centre soft and spongy.",
        nutritionalInfo = "Approx: 100 cal/appam, 2g protein, 3g fat, 18g carbs",
        healthTags = listOf("Street Food", "Traditional", "Fermented"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "GRIDDLE", "temperatureC" to 180, "durationMin" to 20)
    )

    // ─── SNACKS: Farsan / Gujarati ────────────────

    private fun getDhoklaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Khaman Dhokla (Steamed Lentil Cake)",
        category = "Snack",
        subCategory = "Gujarati Farsan",
        cuisine = "Gujarati",
        preparationTime = 15,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 6,
        isBeverage = false,
        isStreetFood = false,
        isSnack = true,
        temperature = "Room Temperature",
        ingredients = listOf(
            "1 cup Besan (Gram Flour / Chickpea Flour)",
            "1 tbsp Suji (Semolina)",
            "1 tsp Ginger-Green Chilli paste",
            "1 tsp Sugar",
            "½ tsp Turmeric powder",
            "1 tsp Lemon juice",
            "1 tsp Eno Fruit Salt (or ½ tsp baking soda + 1 tsp lemon juice)",
            "Salt to taste",
            "Water for batter",
            "TEMPERING:",
            "1 tbsp Oil",
            "1 tsp Mustard seeds",
            "1 tsp Sesame seeds",
            "8-10 Curry leaves",
            "2-3 Green Chillies, slit",
            "2 tbsp Fresh Coriander, chopped",
            "2 tbsp Fresh Coconut, grated",
            "1 tsp Sugar dissolved in 2 tbsp water"
        ),
        steps = listOf(
            "PREP: Grease a thali or idli plate. Prepare a steamer or pressure cooker with water.",
            "STEP 1: In a bowl, mix besan, suji, ginger-chilli paste, sugar, turmeric, lemon juice, salt. Add water to make a smooth batter.",
            "STEP 2: Just before steaming, add Eno fruit salt + 1 tbsp water. Mix gently — the batter will become frothy.",
            "STEP 3: Immediately pour into the greased thali (not more than 1-inch thick). Steam on medium heat for 12-15 minutes.",
            "STEP 4: Insert a knife/toothpick — it should come out clean. Let cool for 5 minutes.",
            "TEMPERING:",
            "STEP 5: Heat oil. Add mustard seeds. Let them pop.",
            "STEP 6: Add sesame seeds + curry leaves + green chillies. Sauté for 10 seconds.",
            "STEP 7: Pour the sugar-water mixture. Let it sizzle. Pour this tempering over the steamed dhokla.",
            "STEP 8: Cut into pieces. Garnish with coriander + coconut.",
            "SERVE: With green chutney or tamarind chutney."
        ),
        proTips = "PRO TIPS: 🔸 Eno should be added just before steaming — the batter cannot wait. 🔸 The steamer must be ready before adding Eno. 🔸 Do not open the lid during steaming. 🔸 The sugar-water tempering gives dhokla its signature sweet-tangy taste.",
        nutritionalInfo = "Approx: 120 cal/serving, 6g protein, 4g fat, 16g carbs",
        healthTags = listOf("Snack", "Steamed", "Healthy", "Fermented"),
        smartApplianceMode = "STEAMER",
        smartApplianceParams = mapOf("mode" to "STEAM", "durationMin" to 15)
    )

    private fun getKhandviRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Khandvi (Gujarati Gram Flour Rolls)",
        category = "Snack",
        subCategory = "Gujarati Farsan",
        cuisine = "Gujarati",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 4,
        isBeverage = false,
        isStreetFood = false,
        isSnack = true,
        temperature = "Room Temperature",
        ingredients = listOf(
            "1 cup Besan (Gram Flour)",
            "1½ cups Water",
            "½ tsp Turmeric powder",
            "½ tsp Ginger paste",
            "Salt to taste",
            "1 tsp Lemon juice",
            "TEMPERING:",
            "1 tbsp Oil",
            "1 tsp Mustard seeds",
            "1 tsp Sesame seeds",
            "8-10 Curry leaves",
            "2-3 Green Chillies, slit",
            "2 tbsp Fresh Coconut, grated",
            "2 tbsp Fresh Coriander, chopped"
        ),
        steps = listOf(
            "STEP 1: In a bowl, mix besan + water + turmeric + ginger + salt + lemon juice. Whisk until smooth (no lumps).",
            "STEP 2: Pour the mixture into a non-stick pan. Cook on medium heat, stirring continuously, until it thickens to a paste (6-8 minutes).",
            "STEP 3: The mixture is ready when it leaves the sides of the pan and forms a soft, glossy dough.",
            "STEP 4: Immediately spread the mixture thinly on a greased marble board or steel plate using a flat spatula. Work quickly — it sets fast.",
            "STEP 5: Let it cool for 2-3 minutes. Cut into thin strips (1.5-inch wide). Roll each strip tightly into a cylindrical roll.",
            "TEMPERING:",
            "STEP 6: Heat oil. Add mustard seeds. Let them pop. Add sesame seeds + curry leaves + green chillies. Sauté for 10 seconds.",
            "STEP 7: Pour the tempering over the khandvi rolls. Garnish with coconut and coriander.",
            "SERVE: At room temperature, with green chutney."
        ),
        proTips = "PRO TIPS: 🔸 The besan mixture must be cooked to the perfect consistency — undercooked is runny, overcooked is hard. 🔸 Work quickly when spreading on the board — the mixture sets fast. 🔸 The spreading should be thin and even for perfect rolls. 🔸 Khandvi is a delicate snack — handle the rolls gently.",
        nutritionalInfo = "Approx: 140 cal/serving, 6g protein, 6g fat, 16g carbs",
        healthTags = listOf("Snack", "Traditional", "Light"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SIMMER", "temperatureC" to 140, "durationMin" to 20)
    )

    private fun getMuthiaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Methi Muthia (Fenugreek Dumplings)",
        category = "Snack",
        subCategory = "Gujarati Farsan",
        cuisine = "Gujarati",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 6,
        isBeverage = false,
        isStreetFood = false,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "1 cup Whole Wheat Flour",
            "½ cup Besan (Gram Flour)",
            "1 cup Fresh Methi (Fenugreek) leaves, chopped",
            "2 tbsp Curd (Yogurt)",
            "1 tsp Ginger-Green Chilli paste",
            "1 tsp Turmeric powder",
            "1 tsp Red Chilli powder",
            "1 tsp Sugar",
            "1 tbsp Oil",
            "½ tsp Baking Soda",
            "Salt to taste",
            "TEMPERING:",
            "1 tbsp Oil",
            "1 tsp Mustard seeds",
            "1 tsp Sesame seeds",
            "8-10 Curry leaves",
            "Fresh Coconut, grated for garnish"
        ),
        steps = listOf(
            "STEP 1: Wash and finely chop methi leaves. Squeeze out excess water.",
            "STEP 2: In a bowl, mix wheat flour + besan + methi + curd + ginger-chilli paste + turmeric + red chilli + sugar + oil + baking soda + salt. Knead into a soft dough.",
            "STEP 3: Divide the dough into 4 portions. Shape each into a cylindrical log (like a thick sausage).",
            "STEP 4: Steam the logs in a steamer for 15-20 minutes until firm. Let cool slightly.",
            "STEP 5: Cut the steamed logs into ½-inch thick rounds (muthia pieces).",
            "STEP 6: Heat oil in a pan. Add mustard seeds. Let them pop. Add sesame seeds + curry leaves.",
            "STEP 7: Add the muthia pieces. Sauté on medium heat until golden and crispy on all sides (5-6 minutes).",
            "SERVE: Hot, garnished with fresh coconut and coriander. Perfect with a cup of chai."
        ),
        proTips = "PRO TIPS: 🔸 Squeeze the methi leaves well to remove bitterness. 🔸 Steam until firm — undercooked muthia will fall apart while sautéing. 🔸 Sautéing after steaming gives them the crispy exterior. 🔸 Muthia can be made in bulk and refrigerated for up to a week.",
        nutritionalInfo = "Approx: 150 cal/serving, 5g protein, 5g fat, 22g carbs",
        healthTags = listOf("Snack", "Steamed", "Healthy", "Tea-Time"),
        smartApplianceMode = "STEAMER",
        smartApplianceParams = mapOf("mode" to "STEAM", "durationMin" to 20)
    )

    private fun getFafdaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Fafda (Crispy Gram Flour Snack)",
        category = "Snack",
        subCategory = "Gujarati Farsan",
        cuisine = "Gujarati",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 6,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Room Temperature",
        ingredients = listOf(
            "2 cups Besan (Gram Flour)",
            "2 tbsp Oil + extra for kneading",
            "½ tsp Turmeric powder",
            "½ tsp Asafoetida (Hing)",
            "1 tsp Ajwain (Carom seeds)",
            "Salt to taste",
            "½ tsp Baking Soda",
            "Water for kneading",
            "Oil for deep frying"
        ),
        steps = listOf(
            "DOUGH:",
            "STEP 1: Mix besan + turmeric + hing + ajwain + salt + baking soda. Rub 2 tbsp oil into the flour.",
            "STEP 2: Add water gradually and knead into a stiff dough. Apply oil on your palms and knead again for 2 minutes. Rest for 15 minutes.",
            "ROLLING:",
            "STEP 3: Divide dough into small balls. Roll each into a thin circle (like a roti).",
            "STEP 4: Cut the circle into thin strips (½-inch wide, 3-inch long).",
            "FRYING:",
            "STEP 5: Heat oil on medium heat. Gently slide in the fafda strips.",
            "STEP 6: Fry on medium heat until light golden and crisp (3-4 minutes). Do not let them brown.",
            "STEP 7: Drain on paper towel. Let cool completely for maximum crispiness.",
            "SERVE: With sweet-spicy papaya chutney (saath) and fried green chillies. Traditionally eaten for breakfast."
        ),
        proTips = "PRO TIPS: 🔸 Fafda is traditionally eaten with 'saath' (sweet-spicy papaya chutney) and jalebi for breakfast. 🔸 The dough should be stiff — soft dough absorbs oil. 🔸 Roll as thin as possible for maximum crispiness. 🔸 Fry on medium heat — high heat burns the outside. 🔸 Fafda-Jalebi is the iconic Gujarati breakfast combination.",
        nutritionalInfo = "Approx: 180 cal/serving, 6g protein, 8g fat, 20g carbs",
        healthTags = listOf("Snack", "Crunchy", "Traditional"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 170, "durationMin" to 20)
    )

    // ─── SNACKS: North Indian ─────────────────────

    private fun getMathriRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Mathri / Namak Para (Crispy Savory Biscuit)",
        category = "Snack",
        subCategory = "North Indian Snack",
        cuisine = "North Indian",
        preparationTime = 15,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 8,
        isBeverage = false,
        isStreetFood = false,
        isSnack = true,
        temperature = "Room Temperature",
        ingredients = listOf(
            "2 cups All-Purpose Flour (Maida)",
            "¼ cup Ghee or Oil",
            "½ tsp Ajwain (Carom seeds)",
            "½ tsp Cumin seeds",
            "½ tsp Black Pepper, crushed",
            "Salt to taste",
            "Water for kneading",
            "Oil for deep frying"
        ),
        steps = listOf(
            "STEP 1: Mix flour + ghee + ajwain + cumin + pepper + salt. Rub with fingertips until crumbly.",
            "STEP 2: Add water gradually and knead into a stiff dough. Rest for 15 minutes.",
            "STEP 3: Roll the dough into a ⅛-inch thick sheet. Prick with a fork all over (prevents puffing).",
            "STEP 4: Cut into small squares or diamonds using a knife or pastry cutter.",
            "STEP 5: Heat oil on medium heat. Deep fry the mathris in batches until light golden and crisp (3-4 minutes).",
            "STEP 6: Drain on paper towel. Let cool completely.",
            "SERVE: At room temperature as a tea-time snack. Store in an airtight container for up to 2 weeks."
        ),
        proTips = "PRO TIPS: 🔸 The dough should be stiff — soft dough absorbs oil. 🔸 Pricking with a fork prevents the mathris from puffing up. 🔸 Roll evenly for uniform cooking. 🔸 Mathri is the perfect travel snack — it stays fresh for weeks. 🔸 For namak para, cut into smaller pieces (finger-sized strips).",
        nutritionalInfo = "Approx: 120 cal/serving, 2g protein, 6g fat, 14g carbs",
        healthTags = listOf("Snack", "Crunchy", "Tea-Time"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 170, "durationMin" to 15)
    )

    private fun getBhujiaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Aloo Bhujia (Crispy Potato Noodles)",
        category = "Snack",
        subCategory = "North Indian Snack",
        cuisine = "North Indian",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Medium",
        servings = 8,
        isBeverage = false,
        isStreetFood = false,
        isSnack = true,
        temperature = "Room Temperature",
        ingredients = listOf(
            "2 cups Besan (Gram Flour)",
            "½ cup Boiled Potato, mashed",
            "1 tbsp Rice Flour (for crispiness)",
            "1 tsp Red Chilli Powder",
            "½ tsp Turmeric Powder",
            "½ tsp Chaat Masala",
            "½ tsp Black Salt",
            "Salt to taste",
            "Water for kneading",
            "Oil for deep frying"
        ),
        steps = listOf(
            "STEP 1: In a bowl, mix besan + rice flour + mashed potato + red chilli + turmeric + chaat masala + black salt + salt.",
            "STEP 2: Add water gradually and knead into a stiff dough (similar to mathri dough).",
            "STEP 3: Fill a sev press (chakli maker) with the dough, fitted with a fine round nozzle.",
            "STEP 4: Heat oil on medium heat. Press the dough directly into the hot oil in a circular motion to form coils.",
            "STEP 5: Fry until golden and crisp (3-4 minutes), turning gently. Drain on paper towel.",
            "STEP 6: Let cool completely before storing.",
            "SERVE: As a snack, or use as a topping for chaat and bhel puri."
        ),
        proTips = "PRO TIPS: 🔸 The dough should be firm enough to hold shape when pressed. 🔸 The mashed potato makes this uniquely soft yet crispy. 🔸 Use a fine nozzle for thin, crispy bhujia. 🔸 Store in an airtight container for up to 2 weeks.",
        nutritionalInfo = "Approx: 140 cal/serving, 4g protein, 6g fat, 18g carbs",
        healthTags = listOf("Snack", "Crunchy", "Tea-Time"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 170, "durationMin" to 20)
    )

    // ─── DESSERT SNACKS ───────────────────────────

    private fun getJalebiRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Jalebi (Crispy Sweet Swirls)",
        category = "Snack",
        subCategory = "Dessert Snack",
        cuisine = "Indian (Pan-India)",
        preparationTime = 120,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Sweet",
        servings = 8,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "BATTER:",
            "1 cup All-Purpose Flour (Maida)",
            "1 tbsp Cornflour",
            "½ cup Curd (Yogurt), sour",
            "½ tsp Baking Soda",
            "Water for batter",
            "A pinch of orange food colour (optional)",
            "SUGAR SYRUP:",
            "1½ cups Sugar",
            "1 cup Water",
            "½ tsp Cardamom powder",
            "1 tsp Rose water",
            "1 tsp Lemon juice",
            "Oil/Ghee for deep frying"
        ),
        steps = listOf(
            "BATTER (Make a day before):",
            "STEP 1: Mix flour + cornflour + curd + baking soda + colour + water to make a flowing batter. Cover and ferment 8-12 hours.",
            "SUGAR SYRUP:",
            "STEP 2: Boil sugar + water + cardamom + lemon juice until 1-string consistency. Add rose water. Keep warm.",
            "FRYING:",
            "STEP 3: Heat oil on medium heat. Pour batter into a squeeze bottle or piping bag with small hole.",
            "STEP 4: Squeeze batter into hot oil in spiral motion — starting from centre, going outward.",
            "STEP 5: Fry until deep golden and crisp. Immediately dip in warm sugar syrup for 30 seconds.",
            "SERVE: Hot and crispy. Piping hot jalebis with rabri is the ultimate combination."
        ),
        proTips = "PRO TIPS: 🔸 Sour curd + overnight fermentation creates tanginess and crispiness. 🔸 The syrup should be warm, not hot. 🔸 Jalebis must be served fresh.",
        nutritionalInfo = "Approx: 200 cal/serving, 2g protein, 6g fat, 38g carbs",
        healthTags = listOf("Dessert", "Indulgent", "Street Food"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 180, "durationMin" to 20)
    )

    private fun getGulabJamunRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Gulab Jamun (Milk Dumplings in Sugar Syrup)",
        category = "Snack",
        subCategory = "Dessert Snack",
        cuisine = "Indian (Pan-India)",
        preparationTime = 15,
        cookingTime = 25,
        difficulty = "Hard",
        spiceLevel = "Sweet",
        servings = 12,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Room Temperature",
        ingredients = listOf(
            "DUMPLINGS:",
            "1 cup Milk Powder",
            "¼ cup All-Purpose Flour (Maida)",
            "¼ tsp Baking Soda",
            "2 tbsp Ghee (melted)",
            "3-4 tbsp Milk (warm)",
            "Oil/Ghee for deep frying",
            "SUGAR SYRUP:",
            "2 cups Sugar",
            "2 cups Water",
            "4-5 Green Cardamoms, crushed",
            "1 tsp Rose water",
            "1 tsp Lemon juice"
        ),
        steps = listOf(
            "SUGAR SYRUP (Make first):",
            "STEP 1: Boil sugar + water + cardamom until sugar dissolves. Simmer for 5 minutes. Add lemon juice + rose water.",
            "DOUGH:",
            "STEP 2: Mix milk powder + flour + baking soda. Sift well. Add melted ghee, mix until crumbly.",
            "STEP 3: Add warm milk gradually, knead into a soft smooth dough. Rest 10 minutes.",
            "SHAPING & FRYING:",
            "STEP 4: Divide and roll into smooth balls (no cracks). Fry on LOW heat until deep golden (5-7 minutes).",
            "STEP 5: Drain and immediately transfer to warm sugar syrup. Soak for at least 2 hours before serving.",
            "SERVE: Warm or room temperature. Garnish with chopped pistachios."
        ),
        proTips = "PRO TIPS: 🔸 Oil MUST be low heat — this is the most critical step! 🔸 Syrup should be warm, not hot. 🔸 Resting in warm syrup for 2+ hours is non-negotiable.",
        nutritionalInfo = "Approx: 150 cal/jamun, 2g protein, 5g fat, 26g carbs",
        healthTags = listOf("Dessert", "Indulgent", "Festival"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "DEEP_FRY", "temperatureC" to 140, "durationMin" to 25)
    )

    private fun getRasgullaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Rasgulla (Bengali Syrupy Cheese Balls)",
        category = "Snack",
        subCategory = "Dessert Snack",
        cuisine = "Bengali",
        preparationTime = 30,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Sweet",
        servings = 10,
        isBeverage = false,
        isStreetFood = false,
        isSnack = true,
        temperature = "Cold",
        ingredients = listOf(
            "CHENA (COTTAGE CHEESE):",
            "1 litre Full-fat Milk",
            "2 tbsp Lemon Juice or Vinegar",
            "SUGAR SYRUP:",
            "2 cups Sugar",
            "4 cups Water",
            "2-3 Green Cardamoms, crushed",
            "1 tsp Rose water",
            "Ice water for soaking"
        ),
        steps = listOf(
            "CHENA:",
            "STEP 1: Boil milk. When it comes to a rolling boil, add lemon juice. Stir until the milk curdles completely.",
            "STEP 2: Strain through a muslin cloth. Wash under cold water to remove sourness.",
            "STEP 3: Hang the cloth for 30 minutes to drain excess water. The chena should be soft and crumbly.",
            "STEP 4: Knead the chena on a flat surface for 5-6 minutes until smooth and soft.",
            "STEP 5: Divide into smooth balls (no cracks).",
            "SUGAR SYRUP:",
            "STEP 6: Boil sugar + water + cardamom in a broad pan. The syrup should be thin.",
            "STEP 7: Gently drop the chena balls into the boiling syrup. Cover and cook on medium heat for 15-20 minutes.",
            "STEP 8: The rasgullas will double in size. Turn off heat and let them cool in the syrup.",
            "STEP 9: Refrigerate for at least 4 hours before serving. Add rose water.",
            "SERVE: Chilled, with a little syrup. The rasgullas should be soft, spongy, and full of syrup."
        ),
        proTips = "PRO TIPS: 🔸 The chena must be kneaded well — under-kneaded rasgullas are grainy. 🔸 The syrup must be thin (not thick like jalebi syrup). 🔸 Cover the pan while cooking — the steam helps them puff. 🔸 Never open the lid suddenly — the rasgullas may collapse.",
        nutritionalInfo = "Approx: 120 cal/rasgulla, 3g protein, 4g fat, 20g carbs",
        healthTags = listOf("Dessert", "Traditional", "Festival"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "BOIL", "temperatureC" to 100, "durationMin" to 20)
    )

    private fun getKulfiRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Matka Kulfi (Traditional Indian Ice Cream)",
        category = "Snack",
        subCategory = "Dessert Snack",
        cuisine = "Indian (North Indian / Mughlai)",
        preparationTime = 20,
        cookingTime = 30,
        difficulty = "Medium",
        spiceLevel = "Sweet",
        servings = 6,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Cold",
        ingredients = listOf(
            "1 litre Full-fat Milk",
            "½ cup Sugar",
            "¼ cup Condensed Milk (Milkmaid)",
            "2 tbsp Cornflour mixed with ¼ cup milk",
            "½ tsp Cardamom powder",
            "2 tbsp Chopped Pistachios",
            "2 tbsp Chopped Almonds",
            "1 tbsp Chopped Cashews",
            "Few saffron strands soaked in 2 tbsp warm milk",
            "1 tsp Rose water",
            "Earthen pots (matkas) or kulfi moulds"
        ),
        steps = listOf(
            "STEP 1: Boil milk in a heavy-bottomed pan on medium heat. Stir frequently.",
            "STEP 2: Simmer until milk reduces by half (20-25 minutes), stirring occasionally.",
            "STEP 3: Add sugar + condensed milk. Stir until dissolved.",
            "STEP 4: Add cornflour slurry. Stir continuously for 3-4 minutes until the mixture thickens.",
            "STEP 5: Add cardamom + saffron milk + chopped nuts + rose water. Mix well.",
            "STEP 6: Let the mixture cool completely. Pour into matkas (earthen pots) or kulfi moulds.",
            "STEP 7: Cover and freeze for 8-10 hours or overnight.",
            "STEP 8: To serve, let the matka sit at room temperature for 5 minutes.",
            "SERVE: In the matka itself, topped with chopped nuts and a sprinkle of cardamom."
        ),
        proTips = "PRO TIPS: 🔸 Slow reduction of milk is the key to creamy kulfi. 🔸 Earthen pots (matkas) give kulfi its signature earthy flavour. 🔸 Do not stir while freezing — kulfi should be dense, not aerated like ice cream. 🔸 For variations: add mango pulp, chocolate, or saffron-pista.",
        nutritionalInfo = "Approx: 250 cal/serving, 6g protein, 10g fat, 34g carbs",
        healthTags = listOf("Dessert", "Indulgent", "Traditional"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SIMMER", "temperatureC" to 130, "durationMin" to 30)
    )

    private fun getRabriRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Rabri (Sweetened Thickened Milk)",
        category = "Snack",
        subCategory = "Dessert Snack",
        cuisine = "Indian (North Indian)",
        preparationTime = 5,
        cookingTime = 60,
        difficulty = "Hard",
        spiceLevel = "Sweet",
        servings = 4,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Cold",
        ingredients = listOf(
            "2 litres Full-fat Milk",
            "½ cup Sugar",
            "¼ cup Condensed Milk (optional)",
            "½ tsp Cardamom powder",
            "Few saffron strands",
            "2 tbsp Chopped Pistachios + Almonds",
            "1 tsp Rose water"
        ),
        steps = listOf(
            "STEP 1: Pour milk into a heavy-bottomed, wide pan. Bring to a boil on medium heat.",
            "STEP 2: As the milk boils, a cream layer (malai) will form on top. Gently push the cream layer to the sides with a spatula.",
            "STEP 3: Continue boiling on low heat, collecting the cream layers. This takes 45-60 minutes.",
            "STEP 4: When milk reduces to one-third, add sugar + condensed milk. Stir gently.",
            "STEP 5: Add cardamom + saffron + rose water. Mix gently.",
            "STEP 6: Let it cool. Refrigerate for 4 hours. The rabri will thicken further.",
            "SERVE: Chilled, garnished with chopped nuts. Traditionally served with jalebi or malpua."
        ),
        proTips = "PRO TIPS: 🔸 Patience is the key — slow reduction creates the rich, creamy texture. 🔸 Do not stir vigorously — the cream layers (malai) are the prized part. 🔸 A wide pan helps faster reduction. 🔸 Rabri is traditionally served with jalebi, malpua, or as a dessert on its own.",
        nutritionalInfo = "Approx: 280 cal/serving, 8g protein, 14g fat, 32g carbs",
        healthTags = listOf("Dessert", "Indulgent", "Traditional"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SIMMER", "temperatureC" to 120, "durationMin" to 60)
    )

    private fun getMalpuaRecipe(): SnackBeverageRecipe = SnackBeverageRecipe(
        name = "Malpua (Crispy Indian Pancake in Syrup)",
        category = "Snack",
        subCategory = "Dessert Snack",
        cuisine = "Indian (North Indian / Bengali)",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Medium",
        spiceLevel = "Sweet",
        servings = 6,
        isBeverage = false,
        isStreetFood = true,
        isSnack = true,
        temperature = "Hot",
        ingredients = listOf(
            "BATTER:",
            "1 cup All-Purpose Flour (Maida)",
            "¼ cup Semolina (Suji)",
            "1 tbsp Fennel Seeds (Saunf), crushed",
            "½ cup Sugar",
            "½ cup Milk",
            "Water for batter",
            "Oil/Ghee for deep frying",
            "SUGAR SYRUP:",
            "1 cup Sugar",
            "½ cup Water",
            "½ tsp Cardamom powder",
            "1 tsp Rose water",
            "Few saffron strands"
        ),
        steps = listOf(
            "BATTER:",
            "STEP 1: Mix flour + semolina + crushed fennel seeds + sugar + milk. Add water gradually to make a thick, flowing batter (like dosa batter). Rest for 30 minutes.",
            "SUGAR SYRUP:",
            "STEP 2: Boil sugar + water + cardamom + saffron until slightly sticky. Add rose water.",
            "FRYING:",
            "STEP 3: Heat oil/ghee in a shallow pan. Pour a ladleful of batter and spread into a small pancake (3-inch diameter).",
            "STEP 4: Fry on medium heat until golden brown on both sides (3-4 minutes). The edges should be crisp.",
            "STEP 5: Immediately dip the hot malpua in warm sugar syrup for 30 seconds. Remove and drain.",
            "SERVE: Hot, garnished with chopped nuts. Best served with rabri."
        ),
        proTips = "PRO TIPS: 🔸 Fennel seeds give malpua its distinctive flavour. 🔸 The batter should be slightly thick — thin batter makes flat, crispy malpuas. 🔸 Deep frying in ghee gives the best flavour. 🔸 Malpua is traditionally prepared during Holi and festivals.",
        nutritionalInfo = "Approx: 220 cal/malpua, 4g protein, 8g fat, 36g carbs",
        healthTags = listOf("Dessert", "Indulgent", "Festival"),
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("mode" to "SHALLOW_FRY", "temperatureC" to 180, "durationMin" to 20)
    )

    // ──────────────────────────────────────────────
    // Generic Guidance
    // ──────────────────────────────────────────────

    private fun getGenericSnackGuidance(dishName: String): SnackBeverageRecipe {
        return SnackBeverageRecipe(
            name = dishName.ifBlank { "Indian Snack or Beverage" },
            category = "Snack",
            cuisine = "Indian",
            preparationTime = 15,
            cookingTime = 15,
            difficulty = "Medium",
            servings = 2,
            isBeverage = false,
            isStreetFood = false,
            isSnack = true,
            temperature = "Hot",
            ingredients = listOf(
                "Please specify a dish name for detailed ingredients."
            ),
            steps = listOf(
                "I don't have a specific recipe for '$dishName' yet, but here are some popular categories:",
                "",
                "🍵 BEVERAGES: Masala Chai, Filter Coffee, Lassi, Buttermilk, Jaljeera, Nimbu Pani, Aam Panna, Cold Coffee",
                "🍲 STREET FOOD: Pani Puri, Pav Bhaji, Momos, Kathi Roll, Vada Pav, Bhel Puri, Aloo Tikki, Samosa Chaat",
                "🍿 SNACKS: Samosa, Kachori, Pakora, Dhokla, Khandvi, Mathri, Bhujia, Fried Idli",
                "🍨 DESSERTS: Jalebi, Gulab Jamun, Kulfi, Rasgulla, Rabri, Malpua",
                "",
                "Please tell me which snack, beverage, or street food you'd like to learn about! मुझे बताएं कि आप क्या बनाना चाहते हैं!"
            ),
            proTips = "Indian snacks and street food are all about the balance of flavours — sweet, spicy, tangy, and crunchy. The fresher the ingredients, the better the taste!"
        )
    }

    // ──────────────────────────────────────────────
    // Smart Kitchen Integration
    // ──────────────────────────────────────────────

    /**
     * Get available smart appliances for making beverages and snacks.
     */
    fun getAvailableAppliances(): List<SmartKitchenHardware> {
        return listOf(
            SmartKitchenHardware(isConnected = false, applianceName = "Smart Kettle", applianceType = "KETTLE"),
            SmartKitchenHardware(isConnected = false, applianceName = "Coffee Maker", applianceType = "COFFEE_MAKER"),
            SmartKitchenHardware(isConnected = false, applianceName = "Induction Cooktop", applianceType = "INDUCTION"),
            SmartKitchenHardware(isConnected = false, applianceName = "Air Fryer", applianceType = "AIR_FRYER"),
            SmartKitchenHardware(isConnected = false, applianceName = "Microwave Oven", applianceType = "MICROWAVE")
        )
    }

    /**
     * Get smart cooking parameters for a snack/beverage recipe.
     */
    fun getSmartCookingParams(recipe: SnackBeverageRecipe): Map<String, Any> {
        return recipe.smartApplianceParams
    }

    // ──────────────────────────────────────────────
    // Health-First Proactive Reminder System
    // ──────────────────────────────────────────────

    /**
     * Get proactive health reminders based on time of day.
     * WORSHIP-FIRST: These are gated and won't fire until Brahmamuhurta worship is complete.
     */
    fun getProactiveHealthReminders(
        currentHour: Int,
        currentMinute: Int,
        isWorshipComplete: Boolean,
        lastWaterIntakeTime: Long = 0L,
        lastMealTime: Long = 0L,
        ownerName: String = "सर"
    ): List<HealthReminder> {
        val reminders = mutableListOf<HealthReminder>()
        val currentMinuteOfDay = currentHour * 60 + currentMinute

        // WORSHIP-FIRST GATE: No food/snack/beverage reminders until Brahmamuhurta worship is complete
        if (!isWorshipComplete) {
            reminders.add(
                HealthReminder(
                    type = "WORSHIP_GATE",
                    title = "पूजन प्राथमिकता",
                    message = "🙏 राधे-राधे $ownerName! पहले भगवान, फिर हम। कृपया पहले ब्रह्ममुहूर्त पूजन पूरा करें। पूजन के बाद ही भोजन और पेय संबंधी सलाह दी जा सकती है।",
                    suggestedTime = "4:00 AM से पहले",
                    priority = 10,
                    isWorshipGated = false
                )
            )
            return reminders
        }

        // 1. Morning Hydration Reminder (6:00 AM - 8:00 AM)
        if (currentMinuteOfDay in 360..480) {
            val timeSinceLastWater = if (lastWaterIntakeTime > 0)
                (System.currentTimeMillis() - lastWaterIntakeTime) / 60000 else 999
            if (timeSinceLastWater > 60) {
                reminders.add(
                    HealthReminder(
                        type = "HYDRATION",
                        title = "🌅 सुप्रभात! पानी पीजिए",
                        message = "🌅 राधे-राधे $ownerName! सुबह उठते ही 1-2 गिलास गुनगुना पानी पीना सेहत के लिए अमृत है। यह पाचन तंत्र को सक्रिय करता है और दिन की शुरुआत ताज़गी से होती है।",
                        suggestedTime = "6:00 AM - 8:00 AM",
                        priority = 8,
                        isWorshipGated = true
                    )
                )
            }
        }

        // 2. Breakfast Reminder (7:00 AM - 9:00 AM)
        if (currentMinuteOfDay in 420..540) {
            val timeSinceLastMeal = if (lastMealTime > 0)
                (System.currentTimeMillis() - lastMealTime) / 60000 else 999
            if (timeSinceLastMeal > 180) {
                reminders.add(
                    HealthReminder(
                        type = "BREAKFAST",
                        title = "🍳 नाश्ते का समय",
                        message = "🍳 राधे-राधे $ownerName! नाश्ता करने का समय हो गया है। हल्का और पौष्टिक नाश्ता जैसे पोहा, उपमा, पराठा, या दलिया ले सकते हैं। खाली पेट लंबे समय तक न रहें — यह मेटाबॉलिज़्म के लिए अच्छा नहीं है।",
                        suggestedTime = "7:00 AM - 9:00 AM",
                        priority = 7,
                        isWorshipGated = true
                    )
                )
            }
        }

        // 3. Morning Tea/Coffee Break (9:00 AM - 10:30 AM)
        if (currentMinuteOfDay in 540..630) {
            reminders.add(
                HealthReminder(
                    type = "TEA_BREAK",
                    title = "☕ चाय या कॉफी का समय",
                    message = "☕ राधे-राधे $ownerName! सुबह की चाय या कॉफी का आनंद लेने का सही समय है। मसाला चाय बना लें? या फ़िल्टर कॉफ़ी पसंद करेंगे? मैं दोनों बनाने में मदद कर सकता हूँ!",
                    suggestedTime = "9:00 AM - 10:30 AM",
                    priority = 5,
                    isWorshipGated = true
                )
            )
        }

        // 4. Mid-Day Hydration (11:00 AM - 12:00 PM)
        if (currentMinuteOfDay in 660..720) {
            reminders.add(
                HealthReminder(
                    type = "HYDRATION",
                    title = "💧 पानी पीना न भूलें",
                    message = "💧 राधे-राधे $ownerName! दोपहर होने वाली है, कृपया पानी पी लें। दिन में कम से कम 8-10 गिलास पानी पीना सेहत के लिए ज़रूरी है। नींबू पानी या नारियल पानी भी ले सकते हैं।",
                    suggestedTime = "11:00 AM - 12:00 PM",
                    priority = 6,
                    isWorshipGated = true
                )
            )
        }

        // 5. Afternoon Tea/Snack Break (3:00 PM - 5:00 PM)
        if (currentMinuteOfDay in 900..1020) {
            reminders.add(
                HealthReminder(
                    type = "SNACK_TIME",
                    title = "🍪 शाम की चाय-नाश्ता",
                    message = "🍪 राधे-राधे $ownerName! शाम की चाय के साथ कुछ हल्का नाश्ता कर लीजिए। समोसे, पकोड़े, या भेल पूरी का मन हो तो बता इए — मैं रेसिपी बता सकता हूँ या बना सकता हूँ!",
                    suggestedTime = "3:00 PM - 5:00 PM",
                    priority = 5,
                    isWorshipGated = true
                )
            )
        }

        // 6. Evening Hydration (5:00 PM - 6:30 PM)
        if (currentMinuteOfDay in 1020..1110) {
            reminders.add(
                HealthReminder(
                    type = "HYDRATION",
                    title = "🥤 शाम का पेय",
                    message = "🥤 राधे-राधे $ownerName! शाम को कुछ ताज़गी भरा पेय ले सकते हैं — जलजीरा, नींबू पानी, या छाछ। यह शरीर को हाइड्रेट रखने और पाचन में मदद करता है।",
                    suggestedTime = "5:00 PM - 6:30 PM",
                    priority = 4,
                    isWorshipGated = true
                )
            )
        }

        return reminders
    }

    /**
     * Get scheduled health reminders for display.
     * Returns a formatted string with all reminders for the day.
     */
    fun getDailyHealthSchedule(isWorshipComplete: Boolean): String {
        if (!isWorshipComplete) {
            return "🙏 राधे-राधे! पहले भगवान, फिर हम — कृपया पहले ब्रह्ममुहूर्त पूजन पूरा करें। पूजन के बाद ही स्वास्थ्य संबंधी सुझाव दिए जाएंगे।"
        }

        return """
🌅 **आपकी दैनिक स्वास्थ्य दिनचर्या (Daily Health Schedule):** 🙏

🌄 **6:00 AM - 8:00 AM** — सुबह उठते ही 1-2 गिलास गुनगुना पानी पीएं
🍳 **7:00 AM - 9:00 AM** — नाश्ता करें (पोहा, पराठा, दलिया, उपमा)
☕ **9:00 AM - 10:30 AM** — चाय या कॉफी का आनंद लें
💧 **11:00 AM - 12:00 PM** — पानी पीएं, हाइड्रेटेड रहें
🍽️ **12:30 PM - 2:00 PM** — दोपहर का भोजन
🍪 **3:00 PM - 5:00 PM** — शाम की चाय और हल्का नाश्ता
🥤 **5:00 PM - 6:30 PM** — ताज़गी भरा पेय (जलजीरा, नींबू पानी)
🍲 **7:00 PM - 8:30 PM** — रात का भोजन (हल्का)
💧 **9:00 PM - 10:00 PM** — सोने से पहले पानी पीएं

💡 **स्वास्थ्य सुझाव:**
• दिन में कम से कम 8-10 गिलास पानी पीएं
• चाय/कॉफी दिन में 2-3 कप से अधिक न लें
• तला-भुना कम, फल-सब्ज़ियाँ अधिक लें
• रात का भोजन हल्का और सोने से 2-3 घंटे पहले करें
""".trimIndent()
    }

    // ──────────────────────────────────────────────
    // Main Query Handler
    // ──────────────────────────────────────────────

    /**
     * Handle a snack, beverage, or street food query.
     */
    fun handleSnackBeverageQuery(query: String): String {
        val lower = query.lowercase(Locale.ROOT)

        return when {
            // Health / Reminder / Schedule queries
            lower.contains("health schedule") || lower.contains("daily routine") || lower.contains("दिनचर्या") ||
            lower.contains("health reminder") || lower.contains("remind") || lower.contains("याद दिलाना") ||
            lower.contains("hydrate") || lower.contains("पानी") || lower.contains("water") ||
            lower.contains("breakfast") || lower.contains("नाश्ता") || lower.contains("diet") || lower.contains("dietary") ||
            (lower.contains("health") && (lower.contains("tip") || lower.contains("suggestion") || lower.contains("care"))) -> {
                "🙏 राधे-राधे! आपकी सेहत मेरी प्राथमिकता है।\n\n" +
                getDailyHealthSchedule(true) + "\n\n" +
                "क्या आप किसी विशेष पेय या नाश्ते की रेसिपी जानना चाहेंगे? मसाला चाई, फ़िल्टर कॉफ़ी, पानी पूरी, या कुछ और?"
            }

            // Smart appliance query
            lower.contains("appliance") || lower.contains("device") || lower.contains("smart kitchen") || lower.contains("hardware") ||
            (lower.contains("smart") && (lower.contains("kettle") || lower.contains("coffee") || lower.contains("cook"))) -> {
                val appliances = getAvailableAppliances()
                if (appliances.isEmpty() || appliances.all { !it.isConnected }) {
                    "🙏 राधे-राधे!\n\n" +
                    "स्मार्ट किचन उपकरणों की स्थिति:\n" +
                    "🚫 कोई कनेक्टेड उपकरण नहीं मिला।\n\n" +
                    "मैं आपको स्टेप-बाय-स्टेप विधि से मार्गदर्शन कर सकता हूँ। " +
                    "जब स्मार्ट हार्डवेयर कनेक्ट होगा, तो ऑटोनॉमस तैयारी उपलब्ध होगी।\n" +
                    "उपलब्ध उपकरण: ${appliances.joinToString { "${it.applianceName} (${it.applianceType})" }}"
                } else {
                    "🙏 राधे-राधे! स्मार्ट किचन devices ready हैं। " +
                    appliances.filter { it.isConnected }.joinToString(", ") { it.applianceName } +
                    " कनेक्टेड हैं। मैं चाय, कॉफी, या नाश्ता बनाने के लिए तैयार हूँ!"
                }
            }

            // Beverage-specific query
            lower.contains("chai") || lower.contains("tea") || lower.contains("चाय") ||
            lower.contains("coffee") || lower.contains("कॉफी") ||
            lower.contains("lassi") || lower.contains("buttermilk") || lower.contains("chaas") ||
            lower.contains("jaljeera") || lower.contains("sharbat") || lower.contains("nimbu") ||
            lower.contains("lemonade") || lower.contains("aam panna") || lower.contains("cold drink") ||
            lower.contains("milk") || lower.contains("smoothie") || lower.contains("milkshake") ||
            lower.contains("beverage") || lower.contains("पेय") || lower.contains("drink") || lower.contains("pina") ||
            (lower.contains("पीना") || lower.contains("पिलाओ") || lower.contains("पिला")) -> {
                val recipe = getSnackBeverageRecipe(query)
                formatSnackResponse(recipe)
            }

            // Street food specific
            lower.contains("pani puri") || lower.contains("golgappa") || lower.contains("gup chup") ||
            lower.contains("bhel") || lower.contains("pav bhaji") || lower.contains("momo") ||
            lower.contains("roll") || lower.contains("kathi") || lower.contains("vada pav") ||
            lower.contains("dabeli") || lower.contains("misal") || lower.contains("chaat") ||
            lower.contains("tikki") || lower.contains("samosa") || lower.contains("kachori") ||
            lower.contains("pakora") || lower.contains("bhajiya") || lower.contains("dosa") ||
            lower.contains("idli") || lower.contains("vada") || lower.contains("uttapam") ||
            lower.contains("appam") || lower.contains("dhokla") || lower.contains("khandvi") ||
            lower.contains("fafda") || lower.contains("cutlet") || lower.contains("farsan") ||
            lower.contains("गली") || lower.contains("स्ट्रीट") || lower.contains("street food") ||
            lower.contains("नाश्ता") && (lower.contains("रेसिपी") || lower.contains("बनाना") || lower.contains("कैसे")) -> {
                val recipe = getSnackBeverageRecipe(query)
                formatSnackResponse(recipe)
            }

            // Dessert snack specific
            lower.contains("jalebi") || lower.contains("gulab jamun") || lower.contains("rasgulla") ||
            lower.contains("kulfi") || lower.contains("rabri") || lower.contains("malpua") ||
            lower.contains("halwa") || lower.contains("kheer") || lower.contains("ice cream") ||
            lower.contains("मिठाई") || lower.contains("dessert") || lower.contains("मीठा") -> {
                val recipe = getSnackBeverageRecipe(query)
                formatSnackResponse(recipe)
            }

            // General snack recipe query
            lower.contains("snack") || lower.contains("नाश्ता") || lower.contains("रेसिपी") ||
            lower.contains("how to make") || lower.contains("कैसे बनाये") || lower.contains("kaise banaye") ||
            lower.contains("recipe") || lower.contains("व्यंजन") || lower.contains("बनाओ") ||
            lower.contains("cook") || lower.contains("पकाना") || lower.contains("make") && (lower.contains("chai") || lower.contains("snack") || lower.contains("food")) -> {
                val recipe = getSnackBeverageRecipe(query)
                formatSnackResponse(recipe)
            }

            // Default
            else -> {
                "🙏 राधे-राधे! मैं आपका स्नैक्स, बेवरेज और स्ट्रीट फूड विशेषज्ञ हूँ! 🍵🍲\n\n" +
                "मेरे पास हज़ारों रेसिपी हैं:\n\n" +
                "🍵 **चाय वेरिएंट:** मसाला चाय, अदरक चाय, तुलसी चाय, ग्रीन टी, कश्मीरी चाय\n" +
                "☕ **कॉफ़ी वेरिएंट:** फ़िल्टर कॉफ़ी, कोल्ड ब्रू, कोल्ड कॉफ़ी, इंस्टेंट कॉफ़ी\n" +
                "🥛 **पेय:** लस्सी, छाछ, जलजीरा, शर्बत, नींबू पानी, आम पन्ना, नारियल पानी\n" +
                "🍲 **स्ट्रीट फूड:** पानी पूरी, भेल पूरी, पाव भाजी, मोमोज़, काठी रोल, वड़ा पाव\n" +
                "🍿 **स्नैक्स:** समोसा, कचौड़ी, पकोड़ा, ढोकला, खांडवी, फाफड़ा\n" +
                "🍨 **डेज़र्ट:** जलेबी, गुलाब जामुन, कुल्फी, रसगुल्ला, राबड़ी, मालपुआ\n\n" +
                "💪 **स्वास्थ्य सेवा:** मैं आपकी दैनिक दिनचर्या के अनुसार पानी, नाश्ता, चाय और नाश्ते के लिए प्रोएक्टिव रिमाइंडर भी दे सकता हूँ।\n\n" +
                "बस नाम बताएं! क्या बनाना है? (Just tell me what you'd like to make or drink!)"
            }
        }
    }

    private fun formatSnackResponse(recipe: SnackBeverageRecipe): String {
        val categoryEmoji = when {
            recipe.isBeverage -> "🍵"
            recipe.isStreetFood -> "🍲"
            recipe.category == "Dessert" || recipe.subCategory == "Dessert Snack" -> "🍨"
            else -> "🍿"
        }

        val tempEmoji = when (recipe.temperature.lowercase()) {
            "cold" -> "🧊"
            "hot" -> "🔥"
            else -> ""
        }

        return "🙏 राधे-राधे!\n\n" +
        "$categoryEmoji **${recipe.name}** $tempEmoji\n" +
        "• श्रेणी (Category): ${recipe.subCategory.ifBlank { recipe.category }}\n" +
        "• व्यंजन (Cuisine): ${recipe.cuisine}\n" +
        "• तैयारी (Preparation): ${recipe.preparationTime} मिनट\n" +
        "• पकाने का समय (Cooking): ${recipe.cookingTime} मिनट\n" +
        "• कठिनाई (Difficulty): ${recipe.difficulty}\n" +
        "• मसाला स्तर (Spice): ${recipe.spiceLevel}\n" +
        "• सर्विंग (Servings): ${recipe.servings}\n\n" +
        "📝 **सामग्री (Ingredients):**\n" +
        recipe.ingredients.joinToString("\n") { "• $it" } + "\n\n" +
        "📋 **विधि (Steps):**\n" +
        recipe.steps.joinToString("\n") { "$it" } + "\n\n" +
        "💡 **प्रो टिप्स:**\n${recipe.proTips}\n\n" +
        "🥗 **पोषण जानकारी (Nutritional Info):**\n${recipe.nutritionalInfo}\n\n" +
        "🏷️ **हेल्थ टैग्स:** ${recipe.healthTags.joinToString(", ")}\n\n" +
        "🙏 राधे-राधे! बोन एपेटिट! आनंद लीजिए! 😊"
    }
}
