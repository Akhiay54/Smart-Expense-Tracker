package com.example.smartexpensetracker.ui.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smartexpensetracker.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { 
        TopAppBar(
            title = { Text("7-Day Report") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
    },
        ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Daily Totals (last 7 days)")
            Spacer(Modifier.height(8.dp))
            DailyBarChart(
                values = state.last7DailyTotals.map { it.totalPaise },
                labels = state.last7DailyTotals.map { it.date.dayOfWeek.name.take(3) }
            )
            Spacer(Modifier.height(16.dp))
            Text("Category Totals")
            val catValues = state.categoryTotals.values.toList()
            if (catValues.isNotEmpty()) {
                DailyBarChart(values = catValues, labels = state.categoryTotals.keys.map { it.name })
                Spacer(Modifier.height(8.dp))
            }
            state.categoryTotals.forEach { (cat, total) ->
                Text("$cat: ${Formatters.paiseToRupeesString(total)}")
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onExportCsv) { Text("Export CSV") }
                Button(onClick = onExportPdf) { Text("Export PDF") }
            }
        }
    }
}

@Composable
private fun DailyBarChart(values: List<Long>, labels: List<String>, modifier: Modifier = Modifier.fillMaxWidth()) {
    val max = (values.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val barCount = values.size
    val barWidthFraction = 0.7f
    Canvas(modifier = modifier.height(160.dp)) {
        val widthPerBar = size.width / barCount
        values.forEachIndexed { index, v ->
            val fraction = v.toFloat() / max.toFloat()
            val barHeight = size.height * fraction
            val left = widthPerBar * index + (widthPerBar * (1 - barWidthFraction) / 2)
            val right = left + widthPerBar * barWidthFraction
            drawRect(
                color = Color(0xFF6750A4),
                topLeft = androidx.compose.ui.geometry.Offset(left, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(right - left, barHeight)
            )
        }
        // Baseline
        drawLine(
            color = Color.LightGray,
            start = androidx.compose.ui.geometry.Offset(0f, size.height),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height),
            strokeWidth = 2f,
            cap = Stroke.DefaultCap
        )
    }
}