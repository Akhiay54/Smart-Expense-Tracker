package com.example.smartexpensetracker.data.model

import java.time.Instant

/**
 * Immutable representation of a single expense entry.
 *
 * Monetary values are stored as paise (Long) to avoid floating point errors.
 */
data class Expense(
    val id: String,
    val title: String,
    val amountInPaise: Long,
    val category: ExpenseCategory,
    val notes: String?,
    val receiptUri: String?,
    val timestamp: Instant,
    val isPendingSync: Boolean,
)