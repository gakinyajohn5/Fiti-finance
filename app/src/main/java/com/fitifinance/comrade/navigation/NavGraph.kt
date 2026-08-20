package com.fitifinance.comrade.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fitifinance.comrade.ui.dashboard.DashboardScreen
import com.fitifinance.comrade.ui.meals.MealPlannerScreen
import com.fitifinance.comrade.ui.nightout.NightOutScreen
import com.fitifinance.comrade.ui.onboarding.OnboardingWizard
import com.fitifinance.comrade.ui.savings.SavingsJarsScreen
import com.fitifinance.comrade.ui.transactions.TransactionsScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val MEALS = "meals"
    const val NIGHT_OUT = "night_out"
    const val SAVINGS = "savings"
    const val TRANSACTIONS = "transactions"
}

@Composable
fun FitiNavGraph(startDestination: String) {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            OnboardingWizard(onComplete = {
                navController.navigate(Routes.DASHBOARD) {
                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                }
            })
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onOpenMeals = { navController.navigate(Routes.MEALS) },
                onOpenNightOut = { navController.navigate(Routes.NIGHT_OUT) },
                onOpenSavings = { navController.navigate(Routes.SAVINGS) },
                onOpenTransactions = { navController.navigate(Routes.TRANSACTIONS) }
            )
        }

        composable(Routes.MEALS) {
            MealPlannerScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.NIGHT_OUT) {
            NightOutScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SAVINGS) {
            SavingsJarsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.TRANSACTIONS) {
            TransactionsScreen(onBack = { navController.popBackStack() })
        }
    }
}
