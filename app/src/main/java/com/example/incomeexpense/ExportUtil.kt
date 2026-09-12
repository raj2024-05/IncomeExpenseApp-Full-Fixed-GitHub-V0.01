package com.example.incomeexpense

import android.content.Context
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ExportUtil {
    private val df = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    private fun q(x: String): String {
        return """ + x.replace(""", """") + """
    }

    suspend fun csv(c: Context): File {
        val d = AppDatabase.get(c).dao()
        val f = File(c.cacheDir, "income_expense.csv")
        f.outputStream().bufferedWriter(Charsets.UTF_8).use { o ->
            o.write("\uFEFFType,Date,Category,Account,Amount,Payment Mode,State,Description,Reference,Notes\n")
            d.incomes().first().forEach { x ->
                o.appendLine(listOf("INCOME", df.format(Date(x.date)), x.category, x.account,
                    x.amount, x.paymentMode, x.state, x.description, x.referenceNumber, x.notes)
                    .joinToString(",") { q(it.toString()) })
            }
            d.expenses().first().forEach { x ->
                o.appendLine(listOf("EXPENSE", df.format(Date(x.date)), x.category, x.account,
                    x.amount, x.paymentMode, x.state, x.description, x.referenceNumber, x.notes)
                    .joinToString(",") { q(it.toString()) })
            }
        }
        return f
    }

    suspend fun pdf(c: Context, currency: String): File {
        val d = AppDatabase.get(c).dao()
        val inc = d.incomes().first()
        val exp = d.expenses().first()
        val f = File(c.cacheDir, "financial_report.pdf")
        val doc = android.graphics.pdf.PdfDocument()
        var pageNo = 1
        var page = doc.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNo).create())
        var y = 50f
        val paint = android.graphics.Paint().apply { textSize = 16f }

        fun line(s: String) {
            if (y > 800f) {
                doc.finishPage(page)
                pageNo++
                page = doc.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pageNo).create())
                y = 50f
            }
            page.canvas.drawText(s, 35f, y, paint)
            y += 24f
        }

        val totalIncome = inc.sumOf { it.amount }
        val totalExpense = exp.sumOf { it.amount }
        line("INCOME & EXPENDITURE REPORT")
        line("Generated: " + df.format(Date()))
        line("")
        line("Total Income: $currency%.2f".format(Locale.getDefault(), totalIncome))
        line("Total Expenditure: $currency%.2f".format(Locale.getDefault(), totalExpense))
        line("Net Profit / (Loss): $currency%.2f".format(Locale.getDefault(), totalIncome - totalExpense))
        line("")
        line("ACCOUNT-WISE SUMMARY")

        val allAccounts = (inc.map { it.account } + exp.map { it.account })
            .filter { it.isNotBlank() }.distinct().sorted()

        allAccounts.forEach { ac ->
            val ci = inc.filter { it.account == ac }.sumOf { it.amount }
            val ce = exp.filter { it.account == ac }.sumOf { it.amount }
            line("$ac  Income: $currency%.2f  Expense: $currency%.2f  Balance: $currency%.2f"
                .format(Locale.getDefault(), ci, ce, ci - ce))
        }

        line("")
        line("INCOME")
        inc.forEach {
            line("${df.format(Date(it.date))}  ${it.category}  ${it.account}  $currency%.2f"
                .format(Locale.getDefault(), it.amount))
        }
        line("")
        line("EXPENDITURE")
        exp.forEach {
            line("${df.format(Date(it.date))}  ${it.category}  ${it.account}  $currency%.2f"
                .format(Locale.getDefault(), it.amount))
        }

        doc.finishPage(page)
        f.outputStream().use { doc.writeTo(it) }
        doc.close()
        return f
    }
}
