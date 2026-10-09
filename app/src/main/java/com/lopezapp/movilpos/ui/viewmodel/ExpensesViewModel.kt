package com.lopezapp.movilpos.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.lopezapp.movilpos.data.model.Expense
import com.lopezapp.movilpos.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpensesViewModel(
    private val repository: AppRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allExpenses: StateFlow<List<Expense>> = repository.expenses

    val expenses: StateFlow<List<Expense>> = combine(
        repository.expenses,
        _searchQuery
    ) { expenseList, query ->
        val filtered = if (query.isBlank()) {
            expenseList
        } else {
            expenseList.filter { expense ->
                expense.description.contains(query, ignoreCase = true) ||
                        expense.category.contains(query, ignoreCase = true) ||
                        expense.cashierName.contains(query, ignoreCase = true)
            }
        }
        filtered.sortedByDescending { it.dateMillis }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val totalExpensesAmount: StateFlow<Double> = expenses.map { list ->
        list.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0.0
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addExpense(category: String, description: String, amount: Double) {
        viewModelScope.launch {
            val expense = Expense(
                category = category,
                description = description,
                amount = amount
            )
            repository.addExpense(expense)
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            repository.deleteExpense(expenseId)
        }
    }

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpensesViewModel::class.java)) {
                return ExpensesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
