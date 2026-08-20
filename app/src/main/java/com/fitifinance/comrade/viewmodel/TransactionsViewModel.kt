package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.Transaction
import com.fitifinance.comrade.data.entity.TransactionCategory
import com.fitifinance.comrade.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TransactionsState(
    val recent: List<Transaction> = emptyList(),
    val pendingPrompts: List<Transaction> = emptyList()
)

/** Handles the P2P "You sent KES 500 to [Name]. What was this for?" interactive prompt. */
class TransactionsViewModel(private val repository: FinanceRepository) : ViewModel() {

    val uiState: StateFlow<TransactionsState> = combine(
        repository.observeTransactions(),
        repository.observePendingPrompts()
    ) { recent, pending ->
        TransactionsState(recent = recent, pendingPrompts = pending)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionsState())

    val quickCategories = listOf(
        TransactionCategory.FAMILY_BLACK_TAX,
        TransactionCategory.SHARED_HOUSE_EXPENSES,
        TransactionCategory.LOAN_DEBT_REPAYMENT,
        TransactionCategory.CHAMA_SOCIAL
    )

    fun resolvePrompt(transaction: Transaction, category: TransactionCategory, alwaysRemember: Boolean) {
        viewModelScope.launch {
            repository.resolvePendingTransaction(transaction, category, alwaysRemember)
        }
    }
}
