package com.example.smartexpensetracker.data.repo

import com.example.smartexpensetracker.data.model.Expense
import kotlinx.coroutines.flow.StateFlow

interface ExpenseRepository {
    val expenses: StateFlow<List<Expense>>

    suspend fun add(expense: Expense)

    suspend fun upsertAll(expenses: List<Expense>)

    suspend fun clear()

    suspend fun markSynced(ids: List<String>)
}