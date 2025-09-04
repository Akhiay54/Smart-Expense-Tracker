package com.example.smartexpensetracker.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.platform.LocalContext
import com.example.smartexpensetracker.ui.entry.EntryScreen
import com.example.smartexpensetracker.ui.entry.EntryViewModel
import com.example.smartexpensetracker.ui.list.ListScreen
import com.example.smartexpensetracker.ui.list.ListViewModel
import com.example.smartexpensetracker.ui.report.ReportScreen
import com.example.smartexpensetracker.ui.report.ReportViewModel
import com.example.smartexpensetracker.export.ExportUtils

object Routes {
    const val ENTRY = "entry"
    const val LIST = "list"
    const val REPORT = "report"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.ENTRY,
        modifier = modifier
    ) {
        composable(Routes.ENTRY) {
            val context = LocalContext.current
            val repo = com.example.smartexpensetracker.ServiceLocator.getRepository(context)
            val vm = EntryViewModel(repo)
            EntryScreen(viewModel = vm, onNavigateList = { navController.navigate(Routes.LIST) })
        }
        composable(Routes.LIST) {
            val context = LocalContext.current
            val repo = com.example.smartexpensetracker.ServiceLocator.getRepository(context)
            val vm = ListViewModel(repo)
            ListScreen(viewModel = vm, onNavigateReport = { navController.navigate(Routes.REPORT) }, onBack = { navController.popBackStack() })
        }
        composable(Routes.REPORT) {
            val context = LocalContext.current
            val repo = com.example.smartexpensetracker.ServiceLocator.getRepository(context)
            val vm = ReportViewModel(repo)
            ReportScreen(viewModel = vm, onExportCsv = {
                val file = ExportUtils.exportCsv(context, repo.expenses.value)
                ExportUtils.shareFile(context, file, "text/csv")
            }, onExportPdf = {
                val lines = buildList {
                    add("7-Day Report")
                    add("")
                    vm.uiState.value.categoryTotals.forEach { (cat, total) ->
                        add("${cat}: ${com.example.smartexpensetracker.util.Formatters.paiseToRupeesString(total)}")
                    }
                }
                val pdf = ExportUtils.exportPdf(context, "Smart Expense Report", lines)
                ExportUtils.shareFile(context, pdf, "application/pdf")
            }, onBack = { navController.popBackStack() })
        }
    }
}