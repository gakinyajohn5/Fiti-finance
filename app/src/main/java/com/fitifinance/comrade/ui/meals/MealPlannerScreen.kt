@file:OptIn(ExperimentalMaterial3Api::class)
package com.fitifinance.comrade.ui.meals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.engine.FoodItem
import com.fitifinance.comrade.viewmodel.MealsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun MealPlannerScreen(onBack: () -> Unit) {
    val vm: MealsViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    var commandText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Planner — ${state.currentWindowLabel}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {

            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Remaining today", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "KES ${state.remainingAllowanceKes.toInt()}",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Spent today", style = MaterialTheme.typography.bodyMedium)
                        Text("KES ${state.todaySpendKes.toInt()}", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            state.lastCommandFeedback?.let { feedback ->
                Spacer(Modifier.height(8.dp))
                AssistChip(onClick = { vm.clearFeedback() }, label = { Text(feedback) })
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = commandText,
                onValueChange = { commandText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("\"I ate lunch, minus 100 KES\" or \"swap supper to Rice Beans\"") },
                trailingIcon = {
                    IconButton(onClick = {
                        if (commandText.isNotBlank()) {
                            vm.submitCommand(commandText)
                            commandText = ""
                        }
                    }) { Icon(Icons.Filled.Send, contentDescription = "Submit") }
                }
            )

            Spacer(Modifier.height(16.dp))
            Text("Suggested for right now", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(state.suggestions) { food ->
                    FoodCard(food, onLog = { vm.logMeal(food) })
                }
            }
        }
    }
}

@Composable
private fun FoodCard(food: FoodItem, onLog: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(food.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("KES ${food.costKes.toInt()}", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(food.vendorTag, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "P ${food.proteinG}g · C ${food.carbsG}g · F ${food.fatG}g",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(10.dp))
            Button(onClick = onLog, modifier = Modifier.align(Alignment.End)) {
                Text("Log this meal")
            }
        }
    }
}
