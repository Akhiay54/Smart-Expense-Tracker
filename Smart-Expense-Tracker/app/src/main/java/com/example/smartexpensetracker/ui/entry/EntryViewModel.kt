package com.example.smartexpensetracker.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartexpensetracker.data.model.Expense
import com.example.smartexpensetracker.data.model.ExpenseCategory
import com.example.smartexpensetracker.data.repo.ExpenseRepository
import com.example.smartexpensetracker.util.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID

data class EntryUiState(
    val title: String = "",
    val amountText: String = "",
    val category: ExpenseCategory = ExpenseCategory.Utility,
    val notes: String = "",
    val receiptUri: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val duplicateWarning: Boolean = false,
    val totalTodayFormatted: String = "₹0",
    val date: LocalDate = LocalDate.now(),
)

class EntryViewModel(
    private val repository: ExpenseRepository,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {

    private val internalState = MutableStateFlow(EntryUiState())

    val uiState: StateFlow<EntryUiState> = combine(
        internalState,
        repository.expenses.map { expenses ->
            val today = LocalDate.now(zoneId)
            val totalPaise = expenses
                .filter { Formatters.instantToLocalDate(it.timestamp, zoneId) == today }
                .sumOf { it.amountInPaise }
            Formatters.paiseToRupeesString(totalPaise)
        }
    ) { state, totalToday ->
        state.copy(totalTodayFormatted = totalToday)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EntryUiState())

    fun onTitleChange(value: String) { internalState.value = internalState.value.copy(title = value) }
    fun onAmountChange(value: String) { internalState.value = internalState.value.copy(amountText = value) }
    fun onCategoryChange(value: ExpenseCategory) { internalState.value = internalState.value.copy(category = value) }
    fun onNotesChange(value: String) {
        internalState.value = internalState.value.copy(notes = value.take(100))
    }
    fun onReceiptPicked(uri: String?) { internalState.value = internalState.value.copy(receiptUri = uri) }
    fun clearError() { internalState.value = internalState.value.copy(errorMessage = null, duplicateWarning = false) }
    fun onDateChange(date: LocalDate) { internalState.value = internalState.value.copy(date = date) }

    private fun findDuplicateNow(amountPaise: Long, title: String): Boolean {
        val nowDate = LocalDate.now(zoneId)
        val now = Instant.now()
        // Consider duplicate if within last 5 minutes (300 seconds)
        val thresholdSeconds = 300L
        val expenses = repository.expenses.value
        return expenses.any { e ->
            val sameDay = Formatters.instantToLocalDate(e.timestamp, zoneId) == nowDate
            val recent = kotlin.runCatching { java.time.Duration.between(e.timestamp, now).seconds }
                .getOrDefault(Long.MAX_VALUE) <= thresholdSeconds
            sameDay && recent && e.amountInPaise == amountPaise && e.title.trim().equals(title.trim(), ignoreCase = true)
        }
    }

    fun submit() {
        val s = internalState.value
        val title = s.title.trim()
        if (title.isEmpty()) {
            internalState.value = s.copy(errorMessage = "Title cannot be empty")
            return
        }
        val amountPaise = Formatters.parseRupeesToPaiseOrNull(s.amountText)
        if (amountPaise == null) {
            internalState.value = s.copy(errorMessage = "Enter a valid amount > 0")
            return
        }
        val notes = if (s.notes.isBlank()) null else s.notes.trim()

        val isDuplicate = findDuplicateNow(amountPaise, title)
        if (isDuplicate && !s.duplicateWarning) {
            internalState.value = s.copy(duplicateWarning = true, errorMessage = "Possible duplicate. Tap Submit again to confirm.")
            return
        }

        internalState.value = s.copy(isSubmitting = true, errorMessage = null, duplicateWarning = false)
        viewModelScope.launch {
            val chosenInstant = ZonedDateTime.of(
                internalState.value.date,
                LocalTime.now(zoneId),
                zoneId
            ).toInstant()

            val expense = Expense(
                id = UUID.randomUUID().toString(),
                title = title,
                amountInPaise = amountPaise,
                category = s.category,
                notes = notes,
                receiptUri = s.receiptUri,
                timestamp = chosenInstant,
                isPendingSync = true
            )
            repository.add(expense)
            // Reset form except category
            internalState.value = EntryUiState(category = s.category)
        }
    }
}