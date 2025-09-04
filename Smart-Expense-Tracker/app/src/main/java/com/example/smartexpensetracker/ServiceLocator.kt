package com.example.smartexpensetracker

import android.content.Context
import androidx.room.Room
import com.example.smartexpensetracker.data.local.AppDatabase
import com.example.smartexpensetracker.data.repo.ExpenseRepository
import com.example.smartexpensetracker.data.repo.ExpenseRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object ServiceLocator {
    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var repository: ExpenseRepository? = null

    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun getRepository(context: Context): ExpenseRepository {
        val cached = repository
        if (cached != null) return cached
        synchronized(this) {
            val db = database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "smart-expense-db"
            ).fallbackToDestructiveMigration().build().also { database = it }

            val repo = ExpenseRepositoryImpl(db.expenseDao(), appScope)
            repository = repo
            return repo
        }
    }
}