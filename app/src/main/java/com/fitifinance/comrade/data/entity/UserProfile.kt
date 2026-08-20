package com.fitifinance.comrade.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class LivingSituation {
    IN_CAMPUS_NO_COOKING,
    IN_CAMPUS_COOKING_ALLOWED,
    OFF_CAMPUS_BEDSITTER
}

enum class RoommateStatus {
    SOLO,
    WITH_ROOMMATES
}

enum class MealStyle {
    CHEF_COMRADE,
    KIBANDA_LOYALIST
}

enum class PartyStatus {
    REGULARLY,
    OCCASIONALLY,
    NEVER
}

enum class PartyFrequency {
    WEEKLY,
    BI_WEEKLY,
    OCCASIONAL,
    NOT_APPLICABLE
}

/**
 * Single-row table (id is always 1) holding the Comrade Persona built by the
 * onboarding wizard. Drives dynamic dashboard visibility across the app.
 */
@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val onboardingComplete: Boolean = false,

    // Step 1
    val livingSituation: LivingSituation = LivingSituation.OFF_CAMPUS_BEDSITTER,

    // Step 2
    val roommateStatus: RoommateStatus = RoommateStatus.SOLO,

    // Step 3
    val mealStyle: MealStyle = MealStyle.KIBANDA_LOYALIST,

    // Step 4
    val partyStatus: PartyStatus = PartyStatus.NEVER,
    val partyFrequency: PartyFrequency = PartyFrequency.NOT_APPLICABLE,
    val favoriteDrinkIds: String = "", // comma-separated DrinkItem ids

    // Budgeting
    val dailyMealBudgetKes: Double = 300.0,
    val monthlyBudgetKes: Double = 12000.0,
    val nightOutBudgetKes: Double = 1500.0,
    val fareShieldKes: Double = 150.0 // Emergency Fare Shield reserve
) {
    /** The Party Dashboard is entirely hidden when the comrade never drinks. */
    val partyDashboardEnabled: Boolean get() = partyStatus != PartyStatus.NEVER
    val canCook: Boolean get() = livingSituation != LivingSituation.IN_CAMPUS_NO_COOKING
    val hasBulkCooking: Boolean get() = livingSituation == LivingSituation.OFF_CAMPUS_BEDSITTER
}
