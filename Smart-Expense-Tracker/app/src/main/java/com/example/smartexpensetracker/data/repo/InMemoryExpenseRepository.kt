package com.example.smartexpensetracker.data.repo

import com.example.smartexpensetracker.data.model.Expense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class InMemoryExpenseRepository : ExpenseRepository {
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    override val expenses: StateFlow<List<Expense>> = _expenses

    override suspend fun add(expense: Expense) = withContext(Dispatchers.Default) {
        _expenses.value = _expenses.value + expense
    }

    override suspend fun upsertAll(expenses: List<Expense>) = withContext(Dispatchers.Default) {
        // Simple replace strategy for in-memory store
        _expenses.value = expenses
    }

    override suspend fun clear() = withContext(Dispatchers.Default) {
        _expenses.value = emptyList()
    }

    override suspend fun markSynced(ids: List<String>) = withContext(Dispatchers.Default) {
        if (ids.isEmpty()) return@withContext
        _expenses.value = _expenses.value.map { e ->
            if (ids.contains(e.id)) e.copy(isPendingSync = false) else e
        }
    }
}