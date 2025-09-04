package com.example.smartexpensetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses")
    suspend fun getAll(): List<ExpenseEntity>

    @Query("SELECT * FROM expenses WHERE timestampEpochMillis BETWEEN :start AND :end ORDER BY timestampEpochMillis DESC")
    suspend fun getByTimeRange(start: Long, end: Long): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExpenseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun clear()

    @Query("UPDATE expenses SET isPendingSync = 0 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)
}