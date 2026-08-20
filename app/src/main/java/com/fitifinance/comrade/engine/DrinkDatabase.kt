package com.fitifinance.comrade.engine

enum class DrinkCategory { TRADITIONAL_BREW, SHOT, QUARTER_BEER, FULL_BOTTLE }

data class DrinkItem(
    val id: String,
    val name: String,
    val category: DrinkCategory,
    val servingSize: String,
    val typicalPriceKes: Double,
    val photoQuery: String
)

object DrinkDatabase {

    val items: List<DrinkItem> = listOf(
        // Traditional Brews
        DrinkItem("muratina", "Muratina", DrinkCategory.TRADITIONAL_BREW, "500ml Calabash", 150.0, "muratina traditional brew calabash"),

        // Small Shots / Tots
        DrinkItem("vodka_shot", "Vodka Single Shot", DrinkCategory.SHOT, "30ml", 100.0, "vodka shot glass"),
        DrinkItem("gin_shot", "Gin Single Shot", DrinkCategory.SHOT, "30ml", 100.0, "gin shot glass"),
        DrinkItem("whiskey_shot", "Whiskey Shot", DrinkCategory.SHOT, "30ml", 150.0, "whiskey shot glass"),
        DrinkItem("brandy_shot", "Brandy Shot", DrinkCategory.SHOT, "30ml", 150.0, "brandy shot glass"),

        // Quarters & Beers
        DrinkItem("chrome", "Chrome", DrinkCategory.QUARTER_BEER, "250ml Quarter", 180.0, "chrome vodka bottle kenya"),
        DrinkItem("konyagi", "Konyagi", DrinkCategory.QUARTER_BEER, "250ml Quarter", 200.0, "konyagi bottle"),
        DrinkItem("kc", "KC", DrinkCategory.QUARTER_BEER, "250ml Quarter", 180.0, "kc spirit kenya bottle"),
        DrinkItem("guarana", "Guarana", DrinkCategory.QUARTER_BEER, "250ml", 150.0, "guarana drink kenya"),
        DrinkItem("tusker", "Tusker", DrinkCategory.QUARTER_BEER, "500ml", 250.0, "tusker beer bottle"),
        DrinkItem("pilsner", "Pilsner", DrinkCategory.QUARTER_BEER, "500ml", 230.0, "pilsner beer bottle kenya"),
        DrinkItem("balozi", "Balozi", DrinkCategory.QUARTER_BEER, "500ml", 220.0, "balozi beer bottle"),

        // Full Bottles
        DrinkItem("gilbeys", "Gilbeys", DrinkCategory.FULL_BOTTLE, "750ml", 1500.0, "gilbeys gin bottle"),
        DrinkItem("best_gin", "Best Gin", DrinkCategory.FULL_BOTTLE, "750ml", 1300.0, "best gin bottle kenya"),
        DrinkItem("captain_morgan", "Captain Morgan", DrinkCategory.FULL_BOTTLE, "750ml", 2200.0, "captain morgan rum bottle")
    )

    fun byId(id: String): DrinkItem? = items.find { it.id == id }
    fun byCategory(category: DrinkCategory): List<DrinkItem> = items.filter { it.category == category }
}
