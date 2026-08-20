package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.fitifinance.comrade.FitiApplication

/**
 * Simple factory that hands every ViewModel the FinanceRepository (+ engines)
 * owned by FitiApplication, avoiding a full DI framework for this scope.
 */
class FitiViewModelFactory(private val app: FitiApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            OnboardingViewModel::class.java -> OnboardingViewModel(app.repository) as T
            DashboardViewModel::class.java -> DashboardViewModel(app.repository, app.locationContextEngine) as T
            MealsViewModel::class.java -> MealsViewModel(app.repository) as T
            NightOutViewModel::class.java -> NightOutViewModel(app.repository) as T
            SavingsViewModel::class.java -> SavingsViewModel(app.repository, app.adviceEngine) as T
            TransactionsViewModel::class.java -> TransactionsViewModel(app.repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

/** Convenience composable: `fitiViewModel<DashboardViewModel>()` */
@Composable
inline fun <reified VM : ViewModel> fitiViewModel(): VM {
    val context = LocalContext.current
    val app = context.applicationContext as FitiApplication
    return viewModel(factory = FitiViewModelFactory(app))
}
