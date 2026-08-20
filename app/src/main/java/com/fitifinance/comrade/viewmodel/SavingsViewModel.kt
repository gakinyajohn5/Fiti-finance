package com.fitifinance.comrade.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitifinance.comrade.data.entity.SavingsJar
import com.fitifinance.comrade.engine.AdviceResult
import com.fitifinance.comrade.engine.AiAdviceEngine
import com.fitifinance.comrade.engine.GoalEditingParser
import com.fitifinance.comrade.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavingsState(
    val jars: List<SavingsJar> = emptyList(),
    val advice: String? = null,
    val commandFeedback: String? = null
)

class SavingsViewModel(
    private val repository: FinanceRepository,
    private val adviceEngine: AiAdviceEngine
) : ViewModel() {

    private val _advice = MutableStateFlow<String?>(null)
    private val _feedback = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SavingsState> = combine(
        repository.observeActiveJars(), _advice, _feedback
    ) { jars, advice, feedback ->
        SavingsState(jars = jars, advice = advice, commandFeedback = feedback)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SavingsState())

    fun addJar(goalName: String, targetAmountKes: Double, priority: Int) {
        viewModelScope.launch {
            repository.addJar(SavingsJar(goalName = goalName, targetAmountKes = targetAmountKes, priority = priority))
        }
    }

    fun deleteJar(jar: SavingsJar) {
        viewModelScope.launch { repository.deleteJar(jar) }
    }

    /** Purchasing Advisor: "You have KES 2,400 surplus. Fund your Gamepad goal or reserve 4 days of meal money." */
    fun requestPurchasingAdvice(surplusKes: Double) {
        viewModelScope.launch {
            val jars = uiState.value.jars
            val result: AdviceResult = adviceEngine.purchasingAdvice(surplusKes, jars)
            _advice.value = result.message
        }
    }

    /** Conversational Goal Editing: "Put KES 500 in my Laptop fund and KES 200 in food". */
    fun submitConversationalTopUp(text: String) {
        viewModelScope.launch {
            val commands = GoalEditingParser.parse(text)
            if (commands.isEmpty()) {
                _feedback.value = "Couldn't parse that — try \"Put 500 in my Laptop fund\"."
                return@launch
            }
            val results = commands.map { cmd ->
                val applied = repository.topUpJarByNameFragment(cmd.jarNameFragment, cmd.amountKes)
                cmd to applied
            }
            val summary = results.joinToString("; ") { (cmd, applied) ->
                if (applied) "KES ${cmd.amountKes.toInt()} → ${cmd.jarNameFragment}"
                else "No jar matching \"${cmd.jarNameFragment}\""
            }
            _feedback.value = summary
        }
    }

    fun clearFeedback() {
        _feedback.value = null
        _advice.value = null
    }
}
