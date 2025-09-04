package com.example.smartexpensetracker.data.repo

import com.example.smartexpensetracker.data.local.ExpenseDao
import com.example.smartexpensetracker.data.local.ExpenseEntity
import com.example.smartexpensetracker.data.model.Expense
import com.example.smartexpensetracker.data.model.ExpenseCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.Instant

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao,
    private val externalScope: CoroutineScope,
) : ExpenseRepository {

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    override val expenses: StateFlow<List<Expense>> = _expenses

    init {
        externalScope.launch(Dispatchers.IO) { refreshFromDb() }
    }

    private suspend fun refreshFromDb() {
        val items = dao.getAll().map { it.toModel() }
        _expenses.value = items
    }

    override suspend fun add(expense: Expense) {
        dao.upsert(expense.toEntity())
        refreshFromDb()
    }

    override suspend fun upsertAll(expenses: List<Expense>) {
        dao.upsertAll(expenses.map { it.toEntity() })
        refreshFromDb()
    }

    override suspend fun clear() {
        dao.clear()
        refreshFromDb()
    }

    override suspend fun markSynced(ids: List<String>) {
        dao.markSynced(ids)
        refreshFromDb()
    }
}

private fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    title = title,
    amountInPaise = amountInPaise,
    category = category.name,
    notes = notes,
    receiptUri = receiptUri,
    timestampEpochMillis = timestamp.toEpochMilli(),
    isPendingSync = isPendingSync,
)

private fun ExpenseEntity.toModel(): Expense = Expense(
    id = id,
    title = title,
    amountInPaise = amountInPaise,
    category = runCatching { ExpenseCategory.valueOf(category) }.getOrElse { ExpenseCategory.Utility },
    notes = notes,
    receiptUri = receiptUri,
    timestamp = Instant.ofEpochMilli(timestampEpochMillis),
    isPendingSync = isPendingSync,
)