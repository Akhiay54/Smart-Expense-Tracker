package com.example.smartexpensetracker.export

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.smartexpensetracker.data.model.Expense
import com.example.smartexpensetracker.util.Formatters
import java.io.File
import java.io.FileOutputStream
import java.time.ZoneId

object ExportUtils {

    fun exportCsv(context: Context, expenses: List<Expense>): File {
        val file = File(context.cacheDir, "expenses-${System.currentTimeMillis()}.csv")
        val header = "date,title,amount,category,notes\n"
        val rows = expenses.joinToString("\n") { e ->
            val d = Formatters.instantToLocalDate(e.timestamp, ZoneId.systemDefault())
            val amt = Formatters.paiseToRupeesString(e.amountInPaise)
            val notes = e.notes?.replace(',', ';') ?: ""
            "${d},${e.title.replace(',', ';')},${amt},${e.category},${notes}"
        }
        file.writeText(header + rows)
        return file
    }

    fun exportPdf(context: Context, title: String, lines: List<String>): File {
        val file = File(context.cacheDir, "report-${System.currentTimeMillis()}.pdf")
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4-ish
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply { textSize = 14f }
        var y = 40f
        canvas.drawText(title, 40f, y, paint)
        y += 24f
        lines.forEach {
            if (y > 800f) return@forEach
            canvas.drawText(it, 40f, y, paint)
            y += 18f
        }
        doc.finishPage(page)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String) {
        val authority = context.packageName + ".fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share"))
    }
}