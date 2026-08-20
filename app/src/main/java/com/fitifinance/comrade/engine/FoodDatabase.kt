package com.fitifinance.comrade.engine

import com.fitifinance.comrade.data.entity.MealWindow

/**
 * A single suggestable food/meal card. `photoQuery` is a short descriptive
 * string the UI can use to resolve an image (mandatory item photo per the
 * blueprint) — kept as a query rather than a bundled resource so the app
 * doesn't need to ship dozens of drawables.
 */
data class FoodItem(
    val id: String,
    val name: String,
    val window: MealWindow,
    val costKes: Double,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val vendorTag: String,
    val requiresCooking: Boolean, // true if it needs a kitchen, not just a kettle/single burner
    val kibandaFriendly: Boolean,
    val photoQuery: String
)

object FoodDatabase {

    val items: List<FoodItem> = listOf(
        // ---- 06:00 – 10:59 Breakfast ----
        FoodItem("bf_tea", "Tea (Chai)", MealWindow.BREAKFAST, 20.0, 2, 12, 3, "Mess / Kibanda", false, true, "kenyan chai tea"),
        FoodItem("bf_mandazi", "Mandazi (2 pcs)", MealWindow.BREAKFAST, 30.0, 3, 28, 6, "Kibanda", false, true, "mandazi kenyan snack"),
        FoodItem("bf_smokies", "Smokies", MealWindow.BREAKFAST, 50.0, 8, 6, 12, "Street Vendor", false, true, "smokie sausage kenya"),
        FoodItem("bf_mess_tea", "Mess Tea + Bread", MealWindow.BREAKFAST, 60.0, 6, 30, 8, "Campus Mess", false, false, "tea bread campus mess"),

        // ---- 11:00 – 15:59 Lunch ----
        FoodItem("ln_chapati_beans", "Chapati Beans", MealWindow.LUNCH, 80.0, 14, 60, 10, "Kibanda", true, true, "chapati beans kenya"),
        FoodItem("ln_rice_ndengu", "Rice Ndengu", MealWindow.LUNCH, 70.0, 12, 65, 6, "Kibanda", true, true, "rice ndengu green grams"),
        FoodItem("ln_mess_veggie", "Mess Veggie Combo", MealWindow.LUNCH, 90.0, 10, 55, 8, "Campus Mess", false, false, "campus mess plate vegetables"),

        // ---- 16:00 – 21:59 Supper ----
        FoodItem("sp_ugali_mayai_sukuma", "Ugali Mayai Sukuma", MealWindow.SUPPER, 90.0, 16, 70, 14, "Kibanda", true, true, "ugali eggs sukuma wiki"),
        FoodItem("sp_kibanda_fry", "Kibanda Fry", MealWindow.SUPPER, 100.0, 18, 40, 20, "Kibanda", true, true, "kibanda fried food kenya"),
        FoodItem("sp_rice_beans", "Rice Beans", MealWindow.SUPPER, 80.0, 13, 68, 7, "Kibanda / Home", true, true, "rice beans plate"),

        // ---- 22:00 – 05:59 Late Night ----
        FoodItem("ln2_noodles", "Instant Noodles", MealWindow.LATE_NIGHT, 50.0, 5, 45, 15, "Room / Kettle", false, true, "instant noodles cup"),
        FoodItem("ln2_leftovers", "Leftovers", MealWindow.LATE_NIGHT, 0.0, 10, 40, 10, "Room", false, false, "leftover food container"),
        FoodItem("ln2_pasua", "Pasua", MealWindow.LATE_NIGHT, 60.0, 12, 30, 18, "Kibanda", false, true, "pasua roast meat kenya"),
        FoodItem("ln2_smocha", "Smocha", MealWindow.LATE_NIGHT, 70.0, 14, 42, 16, "Street Vendor", false, true, "smocha chips sausage")
    )

    fun forWindow(window: MealWindow): List<FoodItem> = items.filter { it.window == window }

    fun byId(id: String): FoodItem? = items.find { it.id == id }
}
