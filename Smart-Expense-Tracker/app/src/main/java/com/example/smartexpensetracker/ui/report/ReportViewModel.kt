package com.example.smartexpensetracker.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensetracker.data.model.ExpenseCategory
import com.example.smartexpensetracker.data.repo.ExpenseRepository
import com.example.smartexpensetracker.util.Formatters
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

data class DailyTotal(val date: LocalDate, val totalPaise: Long)

data class ReportUiState(
    val last7DailyTotals: List<DailyTotal> = emptyList(),
    val categoryTotals: Map<ExpenseCategory, Long> = emptyMap(),
)

class ReportViewModel(
    repository: ExpenseRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    val uiState: StateFlow<ReportUiState> = repository.expenses.map { list ->
        val today = LocalDate.now(zoneId)
        val days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
        val daily = days.map { d ->
            val sum = list.filter { Formatters.instantToLocalDate(it.timestamp, zoneId) == d }
                .sumOf { it.amountInPaise }
            DailyTotal(d, sum)
        }
        val categoryTotals = list
            .filter { Formatters.instantToLocalDate(it.timestamp, zoneId) >= today.minusDays(6) }
            .groupBy { it.category }
            .mapValues { (_, v) -> v.sumOf { it.amountInPaise } }
        ReportUiState(last7DailyTotals = daily, categoryTotals = categoryTotals)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ReportUiState())
}