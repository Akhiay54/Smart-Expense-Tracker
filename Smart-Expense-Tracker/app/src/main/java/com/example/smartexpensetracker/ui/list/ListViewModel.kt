package com.example.smartexpensetracker.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensetracker.data.model.Expense
import com.example.smartexpensetracker.data.model.ExpenseCategory
import com.example.smartexpensetracker.data.repo.ExpenseRepository
import com.example.smartexpensetracker.util.Formatters
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.random.Random

enum class GroupBy { Time, Category }

data class ListUiState(
    val selectedDate: LocalDate?,
    val groupBy: GroupBy = GroupBy.Time,
    val items: List<Expense> = emptyList(),
    val totalCount: Int = 0,
    val totalAmountPaise: Long = 0L,
    val isSyncing: Boolean = false,
)

class ListViewModel(
    private val repository: ExpenseRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val selectedDate = MutableStateFlow<LocalDate?>(LocalDate.now(zoneId))
    private val groupBy = MutableStateFlow(GroupBy.Time)
    private val syncing = MutableStateFlow(false)

    val uiState: StateFlow<ListUiState> = combine(
        selectedDate,
        groupBy,
        repository.expenses,
        syncing,
    ) { date, group, expenses, isSyncing ->
        val filtered = if (date == null) expenses else expenses.filter { Formatters.instantToLocalDate(it.timestamp, zoneId) == date }
        val totalPaise = filtered.sumOf { it.amountInPaise }
        ListUiState(
            selectedDate = date,
            groupBy = group,
            items = when (group) {
                GroupBy.Time -> filtered.sortedByDescending { it.timestamp }
                GroupBy.Category -> filtered.sortedWith(compareBy<Expense> { it.category.name }.thenByDescending { it.timestamp })
            },
            totalCount = filtered.size,
            totalAmountPaise = totalPaise,
            isSyncing = isSyncing,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ListUiState(LocalDate.now(zoneId)))

    fun setDate(date: LocalDate) { selectedDate.value = date }
    fun showAll() { selectedDate.value = null }
    fun toggleGroup() { groupBy.value = if (groupBy.value == GroupBy.Time) GroupBy.Category else GroupBy.Time }

    fun syncPending() {
        if (syncing.value) return
        viewModelScope.launch {
            syncing.value = true
            delay(1200)
            val pendingIds = repository.expenses.value.filter { it.isPendingSync }.map { it.id }
            repository.markSynced(pendingIds)
            syncing.value = false
        }
    }

    fun seedDemo() {
        viewModelScope.launch {
            val today = LocalDate.now(zoneId)
            val sample = mutableListOf<Expense>()
            val categories = ExpenseCategory.values()
            var counter = 0
            for (d in 0..6) {
                val date = today.minusDays(d.toLong())
                val count = 2 + (counter++ % 3) // 2..4 entries per day
                repeat(count) { idx ->
                    val amountPaise = (1000L..15000L).random() // ₹10.00 .. ₹150.00
                    val cat = categories[(idx + d) % categories.size]
                    val title = "${cat.name} Expense ${idx + 1}"
                    val instant = date.atStartOfDay().toInstant(ZoneOffset.UTC).plusSeconds((idx * 3600).toLong())
                    sample.add(
                        Expense(
                            id = "seed-${d}-${idx}-${Random.nextInt(0, 9999)}",
                            title = title,
                            amountInPaise = amountPaise,
                            category = cat,
                            notes = null,
                            receiptUri = null,
                            timestamp = instant,
                            isPendingSync = false,
                        )
                    )
                }
            }
            repository.upsertAll(sample)
            selectedDate.value = today
        }
    }

    fun resetAll() {
        viewModelScope.launch {
            repository.clear()
            selectedDate.value = LocalDate.now(zoneId)
        }
    }
}