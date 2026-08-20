@file:OptIn(ExperimentalMaterial3Api::class)
package com.fitifinance.comrade.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitifinance.comrade.data.entity.Transaction
import com.fitifinance.comrade.data.entity.TransactionCategory
import com.fitifinance.comrade.viewmodel.TransactionsViewModel
import com.fitifinance.comrade.viewmodel.fitiViewModel

@Composable
fun TransactionsScreen(onBack: () -> Unit) {
    val vm: TransactionsViewModel = fitiViewModel()
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            if (state.recent.isEmpty()) {
                Text(
                    "No transactions yet. M-PESA SMS confirmations are parsed automatically once permission is granted.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            LazyColumn {
                items(state.recent) { txn -> TransactionRow(txn) }
            }
        }
    }

    // Pop the "What was this for?" sheet for the oldest unresolved P2P transfer.
    state.pendingPrompts.firstOrNull()?.let { txn ->
        CategorizationPromptSheet(
            transaction = txn,
            categories = vm.quickCategories,
            onResolve = { category, remember -> vm.resolvePrompt(txn, category, remember) }
        )
    }
}

@Composable
private fun TransactionRow(txn: Transaction) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(txn.counterparty, style = MaterialTheme.typography.titleMedium)
                Text(txn.category.name.replace('_', ' '), style = MaterialTheme.typography.bodyMedium)
            }
            Text("KES ${txn.amountKes.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorizationPromptSheet(
    transaction: Transaction,
    categories: List<TransactionCategory>,
    onResolve: (TransactionCategory, Boolean) -> Unit
) {
    var alwaysRemember by remember(transaction.id) { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = { /* Must resolve to dismiss — keeps categorization honest. */ }) {
        Column(Modifier.padding(24.dp)) {
            Text(
                "You sent KES ${transaction.amountKes.toInt()} to ${transaction.counterparty}. What was this for?",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(16.dp))

            categories.forEach { category ->
                OutlinedButton(
                    onClick = { onResolve(category, alwaysRemember) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(category.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = alwaysRemember, onCheckedChange = { alwaysRemember = it })
                Spacer(Modifier.width(4.dp))
                Text("Always remember this rule for ${transaction.counterparty}")
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
