package com.example.smartexpensetracker.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import com.example.smartexpensetracker.data.model.Expense
import com.example.smartexpensetracker.util.Formatters
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ListScreen(
    viewModel: ListViewModel,
    onNavigateReport: () -> Unit,
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncPending() }) {
                        Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync")
                    }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Seed demo data") }, onClick = {
                            menuExpanded = false
                            viewModel.seedDemo()
                        })
                        DropdownMenuItem(text = { Text("Reset") }, onClick = {
                            menuExpanded = false
                            viewModel.resetAll()
                        })
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                var showPicker by remember { mutableStateOf(false) }
                Button(onClick = { viewModel.setDate(LocalDate.now()) }) { Text("Today") }
                Button(onClick = { viewModel.showAll() }) { Text("All") }
                Button(onClick = { showPicker = true }) { Text("Pick Date") }
                Button(onClick = { viewModel.toggleGroup() }) { Text("Group: ${state.groupBy}") }
                Button(onClick = onNavigateReport) { Text("Report") }
                if (showPicker) {
                    val pickerState = rememberDatePickerState()
                    DatePickerDialog(onDismissRequest = { showPicker = false }, confirmButton = {
                        Button(onClick = {
                            val millis = pickerState.selectedDateMillis
                            if (millis != null) {
                                val date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                                viewModel.setDate(date)
                            }
                            showPicker = false
                        }) { Text("OK") }
                    }) { DatePicker(state = pickerState) }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Total: ${Formatters.paiseToRupeesString(state.totalAmountPaise)} (${state.totalCount})", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            if (state.items.isEmpty()) {
                Text("No expenses for selected date.")
            } else {
                LazyColumn {
                    items(state.items) { e -> ExpenseRow(e) }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(expense.title, style = MaterialTheme.typography.titleMedium)
                if (expense.isPendingSync) AssistChip(onClick = {}, label = { Text("Pending") })
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(Formatters.paiseToRupeesString(expense.amountInPaise))
            Spacer(modifier = Modifier.height(2.dp))
            val dateStr = Formatters.formatDate(Formatters.instantToLocalDate(expense.timestamp))
            val timeStr = Formatters.formatTime(Formatters.instantToLocalTime(expense.timestamp))
            Text("${expense.category} • $dateStr • $timeStr")
        }
    }
}