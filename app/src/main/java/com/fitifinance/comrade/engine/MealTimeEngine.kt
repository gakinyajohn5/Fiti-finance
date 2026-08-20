package com.fitifinance.comrade.engine

import com.fitifinance.comrade.data.entity.MealWindow
import java.time.LocalTime

/**
 * Time-Based Detection Windows (java.time.LocalTime), matching the blueprint:
 *   06:00–10:59 Breakfast | 11:00–15:59 Lunch | 16:00–21:59 Supper | 22:00–05:59 Late Night
 */
object MealTimeEngine {

    private val breakfastStart = LocalTime.of(6, 0)
    private val lunchStart = LocalTime.of(11, 0)
    private val supperStart = LocalTime.of(16, 0)
    private val lateNightStart = LocalTime.of(22, 0)

    fun currentWindow(now: LocalTime = LocalTime.now()): MealWindow = when {
        !now.isBefore(breakfastStart) && now.isBefore(lunchStart) -> MealWindow.BREAKFAST
        !now.isBefore(lunchStart) && now.isBefore(supperStart) -> MealWindow.LUNCH
        !now.isBefore(supperStart) && now.isBefore(lateNightStart) -> MealWindow.SUPPER
        else -> MealWindow.LATE_NIGHT // 22:00 - 05:59
    }

    fun windowLabel(window: MealWindow): String = when (window) {
        MealWindow.BREAKFAST -> "Breakfast"
        MealWindow.LUNCH -> "Lunch"
        MealWindow.SUPPER -> "Supper"
        MealWindow.LATE_NIGHT -> "Late Night"
    }
}

/**
 * Parses simple natural-language / voice budget tweak commands, e.g.
 * "I already ate lunch, minus 100 KES" or "Swap Thursday supper to Rice Beans".
 * This is a lightweight on-device rule parser (no network call needed).
 */
sealed class MealCommand {
    data class DeductAmount(val amountKes: Double, val note: String) : MealCommand()
    data class SwapMeal(val day: String?, val window: MealWindow?, val newMealName: String) : MealCommand()
    object Unrecognized : MealCommand()
}

object MealCommandParser {

    private val deductRegex = Regex("""minus\s*(?:ksh?|kes)?\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE)
    private val swapRegex = Regex(
        """swap\s+(?:(\w+)\s+)?(breakfast|lunch|supper|late\s*night)\s+to\s+(.+)""",
        RegexOption.IGNORE_CASE
    )

    fun parse(text: String): MealCommand {
        val trimmed = text.trim()

        swapRegex.find(trimmed)?.let { match ->
            val (day, windowStr, mealName) = match.destructured
            val window = when (windowStr.lowercase().replace(" ", "")) {
                "breakfast" -> MealWindow.BREAKFAST
                "lunch" -> MealWindow.LUNCH
                "supper" -> MealWindow.SUPPER
                "latenight" -> MealWindow.LATE_NIGHT
                else -> null
            }
            return MealCommand.SwapMeal(day.ifBlank { null }, window, mealName.trim())
        }

        deductRegex.find(trimmed)?.let { match ->
            val amount = match.groupValues[1].toDoubleOrNull() ?: return@let
            return MealCommand.DeductAmount(amount, trimmed)
        }

        return MealCommand.Unrecognized
    }
}
