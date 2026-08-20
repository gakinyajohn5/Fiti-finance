package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.DrinkLog
import com.fitifinance.comrade.data.entity.UserProfile
import com.fitifinance.comrade.engine.BillSplitCalculator
import com.fitifinance.comrade.engine.DrinkDatabase
import com.fitifinance.comrade.engine.DrinkItem
import com.fitifinance.comrade.engine.FareShield
import com.fitifinance.comrade.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class NightOutState(
    val profile: UserProfile = UserProfile(),
    val drinks: List<DrinkItem> = DrinkDatabase.items,
    val tonightSpendKes: Double = 0.0,
    val spendableBalanceKes: Double = 0.0,
    val fareShieldBreached: Boolean = false,
    val pendingReceivablesKes: Double = 0.0,
    val lastLogFeedback: String? = null
)

class NightOutViewModel(private val repository: FinanceRepository) : ViewModel() {

    private fun startOfDayMillis(): Long =
        LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val uiState: StateFlow<NightOutState> = combine(
        repository.observeProfileFlowSafe(),
        repository.observeNightOutSpendBetween(startOfDayMillis(), System.currentTimeMillis()),
        repository.observePendingReceivables()
    ) { profile, spend, receivables ->
        val spendable = FareShield.spendableBalance(profile.nightOutBudgetKes, profile.fareShieldKes)
        NightOutState(
            profile = profile,
            tonightSpendKes = spend,
            spendableBalanceKes = spendable,
            fareShieldBreached = FareShield.wouldBreachShield(spend, profile.nightOutBudgetKes, profile.fareShieldKes),
            pendingReceivablesKes = receivables
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NightOutState())

    /** Full Payment: entire drink cost hits the night-out budget. */
    fun logFullPayment(drink: DrinkItem, totalBillKes: Double) {
        viewModelScope.launch {
            val result = BillSplitCalculator.fullPayment(totalBillKes)
            repository.logDrink(
                DrinkLog(
                    drinkId = drink.id,
                    drinkName = drink.name,
                    totalBillKes = totalBillKes,
                    isSplit = false,
                    splitCount = 1,
                    personalShareKes = result.personalShareKes,
                    pendingReceivableKes = result.pendingReceivableKes,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        }
    }

    /** Split Bill Calculator: divides among N comrades, logs the remainder as a pending receivable. */
    fun logSplitBill(drink: DrinkItem, totalBillKes: Double, numberOfComrades: Int) {
        viewModelScope.launch {
            val result = BillSplitCalculator.splitBill(totalBillKes, numberOfComrades)
            repository.logDrink(
                DrinkLog(
                    drinkId = drink.id,
                    drinkName = drink.name,
                    totalBillKes = totalBillKes,
                    isSplit = true,
                    splitCount = numberOfComrades,
                    personalShareKes = result.personalShareKes,
                    pendingReceivableKes = result.pendingReceivableKes,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        }
    }
}
