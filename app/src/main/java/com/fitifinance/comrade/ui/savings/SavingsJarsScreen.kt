@file:OptIn(ExperimentalMaterial3Api::class)
package com.fitifinance.comrade.ui.savings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.data.entity.SavingsJar
import com.fitifinance.comrade.viewmodel.SavingsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun SavingsJarsScreen(onBack: () -> Unit) {
    val vm: SavingsViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()
    var showAddJar by remember { mutableStateOf(false) }
    var chatText by remember { mutableStateOf("") }
    var surplusInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Jars") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddJar = true }) { Icon(Icons.Filled.Add, contentDescription = "Add Jar") }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {

            OutlinedTextField(
                value = surplusInput,
                onValueChange = { surplusInput = it.filter { c -> c.isDigit() } },
                label = { Text("Today's surplus (KES) for AI advice") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        surplusInput.toDoubleOrNull()?.let { vm.requestPurchasingAdvice(it) }
                    }) { Icon(Icons.Filled.AutoAwesome, contentDescription = "Get advice") }
                }
            )

            state.advice?.let { advice ->
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(advice, Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = chatText,
                onValueChange = { chatText = it },
                label = { Text("\"Put 500 in my Laptop fund and 200 in food\"") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        if (chatText.isNotBlank()) { vm.submitConversationalTopUp(chatText); chatText = "" }
                    }) { Icon(Icons.Filled.Send, contentDescription = "Submit") }
                }
            )
            state.commandFeedback?.let {
                Spacer(Modifier.height(6.dp))
                AssistChip(onClick = { vm.clearFeedback() }, label = { Text(it) })
            }

            Spacer(Modifier.height(16.dp))
            Text("Your Goals", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(state.jars) { jar ->
                    JarCard(jar, onDelete = { vm.deleteJar(jar) })
                }
            }
        }
    }

    if (showAddJar) {
        AddJarDialog(
            onDismiss = { showAddJar = false },
            onConfirm = { name, target, priority ->
                vm.addJar(name, target, priority)
                showAddJar = false
            }
        )
    }
}

@Composable
private fun JarCard(jar: SavingsJar, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(jar.goalName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { jar.progressFraction },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "KES ${jar.currentAmountKes.toInt()} / ${jar.targetAmountKes.toInt()}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun AddJarDialog(onDismiss: () -> Unit, onConfirm: (String, Double, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Goal Jar") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = target, onValueChange = { target = it.filter { c -> c.isDigit() } },
                    label = { Text("Target amount (KES)") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = priority, onValueChange = { priority = it.filter { c -> c.isDigit() } },
                    label = { Text("Priority (1 = highest)") }, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val t = target.toDoubleOrNull() ?: return@TextButton
                val p = priority.toIntOrNull() ?: 1
                if (name.isNotBlank()) onConfirm(name, t, p)
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
