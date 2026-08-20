package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.MealLog
import com.fitifinance.comrade.data.entity.UserProfile
import com.fitifinance.comrade.engine.FoodDatabase
import com.fitifinance.comrade.engine.FoodItem
import com.fitifinance.comrade.engine.MealCommand
import com.fitifinance.comrade.engine.MealCommandParser
import com.fitifinance.comrade.engine.MealTimeEngine
import com.fitifinance.comrade.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class MealsState(
    val profile: UserProfile = UserProfile(),
    val currentWindowLabel: String = "",
    val suggestions: List<FoodItem> = emptyList(),
    val todaySpendKes: Double = 0.0,
    val remainingAllowanceKes: Double = 0.0,
    val lastCommandFeedback: String? = null
)

class MealsViewModel(private val repository: FinanceRepository) : ViewModel() {

    private val _manualAdjustmentKes = MutableStateFlow(0.0)
    private val _feedback = MutableStateFlow<String?>(null)

    private fun startOfDayMillis(): Long =
        LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val uiState: StateFlow<MealsState> = combine(
        repository.observeProfileFlowSafe(),
        repository.observeMealSpendBetween(startOfDayMillis(), System.currentTimeMillis()),
        _manualAdjustmentKes,
        _feedback
    ) { profile, spend, manualAdj, feedback ->
        val window = MealTimeEngine.currentWindow()
        val allSuggestions = FoodDatabase.forWindow(window)
        val filtered = if (!profile.canCook) {
            // Hostel, no cooking: campus mess, kibanda, ready-to-eat street snacks only.
            allSuggestions.filter { it.vendorTag != "Room / Kettle" }
        } else allSuggestions

        val totalSpend = spend + manualAdj
        MealsState(
            profile = profile,
            currentWindowLabel = MealTimeEngine.windowLabel(window),
            suggestions = filtered,
            todaySpendKes = totalSpend,
            remainingAllowanceKes = (profile.dailyMealBudgetKes - totalSpend).coerceAtLeast(0.0),
            lastCommandFeedback = feedback
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MealsState())

    fun logMeal(item: FoodItem) {
        viewModelScope.launch {
            repository.logMeal(
                MealLog(
                    mealItemId = item.id,
                    mealName = item.name,
                    costKes = item.costKes,
                    window = item.window,
                    vendorTag = item.vendorTag,
                    timestampMillis = System.currentTimeMillis()
                )
            )
            _feedback.value = "Logged ${item.name} — KES ${item.costKes.toInt()} deducted from today's allowance."
        }
    }

    /** Handles natural-language tweaks like "I already ate lunch, minus 100 KES". */
    fun submitCommand(text: String) {
        when (val command = MealCommandParser.parse(text)) {
            is MealCommand.DeductAmount -> {
                _manualAdjustmentKes.value += command.amountKes
                _feedback.value = "Deducted KES ${command.amountKes.toInt()} from today's allowance."
            }
            is MealCommand.SwapMeal -> {
                _feedback.value = "Noted — swap to \"${command.newMealName}\" saved for " +
                    (command.day?.let { "$it " } ?: "") + (command.window?.let { MealTimeEngine.windowLabel(it) } ?: "that slot") + "."
            }
            MealCommand.Unrecognized -> {
                _feedback.value = "Didn't catch that — try \"minus 100 KES\" or \"swap Thursday supper to Rice Beans\"."
            }
        }
    }

    fun clearFeedback() {
        _feedback.value = null
    }
}
