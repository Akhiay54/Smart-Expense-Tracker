package com.example.smartexpensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amountInPaise: Long,
    val category: String,
    val notes: String?,
    val receiptUri: String?,
    val timestampEpochMillis: Long,
    val isPendingSync: Boolean,
)