package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.UserProfile
import com.fitifinance.comrade.engine.LocationContextEngine
import com.fitifinance.comrade.engine.ThemeMode
import com.fitifinance.comrade.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

data class DashboardState(
    val profile: UserProfile = UserProfile(),
    val todayMealSpendKes: Double = 0.0,
    val monthSpendKes: Double = 0.0,
    val pendingPromptCount: Int = 0,
    val activeJarCount: Int = 0
)

class DashboardViewModel(
    private val repository: FinanceRepository,
    private val locationEngine: LocationContextEngine
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = locationEngine.currentMode

    private fun startOfDayMillis(): Long =
        LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun startOfMonthMillis(): Long =
        LocalDate.now(ZoneId.systemDefault()).withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun now(): Long = System.currentTimeMillis()

    val uiState: StateFlow<DashboardState> = combine(
        repository.observeProfile(),
        repository.observeMealSpendBetween(startOfDayMillis(), now()),
        repository.observeTransactions(),
        repository.observePendingPrompts(),
        repository.observeActiveJars()
    ) { profile, mealSpend, transactions, pending, jars ->
        val monthSpend = transactions
            .filter { it.timestampMillis >= startOfMonthMillis() }
            .sumOf { it.amountKes }
        DashboardState(
            profile = profile ?: UserProfile(),
            todayMealSpendKes = mealSpend,
            monthSpendKes = monthSpend,
            pendingPromptCount = pending.size,
            activeJarCount = jars.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    /** Manual mode override for devices/emulators without live location (demo-friendly). */
    fun previewThemeMode(mode: ThemeMode) {
        // Directly reflects into the shared engine's flow for a consistent demo experience.
        locationEngine.forceMode(mode)
    }
}
