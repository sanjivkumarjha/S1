package com.example.domain

import android.content.Context
import com.example.data.local.AppDatabase
import java.util.Locale

/**
 * MODULE 19: PURE VEGETARIAN INDIAN MASTERCHEF & SMART KITCHEN EXECUTION ENGINE v27.0
 *
 * FEATURES:
 * - Famous Indian vegetarian recipes (North Indian, South Indian, Gujarati, Bengali, Rajasthani, Maharashtrian, Punjabi)
 * - Hybrid assistance: textual/voice guidance OR autonomous smart hardware execution
 * - Integrated with existing non-veg culinary module for comprehensive MasterChef capability
 * - Step-by-step professional culinary guidance with pro-tips
 */
class VegIndianMasterChefEngine(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)

    // ──────────────────────────────────────────────
    // Data Models
    // ──────────────────────────────────────────────

    data class VegRecipe(
        val name: String = "",
        val cuisine: String = "North Indian",
        val category: String = "Main Course", // Main Course, Starter, Snack, Dessert, Bread, Rice, Dal, Sabzi, Chaat, Drink
        val preparationTime: Int = 0,          // minutes
        val cookingTime: Int = 0,
        val totalTime: Int = 0,
        val difficulty: String = "Medium",     // Easy, Medium, Hard
        val spiceLevel: String = "Medium",
        val servings: Int = 4,
        val ingredients: List<String> = emptyList(),
        val steps: List<String> = emptyList(),
        val proTips: String = "",
        val nutritionalInfo: String = "",
        val smartApplianceMode: String = "",   // "STOVE", "INDUCTION", "OVEN", "AIR_FRYER", "MICROWAVE", "PRESSURE_COOKER", "ROBOT_COOK", "MANUAL"
        val smartApplianceParams: Map<String, Any> = emptyMap()
    )

    data class SmartKitchenHardware(
        val isConnected: Boolean = false,
        val applianceName: String = "",
        val applianceType: String = "",
        val protocol: String = "wifi",
        val ipAddress: String = ""
    )

    // ──────────────────────────────────────────────
    // Main Recipe Router
    // ──────────────────────────────────────────────

    /**
     * Get recipe for a given Indian vegetarian dish.
     */
    fun getVegRecipe(dishName: String, cuisineHint: String = "North Indian"): VegRecipe {
        val lower = dishName.lowercase(Locale.ROOT)

        return when {
            // ─── Paneer Dishes ─────────────────────
            (lower.contains("paneer") && lower.contains("butter")) || lower.contains("butter paneer") || lower.contains("paneer makhani") ->
                getButterPaneerRecipe()
            lower.contains("paneer") && (lower.contains("tikka") || lower.contains("grill")) && !lower.contains("masala") ->
                getPaneerTikkaRecipe()
            lower.contains("paneer") && lower.contains("bhurji") || lower.contains("paneer bhurji") ->
                getPaneerBhurjiRecipe()
            lower.contains("paneer") && (lower.contains("korma") || lower.contains("pasanda") || lower.contains("do pyaza") || lower.contains("lababdar") || lower.contains("kathi") || lower.contains("roll")) ->
                getButterPaneerRecipe().copy(
                    name = "Paneer Special (Creamy Gravy)",
                    preparationTime = 20,
                    cookingTime = 25
                )
            lower.contains("shahi paneer") || lower.contains("shahi panir") ->
                getShahiPaneerRecipe()
            lower.contains("kadai paneer") || lower.contains("kadhai paneer") ->
                getKadaiPaneerRecipe()
            lower.contains("matar paneer") || lower.contains("mutter paneer") || lower.contains("matar panir") ->
                getMatarPaneerRecipe()
            lower.contains("palak paneer") || lower.contains("saag paneer") ->
                getPalakPaneerRecipe()
            lower.contains("paneer") && lower.contains("chilli") || lower.contains("chilli paneer") ->
                getChilliPaneerRecipe()

            // ─── Dal / Lentils ─────────────────────
            lower.contains("dal") && (lower.contains("makhani") || lower.contains("daal makhani")) ->
                getDalMakhaniRecipe()
            lower.contains("dal") && (lower.contains("tadka") || lower.contains("palak") || lower.contains("dhokli") || lower.contains("baati") || lower.contains("bati")) ->
                getDalTadkaRecipe().copy(
                    name = "Special Indian Dal",
                    preparationTime = 15,
                    cookingTime = 25
                )
            lower.contains("sambhar") || lower.contains("sambar") ->
                getDalTadkaRecipe().copy(
                    name = "Sambhar (South Indian Lentil Stew)",
                    cuisine = "South Indian",
                    preparationTime = 20,
                    cookingTime = 30,
                    spiceLevel = "Medium"
                )
            lower.contains("rasam") ->
                getDalTadkaRecipe().copy(
                    name = "Rasam (South Indian Pepper Soup)",
                    cuisine = "South Indian",
                    preparationTime = 10,
                    cookingTime = 15,
                    spiceLevel = "Medium-Hot"
                )

            // ─── Sabzi / Vegetable Curries ─────────
            lower.contains("chole") || lower.contains("chana masala") || (lower.contains("chickpea") && lower.contains("curry")) ->
                getCholeMasalaRecipe()
            lower.contains("rajma") || lower.contains("kidney bean") ->
                getRajmaRecipe()
            lower.contains("bhindi") || lower.contains("okra") || lower.contains("ladyfinger") ->
                getBhindiMasalaRecipe()
            lower.contains("aloo") && lower.contains("gobi") || lower.contains("aloo gobi") ->
                getAlooGobiRecipe()
            lower.contains("baingan") || lower.contains("bharta") || lower.contains("baigan bharta") ->
                getBainganBhartaRecipe()
            lower.contains("malai") && lower.contains("kofta") || lower.contains("malai kofta") ->
                getMalaiKoftaRecipe()
            lower.contains("gatte") || lower.contains("shahi aloo") || lower.contains("dum aloo") ->
                getAlooGobiRecipe().copy(
                    name = "Special Aloo / Gatte Sabzi",
                    preparationTime = 15,
                    cookingTime = 20
                )
            lower.contains("veg") && lower.contains("korma") || lower.contains("navratan") ->
                getMalaiKoftaRecipe().copy(
                    name = "Vegetable / Navratan Korma",
                    preparationTime = 25,
                    cookingTime = 25
                )

            // ─── South Indian ──────────────────────
            lower.contains("masala dosa") || lower.contains("masala dose") ->
                getMasalaDosaRecipe()
            lower.contains("dosa") && !lower.contains("masala") ->
                getPlainDosaRecipe()
            lower.contains("idli") ->
                getIdliRecipe()
            lower.contains("vada") || lower.contains("medu vada") ->
                getMeduVadaRecipe()
            lower.contains("uttapam") || lower.contains("ootapam") ->
                getMasalaDosaRecipe().copy(
                    name = "Uttapam (Thick Vegetable Pancake)",
                    preparationTime = 240,
                    cookingTime = 20
                )
            lower.contains("lemon rice") || lower.contains("coconut rice") || lower.contains("tomato rice") || lower.contains("pongal") ->
                getKhichdiRecipe().copy(
                    name = "South Indian Rice Specialty",
                    cuisine = "South Indian",
                    preparationTime = 15,
                    cookingTime = 20
                )

            // ─── Gujarati ──────────────────────────
            lower.contains("dhokla") || lower.contains("khaman") ->
                getDhoklaRecipe()
            lower.contains("thepla") || lower.contains("methi thepla") ->
                getTheplaRecipe()
            lower.contains("khandvi") ->
                getDhoklaRecipe().copy(
                    name = "Khandvi (Gujarati Gram Flour Rolls)",
                    preparationTime = 20,
                    cookingTime = 20
                )
            lower.contains("undhiyu") || lower.contains("fafda") || lower.contains("handvo") ->
                getDhoklaRecipe().copy(
                    name = "Gujarati Specialty Dish",
                    cuisine = "Gujarati",
                    preparationTime = 25,
                    cookingTime = 25
                )

            // ─── Bengali ───────────────────────────
            lower.contains("shukto") || lower.contains("shuktono") ->
                getShuktoRecipe()
            lower.contains("posto") || lower.contains("mochar") || lower.contains("chingri") ->
                getShuktoRecipe().copy(
                    name = "Bengali Vegetarian Specialty",
                    cuisine = "Bengali",
                    preparationTime = 20,
                    cookingTime = 25
                )

            // ─── Rajasthani ────────────────────────
            lower.contains("gatte") && lower.contains("pulao") || lower.contains("ker sangri") || lower.contains("bhakri") ->
                getVegBiryaniRecipe().copy(
                    name = "Rajasthani Specialty Dish",
                    cuisine = "Rajasthani",
                    preparationTime = 20,
                    cookingTime = 30
                )

            // ─── Breads ────────────────────────────
            lower.contains("roti") || lower.contains("chapati") || lower.contains("phulka") ->
                getRotiRecipe()
            lower.contains("paratha") && lower.contains("aloo") || lower.contains("aloo paratha") ->
                getAlooParathaRecipe()
            lower.contains("paratha") && (lower.contains("gobi") || lower.contains("paneer") || lower.contains("mooli")) ->
                getAlooParathaRecipe().copy(
                    name = "Stuffed Paratha",
                    preparationTime = 25,
                    cookingTime = 20
                )
            lower.contains("naan") -> getRotiRecipe().copy(
                name = "Naan Bread",
                cuisine = "North Indian (Tandoori)",
                preparationTime = 30,
                cookingTime = 10,
                difficulty = "Hard"
            )
            lower.contains("puri") || lower.contains("bhatura") || lower.contains("puri bhaji") ->
                getAlooParathaRecipe().copy(
                    name = "Puri / Bhatura (Fried Bread)",
                    preparationTime = 15,
                    cookingTime = 15,
                    steps = listOf("Knead dough with flour, curd, and ghee.", "Roll into small circles.", "Deep fry in hot oil until puffed.", "Serve hot with chole or aloo bhaji.")
                )

            // ─── Rice / Biryani ────────────────────
            lower.contains("veg biryani") || lower.contains("vegetable biryani") || lower.contains("veg biriyani") ->
                getVegBiryaniRecipe()
            lower.contains("pulao") || lower.contains("pulav") || lower.contains("jeera rice") || lower.contains("ghee rice") ->
                getVegBiryaniRecipe().copy(
                    name = "Veg Pulao / Saffron Rice",
                    preparationTime = 15,
                    cookingTime = 20,
                    difficulty = "Easy",
                    smartApplianceMode = "PRESSURE_COOKER",
                    smartApplianceParams = mapOf("mode" to "RICE", "pressure" to "HIGH", "durationMin" to 12)
                )
            lower.contains("khichdi") || lower.contains("khichadi") || lower.contains("khichuri") ->
                getKhichdiRecipe()
            lower.contains("bisibele bath") || lower.contains("bisi bele bath") ->
                getKhichdiRecipe().copy(
                    name = "Bisibele Bath (South Indian Rice & Lentil Dish)",
                    cuisine = "South Indian (Karnataka)",
                    preparationTime = 20,
                    cookingTime = 30
                )

            // ─── Chaat / Snacks ────────────────────
            lower.contains("golgappa") || lower.contains("pani puri") || lower.contains("gup chup") ->
                getSamosaRecipe().copy(
                    name = "Pani Puri / Golgappa",
                    preparationTime = 30,
                    cookingTime = 10,
                    difficulty = "Medium"
                )
            lower.contains("papdi chaat") || lower.contains("papri chaat") || lower.contains("bhel puri") || lower.contains("bhel") ->
                getSamosaRecipe().copy(
                    name = "Chaat (Papdi / Bhel Puri)",
                    preparationTime = 20,
                    cookingTime = 5,
                    difficulty = "Easy"
                )
            lower.contains("samosa") ->
                getSamosaRecipe()
            lower.contains("kachori") || lower.contains("pakora") || lower.contains("pakoda") || lower.contains("bhajiya") ->
                getSamosaRecipe().copy(
                    name = "Kachori / Pakora (Fried Snack)",
                    preparationTime = 25,
                    cookingTime = 15
                )
            lower.contains("aloo tikki") || lower.contains("aloo chaat") ->
                getAlooParathaRecipe().copy(
                    name = "Aloo Tikki (Potato Cutlet)",
                    preparationTime = 20,
                    cookingTime = 15,
                    difficulty = "Easy"
                )

            // ─── Desserts ───────────────────────────
            lower.contains("gulab jamun") || lower.contains("gulab jamoon") ->
                getGulabJamunRecipe()
            lower.contains("jalebi") || lower.contains("jilebi") ->
                getJalebiRecipe()
            lower.contains("rasgulla") || lower.contains("rosogolla") ->
                getGulabJamunRecipe().copy(
                    name = "Rasgulla (Bengali Syrupy Cheese Balls)",
                    cuisine = "Bengali",
                    preparationTime = 30,
                    cookingTime = 20
                )
            lower.contains("kheer") || lower.contains("payasam") || lower.contains("firni") ->
                getKheerRecipe()
            lower.contains("halwa") || lower.contains("gajar halwa") || lower.contains("carrot halwa") ->
                getGajarHalwaRecipe()
            lower.contains("ladoo") || lower.contains("laddoo") || lower.contains("motichoor") || lower.contains("barfi") || lower.contains("burfi") || lower.contains("kulfi") ->
                getGulabJamunRecipe().copy(
                    name = "Indian Sweet (Ladoo / Barfi / Kulfi)",
                    preparationTime = 25,
                    cookingTime = 20
                )

            // ─── Drinks ────────────────────────────
            lower.contains("lassi") || lower.contains("sweet lassi") || lower.contains("mango lassi") ->
                getLassiRecipe()
            lower.contains("chai") || lower.contains("masala chai") || lower.contains("tea") ->
                getLassiRecipe().copy(
                    name = "Masala Chai (Indian Spiced Tea)",
                    preparationTime = 5,
                    cookingTime = 10,
                    ingredients = listOf(
                        "2 cups Water",
                        "1 cup Milk",
                        "2 tsp Tea Leaves (Assam/Darjeeling)",
                        "2-3 Green Cardamoms, crushed",
                        "1-inch Ginger, grated",
                        "1 Cinnamon stick",
                        "2-3 Cloves",
                        "1 Black Peppercorn",
                        "Sugar to taste"
                    ),
                    steps = listOf(
                        "STEP 1: Boil 2 cups water with ginger, cardamom, cinnamon, cloves, and peppercorn for 2 minutes.",
                        "STEP 2: Add tea leaves. Boil for 1-2 minutes (longer = stronger).",
                        "STEP 3: Add milk. Bring to a rolling boil.",
                        "STEP 4: Reduce heat, simmer for 2 minutes.",
                        "STEP 5: Add sugar to taste. Strain and serve hot."
                    )
                )
            lower.contains("jaljeera") || lower.contains("jal jira") ->
                getLassiRecipe().copy(
                    name = "Jaljeera (Spiced Cumin Cooler)",
                    preparationTime = 10,
                    cookingTime = 0,
                    spiceLevel = "Medium-Hot"
                )

            else -> getGenericVegGuidance(dishName)
        }
    }

    // ──────────────────────────────────────────────
    // Recipe Database: Paneer Dishes
    // ──────────────────────────────────────────────

    private fun getButterPaneerRecipe(): VegRecipe = VegRecipe(
        name = "Butter Paneer (Paneer Makhani)",
        cuisine = "North Indian (Punjabi)",
        category = "Main Course",
        preparationTime = 15,
        cookingTime = 25,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 4,
        ingredients = listOf(
            "250g Paneer, cubed",
            "2 tbsp Butter",
            "1 tbsp Oil",
            "1 large Onion, finely chopped",
            "3-4 cloves Garlic, minced",
            "1 inch Ginger, grated",
            "2-3 Green Cardamoms",
            "1-inch Cinnamon stick",
            "2-3 Cloves",
            "3 large Tomatoes, pureed",
            "¼ cup Cashew paste (10-12 cashews soaked & ground)",
            "1 tsp Kashmiri Red Chilli powder",
            "½ tsp Turmeric powder",
            "1 tsp Coriander powder",
            "½ tsp Garam Masala",
            "2 tbsp Fresh Cream",
            "1 tsp Sugar (optional, balances acidity)",
            "Salt to taste",
            "1 tsp Kasuri Methi (dried fenugreek leaves)",
            "2 tbsp Fresh Coriander, chopped for garnish"
        ),
        steps = listOf(
            "FIRST PREP: Soak 10-12 cashews in warm water for 15 minutes. Grind to a smooth paste.",
            "STEP 1 — TEMPER SPICES: Heat 1 tbsp butter + 1 tbsp oil in a heavy pan. Add cardamom, cinnamon, cloves. Sauté for 30 seconds until aromatic.",
            "STEP 2 — SAUTÉ AROMATICS: Add finely chopped onion, garlic, ginger. Sauté on medium heat until onions turn golden brown (5-6 minutes).",
            "STEP 3 — TOMATO BASE: Add tomato puree. Cook on medium heat until the mixture thickens and oil separates (8-10 minutes). Stir occasionally.",
            "STEP 4 — SPICE IT: Add red chilli powder, turmeric, coriander powder. Mix well and cook for 1 minute.",
            "STEP 5 — CASHEW CREAM: Add cashew paste + ½ cup warm water. Stir continuously. Simmer for 5 minutes until the gravy turns smooth and creamy.",
            "STEP 6 — PANEER TIME: Gently add paneer cubes. Simmer for 3-4 minutes. Do not overcook — paneer should remain soft.",
            "STEP 7 — FINISHING TOUCHES: Add fresh cream + sugar + salt. Crush kasuri methi between palms and sprinkle. Add garam masala. Stir gently.",
            "STEP 8 — REST: Let the curry rest for 5 minutes before serving. This allows flavours to meld.",
            "SERVE: Garnish with fresh cream swirl and coriander. Serve hot with butter naan, garlic naan, or jeera rice."
        ),
        proTips = "PRO TIPS: 🔸 Always use fresh, soft paneer — never fry it, just add directly to gravy. 🔸 For restaurant-style orange colour, add 2 tbsp tomato ketchup. 🔸 Kasuri Methi is the secret ingredient — crush it in your palms before adding to release aroma. 🔸 Let the gravy cool slightly before adding cream to prevent curdling. 🔸 For richer taste, replace water with milk while cooking gravy.",
        nutritionalInfo = "Approx: 380 cal/serving, 24g protein, 28g fat, 12g carbs",
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("power" to "Medium", "tempC" to 180, "durationMin" to 25)
    )

    private fun getPaneerTikkaRecipe(): VegRecipe = VegRecipe(
        name = "Paneer Tikka",
        cuisine = "North Indian (Punjabi)",
        category = "Starter",
        preparationTime = 20,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        ingredients = listOf(
            "250g Paneer, cut into 1-inch cubes",
            "1 cup Thick Curd (Greek yogurt preferred)",
            "1 tbsp Ginger-Garlic paste",
            "1 tsp Kashmiri Red Chilli powder",
            "½ tsp Turmeric powder",
            "1 tsp Cumin powder",
            "1 tsp Coriander powder",
            "½ tsp Garam Masala",
            "1 tbsp Lemon juice",
            "1 tbsp Mustard Oil (or any oil)",
            "1 tsp Chaat Masala",
            "Salt to taste",
            "1 medium Capsicum (bell pepper), cubed",
            "1 medium Onion, cubed",
            "Few Cherry Tomatoes",
            "Wooden skewers (soaked in water 30 min)"
        ),
        steps = listOf(
            "FIRST PREP: Soak wooden skewers in water for 30 minutes (prevents burning).",
            "STEP 1 — MARINADE: In a bowl, mix thick curd, ginger-garlic paste, red chilli powder, turmeric, cumin, coriander, garam masala, lemon juice, mustard oil, chaat masala, and salt. Whisk until smooth.",
            "STEP 2 — FIRST MARINATION: Add paneer cubes, capsicum cubes, onion petals. Gently mix with your hands, ensuring each piece is well coated. Cover and refrigerate for 30 minutes (minimum).",
            "STEP 3 — SKEWER: Thread onto skewers — paneer, capsicum, onion, paneer — alternating. Leave small gaps for even cooking.",
            "STEP 4 — COOKING OPTIONS:",
            "  • OVEN: Preheat to 200°C. Brush skewers with oil. Bake for 12-15 minutes, turning halfway.",
            "  • TANDOOR/CLAY OVEN: Cook at high heat for 8-10 minutes until charred spots appear.",
            "  • PAN/GRILL: Brush pan with oil. Grill on medium-high heat, turning every 2-3 minutes.",
            "  • AIR FRYER: 180°C for 10-12 minutes, shake halfway.",
            "STEP 5 — FINAL TOUCH: Once done, brush with melted butter. Sprinkle chaat masala and lemon juice.",
            "SERVE: Hot with green chutney (mint-coriander), tamarind chutney, and onion rings. Garnish with chaat masala."
        ),
        proTips = "PRO TIPS: 🔸 Hanging the yogurt in muslin cloth for 1 hour makes it thick and prevents dripping. 🔸 Add 1 tbsp besan (gram flour) to marinade for better coating. 🔸 Charring is good — slightly burnt spots add smoky flavour. 🔸 For authentic smoky flavour: place a hot coal in a steel bowl in the centre of the marinated paneer, add ghee, and cover for 2 minutes.",
        nutritionalInfo = "Approx: 280 cal/serving, 18g protein, 18g fat, 8g carbs",
        smartApplianceMode = "OVEN",
        smartApplianceParams = mapOf("temperatureC" to 200, "durationMin" to 15, "mode" to "GRILL")
    )

    private fun getShahiPaneerRecipe(): VegRecipe = VegRecipe(
        name = "Shahi Paneer",
        cuisine = "North Indian (Mughlai)",
        category = "Main Course",
        preparationTime = 20,
        cookingTime = 30,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 4,
        ingredients = listOf(
            "250g Paneer, cubed",
            "2 tbsp Ghee",
            "1 large Onion, thinly sliced",
            "1 tsp Ginger-Garlic paste",
            "½ cup Cashew nuts, soaked",
            "¼ cup Melon seeds (magaz), optional",
            "2 tbsp Fresh Cream",
            "1 tbsp Poppy seeds (khus khus), soaked",
            "1 tsp Turmeric powder",
            "1 tsp Red Chilli powder",
            "½ tsp Garam Masala",
            "1 tsp Sugar",
            "¼ cup Milk",
            "Salt to taste",
            "½ tsp Saffron strands soaked in 2 tbsp warm milk",
            "2 tbsp Fresh Coriander"
        ),
        steps = listOf(
            "PREP: Soak cashews, poppy seeds, and melon seeds (if using) in warm water for 20 minutes.",
            "STEP 1: Grind the soaked cashews, poppy seeds, and melon seeds into a smooth paste using milk/water.",
            "STEP 2: Heat ghee in a pan. Add sliced onions and sauté until golden brown.",
            "STEP 3: Add ginger-garlic paste and sauté until the raw smell disappears.",
            "STEP 4: Add turmeric, red chilli powder. Sauté for 30 seconds.",
            "STEP 5: Add the ground cashew-poppy seed paste. Stir continuously. Cook on low flame for 5-6 minutes.",
            "STEP 6: Add 1 cup warm water + salt + sugar. Simmer for 10 minutes until the gravy thickens and releases ghee.",
            "STEP 7: Gently add paneer cubes. Simmer for 3-4 minutes. Do not stir vigorously.",
            "STEP 8: Add fresh cream + saffron milk + garam masala. Stir gently and turn off heat.",
            "SERVE: Garnish with cream swirl and chopped coriander. Best served with naan or tandoori roti."
        ),
        proTips = "PRO TIPS: 🔸 Shahi means 'royal' — the richness comes from the cashew-cream base. Do not skimp. 🔸 Add a pinch of cardamom powder for that Mughlai aroma. 🔸 Saffron is optional but elevates the dish to restaurant quality. 🔸 For extra richness, add 1 tbsp khoya (mawa) along with cream.",
        nutritionalInfo = "Approx: 350 cal/serving, 16g protein, 26g fat, 14g carbs"
    )

    private fun getMatarPaneerRecipe(): VegRecipe = VegRecipe(
        name = "Matar Paneer (Mutter Paneer)",
        cuisine = "North Indian",
        category = "Main Course",
        preparationTime = 15,
        cookingTime = 25,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 4,
        ingredients = listOf(
            "250g Paneer, cubed",
            "1 cup Green Peas (fresh or frozen)",
            "2 tbsp Oil",
            "1 tsp Cumin seeds",
            "1 large Onion, finely chopped",
            "1 tsp Ginger-Garlic paste",
            "2 large Tomatoes, pureed",
            "1 tsp Turmeric powder",
            "1 tsp Red Chilli powder",
            "1 tsp Coriander powder",
            "½ tsp Garam Masala",
            "1 tsp Kasuri Methi, crushed",
            "Salt to taste",
            "2 tbsp Fresh Cream (optional)",
            "Fresh Coriander for garnish"
        ),
        steps = listOf(
            "STEP 1 — TEMPERING: Heat oil in a pan. Add cumin seeds. Let them crackle.",
            "STEP 2 — ONION BASE: Add finely chopped onion. Sauté until golden brown (5-6 minutes).",
            "STEP 3: Add ginger-garlic paste. Sauté for 1 minute until raw smell disappears.",
            "STEP 4 — SPICES: Add turmeric, red chilli powder, coriander powder. Mix well. Cook for 30 seconds.",
            "STEP 5 — TOMATO: Add tomato puree. Cook until the mixture thickens and oil separates (7-8 minutes).",
            "STEP 6 — PEAS: Add green peas + ½ cup water + salt. Cover and cook for 8-10 minutes until peas are tender.",
            "STEP 7 — PANEER: Add paneer cubes. Simmer for 3-4 minutes.",
            "STEP 8 — FINISH: Add crushed kasuri methi + garam masala + fresh cream (optional). Stir gently.",
            "SERVE: Garnish with coriander. Serve hot with roti, naan, or rice."
        ),
        proTips = "PRO TIPS: 🔸 For frozen peas, add them directly without thawing. 🔸 If using fresh peas, boil them separately before adding. 🔸 Paneer should be added last to maintain soft texture. 🔸 A splash of cream or milk at the end makes the gravy rich and silky.",
        nutritionalInfo = "Approx: 290 cal/serving, 14g protein, 18g fat, 18g carbs"
    )

    private fun getKadaiPaneerRecipe(): VegRecipe = VegRecipe(
        name = "Kadai Paneer",
        cuisine = "North Indian (Punjabi)",
        category = "Main Course",
        preparationTime = 15,
        cookingTime = 20,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 4,
        ingredients = listOf(
            "250g Paneer, cubed",
            "1 large Capsicum (bell pepper), cubed",
            "1 large Onion, cubed",
            "2 large Tomatoes, diced",
            "2 tbsp Oil",
            "1 tsp Cumin seeds",
            "1 tbsp Ginger, julienned",
            "2-3 Green Chillies, slit",
            "2 tsp Kadai Masala (see pro tips for homemade)",
            "1 tsp Red Chilli powder",
            "½ tsp Turmeric powder",
            "1 tsp Coriander powder",
            "½ tsp Garam Masala",
            "1 tsp Kasuri Methi, crushed",
            "Salt to taste",
            "Fresh Coriander for garnish"
        ),
        steps = listOf(
            "HOMEMADE KADAI MASALA: Dry roast 1 tbsp coriander seeds + 1 tsp cumin seeds + 5-6 black peppercorns + 2 dry red chillies + 1 black cardamom. Grind to powder.",
            "STEP 1: Heat oil in a kadai (wok). Add cumin seeds. Let them crackle.",
            "STEP 2: Add ginger juliennes and green chillies. Sauté for 30 seconds.",
            "STEP 3: Add onion cubes. Sauté on high heat for 2 minutes — onions should remain crunchy.",
            "STEP 4: Add capsicum cubes. Sauté for 1 minute. Remove vegetables and set aside.",
            "STEP 5 — GRAVY: In the same pan, add diced tomatoes. Cook until soft and mushy (4-5 minutes).",
            "STEP 6 — SPICES: Add kadai masala, red chilli powder, turmeric, coriander powder. Cook for 1 minute.",
            "STEP 7: Return the sautéed onions and capsicum to the pan. Add ¼ cup water + salt.",
            "STEP 8 — PANEER: Add paneer cubes + garam masala + crushed kasuri methi. Toss gently on high heat for 2 minutes.",
            "SERVE: Garnish with ginger juliennes and coriander leaves. Serve immediately with naan or paratha."
        ),
        proTips = "PRO TIPS: 🔸 Kadai masala is the heart of this dish — make your own fresh for best results. 🔸 Onions and capsicum should be crunchy, not overcooked. 🔸 Cook on high heat throughout for the authentic dhaba-style taste. 🔸 Add 1 tbsp tomato ketchup for tangy richness.",
        nutritionalInfo = "Approx: 310 cal/serving, 15g protein, 20g fat, 16g carbs",
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("power" to "HIGH", "tempC" to 200, "durationMin" to 20)
    )

    private fun getPalakPaneerRecipe(): VegRecipe = VegRecipe(
        name = "Palak Paneer",
        cuisine = "North Indian (Punjabi)",
        category = "Main Course",
        preparationTime = 20,
        cookingTime = 25,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 4,
        ingredients = listOf(
            "250g Paneer, cubed",
            "500g (2 bunches) Fresh Spinach (Palak)",
            "1 large Onion, finely chopped",
            "1 tsp Ginger-Garlic paste",
            "2 Green Chillies",
            "2 tbsp Oil / Ghee",
            "1 tsp Cumin seeds",
            "1 tsp Turmeric powder",
            "½ tsp Red Chilli powder",
            "1 tsp Coriander powder",
            "½ tsp Garam Masala",
            "1 tbsp Fresh Cream",
            "1 tsp Kasuri Methi, crushed",
            "Salt to taste",
            "1 tbsp Lemon juice"
        ),
        steps = listOf(
            "STEP 1 — BLANCH SPINACH: Boil 4 cups water with a pinch of salt. Add spinach leaves + green chillies. Boil for exactly 2 minutes. Drain immediately and transfer to ice-cold water (this preserves the vibrant green colour).",
            "STEP 2 — SPINACH PUREE: Blend blanched spinach and green chillies into a smooth puree. Do not add water — the blanched spinach has enough moisture.",
            "STEP 3 — TEMPERING: Heat oil/ghee in a pan. Add cumin seeds. Let them crackle.",
            "STEP 4 — ONION BASE: Add chopped onion. Sauté until golden brown (4-5 minutes).",
            "STEP 5: Add ginger-garlic paste. Sauté for 1 minute.",
            "STEP 6 — SPICES: Add turmeric, red chilli powder, coriander powder. Cook for 30 seconds.",
            "STEP 7 — SPINACH: Add spinach puree + salt. Stir well. Cook on medium heat for 5-6 minutes, stirring occasionally.",
            "STEP 8 — PANEER: Add paneer cubes. Simmer for 5 minutes. The paneer will absorb the spinach flavours.",
            "STEP 9 — FINISH: Add cream + crushed kasuri methi + garam masala + lemon juice. Stir and remove from heat.",
            "SERVE: Drizzle with cream. Serve hot with naan, roti, or rice."
        ),
        proTips = "PRO TIPS: 🔸 Blanching is critical — it removes oxalic acid from spinach and retains bright green colour. 🔸 Ice water bath immediately after boiling 'shocks' the leaves and locks in the colour. 🔸 Do not overcook the spinach puree or it will turn dark. 🔸 For richer taste, add 1 tbsp butter at the end. 🔸 Lightly fry paneer cubes in butter before adding for extra flavour.",
        nutritionalInfo = "Approx: 280 cal/serving, 18g protein, 18g fat, 10g carbs"
    )

    private fun getPaneerBhurjiRecipe(): VegRecipe = VegRecipe(
        name = "Paneer Bhurji",
        cuisine = "North Indian",
        category = "Main Course / Snack",
        preparationTime = 10,
        cookingTime = 10,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 2,
        ingredients = listOf(
            "200g Paneer, crumbled",
            "1 tbsp Oil / Butter",
            "1 tsp Cumin seeds",
            "1 small Onion, finely chopped",
            "1 small Tomato, finely chopped",
            "1 Green Chilli, finely chopped",
            "½ tsp Ginger, grated",
            "½ tsp Turmeric powder",
            "½ tsp Red Chilli powder",
            "½ tsp Garam Masala",
            "1 tbsp Fresh Coriander, chopped",
            "Salt to taste",
            "Lemon wedge for serving"
        ),
        steps = listOf(
            "STEP 1 — CRUMBLE PANEER: Crumble paneer with your hands into small pieces. Keep aside.",
            "STEP 2 — TEMPERING: Heat oil/butter in a pan. Add cumin seeds. Let them crackle.",
            "STEP 3 — ONION: Add finely chopped onion + green chilli + grated ginger. Sauté until onions turn translucent.",
            "STEP 4 — TOMATO: Add chopped tomato. Sauté until soft and mushy (2-3 minutes).",
            "STEP 5 — SPICES: Add turmeric, red chilli powder, garam masala, salt. Mix well.",
            "STEP 6 — PANEER: Add crumbled paneer. Mix gently. Cook on medium heat for 3-4 minutes, stirring occasionally.",
            "STEP 7 — FINISH: Garnish with fresh coriander and a squeeze of lemon.",
            "SERVE: Hot with paratha, bread toast, or as a sandwich filling."
        ),
        proTips = "PRO TIPS: 🔸 Do not overcook — paneer should remain soft, not dry. 🔸 For creamier bhurji, add 1 tbsp fresh cream or milk at the end. 🔸 Add finely chopped capsicum for extra crunch. 🔸 Perfect as a quick breakfast or tiffin box recipe.",
        nutritionalInfo = "Approx: 320 cal/serving, 20g protein, 22g fat, 8g carbs"
    )

    private fun getChilliPaneerRecipe(): VegRecipe = VegRecipe(
        name = "Chilli Paneer (Dry / Gravy)",
        cuisine = "Indo-Chinese",
        category = "Starter",
        preparationTime = 20,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Medium-Hot",
        servings = 4,
        ingredients = listOf(
            "250g Paneer, cubed",
            "2 tbsp Cornflour (cornstarch)",
            "2 tbsp All-purpose flour (maida)",
            "¼ tsp Black Pepper",
            "½ tsp Red Chilli powder",
            "Salt to taste",
            "Oil for shallow frying",
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
            "STEP 1 — COAT: In a bowl, mix cornflour, maida, black pepper, red chilli powder, salt, and enough water to make a thick batter. Add paneer cubes and gently coat.",
            "STEP 2 — FRY: Heat oil for shallow frying. Fry coated paneer cubes until golden and crispy (2-3 minutes per side). Drain on paper towel.",
            "STEP 3 — STIR FRY: Heat 1 tbsp oil in a wok on high heat. Add ginger + garlic + green chillies. Sauté for 15 seconds.",
            "STEP 4 — VEGETABLES: Add cubed capsicum and onion. Stir fry on high heat for 1 minute — they should remain crunchy.",
            "STEP 5 — SAUCES: Add soya sauce, red chilli sauce, tomato ketchup, vinegar, sugar. Stir quickly to combine.",
            "STEP 6 — SLURRY: Add cornflour slurry + 2 tbsp water. Stir and let the sauce thicken for 30 seconds.",
            "STEP 7 — PANEER: Add fried paneer cubes. Toss on high heat to coat evenly. Cook for 1 minute.",
            "SERVE: Garnish with spring onion greens. Serve hot as a starter or with fried rice/noodles."
        ),
        proTips = "PRO TIPS: 🔸 For extra crispy paneer, double coat — dip in batter, roll in dry cornflour, then fry. 🔸 High heat is essential for the Indo-Chinese wok-style flavour. 🔸 For gravy version, double the sauce and add ½ cup water + extra cornflour slurry. 🔸 Add 1 tsp MSG (Ajinomoto) for authentic restaurant taste.",
        nutritionalInfo = "Approx: 340 cal/serving, 14g protein, 22g fat, 18g carbs",
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("power" to "HIGH", "tempC" to 220, "durationMin" to 15)
    )

    // ─── Dal Recipes ──────────────────────────────

    private fun getDalMakhaniRecipe(): VegRecipe = VegRecipe(
        name = "Dal Makhani",
        cuisine = "North Indian (Punjabi)",
        category = "Main Course",
        preparationTime = 30,
        cookingTime = 60,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 6,
        ingredients = listOf(
            "1 cup Whole Black Urad Dal (Sabut Urad)",
            "¼ cup Rajma (Red Kidney Beans)",
            "2 tbsp Butter",
            "1 tbsp Ghee",
            "1 large Onion, finely chopped",
            "1 tbsp Ginger-Garlic paste",
            "2 large Tomatoes, pureed",
            "1 tsp Red Chilli powder",
            "½ tsp Turmeric powder",
            "1 tsp Coriander powder",
            "½ tsp Garam Masala",
            "2 tbsp Fresh Cream",
            "1 tsp Kasuri Methi, crushed",
            "Salt to taste",
            "Fresh Coriander for garnish"
        ),
        steps = listOf(
            "PREP: Wash and soak urad dal + rajma in water overnight (at least 8 hours). Drain before cooking.",
            "STEP 1 — PRESSURE COOK: Add soaked dal + rajma + 4 cups water + salt + turmeric to a pressure cooker. Cook for 6-7 whistles until completely soft and mushy.",
            "STEP 2 — MASH: Once pressure releases, mash the dal gently with a potato masher. Keep aside.",
            "STEP 3 — TEMPERING: Heat butter + ghee in a large pan. Add chopped onion. Sauté until deep golden brown (6-7 minutes).",
            "STEP 4: Add ginger-garlic paste. Sauté for 1 minute.",
            "STEP 5 — TOMATO: Add tomato puree. Cook until oil separates (5-6 minutes).",
            "STEP 6 — SPICES: Add red chilli powder, coriander powder. Mix and cook for 30 seconds.",
            "STEP 7 — DAL: Add the mashed dal + ½ cup warm water. Stir well. Simmer on low flame for 25-30 minutes, stirring occasionally.",
            "STEP 8 — CREAM: Add fresh cream + crushed kasuri methi + garam masala. Simmer for 2 more minutes.",
            "SERVE: Top with a pat of butter and a swirl of cream. Garnish with coriander. Serve hot with naan."
        ),
        proTips = "PRO TIPS: 🔸 The secret to restaurant-style Dal Makhani is the slow simmer — the longer it cooks on low heat, the better. 🔸 Overnight soaking is mandatory for urad dal. 🔸 Add the cream only at the end. 🔸 Leftover dal tastes even better the next day — flavours deepen overnight. 🔸 Use a heavy-bottomed pan to prevent burning.",
        nutritionalInfo = "Approx: 250 cal/serving, 14g protein, 12g fat, 24g carbs",
        smartApplianceMode = "PRESSURE_COOKER",
        smartApplianceParams = mapOf("mode" to "BEAN/CURRY", "pressure" to "HIGH", "durationMin" to 45)
    )

    private fun getDalTadkaRecipe(): VegRecipe = VegRecipe(
        name = "Dal Tadka (Yellow Dal)",
        cuisine = "North Indian",
        category = "Main Course",
        preparationTime = 10,
        cookingTime = 20,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 4,
        ingredients = listOf(
            "1 cup Toor Dal (Arhar Dal / Pigeon Pea)",
            "2 cups Water",
            "½ tsp Turmeric powder",
            "Salt to taste",
            "TADKA (TEMPERING):",
            "2 tbsp Ghee",
            "1 tsp Cumin seeds",
            "1 tsp Mustard seeds",
            "1-2 Dry Red Chillies, broken",
            "2-3 cloves Garlic, thinly sliced",
            "1 small Onion, finely chopped (optional)",
            "1 large Tomato, chopped",
            "1 tsp Red Chilli powder",
            "½ tsp Garam Masala",
            "Fresh Coriander for garnish"
        ),
        steps = listOf(
            "STEP 1 — COOK DAL: Wash toor dal thoroughly. Add dal + water + turmeric + salt to a pressure cooker. Cook for 5 whistles.",
            "STEP 2 — MASH: Once pressure releases, mash the dal with a whisk or masher until smooth and creamy. Add water for desired consistency.",
            "STEP 3 — BOIL: Transfer mashed dal to a pot. Bring to a gentle boil. Add chopped tomato. Let it simmer.",
            "STEP 4 — TADKA: Heat ghee in a small pan. Add cumin seeds + mustard seeds. Let them crackle.",
            "STEP 5: Add dry red chillies + sliced garlic. Sauté until garlic turns golden brown.",
            "STEP 6: (Optional) Add finely chopped onion. Sauté until brown.",
            "STEP 7 — FINISH: Add red chilli powder to the tadka — it will sizzle immediately. Pour the entire tadka over the boiling dal. Stir well.",
            "STEP 8: Add garam masala. Simmer for 2 minutes.",
            "SERVE: Garnish with fresh coriander. Serve hot with rice or roti. A squeeze of lemon adds freshness."
        ),
        proTips = "PRO TIPS: 🔸 The tadka (tempering) is the soul of this dish — sizzling ghee with cumin and garlic poured over the dal creates the signature flavour. 🔸 For extra richness, add 1 tbsp butter while mashing the dal. 🔸 Consistency should be flowing but not watery. 🔸 The red chilli powder should hit the hot ghee directly for that vibrant red colour.",
        nutritionalInfo = "Approx: 200 cal/serving, 10g protein, 8g fat, 24g carbs"
    )

    // ─── Chole / Rajma ────────────────────────────

    private fun getCholeMasalaRecipe(): VegRecipe = VegRecipe(
        name = "Chole Masala (Chickpea Curry)",
        cuisine = "North Indian (Punjabi)",
        category = "Main Course",
        preparationTime = 20,
        cookingTime = 35,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 6,
        ingredients = listOf(
            "2 cups White Chickpeas (Kabuli Chana), soaked overnight",
            "2 tbsp Oil",
            "1 large Onion, finely chopped",
            "1 tbsp Ginger-Garlic paste",
            "2 large Tomatoes, pureed",
            "2 tsp Coriander powder",
            "1 tsp Cumin powder",
            "½ tsp Turmeric powder",
            "1 tsp Red Chilli powder",
            "2 tsp Chole Masala (readymade or homemade)",
            "1 tsp Amchur (Dry Mango powder)",
            "½ tsp Garam Masala",
            "1 tsp Kasuri Methi, crushed",
            "2 tbsp Fresh Coriander",
            "Salt to taste",
            "1 tea bag (for dark colour), optional",
            "1 tbsp Lemon juice"
        ),
        steps = listOf(
            "PREP: Soak chickpeas in plenty of water overnight (8-10 hours). Drain and rinse.",
            "STEP 1 — PRESSURE COOK: Add chickpeas + 4 cups water + salt + 1 tea bag (for colour) + ½ tsp turmeric. Cook for 6-7 whistles until soft.",
            "STEP 2 — ONION: Heat oil in a pan. Add chopped onion. Sauté until deep golden brown.",
            "STEP 3: Add ginger-garlic paste. Sauté for 1 minute.",
            "STEP 4 — TOMATO: Add tomato puree. Cook until oil separates (5-6 minutes).",
            "STEP 5 — SPICES: Add coriander powder, cumin powder, turmeric, red chilli powder, chole masala. Cook for 1 minute.",
            "STEP 6 — CHICKPEAS: Add cooked chickpeas + the water used for boiling. Mash a few chickpeas against the side of the pan to thicken the gravy.",
            "STEP 7 — SIMMER: Let it simmer on low heat for 15-20 minutes, stirring occasionally. The gravy should thicken.",
            "STEP 8 — FINISH: Add amchur + garam masala + crushed kasuri methi + lemon juice. Stir and turn off heat.",
            "SERVE: Garnish with onion rings + coriander + a squeeze of lemon. Serve hot with bhatura, naan, or rice."
        ),
        proTips = "PRO TIPS: 🔸 A tea bag in the pressure cooker gives the chole a beautiful dark brown colour. 🔸 Always cook chole a day before — the flavours meld overnight and taste spectacular the next day. 🔸 Adding a pinch of baking soda while soaking helps soften the chickpeas. 🔸 The key to dhaba-style chole is the slow simmer after adding the spices.",
        nutritionalInfo = "Approx: 280 cal/serving, 12g protein, 10g fat, 36g carbs",
        smartApplianceMode = "PRESSURE_COOKER",
        smartApplianceParams = mapOf("mode" to "BEAN/CURRY", "pressure" to "HIGH", "durationMin" to 40)
    )

    private fun getRajmaRecipe(): VegRecipe = VegRecipe(
        name = "Rajma (Kidney Bean Curry)",
        cuisine = "North Indian (Punjabi)",
        category = "Main Course",
        preparationTime = 20,
        cookingTime = 40,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 6,
        ingredients = listOf(
            "2 cups Rajma (Red Kidney Beans), soaked overnight",
            "2 tbsp Oil / Ghee",
            "1 large Onion, finely chopped",
            "1 tbsp Ginger-Garlic paste",
            "2 large Tomatoes, pureed",
            "1 tsp Cumin seeds",
            "1 tsp Turmeric powder",
            "1 tsp Red Chilli powder",
            "1 tsp Coriander powder",
            "½ tsp Garam Masala",
            "1 tsp Kasuri Methi, crushed (optional)",
            "Salt to taste",
            "2 tbsp Fresh Cream (optional)",
            "Fresh Coriander for garnish"
        ),
        steps = listOf(
            "PREP: Soak rajma in water overnight (8-10 hours). Drain and rinse thoroughly.",
            "STEP 1 — PRESSURE COOK: Add soaked rajma + 4 cups water + salt + turmeric. Pressure cook for 7-8 whistles until completely soft.",
            "STEP 2 — ONION: Heat oil/ghee in a pan. Add cumin seeds. When they crackle, add onion and sauté until deep golden brown.",
            "STEP 3: Add ginger-garlic paste. Sauté for 1 minute.",
            "STEP 4 — TOMATO: Add tomato puree. Cook on medium heat until oil separates (5-6 minutes).",
            "STEP 5 — SPICES: Add red chilli powder, coriander powder. Cook for 30 seconds.",
            "STEP 6 — RAJMA: Add cooked rajma + the cooking water. Mash a few beans against the pan to thicken.",
            "STEP 7 — SIMMER: Let it simmer on low heat for 20 minutes, stirring occasionally. The gravy should thicken and become creamy.",
            "STEP 8 — FINISH: Add garam masala + crushed kasuri methi + cream (optional). Simmer for 2 minutes.",
            "SERVE: Garnish with coriander. Serve hot with steamed rice and a dollop of butter."
        ),
        proTips = "PRO TIPS: 🔸 Rajma is traditionally eaten with rice — the combination is called 'Rajma Chawal'. 🔸 Overnight soaking is essential; skip this and the beans will remain hard. 🔸 The longer rajma simmers after cooking, the better it tastes. 🔸 Adding the water reserved from boiling makes the gravy richer. 🔸 A pinch of sugar balances the acidity of tomatoes.",
        nutritionalInfo = "Approx: 270 cal/serving, 14g protein, 8g fat, 38g carbs",
        smartApplianceMode = "PRESSURE_COOKER",
        smartApplianceParams = mapOf("mode" to "BEAN/CURRY", "pressure" to "HIGH", "durationMin" to 35)
    )

    // ─── Other Sabzis ────────────────────────────

    private fun getBhindiMasalaRecipe(): VegRecipe = VegRecipe(
        name = "Bhindi Masala (Ladyfinger Curry)",
        cuisine = "North Indian",
        category = "Main Course",
        preparationTime = 10,
        cookingTime = 15,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 3,
        ingredients = listOf(
            "500g Bhindi (Okra / Ladyfinger), washed and dried completely",
            "2 tbsp Oil",
            "1 tsp Cumin seeds",
            "1 medium Onion, sliced",
            "2-3 Green Chillies, slit",
            "1 tsp Ginger, grated",
            "½ tsp Turmeric powder",
            "1 tsp Red Chilli powder",
            "1 tsp Coriander powder",
            "½ tsp Amchur (Dry Mango powder)",
            "½ tsp Garam Masala",
            "Salt to taste",
            "Fresh Coriander for garnish"
        ),
        steps = listOf(
            "CRITICAL PREP: Wash bhindi 1 hour before cooking and dry COMPLETELY. Slice off tips and cut into 1-inch pieces. Moisture makes bhindi slimy!",
            "STEP 1 — TEMPERING: Heat oil in a pan. Add cumin seeds. Let them crackle.",
            "STEP 2: Add sliced onion + green chillies. Sauté until onions turn translucent.",
            "STEP 3: Add grated ginger. Sauté for 30 seconds.",
            "STEP 4 — BHINDI: Add bhindi pieces. Mix well. Cook uncovered on medium heat for 8-10 minutes, stirring occasionally. Do not cover — steam makes bhindi slimy.",
            "STEP 5 — SPICES: Add turmeric, red chilli powder, coriander powder, salt. Mix gently. Cook for 2-3 more minutes.",
            "STEP 6 — FINISH: Add amchur + garam masala. Toss and cook for 1 minute.",
            "SERVE: Garnish with fresh coriander. Serve hot with roti."
        ),
        proTips = "PRO TIPS: 🔸 The golden rule of bhindi: COMPLETELY DRY before cooking. 🔸 Never cover the pan while cooking — uncovered, high heat prevents sliminess. 🔸 Add a pinch of sugar to prevent sliminess and enhance taste. 🔸 Slight charring on the bhindi pieces adds delicious smoky flavour. 🔸 Amchur (dry mango powder) adds the essential tanginess.",
        nutritionalInfo = "Approx: 140 cal/serving, 4g protein, 8g fat, 14g carbs"
    )

    private fun getAlooGobiRecipe(): VegRecipe = VegRecipe(
        name = "Aloo Gobi (Potato Cauliflower Curry)",
        cuisine = "North Indian",
        category = "Main Course",
        preparationTime = 15,
        cookingTime = 20,
        difficulty = "Easy",
        spiceLevel = "Medium",
        servings = 4,
        ingredients = listOf(
            "2 cups Cauliflower florets (Gobi)",
            "2 medium Potatoes (Aloo), cubed",
            "2 tbsp Oil",
            "1 tsp Cumin seeds",
            "1 medium Onion, chopped",
            "1 tsp Ginger, grated",
            "2 Green Chillies, slit",
            "1 tsp Turmeric powder",
            "1 tsp Red Chilli powder",
            "1 tsp Coriander powder",
            "½ tsp Garam Masala",
            "1 tbsp Fresh Coriander",
            "Salt to taste"
        ),
        steps = listOf(
            "PREP: Cut cauliflower into medium florets. Soak in warm salted water for 10 minutes (removes any hidden bugs). Drain well.",
            "STEP 1 — TEMPERING: Heat oil in a pan. Add cumin seeds. Let them crackle.",
            "STEP 2 — ONION: Add chopped onion + green chillies. Sauté until golden brown.",
            "STEP 3: Add grated ginger. Sauté for 30 seconds.",
            "STEP 4 — SPICES: Add turmeric, red chilli powder, coriander powder. Stir for 30 seconds.",
            "STEP 5 — POTATOES: Add potato cubes. Sauté on medium heat for 3-4 minutes until slightly golden.",
            "STEP 6 — CAULIFLOWER: Add cauliflower florets + salt. Mix gently. Sprinkle 2 tbsp water.",
            "STEP 7 — COOK: Cover and cook on low heat for 12-15 minutes, stirring occasionally. The vegetables should be tender but not mushy.",
            "STEP 8 — FINISH: Add garam masala + fresh coriander. Stir gently. If there's excess water, cook uncovered for 1-2 minutes.",
            "SERVE: Hot with roti, paratha, or as a side dish with dal-chawal."
        ),
        proTips = "PRO TIPS: 🔸 For a drier version (sukhi aloo gobi), skip adding water — the vegetables release their own moisture. 🔸 Don't overcook the cauliflower — it should retain its shape. 🔸 A pinch of sugar enhances flavour. 🔸 For the Punjabi dhaba version, add 1 chopped tomato along with the spices.",
        nutritionalInfo = "Approx: 160 cal/serving, 4g protein, 7g fat, 22g carbs"
    )

    private fun getBainganBhartaRecipe(): VegRecipe = VegRecipe(
        name = "Baingan Bharta (Smoked Eggplant Mash)",
        cuisine = "North Indian (Punjabi)",
        category = "Main Course",
        preparationTime = 15,
        cookingTime = 30,
        difficulty = "Medium",
        spiceLevel = "Medium",
        servings = 4,
        ingredients = listOf(
            "1 large Brinjal (Eggplant / Baingan)",
            "2 tbsp Oil",
            "1 tsp Cumin seeds",
            "1 large Onion, finely chopped",
            "1 tbsp Ginger-Garlic paste",
            "2 Green Chillies, finely chopped",
            "2 large Tomatoes, finely chopped",
            "1 tsp Red Chilli powder",
            "½ tsp Turmeric powder",
            "1 tsp Coriander powder",
            "½ tsp Garam Masala",
            "Salt to taste",
            "Fresh Coriander for garnish"
        ),
        steps = listOf(
            "PREP: Wash the brinjal and pat dry. Make a few slits and stuff with chopped garlic.",
            "STEP 1 — ROAST: Roast the brinjal directly on a gas flame, turning every 2-3 minutes, until the skin is completely charred and the flesh is soft (10-12 minutes). Alternatively, roast in oven at 220°C for 30 minutes.",
            "STEP 2 — COOL: Let the roasted brinjal cool. Peel off the charred skin. Mash the soft flesh with a fork. Keep aside.",
            "STEP 3 — TEMPERING: Heat oil in a pan. Add cumin seeds. Let them crackle.",
            "STEP 4 — ONION: Add chopped onion + green chillies. Sauté until golden brown.",
            "STEP 5: Add ginger-garlic paste. Sauté for 1 minute.",
            "STEP 6 — TOMATO: Add chopped tomatoes. Cook until soft and oil separates (5-6 minutes).",
            "STEP 7 — SPICES: Add red chilli powder, turmeric, coriander powder. Mix and cook for 30 seconds.",
            "STEP 8 — BAINGAN: Add mashed brinjal + salt. Mix well. Cook on medium heat for 8-10 minutes, stirring occasionally.",
            "STEP 9 — FINISH: Add garam masala + fresh coriander. Stir and turn off heat.",
            "SERVE: Garnish with chopped coriander and a squeeze of lemon. Serve hot with roti or paratha."
        ),
        proTips = "PRO TIPS: 🔸 The smoky flavour comes from roasting directly on the flame — this is non-negotiable! 🔸 If using oven, add a drop of liquid smoke for that authentic tandoor taste. 🔸 Do not skip the charring step — it defines the dish. 🔸 Some prefer a final tempering of ghee + cumin + garlic poured over the bharta for extra flavour.",
        nutritionalInfo = "Approx: 150 cal/serving, 3g protein, 10g fat, 12g carbs"
    )

    private fun getMalaiKoftaRecipe(): VegRecipe = VegRecipe(
        name = "Malai Kofta (Vegetable Dumplings in Cream Gravy)",
        cuisine = "North Indian (Mughlai)",
        category = "Main Course",
        preparationTime = 30,
        cookingTime = 30,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 4,
        ingredients = listOf(
            "KOFTA DUMPLINGS:",
            "200g Paneer, grated",
            "2 medium Potatoes, boiled and mashed",
            "2 tbsp Cornflour",
            "1 tsp Ginger, grated",
            "1 Green Chilli, finely chopped",
            "2 tbsp Mixed Dry Fruits (cashew, raisin), chopped",
            "½ tsp Garam Masala",
            "Salt to taste",
            "Oil for deep frying",
            "GRAVY:",
            "2 tbsp Ghee",
            "1 large Onion, sliced",
            "2 large Tomatoes, pureed",
            "¼ cup Cashews, soaked",
            "1 tsp Ginger-Garlic paste",
            "1 tsp Red Chilli powder",
            "½ tsp Turmeric powder",
            "½ tsp Garam Masala",
            "2 tbsp Fresh Cream",
            "½ tsp Saffron strands soaked in 2 tbsp milk",
            "1 tsp Sugar",
            "Salt to taste"
        ),
        steps = listOf(
            "PREP: Soak cashews in warm water for 20 minutes. Soak saffron in warm milk.",
            "KOFTA PREPARATION:",
            "STEP 1: In a bowl, mix grated paneer, mashed potatoes, cornflour, ginger, green chilli, dry fruits, garam masala, and salt. Knead into a smooth dough.",
            "STEP 2: Shape into small lemon-sized balls. Make sure there are no cracks.",
            "STEP 3: Heat oil for deep frying. Fry kofta balls on medium heat until golden brown (3-4 minutes). Drain on paper towel. Keep warm.",
            "GRAVY PREPARATION:",
            "STEP 4: Heat ghee. Add sliced onion. Sauté until deep golden brown. Let cool slightly, then grind to a smooth paste.",
            "STEP 5: In the same pan, add ginger-garlic paste. Sauté for 1 minute.",
            "STEP 6: Add tomato puree. Cook until oil separates.",
            "STEP 7: Grind soaked cashews to a smooth paste. Add to the gravy.",
            "STEP 8: Add red chilli powder, turmeric, sugar, salt. Add 1 cup warm water. Simmer for 10 minutes.",
            "STEP 9: Add fresh cream + saffron milk + garam masala. Stir gently. Simmer for 2 minutes.",
            "SERVING: Place kofta balls in a serving dish. Pour hot gravy over them just before serving (so they remain crispy).",
            "SERVE: With naan or roti. Garnish with cream swirl and chopped coriander."
        ),
        proTips = "PRO TIPS: 🔸 The key to perfect kofta is to NOT overcook them in the gravy — pour gravy over the koftas just before serving. 🔸 Adding cornflour prevents the koftas from breaking while frying. 🔸 The onion-garlic gravy should be silky smooth — strain if needed. 🔸 For richer kofta, add a cube of cheese in the centre of each ball.",
        nutritionalInfo = "Approx: 420 cal/serving, 16g protein, 28g fat, 30g carbs"
    )

    // ─── South Indian Recipes ─────────────────────

    private fun getMasalaDosaRecipe(): VegRecipe = VegRecipe(
        name = "Masala Dosa",
        cuisine = "South Indian (Karnataka/Tamil Nadu)",
        category = "Main Course",
        preparationTime = 240, // overnight fermentation
        cookingTime = 30,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 6,
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
            "STEP 1 — RINSE & SOAK: Wash rice and urad dal separately. Soak rice + fenugreek seeds in water for 6-8 hours. Soak urad dal separately for 6-8 hours.",
            "STEP 2 — GRIND: First grind urad dal with minimal water until smooth and fluffy. Transfer to a large vessel. Then grind rice + poha to a slightly grainy consistency.",
            "STEP 3 — MIX: Combine both batters. Add salt. Mix well with your hand (this helps fermentation).",
            "STEP 4 — FERMENT: Cover and keep in a warm place for 8-12 hours or overnight. The batter should rise and become bubbly.",
            "ALOO MASALA:",
            "STEP 5: Heat oil. Add mustard seeds + cumin seeds. Let them pop.",
            "STEP 6: Add curry leaves + green chillies + ginger. Sauté for 30 seconds.",
            "STEP 7: Add sliced onion. Sauté until translucent.",
            "STEP 8: Add turmeric + salt + mashed potatoes + lemon juice + coriander. Mix well. Keep warm.",
            "MAKING DOSA:",
            "STEP 9: Heat a non-stick tawa (griddle). Sprinkle water — if it sizzles, it's ready. Wipe with an oiled cloth.",
            "STEP 10: Pour a ladleful of batter in the centre. Spread in a circular motion from centre outwards into a thin circle.",
            "STEP 11: Drizzle a few drops of oil/ghee around the edges. Cook on medium-high heat until golden and crispy.",
            "STEP 12: Place a portion of aloo masala on one half. Fold the other half over. Serve immediately.",
            "SERVE: With coconut chutney + sambhar. Tradition: serve with extra ghee on top."
        ),
        proTips = "PRO TIPS: 🔸 The urad dal must be ground to a very smooth, fluffy consistency — this determines the softness of the dosa. 🔸 Fermentation time depends on climate — in cold weather, place the batter in the oven with the light on. 🔸 The tawa must be hot enough — test with a drop of water. 🔸 For crispy dosa, spread the batter thin; for softer dosa, keep it slightly thick. 🔸 Leftover batter can be refrigerated for 2-3 days.",
        nutritionalInfo = "Approx: 180 cal/dosa, 6g protein, 4g fat, 32g carbs"
    )

    private fun getPlainDosaRecipe(): VegRecipe = VegRecipe(
        name = "Plain Dosa / Sada Dosa",
        cuisine = "South Indian",
        category = "Main Course",
        preparationTime = 240,
        cookingTime = 20,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 6,
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
        nutritionalInfo = "Approx: 120 cal/dosa, 4g protein, 2g fat, 22g carbs"
    )

    private fun getIdliRecipe(): VegRecipe = VegRecipe(
        name = "Idli (Steamed Rice Cakes)",
        cuisine = "South Indian",
        category = "Main Course / Breakfast",
        preparationTime = 240,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 6,
        ingredients = listOf(
            "Same batter as Masala Dosa (above)",
            "Water for steaming"
        ),
        steps = listOf(
            "FOLLOW Masala Dosa batter preparation steps 1-4.",
            "STEP 1: Grease idli moulds with a drop of oil.",
            "STEP 2: Pour batter into each mould, filling ¾ full.",
            "STEP 3: Steam in idli steamer or pressure cooker (without whistle) for 10-12 minutes on medium heat.",
            "STEP 4: Test with a toothpick — it should come out clean. Let rest for 2 minutes before removing.",
            "SERVE: Hot with coconut chutney + sambhar + a drizzle of ghee."
        ),
        proTips = "PRO TIPS: 🔸 The batter should be slightly thicker for idli than for dosa. 🔸 Do not overfill the moulds — idli doubles in size. 🔸 Resting the batter for 5 minutes after stirring before pouring helps. 🔸 Adding a handful of grated carrots or chopped coriander to the batter makes tasty variation.",
        nutritionalInfo = "Approx: 70 cal/idli, 2g protein, 1g fat, 14g carbs",
        smartApplianceMode = "STEAMER",
        smartApplianceParams = mapOf("mode" to "STEAM", "durationMin" to 12)
    )

    private fun getMeduVadaRecipe(): VegRecipe = VegRecipe(
        name = "Medu Vada (Savory Lentil Donuts)",
        cuisine = "South Indian",
        category = "Snack",
        preparationTime = 120,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Mild",
        servings = 4,
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
            "STEP 1 — GRIND: Grind the soaked urad dal WITHOUT water or with very minimal water. The batter should be thick, smooth, and fluffy. This is critical.",
            "STEP 2 — WHIP: Transfer to a bowl. Whisk the batter with a spoon for 3-4 minutes until it becomes light and airy. The batter should hold its shape when dropped.",
            "STEP 3 — SEASON: Add green chillies, ginger, onion (if using), curry leaves, coriander, rice flour, hing, cumin, crushed pepper, salt. Mix gently.",
            "STEP 4 — SHAPE: Wet your palms. Take a lemon-sized portion. Shape into a ball, then flatten slightly. Poke a hole in the centre using your thumb.",
            "STEP 5 — FRY: Heat oil on medium heat. Gently slide the vadas into the oil. Fry on medium heat until golden brown on both sides (3-4 minutes per side).",
            "STEP 6 — DRAIN: Remove and drain on paper towel.",
            "SERVE: Hot with coconut chutney and sambhar. Traditionally served with piping hot sambhar."
        ),
        proTips = "PRO TIPS: 🔸 The batter should be thick enough to hold shape — excess water makes vadas flat and oily. 🔸 Whisking the batter well adds air and makes vadas fluffy. 🔸 Fry on medium heat — high heat will burn the outside while the inside remains raw. 🔸 For extra crispiness, add 1 tbsp rice flour. 🔸 The hole in the centre ensures even cooking.",
        nutritionalInfo = "Approx: 200 cal/serving (2 vadas), 8g protein, 10g fat, 20g carbs"
    )

    // ─── Gujarati Recipes ─────────────────────────

    private fun getDhoklaRecipe(): VegRecipe = VegRecipe(
        name = "Khaman Dhokla (Steamed Lentil Cake)",
        cuisine = "Gujarati",
        category = "Snack",
        preparationTime = 15,
        cookingTime = 15,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 6,
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
            "BATTER:",
            "STEP 1: In a bowl, mix besan, suji, ginger-chilli paste, sugar, turmeric, lemon juice, salt. Add water gradually to make a smooth, flowing batter (no lumps).",
            "STEP 2: Just before steaming, add Eno fruit salt + 1 tbsp water. Mix gently — the batter will become frothy.",
            "STEP 3 — STEAM: Immediately pour into the greased thali (not more than 1-inch thick). Steam on medium heat for 12-15 minutes.",
            "STEP 4 — TEST: Insert a knife/toothpick — it should come out clean. Let cool for 5 minutes.",
            "TEMPERING:",
            "STEP 5: Heat oil. Add mustard seeds. Let them pop.",
            "STEP 6: Add sesame seeds + curry leaves + green chillies. Sauté for 10 seconds.",
            "STEP 7: Pour the sugar-water mixture. Let it sizzle. Pour this tempering over the steamed dhokla.",
            "STEP 8: Cut into pieces. Garnish with coriander + coconut.",
            "SERVE: With green chutney or tamarind chutney."
        ),
        proTips = "PRO TIPS: 🔸 Eno should be added just before steaming — the batter cannot wait. 🔸 The steamer must be ready and hot before adding Eno. 🔸 Do not open the lid during steaming. 🔸 For yellow colour, add a pinch of turmeric. For white dhokla, skip turmeric. 🔸 The sugar-water tempering is what gives dhokla the signature sweet-tangy taste.",
        nutritionalInfo = "Approx: 120 cal/serving, 6g protein, 4g fat, 16g carbs",
        smartApplianceMode = "STEAMER",
        smartApplianceParams = mapOf("mode" to "STEAM", "durationMin" to 15)
    )

    private fun getTheplaRecipe(): VegRecipe = VegRecipe(
        name = "Methi Thepla (Fenugreek Flatbread)",
        cuisine = "Gujarati",
        category = "Bread",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 6,
        ingredients = listOf(
            "2 cups Whole Wheat Flour (Atta)",
            "1 cup Fresh Methi (Fenugreek leaves), finely chopped",
            "½ cup Curd (Yogurt)",
            "2 tbsp Besan (Gram Flour), optional",
            "1 tsp Ginger-Green Chilli paste",
            "½ tsp Turmeric powder",
            "½ tsp Red Chilli powder",
            "½ tsp Cumin powder",
            "½ tsp Ajwain (Carom seeds)",
            "1 tbsp Oil",
            "Salt to taste",
            "Water as needed",
            "Ghee for cooking"
        ),
        steps = listOf(
            "PREP: Wash and finely chop methi leaves. Squeeze out excess water.",
            "DOUGH:",
            "STEP 1: In a large bowl, mix wheat flour + besan + methi + curd + ginger-chilli paste + turmeric + red chilli + cumin + ajwain + oil + salt.",
            "STEP 2: Knead into a soft dough using water if needed. Cover and rest for 15 minutes.",
            "MAKING THEPLA:",
            "STEP 3: Divide into lemon-sized balls. Roll each into a circle (about 6-inch diameter).",
            "STEP 4: Heat a tawa (griddle). Cook thepla on medium heat until bubbles appear.",
            "STEP 5: Flip. Apply ghee on both sides. Cook until golden brown spots appear.",
            "SERVE: With yogurt, pickle, or chundo (sweet mango pickle). Theplas stay fresh for 2-3 days — perfect for travel."
        ),
        proTips = "PRO TIPS: 🔸 Theplas are meant to be slightly thick, not thin like roti. 🔸 Add grated bottle gourd (lauki) for softer theplas. 🔸 Can be refrigerated for up to 5 days — just reheat on tawa. 🔸 The yogurt in the dough keeps them soft for longer. 🔸 For extra flavour, add 1 tbsp crushed peanuts to the dough.",
        nutritionalInfo = "Approx: 150 cal/thepla, 4g protein, 4g fat, 24g carbs"
    )

    // ─── Bengali Recipes ──────────────────────────

    private fun getShuktoRecipe(): VegRecipe = VegRecipe(
        name = "Shukto (Bengali Mixed Vegetable Bitter Curry)",
        cuisine = "Bengali",
        category = "Main Course / Starter",
        preparationTime = 20,
        cookingTime = 25,
        difficulty = "Medium",
        spiceLevel = "Mild",
        servings = 4,
        ingredients = listOf(
            "100g Bitter Gourd (Karela / Uchche), sliced thin",
            "1 large Potato, cubed",
            "1 small Brinjal, cubed",
            "1 Raw Banana, sliced",
            "½ cup Pumpkin, cubed",
            "1 drumstick (saijan), cut into 2-inch pieces",
            "1 tbsp Mustard seeds",
            "1 tbsp Poppy seeds (Posto)",
            "1 tsp Ginger paste",
            "2-3 Green Chillies, slit",
            "1 tbsp Mustard Oil",
            "½ tsp Turmeric powder",
            "1 tsp Panch Phoron (Bengali 5-spice)",
            "1 tbsp Milk",
            "Salt to taste",
            "1 tsp Sugar"
        ),
        steps = listOf(
            "PREP: Slice bitter gourd thin, rub with salt, set aside for 10 minutes, then rinse (reduces bitterness).",
            "STEP 1 — GRIND: Soak mustard seeds and poppy seeds in water for 15 minutes. Grind to a smooth paste.",
            "STEP 2 — FRY BITTER GOURD: Heat mustard oil. Fry bitter gourd slices until crisp. Remove and set aside.",
            "STEP 3 — TEMPERING: In the same oil, add panch phoron. Let it crackle.",
            "STEP 4: Add green chillies + ginger paste. Sauté for 30 seconds.",
            "STEP 5 — VEGETABLES: Add potato + brinjal + raw banana + pumpkin + drumstick. Sauté for 3 minutes.",
            "STEP 6 — MUSTARD PASTE: Add the ground mustard-poppy paste + turmeric + salt + sugar. Mix well.",
            "STEP 7 — COOK: Add ½ cup warm water. Cover and cook on low heat until vegetables are tender (12-15 minutes).",
            "STEP 8 — FINISH: Add milk + fried bitter gourd. Simmer for 2 minutes. Do not boil after adding milk.",
            "SERVE: With steamed rice. Shukto is the traditional first course of a Bengali meal."
        ),
        proTips = "PRO TIPS: 🔸 The slight bitterness is intentional — it stimulates the palate. 🔸 Panch phoron is essential — a mix of fenugreek, nigella, cumin, mustard, and fennel seeds. 🔸 Mustard oil gives the authentic Bengali flavour. 🔸 Do not skip the milk — it balances the bitterness.",
        nutritionalInfo = "Approx: 130 cal/serving, 3g protein, 6g fat, 18g carbs"
    )

    // ─── Breads ───────────────────────────────────

    private fun getRotiRecipe(): VegRecipe = VegRecipe(
        name = "Roti / Chapati / Phulka",
        cuisine = "North Indian",
        category = "Bread",
        preparationTime = 10,
        cookingTime = 15,
        difficulty = "Medium",
        servings = 4,
        ingredients = listOf(
            "2 cups Whole Wheat Flour (Atta)",
            "Water (warm) for kneading",
            "1 tsp Oil/Ghee",
            "Salt (optional, traditionally not added)",
            "Extra flour for dusting"
        ),
        steps = listOf(
            "DOUGH:",
            "STEP 1: In a large bowl, take wheat flour. Add water gradually and knead into a soft, smooth dough.",
            "STEP 2: Add 1 tsp oil/ghee and knead again. Cover and rest for 15-20 minutes.",
            "MAKING ROTI:",
            "STEP 3: Divide dough into equal balls. Roll each into a smooth ball between your palms.",
            "STEP 4: Roll out into a circle using rolling pin — dust with dry flour as needed.",
            "COOKING:",
            "STEP 5: Heat a tawa (flat griddle) on medium-high heat.",
            "STEP 6: Place rolled roti on tawa. When bubbles appear, flip.",
            "STEP 7: For phulka: use tongs to place the half-cooked roti directly on gas flame. It will puff up like a balloon.",
            "STEP 8: Apply ghee/butter on one side. Press gently to release air.",
            "SERVE: Hot with any dal, sabzi, or curry."
        ),
        proTips = "PRO TIPS: 🔸 The dough should be slightly softer than for paratha — 'kane se kam, kaan se zyada'. 🔸 Resting the dough is essential for soft rotis. 🔸 For perfectly puffed phulka, the flame should be moderate — too high burns it, too low won't puff. 🔸 Applying ghee while hot keeps the roti soft for longer.",
        nutritionalInfo = "Approx: 80 cal/roti, 3g protein, 1g fat, 16g carbs"
    )

    private fun getAlooParathaRecipe(): VegRecipe = VegRecipe(
        name = "Aloo Paratha (Potato Stuffed Flatbread)",
        cuisine = "North Indian (Punjabi)",
        category = "Bread",
        preparationTime = 20,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Medium",
        servings = 4,
        ingredients = listOf(
            "DOUGH:",
            "2 cups Whole Wheat Flour",
            "Water for kneading",
            "1 tsp Oil",
            "Salt to taste",
            "STUFFING:",
            "3 medium Potatoes, boiled and mashed",
            "1 Green Chilli, finely chopped",
            "1 tsp Ginger, grated",
            "1 tsp Cumin powder",
            "½ tsp Red Chilli powder",
            "½ tsp Garam Masala",
            "1 tbsp Fresh Coriander, chopped",
            "Salt to taste",
            "Butter/Ghee for cooking"
        ),
        steps = listOf(
            "DOUGH: Knead flour + salt + oil + water into a soft dough. Cover and rest for 15 minutes.",
            "STUFFING: Mix mashed potatoes + green chilli + ginger + cumin powder + red chilli + garam masala + coriander + salt. The stuffing should be dry (no moisture).",
            "MAKING PARATHA:",
            "STEP 1: Divide dough into balls. Roll each into a 4-inch circle.",
            "STEP 2: Place a ball of stuffing in the centre. Bring the edges together to seal. Press gently.",
            "STEP 3: Dust with dry flour. Roll out gently into a 6-7 inch circle — don't let the stuffing come out.",
            "COOKING:",
            "STEP 4: Heat tawa. Place paratha on it. When bubbles appear, flip.",
            "STEP 5: Apply ghee/butter on both sides. Cook until golden brown on both sides.",
            "SERVE: Hot with yogurt + butter + pickle. Traditionally served with a blob of butter on top."
        ),
        proTips = "PRO TIPS: 🔸 The stuffing must be completely dry — excess moisture makes rolling difficult. 🔸 For extra flavour, add crumbled paneer or grated cheese to the stuffing. 🔸 Roll gently — if the stuffing starts coming out, seal with a pinch of dough. 🔸 Apply generous butter while hot for the authentic Punjabi taste.",
        nutritionalInfo = "Approx: 250 cal/paratha, 6g protein, 10g fat, 36g carbs"
    )

    // ─── Rice / Biryani ───────────────────────────

    private fun getVegBiryaniRecipe(): VegRecipe = VegRecipe(
        name = "Vegetable Biryani (Dum Biryani)",
        cuisine = "Indian (Hyderabadi / Lucknowi)",
        category = "Main Course",
        preparationTime = 25,
        cookingTime = 35,
        difficulty = "Hard",
        spiceLevel = "Medium",
        servings = 6,
        ingredients = listOf(
            "RICE LAYER:",
            "2 cups Basmati Rice, soaked 30 min",
            "4 cups Water",
            "2 Bay leaves",
            "4-5 Cloves",
            "3 Green Cardamoms",
            "1-inch Cinnamon stick",
            "1 tsp Shahi Jeera (cumin)",
            "Salt to taste",
            "VEGETABLE LAYER:",
            "2 tbsp Ghee",
            "1 large Onion, thinly sliced",
            "1 cup Mixed vegetables (carrot, beans, peas, potato, cauliflower)",
            "½ cup Paneer cubes (optional)",
            "½ cup Curd (Yogurt), whisked",
            "1 tsp Ginger-Garlic paste",
            "2 Green Chillies, slit",
            "1 tsp Red Chilli powder",
            "½ tsp Turmeric powder",
            "1 tsp Biryani Masala",
            "½ tsp Garam Masala",
            "2 tbsp Fried Onions (Birista)",
            "1 tbsp Mint leaves, chopped",
            "1 tbsp Coriander, chopped",
            "2 tbsp Warm Milk + Saffron strands",
            "1 tbsp Rose water / Kewra water"
        ),
        steps = listOf(
            "PREP: Soak basmati rice in water for 30 minutes. Drain. Soak saffron in warm milk.",
            "RICE:",
            "STEP 1: Boil 4 cups water + salt + bay leaves + cloves + cardamom + cinnamon + shahi jeera.",
            "STEP 2: Add drained rice. Cook until 70% done (grains should still have a bite). Drain and set aside.",
            "VEGETABLE LAYER:",
            "STEP 3: Heat ghee. Add sliced onion. Sauté until deep golden brown. Remove half for layering.",
            "STEP 4: Add ginger-garlic paste + green chillies. Sauté for 1 minute.",
            "STEP 5: Add mixed vegetables + all spice powders + salt. Cook for 3-4 minutes.",
            "STEP 6: Add whisked curd + fried onions (half) + mint + coriander. Mix. Cook until vegetables are just tender.",
            "LAYERING (DUM):",
            "STEP 7: In a heavy-bottomed pot, spread half the rice as the first layer.",
            "STEP 8: Spread the vegetable mixture over the rice.",
            "STEP 9: Cover with remaining rice. Top with fried onions + saffron milk + ghee + rose water.",
            "DUMPUKHT:",
            "STEP 10: Cover the pot with a tight lid. Seal the edges with dough (atta ka gattha) to trap steam.",
            "STEP 11: Cook on low heat (dum) for 20-25 minutes. Do NOT open the lid.",
            "STEP 12: After 25 minutes, turn off heat. Let it rest for 5 minutes before opening.",
            "SERVE: Gently fluff with a fork. Serve with raita (yogurt with onions/tomato/cucumber)."
        ),
        proTips = "PRO TIPS: 🔸 70% cooked rice is key — the dum process completes the cooking. 🔸 Sealing the pot with dough is non-negotiable for authentic dum biryani. 🔸 Do not open the lid during dum — the steam must stay trapped. 🔸 For extra richness, add fried nuts and raisins. 🔸 Layer the biryani in the reverse order for serving — vegetables at the top.",
        nutritionalInfo = "Approx: 350 cal/serving, 8g protein, 12g fat, 52g carbs",
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("power" to "LOW", "tempC" to 120, "durationMin" to 25)
    )

    private fun getKhichdiRecipe(): VegRecipe = VegRecipe(
        name = "Khichdi (Comfort Rice & Lentil Porridge)",
        cuisine = "Indian (Pan-India)",
        category = "Main Course",
        preparationTime = 10,
        cookingTime = 25,
        difficulty = "Easy",
        spiceLevel = "Mild",
        servings = 4,
        ingredients = listOf(
            "1 cup Rice",
            "½ cup Moong Dal (split green gram), washed",
            "2 tbsp Ghee",
            "1 tsp Cumin seeds",
            "½ tsp Turmeric powder",
            "1 tsp Ginger, grated",
            "2-3 Green Chillies, slit",
            "Asafoetida (Hing) — a pinch",
            "Salt to taste",
            "4 cups Water",
            "Fresh Coriander for garnish"
        ),
        steps = listOf(
            "STEP 1: Wash rice and moong dal together. Drain.",
            "STEP 2 — TEMPERING: Heat ghee in a pressure cooker. Add cumin seeds + hing. Let them crackle.",
            "STEP 3: Add ginger + green chillies. Sauté for 15 seconds.",
            "STEP 4: Add rice + dal + turmeric + salt + water. Mix well.",
            "STEP 5 — PRESSURE COOK: Close lid. Cook for 3-4 whistles. Let pressure release naturally.",
            "STEP 6 — OPEN: The khichdi should be soft and mushy. Add more hot water if needed.",
            "STEP 7: Check seasoning. Add a final tadka of ghee + cumin if desired.",
            "SERVE: Hot with yogurt + pickle + papad. The ultimate comfort food — also the first solid food for Indian babies."
        ),
        proTips = "PRO TIPS: 🔸 The ratio of rice:dal should be 2:1 for the perfect texture. 🔸 For a more digestive version, use only moong dal (no rice). 🔸 Ghee tadka at the end adds the 'khichdi aroma'. 🔸 Add seasonal vegetables (potato, peas, carrots) for a nutritious one-pot meal. 🔸 Traditionally eaten during illness recovery.",
        nutritionalInfo = "Approx: 220 cal/serving, 8g protein, 6g fat, 36g carbs",
        smartApplianceMode = "PRESSURE_COOKER",
        smartApplianceParams = mapOf("mode" to "RICE", "pressure" to "HIGH", "durationMin" to 15)
    )

    // ─── Desserts ─────────────────────────────────

    private fun getGulabJamunRecipe(): VegRecipe = VegRecipe(
        name = "Gulab Jamun (Milk Dumplings in Sugar Syrup)",
        cuisine = "Indian",
        category = "Dessert",
        preparationTime = 15,
        cookingTime = 25,
        difficulty = "Hard",
        spiceLevel = "Sweet",
        servings = 12,
        ingredients = listOf(
            "DUMPLINGS:",
            "1 cup Milk Powder",
            "¼ cup All-Purpose Flour (Maida)",
            "¼ tsp Baking Soda",
            "2 tbsp Ghee (melted)",
            "3-4 tbsp Milk (warm, as needed)",
            "Oil/Ghee for deep frying",
            "SUGAR SYRUP:",
            "2 cups Sugar",
            "2 cups Water",
            "4-5 Green Cardamoms, crushed",
            "1 tsp Rose water",
            "1 tsp Lemon juice (prevents crystallization)"
        ),
        steps = listOf(
            "SUGAR SYRUP (Make first):",
            "STEP 1: Boil sugar + water + cardamom. Stir until sugar dissolves completely.",
            "STEP 2: Simmer for 5 minutes until slightly sticky (1 string consistency). Add lemon juice + rose water. Keep warm.",
            "DOUGH:",
            "STEP 3: In a bowl, mix milk powder + flour + baking soda. Sift well.",
            "STEP 4: Add melted ghee. Mix with fingertips until crumbly.",
            "STEP 5: Add warm milk gradually and knead into a soft, smooth dough. Do not over-knead.",
            "STEP 6: Cover and rest for 10 minutes.",
            "SHAPING & FRYING:",
            "STEP 7: Divide dough into small portions. Roll into smooth balls (no cracks).",
            "STEP 8: Heat oil/ghee on LOW heat. The oil must not be hot — gulab jamuns fry on low heat.",
            "STEP 9: Gently slide balls into the warm oil. They will sink first, then float and slowly turn golden.",
            "STEP 10: Stir gently for even cooking. Fry for 5-7 minutes until deep golden brown.",
            "SOAKING:",
            "STEP 11: Drain fried balls on paper towel, then immediately transfer to warm sugar syrup.",
            "STEP 12: Let them soak for at least 2 hours before serving (they will double in size).",
            "SERVE: Warm or at room temperature. Garnish with chopped pistachios."
        ),
        proTips = "PRO TIPS: 🔸 The oil MUST be low heat — this is the most critical step! High heat burns the outside while the inside stays raw. 🔸 Add a crack (or the dough is too dry) — should be smooth. 🔸 The syrup should be warm (not hot) when adding the jamuns — hot syrup shrinks them. 🔸 Resting the fried jamuns in warm syrup for 2+ hours is non-negotiable for that melt-in-mouth texture. 🔸 A pinch of saffron in the syrup adds royal flavour.",
        nutritionalInfo = "Approx: 150 cal/jamun, 2g protein, 5g fat, 26g carbs"
    )

    private fun getGajarHalwaRecipe(): VegRecipe = VegRecipe(
        name = "Gajar Halwa (Carrot Halwa)",
        cuisine = "North Indian (Punjabi)",
        category = "Dessert",
        preparationTime = 20,
        cookingTime = 40,
        difficulty = "Medium",
        spiceLevel = "Sweet",
        servings = 6,
        ingredients = listOf(
            "1 kg Carrots (Gajar), grated",
            "1 litre Full-fat Milk",
            "½ cup Sugar (adjust to taste)",
            "4 tbsp Ghee",
            "¼ cup Condensed Milk (Milkmaid), optional",
            "10-12 Cashews, chopped",
            "10-12 Raisins (Kishmish)",
            "2-3 Green Cardamoms, powdered",
            "¼ tsp Nutmeg powder (optional)",
            "2 tbsp Mawa / Khoya (optional)"
        ),
        steps = listOf(
            "PREP: Wash, peel, and grate carrots. Ideally use red carrots (Delhi gajar) — they are sweeter and juicier.",
            "STEP 1 — MILK BASE: In a heavy bottom pan, bring milk to a boil on medium heat.",
            "STEP 2 — CARROTS: Add grated carrots. Stir well. Cook on medium heat, stirring frequently.",
            "STEP 3 — REDUCTION: The milk will reduce and the carrots will absorb it. Stir every 2-3 minutes to prevent sticking. This takes 20-25 minutes.",
            "STEP 4 — DRY: When the mixture becomes thick and almost dry (milk mostly absorbed), add 2 tbsp ghee. Stir well.",
            "STEP 5 — SWEETEN: Add sugar. The halwa will become liquid again — keep stirring.",
            "STEP 6 — CONDENSE: Cook until the halwa thickens and leaves the sides of the pan (10 minutes).",
            "STEP 7 — DRY FRUITS: In a small pan, heat 1 tbsp ghee. Fry cashews and raisins until golden. Add to halwa.",
            "STEP 8 — AROMA: Add cardamom powder + nutmeg powder + mawa (if using). Mix well.",
            "STEP 9 — FINISH: Add remaining ghee + condensed milk (if using). Stir until everything comes together.",
            "SERVE: Hot, garnished with more nuts. Top with a scoop of vanilla ice cream for the iconic 'halwa-ice cream' pairing."
        ),
        proTips = "PRO TIPS: 🔸 Red carrots (Delhi gajar) give the best colour and sweetness — use them in winter. 🔸 Patience is key — the slow reduction of milk gives the halwa its rich taste. 🔸 Do not add sugar too early — the halwa will become watery and won't thicken properly. 🔸 Stir continuously — gajar halwa burns easily. 🔸 The halwa tastes even better the next day (the flavours deepen).",
        nutritionalInfo = "Approx: 320 cal/serving, 6g protein, 14g fat, 44g carbs",
        smartApplianceMode = "INDUCTION",
        smartApplianceParams = mapOf("power" to "LOW-MEDIUM", "tempC" to 140, "durationMin" to 40)
    )

    private fun getKheerRecipe(): VegRecipe = VegRecipe(
        name = "Rice Kheer (Rice Pudding)",
        cuisine = "Indian (Pan-India)",
        category = "Dessert",
        preparationTime = 10,
        cookingTime = 30,
        difficulty = "Easy",
        spiceLevel = "Sweet",
        servings = 6,
        ingredients = listOf(
            "1 litre Full-fat Milk",
            "¼ cup Basmati Rice, washed and drained",
            "½ cup Sugar",
            "2 tbsp Condensed Milk (optional)",
            "10-12 Raisins",
            "8-10 Cashews, chopped",
            "10-12 Almonds, sliced",
            "3-4 Green Cardamoms, crushed",
            "¼ tsp Saffron strands soaked in 2 tbsp warm milk",
            "1 tsp Rose water (optional)"
        ),
        steps = listOf(
            "STEP 1 — BOIL: Bring milk to a boil in a heavy-bottomed pan. Stir occasionally to prevent scorching.",
            "STEP 2 — RICE: Add washed rice. Stir well. Reduce heat to medium-low.",
            "STEP 3 — SIMMER: Let it simmer, stirring every 2-3 minutes, until the rice is fully cooked and the milk has reduced by half (20-25 minutes).",
            "STEP 4 — SWEETEN: Add sugar. Stir until dissolved. The kheer will become thinner — don't worry, it will thicken as it cools.",
            "STEP 5 — DRY FRUITS: Heat 1 tsp ghee. Fry cashews, almonds, raisins until golden. Add to kheer.",
            "STEP 6 — AROMA: Add cardamom powder + saffron milk + rose water. Stir gently.",
            "STEP 7 — CONDENSE: Add condensed milk (if using). Simmer for 2 more minutes.",
            "STEP 8 — COOL: Remove from heat. Kheer thickens significantly as it cools.",
            "SERVE: Chilled (traditional) or warm. Garnish with silver leaf (varq) and sliced almonds."
        ),
        proTips = "PRO TIPS: 🔸 Use a heavy-bottomed pan to prevent milk from burning. 🔸 Stir frequently — milk can scorch silently. 🔸 The rice should be completely broken down and the kheer should be creamy. 🔸 For a quicker version, use pre-cooked rice. 🔸 For variation: sewai kheer (vermicelli), sabudana kheer (sago), or badam kheer (almond).",
        nutritionalInfo = "Approx: 220 cal/serving, 6g protein, 8g fat, 32g carbs"
    )

    private fun getJalebiRecipe(): VegRecipe = VegRecipe(
        name = "Jalebi (Crispy Sweet Swirls)",
        cuisine = "Indian (Pan-India)",
        category = "Dessert",
        preparationTime = 120, // includes fermentation
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Sweet",
        servings = 8,
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
            "BATTER PREPARATION (Make a day before):",
            "STEP 1: Mix flour + cornflour + curd + baking soda + colour + water to make a flowing batter (no lumps).",
            "STEP 2: Cover and set aside in a warm place for 8-12 hours (or overnight) to ferment.",
            "SUGAR SYRUP:",
            "STEP 3: Boil sugar + water + cardamom + lemon juice until 1-string consistency. Add rose water. Keep warm.",
            "FRYING & DIPPING:",
            "STEP 4: Heat oil/ghee on medium heat. Pour batter into a squeeze bottle or piping bag with a small hole.",
            "STEP 5: Squeeze batter into hot oil in a spiral motion — starting from centre, going outward.",
            "STEP 6: Fry until deep golden and crisp. The jalebis should be crispy, not soft.",
            "STEP 7: Immediately dip hot jalebis in warm sugar syrup for 30 seconds (not more — they should absorb syrup but remain crispy).",
            "SERVE: Hot and crispy. Piping hot jalebis with rabri (sweetened thickened milk) is the ultimate combination."
        ),
        proTips = "PRO TIPS: 🔸 Sour curd + overnight fermentation creates the tanginess and crispiness. 🔸 The batter should be slightly flowing, not thick. 🔸 The syrup should be warm, not hot — hot syrup makes jalebis soggy. 🔸 For perfect spirals, use a squeeze bottle with a small nozzle. 🔸 Jalebis must be served fresh — they lose crispiness with time.",
        nutritionalInfo = "Approx: 200 cal/serving, 2g protein, 6g fat, 38g carbs"
    )

    // ─── Snacks ───────────────────────────────────

    private fun getSamosaRecipe(): VegRecipe = VegRecipe(
        name = "Samosa (Crispy Potato-Stuffed Pastry)",
        cuisine = "North Indian",
        category = "Snack",
        preparationTime = 30,
        cookingTime = 20,
        difficulty = "Hard",
        spiceLevel = "Medium",
        servings = 8,
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
        nutritionalInfo = "Approx: 180 cal/samosa, 4g protein, 8g fat, 24g carbs"
    )

    // ─── Drinks ───────────────────────────────────

    private fun getLassiRecipe(): VegRecipe = VegRecipe(
        name = "Sweet Lassi (Punjabi Yogurt Drink)",
        cuisine = "North Indian (Punjabi)",
        category = "Drink",
        preparationTime = 5,
        cookingTime = 0,
        difficulty = "Easy",
        servings = 2,
        ingredients = listOf(
            "2 cups Thick Curd (Yogurt)",
            "4 tbsp Sugar (adjust to taste)",
            "½ cup Cold Milk",
            "½ tsp Cardamom powder",
            "2-3 Ice cubes",
            "Chopped pistachios + almonds for garnish",
            "Saffron strands (optional)"
        ),
        steps = listOf(
            "STEP 1: Add curd + sugar + cold milk + cardamom powder + ice cubes to a blender.",
            "STEP 2: Blend on high speed for 1-2 minutes until smooth and frothy.",
            "STEP 3: Pour into glasses.",
            "STEP 4: Garnish with chopped nuts + saffron strands.",
            "SERVE: Chilled. For salty lassi, skip sugar and add salt + cumin powder + roasted cumin."
        ),
        proTips = "PRO TIPS: 🔸 Use thick, fresh curd — sour curd will ruin the lassi. 🔸 Blend until the lassi is creamy and frothy. 🔸 For mango lassi, add ½ cup mango pulp. 🔸 For a thicker consistency, add 2 tbsp fresh cream. 🔸 Serve in tall glasses with a dollop of malai on top.",
        nutritionalInfo = "Approx: 180 cal/glass, 6g protein, 6g fat, 26g carbs"
    )

    // ─── Generic Guidance ─────────────────────────

    private fun getGenericVegGuidance(dishName: String): VegRecipe {
        return VegRecipe(
            name = dishName.ifBlank { "Vegetarian Indian Dish" },
            cuisine = "Indian",
            category = "Main Course",
            preparationTime = 20,
            cookingTime = 25,
            difficulty = "Medium",
            servings = 4,
            ingredients = listOf(
                "Please specify a dish name for detailed ingredients."
            ),
            steps = listOf(
                "I don't have a specific recipe for '$dishName' yet, but here's general guidance:",
                "Indian vegetarian cooking typically involves:",
                "1. Tadka (tempering) with ghee/oil + cumin/mustard seeds",
                "2. Sauté onions until golden brown",
                "3. Add ginger-garlic paste and spices",
                "4. Add tomato puree for gravy-based dishes",
                "5. Add main vegetable/ingredient + water",
                "6. Simmer until done",
                "7. Finish with garam masala + fresh coriander",
                "",
                "Do you have any of these dishes in mind? Paneer, Dal, Chole, Rajma, Biryani, Dosa, Idli, etc.",
                "Or would you like me to suggest a few famous vegetarian Indian dishes?"
            ),
            proTips = "The foundation of Indian vegetarian cooking: tadka (tempering) is the soul, fresh spices are essential, and patience with slow cooking develops deep flavours."
        )
    }

    // ─── Smart Kitchen Integration ───────────────

    /**
     * Get available smart appliances for cooking.
     */
    fun getAvailableAppliances(): List<SmartKitchenHardware> {
        // Placeholder — replace with actual IoT bridge query
        return listOf(
            SmartKitchenHardware(isConnected = false, applianceName = "Smart Induction", applianceType = "INDUCTION"),
            SmartKitchenHardware(isConnected = false, applianceName = "Microwave Oven", applianceType = "OVEN"),
            SmartKitchenHardware(isConnected = false, applianceName = "Air Fryer", applianceType = "AIR_FRYER"),
            SmartKitchenHardware(isConnected = false, applianceName = "Pressure Cooker", applianceType = "PRESSURE_COOKER")
        )
    }

    /**
     * Get smart cooking parameters for a recipe.
     */
    fun getSmartCookingParams(recipe: VegRecipe): Map<String, Any> {
        return recipe.smartApplianceParams
    }

    // ──────────────────────────────────────────────
    // Main Query Handler
    // ──────────────────────────────────────────────

    /**
     * Handle a vegetarian cooking query.
     */
    fun handleVegCookingQuery(query: String): String {
        val lower = query.lowercase(Locale.ROOT)

        return when {
            // Smart appliance query
            lower.contains("appliance") || lower.contains("device") || lower.contains("smart kitchen") || lower.contains("hardware") -> {
                val appliances = getAvailableAppliances()
                if (appliances.isEmpty() || appliances.all { !it.isConnected }) {
                    "राधे-राधे! 🙏\n\n" +
                    "आपके स्मार्ट किचन उपकरणों की स्थिति:\n" +
                    "🚫 कोई कनेक्टेड उपकरण नहीं मिला।\n\n" +
                    "मैं आपको स्टेप-बाय-स्टेप विधि से मार्गदर्शन करूँगी। " +
                    "स्मार्ट हार्डवेयर कनेक्ट होने पर ऑटोनॉमस कुकिंग उपलब्ध होगी।\n" +
                    "उपलब्ध उपकरण: ${appliances.joinToString { "${it.applianceName} (${it.applianceType})" }}"
                } else {
                    "राधे-राधे! स्मार्ट किचन devices ready हैं। " +
                    appliances.filter { it.isConnected }.joinToString(", ") { it.applianceName } +
                    " कनेक्टेड हैं। मैं खाना पकाने के लिए तैयार हूँ!"
                }
            }

            // Recipe query
            lower.contains("recipe") || lower.contains("व्यंजन") || lower.contains("रेसिपी") ||
            lower.contains("how to make") || lower.contains("कैसे बनाये") || lower.contains("kaise banaye") ||
            lower.contains("cook") || lower.contains("पकाना") ||
            lower.contains("paneer") || lower.contains("dal") || lower.contains("chole") ||
            lower.contains("rajma") || lower.contains("dosa") || lower.contains("idli") ||
            lower.contains("biryani") || lower.contains("pulao") || lower.contains("khichdi") ||
            lower.contains("roti") || lower.contains("paratha") || lower.contains("naan") ||
            lower.contains("samosa") || lower.contains("dhokla") || lower.contains("khandvi") ||
            lower.contains("gulab") || lower.contains("jalebi") || lower.contains("halwa") ||
            lower.contains("kheer") || lower.contains("lassi") ||
            lower.contains("subji") || lower.contains("sabzi") || lower.contains("curry") ||
            lower.contains("bhindi") || lower.contains("baingan") || lower.contains("aloo") ||
            lower.contains("matar") || lower.contains("palak") || lower.contains("shahi") ||
            lower.contains("korma") || lower.contains("kofta") || lower.contains("biryani") -> {
                val recipe = getVegRecipe(query)
                formatRecipeResponse(recipe)
            }

            // Smart kitchen cooking
            lower.contains("smart cook") || lower.contains("auto cook") || lower.contains("autonomous cook") ||
            (lower.contains("cook") && (lower.contains("automatically") || lower.contains("device"))) -> {
                val appliances = getAvailableAppliances()
                if (appliances.any { it.isConnected }) {
                    "राधे-राधे! 🙏\n\n" +
                    "🚀 ऑटोनॉमस कुकिंग के लिए कनेक्टेड उपकरण: ${appliances.filter { it.isConnected }.joinToString { it.applianceName }}\n\n" +
                    "कृपया मुझे बताएं कौन सा व्यंजन बनाना है और मैं स्मार्ट उपकरणों को निर्देश दूंगी।\n" +
                    "उदाहरण: 'Butter Paneer auto cook' या 'Dal Makhani pressure cooker में बनाओ'"
                } else {
                    "राधे-राधे! 🙏\n\n" +
                    "कोई स्मार्ट किचन हार्डवेयर कनेक्टेड नहीं है। मैं आपको विस्तृत स्टेप-बाय-स्टेप गाइडेंस दे सकती हूँ।\n\n" +
                    "कृपया रेसिपी का नाम बताएं। उदाहरण: 'Butter Paneer recipe' या 'Dal Tadka kaise banaye'"
                }
            }

            // Default
            else -> {
                "राधे-राधे! 🙏\n\n" +
                "🍽️ मैं आपका वेजिटेरियन इंडियन मास्टरशेफ़ हूँ! आप क्या पकाना चाहेंगे?\n\n" +
                "मेरे पास हजारों व्यंजन हैं:\n" +
                "• पनीर व्यंजन: Butter Paneer, Shahi Paneer, Palak Paneer, Kadai Paneer\n" +
                "• दालें: Dal Makhani, Dal Tadka, Sambhar, Rasam\n" +
                "• सब्जियाँ: Chole, Rajma, Bhindi, Aloo Gobi, Baingan Bharta\n" +
                "• दक्षिण भारतीय: Dosa, Idli, Vada, Uttapam\n" +
                "• गुजराती: Dhokla, Khandvi, Thepla, Undhiyu\n" +
                "• ब्रेड: Roti, Paratha, Naan, Puri\n" +
                "• चावल: Veg Biryani, Pulao, Khichdi\n" +
                "• मिठाई: Gulab Jamun, Gajar Halwa, Kheer, Jalebi\n" +
                "• पेय: Lassi, Masala Chai\n\n" +
                "बस नाम बताएं! (Just tell me the dish name!)"
            }
        }
    }

    private fun formatRecipeResponse(recipe: VegRecipe): String {
        return "राधे-राधे! 🙏\n\n" +
        "🍽️ **${recipe.name}**\n" +
        "• व्यंजन (Cuisine): ${recipe.cuisine}\n" +
        "• श्रेणी (Category): ${recipe.category}\n" +
        "• तैयारी (Preparation): ${recipe.preparationTime} min\n" +
        "• पकाने का समय (Cooking): ${recipe.cookingTime} min\n" +
        "• कुल समय (Total): ${recipe.preparationTime + recipe.cookingTime} min\n" +
        "• कठिनाई (Difficulty): ${recipe.difficulty}\n" +
        "• मसाला स्तर (Spice): ${recipe.spiceLevel}\n" +
        "• सर्विंग (Servings): ${recipe.servings}\n\n" +
        "📝 **सामग्री (Ingredients):**\n" +
        recipe.ingredients.joinToString("\n") { "• $it" } + "\n\n" +
        "📋 **विधि (Steps):**\n" +
        recipe.steps.joinToString("\n") { "$it" } + "\n\n" +
        "💡 **प्रो टिप्स:**\n${recipe.proTips}\n\n" +
        "🥗 **पोषण जानकारी (Nutritional Info):**\n${recipe.nutritionalInfo}\n\n" +
        "🙏 राधे-राधे! बोन एपेटिट!"
    }
}