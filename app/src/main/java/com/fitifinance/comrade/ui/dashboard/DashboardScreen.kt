package com.fitifinance.comrade.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.engine.ThemeMode
import com.fitifinance.comrade.viewmodel.DashboardViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun DashboardScreen(
    onOpenMeals: () -> Unit,
    onOpenNightOut: () -> Unit,
    onOpenSavings: () -> Unit,
    onOpenTransactions: () -> Unit
) {
    val vm: DashboardViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    val theme by vm.themeMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Habari, Comrade 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Mode: ${theme.name.lowercase().replaceFirstChar { it.uppercase() }}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(12.dp))
        ThemeModeSwitcher(current = theme, onSelect = vm::previewThemeMode)

        Spacer(Modifier.height(20.dp))
        StatCardRow(state.todayMealSpendKes, state.remainingLabel())

        Spacer(Modifier.height(20.dp))
        Text("Quick Access", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        DashboardTile(Icons.Filled.Restaurant, "Meal Planner", "Time-aware suggestions for right now", onOpenMeals)
        if (state.profile.partyDashboardEnabled) {
            DashboardTile(Icons.Filled.LocalBar, "Night-Out & Party", "Track drinks, split bills, fare shield", onOpenNightOut)
        }
        DashboardTile(Icons.Filled.Savings, "Savings Jars", "${state.activeJarCount} active goal(s)", onOpenSavings)
        DashboardTile(
            Icons.Filled.Receipt, "Transactions",
            if (state.pendingPromptCount > 0) "${state.pendingPromptCount} need categorizing" else "All caught up",
            onOpenTransactions
        )
    }
}

private fun com.fitifinance.comrade.viewmodel.DashboardState.remainingLabel(): String =
    "KES ${monthSpendKes.toInt()} spent this month"

@Composable
private fun ThemeModeSwitcher(current: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
            ThemeMode.CAMPUS to "Campus",
            ThemeMode.BAR to "Bar",
            ThemeMode.KIBANDA to "Kibanda"
        ).forEach { (mode, label) ->
            FilterChip(selected = current == mode, onClick = { onSelect(mode) }, label = { Text(label) })
        }
    }
}

@Composable
private fun StatCardRow(todaySpendKes: Double, subtitle: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("Today's Meal Spend", style = MaterialTheme.typography.bodyMedium)
            Text("KES ${todaySpendKes.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun DashboardTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null)
        }
    }
}
