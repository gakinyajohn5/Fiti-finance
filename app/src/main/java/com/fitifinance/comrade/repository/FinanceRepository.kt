package com.fitifinance.comrade.repository

import com.fitifinance.comrade.data.AppDatabase
import com.fitifinance.comrade.data.entity.*
import com.fitifinance.comrade.engine.AllocationPlan
import com.fitifinance.comrade.engine.DamageControlSummary
import com.fitifinance.comrade.engine.SavingsAllocator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FinanceRepository(private val db: AppDatabase) {

    // ---- Profile ----
    fun observeProfile(): Flow<UserProfile?> = db.userProfileDao().observeProfile()
    /** Same as observeProfile() but never emits null, for screens that assume a default persona. */
    fun observeProfileFlowSafe(): Flow<UserProfile> = db.userProfileDao().observeProfile().map { it ?: UserProfile() }
    suspend fun getProfile(): UserProfile = db.userProfileDao().getProfile() ?: UserProfile()
    suspend fun saveProfile(profile: UserProfile) = db.userProfileDao().upsert(profile)

    // ---- Transactions ----
    fun observeTransactions(): Flow<List<Transaction>> = db.transactionDao().observeAll()
    fun observePendingPrompts(): Flow<List<Transaction>> = db.transactionDao().observePendingPrompts()

    suspend fun insertTransaction(transaction: Transaction): Long = db.transactionDao().insert(transaction)

    suspend fun resolvePendingTransaction(
        transaction: Transaction,
        category: TransactionCategory,
        alwaysRemember: Boolean
    ) {
        db.transactionDao().update(transaction.copy(category = category, needsUserPrompt = false))
        if (alwaysRemember) {
            db.recipientDao().upsert(Recipient(transaction.counterparty, category, true))
        }
    }

    suspend fun findRecipientRule(name: String): Recipient? = db.recipientDao().findByName(name)

    /** Real-time deduction hook: applies a spend against the relevant running budget. */
    suspend fun applySpendToBudget(category: TransactionCategory, amountKes: Double) {
        // Budgets are derived on-the-fly from transaction sums (see DashboardViewModel),
        // so no separate mutable balance needs to be persisted here. This hook exists
        // as the integration point for push-notification "balance updated" alerts.
    }

    // ---- Meals ----
    suspend fun logMeal(mealLog: MealLog): Long = db.mealLogDao().insert(mealLog)
    fun observeMealsBetween(start: Long, end: Long): Flow<List<MealLog>> = db.mealLogDao().observeBetween(start, end)
    fun observeMealSpendBetween(start: Long, end: Long): Flow<Double> = db.mealLogDao().observeTotalSpentBetween(start, end)

    // ---- Night Out ----
    suspend fun logDrink(drinkLog: DrinkLog): Long = db.drinkLogDao().insert(drinkLog)
    fun observeDrinksBetween(start: Long, end: Long): Flow<List<DrinkLog>> = db.drinkLogDao().observeBetween(start, end)
    fun observeNightOutSpendBetween(start: Long, end: Long): Flow<Double> = db.drinkLogDao().observeSpentBetween(start, end)
    fun observePendingReceivables(): Flow<Double> = db.drinkLogDao().observeTotalPendingReceivables()

    // ---- Savings ----
    fun observeActiveJars(): Flow<List<SavingsJar>> = db.savingsJarDao().observeActiveJars()
    suspend fun addJar(jar: SavingsJar): Long = db.savingsJarDao().insert(jar)
    suspend fun updateJar(jar: SavingsJar) = db.savingsJarDao().update(jar)
    suspend fun deleteJar(jar: SavingsJar) = db.savingsJarDao().delete(jar)

    /** Auto-Allocation: directs surplus funds into active goal jars by priority. */
    suspend fun autoAllocateSurplus(surplusKes: Double): AllocationPlan {
        val jars = db.savingsJarDao().getActiveJars()
        val plan = SavingsAllocator.allocateSurplus(surplusKes, jars)
        plan.updatedJars.forEach { db.savingsJarDao().update(it) }
        return plan
    }

    /** Applies a conversational top-up like "Put KES 500 in my Laptop fund". */
    suspend fun topUpJarByNameFragment(nameFragment: String, amountKes: Double): Boolean {
        val jars = db.savingsJarDao().getActiveJars()
        val target = jars.firstOrNull { it.goalName.contains(nameFragment, ignoreCase = true) } ?: return false
        db.savingsJarDao().update(target.copy(currentAmountKes = target.currentAmountKes + amountKes))
        return true
    }

    // ---- Damage Control summary (morning-after) ----
    suspend fun buildDamageControlSummary(sinceMillis: Long, untilMillis: Long): DamageControlSummary {
        val nightOutSpend = db.drinkLogDao().observeSpentBetween(sinceMillis, untilMillis).first()
        val receivables = db.drinkLogDao().observeTotalPendingReceivables().first()
        val profile = getProfile()
        val remainingWeekBudget = (profile.dailyMealBudgetKes * 7) - nightOutSpend
        val adjustedDailyCap = (remainingWeekBudget / 7).coerceAtLeast(0.0)
        return DamageControlSummary(
            totalSpentKes = nightOutSpend,
            pendingReceivablesKes = receivables,
            adjustedDailyFoodCapKes = adjustedDailyCap
        )
    }
}
