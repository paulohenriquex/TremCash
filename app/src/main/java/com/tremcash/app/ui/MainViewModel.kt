package com.tremcash.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tremcash.app.model.Transaction
import com.tremcash.app.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    fun loadTransactions() {
        viewModelScope.launch {
            _transactions.value = repository.getAllTransactions()
        }
    }

    fun addTransaction(transaction: Transaction, installments: Int) {
        repository.insertTransaction(transaction, installments)
        loadTransactions()
    }

    fun updateTransaction(transaction: Transaction) {
        repository.updateTransaction(transaction)
        loadTransactions()
    }

    fun deleteTransaction(id: Int) {
        repository.deleteTransaction(id)
        loadTransactions()
    }

    fun deleteTransactionGroup(groupId: String) {
        repository.deleteTransactionGroup(groupId)
        loadTransactions()
    }

    fun getExpensesByCategory(): Map<String, Double> {
        val currentMonth = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())

        return _transactions.value
            .filter { it.type == com.tremcash.app.model.TransactioType.DESPESA && it.date.startsWith(currentMonth) }
            .groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.value } }
    }
}